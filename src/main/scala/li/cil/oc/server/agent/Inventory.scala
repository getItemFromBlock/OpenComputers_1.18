package li.cil.oc.server.agent

import java.util.function.Predicate

import li.cil.oc.api.internal
import li.cil.oc.util.ExtendedInventory._
import li.cil.oc.util.InventoryUtils
import li.cil.oc.util.StackOption
import li.cil.oc.util.StackOption._
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListNBT
import net.minecraft.util.DamageSource
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.level.block.state.BlockState

import scala.collection.immutable

class Inventory(playerEntity: Player, val agent: internal.Agent) extends Inventory(playerEntity) {

  private def selectedItemStack: ItemStack = agent.mainInventory.getItem(agent.selectedSlot)

  private def inventorySlots: immutable.IndexedSeq[Int] = (agent.selectedSlot until getContainerSize) ++ (0 until agent.selectedSlot)

  override def getSelected: ItemStack = agent.equipmentInventory.getItem(0)

  override def getFreeSlot: Int = {
    if (selectedItemStack.isEmpty) agent.selectedSlot
    else inventorySlots.find(getItem(_).isEmpty).getOrElse(-1)
  }

  override def pickSlot(direction: Int) {}

  override def clearOrCountMatchingItems(f: Predicate[ItemStack], count: Int, inv: Container): Int = 0

  override def tick() {
    for (slot <- 0 until getContainerSize) {
      StackOption(getItem(slot)) match {
        case SomeStack(stack) => try stack.inventoryTick(agent.world, if (!agent.world.isClientSide) agent.player else null, slot, slot == 0) catch {
          case ignored: NullPointerException => // Client side item updates that need a player instance...
        }
        case _ =>
      }
    }
  }

  override def add(stack: ItemStack): Boolean = {
    val slots = this.indices.drop(agent.selectedSlot) ++ this.indices.take(agent.selectedSlot)
    InventoryUtils.insertIntoInventory(stack, InventoryUtils.asItemHandler(this), slots = Option(slots))
  }

  override def getDestroySpeed(state: BlockState): Float = if (getSelected.isEmpty) 1f else getSelected.getDestroySpeed(state)

  override def save(nbt: ListNBT): ListNBT = nbt

  override def load(nbt: ListNBT) {}

  override def getArmor(slot: Int): ItemStack = ItemStack.EMPTY

  override def hurtArmor(source: DamageSource, damage: Float) {}

  override def dropAll(): Unit = {}

  override def contains(stack: ItemStack): Boolean = (0 until getContainerSize).map(getItem).filter(!_.isEmpty).exists(_.sameItem(stack))

  override def replaceWith(from: Inventory) {}

  // Container

  override def getContainerSize: Int = agent.mainInventory.getContainerSize

  override def getItem(slot: Int): ItemStack =
    if (slot < 0) agent.equipmentInventory.getItem(~slot)
    else agent.mainInventory.getItem(slot)

  override def removeItem(slot: Int, amount: Int): ItemStack = {
    if (slot < 0) agent.equipmentInventory.removeItem(~slot, amount)
    else agent.mainInventory.removeItem(slot, amount)
  }

  override def removeItemNoUpdate(slot: Int): ItemStack = {
    if (slot < 0) agent.equipmentInventory.removeItemNoUpdate(~slot)
    else agent.mainInventory.removeItemNoUpdate(slot)
  }

  override def setItem(slot: Int, stack: ItemStack): Unit = {
    if (slot < 0) agent.equipmentInventory.setItem(~slot, stack)
    else agent.mainInventory.setItem(slot, stack)
  }

  override def getName: Component = new TextComponent(agent.name)

  override def getMaxStackSize: Int = agent.mainInventory.getMaxStackSize

  override def setChanged(): Unit = agent.mainInventory.setChanged()

  override def stillValid(player: Player): Boolean = agent.mainInventory.stillValid(player)

  override def canPlaceItem(slot: Int, stack: ItemStack): Boolean =
    if (slot < 0) agent.equipmentInventory.canPlaceItem(~slot, stack)
    else agent.mainInventory.canPlaceItem(slot, stack)
}
