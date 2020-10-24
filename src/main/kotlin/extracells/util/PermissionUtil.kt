package extracells.util

import appeng.api.config.SecurityPermissions
import appeng.api.networking.IGrid
import appeng.api.networking.IGridCache
import appeng.api.networking.IGridHost
import appeng.api.networking.IGridNode
import appeng.api.networking.security.ISecurityGrid
import appeng.api.parts.IPart
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.util.ForgeDirection

object PermissionUtil {
    fun hasPermission(player: EntityPlayer?,
                      permission: SecurityPermissions?, grid: IGrid?): Boolean {
        return if (grid != null) hasPermission(player, permission,
                grid.getCache<IGridCache>(ISecurityGrid::class.java) as ISecurityGrid) else true
    }

    @JvmOverloads
    fun hasPermission(player: EntityPlayer?,
                      permission: SecurityPermissions?, host: IGridHost?, side: ForgeDirection? = ForgeDirection.UNKNOWN): Boolean {
        return if (host != null) hasPermission(player, permission, host.getGridNode(side)) else true
    }

    fun hasPermission(player: EntityPlayer?,
                      permission: SecurityPermissions?, host: IGridNode?): Boolean {
        return if (host != null) hasPermission(player, permission, host.grid) else true
    }

    fun hasPermission(player: EntityPlayer?,
                      permission: SecurityPermissions?, part: IPart?): Boolean {
        return if (part != null) hasPermission(player, permission, part.gridNode) else true
    }

    fun hasPermission(player: EntityPlayer?,
                      permission: SecurityPermissions?, securityGrid: ISecurityGrid?): Boolean {
        return if (player == null || permission == null || securityGrid == null) true else securityGrid.hasPermission(
                player, permission)
    }
}