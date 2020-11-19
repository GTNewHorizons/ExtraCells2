package extracells.tileentity

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.implementations.ICraftingPatternItem
import appeng.api.networking.IGrid
import appeng.api.networking.IGridNode
import appeng.api.networking.crafting.*
import appeng.api.networking.events.MENetworkCraftingPatternChange
import appeng.api.networking.security.IActionHost
import appeng.api.networking.security.MachineSource
import appeng.api.networking.storage.IStorageGrid
import appeng.api.storage.data.IAEItemStack
import appeng.api.util.AECableType
import appeng.api.util.DimensionalCoord
import cpw.mods.fml.common.FMLCommonHandler
import extracells.api.IECTileEntity
import extracells.crafting.CraftingPattern
import extracells.gridblock.ECFluidGridBlock
import extracells.util.PatternUtil
import extracells.util.SlotUtil
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import java.util.*

open class TileEntityFluidCrafter : TileBase(), IActionHost, ICraftingProvider, ICraftingWatcherHost, IECTileEntity {
    inner class FluidCrafterInventory : IInventory {
        val inv = arrayOfNulls<ItemStack>(9)
        override fun closeInventory() {}
        override fun decrStackSize(slot: Int, amt: Int): ItemStack? {
            val its = SlotUtil.decreaseStackInSlot(this, slot, amt)
            update = true
            return its
        }

        override fun getInventoryName(): String {
            return "inventory.fluidCrafter"
        }

        override fun getInventoryStackLimit(): Int {
            return 1
        }

        override fun getSizeInventory(): Int {
            return inv.size
        }

        override fun getStackInSlot(slot: Int): ItemStack? {
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
                return details != null && details.isCraftable
            }
            return false
        }

        override fun isUseableByPlayer(player: EntityPlayer): Boolean {
            return true
        }

        override fun markDirty() {}
        override fun openInventory() {}
        fun readFromNBT(tagCompound: NBTTagCompound) {
            SlotUtil.readSlotsFromNbt(inv, tagCompound)
        }

        override fun setInventorySlotContents(slot: Int, stack: ItemStack?) {
            inv[slot] = stack
            if (stack != null && stack.stackSize > inventoryStackLimit) {
                stack.stackSize = inventoryStackLimit
            }
            update = true
        }

        fun writeToNBT(tagCompound: NBTTagCompound) {
            SlotUtil.writeSlotsToNbt(inv, tagCompound)
        }
    }

    private val gridBlock: ECFluidGridBlock
    private var node: IGridNode? = null
    private var patternHandlers: MutableList<ICraftingPatternDetails> = ArrayList()
    private var requestedItems: MutableList<IAEItemStack> = ArrayList()
    private val removeList: MutableList<IAEItemStack> = ArrayList()
    private var patternHandlerSlot = arrayOfNulls<ICraftingPatternDetails>(9)
    private val oldStack = arrayOfNulls<ItemStack>(9)
    private var isBusy = false
    private var watcher: ICraftingWatcher? = null
    private var isFirstGetGridNode = true
    val inventory: FluidCrafterInventory
    private var finishCraftingTime = 0L
    private var returnStack: ItemStack? = null
    private var optionalReturnStack = arrayOfNulls<ItemStack>(0)
    private var update = false
    private val instance: TileEntityFluidCrafter
    override fun getActionableNode(): IGridNode? {
        if (FMLCommonHandler.instance().effectiveSide.isClient) return null
        if (node == null) {
            node = AEApi.instance().createGridNode(gridBlock)
        }
        return node!!
    }

    override fun getCableConnectionType(dir: ForgeDirection): AECableType {
        return AECableType.SMART
    }

    val gridNode: IGridNode?
        get() = getGridNode(ForgeDirection.UNKNOWN)

    override fun getGridNode(dir: ForgeDirection): IGridNode? {
        if (FMLCommonHandler.instance().side.isClient
                && (getWorldObj() == null || getWorldObj().isRemote)) return null
        if (isFirstGetGridNode) {
            isFirstGetGridNode = false
            actionableNode?.updateState()
        }
        return node!!
    }

    fun getInventory(): IInventory {
        return inventory
    }

    override val location: DimensionalCoord
        get() = DimensionalCoord(this)
    override val powerUsage: Double
        get() = 0.0

    override fun isBusy(): Boolean {
        return isBusy
    }

    override fun onRequestChange(craftingGrid: ICraftingGrid, what: IAEItemStack) {
        if (craftingGrid.isRequesting(what)) {
            if (!requestedItems.contains(what)) {
                requestedItems.add(what)
            }
        } else requestedItems.remove(what)
    }

    override fun provideCrafting(craftingTracker: ICraftingProviderHelper) {
        patternHandlers = ArrayList()
        val oldHandler = patternHandlerSlot
        patternHandlerSlot = arrayOfNulls(9)
        var i = 0
        while (inventory.inv.size > i) {
            val currentPatternStack = inventory.inv[i]
            val oldItem = oldStack[i]
            if (currentPatternStack != null && oldItem != null && ItemStack.areItemStacksEqual(currentPatternStack,
                            oldItem)) {
                val pa = oldHandler[i]
                if (pa != null) {
                    patternHandlerSlot[i] = pa
                    patternHandlers.add(pa)
                    if (pa.condensedInputs.size == 0) {
                        craftingTracker.setEmitable(pa.condensedOutputs[0])
                    } else {
                        craftingTracker.addCraftingOption(this, pa)
                    }
                    i++
                    continue
                }
            }
            if (currentPatternStack != null && currentPatternStack.item != null && currentPatternStack.item is ICraftingPatternItem) {
                val currentPattern = currentPatternStack
                        .item as ICraftingPatternItem?
                if (currentPattern?.getPatternForItem(
                                currentPatternStack, getWorldObj()) != null && currentPattern.getPatternForItem(
                                currentPatternStack, getWorldObj())
                                .isCraftable) {
                    val pattern: ICraftingPatternDetails = CraftingPattern(
                            currentPattern.getPatternForItem(
                                    currentPatternStack, getWorldObj()))
                    patternHandlers.add(pattern)
                    patternHandlerSlot[i] = pattern
                    if (pattern.condensedInputs.isEmpty()) {
                        craftingTracker.setEmitable(pattern
                                .condensedOutputs[0])
                    } else {
                        craftingTracker.addCraftingOption(this, pattern)
                    }
                }
            }
            oldStack[i] = currentPatternStack
            i++
        }
        updateWatcher()
    }

    override fun pushPattern(patternDetails: ICraftingPatternDetails,
                             table: InventoryCrafting): Boolean {
        if (isBusy) return false
        if (patternDetails is CraftingPattern) {
            val fluids = HashMap<Fluid, Long>()
            PatternUtil.pushPatternToFluid(patternDetails, fluids)
            val grid = node!!.grid ?: return false
            val storage = grid.getCache<IStorageGrid>(IStorageGrid::class.java) ?: return false
            if (!PatternUtil.canExtractFluid(storage, fluids, this))
                return false
            for (fluid in fluids.keys) {
                val amount = fluids[fluid]
                storage.fluidInventory.extractItems(
                        AEApi.instance().storage().createFluidStack(FluidStack(fluid, (amount!! + 0).toInt())),
                        Actionable.MODULATE, MachineSource(this))
            }
            finishCraftingTime = System.currentTimeMillis() + 1000
            returnStack = patternDetails.getOutput(table, getWorldObj())
            optionalReturnStack = arrayOfNulls(9)
            for (i in 0..8) {
                val s = table.getStackInSlot(i)
                if (s != null && s.item != null) {
                    optionalReturnStack[i] = s.item.getContainerItem(s.copy())
                }
            }
            isBusy = true
        }
        return true
    }

    override fun readFromNBT(tagCompound: NBTTagCompound) {
        super.readFromNBT(tagCompound)
        inventory.readFromNBT(tagCompound)
        if (hasWorldObj()) {
            val node = gridNode
            if (tagCompound.hasKey("nodes") && node != null) {
                node.loadFromNBT("node0", tagCompound.getCompoundTag("nodes"))
                node.updateState()
            }
        }
    }

    override fun securityBreak() {}
    override fun updateEntity() {
        if (getWorldObj() == null || getWorldObj().provider == null) return
        if (update) {
            update = false
            if (gridNode != null && gridNode!!.grid != null) {
                gridNode!!.grid.postEvent(MENetworkCraftingPatternChange(instance, gridNode))
            }
        }
        if (isBusy && finishCraftingTime <= System.currentTimeMillis() && getWorldObj() != null && !getWorldObj().isRemote) {
            if (node == null || returnStack == null) return
            val grid = node!!.grid ?: return
            val storage = grid.getCache<IStorageGrid>(IStorageGrid::class.java) ?: return
            storage.itemInventory.injectItems(AEApi.instance().storage().createItemStack(returnStack),
                    Actionable.MODULATE, MachineSource(this))
            for (s in optionalReturnStack) {
                if (s == null) continue
                storage.itemInventory.injectItems(AEApi.instance().storage().createItemStack(s), Actionable.MODULATE,
                        MachineSource(this))
            }
            optionalReturnStack = arrayOfNulls(0)
            isBusy = false
            returnStack = null
        }
        if (!isBusy && getWorldObj() != null && !getWorldObj().isRemote) {
            for (stack in removeList) {
                requestedItems.remove(stack)
            }
            removeList.clear()
            if (!requestedItems.isEmpty()) {
                for (s in requestedItems) {
                    val grid = node!!.grid ?: break
                    val crafting = grid.getCache<ICraftingGrid>(ICraftingGrid::class.java) ?: break
                    if (!crafting.isRequesting(s)) {
                        removeList.add(s)
                        continue
                    }
                    for (details in patternHandlers) {
                        if (details.condensedOutputs[0] == s) {
                            val patter = details as CraftingPattern
                            val fluids = HashMap<Fluid, Long>()
                            PatternUtil.pushPatternToFluid(patter, fluids)
                            val storage = grid.getCache<IStorageGrid>(IStorageGrid::class.java) ?: break
                            var doBreak = false
                            for (fluid in fluids.keys) {
                                val amount = fluids[fluid]
                                val extractFluid = storage.fluidInventory.extractItems(
                                        AEApi.instance().storage().createFluidStack(
                                                FluidStack(fluid, (amount!! + 0).toInt())), Actionable.SIMULATE,
                                        MachineSource(this))
                                if (extractFluid == null || extractFluid.stackSize != amount) {
                                    doBreak = true
                                    break
                                }
                            }
                            if (doBreak) break
                            for (fluid in fluids.keys) {
                                val amount = fluids[fluid]
                                storage.fluidInventory.extractItems(
                                        AEApi.instance().storage().createFluidStack(
                                                FluidStack(fluid, (amount!! + 0).toInt())), Actionable.MODULATE,
                                        MachineSource(this))
                            }
                            finishCraftingTime = System.currentTimeMillis() + 1000
                            returnStack = patter.condensedOutputs?.get(0)?.itemStack
                            isBusy = true
                            return
                        }
                    }
                }
            }
        }
    }

    private fun updateWatcher() {
        requestedItems = ArrayList()
        val grid: IGrid?
        val node = gridNode
        var crafting: ICraftingGrid? = null
        if (node != null) {
            grid = node.grid
            if (grid != null) {
                crafting = grid.getCache(ICraftingGrid::class.java)
            }
        }
        for (patter in patternHandlers) {
            watcher!!.clear()
            if (patter.condensedInputs.size == 0) {
                watcher!!.add(patter.condensedOutputs[0])
                if (crafting != null) {
                    if (crafting.isRequesting(patter.condensedOutputs[0])) {
                        requestedItems
                                .add(patter.condensedOutputs[0])
                    }
                }
            }
        }
    }

    override fun updateWatcher(newWatcher: ICraftingWatcher) {
        watcher = newWatcher
        updateWatcher()
    }

    override fun writeToNBT(tagCompound: NBTTagCompound) {
        super.writeToNBT(tagCompound)
        inventory.writeToNBT(tagCompound)
        if (!hasWorldObj()) return
        val node = gridNode
        if (node != null) {
            val nodeTag = NBTTagCompound()
            node.saveToNBT("node0", nodeTag)
            tagCompound.setTag("nodes", nodeTag)
        }
    }

    init {
        gridBlock = ECFluidGridBlock(this)
        inventory = FluidCrafterInventory()
        instance = this
    }
}