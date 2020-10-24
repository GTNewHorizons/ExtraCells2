package extracells.gui.widget

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StatCollector
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidTank
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import java.util.*

class WidgetFluidTank @JvmOverloads constructor(var tank: IFluidTank?, var posX: Int, var posY: Int,
                                                var direction: ForgeDirection = ForgeDirection.UNKNOWN) : Gui() {
    fun draw(guiX: Int, guiY: Int, mouseX: Int, mouseY: Int) {
        if (tank == null || 73 < 31) return
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glColor3f(1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceLocation(
                "extracells", "textures/gui/fluidtank.png"))
        drawTexturedModalRect(posX, posY, 0, 0, 18, 73)
        val iconHeightRemainder = (73 - 4) % 16
        val fluid = tank!!.fluid
        if (fluid != null && fluid.amount > 0) {
            Minecraft.getMinecraft().renderEngine
                    .bindTexture(TextureMap.locationBlocksTexture)
            val fluidIcon = fluid.getFluid().stillIcon
            GL11.glColor3f((fluid.getFluid().color shr 16 and 0xFF) / 255.0f,
                    (fluid.getFluid().color shr 8 and 0xFF) / 255.0f, (fluid.getFluid().color and 0xFF) / 255.0f)
            if (iconHeightRemainder > 0) {
                drawTexturedModelRectFromIcon(posX + 1, posY + 2,
                        fluidIcon, 16, iconHeightRemainder)
            }
            for (i in 0 until (73 - 6) / 16) {
                drawTexturedModelRectFromIcon(posX + 1, posY + 2 + (i
                        * 16) + iconHeightRemainder, fluidIcon, 16, 16)
            }
            GL11.glColor3f(1.0f, 1.0f, 1.0f)
            Minecraft.getMinecraft().renderEngine
                    .bindTexture(ResourceLocation("extracells",
                            "textures/gui/fluidtank.png"))
            drawTexturedModalRect(posX + 2, posY + 1, 1, 1, 15,
                    72 - (73 * (fluid.amount.toFloat() / tank!!
                            .capacity)).toInt())
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceLocation(
                "extracells", "textures/gui/fluidtank.png"))
        drawTexturedModalRect(posX + 1, posY + 1, 19, 1, 16, 73)
        GL11.glEnable(GL11.GL_LIGHTING)
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

    fun drawDirectionTooltip(x: Int, y: Int) {
        val description: MutableList<String> = ArrayList()
        description.add(StatCollector
                .translateToLocal("extracells.tooltip.direction."
                        + direction.ordinal))
        if (tank == null || tank!!.fluid == null) {
            description.add(StatCollector
                    .translateToLocal("extracells.tooltip.empty1"))
        } else {
            if (tank!!.fluid.amount > 0
                    && tank!!.fluid.getFluid() != null) {
                val amountToText = tank!!.fluid.amount.toString() + "mB"
                description.add(tank!!.fluid.getFluid()
                        .getLocalizedName(tank!!.fluid))
                description.add(amountToText)
            }
        }
        drawHoveringText(description, x, y,
                Minecraft.getMinecraft().fontRenderer)
    }

    fun drawTooltip(x: Int, y: Int) {
        val description: MutableList<String> = ArrayList()
        if (tank == null || tank!!.fluid == null) {
            description.add(StatCollector
                    .translateToLocal("extracells.tooltip.empty1"))
        } else {
            if (tank!!.fluid.amount > 0
                    && tank!!.fluid.getFluid() != null) {
                val amountToText = tank!!.fluid.amount.toString() + "mB"
                description.add(tank!!.fluid.getFluid()
                        .getLocalizedName(tank!!.fluid))
                description.add(amountToText)
            }
        }
        drawHoveringText(description, x, y,
                Minecraft.getMinecraft().fontRenderer)
    }
}