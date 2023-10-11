package li.cil.oc.common.item

import java.util

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.client.KeyBindings
import li.cil.oc.client.renderer.block.DroneModel
import li.cil.oc.common.item.data.DroneData
import li.cil.oc.common.entity
import li.cil.oc.server.agent
import li.cil.oc.util.BlockPosition
import li.cil.oc.util.Rarity
import li.cil.oc.util.Tooltip
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.core.Direction
import net.minecraft.core.NonNullList
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.common.extensions.IForgeItem
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

class Drone(props: Properties) extends Item(props) with IForgeItem with traits.SimpleItem with CustomModel {
  @OnlyIn(Dist.CLIENT)
  override def getModelLocation(stack: ItemStack) = new ModelResourceLocation(Settings.resourceDomain + ":" + Constants.ItemName.Drone, "inventory")

  @OnlyIn(Dist.CLIENT)
  override def bakeModels(bakeEvent: ModelBakeEvent): Unit = {
    bakeEvent.getModelRegistry.put(getModelLocation(createItemStack()), DroneModel)
  }

  override protected def tooltipExtended(stack: ItemStack, tooltip: util.List[Component]): Unit = {
    if (KeyBindings.showExtendedTooltips) {
      val info = new DroneData(stack)
      for (component <- info.components if !component.isEmpty) {
        tooltip.add(new TextComponent("- " + component.getHoverName.getString).setStyle(Tooltip.DefaultStyle))
      }
    }
  }

  override def getRarity(stack: ItemStack) = {
    val data = new DroneData(stack)
    Rarity.byTier(data.tier)
  }

  // Must be assembled to be usable so we hide it in the item list.
  override def fillItemCategory(tab: CreativeModeTab, list: NonNullList[ItemStack]) {}

  override def onItemUse(stack: ItemStack, player: Player, position: BlockPosition, side: Direction, hitX: Float, hitY: Float, hitZ: Float) = {
    val world = position.world.get
    if (!world.isClientSide) {
      val drone = entity.EntityTypes.DRONE.create(world)
      player match {
        case fakePlayer: agent.Player =>
          drone.ownerName = fakePlayer.agent.ownerName
          drone.ownerUUID = fakePlayer.agent.ownerUUID
        case _ =>
          drone.ownerName = player.getName.getString
          drone.ownerUUID = player.getGameProfile.getId
      }
      drone.initializeAfterPlacement(stack, player, position.offset(hitX * 1.1f, hitY * 1.1f, hitZ * 1.1f))
      world.addFreshEntity(drone)
    }
    stack.shrink(1)
    true
  }
}
