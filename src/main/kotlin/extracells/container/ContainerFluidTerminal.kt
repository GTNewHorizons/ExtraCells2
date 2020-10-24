package extracells.container

import appeng.api.AEApi
import appeng.api.config.SecurityPermissions
import appeng.api.networking.security.BaseActionSource
import appeng.api.networking.storage.IBaseMonitor
import appeng.api.parts.IPart
import appeng.api.storage.IMEMonitor
import appeng.api.storage.IMEMonitorHandlerReceiver
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IItemList
import extracells.container.slot.SlotRespective
import extracells.gui.GuiFluidTerminal
import extracells.gui.widget.fluid.IFluidSelectorContainer
import extracells.network.packet.part.PacketFluidTerminal
import extracells.part.PartFluidTerminal
import extracells.util.FluidUtil
import extracells.util.PermissionUtil
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.inventory.SlotFurnace
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.Fluid

class ContainerFluidTerminal(val terminal: PartFluidTerminal?,
                             val player: EntityPlayer) : Container(), IMEMonitorHandlerReceiver<IAEFluidStack?>, IFluidSelectorContainer {
    private var monitor: IMEMonitor<IAEFluidStack?>? = null
    var fluidStackList = AEApi.instance()
            .storage().createFluidList()
        private set
    private var selectedFluid: Fluid? = null
    private var guiFluidTerminal: GuiFluidTerminal? = null
    protected fun bindPlayerInventory(inventoryPlayer: InventoryPlayer?) {
        for (i in 0..2) {
            for (j in 0..8) {
                addSlotToContainer(Slot(inventoryPlayer, j + i * 9 + 9,
                        8 + j * 18, i * 18 + 122))
            }
        }
        for (i in 0..8) {
            addSlotToContainer(Slot(inventoryPlayer, i, 8 + i * 18, 180))
        }
    }

    override fun canInteractWith(entityplayer: EntityPlayer): Boolean {
        return terminal?.isValid ?: false
    }

    fun forceFluidUpdate() {
        if (monitor != null) {
            PacketFluidTerminal(player, monitor.storageList)
                    .sendPacketToPlayer(player)
        }
    }

    fun getSelectedFluid(): Fluid? {
        return selectedFluid
    }

    override fun isValid(verificationToken: Any): Boolean {
        return true
    }

    override fun onContainerClosed(entityPlayer: EntityPlayer) {
        super.onContainerClosed(entityPlayer)
        if (!entityPlayer.worldObj.isRemote) {
            if (monitor != null) monitor.removeListener(this)
            terminal!!.removeContainer(this)
        }
    }

    override fun onListUpdate() {}
    override fun postChange(monitor: IBaseMonitor<IAEFluidStack?>,
                            change: Iterable<IAEFluidStack?>, actionSource: BaseActionSource) {
        fluidStackList = (monitor as IMEMonitor<IAEFluidStack?>)
                .storageList
        PacketFluidTerminal(player, fluidStackList)
                .sendPacketToPlayer(player)
    }

    fun receiveSelectedFluid(_selectedFluid: Fluid?) {
        selectedFluid = _selectedFluid
        if (guiFluidTerminal != null) guiFluidTerminal!!.updateSelectedFluid()
    }

    fun setGui(_guiFluidTerminal: GuiFluidTerminal?) {
        if (_guiFluidTerminal != null) guiFluidTerminal = _guiFluidTerminal
    }

    override fun setSelectedFluid(_selectedFluid: Fluid?) {
        PacketFluidTerminal(player, _selectedFluid, terminal)
                .sendPacketToServer()
    }

    override fun slotClick(slotNumber: Int, p_75144_2_: Int, p_75144_3_: Int,
                           player: EntityPlayer): ItemStack {
        var returnStack: ItemStack? = null
        var hasPermission = true
        if (slotNumber == 0 || slotNumber == 1) {
            val stack = player.inventory.itemStack
            if (stack == null) {
            } else {
                if (FluidUtil.isEmpty(stack)
                        && PermissionUtil.hasPermission(player,
                                SecurityPermissions.INJECT,
                                terminal as IPart?)) {
                } else if (FluidUtil.isFilled(stack)
                        && PermissionUtil.hasPermission(player,
                                SecurityPermissions.EXTRACT,
                                terminal as IPart?)) {
                } else {
                    val slotStack = (inventorySlots[slotNumber] as Slot).stack
                    returnStack = slotStack?.copy()
                    hasPermission = false
                }
            }
        }
        if (hasPermission) returnStack = super.slotClick(slotNumber, p_75144_2_, p_75144_3_,
                player)
        if (player is EntityPlayerMP) {
            player.sendContainerToPlayer(this)
        }
        return returnStack!!
    }

    override fun transferStackInSlot(player: EntityPlayer, slotnumber: Int): ItemStack {
        var itemstack: ItemStack? = null
        val slot = inventorySlots[slotnumber] as Slot?
        if (slot != null && slot.hasStack) {
            val itemstack1 = slot.stack
            itemstack = itemstack1.copy()
            if (terminal!!.inventory.isItemValidForSlot(0, itemstack1)) {
                if (slotnumber == 1 || slotnumber == 0) {
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
        if (guiFluidTerminal != null) guiFluidTerminal!!.updateFluids()
    }

    init {
        if (!player.worldObj.isRemote) {
            monitor = terminal.getGridBlock().fluidMonitor
            if (monitor != null) {
                monitor.addListener(this, null)
                fluidStackList = monitor.storageList
            }
            terminal!!.addContainer(this)
        }

        // Input Slot accepts all FluidContainers
        addSlotToContainer(SlotRespective(terminal!!.inventory, 0,
                8, 92))
        // Input Slot accepts nothing
        addSlotToContainer(SlotFurnace(player,
                terminal.inventory, 1, 26, 92))
        bindPlayerInventory(player.inventory)
    }
}