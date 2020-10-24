package extracells.api

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

interface IPortableGasStorageCell : IGasStorageCell {
    fun hasPower(player: EntityPlayer?, amount: Double, `is`: ItemStack?): Boolean
    fun usePower(player: EntityPlayer?, amount: Double, `is`: ItemStack?): Boolean
}