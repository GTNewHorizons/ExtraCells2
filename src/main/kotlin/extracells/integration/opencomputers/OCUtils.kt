package extracells.integration.opencomputers

import li.cil.oc.api.API
import li.cil.oc.api.detail.ItemInfo
import li.cil.oc.common.item.data.DroneData
import li.cil.oc.common.item.data.RobotData
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object OCUtils {
    fun isRobot(stack: ItemStack?): Boolean {
        val item: ItemInfo? = API.items.get(stack)
        return item?.name() == "robot"
    }

    fun isDrone(stack: ItemStack): Boolean {
        val item: ItemInfo = API.items.get(stack)
        return item.name() == "drone"
    }

    fun getComponent(robot: RobotData, item: Item, meta: Int): ItemStack? {
        for (component : ItemStack? in robot.components()) {
            if (component != null && component.item == item)
                return component
        }
        return null
    }

    fun getComponent(robot: RobotData, item: Item): ItemStack? = getComponent(robot, item, 0)
    fun getComponent(drone: DroneData, item: Item, meta: Int): ItemStack? {
        for (component: ItemStack? in drone.components()) {
            if (component != null && component.item == item) return component
        }
        return null
    }

    fun getComponent(drone: DroneData, item: Item): ItemStack? = getComponent(drone, item, 0)
}