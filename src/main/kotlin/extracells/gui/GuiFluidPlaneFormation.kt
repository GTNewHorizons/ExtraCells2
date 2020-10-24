package extracells.gui

import appeng.api.AEApi
import extracells.container.ContainerPlaneFormation
import extracells.gui.widget.WidgetRedstoneModes
import extracells.gui.widget.fluid.WidgetFluidSlot
import extracells.network.packet.other.IFluidSlotGui
import extracells.network.packet.part.PacketFluidPlaneFormation
import extracells.part.PartFluidPlaneFormation
import extracells.util.FluidUtil
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid
import org.lwjgl.opengl.GL11

class GuiFluidPlaneFormation(_part: PartFluidPlaneFormation,
                             _player: EntityPlayer) : ECGuiContainer(
        ContainerPlaneFormation(_part, _player)), IFluidSlotGui {
    private val part: PartFluidPlaneFormation
    private val player: EntityPlayer
    private val hasNetworkTool: Boolean
    override fun drawGuiContainerBackgroundLayer(alpha: Float, mouseX: Int,
                                                 mouseY: Int) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture)
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 176, 184)
        drawTexturedModalRect(guiLeft + 179, guiTop, 179, 0, 32, 86)
        if (hasNetworkTool) drawTexturedModalRect(guiLeft + 179, guiTop + 93, 178,
                93, 68, 68)
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY)
        fluidSlot!!.drawWidget()
        renderOverlay(fluidSlot, mouseX, mouseY)
        for (button in buttonList) {
            if (button is WidgetRedstoneModes) button.drawTooltip(guiLeft,
                    guiTop, (width - xSize) / 2,
                    (height - ySize) / 2)
        }
        showTooltip(mouseX, mouseY)
    }

    protected fun getSlotAtPosition(p_146975_1_: Int, p_146975_2_: Int): Slot? {
        for (k in inventorySlots.inventorySlots.indices) {
            val slot = inventorySlots.inventorySlots[k] as Slot
            if (isMouseOverSlot(slot, p_146975_1_, p_146975_2_)) {
                return slot
            }
        }
        return null
    }

    private fun isMouseOverSlot(p_146981_1_: Slot, p_146981_2_: Int,
                                p_146981_3_: Int): Boolean {
        return func_146978_c(p_146981_1_.xDisplayPosition,
                p_146981_1_.yDisplayPosition, 16, 16, p_146981_2_, p_146981_3_)
    }

    protected fun isPointInRegion(top: Int, left: Int, height: Int, width: Int,
                                  pointX: Int, pointY: Int): Boolean {
        var pointX = pointX
        var pointY = pointY
        val k1 = guiLeft
        val l1 = guiTop
        pointX -= k1
        pointY -= l1
        return pointX >= top - 1 && pointX < top + height + 1 && pointY >= left - 1 && pointY < left + width + 1
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseBtn: Int) {
        val slot = getSlotAtPosition(mouseX, mouseY)
        if (slot != null && slot.stack != null && AEApi.instance().definitions().items().networkTool().isSameAs(
                        slot.stack)) return
        super.mouseClicked(mouseX, mouseY, mouseBtn)
        if (isPointInRegion(fluidSlot.posX, fluidSlot.posY,
                        18, 18, mouseX, mouseY)) fluidSlot!!.mouseClicked(player.inventory.itemStack)
    }

    fun renderOverlay(fluidSlot: WidgetFluidSlot?, mouseX: Int,
                      mouseY: Int): Boolean {
        if (isPointInRegion(fluidSlot.getPosX(), fluidSlot.getPosY(), 18, 18,
                        mouseX, mouseY)) {
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            drawGradientRect(fluidSlot.getPosX() + 1, fluidSlot.getPosY() + 1,
                    fluidSlot.getPosX() + 17, fluidSlot.getPosY() + 17,
                    -0x7F000001, -0x7F000001)
            GL11.glEnable(GL11.GL_LIGHTING)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            return true
        }
        return false
    }

    fun shiftClick(itemStack: ItemStack?) {
        val containerFluid = FluidUtil.getFluidFromContainer(itemStack)
        val fluid = containerFluid?.getFluid()
        if (fluidSlot.fluid == null || fluid != null
                && fluidSlot.fluid === fluid) fluidSlot!!.mouseClicked(itemStack)
    }

    override fun updateFluids(fluidList: List<Fluid?>?) {
        fluidSlot.fluid = fluidList!![0]
    }

    companion object {
        private val guiTexture = ResourceLocation(
                "extracells", "textures/gui/paneformation.png")
    }

    init {
        (inventorySlots as ContainerPlaneFormation).setGui(this)
        part = _part
        player = _player
        fluidSlot = WidgetFluidSlot(player, part, 0, 79, 39)
        PacketFluidPlaneFormation(player, part)
                .sendPacketToServer()
        hasNetworkTool = inventorySlots.inventory.size > 40
        xSize = if (hasNetworkTool) 246 else 211
        ySize = 184
    }
}