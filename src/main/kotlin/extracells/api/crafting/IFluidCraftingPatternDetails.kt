package extracells.api.crafting

import appeng.api.networking.crafting.ICraftingPatternDetails
import appeng.api.storage.data.IAEFluidStack

interface IFluidCraftingPatternDetails : ICraftingPatternDetails {
    val condensedFluidInputs: Array<IAEFluidStack?>?
    val fluidInputs: Array<IAEFluidStack?>?
}