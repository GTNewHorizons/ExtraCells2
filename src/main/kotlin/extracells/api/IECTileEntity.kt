package extracells.api

import appeng.api.networking.IGridHost
import appeng.api.util.DimensionalCoord

interface IECTileEntity : IGridHost {
    val location: DimensionalCoord
    val powerUsage: Double
}