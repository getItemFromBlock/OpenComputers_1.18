package li.cil.oc.client.gui

import li.cil.oc.common.container
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.chat.Component

class Tablet(state: container.Tablet, playerInventory: PlayerInventory, name: Component)
  extends DynamicGuiContainer(state, playerInventory, name)
  with traits.LockedHotbar[container.Tablet] {

  override def lockedStack = inventoryContainer.stack
}
