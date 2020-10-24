package extracells.network.packet.part

import appeng.api.AEApi
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IItemList
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.container.ContainerFluidTerminal
import extracells.gui.GuiFluidTerminal
import extracells.network.AbstractPacket
import extracells.part.PartFluidTerminal
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack

class PacketFluidTerminal : AbstractPacket {
    var fluidStackList: IItemList<IAEFluidStack?>? = null
    var currentFluid: Fluid? = null
    var terminalFluid: PartFluidTerminal? = null

    constructor() {}
    constructor(_player: EntityPlayer?, _currentFluid: Fluid?) : super(_player) {
        mode = 2
        currentFluid = _currentFluid
    }

    constructor(_player: EntityPlayer?, _currentFluid: Fluid?,
                _terminalFluid: PartFluidTerminal?) : super(_player) {
        mode = 1
        currentFluid = _currentFluid
        terminalFluid = _terminalFluid
    }

    constructor(_player: EntityPlayer?,
                _list: IItemList<IAEFluidStack?>?) : super(_player) {
        mode = 0
        fluidStackList = _list
    }

    constructor(_player: EntityPlayer?,
                _terminalFluid: PartFluidTerminal?) : super(_player) {
        mode = 3
        terminalFluid = _terminalFluid
    }

    override fun execute() {
        when (mode) {
            0 -> case0()
            1 -> terminalFluid!!.setCurrentFluid(currentFluid)
            2 -> case2()
            3 -> if (player != null && player!!.openContainer is ContainerFluidTerminal) {
                val fluidContainer = player!!.openContainer as ContainerFluidTerminal
                fluidContainer.forceFluidUpdate()
                terminalFluid!!.sendCurrentFluid(fluidContainer)
                //			} else if (this.player != null && this.player.openContainer instanceof ContainerGasTerminal) {
//				ContainerGasTerminal fluidContainer = (ContainerGasTerminal) this.player.openContainer;
//				fluidContainer.forceFluidUpdate();
//				this.terminalFluid.sendCurrentFluid(fluidContainer);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    fun case0() {
        if (player != null && player!!.isClientWorld) {
            val gui: Gui = Minecraft.getMinecraft().currentScreen
            if (gui is GuiFluidTerminal) {
                val container = gui.inventorySlots as ContainerFluidTerminal
                container.updateFluidList(fluidStackList)
                //			} else if (gui instanceof GuiGasTerminal) {
//				ContainerGasTerminal container = (ContainerGasTerminal) ((GuiGasTerminal) gui).inventorySlots;
//				container.updateFluidList(this.fluidStackList);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    fun case2() {
        if (player != null && Minecraft.getMinecraft().currentScreen is GuiFluidTerminal) {
            val gui = Minecraft.getMinecraft().currentScreen as GuiFluidTerminal
            (gui.container as ContainerFluidTerminal).receiveSelectedFluid(currentFluid)
            //		} else if (this.player != null && Minecraft.getMinecraft().currentScreen instanceof GuiGasTerminal) {
//			GuiGasTerminal gui = (GuiGasTerminal) Minecraft.getMinecraft().currentScreen;
//			((ContainerGasTerminal) gui.getContainer()).receiveSelectedFluid(this.currentFluid);
        }
    }

    override fun readData(`in`: ByteBuf) {
        when (mode) {
            0 -> {
                fluidStackList = AEApi.instance().storage().createFluidList()
                while (`in`.readableBytes() > 0) {
                    val fluid: Fluid = AbstractPacket.Companion.readFluid(`in`)
                    val fluidAmount = `in`.readLong()
                    if (fluid == null || fluidAmount <= 0) {
                        continue
                    }
                    val stack = AEApi.instance().storage()
                            .createFluidStack(FluidStack(fluid, 1))
                    stack.stackSize = fluidAmount
                    fluidStackList.add(stack)
                }
            }
            1 -> {
                terminalFluid = AbstractPacket.Companion.readPart(`in`) as PartFluidTerminal
                currentFluid = AbstractPacket.Companion.readFluid(`in`)
            }
            2 -> currentFluid = AbstractPacket.Companion.readFluid(`in`)
            3 -> terminalFluid = AbstractPacket.Companion.readPart(`in`) as PartFluidTerminal
        }
    }

    override fun writeData(out: ByteBuf) {
        when (mode) {
            0 -> for (stack in fluidStackList!!) {
                val fluidStack = stack!!.fluidStack
                AbstractPacket.Companion.writeFluid(fluidStack.getFluid(), out)
                out.writeLong(fluidStack.amount.toLong())
            }
            1 -> {
                AbstractPacket.Companion.writePart(terminalFluid, out)
                AbstractPacket.Companion.writeFluid(currentFluid, out)
            }
            2 -> AbstractPacket.Companion.writeFluid(currentFluid, out)
            3 -> AbstractPacket.Companion.writePart(terminalFluid, out)
        }
    }
}