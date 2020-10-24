package extracells.container

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.networking.security.BaseActionSource
import appeng.api.networking.security.PlayerSource
import appeng.api.networking.storage.IBaseMonitor
import appeng.api.storage.IMEMonitor
import appeng.api.storage.IMEMonitorHandlerReceiver
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IItemList
import extracells.api.IPortableFluidStorageCell
import extracells.api.IWirelessFluidTermHandler
import extracells.container.slot.SlotPlayerInventory
import extracells.container.slot.SlotRespective
import extracells.gui.GuiFluidStorage
import extracells.gui.widget.fluid.IFluidSelectorContainer
import extracells.inventory.HandlerItemStorageFluid
import extracells.network.packet.part.PacketFluidStorage
import extracells.util.FluidUtil
import extracells.util.inventory.ECPrivateInventory
import extracells.util.inventory.IInventoryUpdateReceiver
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.inventory.SlotFurnace
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack

class ContainerFluidStorage : Container, IMEMonitorHandlerReceiver<IAEFluidStack?>, IFluidSelectorContainer, IInventoryUpdateReceiver, IStorageContainer {
    private var guiFluidStorage: GuiFluidStorage? = null
    var fluidStackList: IItemList<IAEFluidStack?>? = null
        private set
    private var selectedFluid: Fluid? = null
    var selectedFluidStack: IAEFluidStack? = null
        private set
    val player: EntityPlayer
    private val monitor: IMEMonitor<IAEFluidStack?>?
    private val storageFluid: HandlerItemStorageFluid? = null
    private var handler: IWirelessFluidTermHandler? = null
    private var storageCell: IPortableFluidStorageCell? = null
    var hasWirelessTermHandler = false
    private val inventory: ECPrivateInventory = object : ECPrivateInventory("extracells.item.fluid.storage", 2, 64,
            this) {
        override fun isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean {
            return FluidUtil.isFluidContainer(itemStack)
        }
    }

    constructor(_player: EntityPlayer) : this(null, _player) {}
    constructor(_monitor: IMEMonitor<IAEFluidStack?>?, _player: EntityPlayer) {
        monitor = _monitor
        player = _player
        if (!player.worldObj.isRemote && monitor != null) {
            monitor.addListener(this, null)
            fluidStackList = monitor.storageList
        } else {
            fluidStackList = AEApi.instance().storage().createFluidList()
        }

        // Input Slot accepts all FluidContainers
        addSlotToContainer(SlotRespective(inventory, 0, 8, 92))
        // Input Slot accepts nothing
        addSlotToContainer(SlotFurnace(player, inventory, 1, 26,
                92))
        bindPlayerInventory(player.inventory)
    }

    constructor(_monitor: IMEMonitor<IAEFluidStack?>?, _player: EntityPlayer, _storageCell: IPortableFluidStorageCell?) {
        hasWirelessTermHandler = _storageCell != null
        storageCell = _storageCell
        monitor = _monitor
        player = _player
        if (!player.worldObj.isRemote && monitor != null) {
            monitor.addListener(this, null)
            fluidStackList = monitor.storageList
        } else {
            fluidStackList = AEApi.instance().storage().createFluidList()
        }

        // Input Slot accepts all FluidContainers
        addSlotToContainer(SlotRespective(inventory, 0, 8, 92))
        // Input Slot accepts nothing
        addSlotToContainer(SlotFurnace(player, inventory, 1, 26,
                92))
        bindPlayerInventory(player.inventory)
    }

    constructor(_monitor: IMEMonitor<IAEFluidStack?>?,
                _player: EntityPlayer, _handler: IWirelessFluidTermHandler?) {
        hasWirelessTermHandler = _handler != null
        handler = _handler
        monitor = _monitor
        player = _player
        if (!player.worldObj.isRemote && monitor != null) {
            monitor.addListener(this, null)
            fluidStackList = monitor.storageList
        } else {
            fluidStackList = AEApi.instance().storage().createFluidList()
        }

        // Input Slot accepts all FluidContainers
        addSlotToContainer(SlotRespective(inventory, 0, 8, 92))
        // Input Slot accepts nothing
        addSlotToContainer(SlotFurnace(player, inventory, 1, 26,
                92))
        bindPlayerInventory(player.inventory)
    }

    protected fun bindPlayerInventory(inventoryPlayer: InventoryPlayer?) {
        for (i in 0..2) {
            for (j in 0..8) {
                addSlotToContainer(SlotPlayerInventory(inventoryPlayer,
                        this, j + i * 9 + 9, 8 + j * 18, i * 18 + 122))
            }
        }
        for (i in 0..8) {
            addSlotToContainer(SlotPlayerInventory(inventoryPlayer, this,
                    i, 8 + i * 18, 180))
        }
    }

    override fun canInteractWith(entityplayer: EntityPlayer): Boolean {
        return true
    }

    fun decreaseFirstSlot() {
        val slot = inventory.getStackInSlot(0) ?: return
        slot.stackSize--
        if (slot.stackSize <= 0) inventory.setInventorySlotContents(0, null)
    }

    fun doWork() {
        val secondSlot = inventory.getStackInSlot(1)
        if (secondSlot != null && secondSlot.stackSize >= secondSlot.maxStackSize) return
        var container = inventory.getStackInSlot(0)
        if (!FluidUtil.isFluidContainer(container)) return
        if (monitor == null) return
        container = container!!.copy()
        container.stackSize = 1
        if (FluidUtil.isEmpty(container)) {
            if (selectedFluid == null) return
            val capacity = FluidUtil.getCapacity(container)
            //Tries to simulate the extraction of fluid from storage.
            val result = monitor.extractItems(FluidUtil.createAEFluidStack(selectedFluid, capacity.toLong()),
                    Actionable.SIMULATE, PlayerSource(
                    player, null))

            //Calculates the amount of fluid to fill container with.
            val proposedAmount = if (result == null) 0 else Math.min(capacity.toLong(), result.stackSize).toInt()
            if (proposedAmount == 0) return

            //Tries to fill the container with fluid.
            val filledContainer = FluidUtil.fillStack(container, FluidStack(selectedFluid, proposedAmount))

            //Moves it to second slot and commits extraction to grid.
            if (fillSecondSlot(filledContainer!!.getRight())) {
                monitor.extractItems(FluidUtil.createAEFluidStack(
                        selectedFluid, filledContainer.getLeft()),
                        Actionable.MODULATE,
                        PlayerSource(player, null))
                decreaseFirstSlot()
            }
        } else if (FluidUtil.isFilled(container)) {
            val containerFluid = FluidUtil.getFluidFromContainer(container)

            //Tries to inject fluid to network.
            val notInjected = monitor.injectItems(
                    FluidUtil.createAEFluidStack(containerFluid),
                    Actionable.SIMULATE, PlayerSource(player, null))
            if (notInjected != null) return
            if (handler != null) {
                if (!handler!!.hasPower(player, 20.0,
                                player.currentEquippedItem)) {
                    return
                }
                handler!!.usePower(player, 20.0,
                        player.currentEquippedItem)
            } else if (storageCell != null) {
                if (!storageCell!!.hasPower(player, 20.0,
                                player.currentEquippedItem)) {
                    return
                }
                storageCell!!.usePower(player, 20.0,
                        player.currentEquippedItem)
            }
            val drainedContainer = FluidUtil.drainStack(container, containerFluid)
            if (fillSecondSlot(drainedContainer!!.getRight())) {
                monitor.injectItems(
                        FluidUtil.createAEFluidStack(containerFluid),
                        Actionable.MODULATE,
                        PlayerSource(player, null))
                decreaseFirstSlot()
            }
        }
    }

    fun fillSecondSlot(itemStack: ItemStack?): Boolean {
        if (itemStack == null) return false
        val secondSlot = inventory.getStackInSlot(1)
        return if (secondSlot == null) {
            if (handler != null) {
                if (!handler!!.hasPower(player, 20.0,
                                player.currentEquippedItem)) {
                    return false
                }
                handler!!.usePower(player, 20.0,
                        player.currentEquippedItem)
            } else if (storageCell != null) {
                if (!storageCell!!.hasPower(player, 20.0,
                                player.currentEquippedItem)) {
                    return false
                }
                storageCell!!.usePower(player, 20.0,
                        player.currentEquippedItem)
            }
            inventory.setInventorySlotContents(1, itemStack)
            true
        } else {
            if (!secondSlot.isItemEqual(itemStack)
                    || !ItemStack.areItemStackTagsEqual(itemStack, secondSlot)) return false
            if (handler != null) {
                if (!handler!!.hasPower(player, 20.0,
                                player.currentEquippedItem)) {
                    return false
                }
                handler!!.usePower(player, 20.0,
                        player.currentEquippedItem)
            } else if (storageCell != null) {
                if (!storageCell!!.hasPower(player, 20.0,
                                player.currentEquippedItem)) {
                    return false
                }
                storageCell!!.usePower(player, 20.0,
                        player.currentEquippedItem)
            }
            inventory.incrStackSize(1, itemStack.stackSize)
            true
        }
    }

    fun forceFluidUpdate() {
        if (monitor != null) PacketFluidStorage(player, monitor.storageList)
                .sendPacketToPlayer(player)
        PacketFluidStorage(player, hasWirelessTermHandler)
                .sendPacketToPlayer(player)
    }

    fun getSelectedFluid(): Fluid? {
        return selectedFluid
    }

    override fun hasWirelessTermHandler(): Boolean {
        return hasWirelessTermHandler
    }

    override fun isValid(verificationToken: Any): Boolean {
        return true
    }

    override fun onContainerClosed(entityPlayer: EntityPlayer) {
        super.onContainerClosed(entityPlayer)
        if (!entityPlayer.worldObj.isRemote) {
            monitor!!.removeListener(this)
            for (i in 0..1) player.dropPlayerItemWithRandomChoice(
                    (inventorySlots[i] as Slot).stack, false)
        }
    }

    override fun onInventoryChanged() {}
    override fun onListUpdate() {}
    override fun postChange(monitor: IBaseMonitor<IAEFluidStack?>, change: Iterable<IAEFluidStack?>, actionSource: BaseActionSource) {
        fluidStackList = (monitor as IMEMonitor<IAEFluidStack?>).storageList
        PacketFluidStorage(player, fluidStackList).sendPacketToPlayer(player)
        PacketFluidStorage(player, hasWirelessTermHandler).sendPacketToPlayer(player)
    }

    fun receiveSelectedFluid(_selectedFluid: Fluid?) {
        selectedFluid = _selectedFluid
        if (selectedFluid != null) {
            for (stack in fluidStackList!!) {
                if (stack != null && stack.fluid === selectedFluid) {
                    selectedFluidStack = stack
                    break
                }
            }
        } else {
            selectedFluidStack = null
        }
        if (guiFluidStorage != null) guiFluidStorage!!.updateSelectedFluid()
    }

    fun removeEnergyTick() {
        if (handler != null) {
            if (handler!!.hasPower(player, 1.0,
                            player.currentEquippedItem)) {
                handler!!.usePower(player, 1.0,
                        player.currentEquippedItem)
            }
        } else if (storageCell != null) {
            if (storageCell!!.hasPower(player, 0.5,
                            player.currentEquippedItem)) {
                storageCell!!.usePower(player, 0.5,
                        player.currentEquippedItem)
            }
        }
    }

    fun setGui(_guiFluidStorage: GuiFluidStorage?) {
        guiFluidStorage = _guiFluidStorage
    }

    override fun setSelectedFluid(_selectedFluid: Fluid?) {
        PacketFluidStorage(player, _selectedFluid)
                .sendPacketToServer()
        receiveSelectedFluid(_selectedFluid)
    }

    override fun transferStackInSlot(player: EntityPlayer, slotnumber: Int): ItemStack {
        var itemstack: ItemStack? = null
        val slot = inventorySlots[slotnumber] as Slot?
        if (slot != null && slot.hasStack) {
            val itemstack1 = slot.stack
            itemstack = itemstack1.copy()
            if (inventory.isItemValidForSlot(0, itemstack1)) {
                if (slotnumber == 0 || slotnumber == 1) {
                    if (!mergeItemStack(itemstack1, 2, 36, false)) return null
                } else if (!mergeItemStack(itemstack1, 0, 1, false)) {
                    return null
                }
                if (itemstack1.stackSize == 0) {
                    slot.putStack(null)
                } else {
                    slot.onSlotChanged()
                }
            } else {
                return null
            }
        }
        return itemstack!!
    }

    fun updateFluidList(_fluidStackList: IItemList<IAEFluidStack?>?) {
        fluidStackList = _fluidStackList
        if (guiFluidStorage != null) guiFluidStorage!!.updateFluids()
    }
}