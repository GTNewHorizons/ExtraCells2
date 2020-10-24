package extracells.integration.opencomputers

import appeng.api.AEApi
import cpw.mods.fml.common.registry.GameRegistry
import li.cil.oc.api.API
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

object OCRecipes {
    fun loadRecipes() {
        GameRegistry.addShapedRecipe(ItemStack(GameRegistry.findItem("extracells", "oc.upgrade")), "DAD", "MBM", "DCD",
                'A', AEApi.instance().definitions().materials().wireless().maybeStack(1).get(),
                'C', API.items["printedCircuitBoard"].createItemStack(1),
                'B', API.items["wlanCard"].createItemStack(1),
                'D', Items.diamond,
                'M', API.items["chip3"].createItemStack(1))
        GameRegistry.addShapedRecipe(ItemStack(GameRegistry.findItem("extracells", "oc.upgrade"), 1, 1), "DAD", "MBM",
                "DCD",
                'A', AEApi.instance().definitions().materials().wireless().maybeStack(1).get(),
                'C', API.items["printedCircuitBoard"].createItemStack(1),
                'B', API.items["wlanCard"].createItemStack(1),
                'D', Items.gold_ingot,
                'M', API.items["chip2"].createItemStack(1))
        GameRegistry.addShapedRecipe(ItemStack(GameRegistry.findItem("extracells", "oc.upgrade"), 1, 2), "DAD", "MBM",
                "DCD",
                'A', AEApi.instance().definitions().materials().wireless().maybeStack(1).get(),
                'C', API.items["printedCircuitBoard"].createItemStack(1),
                'B', API.items["wlanCard"].createItemStack(1),
                'D', Items.iron_ingot,
                'M', API.items["chip1"].createItemStack(1))
    }
}