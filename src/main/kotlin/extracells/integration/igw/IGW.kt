//package extracells.integration.igw
//
//import cpw.mods.fml.common.registry.GameRegistry
//import extracells.Extracells
//import extracells.integration.Integration
//import extracells.integration.Integration.Mods
//import extracells.registries.BlockEnum
//import extracells.registries.ItemEnum
//import igwmod.api.WikiRegistry
//import net.minecraft.block.Block
//import net.minecraft.creativetab.CreativeTabs
//import net.minecraft.item.Item
//import net.minecraft.item.ItemStack
//import java.lang.CharSequence
//import java.util.List
//
//object IGW {
//    fun initNotifier(): Unit {
//        IGWSupportNotifier
//    }
//
//    fun init(): Unit {
//        for (ItemEnum in ItemEnum.values()) {
//            if (item != ItemEnum.CRAFTINGPATTERN && item != ItemEnum.FLUIDITEM) {
//                if (item == ItemEnum.FLUIDPATTERN) {
//                    WikiRegistry.registerBlockAndItemPageEntry(item.getSizedStack(1), item.getSizedStack(1).getUnlocalizedName().replace(".", "/"))
//                } else if (item == ItemEnum.STORAGECOMPONENT || item == ItemEnum.STORAGECASING) {
//                    val list: ArrayList<Object> = ArrayList<Object>()
//                    item.getItem().getSubItems(item.getItem(), Extracells.ModTab, list)
//                    for (Object in asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(list))))))))))) {
//                        val stack: ItemStack = sub as ItemStack
//                        WikiRegistry.registerBlockAndItemPageEntry(stack, "extracells/item/crafting")
//                    }
//                } else {
//                    val list: ArrayList<Object> = ArrayList<Object>()
//                    item.getItem().getSubItems(item.getItem(), Extracells.ModTab, list)
//                    for (Object in asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(list))))))))))) {
//                        val stack: ItemStack = sub as ItemStack
//                        WikiRegistry.registerBlockAndItemPageEntry(stack, stack.unlocalizedName.replace(".", "/"))
//                    }
//                }
//            }
//        }
//        if (Integration.Mods.OPENCOMPUTERS.isEnabled) {
//            val stack: ItemStack = GameRegistry.findItemStack("extracells", "oc.upgrade", 1)
//            WikiRegistry.registerBlockAndItemPageEntry(stack.item, stack.unlocalizedName.replace(".", "/"))
//        }
//        for (BlockEnum in BlockEnum.values()) {
//            val list: ArrayList<Object> = ArrayList<Object>()
//            Item.getItemFromBlock(block.getBlock()).getSubItems(Item.getItemFromBlock(block.getBlock()), Extracells.ModTab, list)
//            for (Object in asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(asScalaBuffer(list))))))))))) {
//                val stack: ItemStack = sub as ItemStack
//                WikiRegistry.registerBlockAndItemPageEntry(stack, stack.unlocalizedName.replace(".", "/").replace("tile/", ""))
//            }
//        }
//    }
//}