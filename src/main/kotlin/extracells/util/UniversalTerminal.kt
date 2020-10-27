package extracells.util

import appeng.api.AEApi
import extracells.integration.Integration.Mods
import extracells.integration.WirelessCrafting.WirelessCrafting
import extracells.integration.thaumaticenergistics.ThaumaticEnergistics
import extracells.item.TerminalType
import extracells.registries.ItemEnum
import extracells.registries.PartEnum
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object UniversalTerminal {
//    val isMekLoaded: Boolean = Mods.MEKANISMGAS.isEnabled
    val isThaLoaded: Boolean = Mods.THAUMATICENERGISTICS.isEnabled
    val isWcLLoaded: Boolean = Mods.WIRELESSCRAFTING.isEnabled
    val arrayLength: Int = run {
        var length = 2
//        if (isMekLoaded)
//            length += 1
        if (isThaLoaded)
            length += 1
        if (isWcLLoaded)
            length += 1
       return@run length
    }
    val wirelessTerminals: Array<ItemStack?> = run {
        val terminals: Array<ItemStack?> = arrayOfNulls(arrayLength)
        terminals[0] = AEApi.instance().definitions().items().wirelessTerminal().maybeStack(1).get()
        terminals[1] = ItemEnum.FLUIDWIRELESSTERMINAL.getSizedStack(1)
        var next = 2
//        if (isMekLoaded) {
//            terminals[next] = ItemEnum.GASWIRELESSTERMINAL.getSizedStack(1)
//            next += 1
//        }
        if (isThaLoaded) {
            terminals[next] = ThaumaticEnergistics.getWirelessTerminal()
            next += 1
        }
        if (isWcLLoaded) 
            terminals[next] = WirelessCrafting.getCraftingTerminal()
        return@run terminals
    }
    val terminals: Array<ItemStack?> = run {
        val terminals: Array<ItemStack?> = arrayOfNulls(if (isWcLLoaded) arrayLength - 1 else arrayLength)
        terminals[0] = AEApi.instance().definitions().parts().terminal().maybeStack(1).get()
        terminals[1] = ItemEnum.PARTITEM.getDamagedStack(PartEnum.FLUIDTERMINAL.ordinal)
       /* if (isMekLoaded) {
            terminals[2] = ItemEnum.PARTITEM.getDamagedStack(PartEnum.GASTERMINAL.ordinal)
            if (isThaLoaded)
                terminals[3] = ThaumaticEnergistics.getTerminal()
        } else*/ if (isThaLoaded)
            terminals[2] = ThaumaticEnergistics.getTerminal()
        return@run terminals
    }

    fun isTerminal(stack: ItemStack?): Boolean {
        if (stack == null) 
            return false
        val item: Item? = stack.item
        val meta: Int = stack.itemDamage
        if (item == null) 
            return false
        val aeterm: ItemStack = AEApi.instance().definitions().parts().terminal().maybeStack(1).get()
        if (item == aeterm.item && meta == aeterm.itemDamage) return true
        val ecterm: ItemStack = ItemEnum.PARTITEM.getDamagedStack(PartEnum.FLUIDTERMINAL.ordinal)
        if (item == ecterm.item && meta == ecterm.itemDamage) return true
//        val ectermgas: ItemStack = ItemEnum.PARTITEM.getDamagedStack(PartEnum.GASTERMINAL.ordinal)
//        if (item == ectermgas.item && meta == ectermgas.itemDamage) return true
        if (Mods.THAUMATICENERGISTICS.isEnabled) {
            val thterm = ThaumaticEnergistics.getTerminal()
                    if (item == thterm?.item && meta == thterm.itemDamage) return true
        }
        return false
    }

    fun isWirelessTerminal(stack: ItemStack?): Boolean {
        if (stack == null) return false
        val item: Item? = stack.item
        val meta: Int = stack.itemDamage
        if (item == null) return false
        val aeterm: ItemStack = AEApi.instance().definitions().items().wirelessTerminal().maybeStack(1).get()
        if (item == aeterm.item && meta == aeterm.itemDamage) return true
        val ecterm: ItemStack = ItemEnum.FLUIDWIRELESSTERMINAL.getDamagedStack(0)
        if (item == ecterm.item && meta == ecterm.itemDamage) return true
//        val ectermgas: ItemStack = ItemEnum.GASWIRELESSTERMINAL.getDamagedStack(0)
//        if (item == ectermgas.item && meta == ectermgas.itemDamage) return true
        if (Mods.THAUMATICENERGISTICS.isEnabled) {
            val thterm = ThaumaticEnergistics.getTerminal()
                if (item == thterm?.item && meta == thterm.itemDamage) return true
        }
        if (isWcLLoaded) {
            val wcTerm = WirelessCrafting.getCraftingTerminal()
                    if (item == wcTerm?.item && meta == wcTerm.itemDamage) return true
        }
        return false
    }

    fun getTerminalType(stack: ItemStack?): TerminalType? {
        if (stack == null) return null
        val item: Item? = stack.item
        val meta: Int = stack.itemDamage
        if (item == null)
            return null
        val aeterm: ItemStack = AEApi.instance().definitions().parts().terminal().maybeStack(1).get()
        if (item == aeterm.item && meta == aeterm.itemDamage) return TerminalType.ITEM
        val ecterm: ItemStack = ItemEnum.PARTITEM.getDamagedStack(PartEnum.FLUIDTERMINAL.ordinal)
        if (item == ecterm.item && meta == ecterm.itemDamage) return TerminalType.FLUID
//        val ectermgas: ItemStack = ItemEnum.PARTITEM.getDamagedStack(PartEnum.GASTERMINAL.ordinal)
//        if (item == ectermgas.item && meta == ectermgas.itemDamage) return TerminalType.GAS
        if (Mods.THAUMATICENERGISTICS.isEnabled) {
            val thterm = ThaumaticEnergistics.getTerminal()
                    if (item == thterm?.item && meta == thterm.itemDamage) return TerminalType.ESSENTIA
        }
        val aeterm2: ItemStack = AEApi.instance().definitions().items().wirelessTerminal().maybeStack(1).get()
        if (item == aeterm2.item && meta == aeterm2.itemDamage) return TerminalType.ITEM
        val ecterm2: ItemStack = ItemEnum.FLUIDWIRELESSTERMINAL.getDamagedStack(0)
        if (item == ecterm2.item && meta == ecterm2.itemDamage) return TerminalType.FLUID
//        val ectermgas2: ItemStack = ItemEnum.GASWIRELESSTERMINAL.getDamagedStack(0)
//        if (item == ectermgas2.item && meta == ectermgas2.itemDamage) return TerminalType.GAS
        if (Mods.THAUMATICENERGISTICS.isEnabled) {
            val thterm = ThaumaticEnergistics.getWirelessTerminal()
                if (item == thterm?.item && meta == thterm.itemDamage) return TerminalType.ESSENTIA
        }
        if (isWcLLoaded) {
            val wcTerm = WirelessCrafting.getCraftingTerminal()
                    if (item == wcTerm?.item && meta == wcTerm.itemDamage) return TerminalType.CRAFTING
        }
        return null
    }
}