package extracells.item

import appeng.api.config.AccessRestriction
import appeng.api.util.IConfigManager
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.api.IHasUsePower
import extracells.wireless.ConfigManager
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.StatCollector
import kotlin.math.floor


abstract class WirelessTermBase : PowerItem(), IHasUsePower {

  init {
    setMaxStackSize(1)
  }

  open fun getConfigManager(itemStack: ItemStack?): IConfigManager? {
    itemStack?.let {
      val nbt = ensureTagCompound(it)
      if(!nbt.hasKey("settings"))
        nbt.setTag("settings", NBTTagCompound())
      val tag = nbt.getCompoundTag("settings")
      return ConfigManager(tag)
    }
    return null
  }

  override val MAX_POWER: Double = 1600000.0
  override fun MAX_POWER(): Double = MAX_POWER
  
  override fun getPowerFlow(itemStack: ItemStack): AccessRestriction = AccessRestriction.READ_WRITE

  override fun getDurabilityForDisplay(itemStack: ItemStack): Double = 1 - this.getAECurrentPower(itemStack) / MAX_POWER

  fun canHandle(its: ItemStack?): Boolean = its != null && its.item == this

  fun getEncryptionKey(itemStack: ItemStack): String {
    if (!itemStack.hasTagCompound()) itemStack.tagCompound = NBTTagCompound()
    return itemStack.tagCompound.getString("key")
  }

  fun setEncryptionKey(itemStack: ItemStack, encKey: String, name: String?) {
    if (!itemStack.hasTagCompound()) itemStack.tagCompound = NBTTagCompound()
    val tagCompound: NBTTagCompound = itemStack.tagCompound
    tagCompound.setString("key", encKey)
  }

  override fun hasPower(player: EntityPlayer?, amount: Double, `is`: ItemStack?): Boolean = `is`?.let { getAECurrentPower(it) >= amount } == true

  override fun usePower(player: EntityPlayer?, amount: Double, its: ItemStack?): Boolean {
    if (its != null) {
      extractAEPower(its, amount)
    }
    return true
  }

  override fun getSubItems(item: Item, creativeTab: CreativeTabs, itemList: MutableList<Any?>) {
    val itemList2 = itemList as MutableList<ItemStack>
    itemList2.add(ItemStack(item))
    val itemStack = ItemStack(item)
    injectAEPower(itemStack, this.MAX_POWER)
    itemList2.add(itemStack)
  }

  override fun showDurabilityBar(itemStack: ItemStack): Boolean = true

  @SideOnly(Side.CLIENT)
  override fun addInformation(itemStack: ItemStack, player: EntityPlayer, list: MutableList<Any?>, par4: Boolean) {
    val list2 = list as MutableList<String>
    if (!itemStack.hasTagCompound()) itemStack.tagCompound = NBTTagCompound()
    val encryptionKey: String? = itemStack.tagCompound.getString("key")
    val aeCurrentPower: Double = getAECurrentPower(itemStack)
    list2.add(StatCollector.translateToLocal("gui.appliedenergistics2.StoredEnergy") + ": " + aeCurrentPower + " AE - " + floor(aeCurrentPower / this.MAX_POWER * 1e4) / 1e2 + "%")
    list2.add(StatCollector.translateToLocal(if (encryptionKey != null && encryptionKey.isNotEmpty()) "gui.appliedenergistics2.Linked" else "gui.appliedenergistics2.Unlinked"))
  }
}
