package extracells.gui.widget.fluid

import extracells.gui.GuiFluidTerminal
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.StatCollector
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import org.apache.commons.lang3.text.WordUtils
import org.lwjgl.opengl.GL11
import java.util.*
open class WidgetFluidRequest(guiFluidTerminal: GuiFluidTerminal, fluid: Fluid) : AbstractFluidWidget(guiFluidTerminal, 18,
        18, fluid) {
    override fun drawTooltip(posX: Int, posY: Int, mouseX: Int, mouseY: Int): Boolean {
        if (fluid == null
                || !isPointInRegion(posX, posY, height, width,
                        mouseX, mouseY)) return false
        val description: MutableList<String?> = ArrayList()
        description
                .add(StatCollector
                        .translateToLocal("gui.tooltips.appliedenergistics2.Craftable"))
        description.add(fluid.getLocalizedName(FluidStack(fluid,
                1)))
        drawHoveringText(description, mouseX - guiFluidTerminal.guiLeft(),
                mouseY - guiFluidTerminal.guiTop() + 18,
                Minecraft.getMinecraft().fontRenderer)
        return true
    }

    override fun drawWidget(posX: Int, posY: Int) {
        Minecraft.getMinecraft().renderEngine
                .bindTexture(TextureMap.locationBlocksTexture)
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glColor3f(1f, 1f, 1f)
        if (fluid != null && fluid.icon != null) {
            GL11.glColor3f((fluid.color shr 16 and 0xFF) / 255.0f, (fluid.color shr 8 and 0xFF) / 255.0f,
                    (fluid.color and 0xFF) / 255.0f)
            drawTexturedModelRectFromIcon(posX + 1, posY + 1,
                    fluid.icon, height - 2, width - 2)
            GL11.glColor3f(1.0f, 1.0f, 1.0f)
            GL11.glScalef(0.5f, 0.5f, 0.5f)
            var str = StatCollector.translateToLocal("extracells.gui.craft")
            str = WordUtils.capitalize(str.toLowerCase())
            Minecraft.getMinecraft().fontRenderer.drawString(EnumChatFormatting.WHITE.toString() + str,
                    52 + posX - str.length,
                    posY + 24, 0)
        }
        GL11.glEnable(GL11.GL_LIGHTING)
        GL11.glDisable(GL11.GL_BLEND)
    }

    override fun mouseClicked(posX: Int, posY: Int, mouseX: Int, mouseY: Int) {
        // TODO
    }
}