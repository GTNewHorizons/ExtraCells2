package extracells.integration.waila

import extracells.tileentity.TileEntityCertusTank
import mcp.mobius.waila.api.IWailaConfigHandler
import mcp.mobius.waila.api.IWailaDataAccessor
import mcp.mobius.waila.api.IWailaDataProvider
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.StatCollector
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidContainerRegistry
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
open class TileCertusTankWailaDataProvider : IWailaDataProvider {
    override fun getNBTData(player: EntityPlayerMP, tile: TileEntity,
                            tag: NBTTagCompound, world: World, x: Int, y: Int, z: Int): NBTTagCompound {
        if (tile is TileEntityCertusTank) {
            if (tile.tank.fluid == null) tag.setInteger("fluidID", -1) else {
                tag.setInteger("fluidID",
                        tile.tank.fluid.fluidID)
                tag.setInteger("currentFluid",
                        tile.tank.fluidAmount)
            }
            tag.setInteger("maxFluid",
                    tile.tank.capacity)
        }
        return tag
    }

    override fun getWailaBody(itemStack: ItemStack, list: MutableList<String>,
                              accessor: IWailaDataAccessor, config: IWailaConfigHandler): List<String> {
        val tag = accessor.nbtData ?: return list
        if (tag.hasKey("fluidID")) {
            val fluidID = tag.getInteger("fluidID")
            if (fluidID == -1) {
                list.add(StatCollector
                        .translateToLocal("extracells.tooltip.fluid")
                        + ": "
                        + StatCollector
                        .translateToLocal("extracells.tooltip.empty1"))
                list.add(StatCollector
                        .translateToLocal("extracells.tooltip.amount")
                        + ": 0mB / " + tag.getInteger("maxFluid") + "mB")
                return list
            } else {
                val fluid = FluidRegistry.getFluid(tag.getInteger("fluidID"))
                list.add(StatCollector
                        .translateToLocal("extracells.tooltip.fluid")
                        + ": "
                        + fluid.getLocalizedName(FluidStack(fluid,
                        FluidContainerRegistry.BUCKET_VOLUME)))
            }
        } else return list
        if (tag.hasKey("maxFluid") && tag.hasKey("currentFluid")) list.add(StatCollector
                .translateToLocal("extracells.tooltip.amount")
                + ": "
                + tag.getInteger("currentFluid")
                + "mB / "
                + tag.getInteger("maxFluid") + "mB")
        return list
    }

    override fun getWailaHead(itemStack: ItemStack,
                              currenttip: List<String>, accessor: IWailaDataAccessor,
                              config: IWailaConfigHandler): List<String> {
        return currenttip
    }

    override fun getWailaStack(accessor: IWailaDataAccessor,
                               config: IWailaConfigHandler): ItemStack {
        return accessor.stack
    }

    override fun getWailaTail(itemStack: ItemStack,
                              currenttip: List<String>, accessor: IWailaDataAccessor,
                              config: IWailaConfigHandler): List<String> {
        return currenttip
    }
}