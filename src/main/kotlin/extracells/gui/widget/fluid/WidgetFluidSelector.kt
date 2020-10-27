package extracells.gui.widget.fluid

import appeng.api.storage.data.IAEFluidStack
import extracells.Extracells.shortenedBuckets
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraftforge.fluids.FluidStack
import org.lwjgl.opengl.GL11
import java.util.*
open class WidgetFluidSelector(guiFluidTerminal: IFluidSelectorGui,
                          stack: IAEFluidStack?) : AbstractFluidWidget(guiFluidTerminal, 18, 18,
        stack!!.fluidStack.getFluid()) {
    var amount: Long = 0
    private val color: Int
    private val borderThickness: Int
    private fun drawHollowRectWithCorners(posX: Int, posY: Int, height: Int,
                                          width: Int, color: Int, thickness: Int) {
        drawRect(posX, posY, posX + height, posY + thickness, color)
        drawRect(posX, posY + width - thickness, posX + height, posY + width,
                color)
        drawRect(posX, posY, posX + thickness, posY + width, color)
        drawRect(posX + height - thickness, posY, posX + height, posY + width,
                color)
        drawRect(posX, posY, posX + thickness + 1, posY + thickness + 1, color)
        drawRect(posX + height, posY + width, posX + height - thickness - 1,
                posY + width - thickness - 1, color)
        drawRect(posX + height, posY, posX + height - thickness - 1, posY
                + thickness + 1, color)
        drawRect(posX, posY + width, posX + thickness + 1, posY + width - thickness - 1, color)
    }

    override fun drawTooltip(posX: Int, posY: Int, mouseX: Int, mouseY: Int): Boolean {
        if (fluid == null || amount <= 0 || !isPointInRegion(posX, posY, height, width,
                        mouseX, mouseY)) return false
        var amountToText = amount.toString() + "mB"
        if (shortenedBuckets) {
            if (amount > 1000000000L) amountToText = (amount / 1000000000L)
                    .toString() + "MegaB" else if (amount > 1000000L) amountToText = (amount / 1000000L).toString() + "KiloB" else if (amount > 9999L) {
                amountToText = (amount / 1000L).toString() + "B"
            }
        }
        val description: MutableList<String?> = ArrayList()
        description.add(fluid.getLocalizedName(FluidStack(fluid, 0)))
        description.add(amountToText)
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
        val terminalFluid = (guiFluidTerminal as IFluidSelectorGui).currentFluid
        val currentFluid = terminalFluid?.fluid
        if (fluid != null && fluid.icon != null) {
            GL11.glColor3f((fluid.color shr 16 and 0xFF) / 255.0f, (fluid.color shr 8 and 0xFF) / 255.0f,
                    (fluid.color and 0xFF) / 255.0f)
            drawTexturedModelRectFromIcon(posX + 1, posY + 1,
                    fluid.icon, height - 2, width - 2)
        }
        GL11.glColor3f(1f, 1f, 1f)
        if (fluid === currentFluid) drawHollowRectWithCorners(posX, posY, height, width,
                color, borderThickness)
        GL11.glEnable(GL11.GL_LIGHTING)
        GL11.glDisable(GL11.GL_BLEND)
    }

    override fun mouseClicked(posX: Int, posY: Int, mouseX: Int, mouseY: Int) {
        if (fluid != null
                && isPointInRegion(posX, posY, height, width, mouseX,
                        mouseY)) {
            (guiFluidTerminal as IFluidSelectorGui).container
                    .setSelectedFluid(fluid)
        }
    }

    init {
        amount = stack!!.stackSize
        color = -0xff0001
        borderThickness = 1
    }
}