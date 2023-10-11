package li.cil.oc.client.gui

import li.cil.oc.common.container
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.chat.Component

class Adapter(state: container.Adapter, playerInventory: PlayerInventory, name: Component)
  extends DynamicGuiContainer(state, playerInventory, name) {
}
