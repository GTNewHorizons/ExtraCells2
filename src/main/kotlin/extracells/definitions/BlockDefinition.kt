package extracells.definitions

import appeng.api.definitions.ITileDefinition
import extracells.api.definitions.IBlockDefinition
import extracells.registries.BlockEnum
import extracells.tileentity.*
open class BlockDefinition : IBlockDefinition {
    override fun blockInterface(): ITileDefinition {
        return BlockItemDefinitions(BlockEnum.ECBASEBLOCK.block,
                TileEntityFluidInterface::class.java)
    }

    override fun certusTank(): ITileDefinition {
        return BlockItemDefinitions(BlockEnum.CERTUSTANK.block,
                TileEntityCertusTank::class.java)
    }

    override fun fluidCrafter(): ITileDefinition {
        return BlockItemDefinitions(BlockEnum.FLUIDCRAFTER.block,
                TileEntityFluidCrafter::class.java)
    }

    override fun fluidFiller(): ITileDefinition {
        return BlockItemDefinitions(BlockEnum.FLUIDCRAFTER.block, 1,
                TileEntityFluidFiller::class.java)
    }

    override fun walrus(): ITileDefinition {
        return BlockItemDefinitions(BlockEnum.WALRUS.block,
                TileEntityWalrus::class.java)
    }

    override fun craftingStorage256k(): ITileDefinition {
        return BlockItemDefinitions(BlockEnum.CRAFTINGSTORAGE.block, 0,
                TileEntityCraftingStorage::class.java)
    }

    override fun craftingStorage1024k(): ITileDefinition {
        return BlockItemDefinitions(BlockEnum.CRAFTINGSTORAGE.block, 1,
                TileEntityCraftingStorage::class.java)
    }

    override fun craftingStorage4096k(): ITileDefinition {
        return BlockItemDefinitions(BlockEnum.CRAFTINGSTORAGE.block, 2,
                TileEntityCraftingStorage::class.java)
    }

    override fun craftingStorage16384k(): ITileDefinition {
        return BlockItemDefinitions(BlockEnum.CRAFTINGSTORAGE.block, 3,
                TileEntityCraftingStorage::class.java)
    }

    companion object {
        val instance = BlockDefinition()
    }
}