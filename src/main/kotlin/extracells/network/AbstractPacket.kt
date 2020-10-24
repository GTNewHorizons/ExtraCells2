package extracells.network

import appeng.api.parts.IPartHost
import com.google.common.base.Charsets
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.part.PartECBase
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry

abstract class AbstractPacket : IMessage {
    protected var player: EntityPlayer?
    protected var mode: Byte = 0

    constructor() {
        player = null
    }

    constructor(_player: EntityPlayer?) {
        player = _player
    }

    abstract fun execute()
    override fun fromBytes(`in`: ByteBuf) {
        mode = `in`.readByte()
        player = readPlayer(`in`)
        readData(`in`)
    }

    abstract fun readData(`in`: ByteBuf)
    fun sendPacketToAllPlayers() {
        ChannelHandler.sendPacketToAllPlayers(this)
    }

    fun sendPacketToPlayer(player: EntityPlayer?) {
        ChannelHandler.sendPacketToPlayer(this, player)
    }

    fun sendPacketToPlayersAround(point: TargetPoint?) {
        ChannelHandler.sendPacketToPlayersAround(this, point)
    }

    fun sendPacketToServer() {
        ChannelHandler.sendPacketToServer(this)
    }

    override fun toBytes(out: ByteBuf) {
        out.writeByte(mode.toInt())
        writePlayer(player, out)
        writeData(out)
    }

    abstract fun writeData(out: ByteBuf)

    companion object {
        @get:SideOnly(Side.CLIENT)
        val clientWorld: World
            get() = Minecraft.getMinecraft().theWorld

        fun readFluid(`in`: ByteBuf): Fluid {
            return FluidRegistry.getFluid(readString(`in`))
        }

        fun readPart(`in`: ByteBuf): PartECBase {
            return (readTileEntity(`in`) as IPartHost)
                    .getPart(ForgeDirection.getOrientation(`in`.readByte().toInt())) as PartECBase
        }

        fun readPlayer(`in`: ByteBuf): EntityPlayer? {
            if (!`in`.readBoolean()) {
                return null
            }
            val playerWorld = readWorld(`in`)
            return playerWorld!!.getPlayerEntityByName(readString(`in`))
        }

        fun readString(`in`: ByteBuf): String {
            val stringBytes = ByteArray(`in`.readInt())
            `in`.readBytes(stringBytes)
            return String(stringBytes, Charsets.UTF_8)
        }

        fun readTileEntity(`in`: ByteBuf): TileEntity {
            return readWorld(`in`)!!.getTileEntity(`in`.readInt(), `in`.readInt(),
                    `in`.readInt())
        }

        fun readWorld(`in`: ByteBuf): World? {
            val world = DimensionManager.getWorld(`in`.readInt())
            return if (FMLCommonHandler.instance().side == Side.CLIENT) {
                world ?: clientWorld
            } else world
        }

        fun writeFluid(fluid: Fluid?, out: ByteBuf) {
            if (fluid == null) {
                writeString("", out)
                return
            }
            writeString(fluid.name, out)
        }

        fun writePart(part: PartECBase?, out: ByteBuf) {
            writeTileEntity(part.getHost().tile, out)
            out.writeByte(part.getSide().ordinal)
        }

        fun writePlayer(player: EntityPlayer?, out: ByteBuf) {
            if (player == null) {
                out.writeBoolean(false)
                return
            }
            out.writeBoolean(true)
            writeWorld(player.worldObj, out)
            writeString(player.commandSenderName, out)
        }

        fun writeString(string: String, out: ByteBuf) {
            val stringBytes: ByteArray
            stringBytes = string.toByteArray(Charsets.UTF_8)
            out.writeInt(stringBytes.size)
            out.writeBytes(stringBytes)
        }

        fun writeTileEntity(tileEntity: TileEntity?, out: ByteBuf) {
            writeWorld(tileEntity!!.worldObj, out)
            out.writeInt(tileEntity.xCoord)
            out.writeInt(tileEntity.yCoord)
            out.writeInt(tileEntity.zCoord)
        }

        fun writeWorld(world: World, out: ByteBuf) {
            out.writeInt(world.provider.dimensionId)
        }
    }
}