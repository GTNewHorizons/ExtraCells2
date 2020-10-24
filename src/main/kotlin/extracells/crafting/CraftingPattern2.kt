package extracells.crafting

import appeng.api.AEApi
import appeng.api.networking.crafting.ICraftingPatternDetails
import appeng.api.storage.data.IAEItemStack
import extracells.registries.ItemEnum
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

class CraftingPattern2(_pattern: ICraftingPatternDetails?) : CraftingPattern(_pattern) {
    private var needExtra = false
    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        if (this.javaClass != obj.javaClass) return false
        val other = obj as CraftingPattern
        return if (pattern != null && other.pattern != null) pattern == other.pattern else false
    }

    override fun getCondensedInputs(): Array<IAEItemStack> {
        var s = super.getCondensedInputs()
        if (s!!.size == 0) {
            s = arrayOfNulls(1)
            s[0] = AEApi
                    .instance()
                    .storage()
                    .createItemStack(
                            ItemStack(ItemEnum.FLUIDPATTERN.item))
            needExtra = true
        }
        return s
    }

    override fun getCondensedOutputs(): Array<IAEItemStack> {
        condensedInputs
        val s = super.getCondensedOutputs()
        if (needExtra) {
            val s2 = arrayOfNulls<IAEItemStack>(s!!.size + 1)
            for (i in s.indices) {
                s2[i] = s[i]
            }
            s2[s.size] = AEApi
                    .instance()
                    .storage()
                    .createItemStack(
                            ItemStack(ItemEnum.FLUIDPATTERN.item))
            return s2
        }
        return s
    }

    override fun getInputs(): Array<IAEItemStack> {
        var `in` = super.getInputs()
        if (`in`!!.size == 0) {
            `in` = arrayOfNulls(1)
            `in`[0] = AEApi
                    .instance()
                    .storage()
                    .createItemStack(
                            ItemStack(ItemEnum.FLUIDPATTERN.item))
        } else {
            for (s in `in`) {
                if (s != null) return `in`
            }
            `in`[0] = AEApi
                    .instance()
                    .storage()
                    .createItemStack(
                            ItemStack(ItemEnum.FLUIDPATTERN.item))
        }
        return `in`
    }

    override fun getOutputs(): Array<IAEItemStack> {
        var out = super.getOutputs()
        condensedInputs
        if (!needExtra) return out
        if (out!!.size == 0) {
            out = arrayOfNulls(1)
            out[0] = AEApi
                    .instance()
                    .storage()
                    .createItemStack(
                            ItemStack(ItemEnum.FLUIDPATTERN.item))
        } else {
            for (i in out.indices) {
                if (out[i] == null) {
                    out[i] = AEApi
                            .instance()
                            .storage()
                            .createItemStack(
                                    ItemStack(ItemEnum.FLUIDPATTERN
                                            .item))
                    return out
                }
            }
            val s2 = arrayOfNulls<IAEItemStack>(out.size + 1)
            for (i in out.indices) {
                s2[i] = out[i]
            }
            s2[out.size] = AEApi
                    .instance()
                    .storage()
                    .createItemStack(
                            ItemStack(ItemEnum.FLUIDPATTERN.item))
            return s2
        }
        return out
    }

    override fun getPattern(): ItemStack {
        val p = pattern!!.pattern ?: return null
        val s = ItemStack(ItemEnum.CRAFTINGPATTERN.item, 1, 1)
        val tag = NBTTagCompound()
        tag.setTag("item", p.writeToNBT(NBTTagCompound()))
        s.tagCompound = tag
        return s
    }
}