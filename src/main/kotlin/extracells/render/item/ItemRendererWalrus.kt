package extracells.render.item

import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.IItemRenderer.ItemRenderType
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11

class ItemRendererWalrus : IItemRenderer {
    var modelWalrus = AdvancedModelLoader
            .loadModel(ResourceLocation("extracells", "models/walrus.obj"))
    var textureWalrus = ResourceLocation("extracells",
            "textures/blocks/walrus.png")

    override fun handleRenderType(item: ItemStack, type: ItemRenderType): Boolean {
        return true
    }

    override fun renderItem(type: ItemRenderType, item: ItemStack, vararg data: Any) {
        Minecraft.getMinecraft().renderEngine.bindTexture(textureWalrus)
        GL11.glPushMatrix()
        when (type) {
            ItemRenderType.ENTITY -> {
            }
            ItemRenderType.EQUIPPED -> {
            }
            ItemRenderType.EQUIPPED_FIRST_PERSON -> {
                GL11.glRotated(180.0, 0.0, 1.0, 0.0)
                GL11.glTranslatef(-1f, 0.5f, -0.5f)
            }
            ItemRenderType.FIRST_PERSON_MAP -> {
            }
            ItemRenderType.INVENTORY -> GL11.glTranslatef(-0.5f, -0.5f, -0.1f)
            else -> {
            }
        }
        modelWalrus.renderAll()
        GL11.glPopMatrix()
    }

    override fun shouldUseRenderHelper(type: ItemRenderType, item: ItemStack,
                                       helper: ItemRendererHelper): Boolean {
        return true
    }
}