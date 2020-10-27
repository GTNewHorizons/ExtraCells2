package extracells.tileentity

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.networking.IGridNode
import appeng.api.networking.energy.IEnergyGrid
import appeng.api.networking.security.IActionHost
import appeng.api.util.AECableType
import appeng.api.util.DimensionalCoord
import extracells.api.IECTileEntity
import extracells.gridblock.ECGridBlockVibrantChamber
import extracells.util.FuelBurnTime
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.*
open class TileEntityVibrationChamberFluid : TPowerStorage(), IECTileEntity , IFluidHandler , IActionHost {
  private var isFirstGridNode: Boolean = true
  private val gridBlock = ECGridBlockVibrantChamber(this)
  private var node: IGridNode? = null
  private var burnTime: Int = 0
  private var burnTimeTotal: Int = 0
  private var timer: Int = 0
  private var timerEnergy: Int = 0
  private var energyLeft: Double = .0
  var fluidTank: FluidTank = object : FluidTank((16000)) {
    override fun readFromNBT(nbt: NBTTagCompound): FluidTank{
      if (!nbt.hasKey("Empty")) {
        val fluid: FluidStack = FluidStack.loadFluidStackFromNBT(nbt)
        setFluid(fluid)
      }
      else {
        setFluid(null)
      }
      return this
    }
  }

  override fun updateEntity() {
    super.updateEntity()
    if (!hasWorldObj()) return
    var fluidStack1: FluidStack? = fluidTank.fluid
    if (fluidStack1 != null) fluidStack1 = fluidStack1.copy()
    if (worldObj.isRemote) return
    if (burnTime == burnTimeTotal) {
      if (timer >= 40) {
        worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord)
        val fluidStack: FluidStack? = fluidTank.fluid
        val bTime: Int = if (fluidStack != null) FuelBurnTime.getBurnTime(fluidStack.getFluid())
        else 0
        if (fluidStack != null && bTime > 0) {
          if (fluidTank.fluid.amount >= 250) {
            if (energyLeft <= 0) {
              burnTime = 0
              burnTimeTotal = bTime / 4
              fluidTank.drain(250, true)
            }
          }
        }
        timer = 0
      }
      else {
        timer += 1
      }
    }
    else {
      burnTime += 1
      if (timerEnergy == 4) {
        getGridNode(ForgeDirection.UNKNOWN)?.let {
          energyLeft = if (energyLeft.toInt() == 0) {
            val energy: IEnergyGrid = it.grid.getCache(IEnergyGrid::class.java)
            energy.injectPower(24.0, Actionable.MODULATE)
          }
          else {
            val energy: IEnergyGrid = it.grid.getCache(IEnergyGrid::class.java)
            energy.injectPower(energyLeft, Actionable.MODULATE)
          }
        }
        timerEnergy = 0
      }
      else {
        timerEnergy += 1
      }
    }
    if (fluidStack1 == null && fluidTank.fluid == null) return
    if (fluidStack1 == null || fluidTank.fluid == null) {
      worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
      return
    }
    if (!(fluidStack1 == fluidTank.fluid)) {
      worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
      return
    }
    if (fluidStack1.amount != fluidTank.fluid.amount) {
      worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
      return
    }
  }

  override val location
    get() = DimensionalCoord(this)

  override val powerUsage = 0.0

  override fun getGridNode(forgeDirection: ForgeDirection): IGridNode? {
    if (isFirstGridNode && hasWorldObj() && !getWorldObj().isRemote) {
      isFirstGridNode = false
      try {
        node = AEApi.instance().createGridNode(gridBlock)
        node!!.updateState()
      }
      catch (e:Exception) {
          isFirstGridNode = true
      }
    }
    return node!!
  }

  fun getGridNodeWithoutUpdate(): IGridNode? {
    if (isFirstGridNode && hasWorldObj() && !getWorldObj().isRemote) {
      isFirstGridNode = false
      try {
        node = AEApi.instance().createGridNode(gridBlock)
      }
      catch (e:Exception) {
        isFirstGridNode = true
      }
    }
    return node
  }

  override fun getCableConnectionType(forgeDirection: ForgeDirection) = AECableType.SMART

  override fun securityBreak() {}

  override fun fill(from: ForgeDirection, resource: FluidStack?, doFill: Boolean): Int {
    if (resource?.getFluid() == null || FuelBurnTime.getBurnTime(resource.getFluid()) == 0) return 0
    val filled: Int = fluidTank.fill(resource, doFill)
    if (filled != 0 && hasWorldObj()) getWorldObj().markBlockForUpdate(xCoord, yCoord, zCoord)
    return filled
  }

  override fun drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack? = null

  override fun drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack? = null

  override fun canFill(from: ForgeDirection, fluid: Fluid?): Boolean =
          fluid != null && FuelBurnTime.getBurnTime(fluid) != 0

  override fun canDrain(from: ForgeDirection, fluid: Fluid): Boolean = false

  override fun getTankInfo(from: ForgeDirection): Array<FluidTankInfo> = arrayOf(fluidTank.info)

  fun getTank(): FluidTank = fluidTank

  override fun writeToNBT(nbt: NBTTagCompound) {
    super.writeToNBT(nbt)
    writePowerToNBT(nbt)
    nbt.setInteger("BurnTime", this.burnTime)
    nbt.setInteger("BurnTimeTotal", this.burnTimeTotal)
    nbt.setInteger("timer", this.timer)
    nbt.setInteger("timerEnergy", this.timerEnergy)
    nbt.setDouble("energyLeft", this.energyLeft)
    fluidTank.writeToNBT(nbt)
  }

  override fun readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    readPowerFromNBT(nbt)
    if (nbt.hasKey("BurnTime")) this.burnTime = nbt.getInteger("BurnTime")
    if (nbt.hasKey("BurnTimeTotal")) this.burnTimeTotal = nbt.getInteger("BurnTimeTotal")
    if (nbt.hasKey("timer")) this.timer = nbt.getInteger("timer")
    if (nbt.hasKey("timerEnergy")) this.timerEnergy = nbt.getInteger("timerEnergy")
    if (nbt.hasKey("energyLeft")) this.energyLeft = nbt.getDouble("energyLeft")
    fluidTank.readFromNBT(nbt)
  }

  fun getBurntTimeScaled(scal: Int): Int {
    return if (burnTime != 0) burnTime * scal / burnTimeTotal else 0
  }

  override fun getActionableNode(): IGridNode? {
    return getGridNode(ForgeDirection.UNKNOWN)
  }

  override fun getDescriptionPacket(): Packet {
    val nbtTag: NBTTagCompound = NBTTagCompound()
    writeToNBT(nbtTag)
    return S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, this.getBlockMetadata(), nbtTag)
  }

  override fun onDataPacket(net: NetworkManager, pkt: S35PacketUpdateTileEntity) {
    readFromNBT(pkt.func_148857_g())
  }

  fun getBurnTime(): Int {
    return burnTime
  }

  fun getBurnTimeTotal(): Int {
    return burnTimeTotal
  }
}

