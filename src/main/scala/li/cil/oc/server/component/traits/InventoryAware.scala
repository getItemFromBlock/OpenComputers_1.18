package li.cil.oc.server.component.traits

import li.cil.oc.api.machine.Arguments
import li.cil.oc.util.ExtendedArguments._
import li.cil.oc.util.StackOption
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container

import scala.collection.immutable

trait InventoryAware {
  def fakePlayer: Player

  def inventory: Container

  def selectedSlot: Int

  def selectedSlot_=(value: Int): Unit

  def insertionSlots: immutable.IndexedSeq[Int] = (selectedSlot until inventory.getContainerSize) ++ (0 until selectedSlot)

  // ----------------------------------------------------------------------- //

  protected def optSlot(args: Arguments, n: Int): Int =
    if (args.count > 0 && args.checkAny(0) != null) args.checkSlot(inventory, 0)
    else selectedSlot

  protected def stackInSlot(slot: Int): StackOption = StackOption(inventory.getItem(slot))
}
