package extracells.api.definitions

import appeng.api.definitions.IItemDefinition

interface IItemDefinition {
    // Fluid Storage Components
    fun cell1kPartFluid(): IItemDefinition
    fun cell4kPartFluid(): IItemDefinition
    fun cell16kPartFluid(): IItemDefinition
    fun cell64kPartFluid(): IItemDefinition
    fun cell256kPartFluid(): IItemDefinition
    fun cell1024kPartFluid(): IItemDefinition
    fun cell4096kPartFluid(): IItemDefinition

    // Physical Storage Components
    fun cell256kPart(): IItemDefinition
    fun cell1024kPart(): IItemDefinition
    fun cell4096kPart(): IItemDefinition
    fun cell16384kPart(): IItemDefinition

    // Fluid Storage Casing
    fun fluidCasing(): IItemDefinition

    // Fluid Cells
    fun fluidCell1k(): IItemDefinition
    fun fluidCell4k(): IItemDefinition
    fun fluidCell16k(): IItemDefinition
    fun fluidCell64k(): IItemDefinition
    fun fluidCell256k(): IItemDefinition
    fun fluidCell1024k(): IItemDefinition
    fun fluidCell4096k(): IItemDefinition
    fun fluidCellPortable(): IItemDefinition

    // Physical Storage Casing
    fun physCasing(): IItemDefinition

    // Physical Cells
    fun physCell256k(): IItemDefinition
    fun physCell1024k(): IItemDefinition
    fun physCell4096k(): IItemDefinition
    fun physCell16384k(): IItemDefinition
    fun physCellContainer(): IItemDefinition

    // MISC
    fun wirelessFluidTerminal(): IItemDefinition
    fun itemFluidPattern(): IItemDefinition
}