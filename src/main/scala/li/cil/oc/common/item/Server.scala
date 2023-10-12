package li.cil.oc.common.item

import java.util

import li.cil.oc.OpenComputers
import li.cil.oc.client.KeyBindings
import li.cil.oc.common.container.ContainerTypes
import li.cil.oc.common.inventory.ServerInventory
import li.cil.oc.util.Tooltip
import net.minecraft.world.entity.player.Player
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.ItemStack
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionHand
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.level.Level
import net.minecraftforge.common.extensions.IForgeItem

import scala.collection.mutable
import scala.collection.convert.ImplicitConversionsToScala._

class Server(props: Properties, val tier: Int) extends Item(props) with IForgeItem with traits.SimpleItem {
  @Deprecated
  override def getDescriptionId = super.getDescriptionId + tier

  override protected def tooltipName = Option(unlocalizedName)

  private object HelperInventory extends ServerInventory {
    var container = ItemStack.EMPTY

    override def rackSlot = -1
  }

  override protected def tooltipExtended(stack: ItemStack, tooltip: util.List[Component]) {
    super.tooltipExtended(stack, tooltip)
    if (KeyBindings.showExtendedTooltips) {
      HelperInventory.container = stack
      HelperInventory.reinitialize()
      val stacks = mutable.Map.empty[String, Int]
      for (aStack <- (0 until HelperInventory.getContainerSize).map(HelperInventory.getItem) if !aStack.isEmpty) {
        val displayName = aStack.getHoverName.getString
        stacks += displayName -> (if (stacks.contains(displayName)) stacks(displayName) + 1 else 1)
      }
      if (stacks.nonEmpty) {
        for (curr <- Tooltip.get("server.Components")) {
          tooltip.add(new TextComponent(curr).setStyle(Tooltip.DefaultStyle))
        }
        for (itemName <- stacks.keys.toArray.sorted) {
          tooltip.add(new TextComponent("- " + stacks(itemName) + "x " + itemName).setStyle(Tooltip.DefaultStyle))
        }
      }
    }
  }

  override def use(stack: ItemStack, world: Level, player: PlayerEntity): InteractionResultHolder[ItemStack] = {
    if (!player.isCrouching) {
      if (!world.isClientSide) player match {
        case srvPlr: ServerPlayer => ContainerTypes.openServerGui(srvPlr, new ServerInventory {
            override def container = stack

            override def rackSlot = -1

            override def stillValid(player: PlayerEntity) = player == srvPlr
          }, -1)
        case _ =>
      }
      player.swing(Hand.MAIN_HAND)
    }
    new InteractionResultHolder(InteractionResult.sidedSuccess(world.isClientSide), stack)
  }

}
