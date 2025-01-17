package li.cil.oc.common.item.data

import com.google.common.base.Strings
import li.cil.oc.Constants
import li.cil.oc.util.ItemUtils
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag

class DroneData extends MicrocontrollerData(Constants.ItemName.Drone) {
  def this(stack: ItemStack) = {
    this()
    loadData(stack)
  }

  var name = ""

  override def loadData(nbt: CompoundTag): Unit = {
    super.loadData(nbt)
    name = ItemUtils.getDisplayName(nbt).getOrElse("")
    if (Strings.isNullOrEmpty(name)) {
      name = RobotData.randomName
    }
  }

  override def saveData(nbt: CompoundTag): Unit = {
    super.saveData(nbt)
    if (!Strings.isNullOrEmpty(name)) {
      ItemUtils.setDisplayName(nbt, name)
    }
  }
}
