package extracells.container

import extracells.container.slot.SlotRespective
import extracells.part.PartDrive
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

class ContainerDrive(var part: PartDrive, player: EntityPlayer) : Container() {
    protected fun bindPlayerInventory(inventoryPlayer: IInventory?) {
        for (i in 0..2) {
            for (j in 0..8) {
                addSlotToContainer(Slot(inventoryPlayer, j + i * 9 + 9,
                        8 + j * 18, i * 18 + 63))
            }
        }
        for (i in 0..8) {
            addSlotToContainer(Slot(inventoryPlayer, i, 8 + i * 18, 121))
        }
    }

    override fun canInteractWith(entityplayer: EntityPlayer): Boolean {
        return part.isValid
    }

    override fun transferStackInSlot(player: EntityPlayer, slotnumber: Int): ItemStack {
        var itemstack: ItemStack? = null
        val slot = inventorySlots[slotnumber] as Slot?
        if (slot != null && slot.hasStack) {
            val itemstack1 = slot.stack
            itemstack = itemstack1.copy()
            if (slotnumber < 36) {
                if (!mergeItemStack(itemstack1, 36, inventorySlots.size,
                                true)) {
                    return null
                }
            } else if (!mergeItemStack(itemstack1, 0, 36, false)) {
                return null
            }
            if (itemstack1.stackSize == 0) {
                slot.putStack(null)
            } else {
                slot.onSlotChanged()
            }
        }
        return itemstack!!
    }

    init {
        for (i in 0..1) {
            for (j in 0..2) {
                addSlotToContainer(SlotRespective(part.inventory, j
                        + i * 3, 18 + 71 - i * 18, j * 18 - 4))
            }
        }
        bindPlayerInventory(player.inventory)
    }
}