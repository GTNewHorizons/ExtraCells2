package extracells.tileentity

import appeng.api.networking.IGrid
import appeng.api.networking.IGridHost
import appeng.api.networking.storage.IStorageGrid
import appeng.api.storage.IMEMonitor
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IAEItemStack
import net.minecraftforge.common.util.ForgeDirection

interface TNetworkStorage {
    fun getStorageGrid(side: ForgeDirection): IStorageGrid? {
        if (this !is IGridHost) return null
        val host: IGridHost = this
        if (host.getGridNode(side) == null) return null
        val grid: IGrid = host.getGridNode(side).grid
        return grid.getCache(IStorageGrid::class.java)
    }

    fun getFluidInventory(side: ForgeDirection): IMEMonitor<IAEFluidStack>? {
        return getStorageGrid(side)?.fluidInventory
    }

    fun getItemInventory(side: ForgeDirection): IMEMonitor<IAEItemStack>? {
        return getStorageGrid(side)?.itemInventory
    }
}