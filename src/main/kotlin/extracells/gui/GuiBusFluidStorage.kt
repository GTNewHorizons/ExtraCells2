package extracells.gui

import appeng.api.AEApi
import appeng.api.config.AccessRestriction
import extracells.container.ContainerBusFluidStorage
import extracells.gui.widget.WidgetStorageDirection
import extracells.gui.widget.fluid.WidgetFluidSlot
import extracells.network.packet.other.IFluidSlotGui
import extracells.network.packet.part.PacketBusFluidStorage
import extracells.part.PartFluidStorage
import extracells.util.FluidUtil
import extracells.util.GuiUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid
import org.lwjgl.opengl.GL11

class GuiBusFluidStorage(private val part: PartFluidStorage, _player: EntityPlayer) : ECGuiContainer(
        ContainerBusFluidStorage(
                part, _player)), WidgetFluidSlot.IConfigurable, IFluidSlotGui {
    private val player: EntityPlayer
    override var configState: Byte = 0
        private set
    private val hasNetworkTool: Boolean
    public override fun actionPerformed(button: GuiButton) {
        super.actionPerformed(button)
        if (button is WidgetStorageDirection) {
            when (button.accessRestriction) {
                AccessRestriction.NO_ACCESS -> PacketBusFluidStorage(
                        player, AccessRestriction.READ, false).sendPacketToServer()
                AccessRestriction.READ -> PacketBusFluidStorage(player, AccessRestriction.READ_WRITE,
                        false).sendPacketToServer()
                AccessRestriction.READ_WRITE -> PacketBusFluidStorage(player, AccessRestriction.WRITE,
                        false).sendPacketToServer()
                AccessRestriction.WRITE -> PacketBusFluidStorage(player, AccessRestriction.NO_ACCESS,
                        false).sendPacketToServer()
                else -> {
                }
            }
        }
    }

    fun changeConfig(_filterSize: Byte) {
        configState = _filterSize
    }

    override fun drawGuiContainerBackgroundLayer(alpha: Float, mouseX: Int, mouseY: Int) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture)
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 176, 222)
        drawTexturedModalRect(guiLeft + 179, guiTop, 179, 0, 32, 86)
        if (hasNetworkTool) drawTexturedModalRect(guiLeft + 179, guiTop + 93, 178, 93, 68, 68)
        for (s in inventorySlots.inventorySlots) {
            renderBackground(s as Slot?)
        }
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY)
        var overlayRendered = false
        for (i in 0..53) {
            fluidSlotList[i].drawWidget()
            if (!overlayRendered && fluidSlotList[i].canRender()) overlayRendered = GuiUtil.renderOverlay(
                    zLevel, guiLeft, guiTop, fluidSlotList[i], mouseX, mouseY)
        }
        for (button in buttonList) {
            if (button is WidgetStorageDirection) button.drawTooltip(mouseX, mouseY, (width - xSize) / 2,
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

    override fun initGui() {
        super.initGui()
        buttonList.add(WidgetStorageDirection(0, guiLeft - 18, guiTop, 16, 16, AccessRestriction.READ_WRITE))
    }

    private fun isMouseOverSlot(p_146981_1_: Slot, p_146981_2_: Int, p_146981_3_: Int): Boolean {
        return func_146978_c(p_146981_1_.xDisplayPosition, p_146981_1_.yDisplayPosition, 16, 16, p_146981_2_,
                p_146981_3_)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseBtn: Int) {
        val slot = getSlotAtPosition(mouseX, mouseY)
        if (slot != null && slot.stack != null && AEApi.instance().definitions().items().networkTool().isSameAs(
                        slot.stack)) return
        super.mouseClicked(mouseX, mouseY, mouseBtn)
        for (fluidSlot in fluidSlotList) {
            if (GuiUtil.isPointInRegion(guiLeft.toFloat(), guiTop, fluidSlot.posX, fluidSlot.posY, 18, 18, mouseX,
                            mouseY)) {
//				if(part instanceof PartGasStorage && Integration.Mods.MEKANISMGAS.isEnabled())
//					fluidSlot.mouseClickedGas(this.player.inventory.getItemStack());
//				else
                fluidSlot!!.mouseClicked(player.inventory.itemStack)
                break
            }
        }
    }

    private fun renderBackground(slot: Slot?) {
        if (slot!!.stack == null && (slot.slotNumber == 0 || slot.slotNumber > 36)) {
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f)
            mc.textureManager.bindTexture(ResourceLocation("appliedenergistics2", "textures/guis/states.png"))
            drawTexturedModalRect(guiLeft + slot.xDisplayPosition, guiTop + slot.yDisplayPosition, 240, 208, 16, 16)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LIGHTING)
        }
    }

    fun shiftClick(itemStack: ItemStack?) {
        val containerFluid = FluidUtil.getFluidFromContainer(itemStack)
        val fluid = containerFluid?.getFluid()
        for (fluidSlot in fluidSlotList) {
            if (fluidSlot.fluid == null || fluid != null && fluidSlot.fluid === fluid) {
//				if(part instanceof PartGasStorage && Integration.Mods.MEKANISMGAS.isEnabled())
//					fluidSlot.mouseClickedGas(itemStack);
//				else
                fluidSlot!!.mouseClicked(itemStack)
                return
            }
        }
    }

    fun updateAccessRestriction(mode: AccessRestriction?) {
        if (buttonList.size > 0) (buttonList[0] as WidgetStorageDirection).accessRestriction = mode
    }

    override fun updateFluids(fluidList: List<Fluid?>?) {
        var i = 0
        while (i < fluidSlotList.size && i < fluidList!!.size) {
            fluidSlotList[i].fluid = fluidList[i]
            i++
        }
    }

    companion object {
        private val guiTexture = ResourceLocation("extracells", "textures/gui/storagebusfluid.png")
    }

    init {
        (inventorySlots as ContainerBusFluidStorage).setGui(this)
        player = _player
        for (i in 0..8) {
            for (j in 0..5) {
                fluidSlotList.add(WidgetFluidSlot(player, part, i * 6 + j, 18 * i + 7, 18 * j + 17))
            }
        }
        PacketBusFluidStorage(player, part).sendPacketToServer()
        hasNetworkTool = inventorySlots.inventory.size > 40
        xSize = if (hasNetworkTool) 246 else 211
        ySize = 222
    }
}