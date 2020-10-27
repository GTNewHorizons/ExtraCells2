package extracells.part

import appeng.api.config.Actionable
import appeng.api.networking.security.MachineSource
import appeng.api.parts.IPartRenderHelper
import appeng.api.storage.data.IAEFluidStack
import appeng.api.util.AEColor
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.render.TextureManager
import extracells.util.FluidUtil
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidContainerItem

open class PartFluidConversionMonitor : PartFluidStorageMonitor() {

    override fun onActivate(player: EntityPlayer?, pos: Vec3?): Boolean {
        if (super.onActivate(player, pos))
            return true
        if (player?.worldObj == null)
            return true
        if (player.worldObj.isRemote)
            return true
        val s = player.currentEquippedItem
        val mon = fluidStorage
        if (locked && s != null && mon != null) {
            val s2 = s.copy()
            s2.stackSize = 1
            if (FluidUtil.isFilled(s2)) {
                val f = FluidUtil.getFluidFromContainer(s2) ?: return true
                val fl = FluidUtil.createAEFluidStack(f)
                var not = mon.injectItems(fl.copy(),
                        Actionable.SIMULATE, MachineSource(this))
                if (mon.canAccept(fl)
                        && (not == null || not.stackSize == 0L)) {
                    val empty1 = FluidUtil.drainStack(s2, f)
                    val amount = empty1!!.getLeft()
                    if (amount > 0) {
                        f.amount = amount
                        fl.stackSize = amount.toLong()
                        not = mon.injectItems(fl.copy(), Actionable.SIMULATE, MachineSource(this))
                        if (mon.canAccept(fl) && (not == null || not.stackSize == 0L)) {
                            mon.injectItems(fl, Actionable.MODULATE, MachineSource(this))
                            val empty = empty1.right
                            if (empty != null) {
                                val tile = host?.tile
                                val side = side
                                tile?.also {
                                    dropItems(tile.worldObj, tile.xCoord + side!!.offsetX, tile.yCoord + side.offsetY,
                                            tile.zCoord + side.offsetZ, empty)
                                }
                            }
                            val s3 = s.copy()
                            s3.stackSize--
                            if (s3.stackSize <= 0) player.inventory.setInventorySlotContents(
                                    player.inventory.currentItem, null) else player.inventory.setInventorySlotContents(
                                    player.inventory.currentItem, s3)
                        }
                    }
                }
                return true
            } else if (FluidUtil.isEmpty(s2)) {
                if (fluid == null) return true
                var extract: IAEFluidStack?
                extract = if (s2.item is IFluidContainerItem) {
                    mon.extractItems(FluidUtil.createAEFluidStack(
                            fluid, (s2.item as IFluidContainerItem)
                            .getCapacity(s2).toLong()), Actionable.SIMULATE,
                            MachineSource(this))
                } else mon.extractItems(
                        FluidUtil.createAEFluidStack(fluid),
                        Actionable.SIMULATE, MachineSource(this))
                if (extract != null) {
                    if (extract.stackSize <= 0) return true
                    extract = mon.extractItems(extract, Actionable.MODULATE, MachineSource(this))
                    if (extract == null || extract.stackSize <= 0) return true
                    val empty1 = FluidUtil.fillStack(s2, extract.fluidStack)
                    if (empty1!!.left == 0) {
                        mon.injectItems(extract, Actionable.MODULATE, MachineSource(this))
                        return true
                    }
                    val empty = empty1.right
                    if (empty != null) {
                        side?.also { side -> host?.also { dropItems(it.tile.worldObj, it
                                .tile.xCoord + side.offsetX,
                                it.tile.yCoord + side.offsetY,
                                it.tile.zCoord + side.offsetZ,
                                empty) } }
                    }
                    val s3 = s.copy()
                    s3.stackSize = s3.stackSize - 1
                    if (s3.stackSize == 0) {
                        player.inventory.setInventorySlotContents(
                                player.inventory.currentItem, null)
                    } else {
                        player.inventory.setInventorySlotContents(
                                player.inventory.currentItem, s3)
                    }
                }
                return true
            }
        }
        return false
    }

    @SideOnly(Side.CLIENT)
    override fun renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks) {
        val ts = Tessellator.instance
        val side = TextureManager.TERMINAL_SIDE.texture
        rh.setTexture(side)
        rh.setBounds(4f, 4f, 13f, 12f, 12f, 14f)
        rh.renderInventoryBox(renderer)
        rh.setTexture(side, side, side, TextureManager.BUS_BORDER.texture,
                side, side)
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 16f)
        rh.renderInventoryBox(renderer)
        ts.setBrightness(13 shl 20 or 13 shl 4)
        rh.setInvColor(0xFFFFFF)
        rh.renderInventoryFace(TextureManager.BUS_BORDER.texture,
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(3f, 3f, 15f, 13f, 13f, 16f)
        rh.setInvColor(AEColor.Transparent.blackVariant)
        rh.renderInventoryFace(
                TextureManager.CONVERSION_MONITOR.textures[0],
                ForgeDirection.SOUTH, renderer)
        rh.setInvColor(AEColor.Transparent.mediumVariant)
        rh.renderInventoryFace(
                TextureManager.CONVERSION_MONITOR.textures[1],
                ForgeDirection.SOUTH, renderer)
        rh.setInvColor(AEColor.Transparent.whiteVariant)
        rh.renderInventoryFace(
                TextureManager.CONVERSION_MONITOR.textures[2],
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(5f, 5f, 12f, 11f, 11f, 13f)
        renderInventoryBusLights(rh, renderer)
    }

    @SideOnly(Side.CLIENT)
    override fun renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper,
                              renderer: RenderBlocks) {
        val ts = Tessellator.instance
        val side = TextureManager.TERMINAL_SIDE.texture
        rh.setTexture(side)
        rh.setBounds(4f, 4f, 13f, 12f, 12f, 14f)
        rh.renderBlock(x, y, z, renderer)
        rh.setTexture(side, side, side, TextureManager.BUS_BORDER.texture,
                side, side)
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 16f)
        rh.renderBlock(x, y, z, renderer)
        if (isActive) Tessellator.instance.setBrightness(13 shl 20 or 13 shl 4)
        ts.setColorOpaque_I(0xFFFFFF)
        rh.renderFace(x, y, z, TextureManager.BUS_BORDER.texture,
                ForgeDirection.SOUTH, renderer)
        val host = host
        rh.setBounds(3f, 3f, 15f, 13f, 13f, 16f)
        ts.setColorOpaque_I(host!!.color.mediumVariant)
        rh.renderFace(x, y, z,
                TextureManager.CONVERSION_MONITOR.textures[0],
                ForgeDirection.SOUTH, renderer)
        ts.setColorOpaque_I(host.color.whiteVariant)
        rh.renderFace(x, y, z,
                TextureManager.CONVERSION_MONITOR.textures[1],
                ForgeDirection.SOUTH, renderer)
        ts.setColorOpaque_I(host.color.blackVariant)
        rh.renderFace(x, y, z,
                TextureManager.CONVERSION_MONITOR.textures[2],
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(5f, 5f, 12f, 11f, 11f, 13f)
        renderStaticBusLights(x, y, z, rh, renderer)
    }
}