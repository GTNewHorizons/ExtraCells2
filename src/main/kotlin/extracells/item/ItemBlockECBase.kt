package extracells.item

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.tileentity.TileEntityFluidInterface
import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraft.util.StatCollector
import net.minecraft.world.World

class ItemBlockECBase(block: Block?) : ItemBlock(block) {
    @SideOnly(Side.CLIENT)
    override fun getIconFromDamage(damage: Int): IIcon {
        return Block.getBlockFromItem(this).getIcon(0, damage)
    }

    override fun getItemStackDisplayName(stack: ItemStack): String {
        return StatCollector.translateToLocal(getUnlocalizedName(stack) + ".name")
    }

    override fun getMetadata(damage: Int): Int {
        return damage
    }

    @SideOnly(Side.CLIENT)
    override fun getSpriteNumber(): Int {
        return 0
    }

    @SideOnly(Side.CLIENT)
    override fun getSubItems(item: Item, tab: CreativeTabs, list: MutableList<*>) {
        list.add(ItemStack(item))
        list.add(ItemStack(item, 1, 1))
    }

    override fun getUnlocalizedName(stack: ItemStack): String {
        return if (stack == null) "null" else when (stack.itemDamage) {
            0 -> "extracells.block.fluidinterface"
            1 -> "extracells.block.fluidfiller"
            else -> super.getUnlocalizedName(stack)
        }
    }

    override fun placeBlockAt(stack: ItemStack, player: EntityPlayer,
                              world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float,
                              hitZ: Float, metadata: Int): Boolean {
        if (!world.setBlock(x, y, z, field_150939_a, metadata, 3)) {
            return false
        }
        if (world.getBlock(x, y, z) === field_150939_a) {
            field_150939_a.onBlockPlacedBy(world, x, y, z, player, stack)
            field_150939_a.onPostBlockPlaced(world, x, y, z, metadata)
        }
        if (getMetadata(stack.itemDamage) == 0 && stack.hasTagCompound()) {
            val tile = world.getTileEntity(x, y, z)
            if (tile != null && tile is TileEntityFluidInterface) {
                tile.readFilter(stack
                        .tagCompound)
            }
        }
        return true
    }

    init {
        maxDamage = 0
        setHasSubtypes(true)
    }
}