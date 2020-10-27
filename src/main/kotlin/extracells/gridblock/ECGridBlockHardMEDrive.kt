package extracells.gridblock

import appeng.api.networking.*
import appeng.api.util.AEColor
import appeng.api.util.DimensionalCoord
import extracells.tileentity.TileEntityHardMeDrive
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.ForgeDirection
import java.util.*

open class ECGridBlockHardMEDrive(val host: TileEntityHardMeDrive) : IGridBlock{
    protected var grid: IGrid? = null
    protected var usedChannels: Int = 0

    override fun getConnectableSides() : EnumSet<ForgeDirection> =
    EnumSet.of(ForgeDirection.DOWN, ForgeDirection.UP, ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH,
            ForgeDirection.WEST)

    override fun getFlags(): EnumSet<GridFlags> = EnumSet.of(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY)

    override fun getGridColor() = AEColor.Transparent

    override fun getIdlePowerUsage() = host.powerUsage

    override fun getLocation(): DimensionalCoord = host.location

    override fun getMachine(): IGridHost = host

    override fun getMachineRepresentation() : ItemStack = ItemStack(
            location.world.getBlock(location.x, location.y, location.z),
            1,
            location.world.getBlockMetadata(location.x, location.y, location.z)
    )

    override fun gridChanged() {}

    override fun isWorldAccessible() = true

    override fun onGridNotification(notification: GridNotification) {}

    override fun setNetworkStatus(_grid: IGrid, _usedChannels: Int) {
        this.grid = _grid
        this.usedChannels = _usedChannels
    }

}
