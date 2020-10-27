package extracells.render.tileentity

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11
open class TileEntityRendererWalrus : TileEntitySpecialRenderer() {
    var modelWalrus = AdvancedModelLoader
            .loadModel(ResourceLocation("extracells", "models/walrus.obj"))
    var textureWalrus = ResourceLocation("extracells",
            "textures/blocks/walrus.png")

    override fun renderTileEntityAt(tileentity: TileEntity, x: Double, y: Double,
                                    z: Double, partialTickTime: Float) {
        Minecraft.getMinecraft().renderEngine.bindTexture(textureWalrus)
        GL11.glPushMatrix()
        GL11.glTranslated(x + 0.5, y, z + 0.5)
        val orientation = tileentity.getBlockMetadata()
        if (orientation == 4) {
            GL11.glRotatef(90f, 0f, 1f, 0f)
        } else if (orientation == 5) {
            GL11.glRotatef(-90f, 0f, 1f, 0f)
        } else if (orientation == 3) {
            GL11.glRotatef(180f, 0f, 1f, 0f)
        }
        modelWalrus.renderAll()
        GL11.glPopMatrix()
    }
}