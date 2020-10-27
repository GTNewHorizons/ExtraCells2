package extracells.wireless

import appeng.api.features.IWirelessTermHandler
import appeng.api.util.IConfigManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
open class AEWirelessTermHandler : IWirelessTermHandler {
    override fun canHandle(`is`: ItemStack): Boolean {
        val handler = WirelessTermRegistry.getWirelessTermHandler(`is`) ?: return false
        return !handler.isItemNormalWirelessTermToo(`is`)
    }

    override fun getConfigManager(`is`: ItemStack): IConfigManager {
        return ConfigManager()
    }

    override fun getEncryptionKey(item: ItemStack): String? {
        val handler = WirelessTermRegistry.getWirelessTermHandler(item) ?: return null
        return handler.getEncryptionKey(item)
    }

    override fun hasPower(player: EntityPlayer, amount: Double, `is`: ItemStack): Boolean {
        val handler = WirelessTermRegistry.getWirelessTermHandler(`is`) ?: return false
        return handler.hasPower(player, amount, `is`)
    }

    override fun setEncryptionKey(item: ItemStack, encKey: String, name: String) {
        val handler = WirelessTermRegistry.getWirelessTermHandler(item) ?: return
        handler.setEncryptionKey(item, encKey, name)
    }

    override fun usePower(player: EntityPlayer, amount: Double, `is`: ItemStack): Boolean {
        val handler = WirelessTermRegistry.getWirelessTermHandler(`is`) ?: return false
        return handler.usePower(player, amount, `is`)
    }
}