package extracells.integration.waila

import appeng.api.parts.IPart
import appeng.api.parts.IPartHost
import extracells.part.PartECBase
import mcp.mobius.waila.api.IWailaConfigHandler
import mcp.mobius.waila.api.IWailaDataAccessor
import mcp.mobius.waila.api.IWailaDataProvider
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraft.world.World
open class PartWailaDataProvider : IWailaDataProvider {
    override fun getNBTData(player: EntityPlayerMP, te: TileEntity,
                            tag: NBTTagCompound, world: World, x: Int, y: Int, z: Int): NBTTagCompound {
        val mop = retraceBlock(world, player, x, y, z)
        if (mop != null) {
            val part = getPart(te, mop)
            if (part != null && part is PartECBase) {
                tag.setTag("partEC",
                        part.getWailaTag(NBTTagCompound()))
            }
        }
        return tag
    }

    private fun getPart(tile: TileEntity, pos: MovingObjectPosition): IPart? {
        if (tile is IPartHost) {
            val position = pos.hitVec.addVector(-pos.blockX.toDouble(), -pos.blockY.toDouble(), -pos.blockZ.toDouble())
            val host = tile as IPartHost
            val sp = host.selectPart(position)
            if (sp.part != null) {
                return sp.part
            }
        }
        return null
    }

    override fun getWailaBody(itemStack: ItemStack,
                              currenttip: MutableList<String>, accessor: IWailaDataAccessor,
                              config: IWailaConfigHandler): List<String> {
        val tile = accessor.tileEntity
        val part = getPart(tile, accessor.position)
        if (part != null && part is PartECBase) {
            val tag: NBTTagCompound = if (accessor.nbtData != null
                    && accessor.nbtData.hasKey("partEC")) accessor.nbtData.getCompoundTag(
                    "partEC") else NBTTagCompound()
            return part.getWailaBodey(tag, currenttip)
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

    private fun retraceBlock(world: World,
                             player: EntityPlayerMP, x: Int, y: Int, z: Int): MovingObjectPosition? {
        val block = world.getBlock(x, y, z)
        val head = Vec3.createVectorHelper(player.posX, player.posY,
                player.posZ)
        head.yCoord += player.getEyeHeight().toDouble()
        if (player.isSneaking) head.yCoord -= 0.08
        val look = player.getLook(1.0f)
        val reach = player.theItemInWorldManager.blockReachDistance
        val endVec = head.addVector(look.xCoord * reach, look.yCoord * reach,
                look.zCoord * reach)
        return block.collisionRayTrace(world, x, y, z, head, endVec)
    }
}