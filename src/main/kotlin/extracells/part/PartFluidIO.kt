package extracells.part

import appeng.api.AEApi
import appeng.api.config.RedstoneMode
import appeng.api.networking.IGridNode
import appeng.api.networking.ticking.IGridTickable
import appeng.api.networking.ticking.TickRateModulation
import appeng.api.networking.ticking.TickingRequest
import appeng.api.parts.IPartCollisionHelper
import appeng.api.parts.IPartHost
import appeng.api.parts.IPartRenderHelper
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.container.ContainerBusFluidIO
import extracells.gui.GuiBusFluidIO
import extracells.item.ItemPartECBase
import extracells.network.packet.other.IFluidSlotPartOrBlock
import extracells.network.packet.other.PacketFluidSlot
import extracells.network.packet.part.PacketBusFluidIO
import extracells.util.inventory.ECPrivateInventory
import extracells.util.inventory.IInventoryUpdateReceiver
import io.netty.buffer.ByteBuf
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Vec3
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import java.io.IOException
import java.util.*

abstract class PartFluidIO : PartECBase(), IGridTickable, IInventoryUpdateReceiver, IFluidSlotPartOrBlock {
    var filterFluids = arrayOfNulls<Fluid>(9)
    var redstoneMode = RedstoneMode.IGNORE
        private set
    protected var filterSize: Byte = 0
    var speedState: Byte = 0
        protected set
    protected var redstoneControlled = false
    private var lastRedstone = false
    val upgradeInventory: ECPrivateInventory = object : ECPrivateInventory("", 4,
            1, this) {
        override fun isItemValidForSlot(i: Int, itemStack: ItemStack?): Boolean {
            if (itemStack == null) return false
            return when {
                AEApi.instance().definitions().materials().cardCapacity().isSameAs(
                        itemStack) -> true
                AEApi.instance().definitions().materials().cardSpeed().isSameAs(
                        itemStack) -> true
                else -> AEApi.instance().definitions().materials().cardRedstone().isSameAs(
                        itemStack)
            }
        }
    }

    override fun getDrops(drops: MutableList<ItemStack>, wrenched: Boolean) {
        for (stack in drops) {
            if (stack.item.javaClass == ItemPartECBase::class.java) {
                stack.stackTagCompound = null
            }
        }
        for (stack in upgradeInventory.slots) {
            if (stack == null) continue
            drops.add(stack)
        }
    }

    override fun cableConnectionRenderTo(): Int {
        return 5
    }

    private fun canDoWork(): Boolean {
        val redstonePowered = isRedstonePowered
        return if (!redstoneControlled) true else when (redstoneMode) {
            RedstoneMode.IGNORE -> true
            RedstoneMode.LOW_SIGNAL -> !redstonePowered
            RedstoneMode.HIGH_SIGNAL -> redstonePowered
            RedstoneMode.SIGNAL_PULSE -> false
        }
    }

    abstract fun doWork(rate: Int, TicksSinceLastCall: Int): Boolean
    abstract override fun getBoxes(bch: IPartCollisionHelper)
    override fun getClientGuiElement(player: EntityPlayer): Any? {
        return GuiBusFluidIO(this, player)
    }

    override fun getServerGuiElement(player: EntityPlayer?): Any? {
        return ContainerBusFluidIO(this, player)
    }

    override fun getTickingRequest(node: IGridNode): TickingRequest {
        return TickingRequest(1, 20, false, false)
    }

    override fun getWailaBodey(tag: NBTTagCompound, oldList: MutableList<String>): List<String> {
        if (tag.hasKey("speed")) oldList.add(tag.getInteger("speed").toString() + "mB/t") else oldList.add("125mB/t")
        return oldList
    }

    override fun getWailaTag(tag: NBTTagCompound): NBTTagCompound {
        tag.setInteger("speed", 125 + speedState * 125)
        return tag
    }

    fun loopRedstoneMode(player: EntityPlayer?) {
        if (redstoneMode.ordinal + 1 < RedstoneMode.values().size) redstoneMode = RedstoneMode.values()[redstoneMode.ordinal + 1] else redstoneMode = RedstoneMode.values()[0]
        PacketBusFluidIO(redstoneMode).sendPacketToPlayer(player)
        saveData()
    }

    override fun onActivate(player: EntityPlayer?, pos: Vec3?): Boolean {
        val activate = super.onActivate(player, pos)
        onInventoryChanged()
        return activate
    }

    override fun onInventoryChanged() {
        filterSize = 0
        redstoneControlled = false
        speedState = 0
        for (i in 0 until upgradeInventory.sizeInventory) {
            val currentStack = upgradeInventory.getStackInSlot(i)
            if (currentStack != null) {
                if (AEApi.instance().definitions().materials().cardCapacity().isSameAs(currentStack)) filterSize++
                if (AEApi.instance().definitions().materials().cardRedstone().isSameAs(
                                currentStack)) redstoneControlled = true
                if (AEApi.instance().definitions().materials().cardSpeed().isSameAs(currentStack)) speedState++
            }
        }
        try {
            if (host?.location?.world?.isRemote != false) return
        } catch (ignored: Throwable) {
        }
        PacketBusFluidIO(filterSize).sendPacketToAllPlayers()
        PacketBusFluidIO(redstoneControlled).sendPacketToAllPlayers()
        saveData()
    }

    override fun onNeighborChanged() {
        super.onNeighborChanged()
        val redstonePowered = isRedstonePowered
        lastRedstone = redstonePowered
    }

    override fun readFromNBT(data: NBTTagCompound) {
        super.readFromNBT(data)
        redstoneMode = RedstoneMode.values()[data
                .getInteger("redstoneMode")]
        for (i in 0..8) {
            filterFluids[i] = FluidRegistry.getFluid(data
                    .getString("FilterFluid#$i"))
        }
        upgradeInventory.readFromNBT(data.getTagList("upgradeInventory",
                10))
        onInventoryChanged()
    }

    @Throws(IOException::class)
    override fun readFromStream(data: ByteBuf): Boolean {
        return super.readFromStream(data)
    }

    @SideOnly(Side.CLIENT)
    override fun renderDynamic(x: Double, y: Double, z: Double,
                               rh: IPartRenderHelper, renderer: RenderBlocks) {
    }

    @SideOnly(Side.CLIENT)
    abstract override fun renderInventory(rh: IPartRenderHelper,
                                          renderer: RenderBlocks)

    @SideOnly(Side.CLIENT)
    abstract override fun renderStatic(x: Int, y: Int, z: Int,
                                       rh: IPartRenderHelper, renderer: RenderBlocks)

    fun sendInformation(player: EntityPlayer?) {
        PacketFluidSlot(Arrays.asList(*filterFluids))
                .sendPacketToPlayer(player)
        PacketBusFluidIO(redstoneMode).sendPacketToPlayer(player)
        PacketBusFluidIO(filterSize).sendPacketToPlayer(player)
    }

    override fun setFluid(index: Int, fluid: Fluid?, player: EntityPlayer?) {
        filterFluids[index] = fluid
        PacketFluidSlot(Arrays.asList(*filterFluids))
                .sendPacketToPlayer(player)
        saveData()
    }

    override fun setPartHostInfo(_side: ForgeDirection, _host: IPartHost,
                                 _tile: TileEntity) {
        super.setPartHostInfo(_side, _host, _tile)
        onInventoryChanged()
    }

    override fun tickingRequest(node: IGridNode,
                                TicksSinceLastCall: Int): TickRateModulation {
        return if (canDoWork()) if (doWork(125 + speedState * 125,
                        TicksSinceLastCall)) TickRateModulation.FASTER else TickRateModulation.SLOWER else TickRateModulation.SLOWER
    }

    override fun writeToNBT(data: NBTTagCompound) {
        super.writeToNBT(data)
        data.setInteger("redstoneMode", redstoneMode.ordinal)
        for (i in filterFluids.indices) {
            val fluid = filterFluids[i]
            if (fluid != null) data.setString("FilterFluid#$i", fluid.name) else data.setString("FilterFluid#$i", "")
        }
        data.setTag("upgradeInventory", upgradeInventory.writeToNBT())
    }

    @Throws(IOException::class)
    override fun writeToStream(data: ByteBuf) {
        super.writeToStream(data)
    }
}