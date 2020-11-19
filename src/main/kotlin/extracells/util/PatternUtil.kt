package extracells.util

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.networking.IGridHost
import appeng.api.networking.security.IActionHost
import appeng.api.networking.security.MachineSource
import appeng.api.networking.storage.IStorageGrid
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IAEItemStack
import appeng.api.storage.data.IAEStack
import extracells.api.crafting.IFluidCraftingPatternDetails
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack

object PatternUtil {

    fun pushPatternToFluid(patter: IFluidCraftingPatternDetails?, fluids: MutableMap<Fluid, Long>) {
        for (stack in patter?.condensedFluidInputs!!) {
            if (fluids.containsKey(stack!!.fluid)) {
                val amount = (fluids[stack.fluid]!!
                        + stack.stackSize)
                fluids.remove(stack.fluid)
                fluids[stack.fluid] = amount
            } else {
                fluids[stack.fluid] = stack.stackSize
            }
        }
    }

    fun canExtractFluid(storage: IStorageGrid, fluids: Map<Fluid, Long>, reference: IActionHost): Boolean {
        for (fluid in fluids.keys) {
            val amount = fluids[fluid]
            val extractFluid = storage.fluidInventory
                    .extractItems(
                            AEApi.instance()
                                    .storage()
                                    .createFluidStack(
                                            FluidStack(fluid,
                                                    (amount!! + 0).toInt())),
                            Actionable.SIMULATE, MachineSource(reference))
            if (extractFluid == null
                    || extractFluid.stackSize != amount) {
                return false
            }
        }
        return true
    }

    fun <T> fillToNetwork(reference: T, resource: FluidStack?, doFill: Boolean): Int
            where T : IActionHost, T : IGridHost {
        val node = reference.getGridNode(ForgeDirection.UNKNOWN)
        if (node == null || resource == null) return 0
        val grid = node.grid ?: return 0
        val storage = grid.getCache<IStorageGrid>(IStorageGrid::class.java) ?: return 0
        val notRemoved: IAEFluidStack?
        notRemoved = if (doFill) {
            storage.fluidInventory.injectItems(
                    AEApi.instance().storage().createFluidStack(resource),
                    Actionable.MODULATE, MachineSource(reference))
        } else {
            storage.fluidInventory.injectItems(
                    AEApi.instance().storage().createFluidStack(resource),
                    Actionable.SIMULATE, MachineSource(reference))
        }
        return if (notRemoved == null) resource.amount else (resource.amount - notRemoved.stackSize).toInt()
    }

    fun writePattern(startingIndex: Int, addToExport: List<IAEStack<*>?>, export: List<IAEStack<*>?>, tag: NBTTagCompound): NBTTagCompound {
        var i = startingIndex
        for (s in addToExport) {
            if (s != null) {
                tag.setBoolean("add-$i-isItem", s.isItem)
                val data = NBTTagCompound()
                if (s.isItem) {
                    (s as IAEItemStack).itemStack.writeToNBT(data)
                } else {
                    (s as IAEFluidStack).fluidStack.writeToNBT(data)
                }
                tag.setTag("add-$i", data)
                tag.setLong("add-$i-amount", s.stackSize)
            }
            i++
        }
        tag.setInteger("add", addToExport.size)
        i = 0
        for (s in export) {
            if (s != null) {
                tag.setBoolean("export-$i-isItem", s.isItem)
                val data = NBTTagCompound()
                if (s.isItem) {
                    (s as IAEItemStack).itemStack.writeToNBT(data)
                } else {
                    (s as IAEFluidStack).fluidStack.writeToNBT(data)
                }
                tag.setTag("export-$i", data)
                tag.setLong("export-$i-amount", s.stackSize)
            }
            i++
        }
        tag.setInteger("export", export.size)
        return tag
    }

}