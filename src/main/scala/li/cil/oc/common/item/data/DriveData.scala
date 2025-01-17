package li.cil.oc.common.item.data

import li.cil.oc.Settings
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import li.cil.oc.server.fs
import net.minecraft.world.entity.player.Player

class DriveData extends ItemData(null) {
  def this(stack: ItemStack) {
    this()
    loadData(stack)
  }

  var isUnmanaged = false
  var lockInfo: String = ""

  def isLocked: Boolean = {
    lockInfo != null && !lockInfo.isEmpty
  }

  private final val UnmanagedTag = Settings.namespace + "unmanaged"
  private val LockTag = Settings.namespace + "lock"

  override def loadData(nbt: CompoundTag) {
    isUnmanaged = nbt.getBoolean(UnmanagedTag)
    lockInfo = if (nbt.contains(LockTag)) {
      nbt.getString(LockTag)
    } else ""
  }

  override def saveData(nbt: CompoundTag) {
    nbt.putBoolean(UnmanagedTag, isUnmanaged)
    nbt.putString(LockTag, lockInfo)
  }
}

object DriveData {
  def lock(stack: ItemStack, player: Player): Unit = {
    val key = player.getName.getString
    val data = new DriveData(stack)
    if (!data.isLocked) {
      data.lockInfo = key match {
        case name: String if name != null && name.nonEmpty => name
        case _ => "notch" // meaning: "unknown"
      }
      data.saveData(stack)
    }
  }

  def setUnmanaged(stack: ItemStack, unmanaged: Boolean): Unit = {
    val data = new DriveData(stack)
    if (data.isUnmanaged != unmanaged) {
      fs.FileSystem.removeAddress(stack)
      data.lockInfo = ""
    }
    data.isUnmanaged = unmanaged
    data.saveData(stack)
  }
}
