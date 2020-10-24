package extracells.wireless

import appeng.api.config.Settings
import appeng.api.config.SortDir
import appeng.api.config.SortOrder
import appeng.api.config.ViewItems
import appeng.api.util.IConfigManager
import net.minecraft.nbt.NBTTagCompound
import java.util.*

class ConfigManager @JvmOverloads constructor(private val tagCompound: NBTTagCompound? = null) : IConfigManager {
    private val settings: MutableMap<Settings, Enum<*>> = EnumMap(
            Settings::class.java)

    override fun getSettings(): Set<Settings> {
        return settings.keys
    }

    override fun registerSetting(settingName: Settings, defaultValue: Enum<*>) {
        settings[settingName] = defaultValue
    }

    override fun getSetting(settingName: Settings): Enum<*> {
        val oldValue = settings[settingName]
        if (oldValue != null) {
            return oldValue
        }
        throw IllegalStateException("Invalid Config setting. Expected a non-null value for $settingName")
    }

    override fun putSetting(settingName: Settings, newValue: Enum<*>): Enum<*> {
        val oldValue = getSetting(settingName)
        settings[settingName] = newValue
        if (tagCompound != null) writeToNBT(tagCompound)
        return oldValue
    }

    override fun writeToNBT(tagCompound: NBTTagCompound) {
        for ((key) in settings) {
            tagCompound.setString(key.name, settings[key].toString())
        }
    }

    override fun readFromNBT(tagCompound: NBTTagCompound) {
        for ((key) in settings) {
            try {
                if (tagCompound.hasKey(key.name)) {
                    val value = tagCompound.getString(key.name)
                    val oldValue = settings[key]!!
                    val newValue = java.lang.Enum.valueOf(oldValue.javaClass, value)
                    putSetting(key, newValue)
                }
            } catch (e: IllegalArgumentException) {
            }
        }
    }

    init {
        if (tagCompound != null) {
            registerSetting(Settings.SORT_BY, SortOrder.NAME)
            registerSetting(Settings.VIEW_MODE, ViewItems.ALL)
            registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING)
            readFromNBT(tagCompound.copy() as NBTTagCompound)
        }
    }
}