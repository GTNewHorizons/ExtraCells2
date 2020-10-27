package extracells.util.recipe

import appeng.api.features.INetworkEncodable
import appeng.api.implementations.items.IAEItemPowerStorage
import extracells.item.ItemWirelessTerminalUniversal
import extracells.item.TerminalType
import extracells.registries.ItemEnum
import extracells.util.UniversalTerminal
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World

object RecipeUniversalTerminal : IRecipe {

    val itemUniversal: ItemWirelessTerminalUniversal = ItemWirelessTerminalUniversal
    override fun matches(inventory: InventoryCrafting, world: World): Boolean {
        var hasWireless = false
        var isUniversal = false
        var hasTerminal = false
        val terminals: MutableList<TerminalType> = mutableListOf()
        var terminal: ItemStack? = null
        val size: Int = inventory.sizeInventory
        for (i: Int in 0 until size) {
            val stack: ItemStack = inventory.getStackInSlot(i) ?: continue
            val item: Item = stack.item
            when {
                item == itemUniversal -> {
                    if (hasWireless) return false else {
                        hasWireless = true
                        isUniversal = true
                        terminal = stack
                    }
                }
                UniversalTerminal.isWirelessTerminal(stack) -> {
                    if (hasWireless) return false
                    hasWireless = true
                    terminal = stack
                }
                UniversalTerminal.isTerminal(stack) -> {
                    hasTerminal = true
                    val typeTerminal: TerminalType? = UniversalTerminal.getTerminalType(stack)
                    if (terminals.contains(typeTerminal)) {
                        return false
                    } else {
                        typeTerminal?.let { terminals.add(it) }
                    }
                }
            }
        }
        if (!(hasTerminal && hasWireless))
            return false
        return if (isUniversal) {
            for (x:TerminalType in terminals) {
                if (itemUniversal.isInstalled(terminal, x))
                    return false
            }
            true
        } else {
            val terminalType: TerminalType? = UniversalTerminal.getTerminalType(terminal)
            for (x:TerminalType in terminals) {
                if (x == terminalType)
                    return false
            }
            true
        }
    }

    override fun getRecipeOutput(): ItemStack = ItemEnum.UNIVERSALTERMINAL.getDamagedStack(0)
    override fun getRecipeSize(): Int = 2
    override fun getCraftingResult(inventory: InventoryCrafting): ItemStack? {
        var isUniversal = false
        val terminals: MutableList<TerminalType> = mutableListOf()
        var terminal: ItemStack? = null
        val size: Int = inventory.sizeInventory
        for (i: Int in 0.until(size)) {
            val stack: ItemStack = inventory.getStackInSlot(i) ?: continue
            val item: Item? = stack.item
            when {
                item == itemUniversal -> {
                    isUniversal = true
                    terminal = stack.copy()
                }
                UniversalTerminal.isWirelessTerminal(stack) -> {
                    terminal = stack.copy()
                }
                UniversalTerminal.isTerminal(stack) -> {
                    val typeTerminal: TerminalType? = UniversalTerminal.getTerminalType(stack)
                    typeTerminal?.let { terminals.add(it) }
                }
            }
        }
        if (isUniversal) {
            for (x:TerminalType in terminals) {
                itemUniversal.installModule(terminal, x)
            }
        } else {
            val terminalType: TerminalType? = UniversalTerminal.getTerminalType(terminal)
            val itemTerminal: Item? = terminal?.item
            val t = ItemStack(itemUniversal)
            if (itemTerminal is INetworkEncodable) {
                val key: String? = (itemTerminal as INetworkEncodable).getEncryptionKey(terminal)
                if (key != null)
                    itemUniversal.setEncryptionKey(t, key, null)
            }
            if (itemTerminal is IAEItemPowerStorage) {
                val power: Double = (itemTerminal as IAEItemPowerStorage).getAECurrentPower(terminal)
                itemUniversal.injectAEPower(t, power)
            }
            if (terminal?.hasTagCompound() == true) {
                val nbt: NBTTagCompound = terminal.tagCompound
                if (!t.hasTagCompound()) t.tagCompound = NBTTagCompound()
                if (nbt.hasKey("BoosterSlot")) {
                    t.tagCompound.setTag("BoosterSlot", nbt.getTag("BoosterSlot"))
                }
                if (nbt.hasKey("MagnetSlot")) t.tagCompound.setTag("MagnetSlot", nbt.getTag("MagnetSlot"))
            }
            itemUniversal.installModule(t, terminalType)
            terminalType?.ordinal?.toByte()?.let { t.tagCompound.setByte("type", it) }
            terminal = t
            for (x:TerminalType in terminals) {
                itemUniversal.installModule(terminal, x)
            }
        }
        return terminal
    }
}