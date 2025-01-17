package li.cil.oc.common.block

import java.util

import li.cil.oc.common.tileentity
import li.cil.oc.util.Rarity
import li.cil.oc.util.Tooltip
import net.minecraft.block.AbstractBlock.Properties
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.core.BlockPos
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.level.BlockGetter
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

import scala.collection.convert.ImplicitConversionsToScala._

class Hologram(props: Properties, val tier: Int) extends SimpleBlock(props) {
  val shape = VoxelShapes.box(0, 0, 0, 1, 0.5, 1)

  // ----------------------------------------------------------------------- //

  override def getShape(state: BlockState, world: BlockGetter, pos: BlockPos, ctx: ISelectionContext): VoxelShape = shape

  // ----------------------------------------------------------------------- //

  override protected def tooltipBody(stack: ItemStack, world: BlockGetter, tooltip: util.List[Component], advanced: ITooltipFlag) {
    for (curr <- Tooltip.get(getClass.getSimpleName.toLowerCase() + tier)) {
      tooltip.add(new TextComponent(curr).setStyle(Tooltip.DefaultStyle))
    }
  }

  // ----------------------------------------------------------------------- //

  override def newBlockEntity(world: BlockGetter) = new tileentity.Hologram(tileentity.TileEntityTypes.HOLOGRAM, tier)
}
