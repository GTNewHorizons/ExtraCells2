package extracells.api

import appeng.api.features.INetworkEncodable
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

interface IWirelessGasFluidTermHandler : INetworkEncodable {
    fun canHandle(`is`: ItemStack?): Boolean
    fun hasPower(player: EntityPlayer?, amount: Double, `is`: ItemStack?): Boolean
    fun isItemNormalWirelessTermToo(`is`: ItemStack?): Boolean
    fun usePower(player: EntityPlayer?, amount: Double, `is`: ItemStack?): Boolean
}