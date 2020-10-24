package extracells.network.packet.part

import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.container.ContainerFluidInterface
import extracells.gui.GuiFluidInterface
import extracells.network.AbstractPacket
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack

class PacketFluidInterface : AbstractPacket {
    var tank: Array<FluidStack?>
    var filter: Array<Int?>
    var fluidID = 0
    var filterSlot = 0

    constructor() {}
    constructor(_tank: Array<FluidStack?>, _filter: Array<Int?>,
                _player: EntityPlayer?) : super(_player) {
        mode = 0
        tank = _tank
        filter = _filter
    }

    constructor(_fluidID: Int, _filterSlot: Int,
                _player: EntityPlayer?) : super(_player) {
        mode = 1
        fluidID = _fluidID
        filterSlot = _filterSlot
    }

    override fun execute() {
        when (mode) {
            0 -> mode0()
            1 -> if (player!!.openContainer != null
                    && player!!.openContainer is ContainerFluidInterface) {
                val container = player!!.openContainer as ContainerFluidInterface
                container.fluidInterface.setFilter(
                        ForgeDirection.getOrientation(filterSlot),
                        FluidRegistry.getFluid(fluidID))
            }
            else -> {
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private fun mode0() {
        val p: EntityPlayer = Minecraft.getMinecraft().thePlayer
        if (p.openContainer != null
                && p.openContainer is ContainerFluidInterface) {
            val container = p.openContainer as ContainerFluidInterface
            if (Minecraft.getMinecraft().currentScreen != null
                    && Minecraft.getMinecraft().currentScreen is GuiFluidInterface) {
                val gui = Minecraft
                        .getMinecraft().currentScreen as GuiFluidInterface
                for (i in tank.indices) {
                    container.fluidInterface.setFluidTank(
                            ForgeDirection.getOrientation(i), tank[i])
                }
                for (i in filter.indices) {
                    if (gui.filter[i] != null) gui.filter[i].fluid = FluidRegistry
                            .getFluid(filter[i]!!)
                }
            }
        }
    }

    override fun readData(`in`: ByteBuf) {
        when (mode) {
            0 -> {
                val tag = ByteBufUtils.readTag(`in`)
                tank = arrayOfNulls(tag.getInteger("lengthTank"))
                run {
                    var i = 0
                    while (i < this.tank.size) {
                        if (tag.hasKey("tank#$i")) this.tank[i] = FluidStack.loadFluidStackFromNBT(tag
                                .getCompoundTag("tank#$i")) else this.tank[i] = null
                        i++
                    }
                }
                filter = arrayOfNulls(tag.getInteger("lengthFilter"))
                var i = 0
                while (i < filter.size) {
                    if (tag.hasKey("filter#$i")) filter[i] = tag.getInteger("filter#$i") else filter[i] = -1
                    i++
                }
            }
            1 -> {
                filterSlot = `in`.readInt()
                fluidID = `in`.readInt()
            }
            else -> {
            }
        }
    }

    override fun writeData(out: ByteBuf) {
        when (mode) {
            0 -> {
                val tag = NBTTagCompound()
                tag.setInteger("lengthTank", tank.size)
                run {
                    var i = 0
                    while (i < this.tank.size) {
                        if (this.tank[i] != null) {
                            tag.setTag("tank#$i",
                                    this.tank[i]!!.writeToNBT(NBTTagCompound()))
                        }
                        i++
                    }
                }
                tag.setInteger("lengthFilter", filter.size)
                var i = 0
                while (i < filter.size) {
                    if (filter[i] != null) {
                        tag.setInteger("filter#$i", filter[i]!!)
                    }
                    i++
                }
                ByteBufUtils.writeTag(out, tag)
            }
            1 -> {
                out.writeInt(filterSlot)
                out.writeInt(fluidID)
            }
            else -> {
            }
        }
    }
}