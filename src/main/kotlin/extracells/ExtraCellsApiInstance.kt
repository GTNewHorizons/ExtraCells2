package extracells

import appeng.api.AEApi
import appeng.api.implementations.tiles.IWirelessAccessPoint
import appeng.api.networking.IGridHost
import appeng.api.networking.storage.IStorageGrid
import appeng.api.storage.IMEInventoryHandler
import appeng.api.storage.IMEMonitor
import appeng.api.storage.MEMonitorHandler
import appeng.api.storage.StorageChannel
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IAEStack
import extracells.Extracells.VERSION
import extracells.api.*
import extracells.api.definitions.IBlockDefinition
import extracells.api.definitions.IItemDefinition
import extracells.api.definitions.IPartDefinition
import extracells.definitions.BlockDefinition
import extracells.definitions.ItemDefinition
import extracells.definitions.PartDefinition
import extracells.integration.Integration
import extracells.inventory.HandlerItemStorageFluid
import extracells.network.GuiHandler.getGuiId
import extracells.network.GuiHandler.launchGui
import extracells.util.FluidCellHandler
import extracells.util.FuelBurnTime.registerFuel
import extracells.wireless.WirelessTermRegistry
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid
import java.util.*

class ExtraCellsApiInstance : ExtraCellsApi {
    private val blacklistShowClass: MutableList<Class<out Fluid>> = ArrayList()
    private val blacklistShowFluid: MutableList<Fluid> = ArrayList()
    private val blacklistStorageClass: MutableList<Class<out Fluid>> = ArrayList()
    private val blacklistStorageFluid: MutableList<Fluid> = ArrayList()
    override fun addFluidToShowBlacklist(clazz: Class<out Fluid>?) {
        if (clazz == null || clazz == Fluid::class.java) return
        blacklistShowClass.add(clazz)
    }

    override fun addFluidToShowBlacklist(fluid: Fluid?) {
        if (fluid == null) return
        blacklistShowFluid.add(fluid)
    }

    override fun addFluidToStorageBlacklist(clazz: Class<out Fluid>?) {
        if (clazz == null || clazz == Fluid::class.java) return
        blacklistStorageClass.add(clazz)
    }

    override fun addFluidToStorageBlacklist(fluid: Fluid?) {
        if (fluid == null) return
        blacklistStorageFluid.add(fluid)
    }

    override fun blocks(): IBlockDefinition {
        return BlockDefinition.Companion.instance
    }

    override fun canFluidSeeInTerminal(fluid: Fluid?): Boolean {
        if (fluid == null) return false
        if (blacklistShowFluid.contains(fluid)) return false
        for (clazz in blacklistShowClass) {
            if (clazz.isInstance(fluid)) return false
        }
        return true
    }

    override fun canStoreFluid(fluid: Fluid?): Boolean {
        if (fluid == null) return false
        if (blacklistStorageFluid.contains(fluid)) return false
        for (clazz in blacklistStorageClass) {
            if (clazz.isInstance(fluid)) return false
        }
        return true
    }

    @get:Deprecated("Incorrect spelling")
    override val verion: String?
        get() = VERSION
    override val version: String?
        get() = VERSION

    override fun getWirelessFluidTermHandler(`is`: ItemStack?): IWirelessFluidTermHandler? {
        return getWirelessTermHandler(`is`) as IWirelessFluidTermHandler?
    }

    override fun getWirelessTermHandler(`is`: ItemStack?): IWirelessGasFluidTermHandler? {
        return WirelessTermRegistry.getWirelessTermHandler(`is`)
    }

    override fun isWirelessFluidTerminal(`is`: ItemStack?): Boolean {
        return WirelessTermRegistry.isWirelessItem(`is`)
    }

    override fun items(): IItemDefinition {
        return ItemDefinition.Companion.instance
    }

    override fun openPortableCellGui(player: EntityPlayer, stack: ItemStack?, world: World): ItemStack? {
        return openPortableFluidCellGui(player, stack, world)
    }

    override fun openPortableFluidCellGui(player: EntityPlayer, stack: ItemStack?, world: World): ItemStack? {
        if (world.isRemote || stack == null || stack.item == null) return stack
        val item = stack.item
        if (item !is IPortableFluidStorageCell) return stack
        val cellHandler = AEApi.instance().registries().cell().getHandler(stack) as? FluidCellHandler ?: return stack
        val handler: IMEInventoryHandler<out IAEStack<*>> = cellHandler.getCellInventoryPlayer(stack,
                player) as? HandlerItemStorageFluid
                ?: return stack
        val fluidInventory: IMEMonitor<IAEFluidStack> = MEMonitorHandler<IAEFluidStack>(handler, StorageChannel.FLUIDS)
        launchGui(getGuiId(3), player, arrayOf(fluidInventory, item))
        return stack
    }

    override fun openPortableGasCellGui(player: EntityPlayer?, stack: ItemStack, world: World?): ItemStack {
//		if (world.isRemote || stack == null || stack.getItem() == null)
        return stack
        //		Item item = stack.getItem();
//		if (!(item instanceof IPortableGasStorageCell))
//			return stack;
//		ICellHandler cellHandler = AEApi.instance().registries().cell().getHandler(stack);
//		if (!(cellHandler instanceof GasCellHandler))
//			return stack;
//		IMEInventoryHandler<IAEFluidStack> handler = ((GasCellHandler) cellHandler).getCellInventoryPlayer(stack, player);
//		if (!(handler instanceof HandlerItemStorageGas)) {
//			return stack;
//		}
//		IMEMonitor<IAEFluidStack> fluidInventory = new MEMonitorHandler<IAEFluidStack>(handler, StorageChannel.FLUIDS);
//		GuiHandler.launchGui(GuiHandler.getGuiId(6), player, new Object[]{fluidInventory, item});
//		return stack;
    }

    override fun openWirelessTerminal(player: EntityPlayer, stack: ItemStack?, world: World): ItemStack? {
        return openWirelessFluidTerminal(player, stack, world)
    }

    override fun openWirelessFluidTerminal(player: EntityPlayer, stack: ItemStack?, world: World): ItemStack? {
        if (world.isRemote) return stack
        if (!isWirelessFluidTerminal(stack)) return stack
        val handler = getWirelessTermHandler(stack)
        if (!handler!!.hasPower(player, 1.0, stack)) return stack
        val key: Long
        key = try {
            handler.getEncryptionKey(stack).toLong()
        } catch (ignored: Throwable) {
            return stack
        }
        return openWirelessTerminal(player, stack, world, player.posX.toInt(), player.posY.toInt(), player.posZ.toInt(),
                key)
    }

    override fun openWirelessGasTerminal(player: EntityPlayer, itemStack: ItemStack, world: World): ItemStack {
        if (world.isRemote) return itemStack
        if (!isWirelessFluidTerminal(itemStack)) return itemStack
        val handler = getWirelessTermHandler(itemStack)
        if (!handler!!.hasPower(player, 1.0, itemStack)) return itemStack
        val key: Long
        key = try {
            handler.getEncryptionKey(itemStack).toLong()
        } catch (ignored: Throwable) {
            return itemStack
        }
        val x = player.posX.toInt()
        val y = player.posY.toInt()
        val z = player.posZ.toInt()
        val securityTerminal = AEApi.instance().registries().locatable().getLocatableBy(key) as IGridHost
                ?: return itemStack
        val gridNode = securityTerminal.getGridNode(ForgeDirection.UNKNOWN) ?: return itemStack
        val grid = gridNode.grid ?: return itemStack
        for (node in grid.getMachines(
                AEApi.instance().definitions().blocks().wireless().maybeEntity().get() as Class<out IGridHost?>)) {
            val accessPoint = node.machine as IWirelessAccessPoint
            val distance = accessPoint.location.subtract(x, y, z)
            val squaredDistance = distance.x * distance.x + distance.y * distance.y + distance.z * distance.z
            if (squaredDistance <= accessPoint.range * accessPoint.range) {
                val gridCache = grid.getCache<IStorageGrid>(IStorageGrid::class.java)
                if (gridCache != null) {
                    val fluidInventory = gridCache.fluidInventory
                    if (fluidInventory != null) {
                        launchGui(getGuiId(5), player, arrayOf<Any?>(
                                fluidInventory, getWirelessTermHandler(itemStack)))
                    }
                }
            }
        }
        return itemStack
    }

    @Deprecated("")
    override fun openWirelessTerminal(player: EntityPlayer?, itemStack: ItemStack?, world: World, x: Int, y: Int, z: Int, key: Long?): ItemStack? {
        if (world.isRemote) return itemStack
        val securityTerminal = AEApi.instance().registries().locatable().getLocatableBy(key!!) as IGridHost
                ?: return itemStack
        val gridNode = securityTerminal
                .getGridNode(ForgeDirection.UNKNOWN) ?: return itemStack
        val grid = gridNode.grid ?: return itemStack
        for (node in grid.getMachines(
                AEApi.instance().definitions().blocks().wireless().maybeEntity().get() as Class<out IGridHost?>)) {
            val accessPoint = node
                    .machine as IWirelessAccessPoint
            val distance = accessPoint.location.subtract(x, y, z)
            val squaredDistance = distance.x * distance.x + distance.y * distance.y + distance.z * distance.z
            if (squaredDistance <= accessPoint.range * accessPoint.range) {
                val gridCache = grid.getCache<IStorageGrid>(IStorageGrid::class.java)
                if (gridCache != null) {
                    val fluidInventory = gridCache.fluidInventory
                    if (fluidInventory != null) {
                        launchGui(getGuiId(1), player, arrayOf<Any?>(
                                fluidInventory, getWirelessFluidTermHandler(itemStack)))
                    }
                }
            }
        }
        return itemStack
    }

    override fun parts(): IPartDefinition {
        return PartDefinition.Companion.instance
    }

    override fun registerWirelessTermHandler(handler: IWirelessGasFluidTermHandler) {
        WirelessTermRegistry.registerWirelessTermHandler(handler)
    }

    override fun registerWirelessFluidTermHandler(handler: IWirelessFluidTermHandler) {
        registerWirelessTermHandler(handler)
    }

    @Deprecated("Incorrect spelling")
    override fun registryWirelessFluidTermHandler(handler: IWirelessFluidTermHandler) {
        registerWirelessFluidTermHandler(handler)
    }

    override fun registerFuelBurnTime(fuel: Fluid?, burnTime: Int) {
        registerFuel(fuel!!, burnTime)
    }

    //	@Override
    //	public boolean isGasStack(IAEFluidStack stack) {
    //		return stack != null && isGasStack(stack.getFluidStack());
    //	}
    //
    //	@Override
    //	public boolean isGasStack(FluidStack stack) {
    //		return stack != null && isGas(stack.getFluid());
    //	}
    //
    //	@Override
    //	public boolean isGas(Fluid fluid) {
    //		return fluid != null && Integration.Mods.MEKANISMGAS.isEnabled() && checkGas(fluid);
    //	}
    //
    //	@Override
    //	public Object createGasStack(IAEFluidStack stack) {
    //		return Integration.Mods.MEKANISMGAS.isEnabled() ? createGasFromFluidStack(stack) : null;
    //	}
    //
    //	@Override
    //	public IAEFluidStack createFluidStackFromGas(Object gasStack) {
    //		return isMekEnabled() ? createFluidStackFromGasStack(gasStack): null;
    //	}
    //
    //	@Override
    //	public Fluid getGasFluid(Object gas) {
    //		return isMekEnabled() ? createFluidFromGas(gas) : null;
    //	}
    //
    override fun addExternalStorageInterface(esh: IExternalGasStorageHandler?) {
        //if(isMekEnabled())
        //	GasStorageRegistry.addExternalStorageInterface(esh);
    }

    //	@Override
    //	public IExternalGasStorageHandler getHandler(TileEntity te, ForgeDirection opposite, BaseActionSource mySrc) {
    //		return null;//return isMekEnabled() ? GasStorageRegistry.getHandler(te, opposite, mySrc) : null;
    //	}
    //	@Optional.Method(modid = "MekanismAPI|gas")
    //	private IAEFluidStack createFluidStackFromGasStack(Object gasStack){
    //		return gasStack instanceof GasStack ? GasUtil.createAEFluidStack((GasStack) gasStack) : null;
    //	}
    //	@Optional.Method(modid = "MekanismAPI|gas")
    //	private Fluid createFluidFromGas(Object gas){
    //		return null;//gas instanceof Gas ? MekanismGas.getFluidGasMap().containsKey(gas) ? MekanismGas.getFluidGasMap().get(gas) : null : null;
    //	}
    //
    //	@Optional.Method(modid = "MekanismAPI|gas")
    //	private Object createGasFromFluidStack(IAEFluidStack stack) {
    //		return stack == null ? null : GasUtil.getGasStack(stack.getFluidStack());
    //	}
    //
    //	@Optional.Method(modid = "MekanismAPI|gas")
    //	private boolean checkGas(Fluid fluid) {
    //		return false;//fluid instanceof MekanismGas.GasFluid;
    //	}
    val isMekEnabled: Boolean
        private get() = Integration.Mods.MEKANISMGAS.isEnabled

    companion object {
        val instance: ExtraCellsApi = ExtraCellsApiInstance()
    }
}