package extracells.item

import appeng.api.AEApi
import appeng.api.implementations.items.IItemGroup
import appeng.api.parts.IPart
import appeng.api.parts.IPartItem
import cpw.mods.fml.common.FMLLog
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.api.ECApi
import extracells.registries.PartEnum
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.MathHelper
import net.minecraft.world.World
import org.apache.logging.log4j.Level

class ItemPartECBase : Item(), IPartItem, IItemGroup {
    override fun createPartFromItemStack(itemStack: ItemStack): IPart? {
        return try {
            PartEnum.values()[MathHelper.clamp_int(
                    itemStack.itemDamage, 0, PartEnum.values().size - 1)]
                    .newInstance(itemStack)
        } catch (ex: Throwable) {
            FMLLog.log(
                    Level.ERROR,
                    ex,
                    """
                        ExtraCells2 severe error - could not create AE2 Part from ItemStack! This should not happen!
                        [ExtraCells2 SEVERE] Contact Leonelf/M3gaFr3ak with the following stack trace.
                        [ExtraCells2 SEVERE] Offending item: '%s'
                        """.trimIndent(),
                    itemStack.toString())
            null
        }
    }

    override fun getItemStackDisplayName(stack: ItemStack): String {
        if (stack == null) return super.getItemStackDisplayName(null)
        return if (stack.itemDamage == PartEnum.INTERFACE.ordinal) ECApi.instance()!!.blocks().blockInterface().maybeItem().get().getItemStackDisplayName(
                ECApi.instance()!!.blocks().blockInterface().maybeStack(1).get()) else super.getItemStackDisplayName(
                stack)
    }

    @SideOnly(Side.CLIENT)
    override fun getSpriteNumber(): Int {
        return 0
    }

    override fun getSubItems(item: Item, creativeTab: CreativeTabs, itemList: MutableList<*>) {
        for (i in PartEnum.values().indices) {
            val part = PartEnum.values()[i]
            if (part.mod == null || part.mod.isEnabled) itemList.add(ItemStack(item, 1, i))
        }
    }

    override fun getUnlocalizedGroupName(otherItems: Set<ItemStack>,
                                         itemStack: ItemStack): String {
        return PartEnum.values()[MathHelper.clamp_int(
                itemStack.itemDamage, 0, PartEnum.values().size - 1)]
                .groupName
    }

    override fun getUnlocalizedName(itemStack: ItemStack): String {
        return PartEnum.values()[MathHelper.clamp_int(
                itemStack.itemDamage, 0, PartEnum.values().size - 1)]
                .unlocalizedName
    }

    override fun onItemUse(`is`: ItemStack, player: EntityPlayer, world: World,
                           x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        return AEApi.instance().partHelper()
                .placeBus(`is`, x, y, z, side, player, world)
    }

    override fun registerIcons(_iconRegister: IIconRegister) {}

    init {
        maxDamage = 0
        setHasSubtypes(true)
        AEApi.instance().partHelper().setItemBusRenderer(this)
        for (part in PartEnum.values()) {
            val possibleUpgradesList = part.upgrades
            for (upgrade in possibleUpgradesList!!.keys) {
                upgrade!!.registerItem(ItemStack(this, 1, part.ordinal),
                        possibleUpgradesList[upgrade]!!)
            }
        }
    }
}