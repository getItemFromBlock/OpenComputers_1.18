package li.cil.oc.common.container

import li.cil.oc.client.Textures
import li.cil.oc.common
import li.cil.oc.common.entity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.Container
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.inventory.DataSlot
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

class Drone(selfType: MenuType[_ <: Drone], id: Int, playerInventory: Inventory, droneInv: Container, val mainInvSize: Int)
  extends Player(selfType, id, playerInventory, droneInv) {

  val deltaY = 0

  override protected def getHostClass = classOf[entity.Drone]

  for (i <- 0 to 1) {
    val y = 8 + i * slotSize - deltaY
    for (j <- 0 to 3) {
      val x = 98 + j * slotSize
      addSlot(new InventorySlot(this, otherInventory, slots.size, x, y))
    }
  }

  addPlayerInventorySlots(8, 66)

  // This factor is used to make the energy values transferable using
  // MCs 'progress bar' stuff, even though those internally send the
  // values as shorts over the net (for whatever reason).
  private val factor = 100

  private val globalBufferData = droneInv match {
    case droneInv: entity.DroneInventory => {
      addDataSlot(new DataSlot {
        override def get(): Int = droneInv.drone.globalBuffer / factor

        override def set(value: Int): Unit = droneInv.drone.globalBuffer = value * factor
      })
    }
    case _ => addDataSlot(DataSlot.standalone)
  }
  def globalBuffer = globalBufferData.get * factor

  private val globalBufferSizeData = droneInv match {
    case droneInv: entity.DroneInventory => {
      addDataSlot(new DataSlot {
        override def get(): Int = droneInv.drone.globalBufferSize / factor

        override def set(value: Int): Unit = droneInv.drone.globalBufferSize = value * factor
      })
    }
    case _ => addDataSlot(DataSlot.standalone)
  }
  def globalBufferSize = globalBufferSizeData.get * factor

  private val runningData = droneInv match {
    case droneInv: entity.DroneInventory => {
      addDataSlot(new DataSlot {
        override def get(): Int = if (droneInv.drone.isRunning) 1 else 0

        override def set(value: Int): Unit = droneInv.drone.setRunning(value != 0)
      })
    }
    case _ => addDataSlot(DataSlot.standalone)
  }
  def isRunning = runningData.get != 0

  private val selectedSlotData = droneInv match {
    case droneInv: entity.DroneInventory => {
      addDataSlot(new DataSlot {
        override def get(): Int = droneInv.drone.selectedSlot

        override def set(value: Int): Unit = droneInv.drone.setSelectedSlot(value)
      })
    }
    case _ => addDataSlot(DataSlot.standalone)
  }
  def selectedSlot = selectedSlotData.get

  def statusText = synchronizedData.getString("statusText")

  override protected def detectCustomDataChanges(nbt: CompoundTag): Unit = {
    droneInv match {
      case droneInv: entity.DroneInventory => synchronizedData.putString("statusText", droneInv.drone.statusText)
      case _ =>
    }
    super.detectCustomDataChanges(nbt)
  }

  class InventorySlot(container: Player, inventory: Container, index: Int, x: Int, y: Int)
    extends StaticComponentSlot(container, inventory, index, x, y, getHostClass, common.Slot.Any, common.Tier.Any) {

    def isValid = (0 until mainInvSize).contains(getSlotIndex)

    @OnlyIn(Dist.CLIENT) override
    def isActive = isValid && super.isActive

    @OnlyIn(Dist.CLIENT)
    override def getBackgroundLocation =
      if (isValid) super.getBackgroundLocation
      else Textures.Icons.get(common.Tier.None)

    override def getItem = {
      if (isValid) super.getItem
      else ItemStack.EMPTY
    }
  }

}
