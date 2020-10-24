package extracells.network.handler.part

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import extracells.network.packet.part.PacketBusFluidStorage

class HandlerBusFluidStorage : IMessageHandler<PacketBusFluidStorage, IMessage?> {
    override fun onMessage(message: PacketBusFluidStorage, ctx: MessageContext): IMessage? {
        message.execute()
        return null
    }
}