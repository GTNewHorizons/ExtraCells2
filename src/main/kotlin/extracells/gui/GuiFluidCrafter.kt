package extracells.gui

import extracells.container.ContainerFluidCrafter
import extracells.registries.BlockEnum
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
open class GuiFluidCrafter(player: InventoryPlayer?, tileentity: IInventory) : GuiContainer(
        ContainerFluidCrafter(player, tileentity)) {
    private val guiTexture = ResourceLocation("extracells",
            "textures/gui/fluidcrafter.png")

    override fun drawGuiContainerBackgroundLayer(f: Float, i: Int, j: Int) {
        drawDefaultBackground()
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture)
        val posX = (width - Companion.xSize) / 2
        val posY = (height - Companion.ySize) / 2
        drawTexturedModalRect(posX, posY, 0, 0, Companion.xSize, Companion.ySize)
        for (s in inventorySlots.inventorySlots) {
            renderBackground(s as Slot?)
        }
    }

    override fun drawGuiContainerForegroundLayer(i: Int, j: Int) {
        fontRendererObj.drawString(BlockEnum.FLUIDCRAFTER.statName,
                5, 5, 0x000000)
    }

    val rowLength: Int
        get() = 3

    private fun renderBackground(slot: Slot?) {
        if (slot!!.stack == null && slot.slotNumber < 9) {
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f)
            mc.textureManager.bindTexture(
                    ResourceLocation("appliedenergistics2",
                            "textures/guis/states.png"))
            drawTexturedModalRect(guiLeft + slot.xDisplayPosition,
                    guiTop + slot.yDisplayPosition, 240, 128, 16, 16)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LIGHTING)
        }
    }

    companion object {
        const val xSize = 176
        const val ySize = 166
    }
}