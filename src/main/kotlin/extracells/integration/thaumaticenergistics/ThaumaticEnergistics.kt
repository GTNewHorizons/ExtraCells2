package extracells.integration.thaumaticenergistics

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import thaumicenergistics.api.IThEWirelessEssentiaTerminal
import thaumicenergistics.api.ThEApi

object ThaumaticEnergistics {
  fun openEssentiaTerminal(player: EntityPlayer, terminal: Any) = ThEApi.instance()?.interact()?.openWirelessTerminalGui(player, terminal as IThEWirelessEssentiaTerminal?)
  fun getTerminal(): ItemStack? = ThEApi.instance()?.parts()?.Essentia_Terminal?.stack
  fun getWirelessTerminal(): ItemStack? =ThEApi.instance()?.items()?.WirelessEssentiaTerminal?.stack
}