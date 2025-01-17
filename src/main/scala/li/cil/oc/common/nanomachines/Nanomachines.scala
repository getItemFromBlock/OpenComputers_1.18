package li.cil.oc.common.nanomachines

import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.nanomachines.BehaviorProvider
import li.cil.oc.api.nanomachines.Controller
import li.cil.oc.server.PacketSender
import li.cil.oc.util.PlayerUtils
import net.minecraft.world.entity.player.Player

import scala.collection.convert.ImplicitConversionsToJava._
import scala.collection.mutable

object Nanomachines extends api.detail.NanomachinesAPI {
  val providers = mutable.Set.empty[BehaviorProvider]

  val serverControllers: mutable.WeakHashMap[Player, ControllerImpl] = mutable.WeakHashMap.empty[Player, ControllerImpl]
  val clientControllers: mutable.WeakHashMap[Player, ControllerImpl] = mutable.WeakHashMap.empty[Player, ControllerImpl]

  def controllers(player: Player): mutable.WeakHashMap[Player, ControllerImpl] = if (player.level.isClientSide) clientControllers else serverControllers

  override def addProvider(provider: BehaviorProvider): Unit = providers += provider

  override def getProviders: java.lang.Iterable[BehaviorProvider] = providers

  def getController(player: Player): Controller = {
    if (hasController(player)) controllers(player).getOrElseUpdate(player, new ControllerImpl(player))
    else null
  }

  def hasController(player: Player): Boolean = {
    PlayerUtils.persistedData(player).getBoolean(Settings.namespace + "hasNanomachines")
  }

  def installController(player: Player): Controller = {
    if (!hasController(player)) {
      PlayerUtils.persistedData(player).putBoolean(Settings.namespace + "hasNanomachines", true)
    }
    getController(player) // Initialize controller instance.
  }

  override def uninstallController(player: Player): Unit = {
    getController(player) match {
      case controller: ControllerImpl =>
        controller.dispose()
        controllers(player) -= player
        PlayerUtils.persistedData(player).remove(Settings.namespace + "hasNanomachines")
        if (!player.level.isClientSide) {
          PacketSender.sendNanomachineConfiguration(player)
        }
      case _ => // Doesn't have one anyway.
    }
  }
}
