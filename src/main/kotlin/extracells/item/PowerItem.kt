package extracells.item

import appeng.api.config.PowerUnits
import appeng.api.implementations.items.IAEItemPowerStorage
import cofh.api.energy.IEnergyContainerItem
import cpw.mods.fml.common.Optional
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

@Optional.Interface(iface = "cofh.api.energy.IEnergyContainerItem", modid = "CoFHAPI|energy", striprefs = true)
abstract class PowerItem : ItemECBase(), IAEItemPowerStorage , IEnergyContainerItem{

  abstract val MAX_POWER :Double
  abstract fun MAX_POWER() :Double

  @Optional.Method(modid = "CoFHAPI|energy")
  override fun extractEnergy(container: ItemStack?, maxExtract: Int, simulate: Boolean): Int {
    if (container == null)
      return 0
    return if (simulate) {
      if (getEnergyStored(container) >= maxExtract) maxExtract else getEnergyStored(container)
    } else {
      PowerUnits.AE.convertTo(PowerUnits.RF, extractAEPower(container, PowerUnits.RF.convertTo(PowerUnits.AE, maxExtract.toDouble()))).toInt()
    }
  }

  @Optional.Method(modid = "CoFHAPI|energy")
  override fun getEnergyStored(arg0: ItemStack): Int {
    return PowerUnits.AE.convertTo(PowerUnits.RF, getAECurrentPower(arg0)).toInt()
  }

  @Optional.Method(modid = "CoFHAPI|energy")
  override fun getMaxEnergyStored(arg0: ItemStack): Int {
    return PowerUnits.AE.convertTo(PowerUnits.RF, getAEMaxPower(arg0)).toInt()
  }

  @Optional.Method(modid = "CoFHAPI|energy")
  override fun receiveEnergy(container: ItemStack?, maxReceive: Int, simulate: Boolean): Int =
          if (container == null) 0 else if (simulate) {
            val current: Double = PowerUnits.AE.convertTo(PowerUnits.RF, getAECurrentPower(container))
            val max: Double = PowerUnits.AE.convertTo(PowerUnits.RF, getAEMaxPower(container))
            if (max - current >= maxReceive) maxReceive
            else (max - current).toInt()
          }
          else {
            val currentAEPower = getAECurrentPower(container)
            if ( currentAEPower < getAEMaxPower(container)){
              PowerUnits.AE.convertTo(PowerUnits.RF, injectAEPower(container, PowerUnits.RF.convertTo(PowerUnits.AE, maxReceive.toDouble()))).toInt()
            }else
              0
          }

  override fun injectAEPower(itemStack: ItemStack, amt: Double): Double {
    val tagCompound: NBTTagCompound = ensureTagCompound(itemStack)
    val currentPower: Double = tagCompound.getDouble("power")
    val toInject: Double = Math.min(amt, this.MAX_POWER - currentPower)
    tagCompound.setDouble("power", currentPower + toInject)
    return toInject
  }

  override fun extractAEPower(itemStack: ItemStack, amt: Double): Double {
    val tagCompound: NBTTagCompound = ensureTagCompound(itemStack)
    val currentPower: Double = tagCompound.getDouble("power")
    val toExtract: Double = Math.min(amt, currentPower)
    tagCompound.setDouble("power", currentPower - toExtract)
    return toExtract
  }

  override fun getAECurrentPower(itemStack: ItemStack): Double {
    val tagCompound: NBTTagCompound = ensureTagCompound(itemStack)
    return tagCompound.getDouble("power")
  }

  override fun getAEMaxPower(itemStack: ItemStack): Double {
    return this.MAX_POWER
  }

  fun ensureTagCompound(itemStack: ItemStack): NBTTagCompound {
    if (!itemStack.hasTagCompound()) itemStack.tagCompound = NBTTagCompound()
    return itemStack.tagCompound
  }

}
