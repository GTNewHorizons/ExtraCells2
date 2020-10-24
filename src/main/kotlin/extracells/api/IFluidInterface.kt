package extracells.api;

import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public interface IFluidInterface {

	Fluid getFilter(ForgeDirection side);

	IFluidTank getFluidTank(ForgeDirection side);

	IInventory getPatternInventory();

	void setFilter(ForgeDirection side, Fluid fluid);

	void setFluidTank(ForgeDirection side, FluidStack fluid);

}
