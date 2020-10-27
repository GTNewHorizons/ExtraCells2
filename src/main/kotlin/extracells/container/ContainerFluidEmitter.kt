package extracells.container

import extracells.part.PartFluidLevelEmitter
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidContainerRegistry
open class ContainerFluidEmitter(var part: PartFluidLevelEmitter,
                            var player: EntityPlayer?) : Container() {
    protected fun bindPlayerInventory(inventoryPlayer: IInventory?) {
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
        return part.isValid
    }

    override fun transferStackInSlot(player: EntityPlayer, slotnumber: Int): ItemStack? {
        val slot = inventorySlots[slotnumber] as Slot?
        if (slot != null && slot.hasStack) {
            val fluidItem = slot.stack.copy()
            fluidItem.stackSize = 1
            val fluidStack = FluidContainerRegistry
                    .getFluidForFilledItem(fluidItem) ?: return null
            part.setFluid(0, fluidStack.getFluid(), player)
            return null
        }
        return null
    }

    init {
        bindPlayerInventory(player?.inventory)
    }
}