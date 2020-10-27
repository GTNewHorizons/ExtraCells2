package extracells.block

import appeng.api.AEApi
import appeng.api.config.SecurityPermissions
import appeng.api.implementations.items.IAEWrench
import appeng.api.networking.IGridNode
import buildcraft.api.tools.IToolWrench
import extracells.container.ContainerHardMEDrive
import extracells.gui.GuiHardMEDrive
import extracells.network.GuiHandler
import extracells.render.block.RendererHardMEDrive
import extracells.tileentity.TileEntityHardMeDrive
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
import net.minecraft.util.MathHelper
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import java.util.*

object BlockHardMEDrive : BlockEC(Material.rock, 2.0F, 1000000.0F), TGuiBlock {
    var frontIcon: IIcon? = null
    var sideIcon: IIcon? = null
    var bottomIcon: IIcon? = null
    var topIcon: IIcon? = null
    override fun getClientGuiElement(player: EntityPlayer?, world: World, x: Int, y: Int, z: Int): Any? {
        val tile: TileEntity? = world.getTileEntity(x, y, z)
        return when {
            tile == null || player == null -> null
            else -> when (tile) {
                is TileEntityHardMeDrive -> {
                    GuiHardMEDrive(player.inventory, tile)
                }
                else -> {
                    null
                }
            }
        }
    }

    override fun getServerGuiElement(player: EntityPlayer?, world: World, x: Int, y: Int, z: Int): Any? {
        val tile: TileEntity? = world.getTileEntity(x, y, z)
        return when {
            tile == null || player == null -> null
            else -> when (tile) {
                is TileEntityHardMeDrive -> {
                    ContainerHardMEDrive(player.inventory, tile)
                }
                else -> {
                    null
                }
            }
        }
    }

    override fun createNewTileEntity(world: World, meta: Int): TileEntity = TileEntityHardMeDrive()
    override fun breakBlock(world: World, x: Int, y: Int, z: Int, block: Block, par6: Int): Unit {
        dropItems(world, x, y, z)
                super.breakBlock(world, x, y, z, block, par6)
    }

    internal fun dropItems(world: World, x: Int, y: Int, z: Int) {
        val rand = Random()
        val tileEntity: TileEntity = world.getTileEntity(x, y, z)
        if (!(tileEntity is TileEntityHardMeDrive)) {
            return
        }
        val inventory: IInventory = tileEntity.getInventory()

        for (i in 0 until inventory.sizeInventory){
            val item: ItemStack? = inventory.getStackInSlot(i)
            if (item != null && item.stackSize > 0) {
                val rx: Float = rand.nextFloat() * 0.8F + 0.1F
                val ry: Float = rand.nextFloat() * 0.8F + 0.1F
                val rz: Float = rand.nextFloat() * 0.8F + 0.1F
                val entityItem: EntityItem = EntityItem(world, (x + rx).toDouble(), (y + ry).toDouble(), (z + rz).toDouble(), item.copy())
                if (item.hasTagCompound()) {
                    entityItem.entityItem.tagCompound = item.tagCompound.copy() as NBTTagCompound
                }
                val factor: Float = 0.05F
                entityItem.motionX = rand.nextGaussian() * factor
                entityItem.motionY = rand.nextGaussian() * factor + 0.2F
                entityItem.motionZ = rand.nextGaussian() * factor
                world.spawnEntityInWorld(entityItem)
                item.stackSize = 0
            }
        }
    }

    override fun onBlockActivated(world: World, x: Int, y: Int, z: Int, player: EntityPlayer, side: Int, p_149727_7_: Float, p_149727_8_: Float, p_149727_9_: Float): Boolean {
        if (world.isRemote) return false
        val tile: TileEntity = world.getTileEntity(x, y, z)
        if (tile is TileEntityHardMeDrive) if (!PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, tile.getGridNode(ForgeDirection.UNKNOWN))) return false
        val current: ItemStack? = player.inventory.getCurrentItem()
        if (player.isSneaking && current != null) {
            try {
                if (current.item is IToolWrench && (current.item as IToolWrench).canWrench(player, x, y, z)) {
                    dropBlockAsItem(world, x, y, z, ItemStack(this))
                    world.setBlockToAir(x, y, z)
                    (current.item as IToolWrench).wrenchUsed(player, x, y, z)
                    return true
                }
            } catch (e: Throwable) {

            }
            if (current.item is IAEWrench && (current.item as IAEWrench).canWrench(current, player, x, y, z)) {
                dropBlockAsItem(world, x, y, z, ItemStack(this))
                world.setBlockToAir(x, y, z)
                return true
            }
        }
        GuiHandler.launchGui(0, player, world, x, y, z)
        return true
    }

    override fun onBlockPlacedBy(world: World, x: Int, y: Int, z: Int, entity: EntityLivingBase, stack: ItemStack) {
        super.onBlockPlacedBy(world, x, y, z, entity, stack)
        val l: Int = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5) and 3
        if (!entity.isSneaking) {
            if (l == 0) {
                world.setBlockMetadataWithNotify(x, y, z, 2, 2)
            }
            if (l == 1) {
                world.setBlockMetadataWithNotify(x, y, z, 5, 2)
            }
            if (l == 2) {
                world.setBlockMetadataWithNotify(x, y, z, 3, 2)
            }
            if (l == 3) {
                world.setBlockMetadataWithNotify(x, y, z, 4, 2)
            }
        } else {
            if (l == 0) {
                world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(2).opposite.ordinal, 2)
            }
            if (l == 1) {
                world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(5).opposite.ordinal, 2)
            }
            if (l == 2) {
                world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(3).opposite.ordinal, 2)
            }
            if (l == 3) {
                world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(4).opposite.ordinal, 2)
            }
        }
        if (world.isRemote) return
        val tile: TileEntity? = world.getTileEntity(x, y, z)
        if (tile != null) {
            if (tile is TileEntityHardMeDrive) {
                val node: IGridNode? = tile.getGridNode(ForgeDirection.UNKNOWN)
                if (entity is EntityPlayer) {
                    node?.playerID = AEApi.instance().registries().players().getID(entity)
                }
                node?.updateState()
            }
        }
    }

    override fun onBlockPreDestroy(world: World, x: Int, y: Int, z: Int, meta: Int): Unit {
        if (world.isRemote) return
        val tile: TileEntity? = world.getTileEntity(x, y, z)
        if (tile != null) {
            if (tile is TileEntityHardMeDrive) {
                tile.getGridNode(ForgeDirection.UNKNOWN)?.destroy()
            }
        }
    }

    override fun getIcon(side: Int, metadata: Int): IIcon? {
        return if (side == metadata) frontIcon else if (side == 0) bottomIcon else if (side == 1) topIcon else sideIcon
    }

    override fun registerBlockIcons(register: IIconRegister): Unit {
        frontIcon = register.registerIcon("extracells:hardmedrive.face")
        sideIcon = register.registerIcon("extracells:hardmedrive.side")
        bottomIcon = register.registerIcon("extracells:machine.bottom")
        topIcon = register.registerIcon("extracells:machine.top")
    }

    override fun getRenderType(): Int = RendererHardMEDrive.renderId
}