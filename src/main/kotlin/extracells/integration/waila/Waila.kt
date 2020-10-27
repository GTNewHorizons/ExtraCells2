package extracells.integration.waila

import appeng.api.parts.IPartHost
import cpw.mods.fml.common.Optional
import cpw.mods.fml.common.event.FMLInterModComms
import extracells.tileentity.TileEntityCertusTank
import mcp.mobius.waila.api.IWailaDataProvider
import mcp.mobius.waila.api.IWailaRegistrar

object Waila {
    fun init() {
        FMLInterModComms.sendMessage("Waila", "register", Waila::class.java.name + ".register")
    }

    @Optional.Method(modid = "Waila")
    fun register(registrar: IWailaRegistrar) {
        val partHost: IWailaDataProvider = PartWailaDataProvider()
        registrar.registerBodyProvider(partHost, IPartHost::class.java)
        registrar.registerNBTProvider(partHost, IPartHost::class.java)
        val tileCertusTank: IWailaDataProvider = TileCertusTankWailaDataProvider()
        registrar.registerBodyProvider(tileCertusTank,
                TileEntityCertusTank::class.java)
        registrar.registerNBTProvider(tileCertusTank,
                TileEntityCertusTank::class.java)
        val blocks: IWailaDataProvider = BlockWailaDataProvider()
        registrar.registerBodyProvider(blocks, IWailaTile::class.java)
        registrar.registerNBTProvider(blocks, IWailaTile::class.java)
    }
}