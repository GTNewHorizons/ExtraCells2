package extracells.item

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidContainerRegistry
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
open class ItemFluid : ItemECBase() {
    override fun getItemStackDisplayName(stack: ItemStack): String {
        val fluid = FluidRegistry.getFluid(stack.itemDamage)
        return if (fluid != null) fluid.getLocalizedName(
                FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME)) else "null"
    }
}