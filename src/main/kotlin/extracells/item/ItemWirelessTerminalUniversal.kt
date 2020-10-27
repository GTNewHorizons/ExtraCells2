package extracells.item

//import extracells.api.IWirelessGasTermHandler
import appeng.api.AEApi
import appeng.api.features.IWirelessTermHandler
import extracells.api.ECApi
import extracells.api.IWirelessFluidTermHandler
import extracells.integration.Integration
import extracells.integration.WirelessCrafting.WirelessCrafting
import extracells.integration.thaumaticenergistics.ThaumaticEnergistics
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.IIcon
import net.minecraft.util.StatCollector
import net.minecraft.world.World
import java.util.*

object ItemWirelessTerminalUniversal : WirelessTermBase(), EssentiaTerminal, IWirelessFluidTermHandler,/* IWirelessGasTermHandler,*/ IWirelessTermHandler, CraftingTerminal {
    val isTeEnabled: Boolean = Integration.Mods.THAUMATICENERGISTICS.isEnabled
    val isMekEnabled: Boolean = Integration.Mods.MEKANISMGAS.isEnabled
    val isWcEnabled: Boolean = Integration.Mods.WIRELESSCRAFTING.isEnabled
    var icon: IIcon? = null

    override fun isItemNormalWirelessTermToo(its: ItemStack?): Boolean = true

    override fun getUnlocalizedName(itemStack: ItemStack): String = super.getUnlocalizedName(itemStack).replace("item.extracells", "extracells.item")
    override fun onItemRightClick(itemStack: ItemStack, world: World, entityPlayer: EntityPlayer): ItemStack {
        if (world.isRemote) {
            if (entityPlayer.isSneaking)
                return itemStack
            val tag: NBTTagCompound = ensureTagCompound(itemStack)
            if (!tag.hasKey("type"))
                tag.setByte("type", 0)
            if (tag.getByte("type") == 4.toByte() && isWcEnabled)
                WirelessCrafting.openCraftingTerminal(entityPlayer)
            return itemStack
        }
        val tag: NBTTagCompound = ensureTagCompound(itemStack)
        if (!tag.hasKey("type"))
            tag.setByte("type", 0)
        if (entityPlayer.isSneaking)
            return changeMode(itemStack, tag)

        if (tag.getByte("type").toInt() == 0)
            AEApi.instance().registries().wireless().openWirelessTerminalGui(itemStack, world, entityPlayer)
        else if (tag.getByte("type").toInt() == 1)
            ECApi.instance()?.openWirelessFluidTerminal(entityPlayer, itemStack, world)
        else if (tag.getByte("type").toInt() == 2)
            ECApi.instance()?.openWirelessGasTerminal(entityPlayer, itemStack, world)
        else if (tag.getByte("type").toInt() == 3) {
            if (isTeEnabled)
                ThaumaticEnergistics.openEssentiaTerminal(entityPlayer, this)
        }
        return itemStack
    }

    private fun changeMode(itemStack: ItemStack, tag: NBTTagCompound): ItemStack {
        val installed: EnumSet<TerminalType> = getInstalledModules(itemStack)
        when (tag.getByte("type").toInt()) {
            0 -> when {
                installed.contains(TerminalType.FLUID) -> tag.setByte("type", 1)
                isMekEnabled && installed.contains(TerminalType.GAS) -> tag.setByte("type", 2)
                isTeEnabled && installed.contains(TerminalType.ESSENTIA) -> tag.setByte("type", 3)
                isWcEnabled && installed.contains(TerminalType.CRAFTING) -> tag.setByte("type", 4)
            }
            1 -> {
                when {
                    isMekEnabled && installed.contains(TerminalType.GAS) -> tag.setByte("type", 2)
                    isTeEnabled && installed.contains(TerminalType.ESSENTIA) -> tag.setByte("type", 3)
                    isWcEnabled && installed.contains(TerminalType.CRAFTING) -> tag.setByte("type", 4)
                    installed.contains(TerminalType.ITEM) -> tag.setByte("type", 0)
                }
            }
            2 -> {
                when {
                    isTeEnabled && installed.contains(TerminalType.ESSENTIA) -> tag.setByte("type", 3)
                    isWcEnabled && installed.contains(TerminalType.CRAFTING) -> tag.setByte("type", 4)
                    installed.contains(TerminalType.ITEM) -> tag.setByte("type", 0)
                    installed.contains(TerminalType.FLUID) -> tag.setByte("type", 1)
                }
            }
            3 -> {
                when {
                    isWcEnabled && installed.contains(TerminalType.CRAFTING) -> tag.setByte("type", 4)
                    installed.contains(TerminalType.ITEM) -> tag.setByte("type", 0)
                    installed.contains(TerminalType.FLUID) -> tag.setByte("type", 1)
                    isMekEnabled && installed.contains(TerminalType.GAS) -> tag.setByte("type", 2)
                }
            }
            else -> {
                when {
                    installed.contains(TerminalType.ITEM) -> tag.setByte("type", 0)
                    installed.contains(TerminalType.FLUID) -> tag.setByte("type", 1)
                    isMekEnabled && installed.contains(TerminalType.GAS) -> tag.setByte("type", 2)
                    isTeEnabled && installed.contains(TerminalType.ESSENTIA) -> tag.setByte("type", 3)
                    isWcEnabled && installed.contains(TerminalType.CRAFTING) -> tag.setByte("type", 4)
                    else -> tag.setByte("type", 0)
                }
            }
        }
        return itemStack
    }

    override fun registerIcons(iconRegister: IIconRegister) {
        this.icon = iconRegister.registerIcon("extracells:" + "terminal.universal.wireless")
    }

    override fun getIconFromDamage(dmg: Int): IIcon? = this.icon

    override fun addInformation(itemStack: ItemStack, player: EntityPlayer, list: MutableList<Any?>, par4: Boolean) {
        val tag: NBTTagCompound = ensureTagCompound(itemStack)
        if (!tag.hasKey("type")) tag.setByte("type", 0)
        val list2: MutableList<String> = (list as MutableList<String>)
        list2.add(StatCollector.translateToLocal("extracells.tooltip.mode") + ": " + StatCollector.translateToLocal("extracells.tooltip." + TerminalType.values().get(tag.getByte("type").toInt()).toString().toLowerCase()))
        list2.add(StatCollector.translateToLocal("extracells.tooltip.installed"))
        getInstalledModules(itemStack).forEach { list2.add("- " + StatCollector.translateToLocal("extracells.tooltip." + it.name.toLowerCase())) }
        super.addInformation(itemStack, player, list, par4)
    }

    fun installModule(itemStack: ItemStack?, module: TerminalType?) {
        if (itemStack == null || module == null || isInstalled(itemStack, module))
            return
        val install: Byte = (1 shl (module.ordinal)).toByte()
        val tag: NBTTagCompound = ensureTagCompound(itemStack)
        val installed: Byte = if (tag.hasKey("modules")) (tag.getByte("modules") + install).toByte() else install
        tag.setByte("modules", installed)
    }

    private fun getInstalledModules(itemStack: ItemStack): EnumSet<TerminalType> {
        if (itemStack.item == null)
            return EnumSet.noneOf(TerminalType::class.java)
        val tag: NBTTagCompound = ensureTagCompound(itemStack)
        val installed: Int = let {
            if (tag.hasKey("modules"))
                tag.getByte("modules").toInt()
            else 0
        }
        val set: EnumSet<TerminalType> = EnumSet.noneOf(TerminalType::class.java)
        for (x: TerminalType in TerminalType.values()) {
            if (1 == (installed shr (x.ordinal)) % 2)
                set.add(x)
        }
        return set
    }

    fun isInstalled(itemStack: ItemStack?, module: TerminalType): Boolean {
        if (itemStack?.item == null)
            return false
        val tag: NBTTagCompound = ensureTagCompound(itemStack)
        val installed: Byte = if (tag.hasKey("modules")) tag.getByte("modules") else 0
        return (1 == (installed.toInt() shr (module.ordinal)) % 2)
    }

    override fun getSubItems(item: Item, creativeTab: CreativeTabs, itemList: MutableList<Any?>) {
        val itemList2: MutableList<ItemStack> = (itemList as MutableList<ItemStack>)
        val tag = NBTTagCompound()
        tag.setByte("modules", 31)
        val itemStack = ItemStack(item)
        itemStack.tagCompound = tag
        itemStack.tagCompound = tag
        itemList2.add(itemStack.copy())
        injectAEPower(itemStack, this.MAX_POWER())
        itemList2.add(itemStack)
    }

}