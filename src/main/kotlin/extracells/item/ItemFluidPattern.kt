package extracells.item

import extracells.registries.ItemEnum
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.IIcon
import net.minecraft.util.StatCollector
import net.minecraft.world.World
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack

class ItemFluidPattern : ItemECBase() {
    var icon: IIcon? = null
    override fun getIcon(itemStack: ItemStack, pass: Int): IIcon {
        if (pass == 0) {
            val fluid = getFluid(itemStack)
            if (fluid != null) return fluid.icon
        }
        return icon!!
    }

    override fun getItemStackDisplayName(itemStack: ItemStack): String {
        val fluid = getFluid(itemStack)
                ?: return StatCollector.translateToLocal(getUnlocalizedName(itemStack))
        return (StatCollector.translateToLocal(getUnlocalizedName(itemStack))
                + ": " + fluid.getLocalizedName(FluidStack(fluid, 1)))
    }

    override fun getSpriteNumber(): Int {
        return 1
    }

    override fun getSubItems(item: Item, creativeTab: CreativeTabs, itemList: MutableList<*>) {
        super.getSubItems(item, creativeTab, itemList)
        for (fluid in FluidRegistry.getRegisteredFluidIDsByFluid().keys) {
            val itemStack = ItemStack(this, 1)
            itemStack.tagCompound = NBTTagCompound()
            itemStack.tagCompound.setString("fluidID", fluid.name)
            itemList.add(itemStack)
        }
    }

    override fun getUnlocalizedName(itemStack: ItemStack): String {
        return "extracells.item.fluid.pattern"
    }

    override fun onItemRightClick(itemStack: ItemStack, world: World,
                                  entityPlayer: EntityPlayer): ItemStack {
        return if (entityPlayer.isSneaking) ItemEnum.FLUIDPATTERN.getSizedStack(itemStack.stackSize) else itemStack
    }

    override fun registerIcons(iconRegister: IIconRegister) {
        icon = iconRegister.registerIcon("extracells:fluid.pattern")
    }

    override fun requiresMultipleRenderPasses(): Boolean {
        return true
    }

    companion object {
        fun getFluid(itemStack: ItemStack): Fluid {
            if (!itemStack.hasTagCompound()) itemStack.tagCompound = NBTTagCompound()
            return FluidRegistry.getFluid(itemStack.tagCompound.getString(
                    "fluidID"))
        }

        fun getPatternForFluid(fluid: Fluid?): ItemStack {
            val itemStack = ItemStack(ItemEnum.FLUIDPATTERN.item, 1)
            itemStack.tagCompound = NBTTagCompound()
            if (fluid != null) itemStack.tagCompound.setString("fluidID", fluid.name)
            return itemStack
        }
    }

    init {
        setMaxStackSize(1)
    }
}