package li.cil.oc.client.gui.traits

import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

trait LockedHotbar[C <: Container] extends ContainerScreen[C] {
  def lockedStack: ItemStack

  override def slotClicked(slot: Slot, slotId: Int, mouseButton: Int, clickType: ClickType): Unit = {
    if (slot == null || !slot.getItem.sameItem(lockedStack)) {
      super.slotClicked(slot, slotId, mouseButton, clickType)
    }
  }
}
