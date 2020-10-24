package extracells.definitions

import appeng.api.definitions.IItemDefinition
import com.google.common.base.Optional
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.IBlockAccess

class ItemItemDefinitions @JvmOverloads constructor(val item: Item?, val meta: Int = 0) : IItemDefinition {
    override fun maybeItem(): Optional<Item> {
        return Optional.fromNullable(item)
    }

    override fun maybeStack(stackSize: Int): Optional<ItemStack> {
        return Optional.of(ItemStack(item, stackSize, meta))
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun isSameAs(comparableStack: ItemStack): Boolean {
        return comparableStack != null && comparableStack.isItemEqual(maybeStack(1).get())
    }

    override fun isSameAs(world: IBlockAccess, x: Int, y: Int, z: Int): Boolean {
        return false
    }
}