package extracells.util

import appeng.api.implementations.items.IAEWrench
import buildcraft.api.tools.IToolWrench
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

object WrenchUtil {
    fun canWrench(wrench: ItemStack?, player: EntityPlayer?,
                  x: Int, y: Int, z: Int): Boolean {
        if (wrench == null || wrench.item == null) return false
        try {
            val w = wrench.item as IToolWrench
            return w.canWrench(player, x, y, z)
        } catch (e: Throwable) {
        }
        if (wrench.item is IAEWrench) {
            val w = wrench.item as IAEWrench
            return w.canWrench(wrench, player, x, y, z)
        }
        return false
    }

    fun wrenchUsed(wrench: ItemStack?, player: EntityPlayer?, x: Int,
                   y: Int, z: Int) {
        if (wrench == null || wrench.item == null) return
        try {
            val w = wrench.item as IToolWrench
            w.wrenchUsed(player, x, y, z)
        } catch (e: Throwable) {
        }
    }
}