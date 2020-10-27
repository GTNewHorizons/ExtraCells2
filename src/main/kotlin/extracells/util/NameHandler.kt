package extracells.util

import appeng.api.recipes.ISubItemResolver
import appeng.api.recipes.ResolverResult
import extracells.registries.BlockEnum
import extracells.registries.ItemEnum
import extracells.registries.PartEnum
open class NameHandler : ISubItemResolver {
    override fun resolveItemByName(namespace: String, fullName: String): Any? {
        if (namespace != "extracells") return null

        // Fluid Cells
        if (fullName == "fluidCell1k") return ResolverResult(ItemEnum.FLUIDSTORAGE.internalName, 0)
        if (fullName == "fluidCell4k") return ResolverResult(ItemEnum.FLUIDSTORAGE.internalName, 1)
        if (fullName == "fluidCell16k") return ResolverResult(ItemEnum.FLUIDSTORAGE.internalName, 2)
        if (fullName == "fluidCell64k") return ResolverResult(ItemEnum.FLUIDSTORAGE.internalName, 3)
        if (fullName == "fluidCell256k") return ResolverResult(ItemEnum.FLUIDSTORAGE.internalName, 4)
        if (fullName == "fluidCell1024k") return ResolverResult(ItemEnum.FLUIDSTORAGE.internalName, 5)
        if (fullName == "fluidCell4096k") return ResolverResult(ItemEnum.FLUIDSTORAGE.internalName, 6)
        if (fullName == "fluidCellPortable") return ResolverResult(ItemEnum.FLUIDSTORAGEPORTABLE.internalName, 0)

//        // Gas Cells
//        if (fullName == "gasCell1k") return ResolverResult(ItemEnum.GASSTORAGE.internalName, 0)
//        if (fullName == "gasCell4k") return ResolverResult(ItemEnum.GASSTORAGE.internalName, 1)
//        if (fullName == "gasCell16k") return ResolverResult(ItemEnum.GASSTORAGE.internalName, 2)
//        if (fullName == "gasCell64k") return ResolverResult(ItemEnum.GASSTORAGE.internalName, 3)
//        if (fullName == "gasCell256k") return ResolverResult(ItemEnum.GASSTORAGE.internalName, 4)
//        if (fullName == "gasCell1024k") return ResolverResult(ItemEnum.GASSTORAGE.internalName, 5)
//        if (fullName == "gasCell4096k") return ResolverResult(ItemEnum.GASSTORAGE.internalName, 6)
        //		if (fullName.equals("gasCellPortable"))
//			return new ResolverResult(ItemEnum.GASSTORAGEPORTABLE.getInternalName(), 0);

        // Physical Cells
        if (fullName == "physCell256k") return ResolverResult(ItemEnum.PHYSICALSTORAGE.internalName, 0)
        if (fullName == "physCell1024k") return ResolverResult(ItemEnum.PHYSICALSTORAGE.internalName, 1)
        if (fullName == "physCell4096k") return ResolverResult(ItemEnum.PHYSICALSTORAGE.internalName, 2)
        if (fullName == "physCell16384k") return ResolverResult(ItemEnum.PHYSICALSTORAGE.internalName, 3)
        if (fullName == "physCellContainer") return ResolverResult(ItemEnum.PHYSICALSTORAGE.internalName, 4)

        // Fluid Storage Components
        if (fullName == "cell1kPartFluid") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 4)
        if (fullName == "cell4kPartFluid") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 5)
        if (fullName == "cell16kPartFluid") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 6)
        if (fullName == "cell64kPartFluid") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 7)
        if (fullName == "cell256kPartFluid") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 8)
        if (fullName == "cell1024kPartFluid") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 9)
        if (fullName == "cell4096kPartFluid") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 10)

        // Gas Storage Components
        if (fullName == "cell1kPartGas") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 11)
        if (fullName == "cell4kPartGas") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 12)
        if (fullName == "cell16kPartGas") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 13)
        if (fullName == "cell64kPartGas") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 14)
        if (fullName == "cell256kPartGas") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 15)
        if (fullName == "cell1024kPartGas") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 16)
        if (fullName == "cell4096kPartGas") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 17)

        // Physical Storage Components
        if (fullName == "cell256kPart") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 0)
        if (fullName == "cell1024kPart") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 1)
        if (fullName == "cell4096kPart") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 2)
        if (fullName == "cell16384kPart") return ResolverResult(ItemEnum.STORAGECOMPONENT.internalName, 3)

        // Physical Storage Casing
        if (fullName == "physCasing") return ResolverResult(ItemEnum.STORAGECASING.internalName, 0)

        // Fluid Storage Casing
        if (fullName == "fluidCasing") return ResolverResult(ItemEnum.STORAGECASING.internalName, 1)

        // Fluid Storage Casing
        if (fullName == "gasCasing") return ResolverResult(ItemEnum.STORAGECASING.internalName, 2)

        // Parts
        if (fullName == "partFluidImportBus") return ResolverResult(ItemEnum.PARTITEM.internalName,
                PartEnum.FLUIDIMPORT.ordinal)
        if (fullName == "partFluidExportBus") return ResolverResult(ItemEnum.PARTITEM.internalName,
                PartEnum.FLUIDEXPORT.ordinal)
        if (fullName == "partFluidStorageBus") return ResolverResult(ItemEnum.PARTITEM.internalName,
                PartEnum.FLUIDSTORAGE.ordinal)
        if (fullName == "partFluidTerminal") return ResolverResult(ItemEnum.PARTITEM.internalName,
                PartEnum.FLUIDTERMINAL.ordinal)
        if (fullName == "partFluidLevelEmitter") return ResolverResult(ItemEnum.PARTITEM.internalName,
                PartEnum.FLUIDLEVELEMITTER.ordinal)
        if (fullName == "partFluidAnnihilationPlane") return ResolverResult(ItemEnum.PARTITEM.internalName,
                PartEnum.FLUIDPANEANNIHILATION.ordinal)
        if (fullName == "partFluidFormationPlane") return ResolverResult(ItemEnum.PARTITEM.internalName,
                PartEnum.FLUIDPANEFORMATION.ordinal)
        if (fullName == "partBattery") return ResolverResult(ItemEnum.PARTITEM.internalName, PartEnum.BATTERY.ordinal)
        if (fullName == "partDrive") return ResolverResult(ItemEnum.PARTITEM.internalName, PartEnum.DRIVE.ordinal)
        if (fullName == "partInterface") return ResolverResult(ItemEnum.PARTITEM.internalName,
                PartEnum.INTERFACE.ordinal)
        if (fullName == "partStorageMonitor") //TODO: Remve
            return ResolverResult(ItemEnum.PARTITEM.internalName, PartEnum.FLUIDMONITOR.ordinal)
        if (fullName == "partConversionMonitor") //TODO: Remve
            return ResolverResult(ItemEnum.PARTITEM.internalName, PartEnum.FLUIDCONVERSIONMONITOR.ordinal)
        if (fullName == "partFluidStorageMonitor") return ResolverResult(ItemEnum.PARTITEM.internalName,
                PartEnum.FLUIDMONITOR.ordinal)
        if (fullName == "partFluidConversionMonitor") return ResolverResult(ItemEnum.PARTITEM.internalName,
                PartEnum.FLUIDCONVERSIONMONITOR.ordinal)
        if (fullName == "partOreDictExportBus") return ResolverResult(ItemEnum.PARTITEM.internalName,
                PartEnum.OREDICTEXPORTBUS.ordinal)
        //		if (fullName.equals("partGasImportBus"))
//			return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.GASIMPORT.ordinal());
//		if (fullName.equals("partGasExportBus"))
//			return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.GASEXPORT.ordinal());
//		if (fullName.equals("partGasStorageBus"))
//			return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.GASSTORAGE.ordinal());
//		if (fullName.equals("partGasTerminal"))
//			return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.GASTERMINAL.ordinal());
//		if (fullName.equals("partGasLevelEmitter"))
//			return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.GASLEVELEMITTER.ordinal());
//		if (fullName.equals("partGasStorageMonitor"))
//			return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.GASMONITOR.ordinal());
//		if (fullName.equals("partGasConversionMonitor"))
//			return new ResolverResult(ItemEnum.PARTITEM.getInternalName(), PartEnum.GASCONVERSIONMONITOR.ordinal());

        // MISC
        if (fullName == "certusTank") return ResolverResult(BlockEnum.CERTUSTANK.internalName, 0)
        if (fullName == "fluidPattern") return ResolverResult(ItemEnum.FLUIDPATTERN.internalName, 0)
        if (fullName == "fluidCrafter") return ResolverResult(BlockEnum.FLUIDCRAFTER.internalName, 0)
        if (fullName == "wirelessFluidTerminal") return ResolverResult(ItemEnum.FLUIDWIRELESSTERMINAL.internalName, 0)
//        if (fullName == "wirelessGasTerminal") return ResolverResult(ItemEnum.GASWIRELESSTERMINAL.internalName, 0)
        if (fullName == "walrus") return ResolverResult(BlockEnum.WALRUS.internalName, 0)
        if (fullName == "interface") return ResolverResult(BlockEnum.ECBASEBLOCK.internalName, 0)
        if (fullName == "fluidFiller") return ResolverResult(BlockEnum.ECBASEBLOCK.internalName, 1)
        if (fullName == "blockVibrationChamberFluid") return ResolverResult(BlockEnum.VIBRANTCHAMBERFLUID.internalName,
                0)
        if (fullName == "hardMEDrive") return ResolverResult(BlockEnum.BLASTRESISTANTMEDRIVE.internalName, 0)
        if (fullName == "craftingStorage256k") return ResolverResult(BlockEnum.CRAFTINGSTORAGE.internalName, 0)
        if (fullName == "craftingStorage1024k") return ResolverResult(BlockEnum.CRAFTINGSTORAGE.internalName, 1)
        if (fullName == "craftingStorage4096k") return ResolverResult(BlockEnum.CRAFTINGSTORAGE.internalName, 2)
        return if (fullName == "craftingStorage16384k") ResolverResult(BlockEnum.CRAFTINGSTORAGE.internalName, 3) else null
    }
}