package extracells.gui

import cpw.mods.fml.relauncher.Side
import extracells.container.ContainerOreDictExport
import extracells.network.packet.part.PacketOreDictExport
import extracells.part.PartOreDictExporter
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StatCollector
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
open class GuiOreDictExport(private val player: EntityPlayer, _part: PartOreDictExporter) : GuiContainer(
        ContainerOreDictExport(
                player, _part)) {
    private val guiTexture = ResourceLocation("extracells",
            "textures/gui/oredictexport.png")
    private var searchbar: GuiTextField? = null
    override fun actionPerformed(guibutton: GuiButton) {
        PacketOreDictExport(player, filter, Side.SERVER)
                .sendPacketToServer()
    }

    override fun drawGuiContainerBackgroundLayer(f: Float, mouseX: Int,
                                                 mouseY: Int) {
        drawDefaultBackground()
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture)
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize,
                ySize)
        searchbar!!.drawTextBox()
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY)
        fontRendererObj.drawString(
                StatCollector.translateToLocal(
                        "extracells.part.oredict.export.name").replace("ME ",
                        ""), 8, 5, 0x000000)
        fontRendererObj.drawString(
                StatCollector.translateToLocal("container.inventory"), 8,
                ySize - 94, 0x000000)
    }

    override fun initGui() {
        super.initGui()
        guiLeft = (width - xSize) / 2
        guiTop = (height - ySize) / 2
        buttonList.add(GuiButton(1,
                guiLeft + xSize / 2 - 44, guiTop + 35, 88, 20,
                StatCollector.translateToLocal("extracells.tooltip.save")))
        searchbar = object : GuiTextField(fontRendererObj, guiLeft
                + xSize / 2 - 75, guiTop + 20, 150, 10) {
            private val xPos = 0
            private val yPos = 0
            private val width1 = 0
            private val height1 = 0
            override fun mouseClicked(x: Int, y: Int, mouseBtn: Int) {
                val flag = x >= xPos && x < xPos + this.width1 && y >= yPos && y < yPos + this.height1
                if (flag && mouseBtn == 3) text = ""
            }
        }
        (searchbar as GuiTextField).enableBackgroundDrawing = true
        (searchbar as GuiTextField).isFocused = true
        (searchbar as GuiTextField).maxStringLength = 128
        (searchbar as GuiTextField).text = filter
    }

    override fun keyTyped(key: Char, keyID: Int) {
        if (keyID == Keyboard.KEY_ESCAPE) mc.thePlayer.closeScreen()
        searchbar!!.textboxKeyTyped(key, keyID)
        filter = searchbar!!.text
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseBtn: Int) {
        super.mouseClicked(mouseX, mouseY, mouseBtn)
        searchbar!!.mouseClicked(mouseX, mouseY, mouseBtn)
    }

    companion object {
        fun updateFilter(_filter: String?) {
            if (_filter != null) {
                filter = _filter
            }
            val gui: Gui? = Minecraft.getMinecraft().currentScreen
            if (gui != null && gui is GuiOreDictExport) {
                val g = gui
                if (g.searchbar != null) g.searchbar!!.text = filter
            }
        }

        private var filter: String = ""
    }
}