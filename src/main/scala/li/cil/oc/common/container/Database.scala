package li.cil.oc.common.container

import li.cil.oc.common.inventory.DatabaseInventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.Container
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class Database(selfType: MenuType[_ <: Database], id: Int, playerInventory: Inventory, val container: ItemStack, databaseInventory: Container, val tier: Int)
  extends li.cil.oc.common.container.Player(selfType, id, playerInventory, databaseInventory) {

  override protected def getHostClass = null

  val rows = math.sqrt(databaseInventory.getContainerSize).ceil.toInt
  val offset = 8 + Array(3, 2, 0)(tier) * slotSize

  for (row <- 0 until rows; col <- 0 until rows) {
    addSlotToContainer(offset + col * slotSize, offset + row * slotSize)
  }

  // Show the player's inventory.
  addPlayerInventorySlots(8, 174)

  override def stillValid(player: player.Player) = player == playerInventory.player

  override def clicked(slot: Int, dragType: Int, clickType: ClickType, player: player.Player): ItemStack = {
    if (slot >= databaseInventory.getContainerSize() || slot < 0) {
      // if the slot interaction is with the user inventory use
      // default behavior
      return super.clicked(slot, dragType, clickType, player)
    }
    // remove the ghost item
    val ghostSlot = this.slots.get(slot);
    if (ghostSlot != null) {
      val inventoryPlayer = player.inventory
      val hand = inventoryPlayer.getCarried()
      var itemToAdd = ItemStack.EMPTY
      // if the player is holding an item, place a copy
      if (!hand.isEmpty()) {
        itemToAdd = hand.copy()
      }
      ghostSlot.set(itemToAdd)
    }
    ItemStack.EMPTY
  }

  override protected def tryTransferStackInSlot(from: Slot, intoPlayerInventory: Boolean) {
    if (intoPlayerInventory) {
      from.setChanged()
      return
    }
  
    val fromStack = from.getItem().copy()
    if (fromStack.isEmpty) {
      return
    }

    fromStack.setCount(1)
    val (begin, end) = (0, slots.size - 1)

    for (i <- begin to end) {
      val intoSlot = slots.get(i)
      if (intoSlot.container != from.container) {
        if (!intoSlot.hasItem && intoSlot.mayPlace(fromStack)) {
          if (intoSlot.getMaxStackSize > 0) {
            intoSlot.set(fromStack)
            return
          }
        }
      }
    }
  }
}

