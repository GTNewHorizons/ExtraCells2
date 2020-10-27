package extracells.block

import appeng.api.AEApi
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.container.ContainerVibrationChamberFluid
import extracells.gui.GuiVibrationChamberFluid
import extracells.network.GuiHandler.launchGui
import extracells.tileentity.TileEntityVibrationChamberFluid
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import net.minecraft.util.MathHelper
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
open class BlockVibrationChamberFluid : BlockEC(Material.iron, 2.0f, 10.0f), TGuiBlock {
    private val icons = arrayOfNulls<IIcon>(3)
    override fun onBlockActivated(world: World, x: Int, y: Int, z: Int, player: EntityPlayer, side: Int, p_149727_7_: Float, p_149727_8_: Float, p_149727_9_: Float): Boolean {
        if (world.isRemote) return false
        launchGui(0, player, world, x, y, z)
        return true
    }

    override fun createNewTileEntity(world: World, meta: Int): TileEntity {
        return TileEntityVibrationChamberFluid()
    }

    @SideOnly(Side.CLIENT)
    override fun registerBlockIcons(IIconRegister: IIconRegister) {
        icons[0] = IIconRegister.registerIcon("extracells:VibrationChamberFluid")
        icons[1] = IIconRegister.registerIcon("extracells:VibrationChamberFluidFront")
        icons[2] = IIconRegister.registerIcon("extracells:VibrationChamberFluidFrontOn")
    }

    @SideOnly(Side.CLIENT)
    override fun getIcon(world: IBlockAccess, x: Int, y: Int, z: Int, side: Int): IIcon? {
        return if (side == world.getBlockMetadata(x, y, z)) {
            val tile = world.getTileEntity(x, y, z) as? TileEntityVibrationChamberFluid ?: return icons[0]!!
            val chamberFluid = tile
            if (chamberFluid.getBurnTime() > 0 && chamberFluid.getBurnTime() < chamberFluid.getBurnTimeTotal()) icons[2]!! else icons[1]!!
        } else icons[0]!!
    }

    @SideOnly(Side.CLIENT)
    override fun getIcon(side: Int, meta: Int): IIcon? {
        return when (side) {
            4 -> icons[1]!!
            else -> icons[0]!!
        }
    }

    @SideOnly(Side.CLIENT)
    override fun getClientGuiElement(player: EntityPlayer?, world: World, x: Int, y: Int, z: Int): Any? {
        val tileEntity = world.getTileEntity(x, y, z)
        return if (tileEntity != null && tileEntity is TileEntityVibrationChamberFluid) GuiVibrationChamberFluid(player,
                tileEntity) else null
    }

    override fun getServerGuiElement(player: EntityPlayer?, world: World, x: Int, y: Int, z: Int): Any? {
        val tileEntity = world.getTileEntity(x, y, z)
        return if (tileEntity != null && tileEntity is TileEntityVibrationChamberFluid) ContainerVibrationChamberFluid(
                player!!.inventory, tileEntity) else null
    }

    override fun onBlockPlacedBy(world: World?, x: Int, y: Int, z: Int, entity: EntityLivingBase?, stack: ItemStack?) {
        super.onBlockPlacedBy(world, x, y, z, entity, stack)
        if (world == null) return
        if (entity != null) {
            val l = MathHelper.floor_double(entity.rotationYaw * 4.0f / 360.0f + 0.5) and 3
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
        } else world.setBlockMetadataWithNotify(x, y, z, 2, 2)
        if (world.isRemote) return
        val tile = world.getTileEntity(x, y, z)
        if (tile != null) {
            if (tile is TileEntityVibrationChamberFluid) {
                val node = tile.getGridNodeWithoutUpdate()
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
            if (tile is TileEntityVibrationChamberFluid) {
                val node = tile.getGridNode(ForgeDirection.UNKNOWN)
                node?.destroy()
            }
        }
    }
}