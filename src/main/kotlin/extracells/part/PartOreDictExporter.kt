package extracells.part

import appeng.api.config.Actionable
import appeng.api.networking.IGridNode
import appeng.api.networking.events.MENetworkChannelsChanged
import appeng.api.networking.events.MENetworkEventSubscribe
import appeng.api.networking.events.MENetworkPowerStatusChange
import appeng.api.networking.security.MachineSource
import appeng.api.networking.storage.IStorageGrid
import appeng.api.networking.ticking.IGridTickable
import appeng.api.networking.ticking.TickRateModulation
import appeng.api.networking.ticking.TickingRequest
import appeng.api.parts.IPartCollisionHelper
import appeng.api.parts.IPartRenderHelper
import appeng.api.storage.data.IAEItemStack
import appeng.api.util.AEColor
import appeng.util.item.AEItemStack
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.container.ContainerOreDictExport
import extracells.gui.GuiOreDictExport
import extracells.render.TextureManager
import extracells.util.ItemUtils
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.inventory.ISidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.StatCollector
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.oredict.OreDictionary
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.stream.IntStream
open class PartOreDictExporter : PartECBase(), IGridTickable {
    private var _filter : String = ""
    internal var filter : String
    get() = _filter
    set(value) {
        _filter = value
        updateFilter()
        saveData()
    }
    // disabled
    // private Predicate<ItemStack> filterPredicate = null;
    /**
     * White list of itemstacks to extract. OreDict only mode.
     */
    private var oreDictFilteredItems = arrayOfNulls<ItemStack>(0)
    override fun cableConnectionRenderTo(): Int {
        return 5
    }

    /**
     * Call when the filter string has changed to parse and recompile the filter.
     */
    private fun updateFilter() {
        if (filter.trim { it <= ' ' }.isNotEmpty()) {
            //ArrayList<String> matchingNames = new ArrayList<>();
            var matcher: Predicate<ItemStack?>? = null
            val filters = filter.split("[&|]".toRegex()).toTypedArray()
            var lastFilter: String? = null
            for (filter1 in filters) {
                var filter = filter1.trim { it <= ' ' }
                val negated = filter.startsWith("!")
                if (negated)
                    filter = filter.substring(1)
                var test = filterToItemStackPredicate(filter)
                if (negated) test = test.negate()
                if (matcher == null) {
                    matcher = test
                    lastFilter = filter
                } else {
                    val endLast = this.filter.indexOf(lastFilter!!) + lastFilter.length
                    val startThis = this.filter.indexOf(filter)
                    val or = this.filter.substring(endLast, startThis).contains("|")
                    matcher = if (or) {
                        matcher.or(test)
                    } else {
                        matcher.and(test)
                    }
                }
            }
            if (matcher == null) {
                //filterPredicate = null;
                oreDictFilteredItems = arrayOfNulls(0)
                return
            }

            //Mod name and path evaluation can only be done during tick, can't precompile whitelist for this.
            if (!filter.contains("@") && !filter.contains("~")) {
                //Precompiled whitelist of oredict itemstacks.
                val filtered = ArrayList<ItemStack>()
                for (name in OreDictionary.getOreNames()) for (s in OreDictionary.getOres(name)) if (matcher.test(s)) {
                    filtered.add(s)
                }
                oreDictFilteredItems = filtered.toArray(oreDictFilteredItems)
            } else {
                // mod filtering disabled
                //filterPredicate = matcher;
                oreDictFilteredItems = arrayOfNulls(0)
            }
        } else {
            //this.filterPredicate = null;
            oreDictFilteredItems = arrayOfNulls(0)
        }
    }

    /**
     * Given a filter string, returns a predicate that matches a given ItemStack
     *
     * @param filter Filter string.
     * @return Predicate for filter string.
     */
    private fun filterToItemStackPredicate(filter: String): Predicate<ItemStack?> {
        /*if (filter.startsWith("@")) {
			final Predicate<String> test = filterToPredicate(filter.substring(1));
			return (is) -> is != null &&
					Optional.ofNullable(is.getItem(). getRegistryName())
							.map(ResourceLocation::getResourceDomain)
							.map(test::test)
							.orElse(false);
		} else if (filter.startsWith("~")) {
			final Predicate<String> test = filterToPredicate(filter.substring(1));
			return (is) -> is != null &&
					Optional.ofNullable(is.getItem().getRegistryName())
							.map(ResourceLocation::getPath)
							.map(test::test)
							.orElse(false);
		} else {*/
        val test = filterToPredicate(filter)
        return Predicate { `is`: ItemStack? ->
            `is` != null &&
                    IntStream.of(*OreDictionary.getOreIDs(`is`))
                            .mapToObj { id: Int -> OreDictionary.getOreName(id) }
                            .anyMatch(test)
        }
        //}
    }

    /**
     * Given a filter string, returns a Predicate that matches a string.
     *
     * @param filter Filter string
     * @return Predicate for filter string.
     */
    private fun filterToPredicate(filter: String): Predicate<String> {
        val numStars = StringUtils.countMatches(filter, "*")
        return if (numStars == filter.length) {
            Predicate { str: String? -> true }
        } else if (filter.length > 2 && filter.startsWith("*") && filter.endsWith("*") && numStars == 2) {
            val pattern = filter.substring(1, filter.length - 1)
            Predicate { str: String -> str.contains(pattern) }
        } else if (filter.length >= 2 && filter.startsWith("*") && numStars == 1) {
            val pattern = filter.substring(1)
            Predicate { str: String -> str.endsWith(pattern) }
        } else if (filter.length >= 2 && filter.endsWith("*") && numStars == 1) {
            val pattern = filter.substring(0, filter.length - 1)
            Predicate { str: String -> str.startsWith(pattern) }
        } else if (numStars == 0) {
            Predicate { str: String -> str == filter }
        } else {
            val filterRegexFragment = filter.replace("*", ".*")
            val regexPattern = "^$filterRegexFragment$"
            val pattern = Pattern.compile(regexPattern)
            pattern.asPredicate()
        }
    }

    fun doWork(rate: Int, ticksSinceLastCall: Int): Boolean {
        val amount = Math.min(rate * ticksSinceLastCall, 64)
        val storage = storageGrid!!
        val inv = storage.itemInventory
        val src = MachineSource(this)

/*		if (this.filterPredicate != null) {
			//Tick-time filter evaluation.
			IItemList<IAEItemStack> items = inv.getStorageList();
			for (IAEItemStack stack : items) {
				if (stack == null || !this.filterPredicate.test(stack.createItemStack()))
					continue;

				IAEItemStack toExtract = stack.copy();
				toExtract.setStackSize(amount);

				IAEItemStack extracted = inv.extractItems(toExtract, Actionable.SIMULATE, src);
				if (extracted != null) {
					IAEItemStack exported = exportStack(extracted.copy());
					if (exported != null) {
						inv.extractItems(exported, Actionable.MODULATE, src);
						return true;
					}
				}
			}
			return false;
		} else {*/
        //Precompiled oredict whitelist
        for (`is` in oreDictFilteredItems) {
            if (`is` == null || amount == 0) continue
            val toExtract = `is`.copy()
            toExtract.stackSize = amount
            val extracted = inv.extractItems(AEItemStack.create(toExtract), Actionable.SIMULATE, src)
            if (extracted != null) {
                val exported = exportStack(extracted.copy())
                if (exported != null) {
                    inv.extractItems(exported, Actionable.MODULATE, src)
                    return true
                }
            }
        }
        return false
        //}
    }

    fun exportStack(stack0: IAEItemStack?): IAEItemStack? {
        if (tile == null || !tile!!.hasWorldObj() || stack0 == null) return null
        val dir = side
        val tile = tile!!.worldObj.getTileEntity(
                tile!!.xCoord + dir!!.offsetX, tile!!.yCoord + dir.offsetY,
                tile!!.zCoord + dir.offsetZ)
                ?: return null
        val stack = stack0.copy()
        if (tile is IInventory) {
            if (tile is ISidedInventory) {
                val inv = tile as ISidedInventory
                for (i in inv.getAccessibleSlotsFromSide(dir.opposite
                        .ordinal)) {
                    if (inv.canInsertItem(i, stack.itemStack, dir
                                    .opposite.ordinal)) {
                        if (inv.getStackInSlot(i) == null) {
                            inv.setInventorySlotContents(i,
                                    stack.itemStack)
                            return stack0
                        } else if (ItemUtils.areItemEqualsIgnoreStackSize(
                                        inv.getStackInSlot(i), stack.itemStack)) {
                            val max = inv.inventoryStackLimit
                            val current = inv.getStackInSlot(i).stackSize
                            val outStack = stack.stackSize.toInt()
                            if (max == current) continue
                            val s = inv.getStackInSlot(i).copy()
                            return if (current + outStack <= max) {
                                s.stackSize = s.stackSize + outStack
                                inv.setInventorySlotContents(i, s)
                                stack0
                            } else {
                                s.stackSize = max
                                inv.setInventorySlotContents(i, s)
                                stack.stackSize = max - current.toLong()
                                stack
                            }
                        }
                    }
                }
            } else {
                val inv = tile as IInventory
                for (i in 0 until inv.sizeInventory) {
                    if (inv.isItemValidForSlot(i, stack.itemStack)) {
                        if (inv.getStackInSlot(i) == null) {
                            inv.setInventorySlotContents(i,
                                    stack.itemStack)
                            return stack0
                        } else if (ItemUtils.areItemEqualsIgnoreStackSize(
                                        inv.getStackInSlot(i), stack.itemStack)) {
                            val max = inv.inventoryStackLimit
                            val current = inv.getStackInSlot(i).stackSize
                            val outStack = stack.stackSize.toInt()
                            if (max == current) continue
                            val s = inv.getStackInSlot(i).copy()
                            return if (current + outStack <= max) {
                                s.stackSize = s.stackSize + outStack
                                inv.setInventorySlotContents(i, s)
                                stack0
                            } else {
                                s.stackSize = max
                                inv.setInventorySlotContents(i, s)
                                stack.stackSize = max - current.toLong()
                                stack
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    override fun getBoxes(bch: IPartCollisionHelper) {
        bch.addBox(6.0, 6.0, 12.0, 10.0, 10.0, 13.0)
        bch.addBox(4.0, 4.0, 13.0, 12.0, 12.0, 14.0)
        bch.addBox(5.0, 5.0, 14.0, 11.0, 11.0, 15.0)
        bch.addBox(6.0, 6.0, 15.0, 10.0, 10.0, 16.0)
        bch.addBox(6.0, 6.0, 11.0, 10.0, 10.0, 12.0)
    }

    override fun getClientGuiElement(player: EntityPlayer): Any? {
        return GuiOreDictExport(player, this)
    }

    override val powerUsage: Double
        get() = 10.0

    override fun getServerGuiElement(player: EntityPlayer?): Any? {
        return player?.let { ContainerOreDictExport(it, this) }
    }

    private val storageGrid: IStorageGrid?
        private get() {
            val node = gridNode ?: return null
            val grid = node.grid ?: return null
            return grid.getCache(IStorageGrid::class.java)
        }

    override fun getTickingRequest(node: IGridNode): TickingRequest {
        return TickingRequest(1, 20, false, false)
    }

    override fun getWailaBodey(data: NBTTagCompound, list: MutableList<String>): List<String> {
        super.getWailaBodey(data, list)
        if (data.hasKey("name")) list.add(StatCollector
                .translateToLocal("extracells.tooltip.oredict")
                + ": "
                + data.getString("name")) else list.add(StatCollector
                .translateToLocal("extracells.tooltip.oredict") + ":")
        return list
    }

    override fun getWailaTag(tag: NBTTagCompound): NBTTagCompound {
        super.getWailaTag(tag)
        tag.setString("name", filter)
        return tag
    }

    @MENetworkEventSubscribe
    fun powerChange(event: MENetworkPowerStatusChange?) {
        val node = gridNode
        if (node != null) {
            val isNowActive = node.isActive
            if (isNowActive != isActive) {
                isActive = isNowActive
                onNeighborChanged()
                host?.markForUpdate()
            }
        }
    }

    override fun readFromNBT(data: NBTTagCompound) {
        super.readFromNBT(data)
        if (data.hasKey("filter")) filter = data.getString("filter")
        updateFilter()
    }

    @SideOnly(Side.CLIENT)
    override fun renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks) {
        val ts = Tessellator.instance
        rh.setTexture(TextureManager.EXPORT_SIDE.texture)
        rh.setBounds(6f, 6f, 12f, 10f, 10f, 13f)
        rh.renderInventoryBox(renderer)
        rh.setBounds(4f, 4f, 13f, 12f, 12f, 14f)
        rh.renderInventoryBox(renderer)
        rh.setBounds(5f, 5f, 14f, 11f, 11f, 15f)
        rh.renderInventoryBox(renderer)
        val side = TextureManager.EXPORT_SIDE.texture
        rh.setTexture(side, side, side,
                TextureManager.EXPORT_FRONT.texture, side, side)
        rh.setBounds(6f, 6f, 15f, 10f, 10f, 16f)
        rh.renderInventoryBox(renderer)
        rh.setInvColor(AEColor.Black.mediumVariant)
        ts.setBrightness(15 shl 20 or 15 shl 4)
        rh.renderInventoryFace(TextureManager.EXPORT_FRONT.textures[1],
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(6f, 6f, 11f, 10f, 10f, 12f)
        renderInventoryBusLights(rh, renderer)
    }

    @SideOnly(Side.CLIENT)
    override fun renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper,
                              renderer: RenderBlocks) {
        val ts = Tessellator.instance
        rh.setTexture(TextureManager.EXPORT_SIDE.texture)
        rh.setBounds(6f, 6f, 12f, 10f, 10f, 13f)
        rh.renderBlock(x, y, z, renderer)
        rh.setBounds(4f, 4f, 13f, 12f, 12f, 14f)
        rh.renderBlock(x, y, z, renderer)
        rh.setBounds(5f, 5f, 14f, 11f, 11f, 15f)
        rh.renderBlock(x, y, z, renderer)
        val side = TextureManager.EXPORT_SIDE.texture
        rh.setTexture(side, side, side,
                TextureManager.EXPORT_FRONT.textures[0], side, side)
        rh.setBounds(6f, 6f, 15f, 10f, 10f, 16f)
        rh.renderBlock(x, y, z, renderer)
        ts.setColorOpaque_I(AEColor.Black.mediumVariant)
        if (isActive) ts.setBrightness(15 shl 20 or 15 shl 4)
        rh.renderFace(x, y, z, TextureManager.EXPORT_FRONT.textures[1],
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(6f, 6f, 11f, 10f, 10f, 12f)
        renderStaticBusLights(x, y, z, rh, renderer)
    }

    override fun tickingRequest(node: IGridNode,
                                TicksSinceLastCall: Int): TickRateModulation {
        return if (isActive) if (doWork(10,
                        TicksSinceLastCall)) TickRateModulation.FASTER else TickRateModulation.SLOWER else TickRateModulation.SLOWER
    }

    @MENetworkEventSubscribe
    fun updateChannels(channel: MENetworkChannelsChanged?) {
        val node = gridNode
        if (node != null) {
            val isNowActive = node.isActive
            if (isNowActive != isActive) {
                isActive = isNowActive
                onNeighborChanged()
                host?.markForUpdate()
            }
        }
    }

    override fun writeToNBT(data: NBTTagCompound) {
        super.writeToNBT(data)
        data.setString("filter", filter)
    }
}