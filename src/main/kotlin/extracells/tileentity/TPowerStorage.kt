package extracells.tileentity

import appeng.api.config.AccessRestriction
import appeng.api.config.Actionable
import appeng.api.config.PowerMultiplier
import appeng.api.networking.energy.IAEPowerStorage
import net.minecraft.nbt.NBTTagCompound


abstract class TPowerStorage : TileBase(), IAEPowerStorage {

  val powerInformation = PowerInformation()

  override fun getAECurrentPower() = powerInformation.currentPower

  override fun getPowerFlow() = AccessRestriction.READ_WRITE

  override fun getAEMaxPower() = powerInformation.maxPower

  fun setMaxPower(power: Double){
    powerInformation.maxPower = power
  }

  override fun injectAEPower(amt: Double, mode: Actionable): Double {
    val maxStore = powerInformation.maxPower - powerInformation.currentPower
    val notStorred:Double =
      if(maxStore - amt >= 0)
        0.toDouble()
      else
        amt - maxStore
    if(mode == Actionable.MODULATE)
      powerInformation.currentPower += amt - notStorred
    return notStorred
  }

  override fun isAEPublicPowerStorage() = true

  override fun extractAEPower(amount: Double, mode: Actionable, usePowerMultiplier : PowerMultiplier): Double {
    val toExtract = amount.coerceAtMost(powerInformation.currentPower)
    if (mode == Actionable.MODULATE)
      powerInformation.currentPower -= toExtract
    return toExtract
  }

  fun readPowerFromNBT(tag: NBTTagCompound) {
    if(tag.hasKey("currenPowerBattery"))
      powerInformation.currentPower = tag.getDouble("currenPowerBattery")
  }

  fun writePowerToNBT(tag: NBTTagCompound) = tag.setDouble("currenPowerBattery", powerInformation.currentPower)
open class PowerInformation{
    var currentPower = 0.0
    var maxPower = 500.0
  }
}
