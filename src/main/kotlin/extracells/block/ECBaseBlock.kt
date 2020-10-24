package extracells.block

import appeng.api.AEApi
import appeng.api.config.SecurityPermissions
import appeng.api.implementations.items.IAEWrench
import buildcraft.api.tools.IToolWrench
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.api.IECTileEntity
import extracells.network.GuiHandler.launchGui
import extracells.tileentity.IListenerTile
import extracells.tileentity.TileEntityFluidFiller
import extracells.tileentity.TileEntityFluidInterface
import extracells.util.PermissionUtil
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import java.util.*

class ECBaseBlock : BlockEC(Material.iron, 2.0f, 10.0f) {
    private val icons = arrayOfNulls<IIcon>(2)
    override fun breakBlock(world: World, x: Int, y: Int, z: Int, par5: Block,
                            par6: Int) {
        dropPatter(world, x, y, z)
        super.breakBlock(world, x, y, z, par5, par6)
    }

    override fun createNewTileEntity(world: World, meta: Int): TileEntity {
        return when (meta) {
            0 -> TileEntityFluidInterface()
            1 -> TileEntityFluidFiller()
            else -> null
        }
    }

    override fun damageDropped(p_149692_1_: Int): Int {
        return p_149692_1_
    }

    private fun dropPatter(world: World, x: Int, y: Int, z: Int) {
        val rand = Random()
        val tileEntity = world.getTileEntity(x, y, z) as? TileEntityFluidInterface ?: return
        val inventory: IInventory? = tileEntity.inventory
        for (i in 0 until inventory!!.sizeInventory) {
            val item = inventory.getStackInSlot(i)
            if (item != null && item.stackSize > 0) {
                val rx = rand.nextFloat() * 0.8f + 0.1f
                val ry = rand.nextFloat() * 0.8f + 0.1f
                val rz = rand.nextFloat() * 0.8f + 0.1f
                val entityItem = EntityItem(world, (x + rx).toDouble(), (y + ry).toDouble(), (z
                        + rz).toDouble(), item.copy())
                if (item.hasTagCompound()) {
                    entityItem.entityItem.tagCompound = item.tagCompound.copy() as NBTTagCompound
                }
                val factor = 0.05f
                entityItem.motionX = rand.nextGaussian() * factor
                entityItem.motionY = rand.nextGaussian() * factor + 0.2f
                entityItem.motionZ = rand.nextGaussian() * factor
                world.spawnEntityInWorld(entityItem)
                item.stackSize = 0
            }
        }
    }

    @SideOnly(Side.CLIENT)
    override fun getIcon(side: Int, meta: Int): IIcon {
        return if (meta >= 0 && meta + 1 <= icons.size) {
            icons[meta]!!
        } else null
    }

    override fun onBlockActivated(world: World, x: Int, y: Int, z: Int,
                                  player: EntityPlayer, side: Int, p_149727_7_: Float,
                                  p_149727_8_: Float, p_149727_9_: Float): Boolean {
        if (world.isRemote) return false
        val rand = Random()
        return when (world.getBlockMetadata(x, y, z)) {
            0, 1 -> {
                val tile = world.getTileEntity(x, y, z)
                if (tile is IECTileEntity) if (!PermissionUtil.hasPermission(player,
                                SecurityPermissions.BUILD, (tile as IECTileEntity)
                                .getGridNode(ForgeDirection.UNKNOWN))) return false
                val current = player.currentEquippedItem
                if (player.isSneaking && current != null) {
                    try {
                        if (current.item is IToolWrench
                                && (current.item as IToolWrench).canWrench(
                                        player, x, y, z)) {
                            val block = ItemStack(this, 1,
                                    world.getBlockMetadata(x, y, z))
                            if (tile != null
                                    && tile is TileEntityFluidInterface) {
                                block.tagCompound = tile
                                        .writeFilter(NBTTagCompound())
                            }
                            dropBlockAsItem(world, x, y, z, block)
                            world.setBlockToAir(x, y, z)
                            (current.item as IToolWrench).wrenchUsed(player, x,
                                    y, z)
                            return true
                        }
                    } catch (e: Throwable) {
                        // No IToolWrench
                    }
                    if (current.item is IAEWrench
                            && (current.item as IAEWrench).canWrench(current,
                                    player, x, y, z)) {
                        val block = ItemStack(this, 1,
                                world.getBlockMetadata(x, y, z))
                        if (tile != null
                                && tile is TileEntityFluidInterface) {
                            block.tagCompound = tile
                                    .writeFilter(NBTTagCompound())
                        }
                        dropBlockAsItem(world, x, y, z, block)
                        world.setBlockToAir(x, y, z)
                        return true
                    }
                }
                launchGui(0, player, world, x, y, z)
                true
            }
            else -> false
        }
    }

    override fun onBlockPlacedBy(world: World, x: Int, y: Int, z: Int,
                                 entity: EntityLivingBase, stack: ItemStack) {
        if (world.isRemote) return
        when (world.getBlockMetadata(x, y, z)) {
            0, 1 -> {
                val tile = world.getTileEntity(x, y, z)
                if (tile != null) {
                    if (tile is IECTileEntity) {
                        val node = (tile as IECTileEntity)
                                .getGridNode(ForgeDirection.UNKNOWN)
                        if (entity != null && entity is EntityPlayer) {
                            node.playerID = AEApi.instance().registries()
                                    .players().getID(entity)
                        }
                        node.updateState()
                    }
                    if (tile is IListenerTile) (tile as IListenerTile).registerListener()
                }
                return
            }
            else -> return
        }
    }

    override fun onBlockPreDestroy(world: World, x: Int, y: Int, z: Int, meta: Int) {
        if (world.isRemote) return
        when (meta) {
            0, 1 -> {
                val tile = world.getTileEntity(x, y, z)
                if (tile != null) {
                    if (tile is IECTileEntity) {
                        val node = (tile as IECTileEntity)
                                .getGridNode(ForgeDirection.UNKNOWN)
                        node?.destroy()
                    }
                    if (tile is IListenerTile) (tile as IListenerTile).removeListener()
                }
                return
            }
            else -> return
        }
    }

    @SideOnly(Side.CLIENT)
    override fun registerBlockIcons(register: IIconRegister) {
        icons[0] = register.registerIcon("extracells:fluid.interface")
        icons[1] = register.registerIcon("extracells:fluid.filler")
    }
}