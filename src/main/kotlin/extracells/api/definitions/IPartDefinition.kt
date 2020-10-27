package extracells.api.definitions

import appeng.api.definitions.IItemDefinition

interface IPartDefinition {
    fun partBattery(): IItemDefinition
    fun partConversionMonitor(): IItemDefinition
    fun partDrive(): IItemDefinition
    fun partFluidAnnihilationPlane(): IItemDefinition
    fun partFluidExportBus(): IItemDefinition
    fun partFluidFormationPlane(): IItemDefinition
    fun partFluidImportBus(): IItemDefinition
    fun partFluidLevelEmitter(): IItemDefinition
    fun partFluidStorageBus(): IItemDefinition
    fun partFluidTerminal(): IItemDefinition
    fun partInterface(): IItemDefinition
    fun partOreDictExportBus(): IItemDefinition
    fun partStorageMonitor(): IItemDefinition
}