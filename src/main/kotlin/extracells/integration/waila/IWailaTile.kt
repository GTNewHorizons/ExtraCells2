package extracells.integration.waila

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection

interface IWailaTile {
    fun getWailaBody(list: MutableList<String>, tag: NBTTagCompound,
                     side: ForgeDirection?): List<String>

    fun getWailaTag(tag: NBTTagCompound): NBTTagCompound
}