package extracells.part

import appeng.api.config.Actionable
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
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.container.ContainerFluidTerminal
import extracells.gui.GuiFluidTerminal
import extracells.network.packet.part.PacketFluidTerminal
import extracells.render.TextureManager
import extracells.util.FluidUtil
import extracells.util.PermissionUtil
import extracells.util.inventory.ECPrivateInventory
import extracells.util.inventory.IInventoryUpdateReceiver
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.Vec3
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import java.util.*

class PartFluidTerminal : PartECBase(), IGridTickable, IInventoryUpdateReceiver {
    protected var currentFluid: Fluid? = null
    private val containers: MutableList<Any> = ArrayList()
    protected var inventory: ECPrivateInventory = object : ECPrivateInventory(
            "extracells.part.fluid.terminal", 2, 64, this) {
        override fun isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean {
            return isItemValidForInputSlot(i, itemStack)
        }
    }

    protected fun isItemValidForInputSlot(i: Int, itemStack: ItemStack?): Boolean {
        return FluidUtil.isFluidContainer(itemStack)
    }

    protected var machineSource = MachineSource(this)
    override fun getDrops(drops: MutableList<ItemStack>, wrenched: Boolean) {
        for (stack in inventory.slots) {
            if (stack == null) continue
            drops.add(stack)
        }
    }

    override fun getLightLevel(): Int {
        return if (this.isPowered) 9 else 0
    }

    fun addContainer(containerTerminalFluid: ContainerFluidTerminal) {
        containers.add(containerTerminalFluid)
        sendCurrentFluid()
    }

    //	public void addContainer(ContainerGasTerminal containerTerminalGas) {
    //		this.containers.add(containerTerminalGas);
    //		sendCurrentFluid();
    //	}
    override fun cableConnectionRenderTo(): Int {
        return 1
    }

    fun decreaseFirstSlot() {
        val slot = inventory.getStackInSlot(0)
        slot!!.stackSize--
        if (slot.stackSize <= 0) inventory.setInventorySlotContents(0, null)
    }

    fun doWork() {
        val secondSlot = inventory.getStackInSlot(1)
        if (secondSlot != null && secondSlot.stackSize >= secondSlot.maxStackSize) return
        var container = inventory.getStackInSlot(0)
        if (!FluidUtil.isFluidContainer(container)) return
        container = container!!.copy()
        container.stackSize = 1
        val gridBlock = gridBlock ?: return
        val monitor = gridBlock.fluidMonitor ?: return
        if (FluidUtil.isEmpty(container)) {
            if (currentFluid == null) return
            val capacity = FluidUtil.getCapacity(container)
            val result = monitor.extractItems(FluidUtil.createAEFluidStack(currentFluid, capacity.toLong()),
                    Actionable.SIMULATE, machineSource)
            val proposedAmount = if (result == null) 0 else Math.min(capacity.toLong(), result.stackSize).toInt()
            if (proposedAmount == 0) return
            val filledContainer = FluidUtil.fillStack(container, FluidStack(currentFluid, proposedAmount))
            if (filledContainer!!.getLeft()!! > proposedAmount) return
            if (fillSecondSlot(filledContainer.getRight())) {
                monitor.extractItems(FluidUtil.createAEFluidStack(currentFluid, filledContainer.getLeft()),
                        Actionable.MODULATE, machineSource)
                decreaseFirstSlot()
            }
        } else {
            val containerFluid = FluidUtil.getFluidFromContainer(container)
            val notInjected = monitor.injectItems(FluidUtil.createAEFluidStack(containerFluid), Actionable.SIMULATE,
                    machineSource)
            if (notInjected != null) return
            val drainedContainer = FluidUtil.drainStack(container, containerFluid)
            val emptyContainer = drainedContainer!!.getRight()
            if (emptyContainer == null || fillSecondSlot(emptyContainer)) {
                monitor.injectItems(FluidUtil.createAEFluidStack(containerFluid), Actionable.MODULATE, machineSource)
                decreaseFirstSlot()
            }
        }
    }

    fun fillSecondSlot(itemStack: ItemStack?): Boolean {
        if (itemStack == null) return false
        val secondSlot = inventory.getStackInSlot(1)
        return if (secondSlot == null) {
            inventory.setInventorySlotContents(1, itemStack)
            true
        } else {
            if (!secondSlot.isItemEqual(itemStack) || !ItemStack.areItemStackTagsEqual(itemStack,
                            secondSlot)) return false
            inventory.incrStackSize(1, itemStack.stackSize)
            true
        }
    }

    override fun getBoxes(bch: IPartCollisionHelper) {
        bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0)
        bch.addBox(4.0, 4.0, 13.0, 12.0, 12.0, 14.0)
        bch.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 13.0)
    }

    override fun getClientGuiElement(player: EntityPlayer): Any? {
        return GuiFluidTerminal(this, player)
    }

    fun getInventory(): IInventory {
        return inventory
    }

    override val powerUsage: Double
        get() = 0.5

    override fun getServerGuiElement(player: EntityPlayer): Any? {
        return ContainerFluidTerminal(this, player)
    }

    override fun getTickingRequest(node: IGridNode): TickingRequest {
        return TickingRequest(1, 20, false, false)
    }

    override fun onActivate(player: EntityPlayer, pos: Vec3): Boolean {
        return if (isActive && (PermissionUtil.hasPermission(player, SecurityPermissions.INJECT,
                        this as IPart) || PermissionUtil.hasPermission(player, SecurityPermissions.EXTRACT,
                        this as IPart))) super.onActivate(player, pos) else false
    }

    override fun onInventoryChanged() {
        saveData()
    }

    override fun readFromNBT(data: NBTTagCompound) {
        super.readFromNBT(data)
        inventory.readFromNBT(data.getTagList("inventory", 10))
    }

    fun removeContainer(containerTerminalFluid: ContainerFluidTerminal) {
        containers.remove(containerTerminalFluid)
    }

    //	public void removeContainer(ContainerGasTerminal containerTerminalGas) {
    //		this.containers.remove(containerTerminalGas);
    //	}
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
        rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.textures[0],
                ForgeDirection.SOUTH, renderer)
        rh.setInvColor(AEColor.Transparent.mediumVariant)
        rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.textures[1],
                ForgeDirection.SOUTH, renderer)
        rh.setInvColor(AEColor.Transparent.whiteVariant)
        rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.textures[2],
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(5f, 5f, 12f, 11f, 11f, 13f)
        renderInventoryBusLights(rh, renderer)
    }

    @SideOnly(Side.CLIENT)
    override fun renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper, renderer: RenderBlocks) {
        val ts = Tessellator.instance
        val side = TextureManager.TERMINAL_SIDE.texture
        rh.setTexture(side)
        rh.setBounds(4f, 4f, 13f, 12f, 12f, 14f)
        rh.renderBlock(x, y, z, renderer)
        rh.setTexture(side, side, side, TextureManager.BUS_BORDER.texture, side, side)
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 16f)
        rh.renderBlock(x, y, z, renderer)
        if (isActive) Tessellator.instance.setBrightness(13 shl 20 or 13 shl 4)
        ts.setColorOpaque_I(0xFFFFFF)
        rh.renderFace(x, y, z, TextureManager.BUS_BORDER.texture, ForgeDirection.SOUTH, renderer)
        val host = host
        rh.setBounds(3f, 3f, 15f, 13f, 13f, 16f)
        ts.setColorOpaque_I(host!!.color.blackVariant)
        rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.textures[0], ForgeDirection.SOUTH, renderer)
        ts.setColorOpaque_I(host.color.mediumVariant)
        rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.textures[1], ForgeDirection.SOUTH, renderer)
        ts.setColorOpaque_I(host.color.whiteVariant)
        rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.textures[2], ForgeDirection.SOUTH, renderer)
        rh.setBounds(5f, 5f, 12f, 11f, 11f, 13f)
        renderStaticBusLights(x, y, z, rh, renderer)
    }

    fun sendCurrentFluid() {
        for (containerFluidTerminal in containers) {
            sendCurrentFluid(containerFluidTerminal)
        }
    }

    fun sendCurrentFluid(container: Any?) {
        if (container is ContainerFluidTerminal) {
            val containerFluidTerminal = container
            PacketFluidTerminal(containerFluidTerminal.player, currentFluid).sendPacketToPlayer(
                    containerFluidTerminal.player)
        }
        //		else if(container instanceof ContainerGasTerminal){
//			ContainerGasTerminal containerGasTerminal = (ContainerGasTerminal) container;
//			new PacketFluidTerminal(containerGasTerminal.getPlayer(), this.currentFluid).sendPacketToPlayer(containerGasTerminal.getPlayer());
//		}
    }

    fun setCurrentFluid(_currentFluid: Fluid?) {
        currentFluid = _currentFluid
        sendCurrentFluid()
    }

    override fun tickingRequest(node: IGridNode,
                                TicksSinceLastCall: Int): TickRateModulation {
        doWork()
        return TickRateModulation.FASTER
    }

    override fun writeToNBT(data: NBTTagCompound) {
        super.writeToNBT(data)
        data.setTag("inventory", inventory.writeToNBT())
    }
}