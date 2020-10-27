package extracells.integration.opencomputers

import appeng.api.parts.IPartHost
import extracells.part.PartFluidImport
import extracells.registries.ItemEnum
import extracells.registries.PartEnum
import extracells.util.FluidUtil
import li.cil.oc.api.Network
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.NamedBlock
import li.cil.oc.api.driver.SidedBlock
import li.cil.oc.api.internal.Database
import li.cil.oc.api.machine.Arguments
import li.cil.oc.api.machine.Callback
import li.cil.oc.api.machine.Context
import li.cil.oc.api.network.Environment
import li.cil.oc.api.network.Visibility
import li.cil.oc.api.prefab.ManagedEnvironment
import li.cil.oc.server.network.Component
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.FluidStack
open class DriverFluidImportBus : SidedBlock {
    override fun worksWith(world: World, x: Int, y: Int, z: Int, side: ForgeDirection): Boolean {
        return getImportBus(world, x, y, z, side) != null
    }

    override fun createEnvironment(world: World, x: Int, y: Int, z: Int, side: ForgeDirection): ManagedEnvironment? {
        val tile = world.getTileEntity(x, y, z)
        return if (tile == null || tile !is IPartHost) null else Enviroment(tile as IPartHost)
    }

    inner class Enviroment internal constructor(host: IPartHost) : ManagedEnvironment(), NamedBlock {
        protected val tile: TileEntity
        protected val host: IPartHost
        @Callback(
                doc = "function(side:number, [ slot:number]):table -- Get the configuration of the fluid import bus pointing in the specified direction.")
        fun getFluidImportConfiguration(context: Context?, args: Arguments): Array<Any?> {
            val dir = ForgeDirection.getOrientation(args.checkInteger(0))
            if (dir == null || dir == ForgeDirection.UNKNOWN) return arrayOf(null, "unknown side")
            val part = getImportBus(tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord, dir)
                    ?: return arrayOf(null, "no export bus")
            val slot = args.optInteger(1, 4)
            return try {
                val fluid = part.filterFluids[slot] ?: return arrayOf(null)
                arrayOf(FluidStack(fluid, 1000))
            } catch (e: Throwable) {
                arrayOf(null, "Invalid slot")
            }
        }

        @Callback(
                doc = "function(side:number[, slot:number][, database:address, entry:number]):boolean -- Configure the fluid import bus pointing in the specified direction to export fluid stacks matching the specified descriptor.")
        fun setFluidImportConfiguration(context: Context, args: Arguments): Array<Any?> {
            val dir = ForgeDirection.getOrientation(args.checkInteger(0))
            if (dir == null || dir == ForgeDirection.UNKNOWN) return arrayOf(null, "unknown side")
            val part = getImportBus(tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord, dir)
                    ?: return arrayOf(null, "no export bus")
            val slot: Int
            val address: String
            val entry: Int
            if (args.count() == 3) {
                address = args.checkString(1)
                entry = args.checkInteger(2)
                slot = 4
            } else if (args.count() < 3) {
                slot = args.optInteger(1, 4)
                return try {
                    part.filterFluids[slot] = null
                    part.onInventoryChanged()
                    context.pause(0.5)
                    arrayOf(true)
                } catch (e: Throwable) {
                    arrayOf(false, "invalid slot")
                }
            } else {
                slot = args.optInteger(1, 4)
                address = args.checkString(2)
                entry = args.checkInteger(3)
            }
            val node = node().network().node(address) ?: throw IllegalArgumentException("no such component")
            require(node is Component) { "no such component" }
            val env = node.host()
            require(env is Database) { "not a database" }
            val database = env as Database
            return try {
                val data = database.getStackInSlot(entry - 1)
                if (data == null) part.filterFluids[slot] = null else {
                    val fluid = FluidUtil.getFluidFromContainer(data)
                    if (fluid == null || fluid.getFluid() == null) return arrayOf(false, "not a fluid container")
                    part.filterFluids[slot] = fluid.getFluid()
                }
                part.onInventoryChanged()
                context.pause(0.5)
                arrayOf(true)
            } catch (e: Throwable) {
                arrayOf(false, "invalid slot")
            }
        }

        /*@Callback(doc = "function(side:number, amount:number):boolean -- Make the fluid export bus facing the specified direction perform a single export operation.")
		public Object[] exportFluid(Context context, Arguments args){
			ForgeDirection dir = ForgeDirection.getOrientation(args.checkInteger(0));
			if (dir == null || dir == ForgeDirection.UNKNOWN)
				return new Object[]{false, "unknown side"};
			PartFluidImport part = getImportBus(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, dir);
			if (part == null)
				return new Object[]{false, "no export bus"};
			if (part.getFacingTank() == null)
				return new Object[]{false, "no tank"};
			int amount = Math.min(args.optInteger(1, 625), 125 + part.getSpeedState() * 125);
			boolean didSomething = part.doWork(amount, 1);
			if (didSomething)
				context.pause(0.25);
			return new Object[]{didSomething};
		}*/
        override fun preferredName(): String {
            return "me_importbus"
        }

        override fun priority(): Int {
            return 1
        }

        init {
            tile = host as TileEntity
            this.host = host
            setNode(Network.newNode(this, Visibility.Network).withComponent("me_importbus").create())
        }
    }

    internal class Provider : EnvironmentProvider {
        override fun getEnvironment(stack: ItemStack?): Class<out Environment>? {
            if (stack == null) return null
            return if (stack.item === ItemEnum.PARTITEM.item && stack.itemDamage == PartEnum.FLUIDEXPORT.ordinal) Enviroment::class.java else null
        }
    }

    companion object {
        private fun getImportBus(world: World, x: Int, y: Int, z: Int, dir: ForgeDirection?): PartFluidImport? {
            val tile = world.getTileEntity(x, y, z)
            if (tile !is IPartHost) return null
            val host = tile as IPartHost
            if (dir == null || dir == ForgeDirection.UNKNOWN) {
                for (side in ForgeDirection.VALID_DIRECTIONS) {
                    val part = host.getPart(side)
                    if (part is PartFluidImport /*&&!(part instanceof PartGasImport)*/) return part
                }
            } else {
                val part = host.getPart(dir)
                if (part is PartFluidImport /*&&!(part instanceof PartGasImport)*/) return part
            }
            return null
        }
    }
}