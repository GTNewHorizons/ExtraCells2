package extracells.inventory

import appeng.api.AEApi
import appeng.api.config.AccessRestriction
import appeng.api.config.Actionable
import appeng.api.implementations.tiles.ITileStorageMonitorable
import appeng.api.networking.events.MENetworkCellArrayUpdate
import appeng.api.networking.events.MENetworkStorageEvent
import appeng.api.networking.security.BaseActionSource
import appeng.api.networking.security.MachineSource
import appeng.api.networking.storage.IBaseMonitor
import appeng.api.storage.*
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IItemList
import extracells.part.PartFluidStorage
import extracells.util.FluidUtil
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.IFluidHandler
import java.util.*

class HandlerPartStorageFluid(protected var node: PartFluidStorage) : IMEInventoryHandler<IAEFluidStack?>, IMEMonitorHandlerReceiver<IAEFluidStack?> {
    protected var tank: IFluidHandler? = null
    protected var access = AccessRestriction.READ_WRITE
    protected var prioritizedFluids: MutableList<Fluid> = ArrayList()
    protected var inverted = false
    private var externalHandler: IExternalStorageHandler? = null
    protected var tile: TileEntity? = null
    var externalSystem: ITileStorageMonitorable? = null
    override fun canAccept(input: IAEFluidStack?): Boolean {
        if (!node.isActive) return false else if (tank == null && externalSystem == null && externalHandler == null || !(access == AccessRestriction.WRITE || access == AccessRestriction.READ_WRITE) || input == null) return false else if (externalSystem != null) {
            val monitor = externalSystem!!.getMonitorable(
                    node.side.opposite, MachineSource(
                    node))
                    ?: return false
            val fluidInventory = monitor
                    .fluidInventory
            return fluidInventory != null && fluidInventory.canAccept(input)
        } else if (externalHandler != null) {
            val inventory: IMEInventory<IAEFluidStack>? = externalHandler!!.getInventory(tile, node.side.opposite,
                    StorageChannel.FLUIDS, MachineSource(
                    node))
            return inventory != null
        }
        val infoArray = tank!!.getTankInfo(node.side.opposite)
        if (infoArray != null && infoArray.size > 0) {
            for (tank in infoArray) {
                if (tank.fluid == null) return isPrioritized(
                        input) else if (tank.fluid.fluidID == input.fluidStack.fluidID) {
                    if (!isPrioritized(input)) return false
                    if (tank.fluid.amount < tank.capacity) return true
                }
            }
        }
        return false
    }

    override fun extractItems(request: IAEFluidStack, mode: Actionable, src: BaseActionSource): IAEFluidStack? {
        if (!node.isActive
                || !(access == AccessRestriction.READ || access == AccessRestriction.READ_WRITE)) return null
        if (externalSystem != null && request != null) {
            val monitor = externalSystem!!.getMonitorable(
                    node.side.opposite, src) ?: return null
            val fluidInventory = monitor
                    .fluidInventory ?: return null
            return fluidInventory.extractItems(request, mode, src)
        } else if (externalHandler != null && request != null) {
            val inventory = externalHandler!!.getInventory(tile, node.side.opposite, StorageChannel.FLUIDS,
                    MachineSource(node))
                    ?: return null
            return inventory.extractItems(request, mode, MachineSource(node))
        }
        if (tank == null || request == null || access == AccessRestriction.WRITE || access == AccessRestriction.NO_ACCESS) return null
        val toDrain = request.fluidStack
        if (!tank!!.canDrain(node.side.opposite, toDrain.getFluid())) return null
        val drain = tank!!.drain(
                node.side.opposite, FluidStack(toDrain.getFluid(), toDrain.amount), mode == Actionable.MODULATE)
        return if (drain == null) {
            null
        } else if (drain.amount == 0) {
            null
        } else if (drain.amount == toDrain.amount) {
            request
        } else {
            FluidUtil.createAEFluidStack(toDrain.fluidID, drain.amount.toLong())
        }
    }

    override fun getAccess(): AccessRestriction {
        return access
    }

    override fun getAvailableItems(
            out: IItemList<IAEFluidStack>): IItemList<IAEFluidStack> {
        if (!node.isActive || !(access == AccessRestriction.READ || access == AccessRestriction.READ_WRITE)) return out
        if (externalSystem != null) {
            val monitor = externalSystem!!.getMonitorable(
                    node.side.opposite, MachineSource(node))
                    ?: return out
            val fluidInventory = monitor.fluidInventory ?: return out
            val list = externalSystem!!.getMonitorable(
                    node.side.opposite, MachineSource(node)).fluidInventory.storageList
            for (stack in list) {
                out.add(stack)
            }
        } else if (externalHandler != null) {
            val inventory = externalHandler!!.getInventory(tile, node.side.opposite, StorageChannel.FLUIDS,
                    MachineSource(
                            node))
                    ?: return out
            val list = inventory.getAvailableItems(AEApi.instance().storage().createFluidList())
            for (stack in list) {
                out.add(stack)
            }
        } else if (tank != null) {
            val infoArray = tank!!.getTankInfo(node.side.opposite)
            if (infoArray != null && infoArray.size > 0) {
                for (info in infoArray) {
                    if (info.fluid != null) out.add(AEApi.instance().storage().createFluidStack(info.fluid))
                }
            }
        }
        return out
    }

    override fun getChannel(): StorageChannel {
        return StorageChannel.FLUIDS
    }

    override fun getPriority(): Int {
        return node.priority
    }

    override fun getSlot(): Int {
        return 0
    }

    override fun injectItems(input: IAEFluidStack, mode: Actionable,
                             src: BaseActionSource): IAEFluidStack? {
        if (!(access == AccessRestriction.WRITE || access == AccessRestriction.READ_WRITE)) return null
        if (externalSystem != null && input != null) {
            val monitor = externalSystem!!.getMonitorable(node.side.opposite, src) ?: return input
            val fluidInventory = monitor.fluidInventory ?: return input
            return fluidInventory.injectItems(input, mode, src)
        } else if (externalHandler != null && input != null) {
            val inventory = externalHandler!!.getInventory(tile, node.side.opposite, StorageChannel.FLUIDS,
                    MachineSource(node))
                    ?: return input
            return inventory.injectItems(input, mode, MachineSource(node))
        }
        if (tank == null || input == null || !canAccept(input)) return input
        val toFill = input.fluidStack
        var filled = 0
        var filled2 = 0
        do {
            filled2 = tank!!.fill(node.side.opposite, FluidStack(toFill.getFluid(), toFill.amount - filled),
                    mode == Actionable.MODULATE)
            filled = filled + filled2
        } while (filled2 != 0 && filled != toFill.amount)
        return if (filled == toFill.amount) null else FluidUtil.createAEFluidStack(toFill.fluidID,
                toFill.amount - filled.toLong())
    }

    override fun isPrioritized(input: IAEFluidStack?): Boolean {
        if (input == null) return false else if (prioritizedFluids.isEmpty()) return true
        for (fluid in prioritizedFluids) if (fluid === input.fluid) return !inverted
        return inverted
    }

    override fun isValid(verificationToken: Any): Boolean {
        return true
    }

    override fun onListUpdate() {}
    fun onNeighborChange() {
        if (externalSystem != null) {
            val monitor = externalSystem!!.getMonitorable(
                    node.side.opposite, MachineSource(node))
            if (monitor != null) {
                val fluidInventory = monitor.fluidInventory
                fluidInventory?.removeListener(this)
            }
        }
        tank = null
        val orientation = node.side
        val hostTile = node.hostTile ?: return
        if (hostTile.worldObj == null) return
        val tileEntity = hostTile.worldObj.getTileEntity(
                hostTile.xCoord + orientation!!.offsetX,
                hostTile.yCoord + orientation.offsetY,
                hostTile.zCoord + orientation.offsetZ)
        tile = tileEntity
        tank = null
        externalSystem = null
        if (tileEntity == null) {
            externalHandler = null
            return
        }
        externalHandler = AEApi.instance().registries().externalStorage().getHandler(tileEntity, node.side.opposite,
                StorageChannel.FLUIDS, MachineSource(
                node))
        if (tileEntity is ITileStorageMonitorable) {
            externalSystem = tileEntity
            val monitor = externalSystem!!.getMonitorable(
                    node.side.opposite, MachineSource(
                    node))
                    ?: return
            val fluidInventory = monitor
                    .fluidInventory ?: return
            fluidInventory.addListener(this, null)
        } else if (externalHandler == null && tileEntity is IFluidHandler) tank = tileEntity
    }

    override fun postChange(monitor: IBaseMonitor<IAEFluidStack?>,
                            change: Iterable<IAEFluidStack?>, actionSource: BaseActionSource) {
        val gridNode = node.gridNode
        if (gridNode != null) {
            val grid = gridNode.grid
            if (grid != null) {
                grid.postEvent(MENetworkCellArrayUpdate())
                gridNode.grid.postEvent(MENetworkStorageEvent(node.gridBlock.fluidMonitor, StorageChannel.FLUIDS))
            }
            node.host.markForUpdate()
        }
    }

    fun setAccessRestriction(access: AccessRestriction) {
        this.access = access
    }

    fun setInverted(_inverted: Boolean) {
        inverted = _inverted
    }

    fun setPrioritizedFluids(_fluids: Array<Fluid?>) {
        prioritizedFluids.clear()
        for (fluid in _fluids) {
            if (fluid != null) prioritizedFluids.add(fluid)
        }
    }

    override fun validForPass(i: Int): Boolean {
        return true // TODO
    }
}