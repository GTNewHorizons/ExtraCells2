package extracells.network.handler.part

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import extracells.network.packet.part.PacketOreDictExport

class HandlerOreDictExport : IMessageHandler<PacketOreDictExport, IMessage?> {
    override fun onMessage(message: PacketOreDictExport, ctx: MessageContext): IMessage? {
        message.execute()
        return null
    }
}