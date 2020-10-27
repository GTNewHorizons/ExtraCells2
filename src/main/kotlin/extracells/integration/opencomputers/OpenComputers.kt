package extracells.integration.opencomputers

import appeng.api.AEApi
import extracells.integration.opencomputers.DriverFluidExportBus.Provider
import extracells.item.ItemOCUpgrade
import li.cil.oc.api.Driver

object OpenComputers {
    fun init() {
        Driver.add(DriverFluidExportBus())
        Driver.add(Provider())
        Driver.add(DriverFluidImportBus())
        Driver.add(Provider())
        Driver.add(DriverOreDictExportBus())
        Driver.add(Provider())
        Driver.add(DriverFluidInterface())
        Driver.add(Provider())
//        if (Mods.MEKANISMGAS.isEnabled) {
//            Driver.add(DriverGasExportBus())
//            Driver.add(Provider())
//            Driver.add(DriverGasImportBus())
//            Driver.add(Provider())
//        }
        Driver.add(ItemOCUpgrade)
        Driver.add(ItemOCUpgrade.Provider())
        AEApi.instance().registries().wireless().registerWirelessHandler(WirelessHandlerUpgradeAE)
        OCRecipes.loadRecipes()
        ExtraCellsPathProvider
    }
}