package extracells.gui.widget.fluid

import net.minecraftforge.fluids.Fluid

interface IFluidSelectorContainer {
    fun setSelectedFluid(_fluid: Fluid?)
}