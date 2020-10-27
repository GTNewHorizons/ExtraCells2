package extracells.tileentity

import net.minecraft.tileentity.TileEntity
open class TileEntityWalrus : TileEntity() {
    override fun canUpdate(): Boolean {
        return false
    }
}