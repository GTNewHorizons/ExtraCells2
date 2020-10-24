package extracells.gui

import appeng.api.config.RedstoneMode
import extracells.container.ContainerFluidEmitter
import extracells.gui.widget.DigitTextField
import extracells.gui.widget.WidgetRedstoneModes
import extracells.gui.widget.fluid.WidgetFluidSlot
import extracells.network.packet.other.IFluidSlotGui
import extracells.network.packet.part.PacketFluidEmitter
import extracells.part.PartFluidLevelEmitter
import extracells.registries.PartEnum
import extracells.util.GuiUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11

class GuiFluidEmitter(private val part: PartFluidLevelEmitter, private val player: EntityPlayer) : ECGuiContainer(
        ContainerFluidEmitter(
                part, player)), IFluidSlotGui {
    private var amountField: DigitTextField? = null
    private val guiTexture = ResourceLocation("extracells", "textures/gui/levelemitterfluid.png")
    public override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> modifyAmount(-1)
            1 -> modifyAmount(-10)
            2 -> modifyAmount(-100)
            3 -> modifyAmount(+1)
            4 -> modifyAmount(+10)
            5 -> modifyAmount(+100)
            6 -> PacketFluidEmitter(true, part, player).sendPacketToServer()
        }
    }

    override fun drawGuiContainerBackgroundLayer(f: Float, i: Int, j: Int) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture)
        val posX = (width - Companion.xSize) / 2
        val posY = (height - Companion.ySize) / 2
        drawTexturedModalRect(posX, posY, 0, 0, Companion.xSize, Companion.ySize)
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        fontRendererObj.drawString(PartEnum.FLUIDLEVELEMITTER.statName, 5, 5, 0x000000)
        fluidSlot!!.drawWidget()
        (buttonList[6] as WidgetRedstoneModes).drawTooltip(mouseX, mouseY, (width - Companion.xSize) / 2,
                (height - Companion.ySize) / 2)
        GuiUtil.renderOverlay(zLevel, guiLeft, guiTop, fluidSlot, mouseX, mouseY)
        showTooltip(mouseX, mouseY)
    }

    override fun drawScreen(x: Int, y: Int, f: Float) {
        val buttonNames = arrayOf("-1", "-10", "-100", "+1", "+10", "+100")
        val shiftNames = arrayOf("-100", "-1000", "-10000", "+100", "+1000", "+10000")
        for (i in buttonList.indices) {
            if (i == 6) break
            val currentButton = buttonList[i] as GuiButton
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                currentButton.displayString = shiftNames[i] + "mB"
            } else {
                currentButton.displayString = buttonNames[i] + "mB"
            }
        }
        super.drawScreen(x, y, f)
        amountField!!.drawTextBox()
    }

    override fun initGui() {
        val posX = (width - Companion.xSize) / 2
        val posY = (height - Companion.ySize) / 2
        amountField = DigitTextField(fontRendererObj, posX + 10, posY + 40, 59, 10)
        amountField!!.isFocused = true
        amountField!!.enableBackgroundDrawing = false
        amountField!!.setTextColor(0xFFFFFF)
        buttonList.clear()
        buttonList.add(GuiButton(0, posX + 65 - 46, posY + 8 + 6, 42, 20, "-1"))
        buttonList.add(GuiButton(1, posX + 115 - 46, posY + 8 + 6, 42, 20, "-10"))
        buttonList.add(GuiButton(2, posX + 165 - 46, posY + 8 + 6, 42, 20, "-100"))
        buttonList.add(GuiButton(3, posX + 65 - 46, posY + 58 - 2, 42, 20, "+1"))
        buttonList.add(GuiButton(4, posX + 115 - 46, posY + 58 - 2, 42, 20, "+10"))
        buttonList.add(GuiButton(5, posX + 165 - 46, posY + 58 - 2, 42, 20, "+100"))
        buttonList.add(WidgetRedstoneModes(6, posX + 120, posY + 36, 16, 16, RedstoneMode.LOW_SIGNAL, true))
        super.initGui()
    }

    override fun keyTyped(key: Char, keyID: Int) {
        super.keyTyped(key, keyID)
        if ("0123456789".contains(key.toString()) || keyID == Keyboard.KEY_BACK) {
            amountField!!.textboxKeyTyped(key, keyID)
            PacketFluidEmitter(amountField!!.text, part, player).sendPacketToServer()
        }
    }

    private fun modifyAmount(amount: Int) {
        var amount = amount
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) amount *= 100
        PacketFluidEmitter(amount, part, player).sendPacketToServer()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseBtn: Int) {
        super.mouseClicked(mouseX, mouseY, mouseBtn)
        if (GuiUtil.isPointInRegion(guiLeft.toFloat(), guiTop, fluidSlot.posX, fluidSlot.posY, 18, 18, mouseX,
                        mouseY)) {
//			if(part instanceof PartGasLevelEmitter && Integration.Mods.MEKANISMGAS.isEnabled())
//				this.fluidSlot.mouseClickedGas(this.player.inventory.getItemStack());
//			else
            fluidSlot!!.mouseClicked(player.inventory.itemStack)
        }
    }

    fun setAmountField(amount: Long) {
        amountField!!.text = java.lang.Long.toString(amount)
    }

    fun setRedstoneMode(mode: RedstoneMode?) {
        (buttonList[6] as WidgetRedstoneModes).setRedstoneMode(mode)
    }

    override fun updateFluids(_fluids: List<Fluid?>?) {
        if (_fluids == null || _fluids.isEmpty()) {
            fluidSlot.fluid = null
            return
        }
        fluidSlot.fluid = _fluids[0]
    }

    companion object {
        const val xSize = 176
        const val ySize = 166
    }

    init {
        fluidSlot = WidgetFluidSlot(player, part, 79, 36)
        PacketFluidEmitter(false, part, player).sendPacketToServer()
    }
}