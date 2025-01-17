package li.cil.oc.util

import net.minecraft.nbt.CompoundTag

object NbtDataStream {
  def getShortArray(nbt: CompoundTag, key: String, array2d: Array[Array[Short]], w: Int, h: Int) : Boolean = {
    if (!nbt.contains(key)) {
      return false
    }

    val rawByteReader = new java.io.ByteArrayInputStream(nbt.getByteArray(key))
    val memReader = new java.io.DataInputStream(rawByteReader)
    for (y <- 0 until h) {
      for (x <- 0 until w) {
        if (2 > memReader.available()) {
          return true // not great, but get out now
        }
        array2d(y)(x) = memReader.readShort()
      }
    }
    true
  }

  def getIntArrayLegacy(nbt: CompoundTag, key: String, array2d: Array[Array[Short]], w: Int, h: Int) : Boolean = {
    if (!nbt.contains(key)) {
      return false
    }
    // legacy format
    val c = nbt.getIntArray(key)
    for (y <- 0 until h) {
      val rowColor = array2d(y)
      for (x <- 0 until w) {
        val index = x + y * w
        if (index >= c.length) {
          return true // not great, but, the read at least started
        }
        rowColor(x) = c(index).toShort
      }
    }
    true
  }

  def setShortArray(nbt: CompoundTag, key: String, array: Array[Short]): Unit = {
    val rawByteWriter = new java.io.ByteArrayOutputStream()
    val memWriter = new java.io.DataOutputStream(rawByteWriter)
    array.foreach(memWriter.writeShort(_))
    nbt.putByteArray(key, rawByteWriter.toByteArray)
  }

  def getOptBoolean(nbt: CompoundTag, key: String, df: Boolean): Boolean = if (nbt.contains(key)) nbt.getBoolean(key) else df
  def getOptString(nbt: CompoundTag, key: String, df: String): String = if (nbt.contains(key)) nbt.getString(key) else df
  def getOptNbt(nbt: CompoundTag, key: String): CompoundTag = if (nbt.contains(key)) nbt.getCompound(key) else new CompoundTag
  def getOptInt(nbt: CompoundTag, key: String, df: Int): Int = if (nbt.contains(key)) nbt.getInt(key) else df
}
