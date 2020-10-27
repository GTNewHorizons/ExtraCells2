package extracells.part

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.config.RedstoneMode
import appeng.api.config.SecurityPermissions
import appeng.api.networking.IGridNode
import appeng.api.networking.security.MachineSource
import appeng.api.networking.ticking.IGridTickable
import appeng.api.networking.ticking.TickRateModulation
import appeng.api.networking.ticking.TickingRequest
import appeng.api.parts.IPart
import appeng.api.parts.IPartCollisionHelper
import appeng.api.parts.IPartRenderHelper
import appeng.api.util.AEColor
import com.google.common.collect.Lists
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.container.ContainerPlaneFormation
import extracells.gui.GuiFluidPlaneFormation
import extracells.network.packet.other.IFluidSlotPartOrBlock
import extracells.network.packet.other.PacketFluidSlot
import extracells.render.TextureManager
import extracells.util.ColorUtil
import extracells.util.FluidUtil
import extracells.util.PermissionUtil
import extracells.util.inventory.ECPrivateInventory
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.Vec3
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidContainerRegistry
import net.minecraftforge.fluids.FluidRegistry
open class PartFluidPlaneFormation : PartECBase(), IFluidSlotPartOrBlock, IGridTickable {
    private var fluid: Fluid? = null

    // TODO redstone control
    private val redstoneMode: RedstoneMode? = null
    val upgradeInventory: ECPrivateInventory = object : ECPrivateInventory("", 1,
            1) {
        override fun isItemValidForSlot(i: Int, itemStack: ItemStack?): Boolean {
            return AEApi.instance().definitions().materials().cardRedstone().isSameAs(itemStack)
        }
    }

    override fun getDrops(drops: MutableList<ItemStack>, wrenched: Boolean) {
        for (stack in upgradeInventory.slots) {
            if (stack == null) continue
            drops.add(stack)
        }
    }

    override fun cableConnectionRenderTo(): Int {
        return 2
    }

    fun doWork() {
        val hostTile = hostTile
        val gridBlock = gridBlock
        val side = side
        if (fluid == null || hostTile == null || gridBlock == null || fluid!!.block == null) return
        val monitor = gridBlock.fluidMonitor ?: return
        val world = hostTile.worldObj
        val x = hostTile.xCoord + side!!.offsetX
        val y = hostTile.yCoord + side.offsetY
        val z = hostTile.zCoord + side.offsetZ
        val worldBlock = world.getBlock(x, y, z)
        if (worldBlock != null && worldBlock !== Blocks.air) return
        val canDrain = monitor.extractItems(FluidUtil.createAEFluidStack(fluid,
                FluidContainerRegistry.BUCKET_VOLUME.toLong()),
                Actionable.SIMULATE, MachineSource(this))
        if (canDrain == null
                || canDrain.stackSize < FluidContainerRegistry.BUCKET_VOLUME) return
        monitor.extractItems(FluidUtil.createAEFluidStack(fluid,
                FluidContainerRegistry.BUCKET_VOLUME.toLong()), Actionable.MODULATE,
                MachineSource(this))
        val fluidWorldBlock = fluid!!.block
        world.setBlock(x, y, z, fluidWorldBlock)
        world.markBlockForUpdate(x, y, z)
    }

    override fun getBoxes(bch: IPartCollisionHelper) {
        bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0)
        bch.addBox(5.0, 5.0, 13.0, 11.0, 11.0, 14.0)
    }

    override fun getClientGuiElement(player: EntityPlayer): Any? {
        return GuiFluidPlaneFormation(this, player)
    }

    override val powerUsage: Double
        get() = 1.0

    override fun getServerGuiElement(player: EntityPlayer?): Any? {
        return ContainerPlaneFormation(this, player)
    }

    override fun getTickingRequest(node: IGridNode): TickingRequest {
        return TickingRequest(1, 20, false, false)
    }

    override fun onActivate(player: EntityPlayer?, pos: Vec3?): Boolean {
        return if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
                        this as IPart)) {
            super.onActivate(player, pos)
        } else false
    }

    override fun readFromNBT(data: NBTTagCompound) {
        fluid = FluidRegistry.getFluid(data.getString("fluid"))
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
        rh.setInvColor(ColorUtil.getInvertedInt(AEColor.Cyan.mediumVariant))
        rh.renderInventoryFace(TextureManager.PANE_FRONT.textures[1],
                ForgeDirection.SOUTH, renderer)
        rh.setInvColor(ColorUtil.getInvertedInt(AEColor.Cyan.whiteVariant))
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
            ts.setColorOpaque_I(ColorUtil.getInvertedInt(host.color.mediumVariant))
            rh.renderFace(x, y, z, TextureManager.PANE_FRONT.textures[1],
                    ForgeDirection.SOUTH, renderer)
            ts.setColorOpaque_I(ColorUtil.getInvertedInt(host.color.whiteVariant))
            rh.renderFace(x, y, z, TextureManager.PANE_FRONT.textures[2],
                    ForgeDirection.SOUTH, renderer)
        }
        rh.setBounds(5f, 5f, 13f, 11f, 11f, 14f)
        renderStaticBusLights(x, y, z, rh, renderer)
    }

    fun sendInformation(_player: EntityPlayer?) {
        PacketFluidSlot(Lists.newArrayList(fluid))
                .sendPacketToPlayer(_player)
    }

    override fun setFluid(_index: Int, _fluid: Fluid?, _player: EntityPlayer?) {
        fluid = _fluid
        PacketFluidSlot(Lists.newArrayList(fluid))
                .sendPacketToPlayer(_player)
        saveData()
    }

    override fun tickingRequest(node: IGridNode,
                                TicksSinceLastCall: Int): TickRateModulation {
        doWork()
        return TickRateModulation.SAME
    }

    override fun writeToNBT(data: NBTTagCompound) {
        data.setString("fluid", if (fluid == null) "" else fluid!!.name)
    }
}