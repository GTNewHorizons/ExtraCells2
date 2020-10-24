package extracells.wireless

import extracells.api.IWirelessGasFluidTermHandler
import net.minecraft.item.ItemStack
import java.util.*

object WirelessTermRegistry {
    fun getWirelessTermHandler(`is`: ItemStack?): IWirelessGasFluidTermHandler? {
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
            handler: IWirelessGasFluidTermHandler) {
        if (!handlers.contains(handler)) handlers.add(handler)
    }

    var handlers: MutableList<IWirelessGasFluidTermHandler> = ArrayList()
}