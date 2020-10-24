package extracells.part

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.config.SecurityPermissions
import appeng.api.implementations.ICraftingPatternItem
import appeng.api.implementations.tiles.ITileStorageMonitorable
import appeng.api.networking.IGridNode
import appeng.api.networking.crafting.ICraftingPatternDetails
import appeng.api.networking.crafting.ICraftingProvider
import appeng.api.networking.crafting.ICraftingProviderHelper
import appeng.api.networking.events.MENetworkCraftingPatternChange
import appeng.api.networking.security.BaseActionSource
import appeng.api.networking.security.MachineSource
import appeng.api.networking.storage.IStorageGrid
import appeng.api.networking.ticking.IGridTickable
import appeng.api.networking.ticking.TickRateModulation
import appeng.api.networking.ticking.TickingRequest
import appeng.api.parts.IPart
import appeng.api.parts.IPartCollisionHelper
import appeng.api.parts.IPartRenderHelper
import appeng.api.storage.IMEMonitor
import appeng.api.storage.IStorageMonitorable
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IAEItemStack
import appeng.api.storage.data.IAEStack
import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.api.IFluidInterface
import extracells.api.crafting.IFluidCraftingPatternDetails
import extracells.container.ContainerFluidInterface
import extracells.container.IContainerListener
import extracells.crafting.CraftingPattern
import extracells.crafting.CraftingPattern2
import extracells.gui.GuiFluidInterface
import extracells.network.packet.other.IFluidSlotPartOrBlock
import extracells.part.PartFluidInterface
import extracells.registries.ItemEnum
import extracells.render.TextureManager
import extracells.util.EmptyMeItemMonitor
import extracells.util.ItemUtils
import extracells.util.PermissionUtil
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.ISidedInventory
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.StatCollector
import net.minecraft.util.Vec3
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.*
import java.io.IOException
import java.util.*

class PartFluidInterface : PartECBase(), IFluidHandler, IFluidInterface, IFluidSlotPartOrBlock, ITileStorageMonitorable, IStorageMonitorable, IGridTickable, ICraftingProvider {
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

        override fun getStackInSlotOnClosing(slot: Int): ItemStack {
            return null
        }

        override fun hasCustomInventoryName(): Boolean {
            return false
        }

        override fun isItemValidForSlot(slot: Int, stack: ItemStack): Boolean {
            if (stack.item is ICraftingPatternItem) {
                val n = gridNode
                val w: World?
                w = if (n == null) {
                    clientWorld
                } else {
                    n.world
                }
                if (w == null) return false
                val details = (stack
                        .item as ICraftingPatternItem).getPatternForItem(stack, w)
                return details != null
            }
            return false
        }

        override fun isUseableByPlayer(player: EntityPlayer): Boolean {
            return this@PartFluidInterface.isValid
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

        override fun setInventorySlotContents(slot: Int, stack: ItemStack) {
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
    private var patternHandlers: MutableList<ICraftingPatternDetails> = ArrayList()
    private val patternConvert = HashMap<ICraftingPatternDetails, IFluidCraftingPatternDetails>()
    private val requestedItems: List<IAEItemStack> = ArrayList()
    private val removeList: List<IAEItemStack> = ArrayList()
    val inventory: FluidInterfaceInventory = FluidInterfaceInventory()
    private var update = false
    private val export: MutableList<IAEStack<*>> = ArrayList()
    private val removeFromExport: MutableList<IAEStack<*>> = ArrayList()
    private val addToExport: MutableList<IAEStack<*>> = ArrayList()
    private var toExport: IAEItemStack? = null
    private val encodedPattern = AEApi.instance().definitions().items().encodedPattern().maybeItem().orNull()
    private val tank: FluidTank = object : FluidTank(10000) {
        override fun readFromNBT(nbt: NBTTagCompound): FluidTank {
            if (!nbt.hasKey("Empty")) {
                val fluid = FluidStack.loadFluidStackFromNBT(nbt)
                setFluid(fluid)
            } else {
                setFluid(null)
            }
            return this
        }
    }
    private var fluidFilter = -1
    var doNextUpdate = false
    private var needBreake = false
    private val tickCount = 0
    override fun cableConnectionRenderTo(): Int {
        return 3
    }

    override fun canDrain(from: ForgeDirection, fluid: Fluid): Boolean {
        val tankFluid = tank.fluid
        return tankFluid != null && tankFluid.getFluid() === fluid
    }

    override fun canFill(from: ForgeDirection, fluid: Fluid): Boolean {
        return tank.fill(FluidStack(fluid, 1), false) > 0
    }

    override fun drain(from: ForgeDirection, resource: FluidStack,
                       doDrain: Boolean): FluidStack {
        val tankFluid = tank.fluid
        return if (resource == null || tankFluid == null || tankFluid.getFluid() !== resource.getFluid()) null else drain(
                from, resource.amount, doDrain)
    }

    override fun drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack {
        val drained = tank.drain(maxDrain, doDrain)
        if (drained != null) host.markForUpdate()
        doNextUpdate = true
        return drained
    }

    override fun fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int {
        if (resource == null) return 0
        if ((tank.fluid == null || tank.fluid.getFluid() === resource
                        .getFluid())
                && resource.getFluid() === FluidRegistry
                        .getFluid(fluidFilter)) {
            var added = tank.fill(resource.copy(), doFill)
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
        if (filled < resource.amount) filled += tank.fill(FluidStack(resource.getFluid(),
                resource.amount - filled), doFill)
        if (filled > 0) host.markForUpdate()
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
        host.markForUpdate()
        for (listener in listeners) {
            listener?.updateContainer()
        }
        doNextUpdate = false
    }

    override fun getBoxes(bch: IPartCollisionHelper) {
        bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0)
        bch.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 14.0)
    }

    override fun getClientGuiElement(player: EntityPlayer): Any? {
        return GuiFluidInterface(player, this, side)
    }

    @get:SideOnly(Side.CLIENT)
    private val clientWorld: World
        private get() = Minecraft.getMinecraft().theWorld

    override fun getDrops(drops: MutableList<ItemStack>, wrenched: Boolean) {
        for (i in 0 until inventory.sizeInventory) {
            val pattern = inventory.getStackInSlot(i)
            if (pattern != null) drops.add(pattern)
        }
    }

    override fun getFilter(side: ForgeDirection?): Fluid? {
        return FluidRegistry.getFluid(fluidFilter)
    }

    override fun getFluidInventory(): IMEMonitor<IAEFluidStack> {
        if (getGridNode(ForgeDirection.UNKNOWN) == null) return null
        val grid = getGridNode(ForgeDirection.UNKNOWN).grid ?: return null
        val storage = grid.getCache<IStorageGrid>(IStorageGrid::class.java) ?: return null
        return storage.fluidInventory
    }

    override fun getFluidTank(side: ForgeDirection?): IFluidTank? {
        return tank
    }

    override fun getItemInventory(): IMEMonitor<IAEItemStack> {
        return EmptyMeItemMonitor()
    }

    override fun getMonitorable(side: ForgeDirection,
                                src: BaseActionSource): IStorageMonitorable {
        return this
    }

    override val patternInventory: IInventory
        get() = inventory
    override val powerUsage: Double
        get() = 1.0

    override fun getServerGuiElement(player: EntityPlayer): Any? {
        return ContainerFluidInterface(player, this)
    }

    override fun getTankInfo(from: ForgeDirection): Array<FluidTankInfo> {
        return arrayOf(tank.info)
    }

    override fun getTickingRequest(node: IGridNode): TickingRequest {
        return TickingRequest(1, 40, false, false)
    }

    override fun getWailaBodey(tag: NBTTagCompound, list: MutableList<String>): List<String> {
        var fluid: FluidStack? = null
        var id = -1
        var amount = 0
        if (tag.hasKey("fluidID") && tag.hasKey("amount")) {
            id = tag.getInteger("fluidID")
            amount = tag.getInteger("amount")
        }
        if (id != -1) fluid = FluidStack(id, amount)
        if (fluid == null) {
            list.add(StatCollector.translateToLocal("extracells.tooltip.fluid")
                    + ": "
                    + StatCollector
                    .translateToLocal("extracells.tooltip.empty1"))
            list.add(StatCollector
                    .translateToLocal("extracells.tooltip.amount")
                    + ": 0mB / 10000mB")
        } else {
            list.add(StatCollector.translateToLocal("extracells.tooltip.fluid")
                    + ": " + fluid.localizedName)
            list.add(StatCollector
                    .translateToLocal("extracells.tooltip.amount")
                    + ": "
                    + fluid.amount + "mB / 10000mB")
        }
        return list
    }

    override fun getWailaTag(tag: NBTTagCompound): NBTTagCompound {
        if (tank.fluid == null
                || tank.fluid.getFluid() == null) tag.setInteger("fluidID", -1) else tag.setInteger("fluidID",
                tank.fluid.fluidID)
        tag.setInteger("amount", tank.fluidAmount)
        return tag
    }

    override fun initializePart(partStack: ItemStack) {
        if (partStack.hasTagCompound()) {
            readFilter(partStack.tagCompound)
        }
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

    override fun onActivate(player: EntityPlayer, pos: Vec3): Boolean {
        return if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
                        this as IPart)) {
            super.onActivate(player, pos)
        } else false
    }

    override fun provideCrafting(craftingTracker: ICraftingProviderHelper) {
        patternHandlers = ArrayList()
        patternConvert.clear()
        if (!isActive) {
            return
        }
        for (currentPatternStack in inventory.inv) {
            if (currentPatternStack != null && currentPatternStack.item != null && currentPatternStack.item is ICraftingPatternItem) {
                val currentPattern = currentPatternStack
                        .item as ICraftingPatternItem
                if (currentPattern != null
                        && currentPattern.getPatternForItem(
                                currentPatternStack, gridNode.world) != null) {
                    val pattern: IFluidCraftingPatternDetails = CraftingPattern2(
                            currentPattern.getPatternForItem(
                                    currentPatternStack, gridNode
                                    .world))
                    patternHandlers.add(pattern)
                    val `is` = makeCraftingPatternItem(pattern) ?: continue
                    val p = (`is`
                            .item as ICraftingPatternItem).getPatternForItem(`is`, gridNode
                            .world)
                            ?: continue
                    patternConvert[p] = pattern
                    craftingTracker.addCraftingOption(this, p)
                }
            }
        }
    }

    private fun pushItems() {
        for (s in removeFromExport) {
            export.remove(s)
        }
        removeFromExport.clear()
        for (s in addToExport) {
            export.add(s)
        }
        addToExport.clear()
        if (gridNode.world == null || export.isEmpty()) return
        val dir = side
        val tile = gridNode.world.getTileEntity(
                gridNode.gridBlock.location.x + dir!!.offsetX,
                gridNode.gridBlock.location.y + dir.offsetY,
                gridNode.gridBlock.location.z + dir.offsetZ)
        if (tile != null) {
            val stack0 = export.iterator().next()
            val stack = stack0.copy()
            if (stack is IAEItemStack && tile is IInventory) {
                if (tile is ISidedInventory) {
                    val inv = tile as ISidedInventory
                    for (i in inv.getAccessibleSlotsFromSide(dir
                            .opposite.ordinal)) {
                        if (inv.canInsertItem(i, stack
                                        .itemStack, dir.opposite.ordinal)) {
                            if (inv.getStackInSlot(i) == null) {
                                inv.setInventorySlotContents(i,
                                        stack.itemStack)
                                removeFromExport.add(stack0)
                                return
                            } else if (ItemUtils.areItemEqualsIgnoreStackSize(
                                            inv.getStackInSlot(i),
                                            stack.itemStack)) {
                                val max = inv.inventoryStackLimit
                                val current = inv.getStackInSlot(i).stackSize
                                val outStack = stack.getStackSize().toInt()
                                if (max == current) continue
                                if (current + outStack <= max) {
                                    val s = inv.getStackInSlot(i).copy()
                                    s.stackSize = s.stackSize + outStack
                                    inv.setInventorySlotContents(i, s)
                                    removeFromExport.add(stack0)
                                    return
                                } else {
                                    val s = inv.getStackInSlot(i).copy()
                                    s.stackSize = max
                                    inv.setInventorySlotContents(i, s)
                                    removeFromExport.add(stack0)
                                    stack.stackSize = outStack - max + current.toLong()
                                    addToExport.add(stack)
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
                                        stack.itemStack)
                                removeFromExport.add(stack0)
                                return
                            } else if (ItemUtils.areItemEqualsIgnoreStackSize(
                                            inv.getStackInSlot(i),
                                            stack.itemStack)) {
                                val max = inv.inventoryStackLimit
                                val current = inv.getStackInSlot(i).stackSize
                                val outStack = stack.getStackSize().toInt()
                                if (max == current) continue
                                if (current + outStack <= max) {
                                    val s = inv.getStackInSlot(i).copy()
                                    s.stackSize = s.stackSize + outStack
                                    inv.setInventorySlotContents(i, s)
                                    removeFromExport.add(stack0)
                                    return
                                } else {
                                    val s = inv.getStackInSlot(i).copy()
                                    s.stackSize = max
                                    inv.setInventorySlotContents(i, s)
                                    removeFromExport.add(stack0)
                                    stack.stackSize = outStack - max + current.toLong()
                                    addToExport.add(stack)
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
                if (handler.canFill(dir.opposite, fluid.copy().fluid)) {
                    val amount = handler.fill(dir.opposite, fluid
                            .fluidStack.copy(), false)
                    if (amount == 0) return
                    if (amount.toLong() == fluid.stackSize) {
                        handler.fill(dir.opposite, fluid.fluidStack
                                .copy(), true)
                        removeFromExport.add(stack0)
                    } else {
                        val f = fluid.copy()
                        f.stackSize = f.stackSize - amount
                        val fl = fluid.fluidStack.copy()
                        fl.amount = amount
                        handler.fill(dir.opposite, fl, true)
                        removeFromExport.add(stack0)
                        addToExport.add(f)
                        return
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
            for (stack in patter.condensedFluidInputs) {
                if (fluids.containsKey(stack!!.fluid)) {
                    val amount = (fluids[stack!!.fluid]!!
                            + stack!!.stackSize)
                    fluids.remove(stack!!.fluid)
                    fluids[stack!!.fluid] = amount
                } else {
                    fluids[stack!!.fluid] = stack!!.stackSize
                }
            }
            val grid = gridNode.grid ?: return false
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
            for (s in patter.condensedInputs) {
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
        if (tag.hasKey("filter")) fluidFilter = tag.getInteger("filter")
    }

    override fun readFromNBT(data: NBTTagCompound) {
        super.readFromNBT(data)
        if (data.hasKey("tank")) tank.readFromNBT(data.getCompoundTag("tank"))
        if (data.hasKey("filter")) fluidFilter = data.getInteger("filter")
        if (data.hasKey("inventory")) inventory.readFromNBT(data.getCompoundTag("inventory"))
        if (data.hasKey("export")) readOutputFromNBT(data.getCompoundTag("export"))
    }

    @Throws(IOException::class)
    override fun readFromStream(data: ByteBuf): Boolean {
        super.readFromStream(data)
        val tag = ByteBufUtils.readTag(data)
        if (tag.hasKey("tank")) tank.readFromNBT(tag.getCompoundTag("tank"))
        if (tag.hasKey("filter")) fluidFilter = tag.getInteger("filter")
        if (tag.hasKey("inventory")) inventory.readFromNBT(tag.getCompoundTag("inventory"))
        return true
    }

    private fun readOutputFromNBT(tag: NBTTagCompound) {
        addToExport.clear()
        removeFromExport.clear()
        export.clear()
        var i = tag.getInteger("remove")
        for (j in 0 until i) {
            if (tag.getBoolean("remove-$j-isItem")) {
                val s = AEApi
                        .instance()
                        .storage()
                        .createItemStack(
                                ItemStack.loadItemStackFromNBT(tag
                                        .getCompoundTag("remove-$j")))
                s.stackSize = tag.getLong("remove-$j-amount")
                removeFromExport.add(s)
            } else {
                val s = AEApi
                        .instance()
                        .storage()
                        .createFluidStack(
                                FluidStack.loadFluidStackFromNBT(tag
                                        .getCompoundTag("remove-$j")))
                s.stackSize = tag.getLong("remove-$j-amount")
                removeFromExport.add(s)
            }
        }
        i = tag.getInteger("add")
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

    @SideOnly(Side.CLIENT)
    override fun renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks) {
        val ts = Tessellator.instance
        val side = TextureManager.BUS_SIDE.texture
        rh.setTexture(side, side, side,
                TextureManager.INTERFACE.textures[0], side, side)
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 16f)
        rh.renderInventoryBox(renderer)
        rh.renderInventoryFace(TextureManager.INTERFACE.textures[0],
                ForgeDirection.SOUTH, renderer)
        rh.setTexture(side)
        rh.setBounds(5f, 5f, 12f, 11f, 11f, 14f)
        rh.renderInventoryBox(renderer)
    }

    @SideOnly(Side.CLIENT)
    override fun renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper,
                              renderer: RenderBlocks) {
        val ts = Tessellator.instance
        val side = TextureManager.BUS_SIDE.texture
        rh.setTexture(side, side, side,
                TextureManager.INTERFACE.textures[0], side, side)
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 16f)
        rh.renderBlock(x, y, z, renderer)
        ts.setBrightness(20971520)
        rh.renderFace(x, y, z, TextureManager.INTERFACE.textures[0],
                ForgeDirection.SOUTH, renderer)
        rh.setTexture(side)
        rh.setBounds(5f, 5f, 12f, 11f, 11f, 14f)
        rh.renderBlock(x, y, z, renderer)
    }

    override fun setFilter(side: ForgeDirection?, fluid: Fluid?) {
        if (fluid == null) {
            fluidFilter = -1
            doNextUpdate = true
            return
        }
        fluidFilter = fluid.id
        doNextUpdate = true
    }

    override fun setFluid(_index: Int, _fluid: Fluid?, _player: EntityPlayer?) {
        setFilter(ForgeDirection.getOrientation(_index), _fluid)
    }

    override fun setFluidTank(side: ForgeDirection?, fluid: FluidStack?) {
        tank.fluid = fluid
        doNextUpdate = true
    }

    override fun tickingRequest(node: IGridNode,
                                TicksSinceLastCall: Int): TickRateModulation {
        if (doNextUpdate) forceUpdate()
        val grid = node.grid ?: return TickRateModulation.URGENT
        val storage = grid.getCache<IStorageGrid>(IStorageGrid::class.java) ?: return TickRateModulation.URGENT
        pushItems()
        if (toExport != null) {
            storage.itemInventory.injectItems(toExport,
                    Actionable.MODULATE, MachineSource(this))
            toExport = null
        }
        if (update) {
            update = false
            if (gridNode != null && gridNode.grid != null) {
                gridNode.grid
                        .postEvent(
                                MENetworkCraftingPatternChange(this,
                                        gridNode))
            }
        }
        if (tank.fluid != null
                && FluidRegistry.getFluid(fluidFilter) !== tank
                        .fluid.getFluid()) {
            val s = tank.drain(125, false)
            if (s != null) {
                val notAdded = storage.fluidInventory
                        .injectItems(
                                AEApi.instance().storage()
                                        .createFluidStack(s.copy()),
                                Actionable.SIMULATE, MachineSource(this))
                if (notAdded != null) {
                    val toAdd = (s.amount - notAdded.stackSize).toInt()
                    storage.fluidInventory.injectItems(
                            AEApi.instance()
                                    .storage()
                                    .createFluidStack(
                                            tank.drain(toAdd, true)),
                            Actionable.MODULATE, MachineSource(this))
                    doNextUpdate = true
                    needBreake = false
                } else {
                    storage.fluidInventory.injectItems(
                            AEApi.instance()
                                    .storage()
                                    .createFluidStack(
                                            tank.drain(s.amount, true)),
                            Actionable.MODULATE, MachineSource(this))
                    doNextUpdate = true
                    needBreake = false
                }
            }
        }
        if ((tank.fluid == null || tank.fluid.getFluid() === FluidRegistry
                        .getFluid(fluidFilter))
                && FluidRegistry.getFluid(fluidFilter) != null) {
            val extracted = storage.fluidInventory.extractItems(
                    AEApi.instance()
                            .storage()
                            .createFluidStack(
                                    FluidStack(FluidRegistry
                                            .getFluid(fluidFilter), 125)),
                    Actionable.SIMULATE, MachineSource(this))
                    ?: return TickRateModulation.URGENT
            val accepted = tank.fill(extracted.fluidStack, false)
            if (accepted == 0) return TickRateModulation.URGENT
            tank
                    .fill(storage
                            .fluidInventory
                            .extractItems(
                                    AEApi.instance()
                                            .storage()
                                            .createFluidStack(
                                                    FluidStack(
                                                            FluidRegistry
                                                                    .getFluid(fluidFilter),
                                                            accepted)),
                                    Actionable.MODULATE,
                                    MachineSource(this)).fluidStack,
                            true)
            doNextUpdate = true
            needBreake = false
        }
        return TickRateModulation.URGENT
    }

    fun writeFilter(tag: NBTTagCompound): NBTTagCompound? {
        if (FluidRegistry.getFluid(fluidFilter) == null) return null
        tag.setInteger("filter", fluidFilter)
        return tag
    }

    private fun writeOutputToNBT(tag: NBTTagCompound): NBTTagCompound {
        var i = 0
        for (s in removeFromExport) {
            if (s != null) {
                tag.setBoolean("remove-$i-isItem", s.isItem)
                val data = NBTTagCompound()
                if (s.isItem) {
                    (s as IAEItemStack).itemStack.writeToNBT(data)
                } else {
                    (s as IAEFluidStack).fluidStack.writeToNBT(data)
                }
                tag.setTag("remove-$i", data)
                tag.setLong("remove-$i-amount", s.stackSize)
            }
            i++
        }
        tag.setInteger("remove", removeFromExport.size)
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

    fun writeToNBTWithoutExport(data: NBTTagCompound) {
        super.writeToNBT(data)
        data.setTag("tank", tank.writeToNBT(NBTTagCompound()))
        data.setInteger("filter", fluidFilter)
        val inventory = NBTTagCompound()
        this.inventory.writeToNBT(inventory)
        data.setTag("inventory", inventory)
    }

    @Throws(IOException::class)
    override fun writeToStream(data: ByteBuf) {
        super.writeToStream(data)
        val tag = NBTTagCompound()
        tag.setTag("tank", tank.writeToNBT(NBTTagCompound()))
        tag.setInteger("filter", fluidFilter)
        val inventory = NBTTagCompound()
        this.inventory.writeToNBT(inventory)
        tag.setTag("inventory", inventory)
        ByteBufUtils.writeTag(data, tag)
    }
}