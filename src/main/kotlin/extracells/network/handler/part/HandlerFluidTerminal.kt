package extracells.network.handler.part

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import extracells.network.packet.part.PacketFluidTerminal
open class HandlerFluidTerminal : IMessageHandler<PacketFluidTerminal, IMessage?> {
    override fun onMessage(message: PacketFluidTerminal, ctx: MessageContext): IMessage? {
        message.execute()
        return null
    }
}