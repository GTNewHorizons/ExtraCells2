package extracells.container

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.gui.GuiFluidInterface
import extracells.network.packet.part.PacketOreDictExport
import extracells.part.PartOreDictExporter
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
open class ContainerOreDictExport(var player: EntityPlayer, var part: PartOreDictExporter) : Container() {
    @SideOnly(Side.CLIENT)
    var gui: GuiFluidInterface? = null
    protected fun bindPlayerInventory(inventoryPlayer: IInventory?) {
        for (i in 0..2) {
            for (j in 0..8) {
                addSlotToContainer(Slot(inventoryPlayer,
                        j + i * 9 + 9, 8 + j * 18, 84 + i * 18))
            }
        }
        for (i in 0..8) {
            addSlotToContainer(Slot(inventoryPlayer, i, 8 + i * 18,
                    142))
        }
    }

    override fun canInteractWith(entityplayer: EntityPlayer): Boolean {
        return part.isValid
    }

    override fun retrySlotClick(p_75133_1_: Int, p_75133_2_: Int,
                                p_75133_3_: Boolean, p_75133_4_: EntityPlayer) {
    }

    init {
        bindPlayerInventory(player.inventory)
        val tile = part.hostTile
        if (tile != null && tile.hasWorldObj() && !tile.worldObj.isRemote) {
            PacketOreDictExport(player, part.filter, Side.CLIENT)
                    .sendPacketToPlayer(player)
        }
    }
}