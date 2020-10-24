package extracells.item

import appeng.api.AEApi
import appeng.api.config.AccessRestriction
import appeng.api.config.Actionable
import appeng.api.config.FuzzyMode
import appeng.api.config.PowerUnits
import appeng.api.implementations.items.IAEItemPowerStorage
import appeng.api.implementations.items.IStorageCell
import appeng.api.networking.security.PlayerSource
import appeng.api.storage.ICellInventoryHandler
import appeng.api.storage.IMEInventoryHandler
import appeng.api.storage.StorageChannel
import appeng.api.storage.data.IAEItemStack
import cofh.api.energy.IEnergyContainerItem
import cpw.mods.fml.common.Optional
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.Extracells.dynamicTypes
import extracells.registries.ItemEnum
import extracells.util.inventory.ECCellInventory
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.inventory.IInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ChatComponentTranslation
import net.minecraft.util.IIcon
import net.minecraft.util.MathHelper
import net.minecraft.util.StatCollector
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

@Optional.Interface(iface = "cofh.api.energy.IEnergyContainerItem", modid = "CoFHAPI|energy")
class ItemStoragePhysical : ItemECBase(), IStorageCell, IAEItemPowerStorage, IEnergyContainerItem {
    private var icons: Array<IIcon>
    private val MAX_POWER = 32000
    override fun addInformation(itemStack: ItemStack, player: EntityPlayer,
                                list: MutableList<*>, par4: Boolean) {
        val cellRegistry = AEApi.instance().registries().cell()
        val invHandler: IMEInventoryHandler<IAEItemStack> = cellRegistry
                .getCellInventory(itemStack, null, StorageChannel.ITEMS)
        val inventoryHandler = invHandler as ICellInventoryHandler
        val cellInv = inventoryHandler.cellInv
        val usedBytes = cellInv.usedBytes
        list.add(String.format(StatCollector
                .translateToLocal("extracells.tooltip.storage.physical.bytes"),
                usedBytes, cellInv.totalBytes))
        list.add(String.format(StatCollector
                .translateToLocal("extracells.tooltip.storage.physical.types"),
                cellInv.storedItemTypes, cellInv.totalItemTypes))
        if (usedBytes > 0) list.add(String.format(
                StatCollector
                        .translateToLocal("extracells.tooltip.storage.physical.content"),
                cellInv.storedItemCount))
    }

    override fun getBytesPerType(cellItem: ItemStack): Int {
        return if (dynamicTypes) bytes_cell[MathHelper.clamp_int(
                cellItem.itemDamage, 0, suffixes.size - 1)] / 128 else 8
    }

    @Deprecated("")
    override fun BytePerType(cellItem: ItemStack): Int {
        return getBytesPerType(cellItem)
    }

    private fun ensureTagCompound(itemStack: ItemStack): NBTTagCompound {
        if (!itemStack.hasTagCompound()) itemStack.tagCompound = NBTTagCompound()
        return itemStack.tagCompound
    }

    override fun extractAEPower(itemStack: ItemStack, amt: Double): Double {
        if (itemStack == null || itemStack.itemDamage != 4) return 0.0
        val tagCompound = ensureTagCompound(itemStack)
        val currentPower = tagCompound.getDouble("power")
        val toExtract = Math.min(amt, currentPower)
        tagCompound.setDouble("power", currentPower - toExtract)
        return toExtract
    }

    @Optional.Method(modid = "CoFHAPI|energy")
    override fun extractEnergy(container: ItemStack, maxExtract: Int,
                               simulate: Boolean): Int {
        if (container == null || container.itemDamage != 4) return 0
        return if (simulate) {
            if (getEnergyStored(container) >= maxExtract) maxExtract else getEnergyStored(container)
        } else {
            PowerUnits.AE
                    .convertTo(
                            PowerUnits.RF,
                            extractAEPower(container, PowerUnits.RF.convertTo(
                                    PowerUnits.AE, maxExtract.toDouble()))).toInt()
        }
    }

    override fun getAECurrentPower(itemStack: ItemStack): Double {
        if (itemStack == null || itemStack.itemDamage != 4) return 0.0
        val tagCompound = ensureTagCompound(itemStack)
        return tagCompound.getDouble("power")
    }

    override fun getAEMaxPower(itemStack: ItemStack): Double {
        return if (itemStack == null || itemStack.itemDamage != 4) 0.0 else MAX_POWER.toDouble()
    }

    override fun getBytes(cellItem: ItemStack): Int {
        return bytes_cell[MathHelper.clamp_int(cellItem.itemDamage, 0,
                suffixes.size - 1)]
    }

    override fun getConfigInventory(`is`: ItemStack): IInventory {
        return ECCellInventory(`is`, "config", 63, 1)
    }

    override fun getDurabilityForDisplay(itemStack: ItemStack): Double {
        return if (itemStack == null || itemStack.itemDamage != 4) super.getDurabilityForDisplay(
                itemStack) else 1 - getAECurrentPower(itemStack) / MAX_POWER
    }

    @Optional.Method(modid = "CoFHAPI|energy")
    override fun getEnergyStored(arg0: ItemStack): Int {
        return PowerUnits.AE.convertTo(PowerUnits.RF,
                getAECurrentPower(arg0)).toInt()
    }

    override fun getFuzzyMode(`is`: ItemStack): FuzzyMode {
        if (!`is`.hasTagCompound()) `is`.tagCompound = NBTTagCompound()
        return FuzzyMode.values()[`is`.tagCompound.getInteger("fuzzyMode")]
    }

    override fun getIconFromDamage(dmg: Int): IIcon {
        return icons[MathHelper.clamp_int(dmg, 0, suffixes.size - 1)]
    }

    override fun getIdleDrain(): Double {
        return 0
    }

    @SideOnly(Side.CLIENT)
    override fun getItemStackDisplayName(stack: ItemStack): String {
        if (stack == null) return super.getItemStackDisplayName(stack)
        if (stack.itemDamage == 4) {
            try {
                val list = AEApi
                        .instance()
                        .registries()
                        .cell()
                        .getCellInventory(stack, null, StorageChannel.ITEMS)
                        .getAvailableItems(
                                AEApi.instance().storage().createItemList())
                if (list.isEmpty) return (super.getItemStackDisplayName(stack)
                        + " - "
                        + StatCollector
                        .translateToLocal("extracells.tooltip.empty1"))
                val s = list.firstItem as IAEItemStack
                return (super.getItemStackDisplayName(stack) + " - "
                        + s.itemStack.displayName)
            } catch (e: Throwable) {
            }
            return (super.getItemStackDisplayName(stack)
                    + " - "
                    + StatCollector
                    .translateToLocal("extracells.tooltip.empty1"))
        }
        return super.getItemStackDisplayName(stack)
    }

    @Optional.Method(modid = "CoFHAPI|energy")
    override fun getMaxEnergyStored(arg0: ItemStack): Int {
        return PowerUnits.AE
                .convertTo(PowerUnits.RF, getAEMaxPower(arg0)).toInt()
    }

    override fun getPowerFlow(itemStack: ItemStack): AccessRestriction {
        if (itemStack == null) return null
        return if (itemStack.itemDamage == 4) AccessRestriction.READ_WRITE else AccessRestriction.NO_ACCESS
    }

    override fun getSubItems(item: Item, creativeTab: CreativeTabs, itemList: MutableList<*>) {
        for (i in suffixes.indices) {
            itemList.add(ItemStack(item, 1, i))
            if (i == 4) {
                val s = ItemStack(item, 1, i)
                s.tagCompound = NBTTagCompound()
                s.tagCompound.setDouble("power", MAX_POWER.toDouble())
                itemList.add(s)
            }
        }
    }

    override fun getTotalTypes(cellItem: ItemStack): Int {
        return types_cell[MathHelper.clamp_int(cellItem.itemDamage, 0,
                suffixes.size - 1)]
    }

    override fun getUnlocalizedName(itemStack: ItemStack): String {
        return ("extracells.item.storage.physical."
                + suffixes[itemStack.itemDamage])
    }

    override fun getUpgradesInventory(`is`: ItemStack): IInventory {
        return ECCellInventory(`is`, "upgrades", 2, 1)
    }

    override fun injectAEPower(itemStack: ItemStack, amt: Double): Double {
        if (itemStack == null || itemStack.itemDamage != 4) return 0.0
        val tagCompound = ensureTagCompound(itemStack)
        val currentPower = tagCompound.getDouble("power")
        val toInject = Math.min(amt, MAX_POWER - currentPower)
        tagCompound.setDouble("power", currentPower + toInject)
        return toInject
    }

    override fun isBlackListed(cellItem: ItemStack,
                               requestedAddition: IAEItemStack): Boolean {
        return false
    }

    override fun isEditable(`is`: ItemStack): Boolean {
        return true
    }

    override fun isStorageCell(i: ItemStack): Boolean {
        return true
    }

    override fun onItemRightClick(itemStack: ItemStack, world: World,
                                  entityPlayer: EntityPlayer): ItemStack {
        if (itemStack == null) return itemStack
        if (itemStack.itemDamage == 4 && !world.isRemote && entityPlayer.isSneaking) {
            when (itemStack.tagCompound.getInteger("mode")) {
                0 -> {
                    itemStack.tagCompound.setInteger("mode", 1)
                    entityPlayer.addChatMessage(ChatComponentTranslation("extracells.tooltip.storage.container.1"))
                }
                1 -> {
                    itemStack.tagCompound.setInteger("mode", 2)
                    entityPlayer.addChatMessage(ChatComponentTranslation("extracells.tooltip.storage.container.2"))
                }
                2 -> {
                    itemStack.tagCompound.setInteger("mode", 0)
                    entityPlayer.addChatMessage(ChatComponentTranslation("extracells.tooltip.storage.container.0"))
                }
            }
            return itemStack
        }
        if (!entityPlayer.isSneaking) return itemStack
        val invHandler: IMEInventoryHandler<IAEItemStack> = AEApi.instance().registries().cell().getCellInventory(
                itemStack, null, StorageChannel.ITEMS)
        val inventoryHandler = invHandler as ICellInventoryHandler
        val cellInv = inventoryHandler.cellInv
        return if (cellInv.usedBytes == 0L && entityPlayer.inventory.addItemStackToInventory(
                        ItemEnum.STORAGECASING.getDamagedStack(0))) ItemEnum.STORAGECOMPONENT.getDamagedStack(
                itemStack.itemDamage) else itemStack
    }

    override fun onItemUse(itemstack: ItemStack, player: EntityPlayer,
                           world: World, x: Int, y: Int, z: Int, side: Int, xOffset: Float,
                           yOffset: Float, zOffset: Float): Boolean {
        if (itemstack == null || player == null) return false
        return if (itemstack.itemDamage == 4 && !player.isSneaking) {
            val power = getAECurrentPower(itemstack)
            val face = ForgeDirection.getOrientation(side)
            val list = AEApi.instance().registries().cell().getCellInventory(itemstack, null,
                    StorageChannel.ITEMS).getAvailableItems(
                    AEApi.instance().storage().createItemList())
            if (list.isEmpty) return false
            val storageStack = list.firstItem as IAEItemStack
            if (world.getBlock(x + face.offsetX, y + face.offsetY,
                            z + face.offsetZ) === Blocks.air && storageStack.stackSize != 0L && power >= 20.0) {
                if (!world.isRemote) {
                    val request = storageStack.copy()
                    request.stackSize = 1
                    val block = request.itemStack
                    if (block.item is ItemBlock) {
                        val itemblock = request.item as ItemBlock
                        if (world.getBlock(x, y, z) !== Blocks.bedrock && world.getBlock(x, y,
                                        z).getBlockHardness(world, x, y, z) >= 0.0f) {
                            when (itemstack.tagCompound.getInteger("mode")) {
                                0 -> {
                                    request.stackSize = 1
                                    itemblock.onItemUseFirst(request.itemStack, player, world, x, y, z, side, xOffset,
                                            yOffset,
                                            zOffset)
                                    itemblock.onItemUse(request.itemStack, player, world, x, y, z, side, xOffset,
                                            yOffset,
                                            zOffset)
                                    AEApi.instance().registries().cell().getCellInventory(itemstack, null,
                                            StorageChannel.ITEMS).extractItems(request, Actionable.MODULATE,
                                            PlayerSource(player, null))
                                    extractAEPower(player.currentEquippedItem, 20.0)
                                }
                                1 -> {
                                    request.stackSize = 1
                                    world.func_147480_a(x, y, z, true)
                                    placeBlock(request.itemStack, world, player, x, y, z, side, xOffset, yOffset,
                                            zOffset)
                                    AEApi.instance().registries().cell().getCellInventory(itemstack, null,
                                            StorageChannel.ITEMS).extractItems(request, Actionable.MODULATE,
                                            PlayerSource(player, null))
                                }
                                2 -> {
                                    request.stackSize = 9
                                    if (storageStack.stackSize > 9
                                            && power >= 180.0) {
                                        when (ForgeDirection.getOrientation(side)) {
                                            ForgeDirection.DOWN -> {
                                                var posX = x - 1
                                                while (posX < x + 2) {
                                                    var posZ = z - 1
                                                    while (posZ < z + 2) {
                                                        if (world.getBlock(posX, y,
                                                                        posZ) !== Blocks.bedrock && world.getBlock(
                                                                        posX, y, posZ).getBlockHardness(world, posX, y,
                                                                        posZ) >= 0.0f) {
                                                            world.func_147480_a(posX, y, posZ, true)
                                                            placeBlock(request.itemStack, world, player, x, y, z, side,
                                                                    xOffset,
                                                                    yOffset, zOffset)
                                                        }
                                                        posZ++
                                                    }
                                                    posX++
                                                }
                                                AEApi.instance().registries().cell().getCellInventory(itemstack, null,
                                                        StorageChannel.ITEMS).extractItems(request, Actionable.MODULATE,
                                                        PlayerSource(player, null))
                                            }
                                            ForgeDirection.EAST -> {
                                                var posZ = z - 1
                                                while (posZ < z + 2) {
                                                    var posY = y - 1
                                                    while (posY < y + 2) {
                                                        if (world.getBlock(x, posY,
                                                                        posZ) !== Blocks.bedrock && world.getBlock(
                                                                        x,
                                                                        posY, posZ).getBlockHardness(world, x, posY,
                                                                        posZ) >= 0.0f) {
                                                            world.func_147480_a(x, posY, posZ, true)
                                                            placeBlock(request.itemStack, world, player, x, posY, posZ,
                                                                    side,
                                                                    xOffset,
                                                                    yOffset, zOffset)
                                                        }
                                                        posY++
                                                    }
                                                    posZ++
                                                }
                                                AEApi.instance().registries().cell().getCellInventory(itemstack, null,
                                                        StorageChannel.ITEMS).extractItems(request, Actionable.MODULATE,
                                                        PlayerSource(player, null))
                                            }
                                            ForgeDirection.NORTH -> {
                                                var posX = x - 1
                                                while (posX < x + 2) {
                                                    var posY = y - 1
                                                    while (posY < y + 2) {
                                                        if (world.getBlock(posX, posY,
                                                                        z) !== Blocks.bedrock && world.getBlock(
                                                                        posX,
                                                                        posY, z).getBlockHardness(world, posX, posY,
                                                                        z) >= 0.0f) {
                                                            world.func_147480_a(posX, posY,
                                                                    z, true)
                                                            placeBlock(
                                                                    request.itemStack,
                                                                    world, player, posX,
                                                                    posY, z, side, xOffset,
                                                                    yOffset, zOffset)
                                                        }
                                                        posY++
                                                    }
                                                    posX++
                                                }
                                                AEApi.instance()
                                                        .registries()
                                                        .cell()
                                                        .getCellInventory(itemstack,
                                                                null,
                                                                StorageChannel.ITEMS)
                                                        .extractItems(
                                                                request,
                                                                Actionable.MODULATE,
                                                                PlayerSource(
                                                                        player, null))
                                            }
                                            ForgeDirection.SOUTH -> {
                                                var posX = x - 1
                                                while (posX < x + 2) {
                                                    var posY = y - 1
                                                    while (posY < y + 2) {
                                                        if (world.getBlock(posX, posY,
                                                                        z) !== Blocks.bedrock && world.getBlock(
                                                                        posX,
                                                                        posY, z).getBlockHardness(world, posX, posY,
                                                                        z) >= 0.0f) {
                                                            world.func_147480_a(posX, posY,
                                                                    z, true)
                                                            placeBlock(
                                                                    request.itemStack,
                                                                    world, player, posX,
                                                                    posY, z, side, xOffset,
                                                                    yOffset, zOffset)
                                                        }
                                                        posY++
                                                    }
                                                    posX++
                                                }
                                                AEApi.instance()
                                                        .registries()
                                                        .cell()
                                                        .getCellInventory(itemstack,
                                                                null,
                                                                StorageChannel.ITEMS)
                                                        .extractItems(
                                                                request,
                                                                Actionable.MODULATE,
                                                                PlayerSource(
                                                                        player, null))
                                            }
                                            ForgeDirection.UNKNOWN -> {
                                            }
                                            ForgeDirection.UP -> {
                                                var posX = x - 1
                                                while (posX < x + 2) {
                                                    var posZ = z - 1
                                                    while (posZ < z + 2) {
                                                        if (world.getBlock(posX, y,
                                                                        posZ) !== Blocks.bedrock && world.getBlock(
                                                                        posX, y, posZ).getBlockHardness(world, posX, y,
                                                                        posZ) >= 0.0f) {
                                                            world.func_147480_a(posX, y,
                                                                    posZ, true)
                                                            placeBlock(
                                                                    request.itemStack,
                                                                    world, player, posX, y,
                                                                    posZ, side, xOffset,
                                                                    yOffset, zOffset)
                                                        }
                                                        posZ++
                                                    }
                                                    posX++
                                                }
                                                AEApi.instance()
                                                        .registries()
                                                        .cell()
                                                        .getCellInventory(itemstack,
                                                                null,
                                                                StorageChannel.ITEMS)
                                                        .extractItems(
                                                                request,
                                                                Actionable.MODULATE,
                                                                PlayerSource(
                                                                        player, null))
                                            }
                                            ForgeDirection.WEST -> {
                                                var posZ = z - 1
                                                while (posZ < z + 2) {
                                                    var posY = y - 1
                                                    while (posY < y + 2) {
                                                        if (world.getBlock(x, posY,
                                                                        posZ) !== Blocks.bedrock && world.getBlock(
                                                                        x,
                                                                        posY, posZ).getBlockHardness(world, x, posY,
                                                                        posZ) >= 0.0f) {
                                                            world.func_147480_a(x, posY,
                                                                    posZ, true)
                                                            placeBlock(
                                                                    request.itemStack,
                                                                    world, player, x, posY,
                                                                    posZ, side, xOffset,
                                                                    yOffset, zOffset)
                                                        }
                                                        posY++
                                                    }
                                                    posZ++
                                                }
                                                AEApi.instance()
                                                        .registries()
                                                        .cell()
                                                        .getCellInventory(itemstack,
                                                                null,
                                                                StorageChannel.ITEMS)
                                                        .extractItems(
                                                                request,
                                                                Actionable.MODULATE,
                                                                PlayerSource(
                                                                        player, null))
                                            }
                                            else -> {
                                            }
                                        }
                                    }
                                }
                            }
                            true
                        } else {
                            false
                        }
                    } else {
                        player.addChatMessage(ChatComponentTranslation(
                                "extracells.tooltip.onlyblocks"))
                        false
                    }
                } else {
                    false
                }
            } else {
                false
            }
        } else {
            false
        }
    }

    fun placeBlock(itemstack: ItemStack, world: World?,
                   player: EntityPlayer, x: Int, y: Int, z: Int, side: Int, xOffset: Float,
                   yOffset: Float, zOffset: Float) {
        var x = x
        var y = y
        var z = z
        extractAEPower(player.currentEquippedItem, 20.0)
        val itemblock = itemstack.item as ItemBlock
        when (ForgeDirection.getOrientation(side)) {
            ForgeDirection.DOWN -> {
                itemblock.onItemUseFirst(itemstack, player, world, x, y++, z, side,
                        xOffset, yOffset, zOffset)
                itemblock.onItemUse(itemstack, player, world, x, y++, z, side,
                        xOffset, yOffset, zOffset)
            }
            ForgeDirection.EAST -> {
                itemblock.onItemUseFirst(itemstack, player, world, x--, y, z, side,
                        xOffset, yOffset, zOffset)
                itemblock.onItemUse(itemstack, player, world, x--, y, z, side,
                        xOffset, yOffset, zOffset)
            }
            ForgeDirection.NORTH -> {
                itemblock.onItemUseFirst(itemstack, player, world, x, y, z++, side,
                        xOffset, yOffset, zOffset)
                itemblock.onItemUse(itemstack, player, world, x, y, z++, side,
                        xOffset, yOffset, zOffset)
            }
            ForgeDirection.SOUTH -> {
                itemblock.onItemUseFirst(itemstack, player, world, x, y, z--, side,
                        xOffset, yOffset, zOffset)
                itemblock.onItemUse(itemstack, player, world, x, y, z--, side,
                        xOffset, yOffset, zOffset)
            }
            ForgeDirection.UNKNOWN -> {
            }
            ForgeDirection.UP -> {
                itemblock.onItemUseFirst(itemstack, player, world, x, y--, z, side,
                        xOffset, yOffset, zOffset)
                itemblock.onItemUse(itemstack, player, world, x, y--, z, side,
                        xOffset, yOffset, zOffset)
            }
            ForgeDirection.WEST -> {
                itemblock.onItemUseFirst(itemstack, player, world, x++, y, z, side,
                        xOffset, yOffset, zOffset)
                itemblock.onItemUse(itemstack, player, world, x++, y, z, side,
                        xOffset, yOffset, zOffset)
            }
            else -> {
            }
        }
    }

    @Optional.Method(modid = "CoFHAPI|energy")
    override fun receiveEnergy(container: ItemStack, maxReceive: Int,
                               simulate: Boolean): Int {
        if (container == null || container.itemDamage != 4) return 0
        return if (simulate) {
            val current = PowerUnits.AE.convertTo(PowerUnits.RF,
                    getAECurrentPower(container))
            val max = PowerUnits.AE.convertTo(PowerUnits.RF,
                    getAEMaxPower(container))
            if (max - current >= maxReceive) maxReceive else (max - current).toInt()
        } else {
            val notStored = PowerUnits.AE
                    .convertTo(
                            PowerUnits.RF,
                            injectAEPower(container, PowerUnits.RF.convertTo(
                                    PowerUnits.AE, maxReceive.toDouble()))).toInt()
            maxReceive - notStored
        }
    }

    override fun registerIcons(iconRegister: IIconRegister) {
        icons = arrayOfNulls(suffixes.size)
        for (i in suffixes.indices) {
            icons[i] = iconRegister.registerIcon("extracells:"
                    + "storage.physical." + suffixes[i])
        }
    }

    override fun setFuzzyMode(`is`: ItemStack, fzMode: FuzzyMode) {
        if (!`is`.hasTagCompound()) `is`.tagCompound = NBTTagCompound()
        `is`.tagCompound.setInteger("fuzzyMode", fzMode.ordinal)
    }

    override fun showDurabilityBar(itemStack: ItemStack): Boolean {
        return if (itemStack == null) false else itemStack.itemDamage == 4
    }

    override fun storableInStorageCell(): Boolean {
        return false
    }

    companion object {
        val suffixes = arrayOf("256k", "1024k", "4096k", "16384k", "container")
        val bytes_cell = intArrayOf(262144, 1048576, 4194304, 16777216, 65536)
        val types_cell = intArrayOf(63, 63, 63, 63, 1)
    }

    init {
        setMaxStackSize(1)
        maxDamage = 0
        setHasSubtypes(true)
    }
}