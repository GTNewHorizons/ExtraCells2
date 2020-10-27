package extracells.item

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.tileentity.TileEntityCertusTank
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.StatCollector
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.IFluidContainerItem
open class ItemBlockCertusTank(block: Block?) : ItemBlock(block), IFluidContainerItem {
    private val capacity = 32000
    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack?, player: EntityPlayer, list: MutableList<Any?>,
                                par4: Boolean) {
        if (stack != null && stack.hasTagCompound()) {
            if (FluidStack.loadFluidStackFromNBT(stack.tagCompound
                            .getCompoundTag("tileEntity")) != null)
                                list.add(FluidStack.loadFluidStackFromNBT(stack.tagCompound.getCompoundTag("tileEntity")).amount.toString() + "mB")
        }
    }

    override fun drain(container: ItemStack, maxDrain: Int, doDrain: Boolean): FluidStack? {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("tileEntity")
                || container.stackTagCompound.getCompoundTag("tileEntity")
                        .hasKey("Empty")) {
            return null
        }
        val stack = FluidStack
                .loadFluidStackFromNBT(container.stackTagCompound
                        .getCompoundTag("tileEntity"))
                ?: return null
        val currentAmount = stack.amount
        stack.amount = Math.min(stack.amount, maxDrain)
        if (doDrain) {
            if (currentAmount == stack.amount) {
                container.stackTagCompound.removeTag("tileEntity")
                if (container.stackTagCompound.hasNoTags()) {
                    container.stackTagCompound = null
                }
                return stack
            }
            val fluidTag = container.stackTagCompound
                    .getCompoundTag("tileEntity")
            fluidTag.setInteger("Amount", currentAmount - stack.amount)
            container.stackTagCompound.setTag("tileEntity", fluidTag)
        }
        return stack
    }

    override fun fill(container: ItemStack, resource: FluidStack, doFill: Boolean): Int {
        if (resource == null) {
            return 0
        }
        if (!doFill) {
            if (container.stackTagCompound == null
                    || !container.stackTagCompound.hasKey("tileEntity")) {
                return Math.min(capacity, resource.amount)
            }
            val stack = FluidStack
                    .loadFluidStackFromNBT(container.stackTagCompound
                            .getCompoundTag("tileEntity"))
                    ?: return Math.min(capacity, resource.amount)
            return if (!stack.isFluidEqual(resource)) {
                0
            } else Math.min(capacity - stack.amount,
                    resource.amount)
        }
        if (container.stackTagCompound == null) {
            container.stackTagCompound = NBTTagCompound()
        }
        if (!container.stackTagCompound.hasKey("tileEntity")
                || container.stackTagCompound.getCompoundTag("tileEntity")
                        .hasKey("Empty")) {
            val fluidTag = resource.writeToNBT(NBTTagCompound())
            if (capacity < resource.amount) {
                fluidTag.setInteger("Amount", capacity)
                container.stackTagCompound.setTag("tileEntity", fluidTag)
                return capacity
            }
            container.stackTagCompound.setTag("tileEntity", fluidTag)
            return resource.amount
        }
        val fluidTag = container.stackTagCompound
                .getCompoundTag("tileEntity")
        val stack = FluidStack.loadFluidStackFromNBT(fluidTag)
        if (!stack.isFluidEqual(resource)) {
            return 0
        }
        var filled = capacity - stack.amount
        if (resource.amount < filled) {
            stack.amount += resource.amount
            filled = resource.amount
        } else {
            stack.amount = capacity
        }
        container.stackTagCompound.setTag("tileEntity",
                stack.writeToNBT(fluidTag))
        return filled
    }

    override fun getCapacity(container: ItemStack): Int {
        return capacity
    }

    override fun getFluid(container: ItemStack): FluidStack? {
        return if (container.stackTagCompound == null
                || !container.stackTagCompound.hasKey("tileEntity")) {
            null
        } else FluidStack.loadFluidStackFromNBT(container.stackTagCompound
                .getCompoundTag("tileEntity"))
    }

    override fun getItemStackDisplayName(itemstack: ItemStack?): String {
        if (itemstack != null) {
            if (itemstack.hasTagCompound()) {
                try {
                    val fluidInTank = FluidStack
                            .loadFluidStackFromNBT(itemstack.tagCompound
                                    .getCompoundTag("tileEntity"))
                    if (fluidInTank?.getFluid() != null) {
                        return (StatCollector
                                .translateToLocal(getUnlocalizedName(itemstack))
                                + " - "
                                + fluidInTank.getFluid().getLocalizedName(
                                fluidInTank))
                    }
                } catch (ignored: Throwable) {
                }
            }
            return StatCollector
                    .translateToLocal(getUnlocalizedName(itemstack))
        }
        return ""
    }

    override fun placeBlockAt(stack: ItemStack?, player: EntityPlayer,
                              world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float,
                              hitZ: Float, metadata: Int): Boolean {
        if (!world.setBlock(x, y, z, field_150939_a, metadata, 3)) {
            return false
        }
        if (world.getBlock(x, y, z) === field_150939_a) {
            field_150939_a.onBlockPlacedBy(world, x, y, z, player, stack)
            field_150939_a.onPostBlockPlaced(world, x, y, z, metadata)
        }
        if (stack?.hasTagCompound() == true) {
            (world.getTileEntity(x, y, z) as TileEntityCertusTank)
                    .readFromNBTWithoutCoords(stack.tagCompound
                            .getCompoundTag("tileEntity"))
        }
        return true
    }
}