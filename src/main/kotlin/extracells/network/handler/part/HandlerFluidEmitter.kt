package extracells.network.handler.part

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import extracells.network.packet.part.PacketFluidEmitter

class HandlerFluidEmitter : IMessageHandler<PacketFluidEmitter, IMessage?> {
    override fun onMessage(message: PacketFluidEmitter, ctx: MessageContext): IMessage? {
        message.execute()
        return null
    }
}