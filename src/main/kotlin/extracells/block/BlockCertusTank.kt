package extracells.block

import appeng.api.implementations.items.IAEWrench
import buildcraft.api.tools.IToolWrench
import extracells.network.ChannelHandler
import extracells.registries.BlockEnum
import extracells.render.RenderHandler
import extracells.tileentity.TileEntityCertusTank
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.StatCollector
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.FluidContainerRegistry
open class BlockCertusTank : BlockEC(Material.glass, 2.0f, 10.0f) {
    var breakIcon: IIcon? = null
    var topIcon: IIcon? = null
    var bottomIcon: IIcon? = null
    var sideIcon: IIcon? = null
    var sideMiddleIcon: IIcon? = null
    var sideTopIcon: IIcon? = null
    var sideBottomIcon: IIcon? = null
    override fun canRenderInPass(pass: Int): Boolean {
        RenderHandler.renderPass = pass
        return true
    }

    override fun createNewTileEntity(var1: World, var2: Int): TileEntity {
        return TileEntityCertusTank()
    }

    fun getDropWithNBT(world: World, x: Int, y: Int, z: Int): ItemStack? {
        val tileEntity = NBTTagCompound()
        val worldTE = world.getTileEntity(x, y, z)
        if (worldTE is TileEntityCertusTank) {
            val dropStack = ItemStack(
                    BlockEnum.CERTUSTANK.block, 1)
            worldTE
                    .writeToNBTWithoutCoords(tileEntity)
            if (!tileEntity.hasKey("Empty")) {
                dropStack.tagCompound = NBTTagCompound()
                dropStack.stackTagCompound.setTag("tileEntity", tileEntity)
            }
            return dropStack
        }
        return null
    }

    override fun getIcon(side: Int, b: Int): IIcon? {
        return when (b) {
            1 -> sideTopIcon!!
            2 -> sideBottomIcon!!
            3 -> sideMiddleIcon!!
            else -> if (side == 0) bottomIcon!! else (if (side == 1) topIcon else sideIcon)!!
        }
    }

    override fun getLocalizedName(): String {
        return StatCollector.translateToLocal("$unlocalizedName.name")
    }

    override fun getPickBlock(target: MovingObjectPosition, world: World, x: Int, y: Int, z: Int): ItemStack {
        return getDropWithNBT(world, x, y, z)!!
    }

    override fun getRenderBlockPass(): Int {
        return 1
    }

    override fun getRenderType(): Int {
        return RenderHandler.id
    }

    override fun getUnlocalizedName(): String {
        return super.getUnlocalizedName().replace("tile.", "")
    }

    override fun isOpaqueCube(): Boolean {
        return false
    }

    override fun onBlockActivated(worldObj: World, x: Int, y: Int, z: Int,
                                  entityplayer: EntityPlayer, blockID: Int, offsetX: Float,
                                  offsetY: Float, offsetZ: Float): Boolean {
        val current = entityplayer.inventory.getCurrentItem()
        if (entityplayer.isSneaking && current != null) {
            try {
                if (current.item is IToolWrench
                        && (current.item as IToolWrench).canWrench(
                                entityplayer, x, y, z)) {
                    dropBlockAsItem(worldObj, x, y, z,
                            getDropWithNBT(worldObj, x, y, z))
                    worldObj.setBlockToAir(x, y, z)
                    (current.item as IToolWrench).wrenchUsed(entityplayer,
                            x, y, z)
                    return true
                }
            } catch (e: Throwable) {
                // No IToolWrench
            }
            if (current.item is IAEWrench
                    && (current.item as IAEWrench).canWrench(current,
                            entityplayer, x, y, z)) {
                dropBlockAsItem(worldObj, x, y, z,
                        getDropWithNBT(worldObj, x, y, z))
                worldObj.setBlockToAir(x, y, z)
                return true
            }
        }
        if (current != null) {
            var liquid = FluidContainerRegistry.getFluidForFilledItem(current)
            val tank = worldObj.getTileEntity(x, y, z) as TileEntityCertusTank
            if (liquid != null) {
                val amountFilled = tank.fill(ForgeDirection.UNKNOWN, liquid, true)
                if (amountFilled != 0
                        && !entityplayer.capabilities.isCreativeMode) {
                    if (current.stackSize > 1) {
                        entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem].stackSize -= 1
                        entityplayer.inventory.addItemStackToInventory(current.item.getContainerItem(current))
                    } else {
                        entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = current.item.getContainerItem(
                                current)
                    }
                }
                return true

                // Handle empty containers
            } else {
                val available = tank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid
                if (available != null) {
                    val filled = FluidContainerRegistry.fillFluidContainer(available, current)
                    liquid = FluidContainerRegistry.getFluidForFilledItem(filled)
                    if (liquid != null) {
                        tank.drain(ForgeDirection.UNKNOWN, liquid.amount, true)
                        if (!entityplayer.capabilities.isCreativeMode) {
                            if (current.stackSize == 1) {
                                entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = filled
                            } else {
                                entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem].stackSize--
                                if (!entityplayer.inventory.addItemStackToInventory(
                                                filled)) entityplayer.entityDropItem(filled, 0f)
                            }
                        }
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun onNeighborBlockChange(world: World, x: Int, y: Int, z: Int,
                                       neighborBlock: Block) {
        if (!world.isRemote) {
            ChannelHandler.sendPacketToAllPlayers(world.getTileEntity(x, y, z).descriptionPacket, world)
        }
    }

    override fun registerBlockIcons(iconregister: IIconRegister) {
        breakIcon = iconregister.registerIcon("extracells:certustank")
        topIcon = iconregister.registerIcon("extracells:CTankTop")
        bottomIcon = iconregister.registerIcon("extracells:CTankBottom")
        sideIcon = iconregister.registerIcon("extracells:CTankSide")
        sideMiddleIcon = iconregister.registerIcon("extracells:CTankSideMiddle")
        sideTopIcon = iconregister.registerIcon("extracells:CTankSideTop")
        sideBottomIcon = iconregister.registerIcon("extracells:CTankSideBottom")
    }

    override fun renderAsNormalBlock(): Boolean {
        return false
    }

    init {
        setBlockBounds(0.0625f, 0.0f, 0.0625f, 0.9375f, 1.0f, 0.9375f)
    }
}