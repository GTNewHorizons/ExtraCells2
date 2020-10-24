package extracells.part

import appeng.api.AEApi
import appeng.api.config.RedstoneMode
import appeng.api.config.SecurityPermissions
import appeng.api.networking.security.BaseActionSource
import appeng.api.networking.storage.IStackWatcher
import appeng.api.networking.storage.IStackWatcherHost
import appeng.api.networking.storage.IStorageGrid
import appeng.api.parts.IPart
import appeng.api.parts.IPartCollisionHelper
import appeng.api.parts.IPartRenderHelper
import appeng.api.storage.StorageChannel
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IAEStack
import appeng.api.storage.data.IItemList
import com.google.common.collect.Lists
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.container.ContainerFluidEmitter
import extracells.gui.GuiFluidEmitter
import extracells.network.packet.other.IFluidSlotPartOrBlock
import extracells.network.packet.other.PacketFluidSlot
import extracells.network.packet.part.PacketFluidEmitter
import extracells.render.TextureManager
import extracells.util.PermissionUtil
import io.netty.buffer.ByteBuf
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Vec3
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import java.io.IOException
import java.util.*

class PartFluidLevelEmitter : PartECBase(), IStackWatcherHost, IFluidSlotPartOrBlock {
    private var fluid: Fluid? = null
    private var mode = RedstoneMode.HIGH_SIGNAL
    private var watcher: IStackWatcher? = null
    private var wantedAmount: Long = 0
    private var currentAmount: Long = 0
    private var clientRedstoneOutput = false
    override fun cableConnectionRenderTo(): Int {
        return 8
    }

    fun changeWantedAmount(modifier: Int, player: EntityPlayer?) {
        setWantedAmount(wantedAmount + modifier, player)
    }

    override fun getBoxes(bch: IPartCollisionHelper) {
        bch.addBox(7.0, 7.0, 11.0, 9.0, 9.0, 16.0)
    }

    override fun getClientGuiElement(player: EntityPlayer): Any? {
        return GuiFluidEmitter(this, player)
    }

    override val powerUsage: Double
        get() = 1.0

    override fun getServerGuiElement(player: EntityPlayer): Any? {
        return ContainerFluidEmitter(this, player)
    }

    private val isPowering: Boolean
        private get() = when (mode) {
            RedstoneMode.LOW_SIGNAL -> wantedAmount >= currentAmount
            RedstoneMode.HIGH_SIGNAL -> wantedAmount < currentAmount
            else -> false
        }

    override fun isProvidingStrongPower(): Int {
        return if (isPowering) 15 else 0
    }

    override fun isProvidingWeakPower(): Int {
        return isProvidingStrongPower
    }

    private fun notifyTargetBlock(_tile: TileEntity?, _side: ForgeDirection?) {
        // note - params are always the same
        _tile!!.worldObj.notifyBlocksOfNeighborChange(_tile.xCoord,
                _tile.yCoord, _tile.zCoord, Blocks.air)
        _tile.worldObj.notifyBlocksOfNeighborChange(
                _tile.xCoord + _side!!.offsetX, _tile.yCoord + _side.offsetY,
                _tile.zCoord + _side.offsetZ, Blocks.air)
    }

    override fun onActivate(player: EntityPlayer, pos: Vec3): Boolean {
        return if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
                        this as IPart)) {
            super.onActivate(player, pos)
        } else false
    }

    override fun onStackChange(o: IItemList<*>?, fullStack: IAEStack<*>?,
                               diffStack: IAEStack<*>?, src: BaseActionSource, chan: StorageChannel) {
        if (chan == StorageChannel.FLUIDS && diffStack != null && (diffStack as IAEFluidStack).fluid === fluid) {
            currentAmount = fullStack?.stackSize ?: 0
            val node = gridNode
            if (node != null) {
                isActive = node.isActive
                host.markForUpdate()
                notifyTargetBlock(hostTile, side)
            }
        }
    }

    override fun randomDisplayTick(world: World, x: Int, y: Int, z: Int, r: Random) {
        if (clientRedstoneOutput) {
            val d = side
            val d0 = d!!.offsetX * 0.45f + (r.nextFloat() - 0.5f) * 0.2
            val d1 = d.offsetY * 0.45f + (r.nextFloat() - 0.5f) * 0.2
            val d2 = d.offsetZ * 0.45f + (r.nextFloat() - 0.5f) * 0.2
            world.spawnParticle("reddust", 0.5 + x + d0, 0.5 + y + d1, 0.5 + z
                    + d2, 0.0, 0.0, 0.0)
        }
    }

    override fun readFromNBT(data: NBTTagCompound) {
        super.readFromNBT(data)
        fluid = FluidRegistry.getFluid(data.getString("fluid"))
        mode = RedstoneMode.values()[data.getInteger("mode")]
        wantedAmount = data.getLong("wantedAmount")
        if (wantedAmount < 0) wantedAmount = 0
    }

    @Throws(IOException::class)
    override fun readFromStream(data: ByteBuf): Boolean {
        super.readFromStream(data)
        clientRedstoneOutput = data.readBoolean()
        if (host != null) host.markForUpdate()
        return true
    }

    @SideOnly(Side.CLIENT)
    override fun renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks) {
        rh.setTexture(TextureManager.LEVEL_FRONT.textures[0])
        rh.setBounds(7f, 7f, 11f, 9f, 9f, 14f)
        rh.renderInventoryBox(renderer)
        rh.setTexture(TextureManager.LEVEL_FRONT.textures[1])
        rh.setBounds(7f, 7f, 14f, 9f, 9f, 16f)
        rh.renderInventoryBox(renderer)
    }

    @SideOnly(Side.CLIENT)
    override fun renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper,
                              renderer: RenderBlocks) {
        rh.setTexture(TextureManager.LEVEL_FRONT.textures[0])
        rh.setBounds(7f, 7f, 11f, 9f, 9f, 14f)
        rh.renderBlock(x, y, z, renderer)
        rh.setTexture(if (clientRedstoneOutput) TextureManager.LEVEL_FRONT
                .textures[2] else TextureManager.LEVEL_FRONT.textures[1])
        rh.setBounds(7f, 7f, 14f, 9f, 9f, 16f)
        rh.renderBlock(x, y, z, renderer)
    }

    private fun updateCurrentAmount() {
        val n = gridNode ?: return
        val g = n.grid ?: return
        val s = g.getCache<IStorageGrid>(IStorageGrid::class.java) ?: return
        val f = s.fluidInventory ?: return
        currentAmount = 0L
        for (st in f.storageList) {
            if (fluid != null && st.fluid === fluid) currentAmount = st.stackSize
        }
        val h = host
        h?.markForUpdate()
    }

    override fun setFluid(_index: Int, _fluid: Fluid?, _player: EntityPlayer?) {
        fluid = _fluid
        updateCurrentAmount()
        if (watcher == null) return
        watcher!!.clear()
        updateWatcher(watcher!!)
        PacketFluidSlot(Lists.newArrayList(fluid))
                .sendPacketToPlayer(_player)
        notifyTargetBlock(hostTile, side)
        saveData()
    }

    fun setWantedAmount(_wantedAmount: Long, player: EntityPlayer?) {
        wantedAmount = _wantedAmount
        if (wantedAmount < 0) wantedAmount = 0
        val h = host
        h?.markForUpdate()
        PacketFluidEmitter(wantedAmount, player)
                .sendPacketToPlayer(player)
        notifyTargetBlock(hostTile, side)
        saveData()
    }

    fun syncClientGui(player: EntityPlayer?) {
        PacketFluidEmitter(mode, player).sendPacketToPlayer(player)
        PacketFluidEmitter(wantedAmount, player)
                .sendPacketToPlayer(player)
        PacketFluidSlot(Lists.newArrayList(fluid))
                .sendPacketToPlayer(player)
    }

    fun toggleMode(player: EntityPlayer?) {
        when (mode) {
            RedstoneMode.LOW_SIGNAL -> mode = RedstoneMode.HIGH_SIGNAL
            else -> mode = RedstoneMode.LOW_SIGNAL
        }
        val h = host
        h?.markForUpdate()
        PacketFluidEmitter(mode, player).sendPacketToPlayer(player)
        notifyTargetBlock(hostTile, side)
        saveData()
    }

    override fun updateWatcher(newWatcher: IStackWatcher) {
        watcher = newWatcher
        if (fluid != null) watcher!!.add(AEApi.instance().storage()
                .createFluidStack(FluidStack(fluid, 1)))
    }

    override fun writeToNBT(data: NBTTagCompound) {
        super.writeToNBT(data)
        if (fluid != null) data.setString("fluid", fluid!!.name) else data.removeTag("fluid")
        data.setInteger("mode", mode.ordinal)
        data.setLong("wantedAmount", wantedAmount)
    }

    @Throws(IOException::class)
    override fun writeToStream(data: ByteBuf) {
        super.writeToStream(data)
        data.writeBoolean(isPowering)
    }
}