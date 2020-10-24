package extracells.api;

import extracells.api.definitions.IBlockDefinition;
import extracells.api.definitions.IItemDefinition;
import extracells.api.definitions.IPartDefinition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public interface ExtraCellsApi {

	void addFluidToShowBlacklist(Class<? extends Fluid> clazz);

	void addFluidToShowBlacklist(Fluid fluid);

	void addFluidToStorageBlacklist(Class<? extends Fluid> clazz);

	void addFluidToStorageBlacklist(Fluid fluid);

	IBlockDefinition blocks();

	boolean canFluidSeeInTerminal(Fluid fluid);

	boolean canStoreFluid(Fluid fluid);

	/**
	 * @deprecated incorrect spelling
	 */
	@Deprecated
	String getVerion();

	String getVersion();

	@Deprecated
	IWirelessFluidTermHandler getWirelessFluidTermHandler(ItemStack is);

	IWirelessGasFluidTermHandler getWirelessTermHandler(ItemStack is);

	boolean isWirelessFluidTerminal(ItemStack is);

	IItemDefinition items();

	@Deprecated
	ItemStack openPortableCellGui(EntityPlayer player, ItemStack stack, World world);

	ItemStack openPortableGasCellGui(EntityPlayer player, ItemStack stack, World world);

	ItemStack openPortableFluidCellGui(EntityPlayer player, ItemStack stack, World world);

	@Deprecated
	ItemStack openWirelessTerminal(EntityPlayer player, ItemStack stack, World world);

	ItemStack openWirelessFluidTerminal(EntityPlayer player, ItemStack stack, World world);

	ItemStack openWirelessGasTerminal(EntityPlayer player, ItemStack stack, World world);

	@Deprecated
	ItemStack openWirelessTerminal(EntityPlayer player, ItemStack stack, World world, int x, int y, int z, Long key);

	IPartDefinition parts();

	void registerWirelessTermHandler(IWirelessGasFluidTermHandler handler);

	@Deprecated
	void registerWirelessFluidTermHandler(IWirelessFluidTermHandler handler);

	/**
	 * @deprecated incorrect spelling
	 */
	@Deprecated
	void registryWirelessFluidTermHandler(IWirelessFluidTermHandler handler);

	void registerFuelBurnTime(Fluid fuel, int burnTime);

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
	void addExternalStorageInterface( IExternalGasStorageHandler esh );

//	/**
//	 * @param te       tile entity
//	 * @param opposite direction
//	 * @param mySrc    source
//	 *
//	 * @return the handler for a given tile / forge direction
//	 */
//	IExternalGasStorageHandler getHandler(TileEntity te, ForgeDirection opposite, BaseActionSource mySrc );
}
