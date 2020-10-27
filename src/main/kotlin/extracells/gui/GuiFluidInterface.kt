package extracells.gui

import appeng.api.implementations.ICraftingPatternItem
import extracells.api.IFluidInterface
import extracells.container.ContainerFluidInterface
import extracells.gui.widget.WidgetFluidTank
import extracells.gui.widget.fluid.WidgetFluidSlot
import extracells.network.packet.other.IFluidSlotPartOrBlock
import extracells.registries.BlockEnum
import extracells.util.GuiUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.entity.RenderItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StatCollector
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
open class GuiFluidInterface(player: EntityPlayer, fluidInterface: IFluidInterface) : ECGuiContainer(
        ContainerFluidInterface(player, fluidInterface)) {
    var fluidInterface: IFluidInterface
    var tanks = arrayOfNulls<WidgetFluidTank>(6)
    var filter = arrayOfNulls<WidgetFluidSlot>(6)
    private val guiTexture = ResourceLocation("extracells",
            "textures/gui/interfacefluid.png")
    private val player: EntityPlayer
    private var partSide: ForgeDirection? = ForgeDirection.UNKNOWN

    constructor(player: EntityPlayer,
                fluidInterface: IFluidInterface, side: ForgeDirection?) : this(player, fluidInterface) {
        partSide = side
    }

    override fun drawGuiContainerBackgroundLayer(f: Float, mouseX: Int,
                                                 mouseY: Int) {
        drawDefaultBackground()
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture)
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize,
                ySize)
        for (s in inventorySlots.inventorySlots) {
            renderBackground(s as Slot?)
        }
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY)
        fontRendererObj.drawString(
                Item.getItemFromBlock(BlockEnum.ECBASEBLOCK.block)
                        .getItemStackDisplayName(
                                ItemStack(BlockEnum.ECBASEBLOCK.block,
                                        1, 0)).replace("ME ", ""), 8, 5,
                0x000000)
        fontRendererObj.drawString(
                StatCollector.translateToLocal("container.inventory"), 8, 136,
                0x000000)
        for (tank in tanks) {
            tank?.draw(guiLeft, guiTop, mouseX, mouseY)
        }
        for (slot in filter) {
            slot?.drawWidget()
        }
        for (tank in tanks) {
            if (tank != null) if (func_146978_c(tank.posX, tank.posY, 18, 73, mouseX, mouseY)) {
                tank.drawTooltip(mouseX - guiLeft, mouseY
                        - guiTop)
            }
        }
        for (fluidSlot in filter) {
            if (fluidSlot != null) {
                val i = fluidSlot.posX + 1
                val j = fluidSlot.posY + 1
                if (GuiUtil.isPointInRegion(guiLeft.toFloat(), guiTop, i, j,
                                16, 16, mouseX, mouseY)) {
                    drawRect(i, j, i + 16, j + 16, -2130706433)
                    break
                }
            }
        }
        showTooltipList(mouseX, mouseY)
        for (s in inventorySlots.inventorySlots) {
            try {
                renderOutput(s as Slot?, mouseX, mouseY)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun initGui() {
        super.initGui()
        guiLeft = (width - xSize) / 2
        guiTop = (height - ySize) / 2
        for (i in tanks.indices) {
            if (partSide != null && partSide != ForgeDirection.UNKNOWN && partSide!!.ordinal != i) continue
            tanks[i] = WidgetFluidTank(
                    fluidInterface.getFluidTank(ForgeDirection
                            .getOrientation(i)), i * 20 + 30, 16,
                    ForgeDirection.getOrientation(i))
            if (fluidInterface is IFluidSlotPartOrBlock) {
                filter[i] = WidgetFluidSlot(player,
                        fluidInterface as IFluidSlotPartOrBlock, i,
                        i * 20 + 30, 93)
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseBtn: Int) {
        super.mouseClicked(mouseX, mouseY, mouseBtn)
        for (fluidSlot in filter) {
            if (fluidSlot != null) if (GuiUtil.isPointInRegion(guiLeft.toFloat(), guiTop,
                            fluidSlot.posX, fluidSlot.posY, 18, 18,
                            mouseX, mouseY)) {
                fluidSlot
                        .mouseClicked(player.inventory.itemStack)
                break
            }
        }
    }

    private fun renderBackground(slot: Slot?) {
        if (slot!!.stack == null && slot.slotNumber < 9) {
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f)
            mc.textureManager.bindTexture(
                    ResourceLocation("appliedenergistics2",
                            "textures/guis/states.png"))
            drawTexturedModalRect(guiLeft + slot.xDisplayPosition,
                    guiTop + slot.yDisplayPosition, 240, 128, 16, 16)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LIGHTING)
        }
    }

    @Throws(Throwable::class)
    private fun renderOutput(slot: Slot?, mouseX: Int, mouseY: Int) {
        if (slot!!.stack != null && slot.slotNumber < 9) {
            val stack = slot.stack
            if (stack.item is ICraftingPatternItem) {
                val pattern = stack
                        .item as ICraftingPatternItem
                val output = pattern.getPatternForItem(stack,
                        Minecraft.getMinecraft().theWorld)
                        .condensedOutputs[0].itemStack.copy()
                zLevel = 160.0f
                GL11.glDisable(GL11.GL_LIGHTING)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glColor3f(1f, 1f, 1f)
                GL11.glDisable(GL11.GL_LIGHTING)
                GL11.glColor3f(1.0f, 1.0f, 1.0f)
                Minecraft.getMinecraft().renderEngine
                        .bindTexture(guiTexture)
                drawTexturedModalRect(slot.xDisplayPosition,
                        slot.yDisplayPosition, slot.xDisplayPosition,
                        slot.yDisplayPosition, 18, 18)
                GL11.glEnable(GL11.GL_LIGHTING)
                GL11.glTranslatef(0.0f, 0.0f, 32.0f)
                zLevel = 150.0f
                val itemRender = RenderItem.getInstance()
                itemRender.zLevel = 100.0f
                var font: FontRenderer? = null
                if (output != null) font = output.item.getFontRenderer(output)
                if (font == null) font = Minecraft.getMinecraft().fontRenderer
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                itemRender.renderItemAndEffectIntoGUI(font, Minecraft
                        .getMinecraft().textureManager, output,
                        slot.xDisplayPosition, slot.yDisplayPosition)
                itemRender.renderItemOverlayIntoGUI(font, Minecraft
                        .getMinecraft().textureManager, output,
                        slot.xDisplayPosition, slot.yDisplayPosition, null)
                zLevel = 0.0f
                itemRender.zLevel = 0.0f
                val i = slot.xDisplayPosition
                val j = slot.yDisplayPosition
                if (GuiUtil.isPointInRegion(guiLeft.toFloat(), guiTop, i, j,
                                16, 16, mouseX, mouseY)) {
                    GL11.glDisable(GL11.GL_LIGHTING)
                    GL11.glDisable(GL11.GL_DEPTH_TEST)
                    GL11.glColorMask(true, true, true, false)
                    drawGradientRect(i, j, i + 16, j + 16, -2130706433,
                            -2130706433)
                    GL11.glColorMask(true, true, true, true)
                    GL11.glEnable(GL11.GL_LIGHTING)
                    GL11.glEnable(GL11.GL_DEPTH_TEST)
                }
            }
        }
    }

    init {
        ySize = 230
        this.fluidInterface = fluidInterface
        this.player = player
        (inventorySlots as ContainerFluidInterface).gui = this
    }
}