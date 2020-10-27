package extracells.util.inventory

import extracells.registries.ItemEnum
import extracells.util.FluidUtil
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
open class ECFluidFilterInventory(_customName: String, _size: Int,
                             private val cellItem: ItemStack?) : ECPrivateInventory(_customName, _size, 1) {
    override fun isItemValidForSlot(i: Int, itemstack: ItemStack?): Boolean {
        if (itemstack == null) return false
        if (itemstack.item === ItemEnum.FLUIDITEM.item) {
            val fluidID = itemstack.itemDamage
            for (s in slots) {
                if (s == null) continue
                if (s.itemDamage == fluidID) return false
            }
            return true
        }
        if (!FluidUtil.isFilled(itemstack)) return false
        val stack = FluidUtil.getFluidFromContainer(itemstack) ?: return false
        val fluidID = stack.fluidID
        for (s in slots) {
            if (s == null) continue
            if (s.itemDamage == fluidID) return false
        }
        return true
    }

    override fun markDirty() {
        val tag: NBTTagCompound = if (cellItem?.hasTagCompound() == true) cellItem.tagCompound else NBTTagCompound()
        tag.setTag("filter", writeToNBT())
    }

    override fun setInventorySlotContents(slotId: Int, itemstack: ItemStack?) {
        if (itemstack == null) {
            super.setInventorySlotContents(slotId, null)
            return
        }
        val fluid: Fluid?
        if (itemstack.item === ItemEnum.FLUIDITEM.item) {
            fluid = FluidRegistry.getFluid(itemstack.itemDamage)
            if (fluid == null) return
        } else {
            if (!isItemValidForSlot(slotId, itemstack)) return
            val fluidStack = FluidUtil.getFluidFromContainer(itemstack)
            if (fluidStack == null) {
                super.setInventorySlotContents(slotId, null)
                return
            }
            fluid = fluidStack.getFluid()
            if (fluid == null) {
                super.setInventorySlotContents(slotId, null)
                return
            }
        }
        super.setInventorySlotContents(slotId,
                ItemStack(ItemEnum.FLUIDITEM.item, 1, fluid.id))
    }

    init {
        if (cellItem?.hasTagCompound() == true) if (cellItem.tagCompound?.hasKey("filter") == true) readFromNBT(
                cellItem.tagCompound.getTagList("filter",
                        10))
    }
}