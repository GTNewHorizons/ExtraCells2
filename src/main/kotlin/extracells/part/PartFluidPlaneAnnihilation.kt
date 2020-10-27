package extracells.part

import appeng.api.config.Actionable
import appeng.api.config.SecurityPermissions
import appeng.api.networking.events.MENetworkChannelChanged
import appeng.api.networking.events.MENetworkEventSubscribe
import appeng.api.networking.events.MENetworkPowerStatusChange
import appeng.api.networking.security.MachineSource
import appeng.api.parts.IPart
import appeng.api.parts.IPartCollisionHelper
import appeng.api.parts.IPartRenderHelper
import appeng.api.util.AEColor
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.render.TextureManager
import extracells.util.FluidUtil
import extracells.util.PermissionUtil
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.util.Vec3
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.IFluidBlock
open class PartFluidPlaneAnnihilation : PartECBase() {
    override fun cableConnectionRenderTo(): Int {
        return 2
    }

    @MENetworkEventSubscribe
    fun channelChanged(e: MENetworkChannelChanged) {
        if (e.node === gridNode) onNeighborChanged()
    }

    override fun getBoxes(bch: IPartCollisionHelper) {
        bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0)
        bch.addBox(5.0, 5.0, 13.0, 11.0, 11.0, 14.0)
    }

    override val powerUsage: Double
        get() = 1.0

    override fun onActivate(player: EntityPlayer?, pos: Vec3?): Boolean {
        return if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
                        this as IPart)) {
            super.onActivate(player, pos)
        } else false
    }

    override fun onNeighborChanged() {
        val hostTile = hostTile
        val gridBlock = gridBlock
        if (hostTile == null || gridBlock == null) return
        val monitor = gridBlock.fluidMonitor ?: return
        val world = hostTile.worldObj
        val x = hostTile.xCoord
        val y = hostTile.yCoord
        val z = hostTile.zCoord
        val side = side
        val fluidBlock = world.getBlock(x + side!!.offsetX, y + side.offsetY, z
                + side.offsetZ)
        val meta = world.getBlockMetadata(x + side.offsetX, y + side.offsetY, z
                + side.offsetZ)
        if (fluidBlock is IFluidBlock) {
            val block = fluidBlock as IFluidBlock
            val drained = block.drain(world, x + side.offsetX, y
                    + side.offsetY, z + side.offsetZ, false)
                    ?: return
            val toInject = FluidUtil.createAEFluidStack(drained)
            val notInjected = monitor.injectItems(toInject,
                    Actionable.SIMULATE, MachineSource(this))
            if (notInjected != null) return
            monitor.injectItems(toInject, Actionable.MODULATE,
                    MachineSource(this))
            block.drain(world, x + side.offsetX, y + side.offsetY, z
                    + side.offsetZ, true)
        } else if (meta == 0) {
            if (fluidBlock === Blocks.flowing_water) {
                val toInject = FluidUtil.createAEFluidStack(FluidRegistry.WATER)
                val notInjected = monitor.injectItems(toInject,
                        Actionable.SIMULATE, MachineSource(this))
                if (notInjected != null) return
                monitor.injectItems(toInject, Actionable.MODULATE,
                        MachineSource(this))
                world.setBlockToAir(x + side.offsetX, y + side.offsetY, z
                        + side.offsetZ)
            } else if (fluidBlock === Blocks.flowing_lava) {
                val toInject = FluidUtil.createAEFluidStack(FluidRegistry.LAVA)
                val notInjected = monitor.injectItems(toInject,
                        Actionable.SIMULATE, MachineSource(this))
                if (notInjected != null) return
                monitor.injectItems(toInject, Actionable.MODULATE,
                        MachineSource(this))
                world.setBlockToAir(x + side.offsetX, y + side.offsetY, z
                        + side.offsetZ)
            }
        }
    }

    @SideOnly(Side.CLIENT)
    override fun renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks) {
        val side = TextureManager.PANE_SIDE.texture
        rh.setTexture(side, side, side, TextureManager.BUS_BORDER.texture,
                side, side)
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 16f)
        rh.renderInventoryBox(renderer)
        rh.setBounds(3f, 3f, 14f, 13f, 13f, 16f)
        rh.setInvColor(AEColor.Cyan.blackVariant)
        rh.renderInventoryFace(TextureManager.PANE_FRONT.textures[0],
                ForgeDirection.SOUTH, renderer)
        Tessellator.instance.setBrightness(13 shl 20 or 13 shl 4)
        rh.setInvColor(AEColor.Cyan.mediumVariant)
        rh.renderInventoryFace(TextureManager.PANE_FRONT.textures[1],
                ForgeDirection.SOUTH, renderer)
        rh.setInvColor(AEColor.Cyan.whiteVariant)
        rh.renderInventoryFace(TextureManager.PANE_FRONT.textures[2],
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(5f, 5f, 13f, 11f, 11f, 14f)
        renderInventoryBusLights(rh, renderer)
    }

    @SideOnly(Side.CLIENT)
    override fun renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper,
                              renderer: RenderBlocks) {
        val ts = Tessellator.instance
        val side = TextureManager.PANE_SIDE.texture
        rh.setTexture(side, side, side, TextureManager.BUS_BORDER.texture,
                side, side)
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 16f)
        rh.renderBlock(x, y, z, renderer)
        rh.setBounds(3f, 3f, 14f, 13f, 13f, 16f)
        val host = host
        if (host != null) {
            ts.setColorOpaque_I(host.color.blackVariant)
            rh.renderFace(x, y, z, TextureManager.PANE_FRONT.textures[0],
                    ForgeDirection.SOUTH, renderer)
            if (isActive) ts.setBrightness(13 shl 20 or 13 shl 4)
            ts.setColorOpaque_I(host.color.mediumVariant)
            rh.renderFace(x, y, z, TextureManager.PANE_FRONT.textures[1],
                    ForgeDirection.SOUTH, renderer)
            ts.setColorOpaque_I(host.color.whiteVariant)
            rh.renderFace(x, y, z, TextureManager.PANE_FRONT.textures[2],
                    ForgeDirection.SOUTH, renderer)
        }
        rh.setBounds(5f, 5f, 13f, 11f, 11f, 14f)
        renderStaticBusLights(x, y, z, rh, renderer)
    }

    @MENetworkEventSubscribe
    override fun setPower(notUsed: MENetworkPowerStatusChange?) {
        super.setPower(notUsed)
        onNeighborChanged()
    }
}