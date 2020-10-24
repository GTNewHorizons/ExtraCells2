package extracells.container.slot

import extracells.container.IStorageContainer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot

class SlotPlayerInventory(arg0: IInventory?,
                          val container: IStorageContainer?, arg1: Int, arg2: Int, arg3: Int) : Slot(arg0, arg1, arg2,
        arg3) {
    override fun canTakeStack(player: EntityPlayer): Boolean {
        if (player == null || container == null) return true
        val s = player.currentEquippedItem
        return if (s == null || !container.hasWirelessTermHandler()) true else s != inventory.getStackInSlot(
                slotIndex)
    }
}