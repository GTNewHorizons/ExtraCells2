package extracells.tileentity

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.implementations.ICraftingPatternItem
import appeng.api.implementations.tiles.ITileStorageMonitorable
import appeng.api.networking.IGridNode
import appeng.api.networking.crafting.ICraftingPatternDetails
import appeng.api.networking.crafting.ICraftingProvider
import appeng.api.networking.crafting.ICraftingProviderHelper
import appeng.api.networking.events.MENetworkCraftingPatternChange
import appeng.api.networking.security.BaseActionSource
import appeng.api.networking.security.IActionHost
import appeng.api.networking.security.MachineSource
import appeng.api.networking.storage.IStorageGrid
import appeng.api.storage.IMEMonitor
import appeng.api.storage.IStorageMonitorable
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IAEItemStack
import appeng.api.storage.data.IAEStack
import appeng.api.util.AECableType
import appeng.api.util.DimensionalCoord
import cpw.mods.fml.common.FMLCommonHandler
import extracells.api.IECTileEntity
import extracells.api.IFluidInterface
import extracells.api.crafting.IFluidCraftingPatternDetails
import extracells.container.IContainerListener
import extracells.crafting.CraftingPattern
import extracells.crafting.CraftingPattern2
import extracells.gridblock.ECFluidGridBlock
import extracells.integration.waila.IWailaTile
import extracells.network.packet.other.IFluidSlotPartOrBlock
import extracells.registries.ItemEnum
import extracells.util.EmptyMeItemMonitor
import extracells.util.ItemUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.ISidedInventory
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.network.NetworkManager
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.util.StatCollector
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.*
import java.util.*
open class TileEntityFluidInterface : TileBase(), IActionHost, IFluidHandler, IECTileEntity, IFluidInterface, IFluidSlotPartOrBlock, ITileStorageMonitorable, IStorageMonitorable, ICraftingProvider, IWailaTile {
    inner class FluidInterfaceInventory : IInventory {
        val inv = arrayOfNulls<ItemStack>(9)
        override fun closeInventory() {}
        override fun decrStackSize(slot: Int, amt: Int): ItemStack {
            var stack = getStackInSlot(slot)
            if (stack != null) {
                if (stack.stackSize <= amt) {
                    setInventorySlotContents(slot, null)
                } else {
                    stack = stack.splitStack(amt)
                    if (stack.stackSize == 0) {
                        setInventorySlotContents(slot, null)
                    }
                }
            }
            update = true
            return stack
        }

        override fun getInventoryName(): String {
            return "inventory.fluidInterface"
        }

        override fun getInventoryStackLimit(): Int {
            return 1
        }

        override fun getSizeInventory(): Int {
            return inv.size
        }

        override fun getStackInSlot(slot: Int): ItemStack {
            return inv[slot]!!
        }

        override fun getStackInSlotOnClosing(slot: Int): ItemStack? {
            return null
        }

        override fun hasCustomInventoryName(): Boolean {
            return false
        }

        override fun isItemValidForSlot(slot: Int, stack: ItemStack): Boolean {
            if (stack.item is ICraftingPatternItem) {
                val details = (stack
                        .item as ICraftingPatternItem).getPatternForItem(stack, getWorldObj())
                return details != null
            }
            return false
        }

        override fun isUseableByPlayer(player: EntityPlayer): Boolean {
            return true
        }

        override fun markDirty() {}
        override fun openInventory() {}
        fun readFromNBT(tagCompound: NBTTagCompound) {
            val tagList = tagCompound.getTagList("Inventory", 10)
            for (i in 0 until tagList.tagCount()) {
                val tag = tagList.getCompoundTagAt(i)
                val slot = tag.getByte("Slot")
                if (slot >= 0 && slot < inv.size) {
                    inv[slot.toInt()] = ItemStack.loadItemStackFromNBT(tag)
                }
            }
        }

        override fun setInventorySlotContents(slot: Int, stack: ItemStack?) {
            inv[slot] = stack
            if (stack != null && stack.stackSize > inventoryStackLimit) {
                stack.stackSize = inventoryStackLimit
            }
            update = true
        }

        fun writeToNBT(tagCompound: NBTTagCompound) {
            val itemList = NBTTagList()
            for (i in inv.indices) {
                val stack = inv[i]
                if (stack != null) {
                    val tag = NBTTagCompound()
                    tag.setByte("Slot", i.toByte())
                    stack.writeToNBT(tag)
                    itemList.appendTag(tag)
                }
            }
            tagCompound.setTag("Inventory", itemList)
        }
    }

    var listeners: MutableList<IContainerListener> = ArrayList()
    private val gridBlock: ECFluidGridBlock
    private var node: IGridNode? = null
    var tanks = arrayOfNulls<FluidTank>(6)
    var fluidFilter = arrayOfNulls<Int>(tanks.size)
    var doNextUpdate = false
    private var wasIdle = false
    private var tickCount = 0
    private var update = false
    private var patternHandlers: MutableList<ICraftingPatternDetails> = ArrayList()
    private val patternConvert = HashMap<ICraftingPatternDetails, IFluidCraftingPatternDetails>()
    private val requestedItems: List<IAEItemStack?> = ArrayList()
    private val removeList: List<IAEItemStack?> = ArrayList()
    val inventory: FluidInterfaceInventory
    private var toExport: IAEItemStack? = null
    private val encodedPattern = AEApi.instance().definitions().items().encodedPattern()
            .maybeItem().orNull()
    private val export: MutableList<IAEStack<*>?> = ArrayList()
    private val addToExport: MutableList<IAEStack<*>?> = ArrayList()
    private val watcherList: List<IAEItemStack?> = ArrayList()
    private var isFirstGetGridNode = true
    override fun canDrain(from: ForgeDirection, fluid: Fluid): Boolean {
        if (from == ForgeDirection.UNKNOWN) return false
        val tankFluid = tanks[from.ordinal]!!.fluid
        return tankFluid != null && tankFluid.getFluid() === fluid
    }

    override fun canFill(from: ForgeDirection, fluid: Fluid): Boolean {
        return (from != ForgeDirection.UNKNOWN
                && tanks[from.ordinal]!!.fill(FluidStack(fluid, 1), false) > 0)
    }

    override fun drain(from: ForgeDirection, resource: FluidStack?,
                       doDrain: Boolean): FluidStack? {
        val tankFluid = tanks[from.ordinal]!!.fluid
        return if (resource == null || tankFluid == null || tankFluid.getFluid() !== resource.getFluid()) null else drain(
                from, resource.amount, doDrain)
    }

    override fun drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack? {
        if (from == ForgeDirection.UNKNOWN) return null
        val drained = tanks[from.ordinal]
                ?.drain(maxDrain, doDrain)
        if (drained != null) if (getWorldObj() != null) getWorldObj().markBlockForUpdate(xCoord, yCoord,
                zCoord)
        doNextUpdate = true
        return drained
    }

    override fun fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int {
        if (from == ForgeDirection.UNKNOWN || resource == null) return 0
        if ((tanks[from.ordinal]!!.fluid == null || tanks[from
                        .ordinal]!!.fluid.getFluid() === resource.getFluid())
                && resource.getFluid() === FluidRegistry
                        .getFluid(fluidFilter[from.ordinal]!!)) {
            var added = tanks[from.ordinal]!!.fill(resource.copy(), doFill)
            if (added == resource.amount) {
                doNextUpdate = true
                return added
            }
            added += fillToNetwork(FluidStack(resource.getFluid(),
                    resource.amount - added), doFill)
            doNextUpdate = true
            return added
        }
        var filled = 0
        filled += fillToNetwork(resource, doFill)
        if (filled < resource.amount) filled += tanks[from.ordinal]!!.fill(FluidStack(
                resource.getFluid(), resource.amount - filled), doFill)
        if (filled > 0) if (getWorldObj() != null) getWorldObj().markBlockForUpdate(xCoord, yCoord,
                zCoord)
        doNextUpdate = true
        return filled
    }

    fun fillToNetwork(resource: FluidStack?, doFill: Boolean): Int {
        val node = getGridNode(ForgeDirection.UNKNOWN)
        if (node == null || resource == null) return 0
        val grid = node.grid ?: return 0
        val storage = grid.getCache<IStorageGrid>(IStorageGrid::class.java) ?: return 0
        val notRemoved: IAEFluidStack?
        val copy = resource.copy()
        notRemoved = if (doFill) {
            storage.fluidInventory.injectItems(
                    AEApi.instance().storage().createFluidStack(resource),
                    Actionable.MODULATE, MachineSource(this))
        } else {
            storage.fluidInventory.injectItems(
                    AEApi.instance().storage().createFluidStack(resource),
                    Actionable.SIMULATE, MachineSource(this))
        }
        return if (notRemoved == null) resource.amount else (resource.amount - notRemoved.stackSize).toInt()
    }

    private fun forceUpdate() {
        getWorldObj().markBlockForUpdate(yCoord, yCoord, zCoord)
        for (listener in listeners) {
            listener.updateContainer()
        }
        doNextUpdate = false
    }

    override fun getActionableNode(): IGridNode? {
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
        writeToNBTWithoutExport(nbtTag)
        return S35PacketUpdateTileEntity(xCoord, yCoord,
                zCoord, 1, nbtTag)
    }

    override fun getFilter(side: ForgeDirection?): Fluid? {
        return if (side == null || side == ForgeDirection.UNKNOWN) null else FluidRegistry.getFluid(
                fluidFilter[side.ordinal]!!)
    }

    override fun getFluidInventory(): IMEMonitor<IAEFluidStack> {
        return getFluidInventory(ForgeDirection.UNKNOWN)!!
    }

    override fun getFluidTank(side: ForgeDirection?): IFluidTank? {
        return if (side == null || side == ForgeDirection.UNKNOWN) null else tanks[side.ordinal]
    }

    override fun getGridNode(dir: ForgeDirection): IGridNode? {
        if (FMLCommonHandler.instance().side.isClient
                && (getWorldObj() == null || getWorldObj().isRemote)) return null
        if (isFirstGetGridNode) {
            isFirstGetGridNode = false
            actionableNode?.updateState()
        }
        return node!!
    }

    override fun getItemInventory(): IMEMonitor<IAEItemStack> {
        return EmptyMeItemMonitor()
    }

    override val location: DimensionalCoord
        get() = DimensionalCoord(this)

    override fun getMonitorable(side: ForgeDirection,
                                src: BaseActionSource): IStorageMonitorable {
        return this
    }

    override val patternInventory: IInventory
        get() = inventory
    override val powerUsage: Double
        get() = 1.0

    override fun getTankInfo(from: ForgeDirection): Array<FluidTankInfo?>? {
        return if (from == ForgeDirection.UNKNOWN) null else arrayOf(tanks[from.ordinal]!!.info)
    }

    override fun getWailaBody(list: MutableList<String>, tag: NBTTagCompound,
                              side: ForgeDirection?): List<String> {
        if (side == null || side == ForgeDirection.UNKNOWN) return list
        list.add(StatCollector.translateToLocal("extracells.tooltip.direction."
                + side.ordinal))
        val tanks = arrayOfNulls<FluidTank>(6)
        for (i in tanks.indices) {
            tanks[i] = object : FluidTank(10000) {
                override fun readFromNBT(nbt: NBTTagCompound): FluidTank {
                    if (!nbt.hasKey("Empty")) {
                        val fluid = FluidStack
                                .loadFluidStackFromNBT(nbt)
                        setFluid(fluid)
                    } else {
                        setFluid(null)
                    }
                    return this
                }
            }
        }
        for (i in tanks.indices) {
            if (tag.hasKey("tank#$i")) tanks[i]!!.readFromNBT(tag.getCompoundTag("tank#$i"))
        }
        val tank = tanks[side.ordinal]
        if (tank == null || tank.fluid == null || tank.fluid.getFluid() == null) {
            list.add(StatCollector.translateToLocal("extracells.tooltip.fluid")
                    + ": "
                    + StatCollector
                    .translateToLocal("extracells.tooltip.empty1"))
            list.add(StatCollector
                    .translateToLocal("extracells.tooltip.amount")
                    + ": 0mB / 10000mB")
        } else {
            list.add(StatCollector.translateToLocal("extracells.tooltip.fluid")
                    + ": " + tank.fluid.localizedName)
            list.add(StatCollector
                    .translateToLocal("extracells.tooltip.amount")
                    + ": "
                    + tank.fluidAmount + "mB / 10000mB")
        }
        return list
    }

    override fun getWailaTag(tag: NBTTagCompound): NBTTagCompound {
        for (i in tanks.indices) {
            tag.setTag("tank#$i",
                    tanks[i]!!.writeToNBT(NBTTagCompound()))
        }
        return tag
    }

    override fun isBusy(): Boolean {
        return !export.isEmpty()
    }

    private fun makeCraftingPatternItem(details: ICraftingPatternDetails?): ItemStack? {
        if (details == null) return null
        val `in` = NBTTagList()
        val out = NBTTagList()
        for (s in details.inputs) {
            if (s == null) `in`.appendTag(NBTTagCompound()) else `in`.appendTag(
                    s.itemStack.writeToNBT(NBTTagCompound()))
        }
        for (s in details.outputs) {
            if (s == null) out.appendTag(NBTTagCompound()) else out.appendTag(s.itemStack.writeToNBT(NBTTagCompound()))
        }
        val itemTag = NBTTagCompound()
        itemTag.setTag("in", `in`)
        itemTag.setTag("out", out)
        itemTag.setBoolean("crafting", details.isCraftable)
        val pattern = ItemStack(encodedPattern)
        pattern.tagCompound = itemTag
        return pattern
    }

    override fun onDataPacket(net: NetworkManager, pkt: S35PacketUpdateTileEntity) {
        readFromNBT(pkt.func_148857_g())
    }

    override fun provideCrafting(craftingTracker: ICraftingProviderHelper) {
        patternHandlers = ArrayList()
        patternConvert.clear()
        for (currentPatternStack in inventory.inv) {
            if (currentPatternStack != null && currentPatternStack.item != null && currentPatternStack.item is ICraftingPatternItem) {
                val currentPattern = currentPatternStack
                        .item as ICraftingPatternItem
                if (currentPattern != null
                        && currentPattern.getPatternForItem(
                                currentPatternStack, getWorldObj()) != null) {
                    val pattern: IFluidCraftingPatternDetails = CraftingPattern2(
                            currentPattern.getPatternForItem(
                                    currentPatternStack, getWorldObj()))
                    patternHandlers.add(pattern)
                    val `is` = makeCraftingPatternItem(pattern) ?: continue
                    val p = (`is`
                            .item as ICraftingPatternItem).getPatternForItem(`is`, getWorldObj())
                    patternConvert[p] = pattern
                    craftingTracker.addCraftingOption(this, p)
                }
            }
        }
    }

    private fun pushItems() {
        export.addAll(addToExport)
        addToExport.clear()
        if (!hasWorldObj() || export.isEmpty()) return
        val directions = ForgeDirection.VALID_DIRECTIONS
        for (dir in directions) {
            val tile = getWorldObj().getTileEntity(
                    xCoord + dir.offsetX, yCoord + dir.offsetY,
                    zCoord + dir.offsetZ)
            if (tile != null) {
                val stack0 = export[0]
                val stack = stack0?.copy()
                if (stack is IAEItemStack && tile is IInventory) {
                    if (tile is ISidedInventory) {
                        val inv = tile as ISidedInventory
                        for (i in inv.getAccessibleSlotsFromSide(dir
                                .opposite.ordinal)) {
                            if (inv.canInsertItem(i, stack
                                            .itemStack, dir.opposite
                                            .ordinal)) {
                                if (inv.getStackInSlot(i) == null) {
                                    inv.setInventorySlotContents(i,
                                            stack
                                                    .itemStack)
                                    export.removeAt(0)
                                    return
                                } else if (ItemUtils.areItemEqualsIgnoreStackSize(
                                                inv.getStackInSlot(i),
                                                stack.itemStack)) {
                                    val max = inv.inventoryStackLimit
                                    val current = inv.getStackInSlot(i).stackSize
                                    val outStack = stack.getStackSize().toInt()
                                    if (max == current) continue
                                    if (current + outStack <= max) {
                                        val s = inv.getStackInSlot(i)
                                                .copy()
                                        s.stackSize = s.stackSize + outStack
                                        inv.setInventorySlotContents(i, s)
                                        export.removeAt(0)
                                        return
                                    } else {
                                        val s = inv.getStackInSlot(i)
                                                .copy()
                                        s.stackSize = max
                                        inv.setInventorySlotContents(i, s)
                                        export[0]?.stackSize = outStack - max + current.toLong()
                                        return
                                    }
                                }
                            }
                        }
                    } else {
                        val inv = tile as IInventory
                        for (i in 0 until inv.sizeInventory) {
                            if (inv.isItemValidForSlot(i,
                                            stack.itemStack)) {
                                if (inv.getStackInSlot(i) == null) {
                                    inv.setInventorySlotContents(i,
                                            stack
                                                    .itemStack)
                                    export.removeAt(0)
                                    return
                                } else if (ItemUtils.areItemEqualsIgnoreStackSize(
                                                inv.getStackInSlot(i),
                                                stack.itemStack)) {
                                    val max = inv.inventoryStackLimit
                                    val current = inv.getStackInSlot(i).stackSize
                                    val outStack = stack.getStackSize().toInt()
                                    if (max == current) continue
                                    if (current + outStack <= max) {
                                        val s = inv.getStackInSlot(i)
                                                .copy()
                                        s.stackSize = s.stackSize + outStack
                                        inv.setInventorySlotContents(i, s)
                                        export.removeAt(0)
                                        return
                                    } else {
                                        val s = inv.getStackInSlot(i)
                                                .copy()
                                        s.stackSize = max
                                        inv.setInventorySlotContents(i, s)
                                        export[0]?.stackSize = outStack - max + current.toLong()
                                        return
                                    }
                                }
                            }
                        }
                    }
                } else if (stack is IAEFluidStack
                        && tile is IFluidHandler) {
                    val handler = tile as IFluidHandler
                    val fluid = stack
                    if (handler.canFill(dir.opposite, fluid.copy()
                                    .fluid)) {
                        val amount = handler.fill(dir.opposite, fluid
                                .fluidStack.copy(), false)
                        if (amount == 0) continue
                        if (amount.toLong() == fluid.stackSize) {
                            handler.fill(dir.opposite, fluid
                                    .fluidStack.copy(), true)
                            export.removeAt(0)
                        } else {
                            val fl = fluid.fluidStack.copy()
                            fl.amount = amount
                            export[0]?.stackSize = fluid.stackSize - handler.fill(dir.opposite, fl, true)
                            return
                        }
                    }
                }
            }
        }
    }

    override fun pushPattern(patDetails: ICraftingPatternDetails,
                             table: InventoryCrafting): Boolean {
        if (isBusy || !patternConvert.containsKey(patDetails)) return false
        val patternDetails: ICraftingPatternDetails? = patternConvert[patDetails]
        if (patternDetails is CraftingPattern) {
            val patter = patternDetails
            val fluids = HashMap<Fluid, Long>()
            for (stack in patter.condensedFluidInputs!!) {
                if (fluids.containsKey(stack!!.fluid)) {
                    val amount = (fluids[stack.fluid]!!
                            + stack.stackSize)
                    fluids.remove(stack.fluid)
                    fluids[stack.fluid] = amount
                } else {
                    fluids[stack.fluid] = stack.stackSize
                }
            }
            val grid = node!!.grid ?: return false
            val storage = grid.getCache<IStorageGrid>(IStorageGrid::class.java) ?: return false
            for (fluid in fluids.keys) {
                val amount = fluids[fluid]
                val extractFluid = storage.fluidInventory
                        .extractItems(
                                AEApi.instance()
                                        .storage()
                                        .createFluidStack(
                                                FluidStack(fluid,
                                                        (amount!! + 0).toInt())),
                                Actionable.SIMULATE, MachineSource(this))
                if (extractFluid == null
                        || extractFluid.stackSize != amount) {
                    return false
                }
            }
            for (fluid in fluids.keys) {
                val amount = fluids[fluid]
                val extractFluid = storage.fluidInventory
                        .extractItems(
                                AEApi.instance()
                                        .storage()
                                        .createFluidStack(
                                                FluidStack(fluid,
                                                        (amount!! + 0).toInt())),
                                Actionable.MODULATE, MachineSource(this))
                export.add(extractFluid)
            }
            for (s in patter.condensedInputs!!) {
                if (s == null) continue
                if (s.item === ItemEnum.FLUIDPATTERN.item) {
                    toExport = s.copy()
                    continue
                }
                export.add(s)
            }
        }
        return true
    }

    fun readFilter(tag: NBTTagCompound) {
        for (i in fluidFilter.indices) {
            if (tag.hasKey("fluid#$i")) fluidFilter[i] = tag.getInteger("fluid#$i")
        }
    }

    override fun readFromNBT(tag: NBTTagCompound) {
        super.readFromNBT(tag)
        for (i in tanks.indices) {
            if (tag.hasKey("tank#$i")) tanks[i]!!.readFromNBT(tag.getCompoundTag("tank#$i"))
            if (tag.hasKey("filter#$i")) fluidFilter[i] = tag.getInteger("filter#$i")
        }
        if (hasWorldObj()) {
            val node = getGridNode(ForgeDirection.UNKNOWN)
            if (tag.hasKey("nodes") && node != null) {
                node.loadFromNBT("node0", tag.getCompoundTag("nodes"))
                node.updateState()
            }
        }
        if (tag.hasKey("inventory")) inventory.readFromNBT(tag.getCompoundTag("inventory"))
        if (tag.hasKey("export")) readOutputFromNBT(tag.getCompoundTag("export"))
    }

    private fun readOutputFromNBT(tag: NBTTagCompound) {
        addToExport.clear()
        export.clear()
        var i = tag.getInteger("add")
        for (j in 0 until i) {
            if (tag.getBoolean("add-$j-isItem")) {
                val s = AEApi
                        .instance()
                        .storage()
                        .createItemStack(
                                ItemStack.loadItemStackFromNBT(tag
                                        .getCompoundTag("add-$j")))
                s.stackSize = tag.getLong("add-$j-amount")
                addToExport.add(s)
            } else {
                val s = AEApi
                        .instance()
                        .storage()
                        .createFluidStack(
                                FluidStack.loadFluidStackFromNBT(tag
                                        .getCompoundTag("add-$j")))
                s.stackSize = tag.getLong("add-$j-amount")
                addToExport.add(s)
            }
        }
        i = tag.getInteger("export")
        for (j in 0 until i) {
            if (tag.getBoolean("export-$j-isItem")) {
                val s = AEApi
                        .instance()
                        .storage()
                        .createItemStack(
                                ItemStack.loadItemStackFromNBT(tag
                                        .getCompoundTag("export-$j")))
                s.stackSize = tag.getLong("export-$j-amount")
                export.add(s)
            } else {
                val s = AEApi
                        .instance()
                        .storage()
                        .createFluidStack(
                                FluidStack.loadFluidStackFromNBT(tag
                                        .getCompoundTag("export-$j")))
                s.stackSize = tag.getLong("export-$j-amount")
                export.add(s)
            }
        }
    }

    fun registerListener(listener: IContainerListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: IContainerListener) {
        listeners.remove(listener)
    }

    override fun securityBreak() {}
    override fun setFilter(side: ForgeDirection?, fluid: Fluid?) {
        if (side == null || side == ForgeDirection.UNKNOWN) return
        if (fluid == null) {
            fluidFilter[side.ordinal] = -1
            doNextUpdate = true
            return
        }
        fluidFilter[side.ordinal] = fluid.id
        doNextUpdate = true
    }

    override fun setFluid(_index: Int, _fluid: Fluid?, _player: EntityPlayer?) {
        setFilter(ForgeDirection.getOrientation(_index), _fluid)
    }

    override fun setFluidTank(side: ForgeDirection?, fluid: FluidStack?) {
        if (side == null || side == ForgeDirection.UNKNOWN) return
        tanks[side.ordinal]!!.fluid = fluid
        doNextUpdate = true
    }

    private fun tick() {
        if (tickCount >= 40 || !wasIdle) {
            tickCount = 0
            wasIdle = true
        } else {
            tickCount++
            return
        }
        if (node == null) return
        val grid = node!!.grid ?: return
        val storage = grid.getCache<IStorageGrid>(IStorageGrid::class.java) ?: return
        if (toExport != null) {
            storage.itemInventory.injectItems(toExport,
                    Actionable.MODULATE, MachineSource(this))
            toExport = null
        }
        for (i in tanks.indices) {
            if (tanks[i]!!.fluid != null && FluidRegistry.getFluid(fluidFilter[i]!!) !== tanks[i]?.fluid?.getFluid()) {
                val s = tanks[i]!!.drain(125, false)
                if (s != null) {
                    val notAdded = storage.fluidInventory
                            .injectItems(
                                    AEApi.instance().storage()
                                            .createFluidStack(s.copy()),
                                    Actionable.SIMULATE,
                                    MachineSource(this))
                    if (notAdded != null) {
                        val toAdd = (s.amount - notAdded.stackSize).toInt()
                        storage.fluidInventory.injectItems(
                                AEApi.instance()
                                        .storage()
                                        .createFluidStack(
                                                tanks[i]!!.drain(toAdd, true)),
                                Actionable.MODULATE, MachineSource(this))
                        doNextUpdate = true
                        wasIdle = false
                    } else {
                        storage.fluidInventory.injectItems(
                                AEApi.instance()
                                        .storage()
                                        .createFluidStack(
                                                tanks[i]!!.drain(s.amount,
                                                        true)),
                                Actionable.MODULATE, MachineSource(this))
                        doNextUpdate = true
                        wasIdle = false
                    }
                }
            }
            if ((tanks[i]!!.fluid == null || tanks[i]!!.fluid
                            .getFluid() === FluidRegistry.getFluid(fluidFilter[i]!!))
                    && FluidRegistry.getFluid(fluidFilter[i]!!) != null) {
                val extracted = storage
                        .fluidInventory
                        .extractItems(
                                AEApi.instance()
                                        .storage()
                                        .createFluidStack(
                                                FluidStack(
                                                        FluidRegistry
                                                                .getFluid(fluidFilter[i]!!),
                                                        125)),
                                Actionable.SIMULATE, MachineSource(this))
                        ?: continue
                val accepted = tanks[i]!!.fill(extracted.fluidStack,
                        false)
                if (accepted == 0) continue
                tanks[i]!!.fill(storage.fluidInventory.extractItems(
                                        AEApi.instance()
                                                .storage()
                                                .createFluidStack(
                                                        FluidStack(
                                                                FluidRegistry
                                                                        .getFluid(fluidFilter[i]!!),
                                                                accepted)),
                                        Actionable.MODULATE,
                                        MachineSource(this)).fluidStack, true)
                doNextUpdate = true
                wasIdle = false
            }
        }
    }

    override fun updateEntity() {
        if (getWorldObj() == null || getWorldObj().provider == null || getWorldObj().isRemote) return
        if (update) {
            update = false
            getGridNode(ForgeDirection.UNKNOWN)?.grid?.postEvent(
                        MENetworkCraftingPatternChange(this,
                                getGridNode(ForgeDirection.UNKNOWN)))
        }
        pushItems()
        if (doNextUpdate)
            forceUpdate()
        tick()
    }

    fun writeFilter(tag: NBTTagCompound): NBTTagCompound {
        for (i in fluidFilter.indices) {
            tag.setInteger("fluid#$i", fluidFilter[i]!!)
        }
        return tag
    }

    private fun writeOutputToNBT(tag: NBTTagCompound): NBTTagCompound {
        var i = 0
        i = 0
        for (s in addToExport) {
            if (s != null) {
                tag.setBoolean("add-$i-isItem", s.isItem)
                val data = NBTTagCompound()
                if (s.isItem) {
                    (s as IAEItemStack).itemStack.writeToNBT(data)
                } else {
                    (s as IAEFluidStack).fluidStack.writeToNBT(data)
                }
                tag.setTag("add-$i", data)
                tag.setLong("add-$i-amount", s.stackSize)
            }
            i++
        }
        tag.setInteger("add", addToExport.size)
        i = 0
        for (s in export) {
            if (s != null) {
                tag.setBoolean("export-$i-isItem", s.isItem)
                val data = NBTTagCompound()
                if (s.isItem) {
                    (s as IAEItemStack).itemStack.writeToNBT(data)
                } else {
                    (s as IAEFluidStack).fluidStack.writeToNBT(data)
                }
                tag.setTag("export-$i", data)
                tag.setLong("export-$i-amount", s.stackSize)
            }
            i++
        }
        tag.setInteger("export", export.size)
        return tag
    }

    override fun writeToNBT(data: NBTTagCompound) {
        writeToNBTWithoutExport(data)
        val tag = NBTTagCompound()
        writeOutputToNBT(tag)
        data.setTag("export", tag)
    }

    fun writeToNBTWithoutExport(tag: NBTTagCompound) {
        super.writeToNBT(tag)
        for (i in tanks.indices) {
            tag.setTag("tank#$i",
                    tanks[i]!!.writeToNBT(NBTTagCompound()))
            tag.setInteger("filter#$i", fluidFilter[i]!!)
        }
        if (!hasWorldObj()) return
        val node = getGridNode(ForgeDirection.UNKNOWN)
        if (node != null) {
            val nodeTag = NBTTagCompound()
            node.saveToNBT("node0", nodeTag)
            tag.setTag("nodes", nodeTag)
        }
        val inventory = NBTTagCompound()
        this.inventory.writeToNBT(inventory)
        tag.setTag("inventory", inventory)
    }

    init {
        inventory = FluidInterfaceInventory()
        gridBlock = ECFluidGridBlock(this)
        for (i in tanks.indices) {
            tanks[i] = object : FluidTank(10000) {
                override fun readFromNBT(nbt: NBTTagCompound): FluidTank {
                    if (!nbt.hasKey("Empty")) {
                        val fluid = FluidStack
                                .loadFluidStackFromNBT(nbt)
                        setFluid(fluid)
                    } else {
                        setFluid(null)
                    }
                    return this
                }
            }
            fluidFilter[i] = -1
        }
    }
}