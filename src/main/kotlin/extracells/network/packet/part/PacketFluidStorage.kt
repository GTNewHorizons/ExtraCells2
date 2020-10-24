package extracells.network.packet.part

import appeng.api.AEApi
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IItemList
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.container.ContainerFluidStorage
import extracells.gui.GuiFluidStorage
import extracells.network.AbstractPacket
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack

class PacketFluidStorage : AbstractPacket {
    private var fluidStackList: IItemList<IAEFluidStack?>? = null
    private var currentFluid: Fluid? = null
    private var hasTermHandler = false

    constructor() {}
    constructor(_player: EntityPlayer?) : super(_player) {
        mode = 2
    }

    constructor(_player: EntityPlayer?, _hasTermHandler: Boolean) : super(_player) {
        mode = 3
        hasTermHandler = _hasTermHandler
    }

    constructor(_player: EntityPlayer?, _currentFluid: Fluid?) : super(_player) {
        mode = 1
        currentFluid = _currentFluid
    }

    constructor(_player: EntityPlayer?, _list: IItemList<IAEFluidStack?>?) : super(_player) {
        mode = 0
        fluidStackList = _list
    }

    override fun execute() {
        when (mode) {
            0 -> case0()
            1 -> if (player != null && player!!.openContainer is ContainerFluidStorage) {
                (player!!.openContainer as ContainerFluidStorage).receiveSelectedFluid(currentFluid)
                //			}else if (this.player != null && Integration.Mods.MEKANISMGAS.isEnabled() && this.player.openContainer instanceof ContainerGasStorage) {
//				((ContainerGasStorage) this.player.openContainer).receiveSelectedFluid(this.currentFluid);
            }
            2 -> if (player != null) {
                if (!player!!.worldObj.isRemote) {
                    if (player!!.openContainer is ContainerFluidStorage) {
                        (player!!.openContainer as ContainerFluidStorage).forceFluidUpdate()
                        (player!!.openContainer as ContainerFluidStorage).doWork()
                        //					}else if (this.player.openContainer instanceof ContainerGasStorage && Integration.Mods.MEKANISMGAS.isEnabled()) {
//						((ContainerGasStorage) this.player.openContainer).forceFluidUpdate();
//						((ContainerGasStorage) this.player.openContainer).doWork();
                    }
                }
            }
            3 -> case3()
        }
    }

    @SideOnly(Side.CLIENT)
    private fun case0() {
        if (player != null && player!!.isClientWorld) {
            val gui: Gui = Minecraft.getMinecraft().currentScreen
            if (gui is GuiFluidStorage) {
                val container = gui.inventorySlots as ContainerFluidStorage
                container.updateFluidList(fluidStackList)
                //			}else if (gui instanceof GuiGasStorage  && Integration.Mods.MEKANISMGAS.isEnabled()) {
//				ContainerGasStorage container = (ContainerGasStorage) ((GuiGasStorage) gui).inventorySlots;
//				container.updateFluidList(this.fluidStackList);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private fun case3() {
        if (player != null && player!!.isClientWorld) {
            val gui: Gui = Minecraft.getMinecraft().currentScreen
            if (gui is GuiFluidStorage) {
                val container = gui.inventorySlots as ContainerFluidStorage
                container.hasWirelessTermHandler = hasTermHandler
                //			}else if (gui instanceof GuiGasStorage && Integration.Mods.MEKANISMGAS.isEnabled()) {
//				ContainerGasStorage container = (ContainerGasStorage) ((GuiGasStorage) gui).inventorySlots;
//				container.hasWirelessTermHandler = this.hasTermHandler;
            }
        }
    }

    override fun readData(`in`: ByteBuf) {
        when (mode) {
            0 -> {
                fluidStackList = AEApi.instance().storage().createFluidList()
                while (`in`.readableBytes() > 0) {
                    val fluid: Fluid = AbstractPacket.Companion.readFluid(`in`)
                    val fluidAmount = `in`.readLong()
                    if (fluid != null) {
                        val stack = AEApi.instance().storage().createFluidStack(FluidStack(fluid, 1))
                        stack.stackSize = fluidAmount
                        fluidStackList.add(stack)
                    }
                }
            }
            1 -> currentFluid = AbstractPacket.Companion.readFluid(`in`)
            2 -> {
            }
            3 -> hasTermHandler = `in`.readBoolean()
        }
    }

    override fun writeData(out: ByteBuf) {
        when (mode) {
            0 -> for (stack in fluidStackList!!) {
                val fluidStack = stack!!.fluidStack
                AbstractPacket.Companion.writeFluid(fluidStack.getFluid(), out)
                out.writeLong(fluidStack.amount.toLong())
            }
            1 -> AbstractPacket.Companion.writeFluid(currentFluid, out)
            2 -> {
            }
            3 -> out.writeBoolean(hasTermHandler)
        }
    }
}