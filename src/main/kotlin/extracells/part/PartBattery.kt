package extracells.part

import appeng.api.config.AccessRestriction
import appeng.api.config.Actionable
import appeng.api.config.PowerMultiplier
import appeng.api.implementations.items.IAEItemPowerStorage
import appeng.api.networking.energy.IAEPowerStorage
import appeng.api.networking.events.MENetworkPowerStorage
import appeng.api.parts.IPartCollisionHelper
import appeng.api.parts.IPartRenderHelper
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.network.AbstractPacket
import extracells.render.TextureManager
import extracells.util.inventory.ECPrivateInventory
import extracells.util.inventory.IInventoryUpdateReceiver
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import java.io.IOException

class PartBattery : PartECBase(), IAEPowerStorage, IInventoryUpdateReceiver {
    private var batteryIcon = TextureManager.BATTERY_FRONT.texture
    private var battery: ItemStack? = null
    var handler: IAEItemPowerStorage? = null
    private val inventory: ECPrivateInventory = object : ECPrivateInventory(
            "extracells.part.battery", 1, 1) {
        override fun isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean {
            return (itemStack != null
                    && itemStack.item is IAEItemPowerStorage)
        }
    }

    override fun getDrops(drops: MutableList<ItemStack>, wrenched: Boolean) {
        for (stack in inventory.slots) {
            if (stack == null) continue
            drops.add(stack)
        }
    }

    override fun cableConnectionRenderTo(): Int {
        return 2
    }

    override fun extractAEPower(amt: Double, mode: Actionable,
                                usePowerMultiplier: PowerMultiplier): Double {
        return if (handler == null || battery == null) 0 else handler!!.extractAEPower(
                if (mode == Actionable.MODULATE) battery else battery!!
                        .copy(), usePowerMultiplier.multiply(amt))
    }

    override fun getAECurrentPower(): Double {
        return if (handler == null || battery == null) 0 else handler!!.getAECurrentPower(
                battery)
    }

    override fun getAEMaxPower(): Double {
        return if (handler == null || battery == null) 0 else handler!!.getAEMaxPower(battery)
    }

    override fun getBoxes(bch: IPartCollisionHelper) {
        bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0)
    }

    override fun getPowerFlow(): AccessRestriction {
        return if (handler == null || battery == null) AccessRestriction.NO_ACCESS else handler!!.getPowerFlow(battery)
    }

    override fun injectAEPower(amt: Double, mode: Actionable): Double {
        return if (handler == null || battery == null) 0 else handler!!.injectAEPower(
                if (mode == Actionable.MODULATE) battery else battery!!
                        .copy(), amt)
    }

    override fun isAEPublicPowerStorage(): Boolean {
        return true
    }

    override fun onInventoryChanged() {
        battery = inventory.getStackInSlot(0)
        if (battery != null
                && battery!!.item is IAEItemPowerStorage) {
            batteryIcon = battery!!.iconIndex
            handler = battery!!.item as IAEItemPowerStorage
        } else {
            batteryIcon = null
            handler = null
        }
        val node = gridNode
        if (node != null) {
            val grid = node.grid
            grid?.postEvent(MENetworkPowerStorage(this,
                    MENetworkPowerStorage.PowerEventType.REQUEST_POWER))
            host.markForUpdate()
        }
    }

    override fun readFromNBT(data: NBTTagCompound) {
        super.readFromNBT(data)
        inventory.readFromNBT(data.getTagList("inventory", 10))
        onInventoryChanged()
    }

    @Throws(IOException::class)
    override fun readFromStream(data: ByteBuf): Boolean {
        super.readFromStream(data)
        val iconName: String = AbstractPacket.Companion.readString(data)
        if (iconName != "none") {
            batteryIcon = (Minecraft.getMinecraft()
                    .textureManager
                    .getTexture(TextureMap.locationBlocksTexture) as TextureMap)
                    .getAtlasSprite(iconName)
        } else {
            batteryIcon = TextureManager.BATTERY_FRONT.texture
        }
        return true
    }

    @SideOnly(Side.CLIENT)
    override fun renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks) {
        val side = TextureManager.BUS_SIDE.texture
        rh.setTexture(side, side, side,
                TextureManager.BATTERY_FRONT.textures[0], side, side)
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 16f)
        rh.renderInventoryBox(renderer)
        rh.setBounds(5f, 5f, 13f, 11f, 11f, 14f)
        renderInventoryBusLights(rh, renderer)
    }

    @SideOnly(Side.CLIENT)
    override fun renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper,
                              renderer: RenderBlocks) {
        val side = TextureManager.BUS_SIDE.texture
        rh.setTexture(side, side, side, batteryIcon, side, side)
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 16f)
        rh.renderBlock(x, y, z, renderer)
        rh.setBounds(5f, 5f, 13f, 11f, 11f, 14f)
        renderStaticBusLights(x, y, z, rh, renderer)
    }

    override fun writeToNBT(data: NBTTagCompound) {
        super.writeToNBT(data)
        data.setTag("inventory", inventory.writeToNBT())
    }

    @Throws(IOException::class)
    override fun writeToStream(data: ByteBuf) {
        super.writeToStream(data)
        AbstractPacket.Companion.writeString(if (battery != null) battery!!
                .item.getIconIndex(battery).iconName else "none",
                data)
    }
}