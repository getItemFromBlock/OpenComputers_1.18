package li.cil.oc.common.item

import java.util

import li.cil.oc.Localization
import li.cil.oc.Settings
import li.cil.oc.util.BlockPosition
import li.cil.oc.util.Tooltip
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.InteractionResult
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.level.Level
import net.minecraftforge.common.extensions.IForgeItem

class UpgradeMF(props: Properties) extends Item(props) with IForgeItem with traits.SimpleItem with traits.ItemTier {
  override def onItemUseFirst(stack: ItemStack, player: Player, world: Level, pos: BlockPos, side: Direction, hitX: Float, hitY: Float, hitZ: Float, hand: Hand): InteractionResult = {
    if (!player.level.isClientSide && player.isCrouching) {
      val data = stack.getOrCreateTag
      data.putString(Settings.namespace + "dimension", world.dimension.location.toString)
      data.putIntArray(Settings.namespace + "coord", Array(pos.getX, pos.getY, pos.getZ, side.ordinal()))
      return InteractionResult.sidedSuccess(player.level.isClientSide)
    }
    super.onItemUseFirst(stack, player, world, pos, side, hitX, hitY, hitZ, hand)
  }

  override protected def tooltipExtended(stack: ItemStack, tooltip: util.List[Component]) {
    tooltip.add(new TextComponent(Localization.Tooltip.MFULinked(stack.getTag match {
      case data: CompoundTag => data.contains(Settings.namespace + "coord")
      case _ => false
    })).setStyle(Tooltip.DefaultStyle))
  }
}
