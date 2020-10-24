package extracells.gui.widget

import extracells.gui.widget.fluid.AbstractFluidWidget
import net.minecraftforge.fluids.FluidStack
import java.util.*

class FluidWidgetComparator : Comparator<AbstractFluidWidget> {
    override fun compare(o1: AbstractFluidWidget, o2: AbstractFluidWidget): Int {
        return o1.fluid.getLocalizedName(FluidStack(o1.fluid, 0))
                .compareTo(o2.fluid.getLocalizedName(FluidStack(o1.fluid, 0)))
    }
}