package extracells.item

import cpw.mods.fml.common.Optional
import extracells.integration.WirelessCrafting.WirelessCrafting
import net.minecraft.item.ItemStack
import net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTerminalItem

@Optional.Interface(iface = "net.p455w0rd.wirelesscraftingterminal.api.IWirelessCraftingTerminalItem", modid = "ae2wct", striprefs = true)
interface CraftingTerminal : IWirelessCraftingTerminalItem{

  @Optional.Method(modid = "ae2wct")
  override fun checkForBooster (wirelessTerminal: ItemStack?):Boolean {
    if(wirelessTerminal?.hasTagCompound() == true) {
      val boosterNBTList = wirelessTerminal.tagCompound.getTagList("BoosterSlot", 10)
      if(boosterNBTList != null) {
        val boosterTagCompound = boosterNBTList.getCompoundTagAt(0)
        if(boosterTagCompound != null) {
          val boosterCard = ItemStack.loadItemStackFromNBT(boosterTagCompound)
          if(boosterCard != null) {
            return boosterCard.item == WirelessCrafting.getBoosterItem() && WirelessCrafting.isBoosterEnabled()
          }
        }
      }
    }

    return false
  }

  @Optional.Method(modid = "ae2wct")
  override fun handler() = null

  @Optional.Method(modid = "ae2wct")
  override fun postInit() {}

  @Optional.Method(modid = "ae2wct")
  override fun isWirelessCraftingEnabled(itemStack: ItemStack): Boolean =
    if (this == ItemWirelessTerminalUniversal)
      ItemWirelessTerminalUniversal.isInstalled(itemStack, TerminalType.CRAFTING)
    else
      true

}
