package extracells.gui

import extracells.container.ContainerHardMEDrive
import extracells.registries.BlockEnum
import extracells.tileentity.TileEntityHardMeDrive
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Slot
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

open class GuiHardMEDrive(inventory: InventoryPlayer, tile: TileEntityHardMeDrive) : GuiContainer(ContainerHardMEDrive(inventory, tile)) {
  private val guiTexture: ResourceLocation = ResourceLocation("extracells", "textures/gui/hardmedrive.png")
  override fun drawGuiContainerBackgroundLayer(f: Float, i: Int, j: Int): Unit {
    drawDefaultBackground()
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture)
     val posX: Int = (width - xSize) / 2
     val posY: Int = (height - ySize) / 2
    drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize)
    /* ERROR converting `import scala.collection.JavaConversions._`*/
    for (s in this.inventorySlots.inventorySlots) {
      renderBackground((s as Slot))
    }
  }
  override fun drawGuiContainerForegroundLayer(i: Int, j: Int) {
    fontRendererObj.drawString(BlockEnum.BLASTRESISTANTMEDRIVE.statName, 5, 5, 0x000000)
  }

  private fun renderBackground(slot: Slot) {
    if (slot.stack == null && slot.slotNumber < 3) {
      GL11.glDisable(GL11.GL_LIGHTING)
      GL11.glEnable(GL11.GL_BLEND)
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F)
      this.mc.textureManager.bindTexture(ResourceLocation("appliedenergistics2", "textures/guis/states.png"))
      this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition, this.guiTop + slot.yDisplayPosition, 240, 0, 16, 16)
      GL11.glDisable(GL11.GL_BLEND)
      GL11.glEnable(GL11.GL_LIGHTING)
    }
  }
}