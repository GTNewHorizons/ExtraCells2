package extracells.render.block

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler
import cpw.mods.fml.client.registry.RenderingRegistry
import extracells.tileentity.TileEntityHardMeDrive
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import net.minecraft.util.ResourceLocation
import net.minecraft.world.IBlockAccess
import org.lwjgl.opengl.GL11

object RendererHardMEDrive : ISimpleBlockRenderingHandler {
    var renderID: Int = 0
    val tex: ResourceLocation = ResourceLocation("extracells", "textures/blocks/hardmedrive.png")
    val i: Icon = Icon(5, 11, 5, 7)
    val i2: Icon = Icon(5, 11, 8, 10)
    val i3: Icon = Icon(5, 11, 11, 13)
    @JvmStatic
    fun registerRenderer() {
        renderID = RenderingRegistry.getNextAvailableRenderId()
        RenderingRegistry.registerBlockHandler(this)
    }

    override fun getRenderId(): Int = renderID
    override fun shouldRender3DInInventory(modelId: Int): Boolean = true
    override fun renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks) {
        val tessellator: Tessellator = Tessellator.instance
        renderer.setRenderBounds(0.0 , 0.0 , 0.0 , 1.0 , 1.0 , 1.0 )
        GL11.glTranslatef(0.5F, 0.5F, 0.5F)
        tessellator.startDrawingQuads()
        tessellator.setNormal(0.0F, 1.0F, 0.0F)
        renderer.renderFaceYNeg(block, 0.0 , 0.0 , 0.0 , renderer.getBlockIconFromSideAndMetadata(block, 0, 3))
        tessellator.draw()
        tessellator.startDrawingQuads()
        tessellator.setNormal(0.0F, 1.0F, 0.0F)
        renderer.renderFaceYPos(block, 0.0 , 0.0 , 0.0 , renderer.getBlockIconFromSideAndMetadata(block, 1, 3))
        tessellator.draw()
        tessellator.startDrawingQuads()
        tessellator.setNormal(0.0F, 0.0F, 1.0F)
        renderer.renderFaceZNeg(block, 0.0 , 0.0 , 0.0 , renderer.getBlockIconFromSideAndMetadata(block, 2, 3))
        tessellator.draw()
        tessellator.startDrawingQuads()
        tessellator.setNormal(0.0F, 0.0F, 1.0F)
        renderer.renderFaceZPos(block, 0.0 , 0.0 , 0.0 , renderer.getBlockIconFromSideAndMetadata(block, 3, 3))
        tessellator.draw()
        Minecraft.getMinecraft().renderEngine.bindTexture(tex)
        tessellator.startDrawingQuads()
        tessellator.setNormal(0.0F, 0.0F, 1.0F)
        renderer.renderMinX = .3125 
                renderer.renderMinY = .25 
                renderer.renderMaxX = .6875 
                renderer.renderMaxY = .375 
                renderer.renderFaceZPos(block, 0.0 , 0.0 , 0.0 , i)
        tessellator.draw()
        renderer.renderMinY = .43525 
                renderer.renderMaxY = .56025 
                tessellator.startDrawingQuads()
        tessellator.setNormal(0.0F, 0.0F, 1.0F)
        renderer.renderFaceZPos(block, 0.0 , 0.0 , 0.0 , i)
        tessellator.draw()
        renderer.renderMinY = .62275 
                renderer.renderMaxY = .75 
                tessellator.startDrawingQuads()
        tessellator.setNormal(0.0F, 0.0F, 1.0F)
        renderer.renderFaceZPos(block, 0.0 , 0.0 , 0.0 , i)
        renderer.renderMinX = 0.0 
                renderer.renderMinY = 0.0 
                renderer.renderMaxX = 1.0 
                renderer.renderMaxY = 1.0 
                tessellator.draw()
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture)
        tessellator.startDrawingQuads()
        tessellator.setNormal(1.0F, 0.0F, 0.0F)
        renderer.renderFaceXNeg(block, 0.0 , 0.0 , 0.0 , renderer.getBlockIconFromSideAndMetadata(block, 4, 3))
        tessellator.draw()
        tessellator.startDrawingQuads()
        tessellator.setNormal(1.0F, 0.0F, 0.0F)
        renderer.renderFaceXPos(block, 0.0 , 0.0 , 0.0 , renderer.getBlockIconFromSideAndMetadata(block, 5, 3))
        tessellator.draw()
        GL11.glTranslatef(0.5F, 0.5F, 0.5F)
    }

    override fun renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean {
        val tessellator: Tessellator = Tessellator.instance
        renderer.renderStandardBlock(block, x, y, z)
        tessellator.addTranslation(x, y, z)
        val meta: Int = world.getBlockMetadata(x, y, z)
        val tileEntity: TileEntity? = world.getTileEntity(x, y, z)
        if (tileEntity == null || (tileEntity !is TileEntityHardMeDrive)) return false
        val tileEntityHardMeDrive: TileEntityHardMeDrive = tileEntity
        var b = true
        try {
            Tessellator.instance.draw()
        } catch (e: IllegalStateException) {
            b = false
        }
        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_CULL_FACE)
        tessellator.setColorOpaque_I(block.colorMultiplier(world, x, y, z))
        tessellator.setBrightness(240)
        Minecraft.getMinecraft().renderEngine.bindTexture(tex)
            when (meta) {
                2 -> {
                    renderZNeg(renderer, block, generateRenderInformations(tileEntityHardMeDrive))
                }
                3 -> {
                    renderZPos(renderer, block, generateRenderInformations(tileEntityHardMeDrive))
                }
                4 -> {
                    renderXNeg(renderer, block, generateRenderInformations(tileEntityHardMeDrive))
                }
                5 -> {
                    renderXPos(renderer, block, generateRenderInformations(tileEntityHardMeDrive))
                }
                else -> {
                }
            }
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture)
        GL11.glPopMatrix()
        if (b) tessellator.startDrawingQuads()
        tessellator.addTranslation(x, y, z)
        return true
    }

    fun generateRenderInformations(tileEntity: TileEntityHardMeDrive): Array<RenderInformation>
        = arrayOf(
                RenderInformation(10, tileEntity.getColorByStatus(0)),
                RenderInformation(7, tileEntity.getColorByStatus(1)),
                RenderInformation(4, tileEntity.getColorByStatus(2))
        )

    fun renderXPos(renderer: RenderBlocks, block: Block, renderInformations: Array<RenderInformation>) {
        val tessellator: Tessellator = Tessellator.instance
        renderer.renderMinZ = .3125 
                renderer.renderMaxZ = .6875
        for (it in renderInformations) {
            renderer.renderMinY = 1.0 / 16.0 * it.getPos()
            renderer.renderMaxY = 1.0 / 16.0 * (it.getPos() + 2)
            tessellator.startDrawingQuads()
            tessellator.setNormal(1.0F, 0.0F, 0.0F)
            renderer.renderFaceXPos(block, 0.0, 0.0, 0.0, it.getIcon())
            tessellator.draw()
            tessellator.startDrawingQuads()
            tessellator.setNormal(1.0F, 0.0F, 0.0F)
            tessellator.setColorOpaque_I(it.getColor())
            renderer.renderFaceXPos(block, 0.0, 0.0, 0.0, it.getIcon2())
            tessellator.draw()
        }
        renderer.renderMinX = 0.0 
                renderer.renderMinY = 0.0 
                renderer.renderMinZ = 0.0 
                renderer.renderMaxX = 1.0 
                renderer.renderMaxY = 1.0 
                renderer.renderMaxZ = 1.0 
    }

    fun renderXNeg(renderer: RenderBlocks, block: Block, renderInformations: Array<RenderInformation>) {
        val tessellator: Tessellator = Tessellator.instance
        renderer.renderMinZ = .3125 
                renderer.renderMaxZ = .6875
        for (it in renderInformations) {
            renderer.renderMinY = 1.0 / 16.0 * it.getPos()
            renderer.renderMaxY = 1.0 / 16.0 * (it.getPos() + 2)
            tessellator.startDrawingQuads()
            tessellator.setNormal(-1.0F, 0.0F, 0.0F)
            renderer.renderFaceXNeg(block, 0.0, 0.0, 0.0, it.getIcon())
            tessellator.draw()
            tessellator.startDrawingQuads()
            tessellator.setNormal(-1.0F, 0.0F, 0.0F)
            tessellator.setColorOpaque_I(it.getColor())
            renderer.renderFaceXNeg(block, 0.0, 0.0, 0.0, it.getIcon2())
            tessellator.draw()
        }
        renderer.renderMinX = 0.0 
                renderer.renderMinY = 0.0 
                renderer.renderMinZ = 0.0 
                renderer.renderMaxX = 1.0 
                renderer.renderMaxY = 1.0 
                renderer.renderMaxZ = 1.0 
    }

    fun renderZPos(renderer: RenderBlocks, block: Block, renderInformations: Array<RenderInformation>) {
        val tessellator: Tessellator = Tessellator.instance
        renderer.renderMinX = .3125 
                renderer.renderMaxX = .6875
        for (it in renderInformations) {
            renderer.renderMinY = 1.0 / 16.0 * it.getPos()
            renderer.renderMaxY = 1.0 / 16.0 * (it.getPos() + 2.0)
            tessellator.startDrawingQuads()
            tessellator.setNormal(0.0F, 0.0F, 1.0F)
            renderer.renderFaceZPos(block, 0.0, 0.0, 0.0, it.getIcon())
            tessellator.draw()
            tessellator.startDrawingQuads()
            tessellator.setNormal(0.0F, 0.0F, 1.0F)
            tessellator.setColorOpaque_I(it.getColor())
            renderer.renderFaceZPos(block, 0.0, 0.0, 0.0, it.getIcon2())
            tessellator.draw()
        }
        renderer.renderMinX = 0.0
                renderer.renderMinY = 0.0
                renderer.renderMinZ = 0.0
                renderer.renderMaxX = 1.0
                renderer.renderMaxY = 1.0
                renderer.renderMaxZ = 1.0
    }

    fun renderZNeg(renderer: RenderBlocks, block: Block, renderInformations: Array<RenderInformation>) {
        val tessellator: Tessellator = Tessellator.instance
        renderer.renderMinX = .3125 
                renderer.renderMaxX = .6875
        for (it in renderInformations) {
            renderer.renderMinY = 1.0 / 16.0 * it.getPos()
            renderer.renderMaxY = 1.0 / 16.0 * (it.getPos() + 2.0)
            tessellator.startDrawingQuads()
            tessellator.setNormal(0.0F, 0.0F, -1.0F)
            renderer.renderFaceZNeg(block, 0.0, 0.0, 0.0, it.getIcon())
            tessellator.draw()
            tessellator.startDrawingQuads()
            tessellator.setNormal(0.0F, 0.0F, -1.0F)
            tessellator.setColorOpaque_I(it.getColor())
            renderer.renderFaceZNeg(block, 0.0, 0.0, 0.0, it.getIcon2())
            tessellator.draw()
        }
        renderer.renderMinX = 0.0
                renderer.renderMinY = 0.0
                renderer.renderMinZ = 0.0
                renderer.renderMaxX = 1.0
                renderer.renderMaxY = 1.0
                renderer.renderMaxZ = 1.0
    }
open class RenderInformation(private val pos: Double, private val color: Int) {
        constructor(pos: Int,color: Int) : this(pos.toDouble(), color)
        fun getIcon(): Icon = i3
        fun getIcon2(): Icon = i3
        fun getPos(): Double = pos
        fun getColor(): Int = color
    }
open class Icon(private val minU: Float, private val maxU: Float, private val minV: Float, private val maxV: Float) : IIcon {
        override fun getIconHeight(): Int = throw NotImplementedError()
        override fun getMinU(): Float = minU
        override fun getMaxU(): Float = maxU
        override fun getInterpolatedV(p_94207_1_: Double): Float {
            val f: Float = this.getMaxV() - this.getMinV()
            return this.getMinV() + f
        }

        override fun getIconName(): String = ""
        override fun getIconWidth(): Int = 0
        override fun getMinV(): Float = minV
        override fun getMaxV(): Float = maxV
        override fun getInterpolatedU(p_94214_1_: Double): Float {
            val f: Float = this.getMaxU() - this.getMinU()
            return this.getMinU() + f
        }
        constructor(minU: Int, maxU:Int, minV:Int,maxV: Int):this(minU.toFloat(), maxU.toFloat(), minV.toFloat(), maxV.toFloat())
    }
}

private fun Tessellator.addTranslation(x: Int, y: Int, z: Int) {
    this.addTranslation(x.toFloat(), y.toFloat(), z.toFloat())
}
