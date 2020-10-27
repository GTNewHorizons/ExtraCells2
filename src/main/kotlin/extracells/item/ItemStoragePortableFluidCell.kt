package extracells.item

import appeng.api.AEApi
import appeng.api.config.AccessRestriction
import appeng.api.config.FuzzyMode
import appeng.api.storage.IMEInventoryHandler
import appeng.api.storage.StorageChannel
import appeng.api.storage.data.IAEFluidStack
import extracells.api.ECApi
import extracells.api.IHandlerFluidStorage
import extracells.api.IPortableFluidStorageCell
import extracells.util.inventory.ECFluidFilterInventory
import extracells.util.inventory.ECPrivateInventory
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.IIcon
import net.minecraft.util.StatCollector
import net.minecraft.world.World
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry

object ItemStoragePortableFluidCell : PowerItem(), IPortableFluidStorageCell {
    override val MAX_POWER: Double = 20000.0

    internal var icon: IIcon? = null

    override fun addInformation(itemStack: ItemStack, player: EntityPlayer, list: MutableList<Any?>, par4: Boolean): Unit {
        val list2: MutableList<String> = (list as MutableList<String>)
        val handler: IMEInventoryHandler<IAEFluidStack> = (AEApi.instance().registries().cell().getCellInventory(itemStack, null, StorageChannel.FLUIDS) as IMEInventoryHandler<IAEFluidStack>)
        if (!((handler is IHandlerFluidStorage))) {
            return
        }
        val cellHandler: IHandlerFluidStorage = handler
        val partitioned: Boolean = cellHandler.isFormatted
        val usedBytes: Long = cellHandler.usedBytes().toLong()
        val aeCurrentPower: Double = getAECurrentPower(itemStack)
        list2.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.bytes"), ((usedBytes / 250) as Any), ((cellHandler.totalBytes() / 250) as Any)))
        list2.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.types"), (cellHandler.usedTypes() as Any), (cellHandler.totalTypes() as Any)))
        if (usedBytes != 0L) {
            list2.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.content"), (usedBytes as Any)))
        }
        if (partitioned) {
            list2.add(StatCollector.translateToLocal("gui.appliedenergistics2.Partitioned") + " - " + StatCollector.translateToLocal("gui.appliedenergistics2.Precise"))
        }
        list2.add(StatCollector.translateToLocal("gui.appliedenergistics2.StoredEnergy") + ": " + aeCurrentPower + " AE - " + Math.floor(aeCurrentPower / ItemStoragePortableFluidCell.MAX_POWER * 1e4) / 1e2 + "%")
    }

    override fun getConfigInventory(its: ItemStack?): IInventory? {
        return ECFluidFilterInventory("configFluidCell", 63, its)
    }

    override fun getDurabilityForDisplay(itemStack: ItemStack?): Double {
        if (itemStack == null)
            return -1.0
        return 1 - getAECurrentPower(itemStack) / ItemStoragePortableFluidCell.MAX_POWER
    }

    override fun getFilter(stack: ItemStack?): ArrayList<Fluid?>? {
        val inventory: ECFluidFilterInventory = ECFluidFilterInventory("", 63, stack)
        val stacks: Array<ItemStack?> = inventory.slots
        val filter: ArrayList<Fluid?> = ArrayList()
        if (stacks.isEmpty())
            return null
        for (s:ItemStack? in stacks) {
            if (s != null) {
                val f: Fluid? = FluidRegistry.getFluid(s.itemDamage)
                if (f != null)
                    filter.add(f)
            }
        }
        return filter
    }

    override fun getFuzzyMode(its: ItemStack?): FuzzyMode? {
        if ( its == null) return null
        if ( !its.hasTagCompound()) its.tagCompound = NBTTagCompound()
        if ( its.tagCompound.hasKey("fuzzyMode")) return FuzzyMode.valueOf( its .tagCompound.getString("fuzzyMode"))
        its.tagCompound.setString("fuzzyMode", FuzzyMode.IGNORE_ALL.name)
        return FuzzyMode.IGNORE_ALL
    }

    override fun getIconFromDamage(dmg: Int): IIcon? {
        return this.icon
    }

    override fun getMaxBytes(its: ItemStack?): Int {
        return 512
    }

    override fun getMaxTypes(unused: ItemStack?): Int {
        return 3
    }

    override fun getPowerFlow(itemStack: ItemStack?): AccessRestriction {
        return AccessRestriction.READ_WRITE
    }

    override fun MAX_POWER(): Double = MAX_POWER

    override fun getSubItems(item: Item, creativeTab: CreativeTabs, itemList: MutableList<Any?>) {
        val itemList2: MutableList<ItemStack> = (itemList as MutableList<ItemStack>)
        itemList2.add(ItemStack(item))
        val itemStack = ItemStack(item)
        injectAEPower(itemStack, MAX_POWER)
        itemList2.add(itemStack)
    }

    override fun getUnlocalizedName(itemStack: ItemStack?): String {
        return "extracells.item.storage.fluid.portable"
    }

    override fun getUpgradesInventory(its: ItemStack?): IInventory {
        return ECPrivateInventory("configInventory", 0, 64)
    }

    override fun hasPower(player: EntityPlayer?, amount: Double, its: ItemStack?): Boolean {
        return its?.let { getAECurrentPower(it ) >= amount } == true
    }

    override fun isEditable(its: ItemStack?): Boolean {
        return its != null && its.item == this
    }

    override fun onItemRightClick(itemStack: ItemStack?, world: World?, player: EntityPlayer?): ItemStack? {
        return ECApi.instance()?.openPortableFluidCellGui(player, itemStack, world)
    }

    override fun registerIcons(iconRegister: IIconRegister) {
        this.icon = iconRegister.registerIcon("extracells:storage.fluid.portable")
    }

    override fun setFuzzyMode(its: ItemStack?, fzMode: FuzzyMode): Unit {
        if ( its == null) return
        if ( !its.hasTagCompound()) its.tagCompound = NBTTagCompound()
        val tag: NBTTagCompound = its.tagCompound
        tag.setString("fuzzyMode", fzMode.name)
    }

    override fun showDurabilityBar(itemStack: ItemStack?): Boolean {
        return true
    }

    override fun usePower(player: EntityPlayer?, amount: Double, its: ItemStack?): Boolean {
        if (its != null) {
            extractAEPower(its, amount)
        }
        return true
    }
}