package extracells.network.packet.part

import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.container.ContainerOreDictExport
import extracells.gui.GuiOreDictExport
import extracells.network.AbstractPacket
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer

class PacketOreDictExport : AbstractPacket {
    private var filter: String? = null
    private var side: Side? = null

    constructor() {}
    constructor(_player: EntityPlayer?, filter: String?, side: Side?) : super(_player) {
        mode = 0
        this.filter = filter
        this.side = side
    }

    override fun execute() {
        when (mode) {
            0 -> if (side!!.isClient) try {
                handleClient()
            } catch (e: Throwable) {
            } else handleServer()
        }
    }

    @SideOnly(Side.CLIENT)
    private fun handleClient() {
        GuiOreDictExport.Companion.updateFilter(filter)
    }

    private fun handleServer() {
        val con = player!!.openContainer
        if (con != null && con is ContainerOreDictExport) {
            con.part.filter = filter
        }
    }

    override fun readData(`in`: ByteBuf) {
        when (mode) {
            0 -> {
                if (`in`.readBoolean()) side = Side.SERVER else side = Side.CLIENT
                filter = ByteBufUtils.readUTF8String(`in`)
            }
        }
    }

    override fun writeData(out: ByteBuf) {
        when (mode) {
            0 -> {
                out.writeBoolean(side!!.isServer)
                ByteBufUtils.writeUTF8String(out, filter)
            }
        }
    }
}