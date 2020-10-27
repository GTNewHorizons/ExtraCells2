package extracells.gui

import appeng.api.storage.data.IAEFluidStack
import extracells.Extracells.shortenedBuckets
import extracells.api.ECApi
import extracells.container.ContainerFluidTerminal
import extracells.gui.widget.FluidWidgetComparator
import extracells.gui.widget.fluid.AbstractFluidWidget
import extracells.gui.widget.fluid.IFluidSelectorContainer
import extracells.gui.widget.fluid.IFluidSelectorGui
import extracells.gui.widget.fluid.WidgetFluidSelector
import extracells.network.packet.part.PacketFluidTerminal
import extracells.part.PartFluidTerminal
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StatCollector
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.util.*
open class GuiFluidTerminal(_terminal: PartFluidTerminal?, _player: EntityPlayer) : GuiContainer(
        ContainerFluidTerminal(_terminal, _player)), IFluidSelectorGui {
    val terminal: PartFluidTerminal?
    private val player: EntityPlayer
    private var currentScroll = 0
    private var searchbar: GuiTextField? = null
    private var fluidWidgets: MutableList<AbstractFluidWidget> = ArrayList()
    private val guiTexture = ResourceLocation("extracells", "textures/gui/terminalfluid.png")
    override var currentFluid: IAEFluidStack? = null
    private val containerTerminalFluid: ContainerFluidTerminal
    override fun drawGuiContainerBackgroundLayer(alpha: Float, sizeX: Int, sizeY: Int) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture)
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize)
        searchbar!!.drawTextBox()
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        fontRendererObj.drawString(
                StatCollector.translateToLocal("extracells.part.fluid.terminal.name").replace("ME ", ""), 9, 6,
                0x000000)
        drawWidgets(mouseX, mouseY)
        if (currentFluid != null) {
            val currentFluidAmount = currentFluid!!.stackSize
            var amountToText = currentFluidAmount.toString() + "mB"
            if (shortenedBuckets) {
                if (currentFluidAmount > 1000000000L) amountToText = (currentFluidAmount / 1000000000L).toString() + "MegaB" else if (currentFluidAmount > 1000000L) amountToText = (currentFluidAmount / 1000000L).toString() + "KiloB" else if (currentFluidAmount > 9999L) {
                    amountToText = (currentFluidAmount / 1000L).toString() + "B"
                }
            }
            fontRendererObj.drawString(
                    StatCollector.translateToLocal("extracells.tooltip.amount") + ": " + amountToText, 45, 91, 0x000000)
            fontRendererObj.drawString(
                    StatCollector.translateToLocal(
                            "extracells.tooltip.fluid") + ": " + currentFluid!!.fluid.getLocalizedName(
                            currentFluid!!.fluidStack), 45, 101, 0x000000)
        }
    }

    fun drawWidgets(mouseX: Int, mouseY: Int) {
        val listSize = fluidWidgets.size
        if (!containerTerminalFluid.fluidStackList.isEmpty) {
            outerLoop@ for (y in 0..3) {
                for (x in 0..8) {
                    val widgetIndex = y * 9 + x + currentScroll * 9
                    if (0 <= widgetIndex && widgetIndex < listSize) {
                        val widget = fluidWidgets[widgetIndex]
                        widget.drawWidget(x * 18 + 7, y * 18 + 17)
                    } else {
                        break@outerLoop
                    }
                }
            }
            for (x in 0..8) {
                for (y in 0..3) {
                    val widgetIndex = y * 9 + x + currentScroll * 9
                    if (0 <= widgetIndex && widgetIndex < listSize) {
                        if (fluidWidgets[widgetIndex].drawTooltip(x * 18 + 7, y * 18 - 1, mouseX, mouseY)) break
                    } else {
                        break
                    }
                }
            }
            val deltaWheel = Mouse.getDWheel()
            if (deltaWheel > 0) {
                currentScroll--
            } else if (deltaWheel < 0) {
                currentScroll++
            }
            if (currentScroll < 0) currentScroll = 0
            val maxLine = if (listSize % 9 == 0) listSize / 9 else listSize / 9 + 1
            if (currentScroll > maxLine - 4) {
                currentScroll = Math.max(maxLine - 4, 0)
            }
        }
    }

    override val container: IFluidSelectorContainer
        get() = containerTerminalFluid

    override fun guiLeft(): Int {
        return guiLeft
    }

    override fun guiTop(): Int {
        return guiTop
    }

    override fun initGui() {
        super.initGui()
        Mouse.getDWheel()
        updateFluids()
        Collections.sort(fluidWidgets, FluidWidgetComparator())
        searchbar = object : GuiTextField(fontRendererObj,
                guiLeft + 81, guiTop + 6, 88, 10) {
            private val xPos1 = 0
            private val yPos1 = 0
            private val width1 = 0
            private val height1 = 0
            override fun mouseClicked(x: Int, y: Int, mouseBtn: Int) {
                val flag = x >= xPos1 && x < xPos1 + this.width1 && y >= yPos1 && y < yPos1 + this.height
                if (flag && mouseBtn == 3) text = ""
            }
        }
        (searchbar as GuiTextField).enableBackgroundDrawing = false
        (searchbar as GuiTextField).isFocused = true
        (searchbar as GuiTextField).maxStringLength = 15
    }

    override fun keyTyped(key: Char, keyID: Int) {
        if (keyID == Keyboard.KEY_ESCAPE) mc.thePlayer.closeScreen()
        searchbar!!.textboxKeyTyped(key, keyID)
        updateFluids()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseBtn: Int) {
        super.mouseClicked(mouseX, mouseY, mouseBtn)
        searchbar!!.mouseClicked(mouseX, mouseY, mouseBtn)
        val listSize = fluidWidgets.size
        for (x in 0..8) {
            for (y in 0..3) {
                val index = y * 9 + x + currentScroll * 9
                if (0 <= index && index < listSize) {
                    val widget = fluidWidgets[index]
                    widget.mouseClicked(x * 18 + 7, y * 18 - 1, mouseX, mouseY)
                }
            }
        }
    }

    fun updateFluids() {
        fluidWidgets = ArrayList()
        for (fluidStack in containerTerminalFluid.fluidStackList) {
            if (fluidStack!!.fluid.getLocalizedName(fluidStack.fluidStack).toLowerCase().contains(
                            searchbar!!.text.toLowerCase()) && ECApi.instance()!!.canFluidSeeInTerminal(
                            fluidStack.fluid)) {
                fluidWidgets.add(WidgetFluidSelector(this, fluidStack))
            }
        }
        updateSelectedFluid()
    }

    fun updateSelectedFluid() {
        currentFluid = null
        for (stack in containerTerminalFluid.fluidStackList) {
            if (stack!!.fluid === containerTerminalFluid.selectedFluid) currentFluid = stack
        }
    }

    init {
        containerTerminalFluid = inventorySlots as ContainerFluidTerminal
        containerTerminalFluid.setGui(this)
        terminal = _terminal
        player = _player
        xSize = 176
        ySize = 204
        PacketFluidTerminal(player, terminal).sendPacketToServer()
    }
}