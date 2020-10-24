package extracells.registries

import extracells.Extracells.ModTab
import extracells.integration.Integration
import extracells.item.*
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.StatCollector

enum class ItemEnum @JvmOverloads constructor(val internalName: String, val item: Item, _mod: Integration.Mods? = null, creativeTab: CreativeTabs? = ModTab) {
    PARTITEM("part.base", ItemPartECBase()), FLUIDSTORAGE("storage.fluid", ItemStorageFluid()), PHYSICALSTORAGE(
            "storage.physical", ItemStoragePhysical()),
    GASSTORAGE("storage.gas", ItemStorageGas(), Integration.Mods.MEKANISMGAS), FLUIDPATTERN("pattern.fluid",
            ItemFluidPattern()),
    FLUIDWIRELESSTERMINAL("terminal.fluid.wireless", ItemWirelessTerminalFluid), STORAGECOMPONENT("storage.component",
            ItemStorageComponent()),
    STORAGECASING("storage.casing", ItemStorageCasing()), FLUIDITEM("fluid.item", ItemFluid(), null,
            null),  // Internal EC Item
    FLUIDSTORAGEPORTABLE("storage.fluid.portable",
            ItemStoragePortableFluidCell),  //	GASSTORAGEPORTABLE("storage.gas.portable", ItemStoragePortableGasCell.INSTANCE, Integration.Mods.MEKANISMGAS),
    CRAFTINGPATTERN("pattern.crafting", ItemInternalCraftingPattern(), null, null),  // Internal EC Item
    UNIVERSALTERMINAL("terminal.universal.wireless", ItemWirelessTerminalUniversal), GASWIRELESSTERMINAL(
            "terminal.gas.wireless", ItemWirelessTerminalGas, Integration.Mods.MEKANISMGAS),
    OCUPGRADE("oc.upgrade", ItemOCUpgrade, Integration.Mods.OPENCOMPUTERS);

    val mod: Integration.Mods?
    fun getDamagedStack(damage: Int): ItemStack {
        return ItemStack(item, 1, damage)
    }

    fun getSizedStack(size: Int): ItemStack {
        return ItemStack(item, size)
    }

    val statName: String
        get() = StatCollector.translateToLocal(item.unlocalizedName)

    init {
        item.unlocalizedName = "extracells." + internalName
        mod = _mod
        if (creativeTab != null && (_mod == null || _mod.isEnabled)) item.creativeTab = ModTab
    }
}