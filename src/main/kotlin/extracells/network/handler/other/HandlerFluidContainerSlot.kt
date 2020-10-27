package extracells.network.handler.other

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import extracells.network.packet.other.PacketFluidContainerSlot
open class HandlerFluidContainerSlot : IMessageHandler<PacketFluidContainerSlot, IMessage?> {
    override fun onMessage(message: PacketFluidContainerSlot,
                           ctx: MessageContext): IMessage? {
        message.execute()
        return null
    }
}