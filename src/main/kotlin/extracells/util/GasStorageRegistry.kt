package extracells.util

import appeng.api.networking.security.BaseActionSource
import extracells.api.IExternalGasStorageHandler
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection

object GasStorageRegistry {
    internal val handler: MutableList<IExternalGasStorageHandler> = mutableListOf()
    fun addExternalStorageInterface(esh: IExternalGasStorageHandler) {
        handler += esh
    }

    fun getHandler(te: TileEntity, opposite: ForgeDirection, mySrc: BaseActionSource): IExternalGasStorageHandler? {
        var ret : IExternalGasStorageHandler? = null
        handler.stream().filter { it.canHandle(te, opposite, mySrc) }.findAny().ifPresent { ret = it }
        return ret
    }
}