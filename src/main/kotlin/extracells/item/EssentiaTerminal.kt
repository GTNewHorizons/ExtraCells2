package extracells.item

import cpw.mods.fml.common.Optional
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import thaumicenergistics.api.IThEWirelessEssentiaTerminal

@Optional.Interface(iface = "thaumicenergistics.api.IThEWirelessEssentiaTerminal", modid = "thaumicenergistics", striprefs = true)
interface EssentiaTerminal : IThEWirelessEssentiaTerminal{

  override fun getWETerminalTag(terminalItemstack: ItemStack): NBTTagCompound {
    val tag = ensureTagCompound(terminalItemstack)
    if (!tag.hasKey("essentia"))
      tag.setTag("essentia", NBTTagCompound())
    return tag.getCompoundTag("essentia")
  }

  private fun ensureTagCompound(itemStack: ItemStack): NBTTagCompound {
    if (!itemStack.hasTagCompound())
      itemStack.tagCompound = NBTTagCompound()
    return itemStack.tagCompound
  }
}