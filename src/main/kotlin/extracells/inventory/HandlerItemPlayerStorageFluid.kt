package extracells.inventory

import appeng.api.storage.ISaveProvider
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import java.util.*
open class HandlerItemPlayerStorageFluid : HandlerItemStorageFluid {
    private val player: EntityPlayer

    constructor(_storageStack: ItemStack,
                _saveProvider: ISaveProvider?, _filter: ArrayList<Fluid?>?,
                _player: EntityPlayer) : super(_storageStack, _saveProvider, _filter) {
        player = _player
    }

    constructor(_storageStack: ItemStack,
                _saveProvider: ISaveProvider?, _player: EntityPlayer) : super(_storageStack, _saveProvider) {
        player = _player
    }

    override fun writeFluidToSlot(i: Int, fluidStack: FluidStack?) {
        if (player.currentEquippedItem == null) return
        val item = player.currentEquippedItem
        if (!item.hasTagCompound()) item.tagCompound = NBTTagCompound()
        val fluidTag = NBTTagCompound()
        if (fluidStack != null && fluidStack.fluidID > 0 && fluidStack.amount > 0) {
            fluidStack.writeToNBT(fluidTag)
            item.tagCompound.setTag("Fluid#$i", fluidTag)
        } else {
            item.tagCompound.removeTag("Fluid#$i")
        }
        fluidStacks[i] = fluidStack
    }
}