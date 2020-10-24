package extracells.container

import extracells.container.slot.SlotRespective
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

class ContainerFluidCrafter(player: InventoryPlayer?, var tileentity: IInventory) : Container() {
    protected fun bindPlayerInventory(inventoryPlayer: InventoryPlayer?) {
        for (i in 0..2) {
            for (j in 0..8) {
                addSlotToContainer(Slot(inventoryPlayer, j + i * 9 + 9,
                        8 + j * 18, i * 18 + 84))
            }
        }
        for (i in 0..8) {
            addSlotToContainer(Slot(inventoryPlayer, i, 8 + i * 18, 142))
        }
    }

    override fun canInteractWith(entityplayer: EntityPlayer): Boolean {
        return tileentity.isUseableByPlayer(entityplayer)
    }

    override fun onContainerClosed(entityplayer: EntityPlayer) {
        super.onContainerClosed(entityplayer)
    }

    override fun retrySlotClick(par1: Int, par2: Int, par3: Boolean,
                                par4EntityPlayer: EntityPlayer) {
        // DON'T DO ANYTHING, YOU SHITTY METHOD!
    }

    override fun transferStackInSlot(player: EntityPlayer, slotnumber: Int): ItemStack {
        var itemstack: ItemStack? = null
        val slot = inventorySlots[slotnumber] as Slot?
        if (slot != null && slot.hasStack) {
            val itemstack1 = slot.stack
            itemstack = itemstack1.copy()
            if (tileentity.isItemValidForSlot(0, itemstack1)) {
                if (slotnumber < 10) {
                    if (!mergeItemStack(itemstack1, 10, 36, false)) return null
                } else if (slotnumber >= 10 && slotnumber <= 36) {
                    if (!mergeItemStack(itemstack1, 0, 1, false)) return null
                }
                if (itemstack1.stackSize == 0) {
                    slot.putStack(null)
                } else {
                    slot.onSlotChanged()
                }
            } else {
                return null
            }
        }
        return itemstack!!
    }

    init {
        for (i in 0..2) {
            for (j in 0..2) {
                addSlotToContainer(SlotRespective(tileentity, j + i * 3,
                        62 + j * 18, 17 + i * 18))
            }
        }
        bindPlayerInventory(player)
    }
}