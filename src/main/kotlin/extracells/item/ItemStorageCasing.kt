package extracells.item

import extracells.Extracells.ModTab
import extracells.integration.Integration
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraft.util.MathHelper

class ItemStorageCasing : ItemECBase() {
    private var icons: Array<IIcon>
    val suffixes = arrayOf("physical", "fluid", "gas")
    override fun getIconFromDamage(dmg: Int): IIcon {
        val j = MathHelper.clamp_int(dmg, 0, icons.size - 1)
        return icons[j]
    }

    override fun getSubItems(item: Item, creativeTab: CreativeTabs, itemList: MutableList<*>) {
        for (j in suffixes.indices) {
            if (!(suffixes[j].contains("gas") && !Integration.Mods.MEKANISMGAS.isEnabled)) itemList.add(
                    ItemStack(item, 1, j))
        }
    }

    override fun getUnlocalizedName(itemStack: ItemStack): String {
        return ("extracells.item.storage.casing."
                + suffixes[itemStack.itemDamage])
    }

    override fun registerIcons(iconRegister: IIconRegister) {
        icons = arrayOfNulls(suffixes.size)
        for (i in suffixes.indices) {
            icons[i] = iconRegister.registerIcon("extracells:"
                    + "storage.casing." + suffixes[i])
        }
    }

    init {
        maxDamage = 0
        setHasSubtypes(true)
        creativeTab = ModTab
    }
}