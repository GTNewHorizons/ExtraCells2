package extracells.container.slot

import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
open class SlotRespective(var inventory: IInventory?, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {
    override fun isItemValid(itemstack: ItemStack): Boolean {
        return this.inventory.isItemValidForSlot(slotNumber, itemstack)
    }
}