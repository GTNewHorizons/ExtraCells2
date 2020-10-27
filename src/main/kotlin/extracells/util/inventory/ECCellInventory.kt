package extracells.util.inventory

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
open class ECCellInventory(private val storage: ItemStack?, private val tagId: String, private val size: Int,
                      private val stackLimit: Int) : IInventory {
    private lateinit var tagCompound: NBTTagCompound
    private lateinit var slots: Array<ItemStack?>
    private var dirty = false
    override fun closeInventory() {
        if (dirty) {
            for (i in slots.indices) {
                tagCompound.removeTag("ItemStack#$i")
                val content = slots[i]
                if (content != null) {
                    tagCompound.setTag("ItemStack#$i",
                            NBTTagCompound())
                    content.writeToNBT(tagCompound
                            .getCompoundTag("ItemStack#$i"))
                }
            }
        }
    }

    override fun decrStackSize(slotId: Int, amount: Int): ItemStack? {
        val slotContent = slots[slotId] ?: return null
        val stackSize = slotContent.stackSize
        if (stackSize <= 0) return null
        if (amount >= stackSize) {
            slots[slotId] = null
        } else {
            slots[slotId]!!.stackSize -= amount
        }
        val toReturn = slotContent.copy()
        toReturn.stackSize = amount
        markDirty()
        return toReturn
    }

    override fun getInventoryName(): String {
        return ""
    }

    override fun getInventoryStackLimit(): Int {
        return stackLimit
    }

    override fun getSizeInventory(): Int {
        return size
    }

    override fun getStackInSlot(slotId: Int): ItemStack? {
        return slots[slotId]
    }

    override fun getStackInSlotOnClosing(slotId: Int): ItemStack? {
        return getStackInSlot(slotId)
    }

    override fun hasCustomInventoryName(): Boolean {
        return false
    }

    override fun isItemValidForSlot(slotId: Int, itemStack: ItemStack): Boolean {
        return true
    }

    override fun isUseableByPlayer(entityPlayer: EntityPlayer): Boolean {
        return true
    }

    override fun markDirty() {
        dirty = true
        closeInventory()
        dirty = false
    }

    override fun openInventory() {
        slots = arrayOfNulls(size)
        for (i in slots.indices) {
            slots[i] = ItemStack.loadItemStackFromNBT(tagCompound
                    .getCompoundTag("ItemStack#$i"))
        }
    }

    override fun setInventorySlotContents(slotId: Int, content: ItemStack) {
        val slotContent = slots[slotId]
        if (slotContent != content) {
            slots[slotId] = content
            markDirty()
        }
    }

    init {
        if (storage != null) {
            if (!storage.hasTagCompound())
                storage.tagCompound = NBTTagCompound()
        }
        storage?.tagCompound?.setTag(tagId,
                storage.tagCompound.getCompoundTag(tagId))
        if (storage != null) {
            tagCompound = storage.tagCompound.getCompoundTag(
                    tagId)
        }
        openInventory()
    }
}