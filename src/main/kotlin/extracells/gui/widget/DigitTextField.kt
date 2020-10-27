package extracells.gui.widget

import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import org.lwjgl.input.Keyboard
open class DigitTextField(fontRenderer: FontRenderer?, x: Int, y: Int, length: Int,
                     height: Int) : GuiTextField(fontRenderer, x, y, length, height) {
    private fun isWhiteListed(key: Char): Boolean {
        return "0123456789".contains(key.toString())
    }

    override fun textboxKeyTyped(keyChar: Char, keyID: Int): Boolean {
        return if (isFocused) {
            when (keyChar.toInt()) {
                1 -> {
                    setCursorPositionEnd()
                    setSelectionPos(0)
                    true
                }
                3 -> {
                    GuiScreen.setClipboardString(selectedText)
                    true
                }
                22 -> {
                    this.writeText(GuiScreen.getClipboardString())
                    true
                }
                24 -> {
                    GuiScreen.setClipboardString(selectedText)
                    this.writeText("")
                    true
                }
                else -> when (keyID) {
                    Keyboard.KEY_ESCAPE -> {
                        isFocused = false
                        true
                    }
                    14 -> {
                        if (GuiScreen.isCtrlKeyDown()) {
                            deleteWords(-1)
                        } else {
                            deleteFromCursor(-1)
                        }
                        true
                    }
                    199 -> {
                        if (GuiScreen.isShiftKeyDown()) {
                            setSelectionPos(0)
                        } else {
                            setCursorPositionZero()
                        }
                        true
                    }
                    203 -> {
                        if (GuiScreen.isShiftKeyDown()) {
                            if (GuiScreen.isCtrlKeyDown()) {
                                setSelectionPos(getNthWordFromPos(-1,
                                        selectionEnd))
                            } else {
                                setSelectionPos(selectionEnd - 1)
                            }
                        } else if (GuiScreen.isCtrlKeyDown()) {
                            cursorPosition = getNthWordFromCursor(-1)
                        } else {
                            moveCursorBy(-1)
                        }
                        true
                    }
                    205 -> {
                        if (GuiScreen.isShiftKeyDown()) {
                            if (GuiScreen.isCtrlKeyDown()) {
                                setSelectionPos(getNthWordFromPos(1,
                                        selectionEnd))
                            } else {
                                setSelectionPos(selectionEnd + 1)
                            }
                        } else if (GuiScreen.isCtrlKeyDown()) {
                            cursorPosition = getNthWordFromCursor(1)
                        } else {
                            moveCursorBy(1)
                        }
                        true
                    }
                    207 -> {
                        if (GuiScreen.isShiftKeyDown()) {
                            setSelectionPos(text.length)
                        } else {
                            setCursorPositionEnd()
                        }
                        true
                    }
                    211 -> {
                        if (GuiScreen.isCtrlKeyDown()) {
                            deleteWords(1)
                        } else {
                            deleteFromCursor(1)
                        }
                        true
                    }
                    else -> if (isWhiteListed(keyChar)) {
                        this.writeText(Character.toString(keyChar))
                        true
                    } else if (keyChar == '-' && text.isEmpty()) {
                        writeText(Character.toString(keyChar))
                        true
                    } else {
                        false
                    }
                }
            }
        } else {
            false
        }
    }
}