package li.cil.oc.common.block.traits

import java.util

import li.cil.oc.common.block.SimpleBlock
import li.cil.oc.util.Tooltip
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.level.BlockGetter

import scala.collection.convert.ImplicitConversionsToScala._

trait PowerAcceptor extends SimpleBlock {
  def energyThroughput: Double

  // ----------------------------------------------------------------------- //

  override protected def tooltipTail(stack: ItemStack, world: BlockGetter, tooltip: util.List[Component], advanced: ITooltipFlag) {
    super.tooltipTail(stack, world, tooltip, advanced)
    for (curr <- Tooltip.extended("poweracceptor", energyThroughput.toInt)) {
      tooltip.add(new TextComponent(curr).setStyle(Tooltip.DefaultStyle))
    }
  }
}
