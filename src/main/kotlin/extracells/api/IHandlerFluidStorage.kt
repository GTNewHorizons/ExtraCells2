package extracells.api

interface IHandlerFluidStorage {
    val isFormatted: Boolean
    fun totalBytes(): Int
    fun totalTypes(): Int
    fun usedBytes(): Int
    fun usedTypes(): Int
}