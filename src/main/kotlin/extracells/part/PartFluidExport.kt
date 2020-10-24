package extracells.part

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.config.SecurityPermissions
import appeng.api.parts.IPart
import appeng.api.parts.IPartCollisionHelper
import appeng.api.parts.IPartRenderHelper
import appeng.api.util.AEColor
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.render.TextureManager
import extracells.util.PermissionUtil
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import java.util.*

class PartFluidExport : PartFluidIO() {
    override fun cableConnectionRenderTo(): Int {
        return 5
    }

    override fun doWork(rate: Int, TicksSinceLastCall: Int): Boolean {
        val facingTank = facingTank
        if (facingTank == null || !isActive) return false
        val filter: MutableList<Fluid> = ArrayList()
        filter.add(filterFluids[4]!!)
        if (filterSize >= 1) {
            var i: Byte = 1
            while (i < 9) {
                if (i.toInt() != 4) {
                    filter.add(filterFluids[i.toInt()]!!)
                }
                (i += 2).toByte()
            }
        }
        if (filterSize >= 2) {
            var i: Byte = 0
            while (i < 9) {
                if (i.toInt() != 4) {
                    filter.add(filterFluids[i.toInt()]!!)
                }
                (i += 2).toByte()
            }
        }
        for (fluid in filter) {
            if (fluid != null) {
                var stack = extractFluid(
                        AEApi.instance().storage().createFluidStack(FluidStack(fluid, rate * TicksSinceLastCall)),
                        Actionable.SIMULATE)
                if (stack == null || stack.stackSize <= 0) continue
                val filled = facingTank.fill(side.opposite, stack.fluidStack, false)
                if (filled > 0) {
                    stack.stackSize = filled.toLong()
                    stack = extractFluid(stack, Actionable.MODULATE)
                    if (stack != null && stack.stackSize > 0) {
                        facingTank.fill(side.opposite, stack.fluidStack, true)
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun getBoxes(bch: IPartCollisionHelper) {
        bch.addBox(6.0, 6.0, 12.0, 10.0, 10.0, 13.0)
        bch.addBox(4.0, 4.0, 13.0, 12.0, 12.0, 14.0)
        bch.addBox(5.0, 5.0, 14.0, 11.0, 11.0, 15.0)
        bch.addBox(6.0, 6.0, 15.0, 10.0, 10.0, 16.0)
        bch.addBox(6.0, 6.0, 11.0, 10.0, 10.0, 12.0)
    }

    override val powerUsage: Double
        get() = 1.0

    override fun onActivate(player: EntityPlayer, pos: Vec3): Boolean {
        return if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
                        this as IPart)) {
            super.onActivate(player, pos)
        } else false
    }

    @SideOnly(Side.CLIENT)
    override fun renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks) {
        val ts = Tessellator.instance
        rh.setTexture(TextureManager.EXPORT_SIDE.texture)
        rh.setBounds(6f, 6f, 12f, 10f, 10f, 13f)
        rh.renderInventoryBox(renderer)
        rh.setBounds(4f, 4f, 13f, 12f, 12f, 14f)
        rh.renderInventoryBox(renderer)
        rh.setBounds(5f, 5f, 14f, 11f, 11f, 15f)
        rh.renderInventoryBox(renderer)
        val side = TextureManager.EXPORT_SIDE.texture
        rh.setTexture(side, side, side,
                TextureManager.EXPORT_FRONT.texture, side, side)
        rh.setBounds(6f, 6f, 15f, 10f, 10f, 16f)
        rh.renderInventoryBox(renderer)
        rh.setInvColor(AEColor.Cyan.blackVariant)
        ts.setBrightness(15 shl 20 or 15 shl 4)
        rh.renderInventoryFace(TextureManager.EXPORT_FRONT.textures[1],
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(6f, 6f, 11f, 10f, 10f, 12f)
        renderInventoryBusLights(rh, renderer)
    }

    @SideOnly(Side.CLIENT)
    override fun renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper,
                              renderer: RenderBlocks) {
        val ts = Tessellator.instance
        rh.setTexture(TextureManager.EXPORT_SIDE.texture)
        rh.setBounds(6f, 6f, 12f, 10f, 10f, 13f)
        rh.renderBlock(x, y, z, renderer)
        rh.setBounds(4f, 4f, 13f, 12f, 12f, 14f)
        rh.renderBlock(x, y, z, renderer)
        rh.setBounds(5f, 5f, 14f, 11f, 11f, 15f)
        rh.renderBlock(x, y, z, renderer)
        val side = TextureManager.EXPORT_SIDE.texture
        rh.setTexture(side, side, side,
                TextureManager.EXPORT_FRONT.textures[0], side, side)
        rh.setBounds(6f, 6f, 15f, 10f, 10f, 16f)
        rh.renderBlock(x, y, z, renderer)
        ts.setColorOpaque_I(host.color.blackVariant)
        if (isActive) ts.setBrightness(15 shl 20 or 15 shl 4)
        rh.renderFace(x, y, z, TextureManager.EXPORT_FRONT.textures[1],
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(6f, 6f, 11f, 10f, 10f, 12f)
        renderStaticBusLights(x, y, z, rh, renderer)
    }
}