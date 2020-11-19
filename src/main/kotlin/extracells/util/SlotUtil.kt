package extracells.util

import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList

object SlotUtil {

    fun decreaseStackInSlot(dis: IInventory, slot: Int, amt: Int): ItemStack? {
        var stack = dis.getStackInSlot(slot)
        if (stack != null) {
            if (stack.stackSize <= amt) {
                dis.setInventorySlotContents(slot, null)
            } else {
                stack = stack.splitStack(amt)
                if (stack.stackSize == 0) {
                    dis.setInventorySlotContents(slot, null)
                }
            }
        }
        return stack
    }

    fun writeSlotsToNbt(inv: Array<ItemStack?>, tagCompound: NBTTagCompound) {
        val itemList = NBTTagList()
        for (i in inv.indices) {
            val stack = inv[i]
            if (stack != null) {
                val tag = NBTTagCompound()
                tag.setByte("Slot", i.toByte())
                stack.writeToNBT(tag)
                itemList.appendTag(tag)
            }
        }
        tagCompound.setTag("Inventory", itemList)
    }

    fun readSlotsFromNbt(inv: Array<ItemStack?>, tagCompound: NBTTagCompound) {
        val tagList = tagCompound.getTagList("Inventory", 10)
        for (i in 0 until tagList.tagCount()) {
            val tag = tagList.getCompoundTagAt(i)
            val slot = tag.getByte("Slot")
            if (slot >= 0 && slot < inv.size) {
                inv[slot.toInt()] = ItemStack.loadItemStackFromNBT(tag)
            }
        }
    }
}