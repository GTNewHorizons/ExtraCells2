package extracells.inventory

import appeng.api.AEApi
import appeng.api.config.AccessRestriction
import appeng.api.config.Actionable
import appeng.api.networking.security.BaseActionSource
import appeng.api.storage.IMEInventoryHandler
import appeng.api.storage.ISaveProvider
import appeng.api.storage.StorageChannel
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IItemList
import com.google.common.collect.Lists
import extracells.api.ECApi
import extracells.api.IFluidStorageCell
import extracells.api.IHandlerFluidStorage
import extracells.container.ContainerFluidStorage
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import java.util.*

open class HandlerItemStorageFluid(_storageStack: ItemStack,
                                   _saveProvider: ISaveProvider?) : IMEInventoryHandler<IAEFluidStack?>, IHandlerFluidStorage {
    private val stackTag: NBTTagCompound
    protected var fluidStacks = ArrayList<FluidStack?>()
    private var prioritizedFluids = ArrayList<Fluid?>()
    private val totalTypes: Int
    private val totalBytes: Int
    private val containers: List<ContainerFluidStorage> = ArrayList()
    private val saveProvider: ISaveProvider?

    constructor(_storageStack: ItemStack, _saveProvider: ISaveProvider?, _filter: ArrayList<Fluid?>?) : this(
            _storageStack, _saveProvider) {
        if (_filter != null) prioritizedFluids = _filter
    }

    private fun allowedByFormat(fluid: Fluid): Boolean {
        return !isFormatted || prioritizedFluids.contains(fluid)
    }

    override fun canAccept(input: IAEFluidStack?): Boolean {
        if (input == null) return false
        if (!ECApi.instance()!!.canStoreFluid(input.fluid)) return false
        for (fluidStack in fluidStacks) {
            if (fluidStack == null || fluidStack.getFluid() === input.fluid) return allowedByFormat(input.fluid)
        }
        return false
    }

    override fun extractItems(request: IAEFluidStack?, mode: Actionable?, src: BaseActionSource?): IAEFluidStack? {
        if (request == null || !allowedByFormat(request.fluid)) return null
        val removedStack: IAEFluidStack?
        val currentFluids: MutableList<FluidStack?> = Lists.newArrayList(fluidStacks)
        for (i in fluidStacks.indices) {
            val currentStack = fluidStacks[i]
            if (currentStack != null && currentStack.fluidID == request.fluid.id) {
                val endAmount = currentStack.amount - request.stackSize
                if (endAmount >= 0) {
                    removedStack = request.copy()
                    val toWrite = FluidStack(currentStack.getFluid(), endAmount.toInt())
                    currentFluids[i] = toWrite
                    if (mode == Actionable.MODULATE) {
                        writeFluidToSlot(i, toWrite)
                    }
                } else {
                    removedStack = AEApi.instance().storage().createFluidStack(currentStack.copy())
                    if (mode == Actionable.MODULATE) {
                        writeFluidToSlot(i, null)
                    }
                }
                if (removedStack != null && removedStack.stackSize > 0) requestSave()
                return removedStack
            }
        }
        return null
    }

    fun freeBytes(): Int {
        var i = 0
        for (stack in fluidStacks) if (stack != null) i += stack.amount
        return totalBytes - i
    }

    override fun getAccess(): AccessRestriction {
        return AccessRestriction.READ_WRITE
    }

    override fun getAvailableItems(out: IItemList<IAEFluidStack?>?): IItemList<IAEFluidStack?>? {
        for (fluidStack in fluidStacks)
            if (fluidStack != null)
                out?.add(AEApi.instance().storage().createFluidStack(fluidStack))
        return out
    }

    override fun getChannel(): StorageChannel {
        return StorageChannel.FLUIDS
    }

    override fun getPriority(): Int {
        return 0
    }

    override fun getSlot(): Int {
        return 0
    }

    override fun injectItems(input: IAEFluidStack?, mode: Actionable?,
                             src: BaseActionSource?): IAEFluidStack? {
        if (input == null || !allowedByFormat(input.fluid)) return input
        var notAdded = input.copy()
        val currentFluids: MutableList<FluidStack?> = Lists.newArrayList(fluidStacks)
        for (i in currentFluids.indices) {
            val currentStack = currentFluids[i]
            if (notAdded != null && currentStack != null && input.fluid === currentStack.getFluid()) {
                if (notAdded.stackSize <= freeBytes()) {
                    val toWrite = FluidStack(currentStack.getFluid(),
                            currentStack.amount + notAdded.stackSize.toInt())
                    currentFluids[i] = toWrite
                    if (mode == Actionable.MODULATE) {
                        writeFluidToSlot(i, toWrite)
                    }
                    notAdded = null
                } else {
                    val toWrite = FluidStack(currentStack.getFluid(), currentStack.amount + freeBytes())
                    currentFluids[i] = toWrite
                    if (mode == Actionable.MODULATE) {
                        writeFluidToSlot(i, toWrite)
                    }
                    notAdded.stackSize = notAdded.stackSize - freeBytes()
                }
            }
        }
        for (i in currentFluids.indices) {
            val currentStack = currentFluids[i]
            if (notAdded != null && currentStack == null) {
                if (input.stackSize <= freeBytes()) {
                    val toWrite = notAdded.fluidStack
                    currentFluids[i] = toWrite
                    if (mode == Actionable.MODULATE) {
                        writeFluidToSlot(i, toWrite)
                    }
                    notAdded = null
                } else {
                    val toWrite = FluidStack(notAdded.fluid, freeBytes())
                    currentFluids[i] = toWrite
                    if (mode == Actionable.MODULATE) {
                        writeFluidToSlot(i, toWrite)
                    }
                    notAdded.stackSize = notAdded.stackSize - freeBytes()
                }
            }
        }
        if (notAdded == null || notAdded != input) requestSave()
        return notAdded
    }

    // Common case
    override val isFormatted: Boolean
        get() {
            // Common case
            if (prioritizedFluids.isEmpty()) {
                return false
            }
            for (currentFluid in prioritizedFluids) {
                if (currentFluid != null) return true
            }
            return false
        }

    override fun isPrioritized(input: IAEFluidStack?): Boolean {
        return (input != null
                && prioritizedFluids.contains(input.fluid))
    }

    private fun requestSave() {
        saveProvider?.saveChanges(this)
    }

    override fun totalBytes(): Int {
        return totalBytes
    }

    override fun totalTypes(): Int {
        return totalTypes
    }

    override fun usedBytes(): Int {
        return totalBytes - freeBytes()
    }

    override fun usedTypes(): Int {
        var i = 0
        for (stack in fluidStacks) if (stack != null) i++
        return i
    }

    override fun validForPass(i: Int): Boolean {
        return true // TODO
    }

    protected open fun writeFluidToSlot(i: Int, fluidStack: FluidStack?) {
        val fluidTag = NBTTagCompound()
        if (fluidStack != null && fluidStack.fluidID > 0 && fluidStack.amount > 0) {
            fluidStack.writeToNBT(fluidTag)
            stackTag.setTag("Fluid#$i", fluidTag)
        } else {
            stackTag.removeTag("Fluid#$i")
        }
        fluidStacks[i] = fluidStack
    }

    init {
        if (!_storageStack.hasTagCompound()) _storageStack.tagCompound = NBTTagCompound()
        stackTag = _storageStack.tagCompound
        totalTypes = (_storageStack.item as IFluidStorageCell).getMaxTypes(_storageStack)
        totalBytes = (_storageStack.item as IFluidStorageCell).getMaxBytes(_storageStack) * 250
        for (i in 0 until totalTypes) fluidStacks.add(FluidStack.loadFluidStackFromNBT(stackTag.getCompoundTag(
                "Fluid#$i")))
        saveProvider = _saveProvider
    }
}