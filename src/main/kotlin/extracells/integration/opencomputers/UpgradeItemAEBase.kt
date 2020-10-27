package extracells.integration.opencomputers

import cpw.mods.fml.common.Optional.*
import extracells.item.ItemECBase
import li.cil.oc.api.CreativeTab
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.driver.item.Slot
import li.cil.oc.api.internal.Drone
import li.cil.oc.api.internal.Robot
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.api.network.ManagedEnvironment
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.EnumRarity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

@InterfaceList(
        Interface(iface = "li.cil.oc.api.driver.item.HostAware", modid = "OpenComputers", striprefs = true)
)
abstract class UpgradeItemAEBase : ItemECBase(), HostAware {

    @Method(modid = "OpenComputers")
    override fun setCreativeTab(creativeTabs: CreativeTabs): Item = super.setCreativeTab(CreativeTab.instance)

    @Method(modid = "OpenComputers")
    override fun tier(stack: ItemStack): Int =
            when (stack.itemDamage) {
        0 -> 2
        1 -> 1
        else -> 0
    }


    @Method(modid = "OpenComputers")
    override fun slot(stack: ItemStack?): String = Slot.Upgrade

    @Method(modid = "OpenComputers")
    override fun worksWith(stack: ItemStack?): Boolean = stack != null && stack.item == this

    @Method(modid = "OpenComputers")
    override fun createEnvironment(stack: ItemStack?, host: EnvironmentHost): ManagedEnvironment? =
        if (stack != null && stack.item == this && worksWith(stack, host::class.java))
            UpgradeAE(host)
        else
            null

    override fun getRarity (stack: ItemStack) : EnumRarity =
        when(stack.itemDamage) {
            0 -> EnumRarity.rare
            1 -> EnumRarity.uncommon
            else -> super.getRarity(stack)
        }

    @Method(modid = "OpenComputers")
    override fun dataTag(stack: ItemStack) : NBTTagCompound{
        if (!stack.hasTagCompound()) {
            stack.tagCompound = NBTTagCompound()
        }
        val nbt: NBTTagCompound = stack.tagCompound
        if (!nbt.hasKey("oc:data")) {
            nbt.setTag("oc:data",  NBTTagCompound())
        }
        nbt.getCompoundTag("oc:data")
        return nbt
    }

    @Method(modid = "OpenComputers")
    override fun worksWith(stack: ItemStack?, host: Class<out EnvironmentHost>?): Boolean =
        worksWith(stack) && host != null && (Robot::class.java.isAssignableFrom(host) || Drone::class.java.isAssignableFrom(host))


    @InterfaceList(
            Interface(iface = "li.cil.oc.api.driver.EnvironmentProvider", modid = "OpenComputers", striprefs = true)
    )
    inner class Provider : EnvironmentProvider {
        @Method(modid = "OpenComputers")
        override fun getEnvironment(stack: ItemStack) =
        if (worksWith(stack))
            UpgradeAE::class.java
        else null
    }
}
