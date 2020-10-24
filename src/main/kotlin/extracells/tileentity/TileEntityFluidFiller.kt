package extracells.tileentity

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.implementations.ICraftingPatternItem
import appeng.api.networking.IGrid
import appeng.api.networking.IGridNode
import appeng.api.networking.crafting.ICraftingPatternDetails
import appeng.api.networking.crafting.ICraftingProvider
import appeng.api.networking.crafting.ICraftingProviderHelper
import appeng.api.networking.events.MENetworkCellArrayUpdate
import appeng.api.networking.events.MENetworkCraftingPatternChange
import appeng.api.networking.events.MENetworkEventSubscribe
import appeng.api.networking.events.MENetworkPowerStatusChange
import appeng.api.networking.security.BaseActionSource
import appeng.api.networking.security.IActionHost
import appeng.api.networking.security.MachineSource
import appeng.api.networking.storage.IBaseMonitor
import appeng.api.networking.storage.IStorageGrid
import appeng.api.storage.IMEMonitor
import appeng.api.storage.IMEMonitorHandlerReceiver
import appeng.api.storage.data.IAEFluidStack
import appeng.api.util.AECableType
import appeng.api.util.DimensionalCoord
import cpw.mods.fml.common.FMLCommonHandler
import extracells.api.IECTileEntity
import extracells.gridblock.ECFluidGridBlock
import extracells.util.FluidUtil
import net.minecraft.init.Items
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.network.NetworkManager
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import java.util.*

class TileEntityFluidFiller : TileBase(), IActionHost, ICraftingProvider, IECTileEntity, IMEMonitorHandlerReceiver<IAEFluidStack>, IListenerTile {
    private val gridBlock: ECFluidGridBlock
    private var node: IGridNode? = null
    var fluids: MutableList<Fluid> = ArrayList()
    var containerItem: ItemStack? = ItemStack(Items.bucket)
    var returnStack: ItemStack? = null
    var ticksToFinish = 0
    private var isFirstGetGridNode = true
    private val encodedPattern = AEApi.instance().definitions().items().encodedPattern().maybeItem().orNull()
    @MENetworkEventSubscribe
    fun cellUpdate(event: MENetworkCellArrayUpdate?) {
        val storage = storageGrid
        if (storage != null) postChange(storage.fluidInventory, null, null)
    }

    override fun getActionableNode(): IGridNode {
        if (FMLCommonHandler.instance().effectiveSide.isClient) return null
        if (node == null) {
            node = AEApi.instance().createGridNode(gridBlock)
        }
        return node!!
    }

    override fun getCableConnectionType(dir: ForgeDirection): AECableType {
        return AECableType.DENSE
    }

    override fun getDescriptionPacket(): Packet {
        val nbtTag = NBTTagCompound()
        writeToNBT(nbtTag)
        return S35PacketUpdateTileEntity(xCoord, yCoord,
                zCoord, 1, nbtTag)
    }

    override fun getGridNode(dir: ForgeDirection): IGridNode {
        if (FMLCommonHandler.instance().side.isClient
                && (getWorldObj() == null || getWorldObj().isRemote)) return null
        if (isFirstGetGridNode) {
            isFirstGetGridNode = false
            actionableNode.updateState()
            val storage = storageGrid
            storage!!.fluidInventory.addListener(this, null)
        }
        return node!!
    }

    override val location: DimensionalCoord
        get() = DimensionalCoord(this)

    private fun getPattern(emptyContainer: ItemStack?,
                           filledContainer: ItemStack?): ItemStack {
        val `in` = NBTTagList()
        val out = NBTTagList()
        `in`.appendTag(emptyContainer!!.writeToNBT(NBTTagCompound()))
        out.appendTag(filledContainer!!.writeToNBT(NBTTagCompound()))
        val itemTag = NBTTagCompound()
        itemTag.setTag("in", `in`)
        itemTag.setTag("out", out)
        itemTag.setBoolean("crafting", false)
        val pattern = ItemStack(encodedPattern)
        pattern.tagCompound = itemTag
        return pattern
    }

    override val powerUsage: Double
        get() = 1.0
    private val storageGrid: IStorageGrid?
        private get() {
            node = getGridNode(ForgeDirection.UNKNOWN)
            if (node == null) return null
            val grid = node!!.grid ?: return null
            return grid.getCache(IStorageGrid::class.java)
        }

    override fun isBusy(): Boolean {
        return returnStack != null
    }

    override fun isValid(verificationToken: Any): Boolean {
        return true
    }

    override fun onDataPacket(net: NetworkManager, pkt: S35PacketUpdateTileEntity) {
        readFromNBT(pkt.func_148857_g())
    }

    override fun onListUpdate() {}
    override fun postChange(monitor: IBaseMonitor<IAEFluidStack>,
                            change: Iterable<IAEFluidStack>, actionSource: BaseActionSource) {
        val oldFluids: MutableList<Fluid> = ArrayList(fluids)
        var mustUpdate = false
        fluids.clear()
        for (fluid in (monitor as IMEMonitor<IAEFluidStack>)
                .storageList) {
            if (!oldFluids.contains(fluid.fluid)) mustUpdate = true else oldFluids.remove(fluid.fluid)
            fluids.add(fluid.fluid)
        }
        if (!(oldFluids.isEmpty() && !mustUpdate)) {
            if (getGridNode(ForgeDirection.UNKNOWN) != null
                    && getGridNode(ForgeDirection.UNKNOWN).grid != null) {
                getGridNode(ForgeDirection.UNKNOWN).grid.postEvent(
                        MENetworkCraftingPatternChange(this,
                                getGridNode(ForgeDirection.UNKNOWN)))
            }
        }
    }

    fun postUpdateEvent() {
        if (getGridNode(ForgeDirection.UNKNOWN) != null
                && getGridNode(ForgeDirection.UNKNOWN).grid != null) {
            getGridNode(ForgeDirection.UNKNOWN).grid.postEvent(
                    MENetworkCraftingPatternChange(this,
                            getGridNode(ForgeDirection.UNKNOWN)))
        }
    }

    @MENetworkEventSubscribe
    fun powerUpdate(event: MENetworkPowerStatusChange?) {
        val storage = storageGrid
        if (storage != null) postChange(storage.fluidInventory, null, null)
    }

    override fun provideCrafting(craftingTracker: ICraftingProviderHelper) {
        val storage = storageGrid ?: return
        val fluidStorage = storage.fluidInventory
        for (fluidStack in fluidStorage.storageList) {
            val fluid = fluidStack.fluid ?: continue
            val maxCapacity = FluidUtil.getCapacity(containerItem)
            if (maxCapacity == 0) continue
            val filled = FluidUtil.fillStack(
                    containerItem!!.copy(), FluidStack(fluid,
                    maxCapacity))
            if (filled!!.right == null) continue
            val pattern = getPattern(containerItem, filled.right)
            val patter = pattern
                    .item as ICraftingPatternItem
            craftingTracker.addCraftingOption(this,
                    patter.getPatternForItem(pattern, getWorldObj()))
        }
    }

    override fun pushPattern(patternDetails: ICraftingPatternDetails,
                             table: InventoryCrafting): Boolean {
        if (returnStack != null) return false
        val filled = patternDetails.condensedOutputs[0]
                .itemStack
        val fluid = FluidUtil.getFluidFromContainer(filled)
        val storage = storageGrid ?: return false
        val fluidStack = AEApi
                .instance()
                .storage()
                .createFluidStack(
                        FluidStack(
                                fluid!!.getFluid(),
                                FluidUtil.getCapacity(patternDetails
                                        .condensedInputs[0].itemStack)))
        val extracted = storage.fluidInventory
                .extractItems(fluidStack.copy(), Actionable.SIMULATE,
                        MachineSource(this))
        if (extracted == null
                || extracted.stackSize != fluidStack.stackSize) return false
        storage.fluidInventory.extractItems(fluidStack,
                Actionable.MODULATE, MachineSource(this))
        returnStack = filled
        ticksToFinish = 40
        return true
    }

    override fun readFromNBT(tagCompound: NBTTagCompound) {
        super.readFromNBT(tagCompound)
        if (tagCompound.hasKey("container")) containerItem = ItemStack.loadItemStackFromNBT(tagCompound
                .getCompoundTag("container")) else if (tagCompound.hasKey("isContainerEmpty")
                && tagCompound.getBoolean("isContainerEmpty")) containerItem = null
        if (tagCompound.hasKey("return")) returnStack = ItemStack.loadItemStackFromNBT(tagCompound
                .getCompoundTag("return")) else if (tagCompound.hasKey("isReturnEmpty")
                && tagCompound.getBoolean("isReturnEmpty")) returnStack = null
        if (tagCompound.hasKey("time")) ticksToFinish = tagCompound.getInteger("time")
        if (hasWorldObj()) {
            val node = getGridNode(ForgeDirection.UNKNOWN)
            if (tagCompound.hasKey("nodes") && node != null) {
                node.loadFromNBT("node0", tagCompound.getCompoundTag("nodes"))
                node.updateState()
            }
        }
    }

    override fun registerListener() {
        val storage = storageGrid ?: return
        postChange(storage.fluidInventory, null, null)
        storage.fluidInventory.addListener(this, null)
    }

    override fun removeListener() {
        val storage = storageGrid ?: return
        storage.fluidInventory.removeListener(this)
    }

    override fun securityBreak() {
        if (getWorldObj() != null) getWorldObj().func_147480_a(xCoord, yCoord, zCoord,
                true)
    }

    override fun updateEntity() {
        if (getWorldObj() == null || getWorldObj().provider == null) return
        if (ticksToFinish > 0) ticksToFinish = ticksToFinish - 1
        if (ticksToFinish <= 0 && returnStack != null) {
            val storage = storageGrid ?: return
            val toInject = AEApi.instance().storage()
                    .createItemStack(returnStack)
            if (storage.itemInventory.canAccept(toInject.copy())) {
                val nodAdded = storage.itemInventory.injectItems(
                        toInject.copy(), Actionable.SIMULATE,
                        MachineSource(this))
                if (nodAdded == null) {
                    storage.itemInventory.injectItems(toInject,
                            Actionable.MODULATE, MachineSource(this))
                    returnStack = null
                }
            }
        }
    }

    override fun updateGrid(oldGrid: IGrid?, newGrid: IGrid?) {
        if (oldGrid != null) {
            val storage = oldGrid.getCache<IStorageGrid>(IStorageGrid::class.java)
            storage?.fluidInventory?.removeListener(this)
        }
        if (newGrid != null) {
            val storage = newGrid.getCache<IStorageGrid>(IStorageGrid::class.java)
            storage?.fluidInventory?.addListener(this, null)
        }
    }

    override fun writeToNBT(tagCompound: NBTTagCompound) {
        super.writeToNBT(tagCompound)
        if (containerItem != null) tagCompound.setTag("container",
                containerItem!!.writeToNBT(NBTTagCompound())) else tagCompound.setBoolean("isContainerEmpty", true)
        if (returnStack != null) tagCompound.setTag("return",
                returnStack!!.writeToNBT(NBTTagCompound())) else tagCompound.setBoolean("isReturnEmpty", true)
        tagCompound.setInteger("time", ticksToFinish)
        if (!hasWorldObj()) return
        val node = getGridNode(ForgeDirection.UNKNOWN)
        if (node != null) {
            val nodeTag = NBTTagCompound()
            node.saveToNBT("node0", nodeTag)
            tagCompound.setTag("nodes", nodeTag)
        }
    }

    init {
        gridBlock = ECFluidGridBlock(this)
    }
}