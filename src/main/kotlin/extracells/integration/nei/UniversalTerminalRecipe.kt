package extracells.integration.nei

import codechicken.nei.NEIClientUtils
import codechicken.nei.PositionedStack
import codechicken.nei.api.DefaultOverlayRenderer
import codechicken.nei.api.IOverlayHandler
import codechicken.nei.api.IRecipeOverlayRenderer
import codechicken.nei.api.IStackPositioner
import codechicken.nei.recipe.RecipeInfo
import codechicken.nei.recipe.TemplateRecipeHandler
import extracells.registries.ItemEnum
import extracells.util.UniversalTerminal
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiCrafting
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import java.awt.Rectangle

open class UniversalTerminalRecipe : TemplateRecipeHandler() {

    override fun loadTransferRects(): Unit {
        this.transferRects.add(RecipeTransferRect(Rectangle(84, 23, 24, 18), "crafting"))
    }

    override fun loadCraftingRecipes(outputId: String, vararg results: Any): Unit {
        if (outputId == "crafting") {
            val cachedRecipe: CachedShapelessRecipe = CachedShapelessRecipe(true)
            cachedRecipe.computeVisuals()
            arecipes.add(cachedRecipe)
            val cachedRecipe2: CachedShapelessRecipe = CachedShapelessRecipe(false)
            cachedRecipe2.computeVisuals()
            arecipes.add(cachedRecipe2)
        } else {
            super.loadCraftingRecipes(outputId, *results)
        }
    }

    override fun loadCraftingRecipes(result: ItemStack): Unit {
        if (result != null && result.item == ItemEnum.UNIVERSALTERMINAL.item) {
            val cachedRecipe: CachedShapelessRecipe = CachedShapelessRecipe(true)
            cachedRecipe.computeVisuals()
            arecipes.add(cachedRecipe)
            val cachedRecipe2: CachedShapelessRecipe = CachedShapelessRecipe(false)
            cachedRecipe2.computeVisuals()
            arecipes.add(cachedRecipe2)
        }
    }

    override fun loadUsageRecipes(ingredient: ItemStack?): Unit {
        if (ingredient == null || ingredient.item == null) return
        when {
            UniversalTerminal.isTerminal(ingredient) -> {
                val cachedRecipe: CachedShapelessRecipe = CachedShapelessRecipe(true)
                cachedRecipe.computeVisuals()
                arecipes.add(cachedRecipe)
                val cachedRecipe2: CachedShapelessRecipe = CachedShapelessRecipe(false)
                cachedRecipe2.computeVisuals()
                arecipes.add(cachedRecipe2)
            }
            UniversalTerminal.isWirelessTerminal(ingredient) -> {
                val cachedRecipe: CachedShapelessRecipe = CachedShapelessRecipe(false)
                cachedRecipe.computeVisuals()
                arecipes.add(cachedRecipe)
            }
            ingredient.item == ItemEnum.UNIVERSALTERMINAL.item -> {
                val cachedRecipe: CachedShapelessRecipe = CachedShapelessRecipe(true)
                cachedRecipe.computeVisuals()
                arecipes.add(cachedRecipe)
            }
        }
    }

    override fun getGuiTexture(): String = "textures/gui/container/crafting_table.png"
    override fun getOverlayIdentifier(): String = "crafting"
    override fun getGuiClass() = GuiCrafting::class.java
    override fun hasOverlay(gui: GuiContainer, container: Container, recipe: Int): Boolean = (super.hasOverlay(gui, container, recipe)) || ((this.isRecipe2x2(recipe)) && (RecipeInfo.hasDefaultOverlay(gui, "crafting2x2")))
    override fun getOverlayRenderer(gui: GuiContainer, recipe: Int): IRecipeOverlayRenderer? {
        val renderer: IRecipeOverlayRenderer? = super.getOverlayRenderer(gui, recipe)
        if (renderer != null) {
            return renderer
        }
        val positioner: IStackPositioner = RecipeInfo.getStackPositioner(gui, "crafting2x2") ?: return null
        return DefaultOverlayRenderer(this.getIngredientStacks(recipe), positioner)
    }

    override fun getOverlayHandler(gui: GuiContainer, recipe: Int): IOverlayHandler {
        val handler: IOverlayHandler? = super.getOverlayHandler(gui, recipe)
        if (handler != null) {
            return handler
        }
        return RecipeInfo.getOverlayHandler(gui, "crafting2x2")
    }

    private fun isRecipe2x2(recipe: Int): Boolean {
        for (stack:PositionedStack in this.getIngredientStacks(recipe)) {
            if (stack.relx > 43 || stack.rely > 24) {
                return false
            }
        }
        return true
    }

    override fun getRecipeName(): String = NEIClientUtils.translate("recipe.shapeless")

    inner class CachedShapelessRecipe(private val isUniversal: Boolean) : TemplateRecipeHandler.CachedRecipe() {
        private val ingredients: ArrayList<PositionedStack> = ArrayList()
        private val result: PositionedStack = PositionedStack(ItemEnum.UNIVERSALTERMINAL.getDamagedStack(0), 119, 24)
        override fun getResult(): PositionedStack = this.result
        override fun getIngredients(): List<PositionedStack> {
            return this.getCycledIngredients(cycleticks / 20, this.ingredients)
        }

        fun setIngredients() {
            val stack: PositionedStack = run {
                if (isUniversal)
                    PositionedStack(ItemEnum.UNIVERSALTERMINAL.getDamagedStack(0), 25, 6, false) else PositionedStack(UniversalTerminal.wirelessTerminals, 25, 6, false)
            }
            stack.setMaxSize(1)
            this.ingredients.add(stack)
            val stack2 = PositionedStack(UniversalTerminal.terminals, 43, 6, false)
            stack2.setMaxSize(1)
            this.ingredients.add(stack2)
        }

        fun computeVisuals() {
            for (p:PositionedStack in this.ingredients) {
                p.generatePermutations()
            }
            this.result.generatePermutations()
        }
    }
}