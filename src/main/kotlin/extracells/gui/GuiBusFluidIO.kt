package extracells.gui

import appeng.api.AEApi
import appeng.api.config.RedstoneMode
import extracells.container.ContainerBusFluidIO
import extracells.gui.widget.WidgetRedstoneModes
import extracells.gui.widget.fluid.WidgetFluidSlot
import extracells.network.packet.other.IFluidSlotGui
import extracells.network.packet.part.PacketBusFluidIO
import extracells.part.PartFluidIO
import extracells.util.FluidUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid
import org.lwjgl.opengl.GL11
open class GuiBusFluidIO(_terminal: PartFluidIO, _player: EntityPlayer) : ECGuiContainer(
        ContainerBusFluidIO(_terminal, _player)), WidgetFluidSlot.IConfigurable, IFluidSlotGui {
    private val part: PartFluidIO
    private val player: EntityPlayer
    override var configState: Byte = 0
    private var redstoneControlled = false
    private val hasNetworkTool: Boolean
    public override fun actionPerformed(button: GuiButton) {
        super.actionPerformed(button)
        PacketBusFluidIO(player, button.id.toByte(), part)
                .sendPacketToServer()
    }

    fun changeConfig(_filterSize: Byte) {
        configState = _filterSize
    }

    override fun drawGuiContainerBackgroundLayer(alpha: Float, mouseX: Int,
                                                 mouseY: Int) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture)
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 176, 184)
        drawTexturedModalRect(guiLeft + 179, guiTop, 179, 0, 32, 86)
        if (hasNetworkTool) drawTexturedModalRect(guiLeft + 179, guiTop + 93, 178,
                93, 68, 68)
        for (s in inventorySlots.inventorySlots) {
            renderBackground(s as Slot?)
        }
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY)
        var overlayRendered = false
        for (i in 0..8) {
            fluidSlotList[i]?.drawWidget()
            if (!overlayRendered && fluidSlotList[i]?.canRender() == true) overlayRendered = renderOverlay(
                    fluidSlotList[i], mouseX, mouseY)
        }
        for (button in buttonList) {
            if (button is WidgetRedstoneModes) button.drawTooltip(mouseX, mouseY, (width - xSize) / 2,
                    (height - ySize) / 2)
        }
        showTooltipList(mouseX, mouseY)
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

    private fun isMouseOverSlot(p_146981_1_: Slot, p_146981_2_: Int, p_146981_3_: Int): Boolean {
        return func_146978_c(p_146981_1_.xDisplayPosition, p_146981_1_.yDisplayPosition, 16, 16, p_146981_2_,
                p_146981_3_)
    }

    protected fun isPointInRegion(top: Int, left: Int, height: Int, width: Int, pointX: Int, pointY: Int): Boolean {
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
        if (slot != null && slot.stack != null && slot.stack.isItemEqual(
                        AEApi.instance().definitions().items().networkTool().maybeStack(1).get())) return
        super.mouseClicked(mouseX, mouseY, mouseBtn)
        for (fluidSlot in fluidSlotList) {
            if (fluidSlot == null)
                continue
            if (isPointInRegion(fluidSlot.posX, fluidSlot.posY, 18, 18, mouseX, mouseY)) {
//				if((part instanceof PartGasImport || part instanceof PartGasExport) && Integration.Mods.MEKANISMGAS.isEnabled())
//					fluidSlot.mouseClickedGas(this.player.inventory.getItemStack());
//				else
                fluidSlot.mouseClicked(player.inventory.itemStack)
                break
            }
        }
    }

    private fun renderBackground(slot: Slot?) {
        if (slot!!.stack == null && (slot.slotNumber < 4 || slot.slotNumber > 39)) {
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f)
            mc.textureManager.bindTexture(ResourceLocation("appliedenergistics2", "textures/guis/states.png"))
            drawTexturedModalRect(guiLeft + slot.xDisplayPosition, guiTop + slot.yDisplayPosition, 240, 208, 16, 16)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LIGHTING)
        }
    }

    private fun renderOverlay(fluidSlot: WidgetFluidSlot?, mouseX: Int,
                              mouseY: Int): Boolean {
        if (fluidSlot == null)
            return false
        if (isPointInRegion(fluidSlot.posX, fluidSlot.posY, 18, 18, mouseX, mouseY)) {
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            drawGradientRect(fluidSlot.posX + 1, fluidSlot.posY + 1, fluidSlot.posX + 17, fluidSlot.posY + 17,
                    -0x7F000001, -0x7F000001)
            GL11.glEnable(GL11.GL_LIGHTING)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            return true
        }
        return false
    }

    fun setRedstoneControlled(_redstoneControlled: Boolean) {
        redstoneControlled = _redstoneControlled
        buttonList.clear()
        if (redstoneControlled) buttonList.add(WidgetRedstoneModes(0, guiLeft - 18, guiTop, 16, 16, part.redstoneMode))
    }

    fun shiftClick(itemStack: ItemStack?): Boolean {
        val containerFluid = FluidUtil.getFluidFromContainer(itemStack)
        val fluid = containerFluid?.getFluid()
        for (fluidSlot in fluidSlotList) {
            if (fluidSlot!!.canRender() && fluid != null && (fluidSlot.fluid == null || fluidSlot.fluid === fluid)) {
//				if((part instanceof PartGasImport || part instanceof PartGasExport) && Integration.Mods.MEKANISMGAS.isEnabled())
//					fluidSlot.mouseClickedGas(itemStack);
//				else
                fluidSlot.mouseClicked(itemStack)
                return true
            }
        }
        return false
    }

    override fun updateFluids(fluidList: List<Fluid?>?) {
        var i = 0
        while (i < fluidSlotList.size && i < fluidList!!.size) {
            fluidSlotList[i]?.fluid = fluidList[i]
            i++
        }
    }

    fun updateRedstoneMode(mode: RedstoneMode?) {
        if (redstoneControlled && buttonList.size > 0) (buttonList[0] as WidgetRedstoneModes).setRedstoneMode(mode)
    }

    companion object {
        private val guiTexture = ResourceLocation("extracells", "textures/gui/busiofluid.png")
    }

    init {
        (inventorySlots as ContainerBusFluidIO).setGui(this)
        part = _terminal
        player = _player
        fluidSlotList.add(WidgetFluidSlot(player, part, 0, 61, 21, this, 2.toByte()))
        fluidSlotList.add(WidgetFluidSlot(player, part, 1, 79, 21, this, 1.toByte()))
        fluidSlotList.add(WidgetFluidSlot(player, part, 2, 97, 21, this, 2.toByte()))
        fluidSlotList.add(WidgetFluidSlot(player, part, 3, 61, 39, this, 1.toByte()))
        fluidSlotList.add(WidgetFluidSlot(player, part, 4, 79, 39, this, 0.toByte()))
        fluidSlotList.add(WidgetFluidSlot(player, part, 5, 97, 39, this, 1.toByte()))
        fluidSlotList.add(WidgetFluidSlot(player, part, 6, 61, 57, this, 2.toByte()))
        fluidSlotList.add(WidgetFluidSlot(player, part, 7, 79, 57, this, 1.toByte()))
        fluidSlotList.add(WidgetFluidSlot(player, part, 8, 97, 57, this, 2.toByte()))
        PacketBusFluidIO(player, part).sendPacketToServer()
        hasNetworkTool = inventorySlots.inventory.size > 40
        xSize = if (hasNetworkTool) 246 else 211
        ySize = 184
    }
}