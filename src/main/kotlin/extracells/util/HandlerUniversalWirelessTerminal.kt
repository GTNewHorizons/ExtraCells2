package extracells.util

import appeng.api.features.IWirelessTermHandler
import appeng.api.util.IConfigManager
import extracells.api.IWirelessFluidTermHandler
//import extracells.api.IWirelessGasTermHandler
import extracells.item.ItemWirelessTerminalUniversal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

object HandlerUniversalWirelessTerminal : IWirelessTermHandler, IWirelessFluidTermHandler/*, IWirelessGasTermHandler*/ {
    override fun getConfigManager(its: ItemStack?): IConfigManager? = ItemWirelessTerminalUniversal.getConfigManager(its)
    override fun canHandle(its: ItemStack?): Boolean = ItemWirelessTerminalUniversal.canHandle(its)
    override fun usePower(player: EntityPlayer?, amount: Double, its: ItemStack?): Boolean = ItemWirelessTerminalUniversal.usePower(player, amount, its)
    override fun hasPower(player: EntityPlayer?, amount: Double, its: ItemStack?): Boolean = ItemWirelessTerminalUniversal.hasPower(player, amount, its)
    override fun isItemNormalWirelessTermToo(its: ItemStack?): Boolean = ItemWirelessTerminalUniversal.isItemNormalWirelessTermToo(its)
    override fun setEncryptionKey(item: ItemStack, encKey: String, name: String): Unit = ItemWirelessTerminalUniversal.setEncryptionKey(item, encKey, name)
    override fun getEncryptionKey(item: ItemStack): String = ItemWirelessTerminalUniversal.getEncryptionKey(item)
}