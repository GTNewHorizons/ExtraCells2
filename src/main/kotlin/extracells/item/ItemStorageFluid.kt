package extracells.item

import appeng.api.AEApi
import appeng.api.config.FuzzyMode
import appeng.api.storage.IMEInventoryHandler
import appeng.api.storage.StorageChannel
import appeng.api.storage.data.IAEFluidStack
import extracells.api.IFluidStorageCell
import extracells.api.IHandlerFluidStorage
import extracells.registries.ItemEnum
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
import net.minecraft.util.MathHelper
import net.minecraft.util.StatCollector
import net.minecraft.world.World
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import java.util.*
open class ItemStorageFluid : ItemECBase(), IFluidStorageCell {
    private lateinit var icons: Array<IIcon?>
    override fun addInformation(itemStack: ItemStack, player: EntityPlayer,
                                list: MutableList<Any?>, par4: Boolean) {
        val handler: IMEInventoryHandler<IAEFluidStack> = AEApi.instance().registries().cell().getCellInventory(
                itemStack, null, StorageChannel.FLUIDS) as IMEInventoryHandler<IAEFluidStack>
        if (handler !is IHandlerFluidStorage) {
            return
        }
        val cellHandler = handler as IHandlerFluidStorage
        val partitioned = cellHandler.isFormatted
        val usedBytes = cellHandler.usedBytes().toLong()
        list.add(
                String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.bytes"), usedBytes / 250,
                        cellHandler.totalBytes() / 250))
        list.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.types"),
                cellHandler.usedTypes(), cellHandler.totalTypes()))
        if (usedBytes != 0L) {
            list.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.content"),
                    usedBytes))
        }
        if (partitioned) {
            list.add(StatCollector.translateToLocal(
                    "gui.appliedenergistics2.Partitioned") + " - " + StatCollector.translateToLocal(
                    "gui.appliedenergistics2.Precise"))
        }
    }

    override fun getConfigInventory(`is`: ItemStack): IInventory {
        return ECFluidFilterInventory("configFluidCell", 63, `is`)
    }

    override fun getFilter(stack: ItemStack?): ArrayList<Fluid?>? {
        val inventory = ECFluidFilterInventory("", 63, stack)
        val stacks = inventory.slots
        val filter = ArrayList<Fluid?>()
        if (stacks.isEmpty()) return null
        for (s in stacks) {
            if (s == null) continue
            val f = FluidRegistry.getFluid(s.itemDamage)
            if (f != null) filter.add(f)
        }
        return filter
    }

    override fun getFuzzyMode(`is`: ItemStack): FuzzyMode? {
        if (`is` == null) return null
        if (!`is`.hasTagCompound()) `is`.tagCompound = NBTTagCompound()
        if (`is`.tagCompound.hasKey("fuzzyMode")) return FuzzyMode.valueOf(`is`.tagCompound.getString("fuzzyMode"))
        `is`.tagCompound.setString("fuzzyMode", FuzzyMode.IGNORE_ALL.name)
        return FuzzyMode.IGNORE_ALL
    }

    override fun getIconFromDamage(dmg: Int): IIcon? {
        val j = MathHelper.clamp_int(dmg, 0, suffixes.size)
        return icons[j]
    }

    override fun getMaxBytes(`is`: ItemStack?): Int {
        if (`is` != null) {
            return spaces[0.coerceAtLeast(`is`.itemDamage)]
        }
        return -1
    }

    override fun getMaxTypes(unused: ItemStack?): Int {
        return 5
    }

    override fun getSubItems(item: Item, creativeTab: CreativeTabs,
                             listSubItems: MutableList<Any?>) {
        for (i in suffixes.indices) {
            listSubItems.add(ItemStack(item, 1, i))
        }
    }

    override fun getUnlocalizedName(itemStack: ItemStack): String {
        return "extracells.item.storage.fluid." + suffixes[itemStack.itemDamage]
    }

    override fun getUpgradesInventory(`is`: ItemStack): IInventory {
        return ECPrivateInventory("configInventory", 0, 64)
    }

    override fun isEditable(`is`: ItemStack?): Boolean {
        return if (`is` == null) false else `is`.item === this
    }

    override fun onItemRightClick(itemStack: ItemStack, world: World,
                                  entityPlayer: EntityPlayer): ItemStack {
        if (!entityPlayer.isSneaking) {
            return itemStack
        }
        val handler: IMEInventoryHandler<IAEFluidStack> = AEApi.instance().registries().cell().getCellInventory(
                itemStack, null, StorageChannel.FLUIDS) as IMEInventoryHandler<IAEFluidStack>
        if (handler !is IHandlerFluidStorage) {
            return itemStack
        }
        val cellHandler = handler as IHandlerFluidStorage
        return if (cellHandler.usedBytes() == 0 && entityPlayer.inventory.addItemStackToInventory(
                        ItemEnum.STORAGECASING.getDamagedStack(1))) {
            ItemEnum.STORAGECOMPONENT.getDamagedStack(itemStack.itemDamage + 4)
        } else itemStack
    }

    override fun registerIcons(iconRegister: IIconRegister) {
        icons = arrayOfNulls(suffixes.size)
        for (i in suffixes.indices) {
            icons[i] = iconRegister.registerIcon("extracells:" + "storage.fluid." + suffixes[i])
        }
    }

    override fun setFuzzyMode(`is`: ItemStack?, fzMode: FuzzyMode) {
        if (`is` == null) return
        val tag: NBTTagCompound = if (`is`.hasTagCompound()) `is`.tagCompound else NBTTagCompound()
        tag.setString("fuzzyMode", fzMode.name)
        `is`.tagCompound = tag
    }

    companion object {
        val suffixes = arrayOf("1k", "4k", "16k", "64k", "256k", "1024k", "4096k")
        val spaces = intArrayOf(1024, 4096, 16348, 65536, 262144, 1048576, 4194304)
    }

    init {
        setMaxStackSize(1)
        maxDamage = 0
        setHasSubtypes(true)
    }
}