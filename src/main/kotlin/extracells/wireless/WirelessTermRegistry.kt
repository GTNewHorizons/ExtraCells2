package extracells.wireless

import extracells.api.IWirelessFluidTermHandler
import net.minecraft.item.ItemStack
import java.util.*

object WirelessTermRegistry {
    fun getWirelessTermHandler(`is`: ItemStack?): IWirelessFluidTermHandler? {
        if (`is` == null) return null
        for (handler in handlers) {
            if (handler.canHandle(`is`)) return handler
        }
        return null
    }

    fun isWirelessItem(`is`: ItemStack?): Boolean {
        if (`is` == null) return false
        for (handler in handlers) {
            if (handler.canHandle(`is`)) return true
        }
        return false
    }

    fun registerWirelessTermHandler(
            handler: IWirelessFluidTermHandler) {
        if (!handlers.contains(handler)) handlers.add(handler)
    }

    var handlers: MutableList<IWirelessFluidTermHandler> = ArrayList()
}