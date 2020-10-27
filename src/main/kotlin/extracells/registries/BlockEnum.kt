package extracells.registries

import extracells.Extracells.ModTab
import extracells.block.*
import extracells.integration.Integration
import extracells.item.ItemBlockCertusTank
import extracells.item.ItemBlockECBase
import extracells.item.ItemCraftingStorage
import net.minecraft.block.Block
import net.minecraft.item.ItemBlock
import net.minecraft.util.StatCollector

enum class BlockEnum constructor(val internalName: String, val block: Block, _itemBlockClass: Class<out ItemBlock> = ItemBlock::class.java, _mod: Integration.Mods? = null) {
    CERTUSTANK("certustank", BlockCertusTank(), ItemBlockCertusTank::class.java), WALRUS("walrus",
            BlockWalrus()),
    FLUIDCRAFTER("fluidcrafter", BlockFluidCrafter()), ECBASEBLOCK("ecbaseblock", ECBaseBlock(),
            ItemBlockECBase::class.java),
    BLASTRESISTANTMEDRIVE("hardmedrive", BlockHardMEDrive), VIBRANTCHAMBERFLUID("vibrantchamberfluid",
            BlockVibrationChamberFluid()),
    CRAFTINGSTORAGE("craftingstorage", BlockCraftingStorage(), ItemCraftingStorage::class.java);

    val itemBlockClass: Class<out ItemBlock>
    val mod: Integration.Mods?

    constructor(_internalName: String, _block: Block, _mod: Integration.Mods) : this(_internalName, _block,
            ItemBlock::class.java, _mod)

    val statName: String
        get() = StatCollector.translateToLocal(block.unlocalizedName + ".name")

    init {
        block.setBlockName(String.format("extracells.block.%s", internalName))
        itemBlockClass = _itemBlockClass
        mod = _mod
        if (_mod == null || _mod.isEnabled) block.setCreativeTab(ModTab)
    }
}