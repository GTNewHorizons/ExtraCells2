package extracells.network.handler.part

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import extracells.network.packet.part.PacketBusFluidIO

class HandlerBusFluidIO : IMessageHandler<PacketBusFluidIO, IMessage?> {
    override fun onMessage(message: PacketBusFluidIO, ctx: MessageContext): IMessage? {
        message.execute()
        return null
    }
}