package extracells.registries

import appeng.api.config.Upgrades
import extracells.integration.Integration
import extracells.part.*
import net.minecraft.item.ItemStack
import net.minecraft.util.StatCollector
import org.apache.commons.lang3.tuple.MutablePair
import org.apache.commons.lang3.tuple.Pair
import java.util.*

enum class PartEnum @JvmOverloads constructor(_unlocalizedName: String, _partClass: Class<out PartECBase>, _groupName: String? = null, _mod: Integration.Mods? = null as Integration.Mods?) {
    FLUIDEXPORT("fluid.export", PartFluidExport::class.java, "fluid.IO", generatePair(Upgrades.CAPACITY, 2),
            generatePair(
                    Upgrades.REDSTONE, 1), generatePair(Upgrades.SPEED, 2)),
    FLUIDIMPORT("fluid.import", PartFluidImport::class.java, "fluid.IO", generatePair(
            Upgrades.CAPACITY, 2), generatePair(Upgrades.REDSTONE, 1), generatePair(Upgrades.SPEED, 2)),
    FLUIDSTORAGE("fluid.storage", PartFluidStorage::class.java, null, generatePair(
            Upgrades.INVERTER, 1)),
    FLUIDTERMINAL("fluid.terminal", PartFluidTerminal::class.java), FLUIDLEVELEMITTER("fluid.levelemitter",
            PartFluidLevelEmitter::class.java),
    FLUIDPANEANNIHILATION("fluid.plane.annihilation", PartFluidPlaneAnnihilation::class.java,
            "fluid.plane"),
    FLUIDPANEFORMATION("fluid.plane.formation", PartFluidPlaneFormation::class.java, "fluid.plane"), DRIVE("drive",
            PartDrive::class.java),
    BATTERY("battery", PartBattery::class.java), INTERFACE("interface", PartFluidInterface::class.java), FLUIDMONITOR(
            "fluid.monitor", PartFluidStorageMonitor::class.java),
    FLUIDCONVERSIONMONITOR("fluid.conversion.monitor", PartFluidConversionMonitor::class.java), OREDICTEXPORTBUS(
            "oredict.export", PartOreDictExporter::class.java);

    //	GASIMPORT("gas.import", PartGasImport.class, "gas.IO", Integration.Mods.MEKANISMGAS, generatePair(Upgrades.CAPACITY, 2), generatePair(Upgrades.REDSTONE, 1), generatePair(Upgrades.SPEED, 2)),
    //	GASEXPORT("gas.export", PartGasExport.class, "gas.IO", Integration.Mods.MEKANISMGAS, generatePair(Upgrades.CAPACITY, 2), generatePair(Upgrades.REDSTONE, 1), generatePair(Upgrades.SPEED, 2)),
    //	GASTERMINAL("gas.terminal", PartGasTerminal.class, Integration.Mods.MEKANISMGAS),
    //	GASSTORAGE("gas.storage", PartGasStorage.class, null, Integration.Mods.MEKANISMGAS, generatePair(Upgrades.INVERTER, 1)),
    //	GASLEVELEMITTER("gas.levelemitter", PartGasLevelEmitter.class, Integration.Mods.MEKANISMGAS),
    //	GASMONITOR("gas.monitor", PartGasStorageMonitor.class, Integration.Mods.MEKANISMGAS),
    //	GASCONVERSIONMONITOR("gas.conversion.monitor", PartGasConversionMonitor.class, Integration.Mods.MEKANISMGAS);
    val mod: Integration.Mods?
    val unlocalizedName: String
    val partClass: Class<out PartECBase>
    val groupName: String?
    private val upgrades: MutableMap<Upgrades, Int> = HashMap()

    constructor(_unlocalizedName: String, _partClass: Class<out PartECBase>, _mod: Integration.Mods) : this(
            _unlocalizedName, _partClass, null, _mod) {
    }

    constructor(_unlocalizedName: String, _partClass: Class<out PartECBase>, _groupName: String?, vararg _upgrades: Pair<Upgrades, Int>) : this(
            _unlocalizedName, _partClass, _groupName, null as Integration.Mods?) {
        for ((key, value) in _upgrades) {
            upgrades[key] = value
        }
    }

    constructor(_unlocalizedName: String, _partClass: Class<out PartECBase>, _groupName: String, _mod: Integration.Mods, vararg _upgrades: Pair<Upgrades, Int>) : this(
            _unlocalizedName, _partClass, _groupName, _mod) {
        for ((key, value) in _upgrades) {
            upgrades[key] = value
        }
    }

    val statName: String
        get() = StatCollector.translateToLocal(unlocalizedName + ".name")

    fun getUpgrades(): Map<Upgrades, Int> {
        return upgrades
    }

    @Throws(IllegalAccessException::class, InstantiationException::class)
    fun newInstance(partStack: ItemStack): PartECBase {
        val partECBase = partClass.newInstance()
        partECBase.initializePart(partStack)
        return partECBase
    }

    companion object {
        private fun generatePair(_upgrade: Upgrades, integer: Int): Pair<Upgrades, Int> {
            return MutablePair(_upgrade, integer)
        }

        fun getPartID(partClass: Class<out PartECBase?>): Int {
            for (i in values().indices) {
                if (values()[i].partClass == partClass) return i
            }
            return -1
        }

        fun getPartID(partECBase: PartECBase): Int {
            return getPartID(partECBase.javaClass)
        }
    }

    init {
        unlocalizedName = "extracells.part.$_unlocalizedName"
        partClass = _partClass
        groupName = if (_groupName == null || _groupName.isEmpty()) null else "extracells.$_groupName"
        mod = _mod
    }
}