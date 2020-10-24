package extracells.block

import appeng.api.AEApi
import appeng.api.config.SecurityPermissions
import appeng.api.implementations.items.IAEWrench
import buildcraft.api.tools.IToolWrench
import extracells.network.GuiHandler.launchGui
import extracells.tileentity.TileEntityFluidCrafter
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
import net.minecraft.util.StatCollector
import net.minecraft.world.World
import java.util.*

class BlockFluidCrafter : BlockEC(Material.iron, 2.0f, 10.0f) {
    var icon: IIcon? = null
    override fun breakBlock(world: World, x: Int, y: Int, z: Int, par5: Block,
                            par6: Int) {
        dropItems(world, x, y, z)
        super.breakBlock(world, x, y, z, par5, par6)
    }

    override fun createNewTileEntity(world: World, meta: Int): TileEntity {
        return TileEntityFluidCrafter()
    }

    private fun dropItems(world: World, x: Int, y: Int, z: Int) {
        val rand = Random()
        val tileEntity = world.getTileEntity(x, y, z) as? TileEntityFluidCrafter ?: return
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

    override fun getIcon(side: Int, b: Int): IIcon {
        return icon!!
    }

    override fun getLocalizedName(): String {
        return StatCollector.translateToLocal("$unlocalizedName.name")
    }

    override fun getUnlocalizedName(): String {
        return super.getUnlocalizedName().replace("tile.", "")
    }

    override fun onBlockActivated(world: World, x: Int, y: Int, z: Int,
                                  player: EntityPlayer, side: Int, p_149727_7_: Float,
                                  p_149727_8_: Float, p_149727_9_: Float): Boolean {
        if (world.isRemote) return false
        val tile = world.getTileEntity(x, y, z)
        if (tile is TileEntityFluidCrafter) if (!PermissionUtil.hasPermission(player,
                        SecurityPermissions.BUILD,
                        tile.gridNode)) return false
        val current = player.inventory.getCurrentItem()
        if (player.isSneaking && current != null) {
            try {
                if (current.item is IToolWrench
                        && (current.item as IToolWrench).canWrench(player,
                                x, y, z)) {
                    dropBlockAsItem(world, x, y, z, ItemStack(this))
                    world.setBlockToAir(x, y, z)
                    (current.item as IToolWrench).wrenchUsed(player, x, y,
                            z)
                    return true
                }
            } catch (e: Throwable) {
                // No IToolWrench
            }
            if (current.item is IAEWrench
                    && (current.item as IAEWrench).canWrench(current,
                            player, x, y, z)) {
                dropBlockAsItem(world, x, y, z, ItemStack(this))
                world.setBlockToAir(x, y, z)
                return true
            }
        }
        launchGui(0, player, world, x, y, z)
        return true
    }

    override fun onBlockPlacedBy(world: World, x: Int, y: Int, z: Int,
                                 entity: EntityLivingBase, stack: ItemStack) {
        if (world.isRemote) return
        val tile = world.getTileEntity(x, y, z)
        if (tile != null) {
            if (tile is TileEntityFluidCrafter) {
                val node = tile.gridNode
                if (entity != null && entity is EntityPlayer) {
                    node!!.playerID = AEApi.instance().registries().players()
                            .getID(entity)
                }
                node!!.updateState()
            }
        }
    }

    override fun onBlockPreDestroy(world: World, x: Int, y: Int, z: Int, meta: Int) {
        if (world.isRemote) return
        val tile = world.getTileEntity(x, y, z)
        if (tile != null) {
            if (tile is TileEntityFluidCrafter) {
                val node = tile.gridNode
                node?.destroy()
            }
        }
    }

    override fun registerBlockIcons(iconregister: IIconRegister) {
        icon = iconregister.registerIcon("extracells:fluid.crafter")
    }
}