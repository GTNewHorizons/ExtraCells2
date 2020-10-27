package extracells.api

import appeng.api.features.IWirelessTermHandler
import net.minecraft.item.ItemStack

interface IWirelessFluidTermHandler : IWirelessTermHandler {
    fun isItemNormalWirelessTermToo(`is`: ItemStack?): Boolean
}

