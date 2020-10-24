package extracells.item

import appeng.api.implementations.ICraftingPatternItem
import appeng.api.networking.crafting.ICraftingPatternDetails
import extracells.crafting.CraftingPattern
import extracells.crafting.CraftingPattern2
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class ItemInternalCraftingPattern : ItemECBase(), ICraftingPatternItem {
    override fun getPatternForItem(`is`: ItemStack, w: World): ICraftingPatternDetails {
        return if (`is` == null || w == null) null else when (`is`.itemDamage) {
            0 -> {
                if (`is`.hasTagCompound() && `is`.tagCompound.hasKey("item")) {
                    val s = ItemStack.loadItemStackFromNBT(`is`.tagCompound.getCompoundTag("item"))
                    if (s != null && s.item is ICraftingPatternItem) return CraftingPattern(
                            (s.item as ICraftingPatternItem).getPatternForItem(s, w))
                }
                null
            }
            1 -> {
                if (`is`.hasTagCompound() && `is`.tagCompound.hasKey("item")) {
                    val s = ItemStack.loadItemStackFromNBT(`is`.tagCompound.getCompoundTag("item"))
                    if (s != null && s.item is ICraftingPatternItem) return CraftingPattern2(
                            (s.item as ICraftingPatternItem).getPatternForItem(s, w))
                }
                null
            }
            else -> null
        }
    }
}