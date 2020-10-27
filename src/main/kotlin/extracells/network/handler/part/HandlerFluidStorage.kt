package extracells.network.handler.part

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import extracells.network.packet.part.PacketFluidStorage
open class HandlerFluidStorage : IMessageHandler<PacketFluidStorage, IMessage?> {
    override fun onMessage(message: PacketFluidStorage, ctx: MessageContext): IMessage? {
        message.execute()
        return null
    }
}