package extracells.network.packet.part

import extracells.network.AbstractPacket
import extracells.part.PartFluidPlaneFormation
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer

class PacketFluidPlaneFormation : AbstractPacket {
    private var part: PartFluidPlaneFormation? = null

    constructor() {}
    constructor(_player: EntityPlayer?,
                _part: PartFluidPlaneFormation?) : super(_player) {
        mode = 0
        part = _part
    }

    override fun execute() {
        when (mode) {
            0 -> part!!.sendInformation(player)
        }
    }

    override fun readData(`in`: ByteBuf) {
        when (mode) {
            0 -> part = AbstractPacket.Companion.readPart(`in`) as PartFluidPlaneFormation
        }
    }

    override fun writeData(out: ByteBuf) {
        when (mode) {
            0 -> AbstractPacket.Companion.writePart(part, out)
        }
    }
}