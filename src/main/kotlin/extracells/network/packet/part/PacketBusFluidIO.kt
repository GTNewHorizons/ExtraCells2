package extracells.network.packet.part

import appeng.api.config.RedstoneMode
import extracells.gui.GuiBusFluidIO
import extracells.network.AbstractPacket
import extracells.part.PartFluidIO
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fluids.Fluid

class PacketBusFluidIO : AbstractPacket {
    private val filterFluids: List<Fluid>? = null
    private var part: PartFluidIO? = null
    private var action: Byte = 0
    private var ordinal: Byte = 0
    private var filterSize: Byte = 0
    private var redstoneControlled = false

    constructor() {}
    constructor(_redstoneControlled: Boolean) : super() {
        mode = 4
        redstoneControlled = _redstoneControlled
    }

    constructor(_filterSize: Byte) : super() {
        mode = 3
        filterSize = _filterSize
    }

    constructor(_player: EntityPlayer?, _action: Byte,
                _part: PartFluidIO?) : super(_player) {
        mode = 0
        action = _action
        part = _part
    }

    constructor(_player: EntityPlayer?, _part: PartFluidIO?) : super(_player) {
        mode = 2
        part = _part
    }

    constructor(_redstoneMode: RedstoneMode) : super() {
        mode = 1
        ordinal = _redstoneMode.ordinal.toByte()
    }

    override fun execute() {
        val gui: Gui
        when (mode) {
            0 -> part!!.loopRedstoneMode(player)
            1 -> {
                gui = Minecraft.getMinecraft().currentScreen
                if (gui is GuiBusFluidIO) {
                    gui.updateRedstoneMode(RedstoneMode.values()[ordinal.toInt()])
                }
            }
            2 -> part!!.sendInformation(player)
            3 -> {
                gui = Minecraft.getMinecraft().currentScreen
                if (gui is GuiBusFluidIO) {
                    gui.changeConfig(filterSize)
                }
            }
            4 -> {
                gui = Minecraft.getMinecraft().currentScreen
                if (gui is GuiBusFluidIO) {
                    gui.setRedstoneControlled(redstoneControlled)
                }
            }
        }
    }

    override fun readData(`in`: ByteBuf) {
        when (mode) {
            0 -> {
                part = AbstractPacket.Companion.readPart(`in`) as PartFluidIO
                action = `in`.readByte()
            }
            1 -> ordinal = `in`.readByte()
            2 -> part = AbstractPacket.Companion.readPart(`in`) as PartFluidIO
            3 -> filterSize = `in`.readByte()
            4 -> redstoneControlled = `in`.readBoolean()
        }
    }

    override fun writeData(out: ByteBuf) {
        when (mode) {
            0 -> {
                AbstractPacket.Companion.writePart(part, out)
                out.writeByte(action.toInt())
            }
            1 -> out.writeByte(ordinal.toInt())
            2 -> AbstractPacket.Companion.writePart(part, out)
            3 -> out.writeByte(filterSize.toInt())
            4 -> out.writeBoolean(redstoneControlled)
        }
    }
}