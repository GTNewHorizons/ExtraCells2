package extracells.gui.widget

import extracells.gui.widget.fluid.WidgetFluidSlot
import extracells.network.packet.other.PacketFluidContainerSlot
import extracells.tileentity.TileEntityFluidFiller
import extracells.util.FluidUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.entity.RenderItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
open class WidgetSlotFluidContainer(private val player: EntityPlayer,
                               private val fluidFiller: TileEntityFluidFiller, val posX: Int, val posY: Int) : Gui() {
    private val configurable: WidgetFluidSlot.IConfigurable? = null
    private val configOption: Byte = 0
    fun canRender(): Boolean {
        return (configurable == null
                || configurable.configState >= configOption)
    }

    fun drawTooltip() {
        if (canRender()) {
        }
    }

    fun drawWidget() {
        val container = fluidFiller.containerItem
        GL11.glTranslatef(0.0f, 0.0f, 32.0f)
        zLevel = 100.0f
        val itemRender = RenderItem.getInstance()
        itemRender.zLevel = 100.0f
        var font: FontRenderer? = null
        if (container != null) font = container.item.getFontRenderer(container)
        if (font == null) font = Minecraft.getMinecraft().fontRenderer
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        itemRender.renderItemAndEffectIntoGUI(font, Minecraft.getMinecraft()
                .textureManager, container, posX, posY)
        // itemRender.renderItemOverlayIntoGUI(font,
        // Minecraft.getMinecraft().getTextureManager(), container, posX + 1,
        // posY - 7, null);
        zLevel = 0.0f
        itemRender.zLevel = 0.0f
    }

    fun drawWidgetWithRect(i: Int, j: Int) {
        val container = fluidFiller.containerItem
        GL11.glTranslatef(0.0f, 0.0f, 32.0f)
        zLevel = 100.0f
        val itemRender = RenderItem.getInstance()
        itemRender.zLevel = 100.0f
        var font: FontRenderer? = null
        if (container != null) font = container.item.getFontRenderer(container)
        if (font == null) font = Minecraft.getMinecraft().fontRenderer
        drawRect(i, j, i + 16, j + 16, -2130706433)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        itemRender.renderItemAndEffectIntoGUI(font, Minecraft.getMinecraft()
                .textureManager, container, posX, posY)
        // itemRender.renderItemOverlayIntoGUI(font,
        // Minecraft.getMinecraft().getTextureManager(), container, posX + 1,
        // posY - 7, null);
        zLevel = 0.0f
        itemRender.zLevel = 0.0f
    }

    fun mouseClicked(stack: ItemStack?) {
        if (stack != null && stack.item != null && FluidUtil.isEmpty(stack)) PacketFluidContainerSlot(fluidFiller,
                stack, player)
                .sendPacketToServer()
    }

    companion object {
        private val guiTexture = ResourceLocation(
                "extracells", "textures/gui/busiofluid.png")
    }
}