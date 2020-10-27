package extracells.part

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.implementations.IPowerChannelState
import appeng.api.networking.IGridHost
import appeng.api.networking.IGridNode
import appeng.api.networking.energy.IEnergyGrid
import appeng.api.networking.events.MENetworkEventSubscribe
import appeng.api.networking.events.MENetworkPowerStatusChange
import appeng.api.networking.security.IActionHost
import appeng.api.networking.security.MachineSource
import appeng.api.parts.*
import appeng.api.storage.data.IAEFluidStack
import appeng.api.util.AECableType
import appeng.api.util.AEColor
import appeng.api.util.DimensionalCoord
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.Optional
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.gridblock.ECBaseGridBlock
import extracells.integration.Integration
import extracells.network.GuiHandler.getGuiId
import extracells.network.GuiHandler.launchGui
import extracells.registries.ItemEnum
import extracells.registries.PartEnum
import extracells.render.TextureManager
import io.netty.buffer.ByteBuf
import mekanism.api.gas.IGasHandler
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import net.minecraft.util.Vec3
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidHandler
import java.io.IOException
import java.util.*

abstract class PartECBase : IPart, IGridHost, IActionHost, IPowerChannelState {
    private var node: IGridNode? = null
    var side: ForgeDirection? = null
        private set
    var host: IPartHost? = null
        private set
    protected var tile: TileEntity? = null
    var gridBlock: ECBaseGridBlock? = null
        private set
    open val powerUsage = 0.0
    var hostTile: TileEntity? = null
        private set
    var facingTank: IFluidHandler? = null
        private set
    private var facingGasTank: Any? = null
    protected var isRedstonePowered = false
        private set
    private var isActive = false
    private var isPowerd = false
    private var owner: EntityPlayer? = null
    private val `is`: ItemStack = ItemStack(ItemEnum.PARTITEM.item, 1, PartEnum.getPartID(this))
    override fun addToWorld() {
        if (FMLCommonHandler.instance().effectiveSide.isClient) return
        gridBlock = ECBaseGridBlock(this)
        node = AEApi.instance().createGridNode(gridBlock)
        if (node != null) {
            if (owner != null) node!!.playerID = AEApi.instance().registries().players()
                    .getID(owner)
            node!!.updateState()
        }
        setPower(null)
        onNeighborChanged()
    }

    abstract override fun cableConnectionRenderTo(): Int
    override fun canBePlacedOn(what: BusSupport): Boolean {
        return what != BusSupport.DENSE_CABLE
    }

    override fun canConnectRedstone(): Boolean {
        return false
    }

    protected fun extractFluid(toExtract: IAEFluidStack?,
                               action: Actionable?): IAEFluidStack? {
        if (gridBlock == null || facingTank == null) return null
        val monitor = gridBlock?.fluidMonitor ?: return null
        return monitor.extractItems(toExtract, action, MachineSource(this))
    }

    protected fun extractGasFluid(toExtract: IAEFluidStack?,
                                  action: Actionable?): IAEFluidStack? {
        if (gridBlock == null || facingGasTank == null) return null
        val monitor = gridBlock?.fluidMonitor ?: return null
        return monitor.extractItems(toExtract, action, MachineSource(this))
    }

    protected fun extractGas(toExtract: IAEFluidStack?,
                             action: Actionable?): IAEFluidStack? {
        if (gridBlock == null || facingGasTank == null) return null
        val monitor = gridBlock?.fluidMonitor ?: return null
        return monitor.extractItems(toExtract, action, MachineSource(this))
    }

    override fun getActionableNode(): IGridNode {
        return node!!
    }

    abstract override fun getBoxes(bch: IPartCollisionHelper)
    override fun getBreakingTexture(): IIcon? {
        return TextureManager.BUS_SIDE.texture
    }

    override fun getCableConnectionType(dir: ForgeDirection): AECableType {
        return AECableType.GLASS
    }

    open fun getClientGuiElement(player: EntityPlayer): Any? {
        return null
    }

    override fun getDrops(drops: MutableList<ItemStack>, wrenched: Boolean) {}
    override fun getExternalFacingNode(): IGridNode? {
        return null
    }

    @Optional.Method(modid = "MekanismAPI|gas")
    fun getFacingGasTank(): IGasHandler? {
        return facingGasTank as IGasHandler?
    }

    override fun getGridNode(): IGridNode? {
        return node
    }

    override fun getGridNode(dir: ForgeDirection?): IGridNode? {
        return node
    }

    override fun getItemStack(type: PartItemStack?): ItemStack? {
        return `is`
    }

    override fun getLightLevel(): Int {
        return 0
    }

    val location: DimensionalCoord
        get() = DimensionalCoord(tile!!.worldObj, tile!!.xCoord,
                tile!!.yCoord, tile!!.zCoord)

    open fun getServerGuiElement(player: EntityPlayer?): Any? {
        return null
    }

    open fun getWailaBodey(tag: NBTTagCompound, oldList: MutableList<String>): List<String> {
        return oldList
    }

    open fun getWailaTag(tag: NBTTagCompound): NBTTagCompound {
        return tag
    }

    open fun initializePart(partStack: ItemStack) {
        if (partStack.hasTagCompound()) {
            readFromNBT(partStack.tagCompound)
        }
    }

    protected fun injectFluid(toInject: IAEFluidStack?,
                              action: Actionable?): IAEFluidStack? {
        if (gridBlock == null || facingTank == null) {
            return toInject
        }
        val monitor = gridBlock?.fluidMonitor ?: return toInject
        return monitor.injectItems(toInject, action, MachineSource(this))
    }

    protected fun injectGas(toInject: IAEFluidStack?, action: Actionable?): IAEFluidStack? {
        if (gridBlock == null || facingGasTank == null) {
            return toInject
        }
        val monitor = gridBlock?.fluidMonitor ?: return toInject
        return monitor.injectItems(toInject, action, MachineSource(this))
    }

    override fun isActive(): Boolean {
        return if (node != null) node!!.isActive else isActive
    }

    override fun isLadder(entity: EntityLivingBase): Boolean {
        return false
    }

    override fun isPowered(): Boolean {
        return isPowerd
    }

    override fun isProvidingStrongPower(): Int {
        return 0
    }

    override fun isProvidingWeakPower(): Int {
        return 0
    }

    override fun isSolid(): Boolean {
        return false
    }

    override fun onActivate(player: EntityPlayer?, pos: Vec3?): Boolean {
        if (player != null && player is EntityPlayerMP) getGuiId(this)?.let {
            launchGui(it, player,
                hostTile!!.worldObj, hostTile!!.xCoord,
                hostTile!!.yCoord, hostTile!!.zCoord)
        }
        return true
    }

    override fun onEntityCollision(entity: Entity) {}
    val isValid: Boolean
        get() = if (hostTile != null && hostTile!!.hasWorldObj()) {
            val loc = location
            val host = hostTile!!.worldObj.getTileEntity(loc.x, loc.y, loc.z)
            if (host is IPartHost) {
                (host as IPartHost).getPart(side) === this
            } else false
        } else false

    override fun onNeighborChanged() {
        if (hostTile == null) return
        val world = hostTile!!.worldObj
        val x = hostTile!!.xCoord
        val y = hostTile!!.yCoord
        val z = hostTile!!.zCoord
        val tileEntity = world.getTileEntity(x + side!!.offsetX, y
                + side!!.offsetY, z + side!!.offsetZ)
        facingTank = null
        if (tileEntity is IFluidHandler) facingTank = tileEntity
        if (Integration.Mods.MEKANISMGAS.isEnabled) updateCheckGasTank(tileEntity)
        isRedstonePowered = (world.isBlockIndirectlyGettingPowered(x, y, z)
                || world.isBlockIndirectlyGettingPowered(x, y + 1, z))
    }

    @Optional.Method(modid = "MekanismAPI|gas")
    private fun updateCheckGasTank(tile: TileEntity?) {
        facingGasTank = null
        if (tile != null && tile is IGasHandler) {
            facingGasTank = tile
        }
    }

    override fun onPlacement(player: EntityPlayer, held: ItemStack,
                             side: ForgeDirection) {
        owner = player
    }

    override fun onShiftActivate(player: EntityPlayer, pos: Vec3): Boolean {
        return false
    }

    override fun randomDisplayTick(world: World, x: Int, y: Int, z: Int, r: Random) {}
    override fun readFromNBT(data: NBTTagCompound) {
        if (data.hasKey("node") && node != null) {
            node!!.loadFromNBT("node0", data.getCompoundTag("node"))
            node!!.updateState()
        }
    }

    @Throws(IOException::class)
    override fun readFromStream(data: ByteBuf): Boolean {
        isActive = data.readBoolean()
        isPowerd = data.readBoolean()
        return true
    }

    override fun removeFromWorld() {
        if (node != null) node!!.destroy()
    }

    @SideOnly(Side.CLIENT)
    override fun renderDynamic(x: Double, y: Double, z: Double,
                               rh: IPartRenderHelper, renderer: RenderBlocks) {
    }

    @SideOnly(Side.CLIENT)
    abstract override fun renderInventory(rh: IPartRenderHelper,
                                          renderer: RenderBlocks)

    @SideOnly(Side.CLIENT)
    fun renderInventoryBusLights(rh: IPartRenderHelper,
                                 renderer: RenderBlocks?) {
        val ts = Tessellator.instance
        rh.setInvColor(0xFFFFFF)
        val otherIcon = TextureManager.BUS_COLOR.textures[0]
        val side = TextureManager.BUS_SIDE.texture
        rh.setTexture(otherIcon, otherIcon, side, side, otherIcon, otherIcon)
        rh.renderInventoryBox(renderer)
        ts.setBrightness(13 shl 20 or 13 shl 4)
        rh.setInvColor(AEColor.Transparent.blackVariant)
        rh.renderInventoryFace(TextureManager.BUS_COLOR.textures[1], ForgeDirection.UP, renderer)
        rh.renderInventoryFace(TextureManager.BUS_COLOR.textures[1], ForgeDirection.DOWN, renderer)
        rh.renderInventoryFace(TextureManager.BUS_COLOR.textures[1], ForgeDirection.NORTH, renderer)
        rh.renderInventoryFace(TextureManager.BUS_COLOR.textures[1], ForgeDirection.EAST, renderer)
        rh.renderInventoryFace(TextureManager.BUS_COLOR.textures[1], ForgeDirection.SOUTH, renderer)
        rh.renderInventoryFace(TextureManager.BUS_COLOR.textures[1], ForgeDirection.WEST, renderer)
    }

    @SideOnly(Side.CLIENT)
    abstract override fun renderStatic(x: Int, y: Int, z: Int,
                                       rh: IPartRenderHelper, renderer: RenderBlocks)

    @SideOnly(Side.CLIENT)
    fun renderStaticBusLights(x: Int, y: Int, z: Int,
                              rh: IPartRenderHelper, renderer: RenderBlocks?) {
        val ts = Tessellator.instance
        val otherIcon = TextureManager.BUS_COLOR.textures[0]
        val side = TextureManager.BUS_SIDE.texture
        rh.setTexture(otherIcon, otherIcon, side, side, otherIcon, otherIcon)
        rh.renderBlock(x, y, z, renderer)
        if (isActive()) {
            ts.setBrightness(13 shl 20 or 13 shl 4)
            ts.setColorOpaque_I(host!!.color.blackVariant)
        } else {
            ts.setColorOpaque_I(0x000000)
        }
        rh.renderFace(x, y, z, TextureManager.BUS_COLOR.textures[1], ForgeDirection.UP, renderer)
        rh.renderFace(x, y, z, TextureManager.BUS_COLOR.textures[1], ForgeDirection.DOWN, renderer)
        rh.renderFace(x, y, z, TextureManager.BUS_COLOR.textures[1], ForgeDirection.NORTH, renderer)
        rh.renderFace(x, y, z, TextureManager.BUS_COLOR.textures[1], ForgeDirection.EAST, renderer)
        rh.renderFace(x, y, z, TextureManager.BUS_COLOR.textures[1], ForgeDirection.SOUTH, renderer)
        rh.renderFace(x, y, z, TextureManager.BUS_COLOR.textures[1], ForgeDirection.WEST, renderer)
    }

    override fun requireDynamicRender(): Boolean {
        return false
    }

    protected fun saveData() {
        if (host != null) host!!.markForSave()
    }

    override fun securityBreak() {
        host!!.removePart(side, false) // TODO drop item
    }

    protected fun setActive(_active: Boolean) {
        isActive = _active
    }

    override fun setPartHostInfo(_side: ForgeDirection, _host: IPartHost,
                                 _tile: TileEntity) {
        side = _side
        host = _host
        tile = _tile
        hostTile = _tile
        setPower(null)
    }

    @MENetworkEventSubscribe
    open fun setPower(notUsed: MENetworkPowerStatusChange?) {
        if (node != null) {
            isActive = node!!.isActive
            val grid = node!!.grid
            if (grid != null) {
                val energy = grid.getCache<IEnergyGrid>(IEnergyGrid::class.java)
                if (energy != null) isPowerd = energy.isNetworkPowered
            }
            host!!.markForUpdate()
        }
    }

    override fun writeToNBT(data: NBTTagCompound) {
        if (node != null) {
            val nodeTag = NBTTagCompound()
            node!!.saveToNBT("node0", nodeTag)
            data.setTag("node", nodeTag)
        }
    }

    @Throws(IOException::class)
    override fun writeToStream(data: ByteBuf) {
        data.writeBoolean(node != null && node!!.isActive)
        data.writeBoolean(isPowerd)
    }

}