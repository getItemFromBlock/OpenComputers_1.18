package li.cil.oc.common.item

import li.cil.oc.OpenComputers
import li.cil.oc.Settings
import li.cil.oc.common.container.ContainerTypes
import li.cil.oc.common.inventory.DatabaseInventory
import net.minecraft.world.entity.player.Player
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.ItemStack
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionHand
import net.minecraft.world.level.Level
import net.minecraftforge.common.extensions.IForgeItem

class UpgradeDatabase(props: Properties, val tier: Int) extends Item(props) with IForgeItem with traits.SimpleItem with traits.ItemTier {
  @Deprecated
  override def getDescriptionId = super.getDescriptionId + tier

  override protected def tooltipName = Option(unlocalizedName)

  override protected def tooltipData = Seq(Settings.get.databaseEntriesPerTier(tier))

  override def use(stack: ItemStack, world: Level, player: PlayerEntity): InteractionResultHolder[ItemStack] = {
    if (!player.isCrouching) {
      if (!world.isClientSide) player match {
        case srvPlr: ServerPlayerEntity => ContainerTypes.openDatabaseGui(srvPlr, new DatabaseInventory {
            override def container = stack

            override def stillValid(player: PlayerEntity) = player == srvPlr
          })
        case _ =>
      }
      player.swing(Hand.MAIN_HAND)
    }
    else {
      stack.removeTagKey(Settings.namespace + "items")
      player.swing(Hand.MAIN_HAND)
    }
    new InteractionResultHolder(InteractionResult.sidedSuccess(world.isClientSide), stack)
  }
}
