package extracells.api

import appeng.api.networking.security.BaseActionSource
import appeng.api.storage.IMEInventory
import appeng.api.storage.data.IAEFluidStack
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection

/**
 * A Registration Record for [IExternalStorageRegistry]
 */
interface IExternalGasStorageHandler {
    /**
     * if this can handle the provided inventory, return true. ( Generally skipped by AE, and it just calls getInventory
     * )
     *
     * @param te    to be handled tile entity
     * @param mySrc source
     *
     * @return true, if it can get a handler via getInventory
     */
    fun canHandle(te: TileEntity?, d: ForgeDirection?, mySrc: BaseActionSource?): Boolean

    /**
     * if this can handle the given inventory, return the a IMEInventory implementing class for it, if not return null
     *
     *
     * @param te      to be handled tile entity
     * @param d       direction
     * @param src     source
     *
     * @return The Handler for the inventory
     */
    fun getInventory(te: TileEntity?, d: ForgeDirection?, src: BaseActionSource?): IMEInventory<IAEFluidStack?>?
}