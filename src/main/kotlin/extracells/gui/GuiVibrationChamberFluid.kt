package extracells.gui

import extracells.container.ContainerVibrationChamberFluid
import extracells.gui.widget.WidgetFluidTank
import extracells.tileentity.TileEntityVibrationChamberFluid
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

class GuiVibrationChamberFluid(private val player: EntityPlayer?, private val tileEntity: TileEntityVibrationChamberFluid) : GuiContainer(
        ContainerVibrationChamberFluid(
                player!!.inventory, tileEntity)) {
    private val guiTexture = ResourceLocation("extracells",
            "textures/gui/vibrationchamberfluid.png")
    var widgetFluidTank: WidgetFluidTank? = null
    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY)
        if (widgetFluidTank != null) widgetFluidTank!!.draw(widgetFluidTank!!.posX, widgetFluidTank!!.posY, mouseX,
                mouseY)
        if (widgetFluidTank != null) if (func_146978_c(widgetFluidTank!!.posX, widgetFluidTank!!.posY, 18, 73, mouseX,
                        mouseY)) {
            widgetFluidTank!!.drawTooltip(mouseX - guiLeft, mouseY
                    - guiTop)
        }
    }

    override fun drawGuiContainerBackgroundLayer(f: Float, i: Int, j: Int) {
        drawDefaultBackground()
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture)
        val posX = (width - xSize) / 2
        val posY = (height - ySize) / 2
        drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize)
        //int burnTime = tileEntity.getBurntTimeScaled(52);
        //drawTexturedModalRect(posX + 105, posY + 17 + 54 - burnTime, 176, 0 + 54 - burnTime, 3, burnTime);
    }

    override fun initGui() {
        super.initGui()
        widgetFluidTank = WidgetFluidTank(tileEntity.getTank(), 79, 6)
    }
}