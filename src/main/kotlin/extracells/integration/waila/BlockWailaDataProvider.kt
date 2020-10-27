package extracells.integration.waila

import mcp.mobius.waila.api.IWailaConfigHandler
import mcp.mobius.waila.api.IWailaDataAccessor
import mcp.mobius.waila.api.IWailaDataProvider
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
open class BlockWailaDataProvider : IWailaDataProvider {
    override fun getNBTData(player: EntityPlayerMP?, te: TileEntity?,
                            tag: NBTTagCompound?, world: World?, x: Int, y: Int, z: Int): NBTTagCompound? {
        if (te != null && te is IWailaTile) {
            tag?.setTag("WailaTile",
                    (te as IWailaTile).getWailaTag(NBTTagCompound()))
        }
        return tag
    }

    override fun getWailaBody(itemStack: ItemStack,
                              currenttip: MutableList<String>, accessor: IWailaDataAccessor,
                              config: IWailaConfigHandler): List<String> {
        val tile = accessor.tileEntity
        val tag = accessor.nbtData
        if (tile != null && tile is IWailaTile && tag != null && tag.hasKey("WailaTile")) {
            val t = tile as IWailaTile
            return t.getWailaBody(currenttip, tag.getCompoundTag("WailaTile"),
                    accessor.side)
        }
        return currenttip
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