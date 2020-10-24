package extracells.part

import appeng.api.networking.security.BaseActionSource
import appeng.api.networking.storage.IStackWatcher
import appeng.api.networking.storage.IStackWatcherHost
import appeng.api.networking.storage.IStorageGrid
import appeng.api.parts.IPartCollisionHelper
import appeng.api.parts.IPartRenderHelper
import appeng.api.storage.IMEMonitor
import appeng.api.storage.StorageChannel
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.data.IAEStack
import appeng.api.storage.data.IItemList
import appeng.api.util.AEColor
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.render.TextureManager
import extracells.util.FluidUtil
import extracells.util.WrenchUtil
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ChatComponentTranslation
import net.minecraft.util.StatCollector
import net.minecraft.util.Vec3
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidContainerRegistry
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import java.io.IOException

open class PartFluidStorageMonitor : PartECBase(), IStackWatcherHost {
    var fluid: Fluid? = null
    var amount = 0L
    private var dspList: Any? = null
    var locked = false
    var watcher: IStackWatcher? = null
    override fun cableConnectionRenderTo(): Int {
        return 1
    }

    protected fun dropItems(world: World?, x: Int, y: Int, z: Int, stack: ItemStack?) {
        if (world == null) return
        if (!world.isRemote) {
            val f = 0.7f
            val d0 = world.rand.nextFloat() * f + (1.0f - f) * 0.5
            val d1 = world.rand.nextFloat() * f + (1.0f - f) * 0.5
            val d2 = world.rand.nextFloat() * f + (1.0f - f) * 0.5
            val entityitem = EntityItem(world, x + d0, y + d1, z + d2, stack)
            entityitem.delayBeforeCanPickup = 10
            world.spawnEntityInWorld(entityitem)
        }
    }

    override fun getBoxes(bch: IPartCollisionHelper) {
        bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0)
        bch.addBox(4.0, 4.0, 13.0, 12.0, 12.0, 14.0)
        bch.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 13.0)
    }

    protected val fluidStorage: IMEMonitor<IAEFluidStack>?
        protected get() {
            val n = gridNode ?: return null
            val g = n.grid ?: return null
            val storage = g.getCache<IStorageGrid>(IStorageGrid::class.java) ?: return null
            return storage.fluidInventory
        }
    override val powerUsage: Double
        get() = 1.0

    override fun getLightLevel(): Int {
        return if (this.isPowered) 9 else 0
    }

    override fun getWailaBodey(data: NBTTagCompound, list: MutableList<String>): List<String> {
        super.getWailaBodey(data, list)
        var amount = 0L
        var fluid: Fluid? = null
        if (data.hasKey("locked") && data.getBoolean("locked")) list.add(StatCollector
                .translateToLocal("waila.appliedenergistics2.Locked")) else list.add(StatCollector
                .translateToLocal("waila.appliedenergistics2.Unlocked"))
        if (data.hasKey("amount")) amount = data.getLong("amount")
        if (data.hasKey("fluid")) {
            val id = data.getInteger("fluid")
            if (id != -1) fluid = FluidRegistry.getFluid(id)
        }
        if (fluid != null) {
            list.add(StatCollector.translateToLocal("extracells.tooltip.fluid")
                    + ": "
                    + fluid.getLocalizedName(FluidStack(fluid,
                    FluidContainerRegistry.BUCKET_VOLUME)))
            if (isActive) list.add(StatCollector
                    .translateToLocal("extracells.tooltip.amount")
                    + ": "
                    + amount + "mB") else list.add(StatCollector
                    .translateToLocal("extracells.tooltip.amount")
                    + ": 0mB")
        } else {
            list.add(StatCollector.translateToLocal("extracells.tooltip.fluid")
                    + ": "
                    + StatCollector
                    .translateToLocal("extracells.tooltip.empty1"))
            list.add(StatCollector
                    .translateToLocal("extracells.tooltip.amount") + ": 0mB")
        }
        return list
    }

    override fun getWailaTag(tag: NBTTagCompound): NBTTagCompound {
        super.getWailaTag(tag)
        tag.setBoolean("locked", locked)
        tag.setLong("amount", amount)
        if (fluid == null) tag.setInteger("fluid", -1) else tag.setInteger("fluid", fluid!!.id)
        return tag
    }

    override fun onActivate(player: EntityPlayer, pos: Vec3): Boolean {
        if (player == null || player.worldObj == null) return true
        if (player.worldObj.isRemote) return true
        val s = player.currentEquippedItem
        if (s == null) {
            if (locked) return false
            if (fluid == null) return true
            if (watcher != null) watcher!!.remove(FluidUtil.createAEFluidStack(fluid))
            fluid = null
            amount = 0L
            val host = host
            host?.markForUpdate()
            return true
        }
        if (WrenchUtil.canWrench(s, player, tile!!.xCoord, tile!!.yCoord,
                        tile!!.zCoord)) {
            locked = !locked
            WrenchUtil.wrenchUsed(s, player, tile!!.xCoord,
                    tile!!.zCoord, tile!!.yCoord)
            val host = host
            host?.markForUpdate()
            if (locked) player.addChatMessage(ChatComponentTranslation(
                    "chat.appliedenergistics2.isNowLocked")) else player.addChatMessage(ChatComponentTranslation(
                    "chat.appliedenergistics2.isNowUnlocked"))
            return true
        }
        if (locked) return false
        if (FluidUtil.isFilled(s)) {
            if (fluid != null && watcher != null) watcher!!.remove(FluidUtil.createAEFluidStack(fluid))
            fluid = FluidUtil.getFluidFromContainer(s)!!.getFluid()
            if (watcher != null) watcher!!.add(FluidUtil.createAEFluidStack(fluid))
            val host = host
            host?.markForUpdate()
            return true
        }
        return false
    }

    override fun onStackChange(arg0: IItemList<*>?, arg1: IAEStack<*>?, arg2: IAEStack<*>?,
                               arg3: BaseActionSource, arg4: StorageChannel) {
        if (fluid != null) {
            val n = gridNode ?: return
            val g = n.grid ?: return
            val storage = g.getCache<IStorageGrid>(IStorageGrid::class.java) ?: return
            val fluids = fluidStorage ?: return
            for (s in fluids.storageList) {
                if (s.fluid === fluid) {
                    amount = s.stackSize
                    val host = host
                    host?.markForUpdate()
                    return
                }
            }
            amount = 0L
            val host = host
            host?.markForUpdate()
        }
    }

    override fun readFromNBT(data: NBTTagCompound) {
        super.readFromNBT(data)
        if (data.hasKey("amount")) amount = data.getLong("amount")
        if (data.hasKey("fluid")) {
            val id = data.getInteger("fluid")
            if (id == -1) fluid = null else fluid = FluidRegistry.getFluid(id)
        }
        if (data.hasKey("locked")) locked = data.getBoolean("locked")
    }

    @Throws(IOException::class)
    override fun readFromStream(data: ByteBuf): Boolean {
        super.readFromStream(data)
        amount = data.readLong()
        val id = data.readInt()
        if (id == -1) fluid = null else fluid = FluidRegistry.getFluid(id)
        locked = data.readBoolean()
        return true
    }

    @SideOnly(Side.CLIENT)
    override fun renderDynamic(x: Double, y: Double, z: Double,
                               rh: IPartRenderHelper, renderer: RenderBlocks) {
        if (fluid == null) return
        if (dspList == null) dspList = GLAllocation.generateDisplayLists(1)
        val tess = Tessellator.instance
        if (!isActive) return
        val ais = FluidUtil.createAEFluidStack(fluid)
        ais!!.stackSize = amount
        if (ais != null) {
            GL11.glPushMatrix()
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
            GL11.glNewList((dspList as Int?)!!, GL11.GL_COMPILE_AND_EXECUTE)
            renderFluid(tess, ais)
            GL11.glEndList()
            GL11.glPopMatrix()
        }
    }

    @SideOnly(Side.CLIENT)
    private fun renderFluid(tess: Tessellator, fluidStack: IAEFluidStack) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        val d = side
        GL11.glTranslated(d!!.offsetX * 0.77, d.offsetY * 0.77, d.offsetZ * 0.77)
        if (d == ForgeDirection.UP) {
            GL11.glScalef(1.0f, -1.0f, 1.0f)
            GL11.glRotatef(90.0f, 1.0f, 0.0f, 0.0f)
            GL11.glRotatef(90.0f, 0f, 0f, 1f)
        }
        if (d == ForgeDirection.DOWN) {
            GL11.glScalef(1.0f, -1.0f, 1.0f)
            GL11.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f)
            GL11.glRotatef(-90.0f, 0f, 0f, 1f)
        }
        if (d == ForgeDirection.EAST) {
            GL11.glScalef(-1.0f, -1.0f, -1.0f)
            GL11.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f)
        }
        if (d == ForgeDirection.WEST) {
            GL11.glScalef(-1.0f, -1.0f, -1.0f)
            GL11.glRotatef(90.0f, 0.0f, 1.0f, 0.0f)
        }
        if (d == ForgeDirection.NORTH) {
            GL11.glScalef(-1.0f, -1.0f, -1.0f)
        }
        if (d == ForgeDirection.SOUTH) {
            GL11.glScalef(-1.0f, -1.0f, -1.0f)
            GL11.glRotatef(180.0f, 0.0f, 1.0f, 0.0f)
        }
        GL11.glPushMatrix()
        try {
            val br = 16 shl 20 or 16 shl 4
            val var11 = br % 65536
            val var12 = br / 65536
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                    var11 * 0.8f, var12 * 0.8f)
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDisable(GL12.GL_RESCALE_NORMAL)
            // RenderHelper.enableGUIStandardItemLighting();
            tess.setColorOpaque_F(1.0f, 1.0f, 1.0f)
            val fluidIcon = fluid!!.icon
            if (fluidIcon != null) {
                GL11.glTranslatef(0.0f, 0.14f, -0.24f)
                GL11.glScalef(1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f)
                GL11.glTranslated(-8.6, -16.3, -1.2)
                Minecraft.getMinecraft().renderEngine
                        .bindTexture(TextureMap.locationBlocksTexture)
                val cake = Tessellator.instance
                cake.startDrawingQuads()
                try {
                    cake.setBrightness(255)
                    cake.setColorRGBA_F((fluid!!.color shr 16 and 0xFF) / 255.0f,
                            (fluid!!.color shr 8 and 0xFF) / 255.0f, (fluid!!.color and 0xFF) / 255.0f, 1.0f)
                    cake.addVertexWithUV(0.0, 16.0, 0.0, fluidIcon.minU.toDouble(),
                            fluidIcon.maxV.toDouble())
                    cake.addVertexWithUV(16.0, 16.0, 0.0, fluidIcon.maxU.toDouble(),
                            fluidIcon.maxV.toDouble())
                    cake.addVertexWithUV(16.0, 0.0, 0.0, fluidIcon.maxU.toDouble(),
                            fluidIcon.minV.toDouble())
                    cake.addVertexWithUV(0.0, 0.0, 0.0, fluidIcon.minU.toDouble(),
                            fluidIcon.minV.toDouble())
                } finally {
                    cake.draw()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        GL11.glPopMatrix()
        GL11.glTranslatef(0.0f, 0.14f, -0.24f)
        GL11.glScalef(1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f)
        var qty = fluidStack.stackSize
        if (qty > 999999999999L) qty = 999999999999L
        var msg = qty.toString() + "mB"
        if (qty > 1000000000) msg = qty / 1000000000.toString() + "MB" else if (qty > 1000000) msg = qty / 1000000.toString() + "KB" else if (qty > 9999) msg = java.lang.Long.toString(
                qty / 1000) + 'B'
        val fr = Minecraft.getMinecraft().fontRenderer
        val width = fr.getStringWidth(msg)
        GL11.glTranslatef(-0.5f * width, 0.0f, -1.0f)
        fr.drawString(msg, 0, 0, 0)
        GL11.glPopAttrib()
    }

    @SideOnly(Side.CLIENT)
    override fun renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks) {
        val ts = Tessellator.instance
        val side = TextureManager.TERMINAL_SIDE.texture
        rh.setTexture(side)
        rh.setBounds(4f, 4f, 13f, 12f, 12f, 14f)
        rh.renderInventoryBox(renderer)
        rh.setTexture(side, side, side, TextureManager.BUS_BORDER.texture,
                side, side)
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 16f)
        rh.renderInventoryBox(renderer)
        ts.setBrightness(13 shl 20 or 13 shl 4)
        rh.setInvColor(0xFFFFFF)
        rh.renderInventoryFace(TextureManager.BUS_BORDER.texture,
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(3f, 3f, 15f, 13f, 13f, 16f)
        rh.setInvColor(AEColor.Transparent.blackVariant)
        rh.renderInventoryFace(TextureManager.STORAGE_MONITOR.textures[0],
                ForgeDirection.SOUTH, renderer)
        rh.setInvColor(AEColor.Transparent.mediumVariant)
        rh.renderInventoryFace(TextureManager.STORAGE_MONITOR.textures[1],
                ForgeDirection.SOUTH, renderer)
        rh.setInvColor(AEColor.Transparent.whiteVariant)
        rh.renderInventoryFace(TextureManager.STORAGE_MONITOR.textures[2],
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(5f, 5f, 12f, 11f, 11f, 13f)
        renderInventoryBusLights(rh, renderer)
    }

    @SideOnly(Side.CLIENT)
    override fun renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper,
                              renderer: RenderBlocks) {
        val ts = Tessellator.instance
        val side = TextureManager.TERMINAL_SIDE.texture
        rh.setTexture(side)
        rh.setBounds(4f, 4f, 13f, 12f, 12f, 14f)
        rh.renderBlock(x, y, z, renderer)
        rh.setTexture(side, side, side, TextureManager.BUS_BORDER.texture,
                side, side)
        rh.setBounds(2f, 2f, 14f, 14f, 14f, 16f)
        rh.renderBlock(x, y, z, renderer)
        if (isActive) Tessellator.instance.setBrightness(13 shl 20 or 13 shl 4)
        ts.setColorOpaque_I(0xFFFFFF)
        rh.renderFace(x, y, z, TextureManager.BUS_BORDER.texture,
                ForgeDirection.SOUTH, renderer)
        val host = host
        rh.setBounds(3f, 3f, 15f, 13f, 13f, 16f)
        ts.setColorOpaque_I(host!!.color.mediumVariant)
        rh.renderFace(x, y, z, TextureManager.STORAGE_MONITOR.textures[0],
                ForgeDirection.SOUTH, renderer)
        ts.setColorOpaque_I(host.color.whiteVariant)
        rh.renderFace(x, y, z, TextureManager.STORAGE_MONITOR.textures[1],
                ForgeDirection.SOUTH, renderer)
        ts.setColorOpaque_I(host.color.blackVariant)
        rh.renderFace(x, y, z, TextureManager.STORAGE_MONITOR.textures[2],
                ForgeDirection.SOUTH, renderer)
        rh.setBounds(5f, 5f, 12f, 11f, 11f, 13f)
        renderStaticBusLights(x, y, z, rh, renderer)
    }

    override fun requireDynamicRender(): Boolean {
        return true
    }

    override fun updateWatcher(w: IStackWatcher) {
        watcher = w
        if (fluid != null) w.add(FluidUtil.createAEFluidStack(fluid))
        onStackChange(null, null, null, null, null)
    }

    override fun writeToNBT(data: NBTTagCompound) {
        super.writeToNBT(data)
        data.setLong("amount", amount)
        if (fluid == null) data.setInteger("fluid", -1) else data.setInteger("fluid", fluid!!.id)
        data.setBoolean("locked", locked)
    }

    @Throws(IOException::class)
    override fun writeToStream(data: ByteBuf) {
        super.writeToStream(data)
        data.writeLong(amount)
        if (fluid == null) data.writeInt(-1) else data.writeInt(fluid!!.id)
        data.writeBoolean(locked)
    }
}