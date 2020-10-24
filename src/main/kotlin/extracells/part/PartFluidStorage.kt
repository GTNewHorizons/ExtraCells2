package extracells.part

import appeng.api.AEApi
import appeng.api.config.AccessRestriction
import appeng.api.config.SecurityPermissions
import appeng.api.networking.events.*
import appeng.api.parts.IPart
import appeng.api.parts.IPartCollisionHelper
import appeng.api.parts.IPartRenderHelper
import appeng.api.storage.ICellContainer
import appeng.api.storage.IMEInventory
import appeng.api.storage.IMEInventoryHandler
import appeng.api.storage.StorageChannel
import appeng.api.util.AEColor
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.container.ContainerBusFluidStorage
import extracells.gui.GuiBusFluidStorage
import extracells.inventory.HandlerPartStorageFluid
import extracells.network.packet.other.IFluidSlotPartOrBlock
import extracells.network.packet.other.PacketFluidSlot
import extracells.network.packet.part.PacketBusFluidStorage
import extracells.render.TextureManager
import extracells.util.PermissionUtil
import extracells.util.inventory.ECPrivateInventory
import extracells.util.inventory.IInventoryUpdateReceiver
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.Vec3
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import java.util.*

class PartFluidStorage : PartECBase(), ICellContainer, IInventoryUpdateReceiver, IFluidSlotPartOrBlock {
    private val fluidList = HashMap<FluidStack, Int>()
    private var priority = 0
    protected var handler = HandlerPartStorageFluid(this)
    private val filterFluids = arrayOfNulls<Fluid>(54)
    private var access = AccessRestriction.READ_WRITE
    val upgradeInventory: ECPrivateInventory = object : ECPrivateInventory("", 1, 1, this) {
        override fun isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean {
            return itemStack != null && AEApi.instance().definitions().materials().cardInverter().isSameAs(itemStack)
        }
    }

    override fun getDrops(drops: MutableList<ItemStack>, wrenched: Boolean) {
        for (stack in upgradeInventory.slots) {
            if (stack == null) continue
            drops.add(stack)
        }
    }

    override fun blinkCell(slot: Int) {}
    override fun cableConnectionRenderTo(): Int {
        return 3
    }

    override fun getBoxes(bch: IPartCollisionHelper) {
        bch.addBox(2.0, 2.0, 15.0, 14.0, 14.0, 16.0)
        bch.addBox(4.0, 4.0, 14.0, 12.0, 12.0, 15.0)
        bch.addBox(5.0, 5.0, 13.0, 11.0, 11.0, 14.0)
    }

    override fun getCellArray(channel: StorageChannel): List<IMEInventoryHandler<*>> {
        val list: MutableList<IMEInventoryHandler<*>> = ArrayList()
        if (channel == StorageChannel.FLUIDS) {
            list.add(handler)
        }
        updateNeighborFluids()
        return list
    }

    override fun getClientGuiElement(player: EntityPlayer): Any? {
        return GuiBusFluidStorage(this, player)
    }

    override val powerUsage: Double
        get() = 1.0

    override fun getPriority(): Int {
        return priority
    }

    override fun getLightLevel(): Int {
        return if (this.isPowered) 9 else 0
    }

    override fun getServerGuiElement(player: EntityPlayer): Any? {
        return ContainerBusFluidStorage(this, player)
    }

    override fun onActivate(player: EntityPlayer, pos: Vec3): Boolean {
        return PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, this as IPart) && super.onActivate(
                player, pos)
    }

    override fun onInventoryChanged() {
        handler.setInverted(
                AEApi.instance().definitions().materials().cardInverter().isSameAs(upgradeInventory.getStackInSlot(0)))
        saveData()
    }

    override fun onNeighborChanged() {
        handler.onNeighborChange()
        val node = gridNode
        if (node != null) {
            val grid = node.grid
            if (grid != null && wasChanged()) {
                grid.postEvent(MENetworkCellArrayUpdate())
                node.grid.postEvent(MENetworkStorageEvent(gridBlock.fluidMonitor, StorageChannel.FLUIDS))
                node.grid.postEvent(MENetworkCellArrayUpdate())
            }
            host.markForUpdate()
        }
    }

    @MENetworkEventSubscribe
    fun powerChange(event: MENetworkPowerStatusChange?) {
        val node = gridNode
        if (node != null) {
            val isNowActive = node.isActive
            if (isNowActive != isActive) {
                isActive = isNowActive
                onNeighborChanged()
                host.markForUpdate()
            }
        }
        node!!.grid.postEvent(MENetworkStorageEvent(gridBlock.fluidMonitor, StorageChannel.FLUIDS))
        node.grid.postEvent(MENetworkCellArrayUpdate())
    }

    override fun readFromNBT(data: NBTTagCompound) {
        super.readFromNBT(data)
        priority = data.getInteger("priority")
        for (i in 0..53) {
            filterFluids[i] = FluidRegistry.getFluid(data.getString("FilterFluid#$i"))
        }
        if (data.hasKey("access")) {
            try {
                access = AccessRestriction.valueOf(data.getString("access"))
            } catch (e: Throwable) {
            }
        }
        upgradeInventory.readFromNBT(data.getTagList("upgradeInventory", 10))
        onInventoryChanged()
        onNeighborChanged()
        handler.setPrioritizedFluids(filterFluids)
        handler.setAccessRestriction(access)
    }

    @SideOnly(Side.CLIENT)
    override fun renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks) {
        val ts = Tessellator.instance
        val side = TextureManager.STORAGE_SIDE.texture
        rh.setTexture(side, side, side,
                TextureManager.STORAGE_FRONT.textures[0], side, side)
        rh.setBounds(2f, 2f, 15f, 14f, 14f, 16f)
        rh.renderInventoryBox(renderer)
        rh.setBounds(4f, 4f, 14f, 12f, 12f, 15f)
        rh.renderInventoryBox(renderer)
        rh.setBounds(2f, 2f, 15f, 14f, 14f, 16f)
        rh.setInvColor(AEColor.Cyan.blackVariant)
        ts.setBrightness(15 shl 20 or 15 shl 4)
        rh.renderInventoryFace(TextureManager.STORAGE_FRONT.textures[1],
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(5f, 5f, 13f, 11f, 11f, 14f)
        renderInventoryBusLights(rh, renderer)
    }

    @SideOnly(Side.CLIENT)
    override fun renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper,
                              renderer: RenderBlocks) {
        val ts = Tessellator.instance
        val side = TextureManager.STORAGE_SIDE.texture
        rh.setTexture(side, side, side,
                TextureManager.STORAGE_FRONT.texture, side, side)
        rh.setBounds(2f, 2f, 15f, 14f, 14f, 16f)
        rh.renderBlock(x, y, z, renderer)
        ts.setColorOpaque_I(host.color.blackVariant)
        if (isActive) ts.setBrightness(15 shl 20 or 15 shl 4)
        rh.renderFace(x, y, z, TextureManager.STORAGE_FRONT.textures[1],
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(4f, 4f, 14f, 12f, 12f, 15f)
        rh.renderBlock(x, y, z, renderer)
        rh.setBounds(5f, 5f, 13f, 11f, 11f, 14f)
        renderStaticBusLights(x, y, z, rh, renderer)
    }

    override fun saveChanges(cellInventory: IMEInventory<*>?) {
        saveData()
    }

    fun sendInformation(player: EntityPlayer?) {
        PacketFluidSlot(Arrays.asList(*filterFluids))
                .sendPacketToPlayer(player)
        PacketBusFluidStorage(player, access, true)
                .sendPacketToPlayer(player)
    }

    override fun setFluid(_index: Int, _fluid: Fluid?, _player: EntityPlayer?) {
        filterFluids[_index] = _fluid
        handler.setPrioritizedFluids(filterFluids)
        sendInformation(_player)
        saveData()
    }

    fun updateAccess(access: AccessRestriction?) {
        this.access = access!!
        handler.setAccessRestriction(access)
        onNeighborChanged()
    }

    @MENetworkEventSubscribe
    fun updateChannels(channel: MENetworkChannelsChanged?) {
        val node = gridNode
        if (node != null) {
            val isNowActive = node.isActive
            if (isNowActive != isActive) {
                isActive = isNowActive
                onNeighborChanged()
                host.markForUpdate()
            }
        }
        node!!.grid.postEvent(
                MENetworkStorageEvent(gridBlock.fluidMonitor,
                        StorageChannel.FLUIDS))
        node.grid.postEvent(MENetworkCellArrayUpdate())
    }

    override fun writeToNBT(data: NBTTagCompound) {
        super.writeToNBT(data)
        data.setInteger("priority", priority)
        for (i in filterFluids.indices) {
            val fluid = filterFluids[i]
            if (fluid != null) data.setString("FilterFluid#$i", fluid.name) else data.setString("FilterFluid#$i", "")
        }
        data.setTag("upgradeInventory", upgradeInventory.writeToNBT())
        data.setString("access", access.name)
    }

    private fun updateNeighborFluids() {
        fluidList.clear()
        if (access == AccessRestriction.READ || access == AccessRestriction.READ_WRITE) {
            for (stack in handler.getAvailableItems(AEApi.instance().storage().createFluidList())) {
                val s = stack!!.fluidStack.copy()
                fluidList[s] = s.amount
            }
        }
    }

    private fun wasChanged(): Boolean {
        val fluids = HashMap<FluidStack, Int>()
        for (stack in handler.getAvailableItems(AEApi.instance().storage().createFluidList())) {
            val s = stack!!.fluidStack
            fluids[s] = s.amount
        }
        return fluids != fluidList
    }
}