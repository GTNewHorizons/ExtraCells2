package extracells.network.packet.part

import appeng.api.config.AccessRestriction
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.container.ContainerBusFluidStorage
import extracells.gui.GuiBusFluidStorage
import extracells.network.AbstractPacket
import extracells.part.PartFluidStorage
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
open class PacketBusFluidStorage : AbstractPacket {
    var part: PartFluidStorage? = null
    var access: AccessRestriction? = null

    constructor()
    constructor(_player: EntityPlayer?,
                _access: AccessRestriction?, toClient: Boolean) : super(_player) {
        if (toClient) mode = 1 else mode = 2
        access = _access
    }

    constructor(_player: EntityPlayer?, _part: PartFluidStorage?) : super(_player) {
        mode = 0
        player = _player
        part = _part
    }

    override fun execute() {
        when (mode.toInt()) {
            0 -> part!!.sendInformation(player)
            1 -> try {
                handleClient()
            } catch (e: Throwable) {
            }
            2 -> {
                val con = player!!.openContainer
                if (con != null && con is ContainerBusFluidStorage) {
                    con.part.updateAccess(access)
                    PacketBusFluidStorage(player, access, true)
                            .sendPacketToPlayer(player)
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    fun handleClient() {
        val screen = Minecraft.getMinecraft().currentScreen
        if (screen != null && screen is GuiBusFluidStorage) {
            screen.updateAccessRestriction(access)
        }
    }

    override fun readData(`in`: ByteBuf) {
        when (mode.toInt()) {
            0 -> part = readPart(`in`) as PartFluidStorage
            1, 2 -> access = readString(`in`)?.let { AccessRestriction.valueOf(it) }
        }
    }

    override fun writeData(out: ByteBuf) {
        when (mode.toInt()) {
            0 -> writePart(part, out)
            1, 2 -> writeString(access!!.name, out)
        }
    }
}