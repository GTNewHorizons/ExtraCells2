//package extracells.integration.igw
//
//import cpw.mods.fml.client.FMLClientHandler
//import cpw.mods.fml.common.FMLCommonHandler
//import cpw.mods.fml.common.eventhandler.EventBus
//import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent
//import extracells.integration.igw.IGWSupportNotifier.ThreadDownloadIGW
//import net.minecraft.client.Minecraft
//import net.minecraft.client.entity.EntityClientPlayerMP
//import net.minecraft.command.CommandBase
//import net.minecraft.command.ICommandSender
//import net.minecraft.entity.player.EntityPlayer
//import net.minecraft.util.ChatComponentText
//import net.minecraft.util.EnumChatFormatting
//import net.minecraft.util.IChatComponent
//import net.minecraft.util.IChatComponent.Serializer
//import net.minecraft.world.World
//import org.apache.commons.io.FileUtils
//import java.awt.Desktop
//import java.io.File
//import java.net.URL
//import java.net.URLConnection
//
//object IGWSupportNotifier {
//    internal val LATEST_DL_URL: String = "http://minecraft.curseforge.com/mc-mods/223815-in-game-wiki-mod/files/latest"
//    internal var supportingMod: String = null
//    fun onPlayerJoin(event: PlayerTickEvent): Unit {
//        if (event.player.worldObj.isRemote && event.player == FMLClientHandler.instance().getClientPlayerEntity) {
//            event.player.addChatComponentMessage(IChatComponent.Serializer.func_150699_a("[\"" + EnumChatFormatting.GOLD + "The mod " + supportingMod + " is supporting In-Game Wiki mod. " + EnumChatFormatting.GOLD + "However, In-Game Wiki isn't installed! " + "[\"," + "{\"text\":\"Download Latest\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/igwmod_download\"}}," + "\"]\"]"))
//            FMLCommonHandler.instance().bus().unregister(this)
//        }
//    }
//
//    private open class CommandDownloadIGW : CommandBase() {
//        override fun getRequiredPermissionLevel(): Int {
//            return 100.-()
//        }
//
//        override fun getCommandName(): String {
//            return "igwmod_download"
//        }
//
//        override fun getCommandUsage(p_71518_1_: ICommandSender): String {
//            return commandName
//        }
//
//        override fun processCommand(p_71515_1_: ICommandSender, p_71515_2_: scala.Array<String>): Unit {
//            ThreadDownloadIGW()
//        }
//    }
//
//    private open class ThreadDownloadIGW : Thread() {
//        override fun run(): Unit {
//            try {
//                if (Minecraft.getMinecraft().thePlayer != null) Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("Downloading IGW-Mod..."))
//                val url: URL = URL(IGWSupportNotifier.LATEST_DL_URL)
//                val connection: URLConnection = url.openConnection()
//                connection.connect()
//                val fileName: String = "IGW-Mod.jar"
//                val dir: File = File(".", "mods")
//                val f: File = File(dir, fileName)
//                FileUtils.copyURLToFile(url, f)
//                if (Minecraft.getMinecraft().thePlayer != null) Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.GREEN.+("Successfully downloaded. Restart Minecraft to apply.")))
//                Desktop.getDesktop().open(dir)
//                finalize()
//            } catch (e: Throwable) {
//                e.printStackTrace()
//                if (Minecraft.getMinecraft().thePlayer != null) Minecraft.getMinecraft().thePlayer.addChatComponentMessage(ChatComponentText(EnumChatFormatting.RED.+("Failed to download")))
//                try {
//                    finalize()
//                } catch (e1: Throwable) {
//                    e1.printStackTrace()
//                }
//            }
//        }
//    }
//}