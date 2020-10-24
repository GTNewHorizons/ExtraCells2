package extracells.api

import appeng.api.storage.ICellWorkbenchItem
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.Fluid
import java.util.*

interface IFluidStorageCell : ICellWorkbenchItem {
    /**
     *
     * @param ItemStack
     * @return the Fluid Filter. An empty ArrayList or null if the cell accepts
     * all Fluids
     */
    fun getFilter(`is`: ItemStack): ArrayList<Fluid>?
    fun getMaxBytes(`is`: ItemStack): Int
    fun getMaxTypes(`is`: ItemStack?): Int
}