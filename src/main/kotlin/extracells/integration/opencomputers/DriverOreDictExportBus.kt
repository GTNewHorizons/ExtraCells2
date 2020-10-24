package extracells.integration.opencomputers

import appeng.api.parts.IPartHost
import extracells.part.PartOreDictExporter
import extracells.registries.ItemEnum
import extracells.registries.PartEnum
import li.cil.oc.api.Network
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.NamedBlock
import li.cil.oc.api.driver.SidedBlock
import li.cil.oc.api.machine.Arguments
import li.cil.oc.api.machine.Callback
import li.cil.oc.api.machine.Context
import li.cil.oc.api.network.Visibility
import li.cil.oc.api.prefab.ManagedEnvironment
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

class DriverOreDictExportBus : SidedBlock {
    override fun worksWith(world: World, x: Int, y: Int, z: Int, side: ForgeDirection): Boolean {
        return getExportBus(world, x, y, z, side) != null
    }

    override fun createEnvironment(world: World, x: Int, y: Int, z: Int, side: ForgeDirection): ManagedEnvironment {
        val tile = world.getTileEntity(x, y, z)
        return if (tile !is IPartHost) null else Environment(
                tile as IPartHost)
    }

    class Environment internal constructor(host: IPartHost) : ManagedEnvironment(), NamedBlock {
        protected val tile: TileEntity
        protected val host: IPartHost
        @Callback(
                doc = "function(side:number):string -- Get the configuration of the ore dict export bus pointing in the specified direction.")
        fun getOreConfiguration(context: Context?, args: Arguments): Array<Any?> {
            val dir = ForgeDirection.getOrientation(args.checkInteger(0))
            if (dir == null || dir == ForgeDirection.UNKNOWN) return arrayOf(null, "unknown side")
            val part = getExportBus(tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord, dir)
                    ?: return arrayOf(null, "no export bus")
            return arrayOf(part.filter)
        }

        @Callback(
                doc = "function(side:number[, filter:string]):boolean -- Set the configuration of the ore dict export bus pointing in the specified direction.")
        fun setOreConfiguration(context: Context, args: Arguments): Array<Any> {
            val dir = ForgeDirection.getOrientation(args.checkInteger(0))
            if (dir == null || dir == ForgeDirection.UNKNOWN) return arrayOf(false, "unknown side")
            val part = getExportBus(tile.worldObj, tile.xCoord, tile.yCoord, tile.zCoord, dir)
                    ?: return arrayOf(false, "no export bus")
            part.filter = args.optString(1, "")
            context.pause(0.5)
            return arrayOf(true)
        }

        override fun preferredName(): String {
            return "me_exportbus"
        }

        override fun priority(): Int {
            return 0
        }

        init {
            tile = host as TileEntity
            this.host = host
            setNode(Network.newNode(this, Visibility.Network).withComponent("me_exportbus").create())
        }
    }

    internal class Provider : EnvironmentProvider {
        override fun getEnvironment(stack: ItemStack): Class<out li.cil.oc.api.network.Environment> {
            return if (stack.item === ItemEnum.PARTITEM.item && stack.itemDamage == PartEnum.OREDICTEXPORTBUS.ordinal) Environment::class.java else null
        }
    }

    companion object {
        private fun getExportBus(world: World, x: Int, y: Int, z: Int, dir: ForgeDirection?): PartOreDictExporter? {
            val tile = world.getTileEntity(x, y, z)
            if (tile !is IPartHost) return null
            val host = tile as IPartHost
            return if (dir == null || dir == ForgeDirection.UNKNOWN) {
                for (side in ForgeDirection.VALID_DIRECTIONS) {
                    val part = host.getPart(side)
                    if (part is PartOreDictExporter) return part
                }
                null
            } else {
                val part = host.getPart(dir)
                if (part is PartOreDictExporter) part else null
            }
        }
    }
}