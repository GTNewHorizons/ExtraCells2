package extracells.util

import appeng.api.implementations.tiles.IChestOrDrive
import appeng.api.implementations.tiles.IMEChest
import appeng.api.networking.security.PlayerSource
import appeng.api.storage.*
import extracells.api.IFluidStorageCell
import extracells.inventory.HandlerItemPlayerStorageFluid
import extracells.inventory.HandlerItemStorageFluid
import extracells.network.GuiHandler.getGuiId
import extracells.network.GuiHandler.launchGui
import extracells.render.TextureManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection

class FluidCellHandler : ICellHandler {
    override fun cellIdleDrain(`is`: ItemStack, handler: IMEInventory<*>?): Double {
        return 0
    }

    override fun getCellInventory(itemStack: ItemStack, saveProvider: ISaveProvider, channel: StorageChannel): IMEInventoryHandler<*>? {
        return if (channel == StorageChannel.ITEMS || itemStack.item !is IFluidStorageCell) {
            null
        } else HandlerItemStorageFluid(itemStack, saveProvider,
                (itemStack.item as IFluidStorageCell).getFilter(itemStack))
    }

    fun getCellInventoryPlayer(itemStack: ItemStack, player: EntityPlayer): IMEInventoryHandler<*> {
        return HandlerItemPlayerStorageFluid(itemStack, null,
                (itemStack.item as IFluidStorageCell).getFilter(itemStack), player)
    }

    override fun getStatusForCell(`is`: ItemStack, handler: IMEInventory<*>?): Int {
        if (handler == null) {
            return 0
        }
        val inventory = handler as HandlerItemStorageFluid
        if (inventory.freeBytes() == 0) {
            return 3
        }
        return if (inventory.isFormatted || inventory.usedTypes() == inventory.totalTypes()) {
            2
        } else 1
    }

    override fun getTopTexture_Dark(): IIcon {
        return TextureManager.TERMINAL_FRONT.textures[0]
    }

    override fun getTopTexture_Light(): IIcon {
        return TextureManager.TERMINAL_FRONT.textures[2]
    }

    override fun getTopTexture_Medium(): IIcon {
        return TextureManager.TERMINAL_FRONT.textures[1]
    }

    override fun isCell(`is`: ItemStack): Boolean {
        return `is` != null && `is`.item != null && `is`.item is IFluidStorageCell
    }

    override fun openChestGui(player: EntityPlayer, chest: IChestOrDrive, cellHandler: ICellHandler, inv: IMEInventoryHandler<*>?, `is`: ItemStack, chan: StorageChannel) {
        if (chan != StorageChannel.FLUIDS) {
            return
        }
        var monitorable: IStorageMonitorable? = null
        if (chest != null) {
            monitorable = (chest as IMEChest).getMonitorable(ForgeDirection.UNKNOWN, PlayerSource(player, chest))
        }
        if (monitorable != null) {
            launchGui(getGuiId(0), player, arrayOf(monitorable.fluidInventory))
        }
    }
}