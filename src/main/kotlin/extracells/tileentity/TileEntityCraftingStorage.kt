package extracells.tileentity

import appeng.tile.crafting.TileCraftingStorageTile
import appeng.tile.crafting.TileCraftingTile
import extracells.registries.BlockEnum
import net.minecraft.item.ItemStack

class TileEntityCraftingStorage : TileCraftingStorageTile() {
    override fun getItemFromTile(obj: Any): ItemStack? {
        val storage = (obj as TileCraftingTile).storageBytes / KILO_SCALAR
        when (storage) {
            256 -> return ItemStack(BlockEnum.CRAFTINGSTORAGE.block, 1, 0)
            1024 -> return ItemStack(BlockEnum.CRAFTINGSTORAGE.block, 1, 1)
            4096 -> return ItemStack(BlockEnum.CRAFTINGSTORAGE.block, 1, 2)
            16384 -> return ItemStack(BlockEnum.CRAFTINGSTORAGE.block, 1, 3)
        }
        return super.getItemFromTile(obj)
    }

    override fun getStorageBytes(): Int {
        return if (worldObj == null || notLoaded()) 0 else when (blockMetadata and 3) {
            0 -> 256 * KILO_SCALAR
            1 -> 1024 * KILO_SCALAR
            2 -> 4096 * KILO_SCALAR
            3 -> 16384 * KILO_SCALAR
            else -> 0
        }
    }

    companion object {
        private const val KILO_SCALAR = 1024
    }
}