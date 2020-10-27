package extracells.integration.opencomputers

import appeng.api.features.IWirelessTermHandler
import appeng.api.util.IConfigManager
import extracells.item.ItemOCUpgrade
import li.cil.oc.common.item.data.DroneData
import li.cil.oc.common.item.data.RobotData
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

object WirelessHandlerUpgradeAE : IWirelessTermHandler {
    override fun canHandle(itemStack: ItemStack?): Boolean {
        if (itemStack == null) return false
        val item: Item? = itemStack.item
        if (item == ItemOCUpgrade) return true
        return (OCUtils.isRobot(itemStack) && OCUtils.getComponent(RobotData(itemStack), ItemOCUpgrade) != null)||
                (OCUtils.isDrone(itemStack) && OCUtils.getComponent(DroneData(itemStack), ItemOCUpgrade) != null)
    }

    override fun usePower(entityPlayer: EntityPlayer, v: Double, itemStack: ItemStack): Boolean = false
    override fun getConfigManager(itemStack: ItemStack): IConfigManager? = null
    override fun hasPower(entityPlayer: EntityPlayer, v: Double, itemStack: ItemStack): Boolean = true
    override fun setEncryptionKey(itemStack: ItemStack, encKey: String, name: String) {
        if (OCUtils.isRobot(itemStack)) {
            setEncryptionKeyRobot(itemStack, encKey, name)
            return
        }
        if (OCUtils.isDrone(itemStack)) {
            setEncryptionKeyDrone(itemStack, encKey, name)
            return
        }
        if (!itemStack.hasTagCompound()) itemStack.tagCompound = NBTTagCompound()
        val tagCompound: NBTTagCompound = itemStack.tagCompound
        tagCompound.setString("key", encKey)
    }

    override fun getEncryptionKey(itemStack: ItemStack?): String {
        if (itemStack == null)
            return ""
        if (OCUtils.isRobot(itemStack))
            return getEncryptionKeyRobot(itemStack)
        if (OCUtils.isDrone(itemStack))
            return getEncryptionKeyDrone(itemStack)
        if (!itemStack.hasTagCompound())
            itemStack.tagCompound = NBTTagCompound()
        return itemStack.tagCompound.getString("key")
    }

    private fun setEncryptionKeyRobot(itemStack: ItemStack, encKey: String, name: String) {
        val robot = RobotData(itemStack)
        val component = OCUtils.getComponent(robot, ItemOCUpgrade)
        if (component != null)
            setEncryptionKey(component, encKey, name)
        robot.save(itemStack)
    }

    private fun getEncryptionKeyRobot(stack: ItemStack): String {
        return getEncryptionKey(OCUtils.getComponent(RobotData(stack), ItemOCUpgrade) ?: return "")
    }

    private fun setEncryptionKeyDrone(itemStack: ItemStack, encKey: String, name: String) {
        val robot = RobotData(itemStack)
        val component = OCUtils.getComponent(robot, ItemOCUpgrade)
        if (component!=null) setEncryptionKey(component, encKey, name)
        robot.save(itemStack)
    }

    private fun getEncryptionKeyDrone(stack: ItemStack): String {
        return getEncryptionKey(OCUtils.getComponent(DroneData(stack), ItemOCUpgrade) ?: return "")
    }
}