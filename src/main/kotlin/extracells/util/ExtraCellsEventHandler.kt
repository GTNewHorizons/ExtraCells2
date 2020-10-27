package extracells.util

import appeng.api.config.SecurityPermissions
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent
import cpw.mods.fml.relauncher.Side
import extracells.api.IECTileEntity
import extracells.container.ContainerFluidStorage
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.event.world.BlockEvent.BreakEvent
open class ExtraCellsEventHandler {
    @SubscribeEvent
    fun onBlockBreak(event: BreakEvent) {
        val tile = event.world.getTileEntity(event.x, event.y, event.z)
        if (tile is IECTileEntity) {
            if (!PermissionUtil.hasPermission(event.player,
                            SecurityPermissions.BUILD,
                            (tile as IECTileEntity).getGridNode(ForgeDirection.UNKNOWN))) event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onPlayerTick(event: PlayerTickEvent) {
        if (event.phase == TickEvent.Phase.START && event.side == Side.SERVER && event.player != null) {
            if (event.player.openContainer != null) {
                val con = event.player.openContainer
                if (con is ContainerFluidStorage) {
                    con.removeEnergyTick()
                    //				}else if (con instanceof ContainerGasStorage) {
//					((ContainerGasStorage) con).removeEnergyTick();
                }
            }
        }
    }
}