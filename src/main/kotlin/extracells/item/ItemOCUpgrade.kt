package extracells.item

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.integration.opencomputers.UpgradeItemAEBase

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object ItemOCUpgrade : UpgradeItemAEBase() {

    init {
        setTextureName("extracells:upgrade.oc")
    }

    override fun getUnlocalizedName(): String = super.getUnlocalizedName().replace("item.extracells", "extracells.item")
    override fun getUnlocalizedName(stack: ItemStack): String = unlocalizedName

    @SideOnly(Side.CLIENT)
    override fun getSubItems(item: Item, tab: CreativeTabs, list: MutableList<Any?>) {
        list.add(ItemStack(item, 1, 2))
        list.add(ItemStack(item, 1, 1))
        list.add(ItemStack(item, 1, 0))
    }

    override fun getItemStackDisplayName(stack: ItemStack): String {
        val tier: Int =
            when (stack.itemDamage) {
                0 -> {
                    3
                }
                1 -> {
                    2
                }
                else -> {
                    1
                }
            }
        return super.getItemStackDisplayName(stack) + " (Tier " + tier + ")"
    }
}