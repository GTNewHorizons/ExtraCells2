package extracells.gui.widget.fluid

import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.RenderHelper
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12

abstract class AbstractFluidWidget(protected var guiFluidTerminal: IFluidWidgetGui, height: Int,
                                   width: Int, fluid: Fluid) : Gui() {
    protected var height = 0
    protected var width = 0
    var fluid: Fluid
    protected fun drawHoveringText(list: List<*>, x: Int, y: Int,
                                   fontrenderer: FontRenderer) {
        if (!list.isEmpty()) {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL)
            RenderHelper.disableStandardItemLighting()
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            var k = 0
            for (string in list) {
                val s = string as String?
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

    abstract fun drawTooltip(posX: Int, posY: Int, mouseX: Int,
                             mouseY: Int): Boolean

    abstract fun drawWidget(posX: Int, posY: Int)
    protected fun isPointInRegion(top: Int, left: Int, height: Int, width: Int,
                                  pointX: Int, pointY: Int): Boolean {
        var pointX = pointX
        var pointY = pointY
        val k1 = guiFluidTerminal.guiLeft()
        val l1 = guiFluidTerminal.guiTop() + 18
        pointX -= k1
        pointY -= l1
        return pointX >= top - 1 && pointX < top + height + 1 && pointY >= left - 1 && pointY < left + width + 1
    }

    abstract fun mouseClicked(posX: Int, posY: Int, mouseX: Int, mouseY: Int)
    fun setFluid(fluidID: Int) {
        fluid = FluidRegistry.getFluid(fluidID)
    }

    init {
        this.height = height
        this.width = width
        this.fluid = fluid
    }
}