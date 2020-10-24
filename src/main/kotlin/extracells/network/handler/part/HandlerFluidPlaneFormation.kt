package extracells.network.handler.part

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import extracells.network.packet.part.PacketFluidPlaneFormation

class HandlerFluidPlaneFormation : IMessageHandler<PacketFluidPlaneFormation, IMessage?> {
    override fun onMessage(message: PacketFluidPlaneFormation,
                           ctx: MessageContext): IMessage? {
        message.execute()
        return null
    }
}