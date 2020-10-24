package extracells.container

import extracells.tileentity.TileEntityFluidFiller
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot

class ContainerFluidFiller(player: InventoryPlayer?,
                           var tileentity: TileEntityFluidFiller) : Container() {
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
        return true
    }

    override fun retrySlotClick(par1: Int, par2: Int, par3: Boolean,
                                par4EntityPlayer: EntityPlayer) {
        // DON'T DO ANYTHING, YOU SHITTY METHOD!
    }

    init {
        bindPlayerInventory(player)
    }
}