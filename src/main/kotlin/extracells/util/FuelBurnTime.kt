package extracells.util

import buildcraft.api.fuels.BuildcraftFuelRegistry
import extracells.integration.Integration
import extracells.integration.Integration.Mods
import net.minecraftforge.fluids.Fluid
import java.util.*

object FuelBurnTime {
    private val fluidBurnTimes: HashMap<Fluid, Int> = HashMap()

    @JvmStatic
    fun registerFuel(fluid: Fluid, burnTime: Int) {
        if (!fluidBurnTimes.contains(fluid))
            fluidBurnTimes[fluid] = burnTime
    }

    @JvmStatic
    fun getBurnTime(fluid: Fluid?): Int {
        if (fluid == null)
            return 0
        if (fluidBurnTimes.containsKey(fluid))
            return fluidBurnTimes[fluid]!!
        if (Integration.Mods.BCFUEL.isEnabled)
            return getBCBurnTime(fluid)
        return 0
    }

    private fun getBCBurnTime(fluid: Fluid): Int {
        var bt = 0
        BuildcraftFuelRegistry.fuel.fuels.stream().filter { it.fluid == fluid  }.findFirst().ifPresent { bt = it.totalBurningTime }
        return bt
    }
}