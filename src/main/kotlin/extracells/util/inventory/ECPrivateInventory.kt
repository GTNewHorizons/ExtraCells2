package extracells.util.inventory

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList

open class ECPrivateInventory @JvmOverloads constructor(_customName: String, _size: Int, _stackLimit: Int,
                                                        _receiver: IInventoryUpdateReceiver? = null) : IInventory {
    var slots: Array<ItemStack?> = arrayOfNulls(_size)
    var customName: String = _customName
    private val stackLimit: Int
    private val receiver: IInventoryUpdateReceiver?
    override fun closeInventory() {
        // NOBODY needs this!
    }

    override fun decrStackSize(slotId: Int, amount: Int): ItemStack? {
        if (slots[slotId] == null)
            return null
        val itemstack: ItemStack
        return if (slots[slotId]!!.stackSize <= amount) {
            itemstack = slots[slotId]!!
            slots[slotId] = null
            markDirty()
            itemstack
        } else {
            val temp = slots[slotId]!!
            itemstack = temp.splitStack(amount)
            slots[slotId] = temp
            if (temp.stackSize == 0) {
                slots[slotId] = null
            } else {
                slots[slotId] = temp
            }
            markDirty()
            itemstack
        }
    }

    override fun getInventoryName(): String {
        return customName
    }

    override fun getInventoryStackLimit(): Int {
        return stackLimit
    }

    override fun getSizeInventory(): Int {
        return slots.size
    }

    override fun getStackInSlot(i: Int): ItemStack? {
        return slots[i]
    }

    override fun getStackInSlotOnClosing(slotId: Int): ItemStack? {
        return slots[slotId]
    }

    override fun hasCustomInventoryName(): Boolean {
        return false
    }

    /**
     * Increases the stack size of a slot.
     *
     * @param slotId
     * ID of the slot
     * @param amount
     * amount to be drained
     *
     * @return the added Stack
     */
    fun incrStackSize(slotId: Int, amount: Int): ItemStack? {
        val slot = slots[slotId] ?: return null
        var stackLimit = inventoryStackLimit
        if (stackLimit > slot.maxStackSize) stackLimit = slot.maxStackSize
        val added = slot.copy()
        added.stackSize = if (slot.stackSize + amount > stackLimit) stackLimit else amount
        slot.stackSize += added.stackSize
        return added
    }

    override fun isItemValidForSlot(i: Int, itemstack: ItemStack?): Boolean {
        return true
    }

    override fun isUseableByPlayer(entityplayer: EntityPlayer?): Boolean {
        return true
    }

    override fun markDirty() {
        receiver?.onInventoryChanged()
    }

    override fun openInventory() {
        // NOBODY needs this!
    }

    fun readFromNBT(nbtList: NBTTagList?) {
        if (nbtList == null) {
            for (i in slots.indices) {
                slots[i] = null
            }
            return
        }
        for (i in 0 until nbtList.tagCount()) {
            val nbttagcompound = nbtList.getCompoundTagAt(i)
            val j: Int = nbttagcompound.getByte("Slot").toInt() and 255
            if (j >= 0 && j < slots.size) {
                slots[j] = ItemStack.loadItemStackFromNBT(nbttagcompound)
            }
        }
    }

    override fun setInventorySlotContents(slotId: Int, itemstack: ItemStack?) {
        if (itemstack != null && itemstack.stackSize > inventoryStackLimit) {
            itemstack.stackSize = inventoryStackLimit
        }
        slots[slotId] = itemstack
        markDirty()
    }

    fun writeToNBT(): NBTTagList {
        val nbtList = NBTTagList()
        for (i in slots.indices) {
            if (slots[i] != null) {
                val nbttagcompound = NBTTagCompound()
                nbttagcompound.setByte("Slot", i.toByte())
                slots[i]!!.writeToNBT(nbttagcompound)
                nbtList.appendTag(nbttagcompound)
            }
        }
        return nbtList
    }

    init {
        stackLimit = _stackLimit
        receiver = _receiver
    }
}