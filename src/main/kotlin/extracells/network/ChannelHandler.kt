package extracells.network

import cpw.mods.fml.common.network.FMLEmbeddedChannel
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint
import cpw.mods.fml.relauncher.Side
import extracells.network.handler.other.HandlerFluidContainerSlot
import extracells.network.handler.other.HandlerFluidSlot
import extracells.network.handler.part.*
import extracells.network.packet.other.PacketFluidContainerSlot
import extracells.network.packet.other.PacketFluidSlot
import extracells.network.packet.part.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.Packet
import net.minecraft.world.World
import java.util.*

object ChannelHandler {
    fun registerMessages() {
        wrapper.registerMessage(HandlerFluidSlot::class.java, PacketFluidSlot::class.java,
                0, Side.CLIENT)
        wrapper.registerMessage(HandlerFluidSlot::class.java, PacketFluidSlot::class.java,
                0, Side.SERVER)
        wrapper.registerMessage(HandlerBusFluidIO::class.java,
                PacketBusFluidIO::class.java, 1, Side.CLIENT)
        wrapper.registerMessage(HandlerBusFluidIO::class.java,
                PacketBusFluidIO::class.java, 1, Side.SERVER)
        wrapper.registerMessage(
                HandlerBusFluidStorage::class.java,
                PacketBusFluidStorage::class.java, 2, Side.CLIENT)
        wrapper.registerMessage(
                HandlerBusFluidStorage::class.java,
                PacketBusFluidStorage::class.java, 2, Side.SERVER)
        wrapper.registerMessage(HandlerFluidEmitter::class.java,
                PacketFluidEmitter::class.java, 3, Side.CLIENT)
        wrapper.registerMessage(HandlerFluidEmitter::class.java,
                PacketFluidEmitter::class.java, 3, Side.SERVER)
        wrapper.registerMessage(
                HandlerFluidPlaneFormation::class.java,
                PacketFluidPlaneFormation::class.java, 4, Side.CLIENT)
        wrapper.registerMessage(
                HandlerFluidPlaneFormation::class.java,
                PacketFluidPlaneFormation::class.java, 4, Side.SERVER)
        wrapper.registerMessage(HandlerFluidStorage::class.java,
                PacketFluidStorage::class.java, 5, Side.CLIENT)
        wrapper.registerMessage(HandlerFluidStorage::class.java,
                PacketFluidStorage::class.java, 5, Side.SERVER)
        wrapper.registerMessage(HandlerFluidTerminal::class.java,
                PacketFluidTerminal::class.java, 6, Side.CLIENT)
        wrapper.registerMessage(HandlerFluidTerminal::class.java,
                PacketFluidTerminal::class.java, 6, Side.SERVER)
        wrapper.registerMessage(HandlerFluidInterface::class.java,
                PacketFluidInterface::class.java, 7, Side.CLIENT)
        wrapper.registerMessage(HandlerFluidInterface::class.java,
                PacketFluidInterface::class.java, 7, Side.SERVER)
        wrapper.registerMessage(
                HandlerFluidContainerSlot::class.java,
                PacketFluidContainerSlot::class.java, 8, Side.CLIENT)
        wrapper.registerMessage(
                HandlerFluidContainerSlot::class.java,
                PacketFluidContainerSlot::class.java, 8, Side.SERVER)
        wrapper.registerMessage(HandlerOreDictExport::class.java,
                PacketOreDictExport::class.java, 9, Side.CLIENT)
        wrapper.registerMessage(HandlerOreDictExport::class.java,
                PacketOreDictExport::class.java, 9, Side.SERVER)
    }

    fun sendPacketToAllPlayers(packet: AbstractPacket?) {
        wrapper.sendToAll(packet)
    }

    fun sendPacketToAllPlayers(packet: Packet?, world: World) {
        for (player in world.playerEntities) {
            if (player is EntityPlayerMP) {
                player.playerNetServerHandler
                        .sendPacket(packet)
            }
        }
    }

    fun sendPacketToPlayer(packet: AbstractPacket?,
                           player: EntityPlayer?) {
        wrapper.sendTo(packet, player as EntityPlayerMP?)
    }

    fun sendPacketToPlayersAround(abstractPacket: AbstractPacket?,
                                  point: TargetPoint?) {
        wrapper.sendToAllAround(abstractPacket, point)
    }

    fun sendPacketToServer(packet: AbstractPacket?) {
        wrapper.sendToServer(packet)
    }

    private val channels: EnumMap<Side, FMLEmbeddedChannel>? = null
    var wrapper = NetworkRegistry.INSTANCE
            .newSimpleChannel("extracells")
}