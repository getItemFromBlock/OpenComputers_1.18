package li.cil.oc.common.container

import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import li.cil.oc.common.template.DisassemblerTemplates
import li.cil.oc.common.tileentity
import li.cil.oc.util.ItemUtils
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.world.Container
import net.minecraft.world.inventory.MenuType
import net.minecraft.nbt.CompoundTag

class Disassembler(selfType: MenuType[_ <: Disassembler], id: Int, playerInventory: Inventory, val disassembler: Container)
  extends Player(selfType, id, playerInventory, disassembler) {

  private def allowDisassembling(stack: ItemStack) = !stack.isEmpty && (!stack.hasTag || !stack.getTag.getBoolean(Settings.namespace + "undisassemblable"))

  override protected def getHostClass = classOf[tileentity.Disassembler]

  addSlot(new StaticComponentSlot(this, otherInventory, slots.size, 80, 35, getHostClass, "ocitem", Tier.Any) {
    override def mayPlace(stack: ItemStack): Boolean = {
      if (!container.canPlaceItem(getSlotIndex, stack)) return false
      allowDisassembling(stack) &&
        (((Settings.get.disassembleAllTheThings || api.Items.get(stack) != null) &&
            ItemUtils.getIngredients(playerInventory.player.level.getRecipeManager, stack).nonEmpty) ||
          DisassemblerTemplates.select(stack).isDefined)
    }
  })
  addPlayerInventorySlots(8, 84)

  def disassemblyProgress = synchronizedData.getDouble("disassemblyProgress")

  override protected def detectCustomDataChanges(nbt: CompoundTag): Unit = {
    disassembler match {
      case te: tileentity.Disassembler => synchronizedData.putDouble("disassemblyProgress", te.progress)
      case _ =>
    }
    super.detectCustomDataChanges(nbt)
  }
}
