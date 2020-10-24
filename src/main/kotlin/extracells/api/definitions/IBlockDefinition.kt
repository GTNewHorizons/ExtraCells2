package extracells.api.definitions

import appeng.api.definitions.ITileDefinition

interface IBlockDefinition {
    fun blockInterface(): ITileDefinition
    fun certusTank(): ITileDefinition
    fun fluidCrafter(): ITileDefinition
    fun fluidFiller(): ITileDefinition
    fun walrus(): ITileDefinition
    fun craftingStorage256k(): ITileDefinition
    fun craftingStorage1024k(): ITileDefinition
    fun craftingStorage4096k(): ITileDefinition
    fun craftingStorage16384k(): ITileDefinition
}