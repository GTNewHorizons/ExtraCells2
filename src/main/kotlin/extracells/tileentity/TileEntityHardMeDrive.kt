package extracells.tileentity

import appeng.api.AEApi
import appeng.api.networking.IGridNode
import appeng.api.networking.events.MENetworkCellArrayUpdate
import appeng.api.networking.security.IActionHost
import appeng.api.storage.ICellContainer
import appeng.api.storage.IMEInventory
import appeng.api.storage.IMEInventoryHandler
import appeng.api.storage.StorageChannel
import appeng.api.util.AECableType
import appeng.api.util.DimensionalCoord
import extracells.api.IECTileEntity
import extracells.gridblock.ECGridBlockHardMEDrive
import extracells.util.inventory.ECPrivateInventory
import extracells.util.inventory.IInventoryUpdateReceiver
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraftforge.common.util.ForgeDirection
import java.util.*
open class TileEntityHardMeDrive : TileBase(), IActionHost, IECTileEntity, ICellContainer, IInventoryUpdateReceiver {
    private val priority = 0
    var isFirstGridNode = true
    var cellStatuses = ByteArray(3)
    var fluidHandlers: List<IMEInventoryHandler<*>> = ArrayList()
    var itemHandlers: List<IMEInventoryHandler<*>> = ArrayList()
    private val gridBlock = ECGridBlockHardMEDrive(this)
    private val inventory: ECPrivateInventory = object : ECPrivateInventory(
            "extracells.part.drive", 3, 1, this) {
        val cellRegistry = AEApi.instance().registries().cell()
        override fun isItemValidForSlot(i: Int, itemStack: ItemStack?): Boolean {
            return cellRegistry.isCellHandled(itemStack)
        }
    }

    fun getInventory(): IInventory {
        return inventory
    }

    var node: IGridNode? = null
    override fun blinkCell(i: Int) {}
    override fun getActionableNode(): IGridNode? {
        return getGridNode(ForgeDirection.UNKNOWN)
    }

    override fun getCellArray(channel: StorageChannel): List<IMEInventoryHandler<*>> {
        if (!isActive) return ArrayList()
        return if (channel == StorageChannel.ITEMS) itemHandlers else fluidHandlers
    }

    override fun getPriority(): Int {
        return priority
    }

    override val location: DimensionalCoord
        get() = DimensionalCoord(this)
    override val powerUsage: Double
        get() = 0.0

    override fun getGridNode(forgeDirection: ForgeDirection): IGridNode? {
        if (isFirstGridNode && hasWorldObj() && !getWorldObj().isRemote) {
            isFirstGridNode = false
            try {
                node = AEApi.instance().createGridNode(gridBlock)
                node?.updateState()
            } catch (e: Exception) {
                isFirstGridNode = true
            }
        }
        return node
    }

    override fun getCableConnectionType(forgeDirection: ForgeDirection): AECableType {
        return AECableType.SMART
    }

    override fun securityBreak() {}
    override fun saveChanges(imeInventory: IMEInventory<*>?) {}

    //TODO
    val isActive: Boolean
        get() = true

    fun getColorByStatus(status: Int): Int {
        return when (status) {
            1 -> 0x00FF00
            2 -> 0xFFFF00
            3 -> 0xFF0000
            else -> 0x000000
        }
    }

    override fun onInventoryChanged() {
        itemHandlers = updateHandlers(StorageChannel.ITEMS)
        fluidHandlers = updateHandlers(StorageChannel.FLUIDS)
        for (i in cellStatuses.indices) {
            val stackInSlot = inventory.getStackInSlot(i)
            var inventoryHandler = AEApi.instance()
                    .registries().cell()
                    .getCellInventory(stackInSlot, null, StorageChannel.ITEMS)
            if (inventoryHandler == null) inventoryHandler = AEApi
                    .instance()
                    .registries()
                    .cell()
                    .getCellInventory(stackInSlot, null,
                            StorageChannel.FLUIDS)
            val cellHandler = AEApi.instance().registries().cell()
                    .getHandler(stackInSlot)
            if (cellHandler == null || inventoryHandler == null) {
                cellStatuses[i] = 0
            } else {
                cellStatuses[i] = cellHandler.getStatusForCell(
                        stackInSlot, inventoryHandler).toByte()
            }
        }
        val node = getGridNode(ForgeDirection.UNKNOWN)
        if (node != null) {
            val grid = node.grid
            grid?.postEvent(MENetworkCellArrayUpdate())
            getWorldObj().markBlockForUpdate(xCoord, yCoord, zCoord)
        }
    }

    private fun updateHandlers(channel: StorageChannel): List<IMEInventoryHandler<*>> {
        val cellRegistry = AEApi.instance().registries().cell()
        val handlers: MutableList<IMEInventoryHandler<*>> = ArrayList()
        for (i in 0 until inventory.sizeInventory) {
            val cell = inventory.getStackInSlot(i)
            if (cellRegistry.isCellHandled(cell)) {
                val cellInventory = cellRegistry
                        .getCellInventory(cell, null, channel)
                if (cellInventory != null) handlers.add(cellInventory)
            }
        }
        return handlers
    }

    override fun readFromNBT(tag: NBTTagCompound) {
        super.readFromNBT(tag)
        inventory.readFromNBT(tag.getTagList("inventory", 10))
        onInventoryChanged()
    }

    override fun writeToNBT(tag: NBTTagCompound) {
        super.writeToNBT(tag)
        tag.setTag("inventory", inventory.writeToNBT())
    }

    override fun getDescriptionPacket(): Packet {
        val nbtTag = NBTTagCompound()
        writeToNBT(nbtTag)
        for ((i, aCellStati) in cellStatuses.withIndex()) {
            nbtTag.setByte("status#$i", aCellStati)
        }
        return S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbtTag)
    }
}