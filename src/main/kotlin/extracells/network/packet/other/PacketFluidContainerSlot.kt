package extracells.network.packet.other

import cpw.mods.fml.common.network.ByteBufUtils
import extracells.network.AbstractPacket
import extracells.tileentity.TileEntityFluidFiller
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

class PacketFluidContainerSlot : AbstractPacket {
    private var container: ItemStack? = null
    private var fluidFiller: TileEntityFluidFiller? = null

    constructor() {}
    constructor(_fluidFiller: TileEntityFluidFiller?,
                _container: ItemStack?, _player: EntityPlayer?) : super(_player) {
        mode = 0
        fluidFiller = _fluidFiller
        container = _container
    }

    override fun execute() {
        when (mode) {
            0 -> {
                container!!.stackSize = 1
                fluidFiller!!.containerItem = container
                if (fluidFiller!!.hasWorldObj()) fluidFiller!!.worldObj.markBlockForUpdate(
                        fluidFiller!!.xCoord, fluidFiller!!.yCoord,
                        fluidFiller!!.zCoord)
                fluidFiller!!.postUpdateEvent()
            }
        }
    }

    override fun readData(`in`: ByteBuf) {
        when (mode) {
            0 -> {
                fluidFiller = AbstractPacket.Companion.readTileEntity(`in`) as TileEntityFluidFiller
                container = ByteBufUtils.readItemStack(`in`)
            }
        }
    }

    override fun writeData(out: ByteBuf) {
        when (mode) {
            0 -> {
                AbstractPacket.Companion.writeTileEntity(fluidFiller, out)
                ByteBufUtils.writeItemStack(out, container)
            }
        }
    }
}