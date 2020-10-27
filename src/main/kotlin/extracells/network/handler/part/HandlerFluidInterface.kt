package extracells.network.handler.part

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import extracells.network.packet.part.PacketFluidInterface
open class HandlerFluidInterface : IMessageHandler<PacketFluidInterface, IMessage?> {
    override fun onMessage(message: PacketFluidInterface, ctx: MessageContext): IMessage? {
        message.execute()
        return null
    }
}