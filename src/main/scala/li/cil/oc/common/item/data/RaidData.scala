package li.cil.oc.common.item.data

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.util.ExtendedNBT._
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraftforge.common.util.Constants.NBT

class RaidData extends ItemData(Constants.BlockName.Raid) {
  def this(stack: ItemStack) {
    this()
    loadData(stack)
  }

  var disks = Array.empty[ItemStack]

  var filesystem = new CompoundTag()

  var label: Option[String] = None

  private final val DisksTag = Settings.namespace + "disks"
  private final val FileSystemTag = Settings.namespace + "filesystem"
  private final val LabelTag = Settings.namespace + "label"

  override def loadData(nbt: CompoundTag): Unit = {
    disks = nbt.getList(DisksTag, NBT.TAG_COMPOUND).
      toTagArray[CompoundTag].map(ItemStack.of(_))
    filesystem = nbt.getCompound(FileSystemTag)
    if (nbt.contains(LabelTag)) {
      label = Option(nbt.getString(LabelTag))
    }
  }

  override def saveData(nbt: CompoundTag): Unit = {
    nbt.setNewTagList(DisksTag, disks.toIterable)
    nbt.put(FileSystemTag, filesystem)
    label.foreach(nbt.putString(LabelTag, _))
  }
}
