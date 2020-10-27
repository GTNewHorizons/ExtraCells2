package extracells.gui

import extracells.container.ContainerDrive
import extracells.network.packet.part.PacketFluidStorage
import extracells.part.PartDrive
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Slot
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
open class GuiDrive(_part: PartDrive, private val player: EntityPlayer) : GuiContainer(ContainerDrive(_part, player)) {
    private val guiTexture = ResourceLocation("extracells",
            "textures/gui/drive.png")

    override fun drawGuiContainerBackgroundLayer(alpha: Float, sizeX: Int,
                                                 sizeY: Int) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture)
        drawTexturedModalRect(guiLeft, guiTop - 18, 0, 0, xSize,
                ySize)
        for (s in inventorySlots.inventorySlots) {
            renderBackground(s as Slot?)
        }
    }

    private fun renderBackground(slot: Slot?) {
        if (slot!!.stack == null && slot.slotNumber < 6) {
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f)
            mc.textureManager.bindTexture(
                    ResourceLocation("appliedenergistics2",
                            "textures/guis/states.png"))
            drawTexturedModalRect(guiLeft + slot.xDisplayPosition,
                    guiTop + slot.yDisplayPosition, 240, 0, 16, 16)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LIGHTING)
        }
    }

    init {
        xSize = 176
        ySize = 163
        PacketFluidStorage(player).sendPacketToServer()
    }
}