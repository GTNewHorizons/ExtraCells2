package extracells.block

import appeng.block.crafting.BlockCraftingStorage
import extracells.Extracells.ModTab
import extracells.tileentity.TileEntityCraftingStorage
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon

class BlockCraftingStorage : BlockCraftingStorage() {
    override fun getUnlocalizedName(`is`: ItemStack): String {
        return getItemUnlocalizedName(`is`)
    }

    override fun getItemUnlocalizedName(`is`: ItemStack): String {
        return super.getUnlocalizedName(`is`)
    }

    override fun registerBlockIcons(ir: IIconRegister) {
        icons[0] = ir.registerIcon("extracells:crafting.storage.256k")
        icons[1] = ir.registerIcon("extracells:crafting.storage.256k.fit")
        icons[2] = ir.registerIcon("extracells:crafting.storage.1024k")
        icons[3] = ir.registerIcon("extracells:crafting.storage.1024k.fit")
        icons[4] = ir.registerIcon("extracells:crafting.storage.4096k")
        icons[5] = ir.registerIcon("extracells:crafting.storage.4096k.fit")
        icons[6] = ir.registerIcon("extracells:crafting.storage.16384k")
        icons[7] = ir.registerIcon("extracells:crafting.storage.16384k.fit")
    }

    override fun getIcon(side: Int, meta: Int): IIcon {
        return when (meta and 4.inv()) {
            0 -> icons[0]!!
            1 -> icons[2]!!
            2 -> icons[4]!!
            3 -> icons[6]!!
            8 -> icons[1]!!
            1 or 8 -> icons[3]!!
            2 or 8 -> icons[5]!!
            3 or 8 -> icons[7]!!
            else -> null
        }
    }

    companion object {
        private val icons = arrayOfNulls<IIcon>(8)
    }

    init {
        setTileEntity(TileEntityCraftingStorage::class.java)
        setCreativeTab(ModTab)
        hasSubtypes = true
        setBlockName("blockCraftingStorage")
    }
}