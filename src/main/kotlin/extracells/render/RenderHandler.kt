package extracells.render

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler
import extracells.block.BlockCertusTank
import extracells.render.model.ModelCertusTank
import net.minecraft.block.Block
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.world.IBlockAccess

class RenderHandler(id: Int) : ISimpleBlockRenderingHandler {
    var tank = ModelCertusTank()
    override fun getRenderId(): Int {
        return id
    }

    override fun renderInventoryBlock(block: Block, metadata: Int, modelID: Int,
                                      renderer: RenderBlocks) {
    }

    override fun renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int,
                                  block: Block, modelId: Int, renderer: RenderBlocks): Boolean {
        if (block is BlockCertusTank) {
            val tessellator = Tessellator.instance
            tessellator.setColorOpaque_F(1f, 1f, 1f)
            val oldAO = renderer.enableAO
            renderer.enableAO = false
            if (renderPass == 0) {
                tank.renderOuterBlock(block, x, y, z, renderer, world)
            } else {
                tank.renderInnerBlock(block, x, y, z, renderer, world)
                val tileEntity = world.getTileEntity(x, y, z)
                tank.renderFluid(tileEntity, x.toDouble(), y.toDouble(), z.toDouble(), renderer)
            }
            renderer.enableAO = oldAO
            return true
        }
        return false
    }

    override fun shouldRender3DInInventory(modelId: Int): Boolean {
        return true
    }

    companion object {
        var id = 0
            private set
        var renderPass = 0
    }

    init {
        renderPass = 0
        Companion.id = id
    }
}