package li.cil.oc.common.item

import javax.annotation.Nonnull

import li.cil.oc.api
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.ItemStack
import net.minecraft.item.UseAction
import net.minecraft.potion.Effect
import net.minecraft.potion.Effects
import net.minecraft.potion.EffectInstance
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionHand
import net.minecraft.world.level.Level
import net.minecraftforge.common.extensions.IForgeItem

class Acid(props: Properties) extends Item(props) with IForgeItem with traits.SimpleItem {
  override def use(stack: ItemStack, world: Level, player: Player): InteractionResultHolder[ItemStack] = {
    player.startUsingItem(if (player.getItemInHand(Hand.MAIN_HAND) == stack) Hand.MAIN_HAND else Hand.OFF_HAND)
    new InteractionResultHolder(InteractionResult.sidedSuccess(world.isClientSide), stack)
  }

  override def getUseAnimation(stack: ItemStack): UseAction = UseAction.DRINK

  override def getUseDuration(stack: ItemStack): Int = 32

  override def finishUsingItem(stack: ItemStack, world: Level, entity: LivingEntity): ItemStack = {
    entity match {
      case player: Player =>
        if (!world.isClientSide) {
          player.addEffect(new EffectInstance(Effects.BLINDNESS, 200))
          player.addEffect(new EffectInstance(Effects.POISON, 100))
          player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 600))
          player.addEffect(new EffectInstance(Effects.CONFUSION, 1200))
          player.addEffect(new EffectInstance(Effects.SATURATION, 2000))

          // Remove nanomachines if installed.
          api.Nanomachines.uninstallController(player)
        }
        stack.shrink(1)
        if (stack.getCount > 0) stack
        else ItemStack.EMPTY
      case _ => stack
    }
  }
}
