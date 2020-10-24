package extracells.api

import appeng.api.storage.ICellWorkbenchItem
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.Fluid
import java.util.*

interface IGasStorageCell : ICellWorkbenchItem {
    /**
     *
     * @param ItemStack
     * @return the Fluid Filter. An empty ArrayList or null if the cell accepts
     * all Gas
     */
    fun getFilter(`is`: ItemStack): ArrayList<Fluid>?
    fun getMaxBytes(`is`: ItemStack): Int
    fun getMaxTypes(`is`: ItemStack?): Int
}