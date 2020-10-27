package extracells.util

import extracells.gui.widget.fluid.WidgetFluidSlot
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.IIcon
import org.lwjgl.opengl.GL11

object GuiUtil {
    fun drawGradientRect(zLevel: Float, par1: Int, par2: Int,
                         par3: Int, par4: Int, par5: Int, par6: Int) {
        val f = (par5 shr 24 and 255) / 255.0f
        val f1 = (par5 shr 16 and 255) / 255.0f
        val f2 = (par5 shr 8 and 255) / 255.0f
        val f3 = (par5 and 255) / 255.0f
        val f4 = (par6 shr 24 and 255) / 255.0f
        val f5 = (par6 shr 16 and 255) / 255.0f
        val f6 = (par6 shr 8 and 255) / 255.0f
        val f7 = (par6 and 255) / 255.0f
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_ALPHA_TEST)
        OpenGlHelper.glBlendFunc(770, 771, 1, 0)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        val tessellator = Tessellator.instance
        tessellator.startDrawingQuads()
        tessellator.setColorRGBA_F(f1, f2, f3, f)
        tessellator.addVertex(par3.toDouble(), par2.toDouble(), zLevel.toDouble())
        tessellator.addVertex(par1.toDouble(), par2.toDouble(), zLevel.toDouble())
        tessellator.setColorRGBA_F(f5, f6, f7, f4)
        tessellator.addVertex(par1.toDouble(), par4.toDouble(), zLevel.toDouble())
        tessellator.addVertex(par3.toDouble(), par4.toDouble(), zLevel.toDouble())
        tessellator.draw()
        GL11.glShadeModel(GL11.GL_FLAT)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
    }

    fun drawIcon(icon: IIcon?, x: Int, y: Int, z: Int, width: Float,
                 height: Float) {
        if (icon == null) return
        val tessellator = Tessellator.instance
        tessellator.startDrawingQuads()
        tessellator.addVertexWithUV(x.toDouble(), y + height.toDouble(), z.toDouble(), icon.minU.toDouble(),
                icon.maxV.toDouble())
        tessellator.addVertexWithUV(x + width.toDouble(), y + height.toDouble(), z.toDouble(), icon.maxU.toDouble(),
                icon.maxV.toDouble())
        tessellator.addVertexWithUV(x + width.toDouble(), y.toDouble(), z.toDouble(), icon.maxU.toDouble(),
                icon.minV.toDouble())
        tessellator.addVertexWithUV(x.toDouble(), y.toDouble(), z.toDouble(), icon.minU.toDouble(),
                icon.minV.toDouble())
        tessellator.draw()
    }

    fun isPointInRegion(guiLeft: Float, guiTop: Int, top: Int,
                        left: Int, height: Int, width: Int, pointX: Int, pointY: Int): Boolean {
        var pointX = pointX
        var pointY = pointY
        pointX -= guiLeft.toInt()
        pointY -= guiTop
        return pointX >= top - 1 && pointX < top + height + 1 && pointY >= left - 1 && pointY < left + width + 1
    }

    fun renderOverlay(zLevel: Float, guiLeft: Int, guiTop: Int,
                      fluidSlot: WidgetFluidSlot?, mouseX: Int, mouseY: Int): Boolean {
        if (fluidSlot?.let { isPointInRegion(guiLeft.toFloat(), guiTop, it.posX,
                        it.posY, 18, 18, mouseX, mouseY)} == true) {
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            drawGradientRect(zLevel, fluidSlot.posX + 1,
                    fluidSlot.posY + 1, fluidSlot.posX + 17,
                    fluidSlot.posY + 17, -0x7F000001, -0x7F000001)
            GL11.glEnable(GL11.GL_LIGHTING)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            return true
        }
        return false
    }
}