package extracells.gridblock

import appeng.api.networking.*
import appeng.api.util.AEColor
import appeng.api.util.DimensionalCoord
import extracells.tileentity.TileEntityVibrationChamberFluid
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.ForgeDirection
import java.util.*

open class ECGridBlockVibrantChamber(val host : TileEntityVibrationChamberFluid) : IGridBlock {
    protected var grid: IGrid? = null
    protected var usedChannels: Int = 0

    override fun getIdlePowerUsage(): Double = host.powerUsage

    override fun getFlags(): EnumSet<GridFlags> = EnumSet.noneOf(GridFlags::class.java)

    override fun isWorldAccessible() = true

    override fun getLocation(): DimensionalCoord = host.location

    override fun getGridColor(): AEColor = AEColor.Transparent

    override fun onGridNotification(p0: GridNotification?) {}

    override fun setNetworkStatus(_grid: IGrid?, _usedChannels: Int) {
        this.grid = _grid
        this.usedChannels = _usedChannels
    }

    override fun getConnectableSides(): EnumSet<ForgeDirection> =
            EnumSet.of(ForgeDirection.DOWN, ForgeDirection.UP, ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH,
            ForgeDirection.WEST)

    override fun getMachine(): IGridHost = host

    override fun gridChanged() {
    }

    override fun getMachineRepresentation() : ItemStack = ItemStack(
            location.world.getBlock(location.x, location.y, location.z),
            1,
            location.world.getBlockMetadata(location.x, location.y, location.z)
    )
}