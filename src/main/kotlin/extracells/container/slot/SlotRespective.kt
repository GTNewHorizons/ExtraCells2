package extracells.container.slot

import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

class SlotRespective(inventory: IInventory?, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {
    var inventory: IInventory
    override fun isItemValid(itemstack: ItemStack): Boolean {
        return this.inventory.isItemValidForSlot(slotNumber, itemstack)
    }

    init {
        this.inventory = inventory
    }
}