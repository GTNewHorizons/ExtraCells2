package extracells.api

import extracells.api.definitions.IBlockDefinition
import extracells.api.definitions.IItemDefinition
import extracells.api.definitions.IPartDefinition
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.fluids.Fluid

interface ExtraCellsApi {
    fun addFluidToShowBlacklist(clazz: Class<out Fluid>?)
    fun addFluidToShowBlacklist(fluid: Fluid?)
    fun addFluidToStorageBlacklist(clazz: Class<out Fluid>?)
    fun addFluidToStorageBlacklist(fluid: Fluid?)
    fun blocks(): IBlockDefinition
    fun canFluidSeeInTerminal(fluid: Fluid?): Boolean
    fun canStoreFluid(fluid: Fluid?): Boolean

    @get:Deprecated("incorrect spelling", ReplaceWith("version","extracells.api"))
    val verion: String?
    val version: String?

    @Deprecated("")
    fun getWirelessFluidTermHandler(`is`: ItemStack?): IWirelessFluidTermHandler?
    fun getWirelessTermHandler(`is`: ItemStack?): IWirelessFluidTermHandler?
    fun isWirelessFluidTerminal(`is`: ItemStack?): Boolean
    fun items(): IItemDefinition

    @Deprecated("")
    fun openPortableCellGui(player: EntityPlayer?, stack: ItemStack?, world: World?): ItemStack?
    fun openPortableGasCellGui(player: EntityPlayer?, stack: ItemStack, world: World?): ItemStack
    fun openPortableFluidCellGui(player: EntityPlayer?, stack: ItemStack?, world: World?): ItemStack?

    @Deprecated("")
    fun openWirelessTerminal(player: EntityPlayer, stack: ItemStack?, world: World): ItemStack?
    fun openWirelessFluidTerminal(player: EntityPlayer, stack: ItemStack?, world: World): ItemStack?
    fun openWirelessGasTerminal(player: EntityPlayer, stack: ItemStack, world: World): ItemStack

    @Deprecated("")
    fun openWirelessTerminal(player: EntityPlayer?, stack: ItemStack?, world: World, x: Int, y: Int, z: Int, key: Long?): ItemStack?
    fun parts(): IPartDefinition
    fun registerWirelessTermHandler(handler: IWirelessFluidTermHandler)

    @Deprecated("")
    fun registerWirelessFluidTermHandler(handler: IWirelessFluidTermHandler)

    @Deprecated("incorrect spelling")
    fun registryWirelessFluidTermHandler(handler: IWirelessFluidTermHandler)
    fun registerFuelBurnTime(fuel: Fluid?, burnTime: Int)
    //	public boolean isGasStack(IAEFluidStack stack);
    //
    //	public boolean isGasStack(FluidStack stack);
    //
    //	public boolean isGas(Fluid fluid);
    //	/**
    //	 * Converts an IAEFluid stack to a GasStack
    //	 *
    //	 * @param fluidStack
    //	 * @return GasStack
    //     */
    //	public Object createGasStack(IAEFluidStack fluidStack);
    //
    //	/**
    //	 * Create the fluidstack from the specific gas
    //	 *
    //	 * @param gasStack
    //	 * @return FluidStack
    //     */
    //	public IAEFluidStack createFluidStackFromGas(Object gasStack);
    //
    //	/**
    //	 * Create the ec fluid from the specific gas
    //	 *
    //	 * @param gas
    //	 * @return Fluid
    //     */
    //	public Fluid getGasFluid(Object gas);
    /**
     * A registry for StorageBus interactions
     *
     * @param esh storage handler
     */
    fun addExternalStorageInterface(esh: IExternalGasStorageHandler?) //	/**
    //	 * @param te       tile entity
    //	 * @param opposite direction
    //	 * @param mySrc    source
    //	 *
    //	 * @return the handler for a given tile / forge direction
    //	 */
    //	IExternalGasStorageHandler getHandler(TileEntity te, ForgeDirection opposite, BaseActionSource mySrc );
}