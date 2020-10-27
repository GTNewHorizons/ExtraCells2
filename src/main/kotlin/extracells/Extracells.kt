package extracells

import appeng.api.AEApi
import cpw.mods.fml.client.registry.RenderingRegistry
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPostInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.network.NetworkRegistry
import extracells.integration.Integration
import extracells.network.ChannelHandler
import extracells.network.GuiHandler
import extracells.proxy.CommonProxy
import extracells.registries.ItemEnum
import extracells.render.RenderHandler
import extracells.util.ExtraCellsEventHandler
import extracells.util.FluidCellHandler
import extracells.util.NameHandler
import extracells.wireless.AEWirelessTermHandler
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import java.io.File

@Mod(
        modid = "extracells",
        name = "Extra Cells",
        modLanguage = "kotlin",
        dependencies = "required-after:forgelin;after:LogisticsPipes|Main;after:Waila;required-after:appliedenergistics2",
        modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter"
)
object Extracells {
    lateinit var proxy: CommonProxy
    var VERSION: String = ""
    var bcBurnTimeMultiplicator: Int = 4
    lateinit var configFolder: File
    var shortenedBuckets: Boolean = true
    var dynamicTypes: Boolean = true
    val integration: Integration = Integration()

    val ModTab : CreativeTabs = object : CreativeTabs("Extra_Cells") {
        override fun getIconItemStack() = ItemStack(ItemEnum.FLUIDSTORAGE.item)
        override fun getTabIconItem() = ItemEnum.FLUIDSTORAGE.item
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        AEApi.instance().registries().recipes().addNewSubItemResolver(NameHandler())
        AEApi.instance().registries().wireless().registerWirelessHandler(AEWirelessTermHandler())
        AEApi.instance().registries().cell().addCellHandler(FluidCellHandler())
        val handler = ExtraCellsEventHandler()
        FMLCommonHandler.instance().bus().register(handler)
        MinecraftForge.EVENT_BUS.register(handler)
        proxy.registerMovables()
        proxy.registerRenderers()
        proxy.registerTileEntities()
        proxy.registerFluidBurnTimes()
        proxy.addRecipes(configFolder)
        ChannelHandler.registerMessages()
        RenderingRegistry.registerBlockHandler(RenderHandler(RenderingRegistry.getNextAvailableRenderId()))
        integration.init()
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        integration.postInit()
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        VERSION = Loader.instance().activeModContainer().version
        configFolder = event.modConfigurationDirectory
        NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandler)
        val config = Configuration(File(configFolder.path + File.separator + "AppliedEnergistics2" + File.separator + "extracells.cfg"))
        config.load()
        shortenedBuckets = config.get("Tooltips", "shortenedBuckets", true, "Shall the guis shorten large mB values?").getBoolean(true)
        dynamicTypes = config.get("Storage Cells", "dynamicTypes", true, "Should the mount of bytes needed for a new type depend on the cellsize?").getBoolean(true)
        integration.loadConfig(config)
        config.save()
        proxy.registerItems()
        proxy.registerBlocks()
        integration.preInit()
    }
}