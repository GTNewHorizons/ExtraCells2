package extracells.container

import appeng.api.AEApi
import appeng.api.implementations.guiobjects.IGuiItem
import appeng.api.implementations.guiobjects.INetworkTool
import extracells.container.slot.SlotNetworkTool
import extracells.container.slot.SlotRespective
import extracells.gui.GuiBusFluidIO
import extracells.part.PartFluidIO
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

class ContainerBusFluidIO(private val part: PartFluidIO, player: EntityPlayer) : Container() {
    private var guiBusFluidIO: GuiBusFluidIO? = null
    protected fun bindPlayerInventory(inventoryPlayer: IInventory?) {
        for (i in 0..2) {
            for (j in 0..8) {
                addSlotToContainer(Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 102))
            }
        }
        for (i in 0..8) {
            addSlotToContainer(Slot(inventoryPlayer, i, 8 + i * 18, 160))
        }
    }

    override fun canInteractWith(entityplayer: EntityPlayer): Boolean {
        return part.isValid
    }

    override fun retrySlotClick(par1: Int, par2: Int, par3: Boolean, par4EntityPlayer: EntityPlayer) {
        // NOPE
    }

    fun setGui(_guiBusFluidIO: GuiBusFluidIO?) {
        guiBusFluidIO = _guiBusFluidIO
    }

    override fun transferStackInSlot(player: EntityPlayer, slotnumber: Int): ItemStack {
        if (guiBusFluidIO != null && guiBusFluidIO!!.shiftClick(
                        getSlot(slotnumber).stack)) return (inventorySlots[slotnumber] as Slot).stack
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
                return itemstack1
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
        for (i in 0..3) addSlotToContainer(SlotRespective(part.upgradeInventory, i, 187, i * 18 + 8))
        bindPlayerInventory(player.inventory)
        for (i in 0 until player.inventory.sizeInventory) {
            val stack = player.inventory.getStackInSlot(i)
            if (stack != null && AEApi.instance().definitions().items().networkTool().isSameAs(stack)) {
                val coord = part.host.location
                val guiItem = stack.item as IGuiItem
                val networkTool = guiItem.getGuiObject(stack, coord.world, coord.x, coord.y, coord.z) as INetworkTool
                for (j in 0..2) {
                    for (k in 0..2) {
                        addSlotToContainer(SlotNetworkTool(networkTool, j + k * 3, 187 + k * 18, j * 18 + 102))
                    }
                }
                return
            }
        }
    }
}