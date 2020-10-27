package extracells.container

import appeng.api.AEApi
import extracells.tileentity.TileEntityHardMeDrive
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

open class ContainerHardMEDrive(private val inventory: InventoryPlayer, private val tile: TileEntityHardMeDrive) : Container() {
  protected fun bindPlayerInventory() {
    for (i in 0 .. 2) {
      for (j in 0 .. 8) {
        addSlotToContainer(Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18))
      }
    }
    for (i in 0 .. 8) {
      addSlotToContainer(Slot(inventory, i, 8 + i * 18, 142))
    }
  }
  override fun transferStackInSlot(p: EntityPlayer, i: Int): ItemStack? {
     var itemstack: ItemStack? = null
     val slot: Slot? = (inventorySlots[i] as Slot?)
    if (slot != null && slot.hasStack) {
       val itemstack1: ItemStack = slot.stack
      itemstack = itemstack1.copy()
      if (AEApi.instance().registries().cell().isCellHandled(itemstack)) {
        if (i < 3) {
          if (!mergeItemStack(itemstack1, 3, 38, false)) {
            return null
          }
        } else if (!mergeItemStack(itemstack1, 0, 3, false)) {
          return null
        }
        if (itemstack1.stackSize == 0) {
          slot.putStack(null)
        } else {
          slot.onSlotChanged()
        }
      }
    }
    return itemstack
  }
  override fun canInteractWith(p_75145_1_: EntityPlayer): Boolean {
    return if (tile.hasWorldObj()) tile.worldObj.getTileEntity(tile.xCoord, tile.yCoord, tile.zCoord) == this.tile else false
  }
}