package extracells.network.packet.part

import appeng.api.config.RedstoneMode
import extracells.gui.GuiFluidEmitter
import extracells.network.AbstractPacket
import extracells.part.PartFluidLevelEmitter
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.entity.player.EntityPlayer
open class PacketFluidEmitter : AbstractPacket {
    private var wantedAmount: Long = 0
    private var part: PartFluidLevelEmitter? = null
    private var redstoneMode: RedstoneMode? = null
    private var toggle = false

    constructor()
    constructor(_toggle: Boolean, _part: PartFluidLevelEmitter?,
                _player: EntityPlayer?) {
        mode = 3
        toggle = _toggle
        part = _part
        player = _player
    }

    constructor(_wantedAmount: Int, _part: PartFluidLevelEmitter?,
                _player: EntityPlayer?) {
        mode = 0
        wantedAmount = _wantedAmount.toLong()
        part = _part
        player = _player
    }

    constructor(_wantedAmount: Long, _player: EntityPlayer?) {
        mode = 2
        wantedAmount = _wantedAmount
        player = _player
    }

    constructor(_redstoneMode: RedstoneMode?, _player: EntityPlayer?) {
        mode = 4
        redstoneMode = _redstoneMode
        player = _player
    }

    constructor(textField: String, _part: PartFluidLevelEmitter?,
                _player: EntityPlayer?) {
        mode = 1
        wantedAmount = if (textField.isEmpty()) 0 else textField.toLong()
        part = _part
        player = _player
    }

    override fun execute() {
        when (mode.toInt()) {
            0 -> part!!.changeWantedAmount(wantedAmount.toInt(), player)
            1 -> part!!.setWantedAmount(wantedAmount, player)
            2 -> if (player != null && player!!.isClientWorld) {
                val gui: Gui = Minecraft.getMinecraft().currentScreen
                if (gui is GuiFluidEmitter) {
                    gui.setAmountField(wantedAmount)
                }
            }
            3 -> if (toggle) {
                part!!.toggleMode(player)
            } else {
                part!!.syncClientGui(player)
            }
            4 -> if (player != null && player!!.isClientWorld) {
                val gui: Gui = Minecraft.getMinecraft().currentScreen
                if (gui is GuiFluidEmitter) {
                    gui.setRedstoneMode(redstoneMode)
                }
            }
        }
    }

    override fun readData(`in`: ByteBuf) {
        when (mode.toInt()) {
            0 -> {
                wantedAmount = `in`.readLong()
                part = AbstractPacket.readPart(`in`) as PartFluidLevelEmitter
            }
            1 -> {
                wantedAmount = `in`.readLong()
                part = AbstractPacket.readPart(`in`) as PartFluidLevelEmitter
            }
            2 -> wantedAmount = `in`.readLong()
            3 -> {
                toggle = `in`.readBoolean()
                part = AbstractPacket.readPart(`in`) as PartFluidLevelEmitter
            }
            4 -> redstoneMode = RedstoneMode.values()[`in`.readInt()]
        }
    }

    override fun writeData(out: ByteBuf) {
        when (mode.toInt()) {
            0 -> {
                out.writeLong(wantedAmount)
                AbstractPacket.writePart(part, out)
            }
            1 -> {
                out.writeLong(wantedAmount)
                AbstractPacket.writePart(part, out)
            }
            2 -> out.writeLong(wantedAmount)
            3 -> {
                out.writeBoolean(toggle)
                AbstractPacket.writePart(part, out)
            }
            4 -> out.writeInt(redstoneMode!!.ordinal)
        }
    }
}