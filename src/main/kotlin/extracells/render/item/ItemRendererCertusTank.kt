package extracells.render.item

import extracells.render.model.ModelCertusTank
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.IItemRenderer.ItemRenderType
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import org.lwjgl.opengl.GL11
open class ItemRendererCertusTank : IItemRenderer {
    private val model = ModelCertusTank()
    override fun handleRenderType(item: ItemStack, type: ItemRenderType): Boolean {
        return true
    }

    override fun renderItem(type: ItemRenderType, item: ItemStack, vararg data: Any) {
        Minecraft.getMinecraft().renderEngine.bindTexture(ResourceLocation(
                "extracells", "textures/blocks/texmap_tank.png"))
        GL11.glPushMatrix()
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glEnable(GL11.GL_LIGHTING)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glTranslatef(0.5f, 0.5f, 0.5f)
        GL11.glScalef(1f, -1f, -1f)
        model.render(0.0625f)
        GL11.glScalef(1f, -1f, 1f)
        model.render(0.0625f)
        if (item != null && item.hasTagCompound()) {
            val storedFluid = FluidStack.loadFluidStackFromNBT(item
                    .tagCompound.getCompoundTag("tileEntity"))
            val tankCapacity = 32000
            if (storedFluid != null && storedFluid.getFluid() != null) {
                var fluidIcon = storedFluid.getFluid().icon
                if (fluidIcon == null) fluidIcon = FluidRegistry.LAVA.icon
                val tessellator = Tessellator.instance
                val renderer = RenderBlocks()
                GL11.glScalef(1f, 1f, -1f)
                renderer.setRenderBounds(0.08, 0.001, 0.08, 0.92, (
                        storedFluid.amount.toFloat() / tankCapacity.toFloat()
                                * 0.999f).toDouble(), 0.92)
                Minecraft.getMinecraft().renderEngine
                        .bindTexture(TextureMap.locationBlocksTexture)
                GL11.glTranslatef(-0.5f, -0.5f, -0.5f)
                val waterBlock = FluidRegistry.WATER.block
                tessellator.startDrawingQuads()
                tessellator.setColorRGBA_F((storedFluid.getFluid().color shr 16 and 0xFF) / 255.0f,
                        (storedFluid.getFluid().color shr 8 and 0xFF) / 255.0f,
                        (storedFluid.getFluid().color and 0xFF) / 255.0f, 1.0f)
                tessellator.setNormal(0.0f, -1f, 0.0f)
                renderer.renderFaceYNeg(waterBlock, 0.0, 0.0, 0.0, fluidIcon)
                tessellator.setNormal(0.0f, 1.0f, 0.0f)
                renderer.renderFaceYPos(waterBlock, 0.0, 0.0, 0.0, fluidIcon)
                tessellator.setNormal(0.0f, 0.0f, -1f)
                renderer.renderFaceZNeg(waterBlock, 0.0, 0.0, 0.0, fluidIcon)
                tessellator.setNormal(0.0f, 0.0f, 1.0f)
                renderer.renderFaceZPos(waterBlock, 0.0, 0.0, 0.0, fluidIcon)
                tessellator.setNormal(-1f, 0.0f, 0.0f)
                renderer.renderFaceXNeg(waterBlock, 0.0, 0.0, 0.0, fluidIcon)
                tessellator.setNormal(1.0f, 0.0f, 0.0f)
                renderer.renderFaceXPos(waterBlock, 0.0, 0.0, 0.0, fluidIcon)
                tessellator.draw()
            }
        }
        GL11.glPopAttrib()
        GL11.glPopMatrix()
    }

    override fun shouldUseRenderHelper(type: ItemRenderType, item: ItemStack,
                                       helper: ItemRendererHelper): Boolean {
        return true
    }
}