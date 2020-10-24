package extracells.util

import appeng.api.AEApi
import appeng.api.storage.data.IAEFluidStack
import extracells.item.ItemFluidPattern
import extracells.registries.ItemEnum
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.*
import org.apache.commons.lang3.tuple.MutablePair

object FluidUtil {
    fun createAEFluidStack(fluid: Fluid?): IAEFluidStack {
        return createAEFluidStack(FluidStack(fluid,
                FluidContainerRegistry.BUCKET_VOLUME))
    }

    fun createAEFluidStack(fluid: Fluid?, amount: Long): IAEFluidStack {
        return createAEFluidStack(fluid!!.id, amount)
    }

    fun createAEFluidStack(fluid: FluidStack?): IAEFluidStack {
        return AEApi.instance().storage().createFluidStack(fluid)
    }

    fun createAEFluidStack(fluidId: Int, amount: Long): IAEFluidStack {
        return createAEFluidStack(FluidStack(FluidRegistry.getFluid(fluidId), 1)).setStackSize(
                amount)
    }

    fun drainStack(
            itemStack: ItemStack?, fluid: FluidStack?): MutablePair<Int, ItemStack>? {
        if (itemStack == null) return null
        val item = itemStack.item
        if (item is IFluidContainerItem) {
            val drained = (item as IFluidContainerItem).drain(itemStack,
                    fluid!!.amount, true)
            val amountDrained = if (drained != null
                    && drained.getFluid() === fluid.getFluid()) drained.amount else 0
            return MutablePair(amountDrained, itemStack)
        } else if (FluidContainerRegistry.isContainer(itemStack)) {
            val content = FluidContainerRegistry
                    .getFluidForFilledItem(itemStack)
            val amountDrained = if (content != null
                    && content.getFluid() === fluid!!.getFluid()) content.amount else 0
            return MutablePair(amountDrained, itemStack
                    .item.getContainerItem(itemStack))
        }
        return null
    }

    fun fillStack(
            itemStack: ItemStack?, fluid: FluidStack): MutablePair<Int, ItemStack>? {
        if (itemStack == null) return null
        val item = itemStack.item
        //If its a fluid container item instance
        if (item is IFluidContainerItem) {
            //Call the fill method on it.
            val filled = (item as IFluidContainerItem).fill(itemStack, fluid,
                    true)

            //Return the filled itemstack.
            return MutablePair(filled, itemStack)
        } else if (FluidContainerRegistry.isContainer(itemStack)) {
            //Fill it through the fluidcontainer registry.
            val filledContainer = FluidContainerRegistry
                    .fillFluidContainer(fluid, itemStack)
            //get the filled fluidstack.
            val filled = FluidContainerRegistry
                    .getFluidForFilledItem(filledContainer)
            //Return filled container and fill amount.
            return MutablePair(
                    filled?.amount ?: 0, filledContainer)
        } else if (item === ItemEnum.FLUIDPATTERN.item) {
            return MutablePair(0,
                    ItemFluidPattern.Companion.getPatternForFluid(fluid.getFluid()))
        }
        return null
    }

    fun getCapacity(itemStack: ItemStack?): Int {
        if (itemStack == null) return 0
        val item = itemStack.item
        if (item is IFluidContainerItem) {
            return (item as IFluidContainerItem).getCapacity(itemStack)
        } else if (FluidContainerRegistry.isEmptyContainer(itemStack)) {
            for (data in FluidContainerRegistry
                    .getRegisteredFluidContainerData()) {
                if (data != null && data.emptyContainer.isItemEqual(itemStack)) {
                    val interior = data.fluid
                    return interior?.amount ?: 0
                }
            }
        }
        return 0
    }

    fun getFluidFromContainer(itemStack: ItemStack?): FluidStack? {
        if (itemStack == null) return null
        val container = itemStack.copy()
        val item = container.item
        return if (item is IFluidContainerItem) {
            (item as IFluidContainerItem).getFluid(container)
        } else if (item === ItemEnum.FLUIDPATTERN.item) {
            FluidStack(ItemFluidPattern.Companion.getFluid(itemStack), 0)
        } else {
            FluidContainerRegistry.getFluidForFilledItem(container)
        }
    }

    fun isEmpty(itemStack: ItemStack?): Boolean {
        if (itemStack == null) return false
        val item = itemStack.item
        if (item is IFluidContainerItem) {
            val content = (item as IFluidContainerItem)
                    .getFluid(itemStack)
            return content == null || content.amount <= 0
        }
        return (item === ItemEnum.FLUIDPATTERN.item
                || FluidContainerRegistry.isEmptyContainer(itemStack))
    }

    fun isFilled(itemStack: ItemStack?): Boolean {
        if (itemStack == null) return false
        val item = itemStack.item
        if (item is IFluidContainerItem) {
            val content = (item as IFluidContainerItem)
                    .getFluid(itemStack)
            return content != null && content.amount > 0
        }
        return FluidContainerRegistry.isFilledContainer(itemStack)
    }

    fun isFluidContainer(itemStack: ItemStack?): Boolean {
        if (itemStack == null) return false
        val item = itemStack.item
        return (item is IFluidContainerItem
                || item === ItemEnum.FLUIDPATTERN.item || FluidContainerRegistry.isContainer(itemStack))
    }
}