package extracells.gui.widget

import appeng.api.config.AccessRestriction
import com.google.common.base.Splitter
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StatCollector
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import java.util.*
open class WidgetStorageDirection(ID: Int, xPos: Int, yPos: Int, width: Int,
                             height: Int, var accessRestriction: AccessRestriction?) : GuiButton(ID, xPos, yPos, width,
        height, "") {
    override fun drawButton(minecraftInstance: Minecraft, x: Int, y: Int) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        mouseDragged(minecraftInstance, x, y)
        minecraftInstance.textureManager.bindTexture(
                ResourceLocation("appliedenergistics2",
                        "textures/guis/states.png"))
        drawTexturedModalRect(xPosition, yPosition, 240, 240, 16, 16)
        when (accessRestriction) {
            AccessRestriction.NO_ACCESS -> drawTexturedModalRect(xPosition, yPosition, 96, 0, 16, 16)
            AccessRestriction.READ -> drawTexturedModalRect(xPosition, yPosition, 16, 144, 16,
                    16)
            AccessRestriction.READ_WRITE -> drawTexturedModalRect(xPosition, yPosition, 32, 144, 16,
                    16)
            AccessRestriction.WRITE -> drawTexturedModalRect(xPosition, yPosition, 0, 144, 16,
                    16)
            else -> {
            }
        }
    }

    protected fun drawHoveringText(list: List<*>, x: Int, y: Int,
                                   fontrenderer: FontRenderer) {
        if (!list.isEmpty()) {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL)
            RenderHelper.disableStandardItemLighting()
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            var k = 0
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val s = iterator.next() as String
                val l = fontrenderer.getStringWidth(s)
                if (l > k) {
                    k = l
                }
            }
            val i1 = x + 12
            var j1 = y - 12
            var k1 = 8
            if (list.size > 1) {
                k1 += 2 + (list.size - 1) * 10
            }
            zLevel = 300.0f
            val l1 = -267386864
            drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1)
            drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4,
                    l1, l1)
            drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1,
                    l1)
            drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1)
            drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3,
                    l1, l1)
            val i2 = 1347420415
            val j2 = i2 and 16711422 shr 1 or i2 and -16777216
            drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3
                    - 1, i2, j2)
            drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, (j1 + k1
                    + 3) - 1, i2, j2)
            drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2,
                    i2)
            drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3,
                    j2, j2)
            for (k2 in list.indices) {
                val s1 = list[k2] as String
                fontrenderer.drawStringWithShadow(s1, i1, j1, -1)
                if (k2 == 0) {
                    j1 += 2
                }
                j1 += 10
            }
            zLevel = 0.0f
            GL11.glEnable(GL11.GL_LIGHTING)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            RenderHelper.enableStandardItemLighting()
            GL11.glEnable(GL12.GL_RESCALE_NORMAL)
        }
    }

    fun drawTooltip(mouseX: Int, mouseY: Int, guiXPos: Int, guiYPos: Int) {
        val description: MutableList<String> = ArrayList()
        description.add(StatCollector
                .translateToLocal("gui.tooltips.appliedenergistics2.IOMode"))
        var explanation: String? = ""
        when (accessRestriction) {
            AccessRestriction.NO_ACCESS -> explanation = StatCollector
                    .translateToLocal("gui.tooltips.appliedenergistics2.Disabled")
            AccessRestriction.READ -> explanation = StatCollector
                    .translateToLocal("gui.tooltips.appliedenergistics2.Read")
            AccessRestriction.READ_WRITE -> explanation = StatCollector
                    .translateToLocal("gui.tooltips.appliedenergistics2.ReadWrite")
            AccessRestriction.WRITE -> explanation = StatCollector
                    .translateToLocal("gui.tooltips.appliedenergistics2.Write")
            else -> {
            }
        }
        for (current in Splitter.fixedLength(30).split(explanation)) {
            description.add(EnumChatFormatting.GRAY.toString() + current)
        }
        val mc = Minecraft.getMinecraft()
        if (mouseX >= xPosition && mouseX <= xPosition + width && mouseY >= yPosition && mouseY <= yPosition + height) {
            drawHoveringText(description, mouseX - guiXPos, mouseY - guiYPos,
                    mc.fontRenderer)
        }
    }
}