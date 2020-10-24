package extracells.util

import java.awt.Color

object ColorUtil {
    fun getInvertedColor(color: Color): Color {
        return Color(0xFFFFFF - color.rgb)
    }

    fun getInvertedColor(colorCode: Int): Color {
        return getInvertedColor(Color(colorCode))
    }

    fun getInvertedInt(color: Color): Int {
        return getInvertedColor(color).rgb
    }

    fun getInvertedInt(colorCode: Int): Int {
        return getInvertedColor(colorCode).rgb
    }
}