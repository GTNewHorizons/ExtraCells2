package extracells.render.item

import extracells.registries.ItemEnum
import extracells.util.GuiUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.IItemRenderer.ItemRenderType
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper
import org.lwjgl.opengl.GL11
open class ItemRendererFluidPattern : IItemRenderer {
    override fun handleRenderType(itemStack: ItemStack, type: ItemRenderType): Boolean {
        return type != ItemRenderType.ENTITY
    }

    override fun renderItem(type: ItemRenderType, itemStack: ItemStack,
                            vararg data: Any) {
        val item = ItemEnum.FLUIDPATTERN.item
        val fluid = item.getIcon(itemStack, 0)
        val texture = item.getIcon(itemStack, 1)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glColor3f(1f, 1f, 1f)
        if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) GL11.glTranslated(0.0, -10.0, 5.0)
        Minecraft.getMinecraft().renderEngine
                .bindTexture(TextureMap.locationBlocksTexture)
        if (fluid != null) GuiUtil.drawIcon(fluid, 5, 5, 0, 6f, 6f)
        Minecraft.getMinecraft().renderEngine
                .bindTexture(TextureMap.locationItemsTexture)
        GL11.glTranslated(0.0, 0.0, 0.001)
        GuiUtil.drawIcon(texture, 0, 0, 0, 16f, 16f)
    }

    override fun shouldUseRenderHelper(type: ItemRenderType,
                                       itemStack: ItemStack, helper: ItemRendererHelper): Boolean {
        return type == ItemRenderType.ENTITY
    }
}