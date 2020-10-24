package extracells.network.packet.other

import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fluids.Fluid

interface IFluidSlotPartOrBlock {
    fun setFluid(_index: Int, _fluid: Fluid?, _player: EntityPlayer?)
}