package extracells.item

import appeng.api.implementations.items.IStorageComponent
import extracells.integration.Integration
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraft.util.MathHelper
open class ItemStorageComponent : ItemECBase(), IStorageComponent {
    private lateinit var icons: Array<IIcon?>
    val suffixes = arrayOf("physical.256k", "physical.1024k", "physical.4096k", "physical.16384k", "fluid.1k",
            "fluid.4k", "fluid.16k", "fluid.64k", "fluid.256k", "fluid.1024k", "fluid.4096k", "gas.1k", "gas.4k",
            "gas.16k", "gas.64k", "gas.256k", "gas.1024k", "gas.4096k")
    val size = intArrayOf(262144, 1048576, 4194304, 16777216,
            1024, 4096, 16384, 65536, 262144, 1048576, 4194304)

    override fun getBytes(`is`: ItemStack): Int {
        return size[MathHelper.clamp_int(`is`.itemDamage, 0, size.size)]
    }

    override fun getIconFromDamage(dmg: Int): IIcon? {
        val j = MathHelper.clamp_int(dmg, 0, suffixes.size)
        return icons[j]
    }

    override fun getSubItems(item: Item, creativeTab: CreativeTabs, itemList: MutableList<Any?>) {
        for (j in suffixes.indices) {
            if (!(suffixes[j].contains("gas") && !Integration.Mods.MEKANISMGAS.isEnabled)) itemList.add(
                    ItemStack(item, 1, j))
        }
    }

    override fun getUnlocalizedName(itemStack: ItemStack): String {
        return String.format("extracells.item.storage.component.%s", suffixes[itemStack.itemDamage])
    }

    override fun isStorageComponent(`is`: ItemStack): Boolean {
        return `is`.item === this
    }

    override fun registerIcons(iconRegister: IIconRegister) {
        icons = arrayOfNulls(suffixes.size)
        for (i in suffixes.indices) {
            icons[i] = iconRegister.registerIcon(String.format("extracells:storage.component.%s", suffixes[i]))
        }
    }

    init {
        maxDamage = 0
        setHasSubtypes(true)
    }
}