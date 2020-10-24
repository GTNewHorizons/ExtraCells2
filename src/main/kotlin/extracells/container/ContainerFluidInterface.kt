package extracells.container

import appeng.api.implementations.ICraftingPatternItem
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.api.IFluidInterface
import extracells.container.slot.SlotRespective
import extracells.gui.GuiFluidInterface
import extracells.network.packet.part.PacketFluidInterface
import extracells.part.PartFluidInterface
import extracells.tileentity.TileEntityFluidInterface
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.ForgeDirection

class ContainerFluidInterface(var player: EntityPlayer,
                              var fluidInterface: IFluidInterface) : Container(), IContainerListener {
    @SideOnly(Side.CLIENT)
    var gui: GuiFluidInterface? = null
    protected fun bindPlayerInventory(inventoryPlayer: IInventory?) {
        for (i in 0..2) {
            for (j in 0..8) {
                addSlotToContainer(Slot(inventoryPlayer, j + i * 9 + 9,
                        8 + j * 18, i * 18 + 149))
            }
        }
        for (i in 0..8) {
            addSlotToContainer(Slot(inventoryPlayer, i, 8 + i * 18, 207)) // 173
        }
    }

    override fun canInteractWith(entityplayer: EntityPlayer): Boolean {
        return true
    }

    private fun getFluidID(side: ForgeDirection): Int {
        val fluid = fluidInterface.getFilter(side) ?: return -1
        return fluid.id
    }

    override fun onContainerClosed(player: EntityPlayer) {
        super.onContainerClosed(player)
        if (fluidInterface is TileEntityFluidInterface) {
            (fluidInterface as TileEntityFluidInterface)
                    .removeListener(this)
        } else if (fluidInterface is PartFluidInterface) {
            (fluidInterface as PartFluidInterface).removeListener(this)
        }
    }

    override fun retrySlotClick(p_75133_1_: Int, p_75133_2_: Int,
                                p_75133_3_: Boolean, p_75133_4_: EntityPlayer) {
    }

    override fun transferStackInSlot(player: EntityPlayer, slotnumber: Int): ItemStack {
        var itemstack: ItemStack? = null
        val slot = inventorySlots[slotnumber] as Slot?
        if (slot != null && slot.hasStack) {
            val itemstack1 = slot.stack
            itemstack = itemstack1.copy()
            if (itemstack.item is ICraftingPatternItem) {
                if (slotnumber < 9) {
                    if (!mergeItemStack(itemstack1,
                                    inventorySlots.size - 9,
                                    inventorySlots.size, false)) {
                        if (!mergeItemStack(itemstack1, 9,
                                        inventorySlots.size - 9, false)) return null
                    }
                } else if (!mergeItemStack(itemstack1, 0, 9, false)) {
                    return null
                }
                if (itemstack1.stackSize == 0) {
                    slot.putStack(null)
                } else {
                    slot.onSlotChanged()
                }
                return itemstack
            }
            if (slotnumber < inventorySlots.size - 9) {
                if (!mergeItemStack(itemstack1, inventorySlots.size - 9,
                                inventorySlots.size, true)) {
                    return null
                }
            } else if (!mergeItemStack(itemstack1, 9,
                            inventorySlots.size - 9, false)) {
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

    override fun updateContainer() {
        PacketFluidInterface(arrayOf(
                fluidInterface.getFluidTank(
                        ForgeDirection.getOrientation(0))!!.fluid,
                fluidInterface.getFluidTank(
                        ForgeDirection.getOrientation(1))!!.fluid,
                fluidInterface.getFluidTank(
                        ForgeDirection.getOrientation(2))!!.fluid,
                fluidInterface.getFluidTank(
                        ForgeDirection.getOrientation(3))!!.fluid,
                fluidInterface.getFluidTank(
                        ForgeDirection.getOrientation(4))!!.fluid,
                fluidInterface.getFluidTank(
                        ForgeDirection.getOrientation(5))!!.fluid),
                arrayOf<Int?>(getFluidID(ForgeDirection.getOrientation(0)),
                        getFluidID(ForgeDirection.getOrientation(1)),
                        getFluidID(ForgeDirection.getOrientation(2)),
                        getFluidID(ForgeDirection.getOrientation(3)),
                        getFluidID(ForgeDirection.getOrientation(4)),
                        getFluidID(ForgeDirection.getOrientation(5))),
                player).sendPacketToPlayer(player)
    }

    init {
        for (j in 0..8) {
            addSlotToContainer(SlotRespective(
                    fluidInterface.patternInventory, j, 8 + j * 18, 115))
        }
        bindPlayerInventory(player.inventory)
        if (fluidInterface is TileEntityFluidInterface) {
            (fluidInterface as TileEntityFluidInterface).registerListener(this)
        } else if (fluidInterface is PartFluidInterface) {
            (fluidInterface as PartFluidInterface).registerListener(this)
        }
        if (fluidInterface is TileEntityFluidInterface) {
            (fluidInterface as TileEntityFluidInterface).doNextUpdate = true
        } else if (fluidInterface is PartFluidInterface) {
            (fluidInterface as PartFluidInterface).doNextUpdate = true
        }
    }
}