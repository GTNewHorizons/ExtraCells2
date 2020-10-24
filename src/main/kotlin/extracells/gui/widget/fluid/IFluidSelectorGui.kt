package extracells.gui.widget.fluid

import appeng.api.storage.data.IAEFluidStack

interface IFluidSelectorGui : IFluidWidgetGui {
    val container: IFluidSelectorContainer
    val currentFluid: IAEFluidStack?
}