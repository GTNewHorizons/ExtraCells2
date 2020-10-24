package extracells.integration.opencomputers

import appeng.api.parts.IPartHost
import extracells.api.IFluidInterface
import extracells.part.PartFluidInterface
import extracells.registries.BlockEnum
import extracells.registries.ItemEnum
import extracells.registries.PartEnum
import extracells.tileentity.TileEntityFluidInterface
import extracells.util.FluidUtil
import li.cil.oc.api.Network
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.NamedBlock
import li.cil.oc.api.driver.SidedBlock
import li.cil.oc.api.internal.Database
import li.cil.oc.api.machine.Arguments
import li.cil.oc.api.machine.Callback
import li.cil.oc.api.machine.Context
import li.cil.oc.api.network.Visibility
import li.cil.oc.api.prefab.ManagedEnvironment
import li.cil.oc.server.network.Component
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.FluidStack

class DriverFluidInterface : SidedBlock {
    override fun worksWith(world: World, x: Int, y: Int, z: Int, side: ForgeDirection): Boolean {
        val tile = world.getTileEntity(x, y, z)
        return tile != null && (getFluidInterface(world, x, y, z, side) != null || tile is IFluidInterface)
    }

    override fun createEnvironment(world: World, x: Int, y: Int, z: Int, side: ForgeDirection): ManagedEnvironment {
        val tile = world.getTileEntity(x, y, z)
        return if (tile == null || !(tile is IPartHost || tile is IFluidInterface)) null else Environment(tile)
    }

    inner class Environment internal constructor(protected val tile: TileEntity) : ManagedEnvironment(), NamedBlock {
        protected val host: IPartHost?
        @Callback(
                doc = "function(side:number):table -- Get the configuration of the fluid interface on the specified direction.")
        fun getFluidInterfaceConfiguration(context: Context?, args: Arguments): Array<Any?> {
            val dir = ForgeDirection.getOrientation(args.checkInteger(0))
            if (dir == null || dir == ForgeDirection.UNKNOWN) return arrayOf(null, "unknown side")
            if (tile is TileEntityFluidInterface) {
                val fluid = tile.getFilter(dir) ?: return arrayOf(null)
                return arrayOf(FluidStack(fluid, 1000))
            }
            val part = getFluidInterface(
                    tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord, dir)
                    ?: return arrayOf(null, "no interface")
            val fluid = part.getFilter(dir) ?: return arrayOf(null)
            return arrayOf(FluidStack(fluid, 1000))
        }

        @Callback(
                doc = "function(side:number[, database:address, entry:number]):boolean -- Configure the filter in fluid interface on the specified direction.")
        fun setFluidInterfaceConfiguration(context: Context, args: Arguments): Array<Any?> {
            val dir = ForgeDirection.getOrientation(args.checkInteger(0))
            if (dir == null || dir == ForgeDirection.UNKNOWN) return arrayOf(null, "unknown side")
            val part = (if (tile is IFluidInterface) tile else getFluidInterface(
                    tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord, dir))
                    ?: return arrayOf(null, "no export bus")
            val address: String
            val entry: Int
            if (args.count() == 3) {
                address = args.checkString(1)
                entry = args.checkInteger(2)
            } else {
                part.setFilter(dir, null)
                context.pause(0.5)
                return arrayOf(true)
            }
            val node = node().network().node(address) ?: throw IllegalArgumentException("no such component")
            require(node is Component) { "no such component" }
            val component = node
            val env = node.host()
            require(env is Database) { "not a database" }
            val database = env as Database
            return try {
                val data = database.getStackInSlot(entry - 1)
                if (data == null) part.setFilter(dir, null) else {
                    val fluid = FluidUtil.getFluidFromContainer(data)
                    if (fluid == null || fluid.getFluid() == null) return arrayOf(false, "not a fluid container")
                    part.setFilter(dir, fluid.getFluid())
                }
                context.pause(0.5)
                arrayOf(true)
            } catch (e: Throwable) {
                arrayOf(false, "invalid slot")
            }
        }

        override fun preferredName(): String {
            return "me_interface"
        }

        override fun priority(): Int {
            return 0
        }

        init {
            host = if (tile is IPartHost) tile else null
            setNode(Network.newNode(this, Visibility.Network).withComponent("me_interface").create())
        }
    }

    internal class Provider : EnvironmentProvider {
        override fun getEnvironment(stack: ItemStack): Class<out li.cil.oc.api.network.Environment> {
            if (stack == null) return null
            if (stack.item === ItemEnum.PARTITEM.item && stack.itemDamage == PartEnum.INTERFACE.ordinal) return Environment::class.java
            return if (stack.item === Item.getItemFromBlock(
                            BlockEnum.ECBASEBLOCK.block) && stack.itemDamage == 0) Environment::class.java else null
        }
    }

    companion object {
        private fun getFluidInterface(world: World, x: Int, y: Int, z: Int, dir: ForgeDirection?): PartFluidInterface? {
            val tile = world.getTileEntity(x, y, z)
            if (tile == null || tile !is IPartHost) return null
            val host = tile as IPartHost
            return if (dir == null || dir == ForgeDirection.UNKNOWN) {
                for (side in ForgeDirection.VALID_DIRECTIONS) {
                    val part = host.getPart(side)
                    if (part != null && part is PartFluidInterface) return part
                }
                null
            } else {
                val part = host.getPart(dir)
                if (part is PartFluidInterface) part else null
            }
        }
    }
}