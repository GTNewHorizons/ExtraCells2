package extracells.network.packet.other

import net.minecraftforge.fluids.Fluid

interface IFluidSlotGui {
    fun updateFluids(_fluids: List<Fluid?>?)
}