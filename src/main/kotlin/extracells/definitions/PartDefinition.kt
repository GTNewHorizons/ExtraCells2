package extracells.definitions

import appeng.api.definitions.IItemDefinition
import extracells.api.definitions.IPartDefinition
import extracells.registries.ItemEnum
import extracells.registries.PartEnum

class PartDefinition : IPartDefinition {
    override fun partBattery(): IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PARTITEM.item,
                PartEnum.BATTERY.ordinal)
    }

    override fun partConversionMonitor(): IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PARTITEM.item,
                PartEnum.FLUIDCONVERSIONMONITOR.ordinal)
    }

    override fun partDrive(): IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PARTITEM.item,
                PartEnum.DRIVE.ordinal)
    }

    override fun partFluidAnnihilationPlane(): IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PARTITEM.item,
                PartEnum.FLUIDPANEANNIHILATION.ordinal)
    }

    override fun partFluidExportBus(): IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PARTITEM.item,
                PartEnum.FLUIDIMPORT.ordinal)
    }

    override fun partFluidFormationPlane(): IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PARTITEM.item,
                PartEnum.FLUIDPANEFORMATION.ordinal)
    }

    override fun partFluidImportBus(): IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PARTITEM.item,
                PartEnum.FLUIDEXPORT.ordinal)
    }

    override fun partFluidLevelEmitter(): IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PARTITEM.item,
                PartEnum.FLUIDLEVELEMITTER.ordinal)
    }

    override fun partFluidStorageBus(): IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PARTITEM.item,
                PartEnum.FLUIDSTORAGE.ordinal)
    }

    override fun partFluidTerminal(): IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PARTITEM.item,
                PartEnum.FLUIDTERMINAL.ordinal)
    }

    override fun partInterface(): IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PARTITEM.item,
                PartEnum.INTERFACE.ordinal)
    }

    override fun partOreDictExportBus(): IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PARTITEM.item,
                PartEnum.OREDICTEXPORTBUS.ordinal)
    }

    override fun partStorageMonitor(): IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PARTITEM.item,
                PartEnum.FLUIDMONITOR.ordinal)
    }

    companion object {
        val instance = PartDefinition()
    }
}