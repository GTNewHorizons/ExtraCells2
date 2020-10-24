package extracells.gui.widget.fluid

import extracells.network.packet.other.IFluidSlotPartOrBlock
import extracells.network.packet.other.PacketFluidSlot
import extracells.util.FluidUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import java.util.*

class WidgetFluidSlot @JvmOverloads constructor(private val player: EntityPlayer?, private val part: IFluidSlotPartOrBlock?,
                                                private val id: Int, val posX: Int, val posY: Int, private val configurable: IConfigurable? = null,
                                                private val configOption: Byte = 0.toByte()) : Gui() {
    interface IConfigurable {
        val configState: Byte
    }

    //	@Optional.Method(modid = "MekanismAPI|gas")
    //	public void mouseClickedGas(ItemStack stack) {
    //		GasStack gasStack = GasUtil.getGasFromContainer(stack);
    //		FluidStack fluidStack = GasUtil.getFluidStack(gasStack);
    //		this.fluid = fluidStack == null ? null : fluidStack.getFluid();
    //		new PacketFluidSlot(this.part, this.id, this.fluid, this.player).sendPacketToServer();
    //	}
    var fluid: Fluid? = null

    constructor(_player: EntityPlayer?, _part: IFluidSlotPartOrBlock?,
                _posX: Int, _posY: Int) : this(_player, _part, 0, _posX, _posY, null, 0.toByte()) {
    }

    fun canRender(): Boolean {
        return (configurable == null
                || configurable.configState >= configOption)
    }

    protected fun drawHoveringText(list: List<*>, x: Int, y: Int,
                                   fontrenderer: FontRenderer) {
        val lighting_enabled = GL11.glIsEnabled(GL11.GL_LIGHTING)
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
            if (lighting_enabled) GL11.glEnable(GL11.GL_LIGHTING)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            RenderHelper.enableStandardItemLighting()
            GL11.glEnable(GL12.GL_RESCALE_NORMAL)
        }
    }

    fun drawTooltip(x: Int, y: Int) {
        if (canRender()) {
            if (fluid != null) {
                val description: MutableList<String> = ArrayList()
                description.add(fluid!!.getLocalizedName(FluidStack(fluid, 0)))
                drawHoveringText(description, x, y, Minecraft.getMinecraft().fontRenderer)
            }
        }
    }

    fun drawWidget() {
        if (!canRender()) return
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glColor3f(1f, 1f, 1f)
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glColor3f(1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture)
        drawTexturedModalRect(posX, posY, 79, 39, 18, 18)
        GL11.glEnable(GL11.GL_LIGHTING)
        if (fluid == null || fluid!!.icon == null) return
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture)
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glColor3f(1.0f, 1.0f, 1.0f)
        GL11.glColor3f((fluid!!.color shr 16 and 0xFF) / 255.0f, (fluid!!.color shr 8 and 0xFF) / 255.0f,
                (fluid!!.color and 0xFF) / 255.0f)
        drawTexturedModelRectFromIcon(posX + 1, posY + 1,
                fluid!!.icon, 16, 16)
        GL11.glColor3f(1.0f, 1.0f, 1.0f)
        GL11.glEnable(GL11.GL_LIGHTING)
        GL11.glDisable(GL11.GL_BLEND)
    }

    fun mouseClicked(stack: ItemStack?) {
        val fluidStack = FluidUtil.getFluidFromContainer(stack)
        fluid = fluidStack?.getFluid()
        PacketFluidSlot(part, id, fluid, player).sendPacketToServer()
    }

    companion object {
        private val guiTexture = ResourceLocation(
                "extracells", "textures/gui/busiofluid.png")
    }
}