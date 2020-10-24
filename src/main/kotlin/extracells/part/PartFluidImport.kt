package extracells.part

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.config.RedstoneMode
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
import net.minecraftforge.fluids.FluidTankInfo
import net.minecraftforge.fluids.IFluidHandler
import java.util.*

class PartFluidImport : PartFluidIO(), IFluidHandler {
    override fun cableConnectionRenderTo(): Int {
        return 5
    }

    override fun canDrain(from: ForgeDirection, fluid: Fluid): Boolean {
        return false
    }

    override fun canFill(from: ForgeDirection, fluid: Fluid): Boolean {
        return true
    }

    override fun doWork(rate: Int, TicksSinceLastCall: Int): Boolean {
        if (facingTank == null || !isActive) return false
        var empty = true
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
                empty = false
                if (fillToNetwork(fluid, rate * TicksSinceLastCall)) {
                    return true
                }
            }
        }
        return empty && fillToNetwork(null, rate * TicksSinceLastCall)
    }

    override fun drain(from: ForgeDirection, resource: FluidStack,
                       doDrain: Boolean): FluidStack {
        return null
    }

    override fun drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack {
        return null
    }

    override fun fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int {
        val redstonePowered = isRedstonePowered
        if (resource == null || redstonePowered && redstoneMode == RedstoneMode.LOW_SIGNAL || !redstonePowered && redstoneMode == RedstoneMode.HIGH_SIGNAL) return 0
        val drainAmount = Math.min(125 + speedState * 125, resource.amount)
        val toFill = FluidStack(resource.getFluid(), drainAmount)
        val action = if (doFill) Actionable.MODULATE else Actionable.SIMULATE
        val filled = injectFluid(AEApi.instance().storage().createFluidStack(toFill), action) ?: return toFill.amount
        return toFill.amount - filled.stackSize.toInt()
    }

    protected fun fillToNetwork(fluid: Fluid?, toDrain: Int): Boolean {
        val drained: FluidStack?
        val facingTank = facingTank
        val side = side
        drained = if (fluid == null) {
            facingTank!!.drain(side!!.opposite, toDrain, false)
        } else {
            facingTank!!.drain(side!!.opposite, FluidStack(
                    fluid, toDrain), false)
        }
        if (drained == null || drained.amount <= 0 || drained.fluidID <= 0) return false
        val toFill = AEApi.instance().storage()
                .createFluidStack(drained)
        val notInjected = injectFluid(toFill, Actionable.MODULATE)
        return if (notInjected != null) {
            val amount = (toFill.stackSize - notInjected
                    .stackSize).toInt()
            if (amount > 0) {
                if (fluid == null) facingTank.drain(side.opposite, amount, true) else facingTank.drain(
                        side.opposite,
                        FluidStack(toFill.fluid, amount), true)
                true
            } else {
                false
            }
        } else {
            if (fluid == null) facingTank.drain(side.opposite,
                    toFill.fluidStack.amount, true) else facingTank.drain(side.opposite, toFill.fluidStack,
                    true)
            true
        }
    }

    override fun getBoxes(bch: IPartCollisionHelper) {
        bch.addBox(4.0, 4.0, 14.0, 12.0, 12.0, 16.0)
        bch.addBox(5.0, 5.0, 13.0, 11.0, 11.0, 14.0)
        bch.addBox(6.0, 6.0, 12.0, 10.0, 10.0, 13.0)
        bch.addBox(6.0, 6.0, 11.0, 10.0, 10.0, 12.0)
    }

    override val powerUsage: Double
        get() = 1.0

    override fun getTankInfo(from: ForgeDirection): Array<FluidTankInfo> {
        return arrayOfNulls(0)
    }

    override fun onActivate(player: EntityPlayer, pos: Vec3): Boolean {
        return PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, this as IPart) && super.onActivate(
                player, pos)
    }

    @SideOnly(Side.CLIENT)
    override fun renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks) {
        val ts = Tessellator.instance
        val side = TextureManager.IMPORT_SIDE.texture
        rh.setTexture(side, side, side,
                TextureManager.IMPORT_FRONT.texture, side, side)
        rh.setBounds(4f, 4f, 14f, 12f, 12f, 16f)
        rh.renderInventoryBox(renderer)
        rh.setTexture(side)
        rh.setBounds(5f, 5f, 13f, 11f, 11f, 14f)
        rh.renderInventoryBox(renderer)
        rh.setBounds(6f, 6f, 12f, 10f, 10f, 13f)
        rh.renderInventoryBox(renderer)
        rh.setBounds(4f, 4f, 14f, 12f, 12f, 16f)
        rh.setInvColor(AEColor.Cyan.blackVariant)
        ts.setBrightness(15 shl 20 or 15 shl 4)
        rh.renderInventoryFace(TextureManager.IMPORT_FRONT.textures[1],
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(6f, 6f, 11f, 10f, 10f, 12f)
        renderInventoryBusLights(rh, renderer)
    }

    @SideOnly(Side.CLIENT)
    override fun renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper,
                              renderer: RenderBlocks) {
        val ts = Tessellator.instance
        val side = TextureManager.IMPORT_SIDE.texture
        rh.setTexture(side, side, side,
                TextureManager.IMPORT_FRONT.textures[0], side, side)
        rh.setBounds(4f, 4f, 14f, 12f, 12f, 16f)
        rh.renderBlock(x, y, z, renderer)
        ts.setColorOpaque_I(host.color.blackVariant)
        if (isActive) ts.setBrightness(15 shl 20 or 15 shl 4)
        rh.renderFace(x, y, z, TextureManager.IMPORT_FRONT.textures[1],
                ForgeDirection.SOUTH, renderer)
        rh.setTexture(side)
        rh.setBounds(5f, 5f, 13f, 11f, 11f, 14f)
        rh.renderBlock(x, y, z, renderer)
        rh.setBounds(6f, 6f, 12f, 10f, 10f, 13f)
        rh.renderBlock(x, y, z, renderer)
        rh.setBounds(6f, 6f, 11f, 10f, 10f, 12f)
        renderStaticBusLights(x, y, z, rh, renderer)
    }
}