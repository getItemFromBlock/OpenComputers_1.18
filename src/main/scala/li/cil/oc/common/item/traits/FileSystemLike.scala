package li.cil.oc.common.item.traits

import java.util

import li.cil.oc.Localization
import li.cil.oc.OpenComputers
import li.cil.oc.Settings
import li.cil.oc.client.gui
import li.cil.oc.common.item.data.DriveData
import li.cil.oc.util.Tooltip
import net.minecraft.client.Minecraft
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionHand
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.level.Level
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

trait FileSystemLike extends SimpleItem {
  override protected def tooltipName = None

  def kiloBytes: Int

  @OnlyIn(Dist.CLIENT)
  override def appendHoverText(stack: ItemStack, world: Level, tooltip: util.List[Component], flag: ITooltipFlag) {
    super.appendHoverText(stack, world, tooltip, flag)
    if (stack.hasTag) {
      val nbt = stack.getTag
      if (nbt.contains(Settings.namespace + "data")) {
        val data = nbt.getCompound(Settings.namespace + "data")
        if (data.contains(Settings.namespace + "fs.label")) {
          tooltip.add(new TextComponent(data.getString(Settings.namespace + "fs.label")).setStyle(Tooltip.DefaultStyle))
        }
        if (flag.isAdvanced && data.contains("fs")) {
          val fsNbt = data.getCompound("fs")
          if (fsNbt.contains("capacity.used")) {
            val used = fsNbt.getLong("capacity.used")
            tooltip.add(new TextComponent(Localization.Tooltip.DiskUsage(used, kiloBytes * 1024)).setStyle(Tooltip.DefaultStyle))
          }
        }
      }
      val data = new DriveData(stack)
      tooltip.add(new TextComponent(Localization.Tooltip.DiskMode(data.isUnmanaged)).setStyle(Tooltip.DefaultStyle))
      tooltip.add(new TextComponent(Localization.Tooltip.DiskLock(data.lockInfo)).setStyle(Tooltip.DefaultStyle))
    }
  }

  override def use(stack: ItemStack, world: Level, player: Player): InteractionResultHolder[ItemStack] = {
    if (!player.isCrouching && (!stack.hasTag || !stack.getTag.contains(Settings.namespace + "lootFactory"))) {
      if (world.isClientSide) showGui(stack, player)
      player.swing(Hand.MAIN_HAND)
    }
    new InteractionResultHolder(InteractionResult.sidedSuccess(world.isClientSide), stack)
  }

  @OnlyIn(Dist.CLIENT)
  private def showGui(stack: ItemStack, player: Player) {
    Minecraft.getInstance.pushGuiLayer(new gui.Drive(player.inventory, () => stack))
  }
}
