package extracells.integration.opencomputers

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.implementations.tiles.IWirelessAccessPoint
import appeng.api.networking.*
import appeng.api.networking.security.MachineSource
import appeng.api.networking.storage.IStorageGrid
import appeng.api.storage.IMEMonitor
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IAEItemStack
import appeng.api.util.DimensionalCoord
import appeng.api.util.WorldCoord
import appeng.tile.misc.TileSecurity
import extracells.item.ItemOCUpgrade
import li.cil.oc.api.internal.*
import li.cil.oc.api.machine.Arguments
import li.cil.oc.api.machine.Callback
import li.cil.oc.api.machine.Context
import li.cil.oc.api.network.*
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.FluidContainerRegistry
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.IFluidTank
open class UpgradeAE(private val host: EnvironmentHost) : UpgradeAEBase(host) {
    val robot: Robot? = if (host is Robot) host else null
    val drone: Drone? = if (host is Drone) host else null
    var isActive: Boolean = false
    val agent: Agent = host as Agent

    override fun getComponent(): ItemStack? {
        if (robot != null)
            return robot.getStackInSlot(robot.componentSlot(node().address()))
        else
            drone?.internalComponents()?.forEach {
                if(it != null && it.item == ItemOCUpgrade)
                    return@getComponent it
        }
        return null
    }

    override fun getSecurity(): IGridHost? {
        if (host.world().isRemote) return null
        val component: ItemStack? = component
        val sec: IGridHost = AEApi.instance().registries().locatable().getLocatableBy(getAEKey(component)) as IGridHost
        return if (checkRange(component, sec)) sec else null
    }

    override fun checkRange(stack: ItemStack?, sec: IGridHost?): Boolean {
        if (sec == null) return false
        val gridNode: IGridNode = sec.getGridNode(ForgeDirection.UNKNOWN) ?: return false
        val grid: IGrid = gridNode.grid ?: return false
        return when (stack?.itemDamage) {
                0 -> {
                    grid.getMachines(AEApi.instance().definitions().blocks().wireless().maybeEntity().get() as Class<out IGridHost>).iterator().hasNext()
                }
                1 -> {
                    val gridBlock: IGridBlock = gridNode.gridBlock ?: return false
                    val loc: DimensionalCoord = gridBlock.location ?: return false
                    for (node in grid.getMachines(AEApi.instance().definitions().blocks().wireless().maybeEntity().get() as Class<out IGridHost>)) {
                        val accessPoint: IWirelessAccessPoint = node.machine as IWirelessAccessPoint
                        val distance: WorldCoord = accessPoint.location.subtract(agent.xPosition().toInt(), agent.yPosition().toInt(), agent.zPosition().toInt())
                        val squaredDistance: Int = distance.x * distance.x + distance.y * distance.y + distance.z * distance.z
                        val range: Double = accessPoint.range
                        if (squaredDistance <= range * range)
                            return true
                    }
                    return false
                }
                else -> {
                    val gridBlock: IGridBlock = gridNode.gridBlock ?: return false
                    val loc: DimensionalCoord = gridBlock.location ?: return false
                    for (node in grid.getMachines(AEApi.instance().definitions().blocks().wireless().maybeEntity().get() as Class<out IGridHost>)) {
                        val accessPoint: IWirelessAccessPoint = node.machine as IWirelessAccessPoint
                        val distance: WorldCoord = accessPoint.location.subtract(agent.xPosition().toInt(), agent.yPosition().toInt(), agent.zPosition().toInt())
                        val squaredDistance: Int = distance.x * distance.x + distance.y * distance.y + distance.z * distance.z
                        val range: Double = accessPoint.range / 2
                        if (squaredDistance <= range * range) return true
                    }
                    false
                }
            }
        }

    override fun getGrid(): IGrid? {
        if (host.world().isRemote) return null
        val securityTerminal: IGridHost = security ?: return null
        val gridNode: IGridNode = securityTerminal.getGridNode(ForgeDirection.UNKNOWN) ?: return null
        return gridNode.grid
    }

    override fun getAEKey(stack: ItemStack?): Long {
        try {
            return WirelessHandlerUpgradeAE.getEncryptionKey(stack).toLong()
        } catch (_: Throwable) {
        }
        return 0L
    }

    override fun tile(): TileSecurity {
        val sec: IGridHost = security ?: throw SecurityException("No Security Station")
        val node: IGridNode = sec.getGridNode(ForgeDirection.UNKNOWN) ?: throw SecurityException("No Security Station")
        val gridBlock: IGridBlock = node.gridBlock
        val coord: DimensionalCoord = gridBlock.location
        val tileSecurity: TileSecurity = coord.world.getTileEntity(coord.x, coord.y, coord.z) as TileSecurity
        return tileSecurity
    }

    override fun getFluidInventory(): IMEMonitor<IAEFluidStack>? {
        val grid: IGrid = grid ?: return null
        val storage: IStorageGrid = grid.getCache(IStorageGrid::class.java) ?: return null
        return storage.fluidInventory
    }

    override fun getItemInventory(): IMEMonitor<IAEItemStack>? {
        val grid: IGrid = grid ?: return null
        val storage: IStorageGrid = grid.getCache(IStorageGrid::class.java) ?: return null
        return storage.itemInventory
    }

    @Callback(doc = "function([number:amount]):number -- Transfer selected items to your ae system.")
    override fun sendItems(context: Context, args: Arguments): Array<Any> {
        val selected: Int = agent.selectedSlot()
        val invRobot: IInventory = agent.mainInventory()
        if (invRobot.sizeInventory <= 0)
            return arrayOf(0)
        val stack: ItemStack? = invRobot.getStackInSlot(selected)
        val inv: IMEMonitor<IAEItemStack>? = itemInventory
        if (stack == null || inv == null)
            return arrayOf(0)
        val amount: Int = args.optInteger(0, 64).coerceAtMost(stack.stackSize)
        val stack2: ItemStack = stack.copy()
        stack2.stackSize = amount
        val notInjected: IAEItemStack? = inv.injectItems(AEApi.instance().storage().createItemStack(stack2), Actionable.MODULATE, MachineSource(tile()))
        return if (notInjected == null) {
            stack.stackSize -= (amount)
            if (stack.stackSize <= 0) invRobot.setInventorySlotContents(selected, null) else invRobot.setInventorySlotContents(selected, stack)
            arrayOf(stack2)
        } else {
            stack.stackSize = stack.stackSize - amount + notInjected.stackSize.toInt()
            if (stack.stackSize <= 0)
                invRobot.setInventorySlotContents(selected, null) else invRobot.setInventorySlotContents(selected, stack)
            stack2.stackSize -= notInjected.stackSize.toInt()
            arrayOf(stack2.stackSize)
        }
    }

    @Callback(doc = "function(database:address, entry:number[, number:amount]):number -- Get items from your ae system.")
    override fun requestItems(context: Context, args: Arguments): Array<Any> {
        val address: String = args.checkString(0)
        val entry: Int = args.checkInteger(1)
        val amount: Int = args.optInteger(2, 64)
        val selected: Int = agent.selectedSlot()
        val invRobot: IInventory = agent.mainInventory()
        if (invRobot.sizeInventory <= 0)
            return arrayOf(0)
        val inv: IMEMonitor<IAEItemStack> = itemInventory ?: return arrayOf(0)
        val n: Node = node().network().node(address) ?: throw IllegalArgumentException("no such component")
        if (n !is Component) throw IllegalArgumentException("no such component")
        val env: Environment = n.host()
        if (env !is Database) throw IllegalArgumentException("not a database")
        val database: Database = env
        val sel: ItemStack? = invRobot.getStackInSlot(selected)
        val inSlot: Int = sel?.stackSize ?: 0
        val maxSize: Int = sel?.maxStackSize ?: 64
        val stack: ItemStack = database.getStackInSlot(entry - 1) ?: return arrayOf(0)
        stack.stackSize = Math.min(amount, maxSize - inSlot)
        val stack2: ItemStack = stack.copy()
        stack2.stackSize = 1
        val sel2: ItemStack? = if (sel != null) {
            val sel3: ItemStack = sel.copy()
            sel3.stackSize = 1
            sel3
        } else null
        if (sel != null && !ItemStack.areItemStacksEqual(sel2, stack2)) return arrayOf(0)
        val extracted: IAEItemStack = inv.extractItems(AEApi.instance().storage().createItemStack(stack), Actionable.MODULATE, MachineSource(tile()))
                ?: return arrayOf(0)
        val ext: Int = extracted.stackSize.toInt()
        stack.stackSize = inSlot + ext
        invRobot.setInventorySlotContents(selected, stack)
        return arrayOf(ext)
    }

    @Callback(doc = "function([number:amount]):number -- Transfer selecte fluid to your ae system.")
    override fun sendFluids(context: Context, args: Arguments): Array<Any> {
        val selected: Int = agent.selectedTank()
        val tanks: MultiTank? = agent.tank()
        if (tanks == null || tanks.tankCount() <= 0)  return arrayOf(0)
        val tank: IFluidTank? = tanks.getFluidTank(selected)
        val inv: IMEMonitor<IAEFluidStack>? = fluidInventory
        if (tank == null || inv == null || tank.fluid == null)  return arrayOf(0)
        var amount: Int = args.optInteger(0, tank.capacity).coerceAtMost(tank.fluidAmount)
        val fluid: FluidStack = tank.fluid
        val fluid2: FluidStack = fluid.copy()
        fluid2.amount = amount
        val notInjectet: IAEFluidStack? = inv.injectItems(AEApi.instance().storage().createFluidStack(fluid2), Actionable.MODULATE, MachineSource(tile()))
        return if (notInjectet == null) {
            tank.drain(amount, true)
            arrayOf(amount)
        } else {
            tank.drain(amount - notInjectet.stackSize.toInt(), true)
            amount -= notInjectet.stackSize.toInt()
            arrayOf(amount)
        }
    }
    @Callback(doc = "function(database:address, entry:number[, number:amount]):number -- Get fluid from your ae system.")
    override fun requestFluids(context: Context, args: Arguments): Array<Any> {
        val address: String = args.checkString(0)
        val entry: Int = args.checkInteger(1)
        val amount: Int = args.optInteger(2, FluidContainerRegistry.BUCKET_VOLUME)
        val tanks: MultiTank? = agent.tank()
        val selected: Int = agent.selectedTank()
        if (tanks == null || tanks.tankCount() <= 0)
            return arrayOf(0)
        val tank: IFluidTank? = tanks.getFluidTank(selected)
        val inv: IMEMonitor<IAEFluidStack>? = fluidInventory
        if (tank == null || inv == null) return arrayOf(0)
        val n: Node = node().network().node(address) ?: throw IllegalArgumentException("no such component")
        if (n !is Component) throw IllegalArgumentException("no such component")
        val env: Environment? = n.host()
        if (env !is Database) throw IllegalArgumentException("not a database")
        val database: Database = env
        val fluid: FluidStack = FluidContainerRegistry.getFluidForFilledItem(database.getStackInSlot(entry - 1))
        fluid.amount = amount
        val fluid2: FluidStack = fluid.copy()
        fluid2.amount = tank.fill(fluid, false)
        if (fluid2.amount == 0) return arrayOf(0)
        val extracted: IAEFluidStack = inv.extractItems(AEApi.instance().storage().createFluidStack(fluid2), Actionable.MODULATE, MachineSource(tile()))
                ?: return arrayOf(0)
        return arrayOf(tank.fill(extracted.fluidStack, true))
    }

    @Callback(doc = "function():boolean -- Return true if the card is linket to your ae network.")
    override fun isLinked(context: Context, args: Arguments): Array<Any> = arrayOf(grid != null)

    override fun update() {
        super.update()
        if (host.world().totalWorldTime % 10 == 0L && isActive) {
            if (!(node() as Connector).tryChangeBuffer(-getEnergy())) {
                isActive = false
            }
        }
    }

    private fun getEnergy(): Double {
        val c: ItemStack? = component
        return if (c == null) .0 else {
            when (c.itemDamage) {
                0 -> {
                    .6
                }
                1 -> {
                    .3
                }
                else -> {
                    .05
                }
            }
        }
    }

    override fun onMessage(message: Message): Unit {
        super.onMessage(message)
        if (message.name() == "computer.stopped") {
            isActive = false
        } else if (message.name() == "computer.started") {
            isActive = true
        }
    }
}