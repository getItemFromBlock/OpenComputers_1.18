package li.cil.oc.common.block

import java.util

import li.cil.oc.CreativeTab
import li.cil.oc.common.tileentity
import li.cil.oc.common.tileentity.traits.Colored
import li.cil.oc.common.tileentity.traits.Inventory
import li.cil.oc.common.tileentity.traits.Rotatable
import li.cil.oc.server.loot.LootFunctions
import li.cil.oc.util.Color
import li.cil.oc.util.ExtendedWorld._
import li.cil.oc.util.Tooltip
import net.minecraft.block.AbstractBlock.Properties
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.block.BlockRenderType
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.block.material.Material
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.world.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootParameters
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.InteractionResult
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.core.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.IWorldReader
import net.minecraft.world.level.Level
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.common.ToolType

import scala.collection.convert.ImplicitConversionsToScala._

abstract class SimpleBlock(props: Properties) extends BaseEntityBlock(props) {
  @Deprecated
  private var unlocalizedName = super.getDescriptionId()

  @Deprecated
  private[oc] def setUnlocalizedName(name: String): Unit = unlocalizedName = "tile." + name

  @Deprecated
  override def getDescriptionId = unlocalizedName

  protected val validRotations_ = Array(Direction.UP, Direction.DOWN)

  def createItemStack(amount: Int = 1) = new ItemStack(this, amount)

  override def newBlockEntity(world: BlockGetter): BlockEntity = null

  override def getRenderShape(state: BlockState): BlockRenderType = BlockRenderType.MODEL

  // ----------------------------------------------------------------------- //
  // BlockItem
  // ----------------------------------------------------------------------- //

  @OnlyIn(Dist.CLIENT)
  override def appendHoverText(stack: ItemStack, world: BlockGetter, tooltip: util.List[Component], flag: ITooltipFlag) {
    tooltipHead(stack, world, tooltip, flag)
    tooltipBody(stack, world, tooltip, flag)
    tooltipTail(stack, world, tooltip, flag)
  }

  protected def tooltipHead(stack: ItemStack, world: BlockGetter, tooltip: util.List[Component], flag: ITooltipFlag) {
  }

  protected def tooltipBody(stack: ItemStack, world: BlockGetter, tooltip: util.List[Component], flag: ITooltipFlag) {
    for (curr <- Tooltip.get(getClass.getSimpleName.toLowerCase)) {
      tooltip.add(new TextComponent(curr).setStyle(Tooltip.DefaultStyle))
    }
  }

  protected def tooltipTail(stack: ItemStack, world: BlockGetter, tooltip: util.List[Component], flag: ITooltipFlag) {
  }

  // ----------------------------------------------------------------------- //
  // Rotation
  // ----------------------------------------------------------------------- //

  def getFacing(world: BlockGetter, pos: BlockPos): Direction =
    world.getBlockEntity(pos) match {
      case tileEntity: Rotatable => tileEntity.facing
      case _ => Direction.SOUTH
    }

  def setFacing(world: World, pos: BlockPos, value: Direction): Boolean =
    world.getBlockEntity(pos) match {
      case rotatable: Rotatable => rotatable.setFromFacing(value); true
      case _ => false
    }

  def setRotationFromEntityPitchAndYaw(world: World, pos: BlockPos, value: Entity): Boolean =
    world.getBlockEntity(pos) match {
      case rotatable: Rotatable => rotatable.setFromEntityPitchAndYaw(value); true
      case _ => false
    }

  def toLocal(world: BlockGetter, pos: BlockPos, value: Direction): Direction =
    world.getBlockEntity(pos) match {
      case rotatable: Rotatable => rotatable.toLocal(value)
      case _ => value
    }

  // ----------------------------------------------------------------------- //
  // Block
  // ----------------------------------------------------------------------- //

  override def canHarvestBlock(state: BlockState, world: BlockGetter, pos: BlockPos, player: Player) = true

  override def getHarvestTool(state: BlockState): ToolType = null

  override def canBeReplacedByLeaves(state: BlockState, world: IWorldReader, pos: BlockPos): Boolean = false

  def getValidRotations(world: World, pos: BlockPos): Array[Direction] = validRotations_

  override def getDrops(state: BlockState, ctx: LootContext.Builder): util.List[ItemStack] = {
    val newCtx = ctx.getOptionalParameter(LootParameters.BLOCK_ENTITY) match {
      case _: Inventory => ctx.withDynamicDrop(LootFunctions.DYN_VOLATILE_CONTENTS, (c, f) => {
          c.getParamOrNull(LootParameters.BLOCK_ENTITY) match {
            case inventory: Inventory => inventory.forAllLoot(f)
            case _ =>
          }
        })
      case _ => ctx
    }
    super.getDrops(state, newCtx)
  }

  override def playerWillDestroy(world: World, pos: BlockPos, state: BlockState, player: Player) {
    if (!world.isClientSide && player.isCreative) world.getBlockEntity(pos) match {
      case inventory: Inventory => inventory.dropAllSlots()
      case _ => // Ignore.
    }
    super.playerWillDestroy(world, pos, state, player)
  }

  // ----------------------------------------------------------------------- //

  @Deprecated
  def rotateBlock(world: World, pos: BlockPos, axis: Direction): Boolean =
    world.getBlockEntity(pos) match {
      case rotatable: tileentity.traits.Rotatable if rotatable.rotate(axis) =>
        world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3)
        true
      case _ => false
    }

  // ----------------------------------------------------------------------- //

  override def use(state: BlockState, world: World, pos: BlockPos, player: Player, hand: Hand, trace: BlockRayTraceResult): InteractionResult = {
    val heldItem = player.getItemInHand(hand)
    world.getBlockEntity(pos) match {
      case colored: Colored if Color.isDye(heldItem) =>
        colored.setColor(Color.rgbValues(Color.dyeColor(heldItem)))
        world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3)
        if (!player.isCreative && colored.consumesDye) {
          heldItem.split(1)
        }
        InteractionResult.sidedSuccess(world.isClientSide)
      case _ => {
        val loc = trace.getLocation
        val pos = trace.getBlockPos
        val x = loc.x.toFloat - pos.getX
        val y = loc.y.toFloat - pos.getY
        val z = loc.z.toFloat - pos.getZ
        if (localOnBlockActivated(world, pos, player, hand, heldItem, trace.getDirection, x, y, z))
          InteractionResult.sidedSuccess(world.isClientSide) else InteractionResult.PASS
      }
    }
  }

  def localOnBlockActivated(world: World, pos: BlockPos, player: Player, hand: Hand, heldItem: ItemStack, side: Direction, hitX: Float, hitY: Float, hitZ: Float) = false
}
