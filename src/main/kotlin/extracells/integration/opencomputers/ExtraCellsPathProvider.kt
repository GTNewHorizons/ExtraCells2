package extracells.integration.opencomputers

import extracells.item.ItemOCUpgrade
import extracells.registries.ItemEnum
import li.cil.oc.api.Manual
import li.cil.oc.api.manual.PathProvider
import li.cil.oc.api.prefab.ItemStackTabIconRenderer
import li.cil.oc.api.prefab.ResourceContentProvider
import net.minecraft.item.ItemStack
import net.minecraft.world.World

object ExtraCellsPathProvider : PathProvider {
    init {
        Manual.addProvider(this)
        Manual.addProvider(ResourceContentProvider("extracells", "doc/"))
        Manual.addTab(ItemStackTabIconRenderer(ItemStack(ItemEnum.FLUIDSTORAGE.item)),"itemGroup.Extra_Cells", "extracells/%LANGUAGE%/index.md")

    }
    override fun pathFor(stack: ItemStack?): String? = if (stack != null && stack.item == ItemOCUpgrade) "extracells/%LANGUAGE%/me_upgrade.md" else null
    override fun pathFor(world: World, x: Int, y: Int, z: Int): String? = null
}