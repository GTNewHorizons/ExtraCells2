package extracells.util

import net.minecraft.item.ItemStack

object ItemUtils {
    fun areItemEqualsIgnoreStackSize(stack1: ItemStack?, stack2: ItemStack?): Boolean {
        if (stack1 == null && stack2 == null) return true else if (stack1 == null || stack2 == null) return false
        val s1 = stack1.copy()
        val s2 = stack2.copy()
        s1.stackSize = 1
        s2.stackSize = 1
        return ItemStack.areItemStacksEqual(s1, s2)
    }
}