package extracells.gridblock

import appeng.api.networking.*
import appeng.api.networking.storage.IStorageGrid
import appeng.api.util.AEColor
import appeng.api.util.DimensionalCoord
import extracells.api.IECTileEntity
import extracells.tileentity.IListenerTile
import extracells.tileentity.TileEntityFluidFiller
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.ForgeDirection
import java.util.*
open class ECFluidGridBlock(protected var host: IECTileEntity) : IGridBlock {
    protected var grid: IGrid? = null
    protected var usedChannels = 0
    override fun getConnectableSides(): EnumSet<ForgeDirection> {
        return EnumSet.of(ForgeDirection.DOWN, ForgeDirection.UP,
                ForgeDirection.NORTH, ForgeDirection.EAST,
                ForgeDirection.SOUTH, ForgeDirection.WEST)
    }

    override fun getFlags(): EnumSet<GridFlags> {
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL)
    }

    override fun getGridColor(): AEColor {
        return AEColor.Transparent
    }

    override fun getIdlePowerUsage(): Double {
        return host.powerUsage
    }

    override fun getLocation(): DimensionalCoord? {
        return host.location
    }

    override fun getMachine(): IGridHost? {
        return host
    }

    override fun getMachineRepresentation(): ItemStack? {
        val loc = location ?: return null
        return ItemStack(loc.world.getBlock(loc.x, loc.y, loc.z), 1,
                loc.world.getBlockMetadata(loc.x, loc.y, loc.z))
    }

    override fun gridChanged() {}
    override fun isWorldAccessible(): Boolean {
        return true
    }

    override fun onGridNotification(notification: GridNotification) {}
    override fun setNetworkStatus(_grid: IGrid, _usedChannels: Int) {
        if (grid != null && host is IListenerTile
                && grid !== _grid) {
            (host as IListenerTile).updateGrid(grid, _grid)
            grid = _grid
            usedChannels = _usedChannels
            if (host is TileEntityFluidFiller
                    && grid!!.getCache<IGridCache?>(
                            IStorageGrid::class.java) != null) (host as TileEntityFluidFiller).postChange(
                    (grid!!.getCache<IGridCache>(IStorageGrid::class.java) as IStorageGrid)
                            .fluidInventory, null, null)
        } else {
            grid = _grid
            usedChannels = _usedChannels
        }
    }
}