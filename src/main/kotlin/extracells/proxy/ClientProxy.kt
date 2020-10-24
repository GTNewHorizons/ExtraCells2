package extracells.proxy

import cpw.mods.fml.client.registry.ClientRegistry
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import extracells.registries.BlockEnum
import extracells.registries.ItemEnum
import extracells.render.TextureManager
import extracells.render.block.RendererHardMEDrive.registerRenderer
import extracells.render.item.ItemRendererCertusTank
import extracells.render.item.ItemRendererFluid
import extracells.render.item.ItemRendererFluidPattern
import extracells.render.item.ItemRendererWalrus
import extracells.render.tileentity.TileEntityRendererWalrus
import extracells.tileentity.TileEntityWalrus
import net.minecraft.item.Item
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.MinecraftForge

class ClientProxy : CommonProxy() {
    override fun registerRenderers() {
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(BlockEnum.CERTUSTANK.block),
                ItemRendererCertusTank())
        MinecraftForgeClient.registerItemRenderer(ItemEnum.FLUIDPATTERN.item,
                ItemRendererFluidPattern())
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(BlockEnum.WALRUS.block),
                ItemRendererWalrus())
        MinecraftForgeClient.registerItemRenderer(ItemEnum.FLUIDITEM.item, ItemRendererFluid())
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWalrus::class.java, TileEntityRendererWalrus())
        registerRenderer()
    }

    @SubscribeEvent
    fun registerTextures(textureStitchEvent: TextureStitchEvent.Pre) {
        val map = textureStitchEvent.map
        for (currentTexture in TextureManager.values()) {
            currentTexture.registerTexture(map)
        }
    }

    override val isClient: Boolean
        get() = true
    override val isServer: Boolean
        get() = false

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }
}