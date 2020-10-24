package extracells.api

import net.minecraft.inventory.IInventory
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.IFluidTank

interface IFluidInterface {
    fun getFilter(side: ForgeDirection?): Fluid?
    fun getFluidTank(side: ForgeDirection?): IFluidTank?
    val patternInventory: IInventory
    fun setFilter(side: ForgeDirection?, fluid: Fluid?)
    fun setFluidTank(side: ForgeDirection?, fluid: FluidStack?)
}