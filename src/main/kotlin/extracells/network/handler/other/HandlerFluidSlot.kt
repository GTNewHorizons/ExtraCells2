package extracells.network.handler.other

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import extracells.network.packet.other.PacketFluidSlot

class HandlerFluidSlot : IMessageHandler<PacketFluidSlot, IMessage?> {
    override fun onMessage(message: PacketFluidSlot, ctx: MessageContext): IMessage? {
        message.execute()
        return null
    }
}