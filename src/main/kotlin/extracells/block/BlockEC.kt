package extracells.block

import net.minecraft.block.BlockContainer
import net.minecraft.block.material.Material

abstract class BlockEC : BlockContainer {
    protected constructor(material: Material?, hardness: Float, resistance: Float) : super(material) {
        setHardness(hardness)
        setResistance(resistance)
    }

    protected constructor(material: Material?) : super(material) {}
}