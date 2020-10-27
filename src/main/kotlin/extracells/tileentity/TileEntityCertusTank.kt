package extracells.tileentity

import extracells.network.ChannelHandler
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.*
open class TileEntityCertusTank : TileEntity(), IFluidHandler {
    private var lastBeforeUpdate: FluidStack? = null
    var tank: FluidTank = object : FluidTank(32000) {
        override fun readFromNBT(nbt: NBTTagCompound): FluidTank {
            if (!nbt.hasKey("Empty")) {
                val fluid = FluidStack.loadFluidStackFromNBT(nbt)
                setFluid(fluid)
            } else {
                setFluid(null)
            }
            return this
        }
    }

    override fun canDrain(from: ForgeDirection, fluid: Fluid): Boolean {
        return (tank.fluid == null
                || tank.fluid.getFluid() === fluid)
    }

    override fun canFill(from: ForgeDirection, fluid: Fluid): Boolean {
        return (tank.fluid == null
                || tank.fluid.getFluid() === fluid)
    }

    fun compareAndUpdate() {
        if (!worldObj.isRemote) {
            val current = tank.fluid
            if (current != null) {
                if (lastBeforeUpdate != null) {
                    if (Math.abs(current.amount - lastBeforeUpdate!!.amount) >= 500) {
                        ChannelHandler.sendPacketToAllPlayers(
                                descriptionPacket, worldObj)
                        lastBeforeUpdate = current.copy()
                    } else if (lastBeforeUpdate!!.amount < tank
                                    .capacity
                            && current.amount == tank.capacity
                            || lastBeforeUpdate!!.amount == tank
                                    .capacity
                            && current.amount < tank.capacity) {
                        ChannelHandler.sendPacketToAllPlayers(
                                descriptionPacket, worldObj)
                        lastBeforeUpdate = current.copy()
                    }
                } else {
                    ChannelHandler.sendPacketToAllPlayers(
                            descriptionPacket, worldObj)
                    lastBeforeUpdate = current.copy()
                }
            } else if (lastBeforeUpdate != null) {
                ChannelHandler.sendPacketToAllPlayers(descriptionPacket,
                        worldObj)
                lastBeforeUpdate = null
            }
        }
    }

    /* Multiblock stuff */
    fun drain(fluid: FluidStack, doDrain: Boolean,
              findMainTank: Boolean): FluidStack? {
        if (findMainTank) {
            var yOff = 0
            var offTE = worldObj.getTileEntity(xCoord,
                    yCoord + yOff, zCoord)
            var mainTank = this
            while (true) {
                if (offTE != null && offTE is TileEntityCertusTank) {
                    val offFluid = offTE.fluid
                    if (offFluid != null && offFluid === fluid.getFluid()) {
                        mainTank = worldObj
                                .getTileEntity(xCoord, yCoord + yOff,
                                        zCoord) as TileEntityCertusTank
                        yOff++
                        offTE = worldObj.getTileEntity(xCoord,
                                yCoord + yOff, zCoord)
                        continue
                    }
                }
                break
            }
            return mainTank.drain(fluid, doDrain, false)
        }
        val drained = tank.drain(fluid.amount, doDrain)
        compareAndUpdate()
        if (drained == null || drained.amount < fluid.amount) {
            val offTE = worldObj.getTileEntity(xCoord,
                    yCoord - 1, zCoord)
            if (offTE is TileEntityCertusTank) {
                val externallyDrained = offTE.drain(FluidStack(
                        fluid.getFluid(), fluid.amount
                        - (drained?.amount ?: 0)),
                        doDrain, false)
                return if (externallyDrained != null) FluidStack(fluid.getFluid(), (drained?.amount ?: 0)
                        + externallyDrained.amount) else drained
            }
        }
        return drained
    }

    override fun drain(from: ForgeDirection, resource: FluidStack?,
                       doDrain: Boolean): FluidStack? {
        return if (tank.fluid == null || resource == null || resource.getFluid() !== tank.fluid.getFluid()) null else drain(
                resource, doDrain, true)!!
    }

    override fun drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack? {
        return if (tank.fluid == null) null else drain(from, FluidStack(tank.fluid, maxDrain),
                doDrain)
    }

    fun fill(fluid: FluidStack, doFill: Boolean, findMainTank: Boolean): Int {
        if (findMainTank) {
            var yOff = 0
            var offTE = worldObj.getTileEntity(xCoord,
                    yCoord - yOff, zCoord)
            var mainTank = this
            while (true) {
                if (offTE != null && offTE is TileEntityCertusTank) {
                    val offFluid = offTE.fluid
                    if (offFluid == null || offFluid === fluid.getFluid()) {
                        mainTank = worldObj
                                .getTileEntity(xCoord, yCoord - yOff,
                                        zCoord) as TileEntityCertusTank
                        yOff++
                        offTE = worldObj.getTileEntity(xCoord,
                                yCoord - yOff, zCoord)
                        continue
                    }
                }
                break
            }
            return mainTank.fill(fluid, doFill, false)
        }
        val filled = tank.fill(fluid, doFill)
        compareAndUpdate()
        if (filled < fluid.amount) {
            val offTE = worldObj.getTileEntity(xCoord,
                    yCoord + 1, zCoord)
            if (offTE is TileEntityCertusTank) {
                return (filled
                        + offTE.fill(FluidStack(fluid.getFluid(), fluid.amount
                        - filled), doFill, false))
            }
        }
        return filled
    }

    /* IFluidHandler */
    override fun fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int {
        return if (resource == null || tank.fluid != null
                && resource.getFluid() !== tank.fluid.getFluid()) 0 else fill(resource, doFill, true)
    }

    override fun getDescriptionPacket(): Packet {
        val nbtTag = NBTTagCompound()
        writeToNBT(nbtTag)
        return S35PacketUpdateTileEntity(xCoord, yCoord,
                zCoord, 1, nbtTag)
    }

    val fluid: Fluid?
        get() {
            val tankFluid = tank.fluid
            return if (tankFluid != null && tankFluid.amount > 0) tankFluid.getFluid() else null
        }
    val renderFluid: Fluid?
        get() = if (tank.fluid != null) tank.fluid.getFluid() else null
    val renderScale: Float
        get() = tank.fluidAmount.toFloat() / tank.capacity

    fun getTankInfo(goToMainTank: Boolean): Array<FluidTankInfo> {
        if (!goToMainTank) return arrayOf(tank.info)
        var amount = 0
        var capacity = 0
        var fluid: Fluid? = null
        var yOff = 0
        var offTE = worldObj.getTileEntity(xCoord, yCoord
                - yOff, zCoord)
        var mainTank = this
        while (true) {
            if (offTE != null && offTE is TileEntityCertusTank) {
                if (offTE.fluid == null
                        || offTE.fluid === fluid) {
                    mainTank = worldObj
                            .getTileEntity(xCoord, yCoord - yOff,
                                    zCoord) as TileEntityCertusTank
                    yOff++
                    offTE = worldObj.getTileEntity(xCoord,
                            yCoord - yOff, zCoord)
                    continue
                }
            }
            break
        }
        yOff = 0
        offTE = worldObj.getTileEntity(xCoord, yCoord + yOff,
                zCoord)
        while (true) {
            if (offTE != null && offTE is TileEntityCertusTank) {
                mainTank = offTE
                if (mainTank.fluid == null
                        || mainTank.fluid === fluid) {
                    val info = mainTank.getTankInfo(false)[0]
                    if (info != null) {
                        capacity += info.capacity
                        if (info.fluid != null) {
                            amount += info.fluid.amount
                            if (info.fluid.getFluid() != null) fluid = info.fluid.getFluid()
                        }
                    }
                    yOff++
                    offTE = worldObj.getTileEntity(xCoord,
                            yCoord + yOff, zCoord)
                    continue
                }
            }
            break
        }
        return arrayOf(FluidTankInfo(
                fluid?.let { FluidStack(it, amount) }, capacity))
    }

    override fun getTankInfo(from: ForgeDirection): Array<FluidTankInfo> {
        return getTankInfo(true)
    }

    override fun onDataPacket(net: NetworkManager,
                              packet: S35PacketUpdateTileEntity) {
        worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord,
                zCoord, xCoord, yCoord, zCoord)
        readFromNBT(packet.func_148857_g())
    }

    override fun readFromNBT(tag: NBTTagCompound) {
        super.readFromNBT(tag)
        readFromNBTWithoutCoords(tag)
    }

    fun readFromNBTWithoutCoords(tag: NBTTagCompound?) {
        tank.readFromNBT(tag)
    }

    override fun writeToNBT(tag: NBTTagCompound) {
        super.writeToNBT(tag)
        writeToNBTWithoutCoords(tag)
    }

    fun writeToNBTWithoutCoords(tag: NBTTagCompound?) {
        tank.writeToNBT(tag)
    }
}