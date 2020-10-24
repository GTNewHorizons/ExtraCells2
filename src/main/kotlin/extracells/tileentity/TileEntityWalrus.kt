package extracells.tileentity

import net.minecraft.tileentity.TileEntity

class TileEntityWalrus : TileEntity() {
    override fun canUpdate(): Boolean {
        return false
    }
}