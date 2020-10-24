package extracells.definitions

import appeng.api.definitions.ITileDefinition
import com.google.common.base.Optional
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.IBlockAccess

class BlockItemDefinitions @JvmOverloads constructor(private val block: Block?, private val meta: Int = 0,
                                                     private val blockTileEntity: Class<out TileEntity?>? = null) : ITileDefinition {
    constructor(_block: Block?,
                _blockTileEntity: Class<out TileEntity?>?) : this(_block, 0, _blockTileEntity) {
    }

    override fun maybeBlock(): Optional<Block> {
        return Optional.fromNullable(block)
    }

    override fun maybeItemBlock(): Optional<ItemBlock> {
        return Optional.absent()
    }

    override fun isSameAs(comparableStack: ItemStack): Boolean {
        return comparableStack != null && ItemStack.areItemStacksEqual(maybeStack(1).orNull(), comparableStack)
    }

    override fun isSameAs(world: IBlockAccess, x: Int, y: Int, z: Int): Boolean {
        val block = world.getBlock(x, y, z)
        return !maybeBlock().isPresent && block === this.block
    }

    override fun maybeItem(): Optional<Item> {
        return Optional.fromNullable(Item.getItemFromBlock(block))
    }

    override fun maybeStack(stackSize: Int): Optional<ItemStack> {
        return Optional.of(ItemStack(block, stackSize, meta))
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun maybeEntity(): Optional<out Class<out TileEntity>> {
        return Optional.fromNullable(blockTileEntity)
    }
}