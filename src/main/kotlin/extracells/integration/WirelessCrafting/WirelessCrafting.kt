package extracells.integration.WirelessCrafting

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.p455w0rd.wirelesscraftingterminal.api.WCTApi
import net.p455w0rd.wirelesscraftingterminal.reference.Reference

object WirelessCrafting {
  fun openCraftingTerminal(player: EntityPlayer) = WCTApi.instance()?.interact()?.openWirelessCraftingTerminalGui(player)
  fun getBoosterItem(): Item? =WCTApi.instance()?.items()?.InfinityBoosterCard?.item
  fun isBoosterEnabled(): Boolean = Reference.WCT_BOOSTER_ENABLED
  fun getCraftingTerminal(): ItemStack? = WCTApi.instance()?.items()?.WirelessCraftingTerminal?.stack
}