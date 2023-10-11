package li.cil.oc.common.item.traits

import java.util

import com.mojang.blaze3d.matrix.MatrixStack
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.event.RobotRenderEvent.MountPoint
import li.cil.oc.api.internal.Robot
import li.cil.oc.client.renderer.item.UpgradeRenderer
import li.cil.oc.common.tileentity
import li.cil.oc.integration.opencomputers.{Item => OpenComputersItem}
import li.cil.oc.util.BlockPosition
import li.cil.oc.util.Tooltip
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.world.entity.player.Player
import net.minecraft.item.BlockItemUseContext
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.InteractionResult
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.IWorldReader
import net.minecraft.world.level.Level
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

import scala.collection.convert.ImplicitConversionsToScala._

trait SimpleItem extends Item with api.driver.item.UpgradeRenderer {
  def createItemStack(amount: Int = 1) = new ItemStack(this, amount)

  @Deprecated
  protected var unlocalizedName = getClass.getSimpleName.toLowerCase

  @Deprecated
  override def getDescriptionId = "item.oc." + unlocalizedName

  override def doesSneakBypassUse(stack: ItemStack, world: IWorldReader, pos: BlockPos, player: Player): Boolean = {
    world.getBlockEntity(pos) match {
      case drive: tileentity.DiskDrive => true
      case _ => super.doesSneakBypassUse(stack, world, pos, player)
    }
  }

  @Deprecated
  override def onItemUseFirst(stack: ItemStack, ctx: ItemUseContext): InteractionResult = {
    val pos = ctx.getClickedPos
    val hitPos = ctx.getClickLocation
    onItemUseFirst(stack, ctx.getPlayer, ctx.getPlayer.level, pos, ctx.getClickedFace,
      (hitPos.x - pos.getX).toFloat, (hitPos.y - pos.getY).toFloat, (hitPos.z - pos.getZ).toFloat, ctx.getHand)
  }

  @Deprecated
  def onItemUseFirst(stack: ItemStack, player: Player, world: World, pos: BlockPos, side: Direction, hitX: Float, hitY: Float, hitZ: Float, hand: Hand): InteractionResult = InteractionResult.PASS

  @Deprecated
  override def useOn(ctx: ItemUseContext): InteractionResult =
    ctx.getItemInHand match {
      case stack: ItemStack => {
        val world = ctx.getLevel
        val pos = BlockPosition(ctx.getClickedPos, world)
        val hitPos = ctx.getClickLocation
        val success = onItemUse(stack, ctx.getPlayer, pos, ctx.getClickedFace,
          (hitPos.x - pos.x).toFloat, (hitPos.y - pos.y).toFloat, (hitPos.z - pos.z).toFloat)
        if (success) InteractionResult.sidedSuccess(world.isClientSide) else InteractionResult.PASS
      }
      case _ => super.useOn(ctx)
    }

  @Deprecated
  def onItemUse(stack: ItemStack, player: Player, position: BlockPosition, side: Direction, hitX: Float, hitY: Float, hitZ: Float): Boolean = false

  @Deprecated
  override def use(world: World, player: Player, hand: Hand): InteractionResultHolder[ItemStack] =
    player.getItemInHand(hand) match {
      case stack: ItemStack => use(stack, world, player)
      case _ => super.use(world, player, hand)
    }

  @Deprecated
  def use(stack: ItemStack, world: World, player: Player): InteractionResultHolder[ItemStack] = new InteractionResultHolder(InteractionResult.PASS, stack)

  protected def tierFromDriver(stack: ItemStack): Int =
    api.Driver.driverFor(stack) match {
      case driver: api.driver.DriverItem => driver.tier(stack)
      case _ => 0
    }

  protected def tooltipName = Option(unlocalizedName)

  protected def tooltipData = Seq.empty[Any]

  @OnlyIn(Dist.CLIENT)
  override def appendHoverText(stack: ItemStack, world: World, tooltip: util.List[Component], flag: ITooltipFlag) {
    if (tooltipName.isDefined) {
      for (curr <- Tooltip.get(tooltipName.get, tooltipData: _*)) {
        tooltip.add(new TextComponent(curr).setStyle(Tooltip.DefaultStyle))
      }
      tooltipExtended(stack, tooltip)
    }
    else {
      for (curr <- Tooltip.get(getClass.getSimpleName.toLowerCase)) {
        tooltip.add(new TextComponent(curr).setStyle(Tooltip.DefaultStyle))
      }
    }
    tooltipCosts(stack, tooltip)
  }

  // For stuff that goes to the normal 'extended' tooltip, before the costs.
  protected def tooltipExtended(stack: ItemStack, tooltip: java.util.List[Component]) {}

  protected def tooltipCosts(stack: ItemStack, tooltip: java.util.List[Component]) {
    if (stack.hasTag && stack.getTag.contains(Settings.namespace + "data")) {
      val data = stack.getTag.getCompound(Settings.namespace + "data")
      if (data.contains("node") && data.getCompound("node").contains("address")) {
        tooltip.add(new TextComponent("§8" + data.getCompound("node").getString("address").substring(0, 13) + "...§7"))
      }
    }
  }

  // ----------------------------------------------------------------------- //

  override def computePreferredMountPoint(stack: ItemStack, robot: Robot, availableMountPoints: util.Set[String]): String = UpgradeRenderer.preferredMountPoint(stack, availableMountPoints)

  override def render(matrix: MatrixStack, buffer: IRenderTypeBuffer, stack: ItemStack, mountPoint: MountPoint, robot: Robot, pt: Float): Unit = UpgradeRenderer.render(matrix, buffer, stack, mountPoint)
}
