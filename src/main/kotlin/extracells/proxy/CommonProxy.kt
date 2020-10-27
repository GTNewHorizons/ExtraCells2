package extracells.proxy

import appeng.api.AEApi
import appeng.api.recipes.IRecipeLoader
import cpw.mods.fml.common.registry.GameRegistry
import extracells.registries.BlockEnum
import extracells.registries.ItemEnum
import extracells.tileentity.*
import extracells.util.FuelBurnTime.registerFuel
import extracells.util.recipe.RecipeUniversalTerminal
import net.minecraftforge.fluids.FluidRegistry
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

open class CommonProxy {
    private inner class ExternalRecipeLoader : IRecipeLoader {
        @Throws(Exception::class)
        override fun getFile(path: String): BufferedReader {
            return BufferedReader(FileReader(File(path)))
        }
    }

    private inner class InternalRecipeLoader : IRecipeLoader {
        @Throws(Exception::class)
        override fun getFile(path: String): BufferedReader {
            val resourceAsStream = javaClass.getResourceAsStream("/assets/extracells/recipes/$path")
            val reader = InputStreamReader(resourceAsStream, StandardCharsets.UTF_8)
            return BufferedReader(reader)
        }
    }

    fun addRecipes(configFolder: File) {
        val recipeHandler = AEApi.instance().registries().recipes().createNewRecipehandler()
        val externalRecipe = File(
                configFolder.path + File.separator + "AppliedEnergistics2" + File.separator + "extracells.recipe")
        if (externalRecipe.exists()) {
            recipeHandler.parseRecipes(ExternalRecipeLoader(), externalRecipe.path)
        } else {
            recipeHandler.parseRecipes(InternalRecipeLoader(), "main.recipe")
        }
        recipeHandler.injectRecipes()
        GameRegistry.addRecipe(RecipeUniversalTerminal)
    }

    fun registerBlocks() {
        for (current in BlockEnum.values()) {
            GameRegistry.registerBlock(current.block, current.itemBlockClass, current.internalName)
        }
    }

    fun registerItems() {
        for (current in ItemEnum.values()) {
            GameRegistry.registerItem(current.item, current.internalName)
        }
    }

    fun registerMovables() {
        val api = AEApi.instance()
        api.registries().movable().whiteListTileEntity(TileEntityCertusTank::class.java)
        api.registries().movable().whiteListTileEntity(TileEntityWalrus::class.java)
        api.registries().movable().whiteListTileEntity(TileEntityFluidCrafter::class.java)
        api.registries().movable().whiteListTileEntity(TileEntityFluidInterface::class.java)
        api.registries().movable().whiteListTileEntity(TileEntityFluidFiller::class.java)
        api.registries().movable().whiteListTileEntity(TileEntityHardMeDrive::class.java)
        api.registries().movable().whiteListTileEntity(TileEntityVibrationChamberFluid::class.java)
        api.registries().movable().whiteListTileEntity(TileEntityCraftingStorage::class.java)
    }

    open fun registerRenderers() {
        // Only Clientside
    }

    fun registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityCertusTank::class.java, "tileEntityCertusTank")
        GameRegistry.registerTileEntity(TileEntityWalrus::class.java, "tileEntityWalrus")
        GameRegistry.registerTileEntity(TileEntityFluidCrafter::class.java, "tileEntityFluidCrafter")
        GameRegistry.registerTileEntity(TileEntityFluidInterface::class.java, "tileEntityFluidInterface")
        GameRegistry.registerTileEntity(TileEntityFluidFiller::class.java, "tileEntityFluidFiller")
        GameRegistry.registerTileEntity(TileEntityHardMeDrive::class.java, "tileEntityHardMEDrive")
        GameRegistry.registerTileEntity(TileEntityVibrationChamberFluid::class.java, "tileEntityVibrationChamberFluid")
        GameRegistry.registerTileEntity(TileEntityCraftingStorage::class.java, "tileEntityCraftingStorage")
    }

    fun registerFluidBurnTimes() {
        registerFuel(FluidRegistry.LAVA, 800)
    }

    open val isClient: Boolean
        get() = false
    open val isServer: Boolean
        get() = true
}