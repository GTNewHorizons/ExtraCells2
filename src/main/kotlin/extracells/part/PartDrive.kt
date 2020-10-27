package extracells.part

import appeng.api.AEApi
import appeng.api.config.SecurityPermissions
import appeng.api.networking.events.MENetworkCellArrayUpdate
import appeng.api.networking.events.MENetworkChannelsChanged
import appeng.api.networking.events.MENetworkEventSubscribe
import appeng.api.networking.events.MENetworkPowerStatusChange
import appeng.api.parts.IPart
import appeng.api.parts.IPartCollisionHelper
import appeng.api.parts.IPartHost
import appeng.api.parts.IPartRenderHelper
import appeng.api.storage.ICellContainer
import appeng.api.storage.IMEInventory
import appeng.api.storage.IMEInventoryHandler
import appeng.api.storage.StorageChannel
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.container.ContainerDrive
import extracells.gui.GuiDrive
import extracells.render.TextureManager
import extracells.util.PermissionUtil
import extracells.util.inventory.ECPrivateInventory
import extracells.util.inventory.IInventoryUpdateReceiver
import io.netty.buffer.ByteBuf
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Vec3
import net.minecraftforge.common.util.ForgeDirection
import java.io.IOException
import java.util.*
open class PartDrive : PartECBase(), ICellContainer, IInventoryUpdateReceiver {
    private val priority = 0
    private val blinkTimers
            : ShortArray = TODO()
    private val cellStatuses = ByteArray(6)
    var fluidHandlers: List<IMEInventoryHandler<*>> = ArrayList()
    var itemHandlers: List<IMEInventoryHandler<*>> = ArrayList()
    val inventory: ECPrivateInventory = object : ECPrivateInventory(
            "extracells.part.drive", 6, 1, this) {
        val cellRegistry = AEApi.instance().registries().cell()
        override fun isItemValidForSlot(i: Int, itemStack: ItemStack?): Boolean {
            return cellRegistry.isCellHandled(itemStack)
        }
    }

    override fun addToWorld() {
        super.addToWorld()
        onInventoryChanged()
    }

    override fun blinkCell(slot: Int) {
        if (slot > 0 && slot < blinkTimers.size) blinkTimers[slot] = 15
    }

    override fun cableConnectionRenderTo(): Int {
        return 2
    }

    override fun getBoxes(bch: IPartCollisionHelper) {
        bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0)
        bch.addBox(5.0, 5.0, 13.0, 11.0, 11.0, 14.0)
    }

    override fun getCellArray(channel: StorageChannel): List<IMEInventoryHandler<*>> {
        if (!isActive) return ArrayList()
        return if (channel == StorageChannel.ITEMS) itemHandlers else fluidHandlers
    }

    override fun getClientGuiElement(player: EntityPlayer): Any? {
        return GuiDrive(this, player)
    }

    fun getColorByStatus(status: Int): Int {
        return when (status) {
            1 -> 0x00FF00
            2 -> 0xFFFF00
            3 -> 0xFF0000
            else -> 0x000000
        }
    }

    override fun getDrops(drops: MutableList<ItemStack>, wrenched: Boolean) {
        if (!wrenched) for (i in 0 until inventory.sizeInventory) {
            val cell = inventory.getStackInSlot(i)
            if (cell != null) drops.add(cell)
        }
    }

    override fun getPriority(): Int {
        return priority
    }

    override fun getServerGuiElement(player: EntityPlayer?): Any? {
        return player?.let { ContainerDrive(this, it) }
    }

    override fun onActivate(player: EntityPlayer?, pos: Vec3?): Boolean {
        return if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
                        this as IPart)) {
            super.onActivate(player, pos)
        } else false
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
        val node = gridNode
        if (node != null) {
            val grid = node.grid
            grid?.postEvent(MENetworkCellArrayUpdate())
            host?.markForUpdate()
        }
        saveData()
    }

    @MENetworkEventSubscribe
    fun powerChange(event: MENetworkPowerStatusChange?) {
        val node = gridNode
        if (node != null) {
            val isNowActive = node.isActive
            if (isNowActive != isActive) {
                isActive = isNowActive
                onNeighborChanged()
                host?.markForUpdate()
            }
        }
        node!!.grid.postEvent(MENetworkCellArrayUpdate())
    }

    override fun readFromNBT(data: NBTTagCompound) {
        super.readFromNBT(data)
        inventory.readFromNBT(data.getTagList("inventory", 10))
        onInventoryChanged()
    }

    @Throws(IOException::class)
    override fun readFromStream(data: ByteBuf): Boolean {
        super.readFromStream(data)
        for (i in cellStatuses.indices) cellStatuses[i] = data.readByte()
        return true
    }

    @SideOnly(Side.CLIENT)
    override fun renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks) {
        val side = TextureManager.DRIVE_SIDE.texture
        val front = TextureManager.DRIVE_FRONT.textures
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 15.999f)
        rh.renderInventoryFace(front[3], ForgeDirection.SOUTH, renderer)
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 16f)
        rh.setTexture(side, side, side, front[0], side, side)
        rh.renderInventoryBox(renderer)
        rh.setBounds(5f, 5f, 13f, 11f, 11f, 14f)
        renderInventoryBusLights(rh, renderer)
    }

    @SideOnly(Side.CLIENT)
    override fun renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper,
                              renderer: RenderBlocks) {
        val ts = Tessellator.instance
        val side = TextureManager.DRIVE_SIDE.texture
        val front = TextureManager.DRIVE_FRONT.textures
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 15.999f)
        rh.renderFace(x, y, z, front[3], ForgeDirection.SOUTH, renderer)
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 16f)
        rh.setTexture(side, side, side, front[0], side, side)
        rh.renderBlock(x, y, z, renderer)
        ts.setColorOpaque_I(0xFFFFFF)
        for (i in 0..1) {
            for (j in 0..2) {
                if (cellStatuses[j + i * 3] > 0) {
                    if (if (this.side == ForgeDirection.EAST
                                    || this.side == ForgeDirection.WEST) i == 1 else i == 0) rh.setBounds(8f,
                            12 - j * 3.toFloat(), 14f, 13f, 10 - j * 3.toFloat(), 16f) else rh.setBounds(3f,
                            12 - j * 3.toFloat(), 14f, 8f, 10 - j * 3.toFloat(), 16f)
                    rh.renderFace(x, y, z, front[1], ForgeDirection.SOUTH,
                            renderer)
                }
            }
        }
        for (i in 0..1) {
            for (j in 0..2) {
                if (if (this.side == ForgeDirection.EAST
                                || this.side == ForgeDirection.WEST) i == 1 else i == 0) rh.setBounds(8f,
                        12 - j * 3.toFloat(), 14f, 13f, 10 - j * 3.toFloat(), 16f) else rh.setBounds(3f,
                        12 - j * 3.toFloat(), 14f, 8f, 10 - j * 3.toFloat(), 16f)
                ts.setColorOpaque_I(getColorByStatus(cellStatuses[j + i
                        * 3].toInt()))
                ts.setBrightness(13 shl 20 or 13 shl 4)
                rh.renderFace(x, y, z, front[2], ForgeDirection.SOUTH, renderer)
            }
        }
        rh.setBounds(5f, 5f, 13f, 11f, 11f, 14f)
        renderStaticBusLights(x, y, z, rh, renderer)
    }

    override fun saveChanges(cellInventory: IMEInventory<*>?) {
        host?.markForSave()
    }

    override fun setPartHostInfo(_side: ForgeDirection, _host: IPartHost,
                                 _tile: TileEntity) {
        super.setPartHostInfo(_side, _host, _tile)
        onInventoryChanged()
    }

    @MENetworkEventSubscribe
    fun updateChannels(channel: MENetworkChannelsChanged?) {
        val node = gridNode
        if (node != null) {
            val isNowActive = node.isActive
            if (isNowActive != isActive) {
                isActive = isNowActive
                onNeighborChanged()
                host?.markForUpdate()
            }
        }
        node!!.grid.postEvent(MENetworkCellArrayUpdate())
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

    override fun writeToNBT(data: NBTTagCompound) {
        super.writeToNBT(data)
        data.setTag("inventory", inventory.writeToNBT())
    }

    @Throws(IOException::class)
    override fun writeToStream(data: ByteBuf) {
        super.writeToStream(data)
        for (aCellStati in cellStatuses) {
            data.writeByte(aCellStati.toInt())
        }
    }
}