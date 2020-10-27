package extracells.item

import extracells.api.ECApi
import extracells.api.IWirelessFluidTermHandler
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraft.world.World

object ItemWirelessTerminalFluid : WirelessTermBase(), IWirelessFluidTermHandler {
    internal var icon: IIcon? = null
    override fun getIconFromDamage(dmg: Int): IIcon? = this.icon
    override fun getUnlocalizedName(itemStack: ItemStack): String =super.getUnlocalizedName(itemStack).replace("item.extracells", "extracells.item")
    override fun isItemNormalWirelessTermToo(its: ItemStack?): Boolean = false
    override fun onItemRightClick(itemStack: ItemStack, world: World, entityPlayer: EntityPlayer): ItemStack? = ECApi.instance()?.openWirelessFluidTerminal(entityPlayer, itemStack, world)
    override fun registerIcons(iconRegister: IIconRegister) {
        this.icon = iconRegister.registerIcon("extracells:" + "terminal.fluid.wireless")
    }
}