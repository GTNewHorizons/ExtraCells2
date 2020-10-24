package extracells.gui

import extracells.container.ContainerFluidFiller
import extracells.gui.widget.WidgetSlotFluidContainer
import extracells.tileentity.TileEntityFluidFiller
import extracells.util.GuiUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StatCollector
import org.lwjgl.opengl.GL11

class GuiFluidFiller(private val player: EntityPlayer, tileentity: TileEntityFluidFiller) : GuiContainer(
        ContainerFluidFiller(
                player.inventory, tileentity)) {
    private val guiTexture = ResourceLocation("extracells",
            "textures/gui/fluidfiller.png")
    private val fluidContainerSlot: WidgetSlotFluidContainer?
    override fun drawGuiContainerBackgroundLayer(f: Float, i: Int, j: Int) {
        drawDefaultBackground()
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture)
        val posX = (width - Companion.xSize) / 2
        val posY = (height - Companion.ySize) / 2
        drawTexturedModalRect(posX, posY, 0, 0, Companion.xSize, Companion.ySize)
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        fontRendererObj
                .drawString(
                        StatCollector.translateToLocal(
                                "extracells.block.fluidfiller.name").replace(
                                "ME ", ""), 5, 5, 0x000000)
        val i = fluidContainerSlot.getPosX()
        val j = fluidContainerSlot.getPosY()
        if (GuiUtil.isPointInRegion(guiLeft.toFloat(), guiTop, i, j, 16, 16,
                        mouseX, mouseY)) {
            fluidContainerSlot!!.drawWidgetWithRect(i, j)
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glColorMask(true, true, true, false)
            drawGradientRect(i, j, i + 16, j + 16, -2130706433,
                    -2130706433)
            GL11.glColorMask(true, true, true, true)
            GL11.glEnable(GL11.GL_LIGHTING)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
        } else fluidContainerSlot!!.drawWidget()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseBtn: Int) {
        super.mouseClicked(mouseX, mouseY, mouseBtn)
        if (fluidContainerSlot != null) if (GuiUtil.isPointInRegion(guiLeft.toFloat(), guiTop,
                        fluidContainerSlot.posX,
                        fluidContainerSlot.posY, 18, 18, mouseX, mouseY)) {
            fluidContainerSlot.mouseClicked(player.inventory
                    .itemStack)
        }
    }

    companion object {
        const val xSize = 176
        const val ySize = 166
    }

    init {
        fluidContainerSlot = WidgetSlotFluidContainer(player,
                tileentity, 80, 35)
    }
}