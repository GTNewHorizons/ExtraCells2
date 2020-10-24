package extracells.definitions

import extracells.api.definitions.IItemDefinition
import extracells.registries.ItemEnum

class ItemDefinition : IItemDefinition {
    override fun cell1024kPart(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.item, 1)
    }

    override fun cell1024kPartFluid(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.item, 9)
    }

    override fun cell16384kPart(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.item, 3)
    }

    override fun cell16kPartFluid(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.item, 6)
    }

    // Fluid Storage Components
    override fun cell1kPartFluid(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.item, 4)
    }

    // Physical Storage Components
    override fun cell256kPart(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.item)
    }

    override fun cell256kPartFluid(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.item, 8)
    }

    override fun cell4096kPart(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.item, 2)
    }

    override fun cell4096kPartFluid(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.item, 10)
    }

    override fun cell4kPartFluid(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.item, 5)
    }

    override fun cell64kPartFluid(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.item, 7)
    }

    // Fluid Storage
    override fun fluidCasing(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.STORAGECASING.item, 1)
    }

    override fun fluidCell1024k(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.item, 5)
    }

    override fun fluidCell16k(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.item, 2)
    }

    // Fluid Cells
    override fun fluidCell1k(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.item)
    }

    override fun fluidCell256k(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.item, 4)
    }

    override fun fluidCell4096k(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.item, 6)
    }

    override fun fluidCell4k(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.item, 1)
    }

    override fun fluidCell64k(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.item, 3)
    }

    override fun fluidCellPortable(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.FLUIDSTORAGEPORTABLE.item)
    }

    // Physical Storage Casing
    override fun physCasing(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.STORAGECASING.item)
    }

    override fun physCell1024k(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.item, 1)
    }

    override fun physCell16384k(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.item, 3)
    }

    // Physical Cells
    override fun physCell256k(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.item)
    }

    override fun physCell4096k(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.item, 2)
    }

    override fun physCellContainer(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.item, 4)
    }

    override fun wirelessFluidTerminal(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.FLUIDWIRELESSTERMINAL.item)
    }

    override fun itemFluidPattern(): appeng.api.definitions.IItemDefinition {
        return ItemItemDefinitions(ItemEnum.FLUIDPATTERN.item)
    }

    companion object {
        val instance = ItemDefinition()
    }
}