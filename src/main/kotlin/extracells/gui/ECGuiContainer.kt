package extracells.gui

import extracells.gui.widget.fluid.WidgetFluidSlot
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container

abstract class ECGuiContainer(p_i1072_1_: Container?) : GuiContainer(p_i1072_1_) {
    var fluidSlotList: MutableList<WidgetFluidSlot?> = mutableListOf()
    var fluidSlot: WidgetFluidSlot? = null
    fun showTooltip(mouseX: Int, mouseY: Int) {
        if (fluidSlot != null) {
            if (func_146978_c(fluidSlot!!.posX, fluidSlot!!.posY, 16, 16, mouseX, mouseY)) {
                fluidSlot!!.drawTooltip(mouseX - guiLeft, mouseY - guiTop)
            }
        }
    }

    fun showTooltipList(mouseX: Int, mouseY: Int) {
        for (fluidSlot in fluidSlotList) {
            if (fluidSlot == null)
                continue
            if (func_146978_c(fluidSlot.posX, fluidSlot.posY, 16, 16, mouseX, mouseY)) {
                fluidSlot.drawTooltip(mouseX - guiLeft, mouseY - guiTop)
            }
        }
    }
}