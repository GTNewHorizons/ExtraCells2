package extracells.integration.nei

import codechicken.nei.api.API
import extracells.Extracells
import extracells.registries.BlockEnum
import extracells.registries.ItemEnum
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object Nei {
    private fun hideItems() {
        API.hideItem(ItemStack(ItemEnum.FLUIDITEM.item))
        API.hideItem(ItemStack(ItemEnum.CRAFTINGPATTERN.item))
        for (item: ItemEnum in ItemEnum.values()) {
            if (item.mod != null && !item.mod.isEnabled) {
                val i: Item? = item.item
                val list: ArrayList<ItemStack> = ArrayList<ItemStack>()
                i?.getSubItems(i, Extracells.ModTab, list)
                list.forEach { API.hideItem(it) }
            }
        }
        for (block:BlockEnum in BlockEnum.values()) {
            if (block.mod != null && !block.mod.isEnabled) {
                val b: Block? = block.block
                val list: ArrayList<ItemStack> = ArrayList<ItemStack>()
                b?.getSubBlocks(Item.getItemFromBlock(b), Extracells.ModTab, list)
                list.forEach { API.hideItem(it) }
            }
        }
    }

    fun init() {
        hideItems()
        if (Extracells.proxy.isClient) {
            val handler = UniversalTerminalRecipe()
            API.registerUsageHandler(handler)
            API.registerRecipeHandler(handler)
        }
    }
}