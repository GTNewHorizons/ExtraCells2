package extracells.util

import appeng.api.AEApi
import appeng.api.storage.data.IAEStack
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids.FluidStack

object NBTUtil {

    fun readOutputFromNBT(tag: NBTTagCompound, export: MutableList<IAEStack<*>?>, addToExport: MutableList<IAEStack<*>?>) {
        addToExport.clear()
        export.clear()
        var i = tag.getInteger("add")
        for (j in 0 until i) {
            if (tag.getBoolean("add-$j-isItem")) {
                val s = AEApi
                        .instance()
                        .storage()
                        .createItemStack(
                                ItemStack.loadItemStackFromNBT(tag
                                        .getCompoundTag("add-$j")))
                s.stackSize = tag.getLong("add-$j-amount")
                addToExport.add(s)
            } else {
                val s = AEApi
                        .instance()
                        .storage()
                        .createFluidStack(
                                FluidStack.loadFluidStackFromNBT(tag
                                        .getCompoundTag("add-$j")))
                s.stackSize = tag.getLong("add-$j-amount")
                addToExport.add(s)
            }
        }
        i = tag.getInteger("export")
        for (j in 0 until i) {
            if (tag.getBoolean("export-$j-isItem")) {
                val s = AEApi
                        .instance()
                        .storage()
                        .createItemStack(
                                ItemStack.loadItemStackFromNBT(tag
                                        .getCompoundTag("export-$j")))
                s.stackSize = tag.getLong("export-$j-amount")
                export.add(s)
            } else {
                val s = AEApi
                        .instance()
                        .storage()
                        .createFluidStack(
                                FluidStack.loadFluidStackFromNBT(tag
                                        .getCompoundTag("export-$j")))
                s.stackSize = tag.getLong("export-$j-amount")
                export.add(s)
            }
        }
    }
}