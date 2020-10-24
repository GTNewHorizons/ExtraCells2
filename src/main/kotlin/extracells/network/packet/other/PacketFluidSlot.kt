package extracells.network.packet.other

import extracells.network.AbstractPacket
import extracells.part.PartECBase
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fluids.Fluid
import java.util.*

class PacketFluidSlot : AbstractPacket {
    private var index = 0
    private var fluid: Fluid? = null
    private var partOrBlock: IFluidSlotPartOrBlock? = null
    private var filterFluids: MutableList<Fluid?>? = null

    constructor() {}
    constructor(_partOrBlock: IFluidSlotPartOrBlock?, _index: Int,
                _fluid: Fluid?, _player: EntityPlayer?) : super(_player) {
        mode = 0
        partOrBlock = _partOrBlock
        index = _index
        fluid = _fluid
    }

    constructor(_filterFluids: MutableList<Fluid?>?) {
        mode = 1
        filterFluids = _filterFluids
    }

    override fun execute() {
        when (mode) {
            0 -> partOrBlock!!.setFluid(index, fluid, player)
            1 -> {
                val gui: Gui = Minecraft.getMinecraft().currentScreen
                if (gui is IFluidSlotGui) {
                    val partGui = gui as IFluidSlotGui
                    partGui.updateFluids(filterFluids)
                }
            }
        }
    }

    override fun readData(`in`: ByteBuf) {
        when (mode) {
            0 -> {
                if (`in`.readBoolean()) partOrBlock = AbstractPacket.Companion.readPart(
                        `in`) as IFluidSlotPartOrBlock else partOrBlock = AbstractPacket.Companion.readTileEntity(
                        `in`) as IFluidSlotPartOrBlock
                index = `in`.readInt()
                fluid = AbstractPacket.Companion.readFluid(`in`)
            }
            1 -> {
                filterFluids = ArrayList()
                val size = `in`.readInt()
                var i = 0
                while (i < size) {
                    filterFluids.add(AbstractPacket.Companion.readFluid(`in`))
                    i++
                }
            }
        }
    }

    override fun writeData(out: ByteBuf) {
        when (mode) {
            0 -> {
                if (partOrBlock is PartECBase) {
                    out.writeBoolean(true)
                    AbstractPacket.Companion.writePart(partOrBlock as PartECBase?, out)
                } else {
                    out.writeBoolean(false)
                    AbstractPacket.Companion.writeTileEntity(partOrBlock as TileEntity?, out)
                }
                out.writeInt(index)
                AbstractPacket.Companion.writeFluid(fluid, out)
            }
            1 -> {
                out.writeInt(filterFluids!!.size)
                var i = 0
                while (i < filterFluids!!.size) {
                    AbstractPacket.Companion.writeFluid(filterFluids!![i], out)
                    i++
                }
            }
        }
    }
}