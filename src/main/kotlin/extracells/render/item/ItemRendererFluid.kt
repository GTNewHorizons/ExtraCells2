package extracells.render.item

import extracells.util.GuiUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.IItemRenderer.ItemRenderType
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper
import net.minecraftforge.fluids.FluidRegistry

class ItemRendererFluid : IItemRenderer {
    override fun handleRenderType(item: ItemStack, type: ItemRenderType): Boolean {
        return true
    }

    override fun renderItem(type: ItemRenderType, item: ItemStack, vararg data: Any) {
        val fluid = FluidRegistry.getFluid(item.itemDamage) ?: return
        val icon = fluid.icon ?: return
        val f = icon.minU
        val f1 = icon.maxU
        val f2 = icon.minV
        val f3 = icon.maxV
        Minecraft.getMinecraft().renderEngine
                .bindTexture(TextureMap.locationBlocksTexture)
        GuiUtil.drawIcon(icon, 0, 0, 0, 16f, 16f)
        Minecraft.getMinecraft().renderEngine
                .bindTexture(TextureMap.locationItemsTexture)
    }

    override fun shouldUseRenderHelper(type: ItemRenderType, item: ItemStack,
                                       helper: ItemRendererHelper): Boolean {
        return false
    }
}