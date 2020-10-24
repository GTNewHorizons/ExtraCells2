package extracells.block

import extracells.tileentity.TileEntityWalrus
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.MathHelper
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

class BlockWalrus : BlockEC(Material.clay, 2.0f, 10.0f) {
    override fun createNewTileEntity(world: World, meta: Int): TileEntity {
        return TileEntityWalrus()
    }

    override fun getRenderType(): Int {
        return -1
    }

    override fun getUnlocalizedName(): String {
        return super.getUnlocalizedName().replace("tile.", "")
    }

    override fun isOpaqueCube(): Boolean {
        return false
    }

    override fun onBlockPlacedBy(world: World, x: Int, y: Int, z: Int,
                                 player: EntityLivingBase, itemstack: ItemStack) {
        val l = MathHelper
                .floor_double(player.rotationYaw * 4.0f / 360.0f + 0.5) and 3
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
    }

    override fun renderAsNormalBlock(): Boolean {
        return false
    }

    override fun setBlockBoundsBasedOnState(blockAccess: IBlockAccess, x: Int,
                                            y: Int, z: Int) {
        when (ForgeDirection.getOrientation(blockAccess.getBlockMetadata(x,
                y, z))) {
            ForgeDirection.NORTH -> setBlockBounds(0.0f, 0.0f, -1.0f, 1.0f, 1.0f, 1.0f)
            ForgeDirection.EAST -> setBlockBounds(0.0f, 0.0f, 0.0f, 2.0f, 1.0f, 1.0f)
            ForgeDirection.SOUTH -> setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 2.0f)
            ForgeDirection.WEST -> setBlockBounds(-1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f)
            else -> setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f)
        }
    }
}