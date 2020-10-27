package extracells.block

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World

interface TGuiBlock {
    @SideOnly(Side.CLIENT)
    fun getClientGuiElement(player: EntityPlayer?, world: World, x: Int, y: Int, z: Int): Any? = null
    fun getServerGuiElement(player: EntityPlayer?, world: World, x: Int, y: Int, z: Int): Any? = null
}