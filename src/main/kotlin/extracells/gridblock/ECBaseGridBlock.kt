package extracells.gridblock

import appeng.api.networking.*
import appeng.api.networking.storage.IStorageGrid
import appeng.api.parts.PartItemStack
import appeng.api.storage.IMEMonitor
import appeng.api.storage.data.IAEFluidStack
import appeng.api.util.AEColor
import appeng.api.util.DimensionalCoord
import extracells.part.PartECBase
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.ForgeDirection
import java.util.*
open class ECBaseGridBlock(protected var host: PartECBase) : IGridBlock {
    protected var color: AEColor? = null
    protected var grid: IGrid? = null
    protected var usedChannels = 0
    override fun getConnectableSides(): EnumSet<ForgeDirection> {
        return EnumSet.noneOf(ForgeDirection::class.java)
    }

    override fun getFlags(): EnumSet<GridFlags> {
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL)
    }

    val fluidMonitor: IMEMonitor<IAEFluidStack?>?
        get() {
            val node = host.gridNode ?: return null
            val grid = node.grid ?: return null
            val storageGrid = grid.getCache<IStorageGrid>(IStorageGrid::class.java) ?: return null
            return storageGrid.fluidInventory
        }

    override fun getGridColor(): AEColor {
        return if (color == null) AEColor.Transparent else color!!
    }

    override fun getIdlePowerUsage(): Double {
        return host.powerUsage
    }

    override fun getLocation(): DimensionalCoord {
        return host.location
    }

    override fun getMachine(): IGridHost {
        return host
    }

    override fun getMachineRepresentation(): ItemStack? {
        return host.getItemStack(PartItemStack.Network)
    }

    override fun gridChanged() {}
    override fun isWorldAccessible(): Boolean {
        return false
    }

    override fun onGridNotification(notification: GridNotification) {}
    override fun setNetworkStatus(_grid: IGrid, _usedChannels: Int) {
        grid = _grid
        usedChannels = _usedChannels
    }
}