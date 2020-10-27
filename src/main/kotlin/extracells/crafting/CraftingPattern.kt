package extracells.crafting

import appeng.api.AEApi
import appeng.api.networking.crafting.ICraftingPatternDetails
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IAEItemStack
import extracells.api.crafting.IFluidCraftingPatternDetails
import extracells.registries.ItemEnum
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidContainerRegistry
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.IFluidContainerItem

open class CraftingPattern(val pattern: ICraftingPatternDetails?) : IFluidCraftingPatternDetails, Comparable<CraftingPattern> {
    private var fluidsCondensed: Array<IAEFluidStack?>? = null
    private var fluids: Array<IAEFluidStack?>? = null
    override fun canSubstitute(): Boolean {
        return pattern!!.canSubstitute()
    }

    fun compareInt(int1: Int, int2: Int): Int {
        if (int1 == int2) return 0
        return if (int1 < int2) -1 else 1
    }

    override fun compareTo(other: CraftingPattern): Int {
        return compareInt(other.priority, this.priority)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (this.javaClass != obj.javaClass) return false
        val other = obj as CraftingPattern
        return if (pattern != null && other.pattern != null) pattern == other.pattern else false
    }

    override val condensedFluidInputs: Array<IAEFluidStack?>?
        get() {
            if (fluidsCondensed == null) {
                condensedInputs
            }
            return fluidsCondensed
        }

    override fun getCondensedInputs(): Array<IAEItemStack?>? {
        return removeFluidContainers(pattern!!.condensedInputs, true)
    }

    override fun getCondensedOutputs(): Array<IAEItemStack?>? {
        return pattern?.condensedOutputs
    }

    override val fluidInputs: Array<IAEFluidStack?>?
        get() {
            if (fluids == null) {
                inputs
            }
            return fluids
        }

    override fun getInputs(): Array<IAEItemStack?>? {
        return removeFluidContainers(pattern!!.inputs, false)
    }

    override fun getOutput(craftingInv: InventoryCrafting, world: World): ItemStack? {
        val input = pattern!!.inputs
        for (i in input.indices) {
            val stack = input[i]
            if (stack != null
                    && FluidContainerRegistry.isFilledContainer(stack
                            .itemStack)) {
                try {
                    craftingInv.setInventorySlotContents(i,
                            input[i].itemStack)
                } catch (e: Throwable) {
                }
            } else if (stack != null
                    && stack.item is IFluidContainerItem) {
                try {
                    craftingInv.setInventorySlotContents(i,
                            input[i].itemStack)
                } catch (e: Throwable) {
                }
            }
        }
        val returnStack = pattern.getOutput(craftingInv, world)
        for (i in input.indices) {
            val stack = input[i]
            if (stack != null
                    && FluidContainerRegistry.isFilledContainer(stack
                            .itemStack)) {
                craftingInv.setInventorySlotContents(i, null)
            } else if (stack != null
                    && stack.item is IFluidContainerItem) {
                craftingInv.setInventorySlotContents(i, null)
            }
        }
        return returnStack
    }

    override fun getOutputs(): Array<IAEItemStack?>? {
        return pattern?.outputs
    }

    override fun getPattern(): ItemStack? {
        val p = pattern!!.pattern ?: return null
        val s = ItemStack(ItemEnum.CRAFTINGPATTERN.item)
        val tag = NBTTagCompound()
        tag.setTag("item", p.writeToNBT(NBTTagCompound()))
        s.tagCompound = tag
        return s
    }

    override fun getPriority(): Int {
        return pattern!!.priority
    }

    override fun isCraftable(): Boolean {
        return pattern!!.isCraftable
    }

    override fun isValidItemForSlot(slotIndex: Int, itemStack: ItemStack,
                                    world: World): Boolean {
        return pattern!!.isValidItemForSlot(slotIndex, itemStack, world)
    }

    fun removeFluidContainers(requirements: Array<IAEItemStack?>,
                              isCondenced: Boolean): Array<IAEItemStack?>? {
        var returnStack = arrayOfNulls<IAEItemStack>(requirements.size)
        val fluidStacks = arrayOfNulls<IAEFluidStack>(requirements.size)
        var removed = 0
        var i = 0
        for (currentRequirement in requirements) {
            if (currentRequirement != null) {
                val current = currentRequirement.itemStack
                current.stackSize = 1
                var fluid: FluidStack? = null
                if (FluidContainerRegistry.isFilledContainer(current)) {
                    fluid = FluidContainerRegistry
                            .getFluidForFilledItem(current)
                } else if (currentRequirement.item is IFluidContainerItem) {
                    fluid = (currentRequirement.item as IFluidContainerItem)
                            .getFluid(current)
                }
                if (fluid == null) {
                    returnStack[i] = currentRequirement
                } else {
                    removed++
                    fluidStacks[i] = AEApi
                            .instance()
                            .storage()
                            .createFluidStack(
                                    FluidStack(
                                            fluid.getFluid(),
                                            (fluid.amount * currentRequirement
                                                    .stackSize).toInt()))
                }
            }
            i++
        }
        if (isCondenced) {
            var i2 = 0
            val fluids = arrayOfNulls<IAEFluidStack>(removed)
            for (fluid in fluidStacks) {
                if (fluid != null) {
                    fluids[i2] = fluid
                    i2++
                }
            }
            var i3 = 0
            val items = arrayOfNulls<IAEItemStack>(requirements.size
                    - removed)
            for (item in returnStack) {
                if (item != null) {
                    items[i3] = item
                    i3++
                }
            }
            returnStack = items
            fluidsCondensed = fluids
        } else {
            fluids = fluidStacks
        }
        return returnStack
    }

    override fun setPriority(priority: Int) {
        pattern!!.priority = priority
    }

    override fun hashCode(): Int {
        return pattern.hashCode()
    }
}