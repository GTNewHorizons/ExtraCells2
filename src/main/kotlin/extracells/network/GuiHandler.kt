package extracells.network

import appeng.api.parts.IPartHost
import appeng.api.storage.IMEMonitor
import appeng.api.storage.data.IAEFluidStack
import cpw.mods.fml.common.network.IGuiHandler
import extracells.Extracells
import extracells.api.IFluidInterface
import extracells.api.IPortableFluidStorageCell
import extracells.api.IWirelessFluidTermHandler
import extracells.block.TGuiBlock
import extracells.container.ContainerFluidCrafter
import extracells.container.ContainerFluidFiller
import extracells.container.ContainerFluidInterface
import extracells.container.ContainerFluidStorage
import extracells.gui.GuiFluidCrafter
import extracells.gui.GuiFluidFiller
import extracells.gui.GuiFluidInterface
import extracells.gui.GuiFluidStorage
import extracells.part.PartECBase
import extracells.registries.BlockEnum
import extracells.tileentity.TileEntityFluidCrafter
import extracells.tileentity.TileEntityFluidFiller
import extracells.tileentity.TileEntityFluidInterface
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

object GuiHandler : IGuiHandler {
    fun getContainer(ID: Int, player: EntityPlayer?, args: Array<Any?>): Any? {
        if (ID > 6 || ID < 0)
            return null
        val fluidInventory: IMEMonitor<IAEFluidStack?>? = args[0] as? IMEMonitor<IAEFluidStack?>
        return when (ID) {
            0 -> {
                player?.let {ContainerFluidStorage(fluidInventory, it)}
            }
            1 -> {
                val handler: IWirelessFluidTermHandler = args[1] as IWirelessFluidTermHandler
                player?.let {ContainerFluidStorage(fluidInventory, it,handler)}
            }
            3 -> {
                val storageCell: IPortableFluidStorageCell = args[1] as IPortableFluidStorageCell
                player?.let {ContainerFluidStorage(fluidInventory, it,storageCell)}
            }
           // 4 -> {
           //   //  ContainerGasStorage(fluidInventory, player)
           // }
           // 5 -> {
           //     val handler: IWirelessGasTermHandler = args[1] as IWirelessGasTermHandler
           //   //  ContainerGasStorage(fluidInventory, player, handler)
           // }
           // 6 -> {
           //     val storageCell: IPortableGasStorageCell = args[1] as IPortableGasStorageCell
           //     ContainerGasStorage(fluidInventory, player, storageCell)
           // }
            else -> {
                null
            }
        }
    }

    private fun getGui(ID: Int, player: EntityPlayer): Any? {
        return when (ID) {
            0 -> {
                GuiFluidStorage(player, "extracells.part.fluid.terminal.name")
            }
            1 -> {
                GuiFluidStorage(player, "extracells.part.fluid.terminal.name")
            }
            3 -> {
                GuiFluidStorage(player, "extracells.item.storage.fluid.portable.name")
            }
//            4 -> {
//                GuiGasStorage(player, "extracells.part.gas.terminal.name")
//            }
//            5 -> {
//                GuiGasStorage(player, "extracells.part.gas.terminal.name")
//            }
//            6 -> {
//                GuiGasStorage(player, "extracells.item.storage.gas.portable.name")
//            }
            else -> {
                null
            }
        }
    }

    fun getGuiId(guiId: Int): Int = guiId + 6
    fun getGuiId(part: PartECBase): Int? = part.side?.ordinal
    private fun getPartContainer(side: ForgeDirection, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? = player?.let { ((world?.getTileEntity(x, y, z) as IPartHost).getPart(side) as PartECBase).getServerGuiElement(it) }
    private fun getPartGui(side: ForgeDirection, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? = player?.let { ((world?.getTileEntity(x, y, z) as IPartHost).getPart(side) as PartECBase).getClientGuiElement(it) }
    fun launchGui(ID: Int, player: EntityPlayer?, args: Array<Any?>) {
        temp = args
        player?.openGui(Extracells, ID, null, 0, 0, 0)
    }
    fun launchGui(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Any = player.openGui(Extracells, ID, world, x, y, z)
    var temp: Array<Any?> = emptyArray()
    override fun getClientGuiElement(ID: Int, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? {
        val gui: Any? = getGuiBlockElement(player, world, x, y, z)
        if (gui != null)
            return gui
        val side: ForgeDirection = ForgeDirection.getOrientation(ID)
        if (world?.getBlock(x, y, z) == BlockEnum.FLUIDCRAFTER.block) {
            val tileEntity: TileEntity? = world.getTileEntity(x, y, z)
            if (tileEntity == null || tileEntity !is TileEntityFluidCrafter) return null
            return GuiFluidCrafter(player?.inventory, tileEntity.getInventory())
        }
        if (world != null && world.getBlock(x, y, z) == BlockEnum.ECBASEBLOCK.block) {
            val tileEntity: TileEntity = world.getTileEntity(x, y, z) ?: return null
            if (tileEntity is TileEntityFluidInterface)
                return player?.let { GuiFluidInterface(it, tileEntity as IFluidInterface) } else if (tileEntity is TileEntityFluidFiller) return player?.let { GuiFluidFiller(it, tileEntity) }
            return null
        }
        if (world != null && side != ForgeDirection.UNKNOWN) return getPartGui(side, player, world, x, y, z)
        return player?.let { getGui(ID - 6, it) } as Any
    }

    override fun getServerGuiElement(ID: Int, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? {
        val con: Any? = getContainerBlockElement(player, world, x, y, z)
        if (con != null) return con
        val side: ForgeDirection = ForgeDirection.getOrientation(ID)
        if (world != null && world.getBlock(x, y, z) == BlockEnum.FLUIDCRAFTER.block) {
            val tileEntity: TileEntity? = world.getTileEntity(x, y, z)
            if (tileEntity == null || tileEntity !is TileEntityFluidCrafter) return null
            if (player != null) {
                return ContainerFluidCrafter(player.inventory, tileEntity.getInventory())
            }
        }
        if (world != null && world.getBlock(x, y, z) == BlockEnum.ECBASEBLOCK.block) {
            val tileEntity: TileEntity = world.getTileEntity(x, y, z) ?: return null
            if (tileEntity is TileEntityFluidInterface) return ContainerFluidInterface(player, tileEntity as IFluidInterface) else if (tileEntity is TileEntityFluidFiller) return ContainerFluidFiller(player?.inventory, tileEntity)
            return null
        }
        if (world != null && side != ForgeDirection.UNKNOWN)
            return getPartContainer(side, player, world, x, y, z)
        return getContainer(ID - 6, player, temp) as Any
    }

    private fun getGuiBlockElement(player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? {
        if (world == null || player == null) return null
        val block: Block = world.getBlock(x, y, z) ?: return null
        return when (block) {
            is TGuiBlock -> {
                block.getClientGuiElement(player, world, x, y, z)
            }
            else -> {
                null
            }
        }
    }

    private fun getContainerBlockElement(player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? {
        if (world == null || player == null) return null
        val block: Block = world.getBlock(x, y, z) ?: return null
        return when (block) {
                is TGuiBlock -> {
                    block.getServerGuiElement(player, world, x, y, z)
                }
                else -> {
                    null
                }
            }
    }
}