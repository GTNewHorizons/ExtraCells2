package extracells.integration

import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.ModAPIManager
import cpw.mods.fml.relauncher.Side
import extracells.Extracells.proxy
import extracells.integration.nei.Nei
import extracells.integration.opencomputers.OpenComputers
import extracells.integration.waila.Waila
import net.minecraftforge.common.config.Configuration
open class Integration {
    enum class Mods constructor(val modID: String, val modName: String = modID, private val side: Side? = null) {
        WAILA("Waila"), OPENCOMPUTERS("OpenComputers"), BCFUEL("BuildCraftAPI|fuels", "BuildCraftFuel"), NEI(
                "NotEnoughItems", Side.CLIENT),
        MEKANISMGAS("MekanismAPI|gas", "MekanismGas"), IGW("IGWMod", "IngameWikiMod",
                Side.CLIENT),
        THAUMATICENERGISTICS("thaumicenergistics", "Thaumatic Energistics"), MEKANISM("Mekanism"), WIRELESSCRAFTING(
                "ae2wct", "AE2 Wireless Crafting Terminal");

        private var shouldLoad = true

        constructor(modid: String, side: Side) : this(modid, modid, side)

        val isOnClient: Boolean
            get() = side != Side.SERVER
        val isOnServer: Boolean
            get() = side != Side.CLIENT

        fun loadConfig(config: Configuration) {
            shouldLoad = config["Integration", "enable$modName", true, "Enable $modName Integration."].getBoolean(true)
        }

        val isEnabled: Boolean
            get() = Loader.isModLoaded(modID) && shouldLoad && correctSide() || ModAPIManager.INSTANCE.hasAPI(
                    modID) && shouldLoad && correctSide()

        private fun correctSide(): Boolean {
            return if (proxy.isClient) isOnClient else isOnServer
        }
    }

    fun loadConfig(config: Configuration) {
        for (mod in Mods.values()) {
            mod.loadConfig(config)
        }
    }

    fun preInit() {
//		if (Mods.IGW.correctSide() && Mods.IGW.shouldLoad)
//			IGW.initNotifier();
    }

    fun init() {
        if (Mods.WAILA.isEnabled) Waila.init()
        if (Mods.OPENCOMPUTERS.isEnabled) OpenComputers.init()
        if (Mods.NEI.isEnabled) Nei.init()
        //		if (Mods.MEKANISMGAS.isEnabled())
//			MekanismGas.init();
//		if (Mods.IGW.isEnabled())
//			IGW.init();
//		if(Mods.MEKANISM.isEnabled())
//			Mekanism.init();
    }

    fun postInit() {
//		if (Mods.MEKANISMGAS.isEnabled())
//			MekanismGas.postInit();
    }
}