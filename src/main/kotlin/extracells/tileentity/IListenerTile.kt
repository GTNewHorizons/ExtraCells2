package extracells.tileentity

import appeng.api.networking.IGrid

interface IListenerTile {
    fun registerListener()
    fun removeListener()
    fun updateGrid(oldGrid: IGrid?, newGrid: IGrid?)
}