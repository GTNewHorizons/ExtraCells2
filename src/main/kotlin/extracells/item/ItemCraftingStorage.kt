package extracells.item

import appeng.block.AEBaseItemBlock
import net.minecraft.block.Block
import net.minecraft.item.ItemStack

class ItemCraftingStorage(b: Block?) : AEBaseItemBlock(b) {
    override fun getUnlocalizedName(stack: ItemStack): String {
        return String.format("%s.%s", super.getUnlocalizedName(),
                ItemStoragePhysical.Companion.suffixes.get(stack.itemDamage))
    }

    override fun getMetadata(meta: Int): Int {
        return meta
    }
}