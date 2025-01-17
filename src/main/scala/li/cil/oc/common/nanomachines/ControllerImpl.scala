package li.cil.oc.common.nanomachines

import java.lang
import java.util.UUID

import com.google.common.base.Charsets
import com.google.common.base.Strings
import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.nanomachines.Behavior
import li.cil.oc.api.nanomachines.Controller
import li.cil.oc.api.nanomachines.DisableReason
import li.cil.oc.api.network.Packet
import li.cil.oc.api.network.WirelessEndpoint
import li.cil.oc.common.item.data.NanomachineData
import li.cil.oc.common.Tier
import li.cil.oc.integration.util.DamageSourceWithRandomCause
import li.cil.oc.server.PacketSender
import li.cil.oc.util.BlockPosition
import li.cil.oc.util.ExtendedNBT._
import li.cil.oc.util.InventoryUtils
import li.cil.oc.util.PlayerUtils
import net.minecraft.world.entity.player.Player
import net.minecraft.server.level.ServerPlayer
import net.minecraft.nbt.CompoundTag
import net.minecraft.particles.ParticleTypes
import net.minecraft.potion.Effect
import net.minecraft.potion.Effects
import net.minecraft.potion.EffectInstance
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

import scala.collection.convert.ImplicitConversionsToJava._
import scala.collection.convert.ImplicitConversionsToScala._
import scala.collection.mutable

class ControllerImpl(val player: PlayerEntity) extends Controller with WirelessEndpoint {
  if (isServer) api.Network.joinWirelessNetwork(this)
  var previousDimension = player.level.dimension

  lazy val CommandRange = Settings.get.nanomachinesCommandRange * Settings.get.nanomachinesCommandRange
  final val FullSyncInterval = 20 * 60

  final val OverloadDamage = new DamageSourceWithRandomCause("oc.nanomachinesOverload", 3).
    bypassArmor().
    bypassMagic()

  var uuid = UUID.randomUUID.toString
  var responsePort = 0
  var commandDelay = 0
  var queuedCommand: Option[() => Unit] = None
  var storedEnergy = Settings.get.bufferNanomachines * 0.25
  var hadPower = true
  val configuration = new NeuralNetwork(this)
  val activeBehaviors = mutable.Set.empty[Behavior]
  var activeBehaviorsDirty = true
  var hasSentConfiguration = false

  override def world: Level = player.level

  override def x: Int = BlockPosition(player).x

  override def y: Int = BlockPosition(player).y

  override def z: Int = BlockPosition(player).z

  override def receivePacket(packet: Packet, sender: WirelessEndpoint): Unit = {
    if (getLocalBuffer > 0 && commandDelay < 1 && player.isAlive) {
      val (dx, dy, dz) = ((sender.x + 0.5) - player.getX, (sender.y + 0.5) - player.getY, (sender.z + 0.5) - player.getZ)
      val dSquared = Math.sqrt(dx * dx + dy * dy + dz * dz)
      if (dSquared <= CommandRange) packet.data.headOption match {
        case Some(header: Array[Byte]) if new String(header, Charsets.UTF_8) == "nanomachines" =>
          val command = packet.data.drop(1).map {
            case value: Array[Byte] => new String(value, Charsets.UTF_8)
            case value => value
          }
          command match {
            case Array("setResponsePort", port: java.lang.Number) =>
              responsePort = port.intValue max 0 min 0xFFFF
              respond(sender, "port", responsePort)
            case Array("getPowerState") =>
              respond(sender, "power", getLocalBuffer, getLocalBufferSize)
            case Array("saveConfiguration") =>
              val nanomachines = api.Items.get(Constants.ItemName.Nanomachines)
              try {
                val index = player.inventory.items.indexWhere(stack => api.Items.get(stack) == nanomachines && new NanomachineData(stack).configuration.isEmpty)
                if (index >= 0) {
                  val stack = player.inventory.removeItem(index, 1)
                  new NanomachineData(this).saveData(stack)
                  player.inventory.add(stack)
                  InventoryUtils.spawnStackInWorld(BlockPosition(player), stack)
                  respond(sender, "saved", true)
                }
                else respond(sender, "saved", false, "no nanomachines")
              }
              catch {
                case _: Throwable =>
                  respond(sender, "saved", false, "error")
              }
            case Array("getHealth") =>
              respond(sender, "health", player.getHealth, player.getMaxHealth)
            case Array("getHunger") =>
              respond(sender, "hunger", player.getFoodData.getFoodLevel, player.getFoodData.getSaturationLevel)
            case Array("getAge") =>
              respond(sender, "age", (player.getNoActionTime / 20f).toInt)
            case Array("getName") =>
              respond(sender, "name", player.getDisplayName.getString)
            case Array("getExperience") =>
              respond(sender, "experience", player.experienceLevel)

            case Array("getTotalInputCount") =>
              respond(sender, "totalInputCount", getTotalInputCount)
            case Array("getSafeActiveInputs") =>
              respond(sender, "safeActiveInputs", getSafeActiveInputs)
            case Array("getMaxActiveInputs") =>
              respond(sender, "maxActiveInputs", getMaxActiveInputs)
            case Array("getInput", index: java.lang.Number) =>
              try {
                val trigger = getInput(index.intValue - 1)
                respond(sender, "input", index.intValue, trigger)
              }
              catch {
                case _: Throwable =>
                  respond(sender, "input", "error")
              }
            case Array("setInput", index: java.lang.Number, value: java.lang.Boolean) =>
              try {
                if (setInput(index.intValue - 1, value.booleanValue)) {
                  respond(sender, "input", index.intValue, getInput(index.intValue - 1))
                }
                else {
                  respond(sender, "input", "too many active inputs")
                }
              }
              catch {
                case _: Throwable =>
                  respond(sender, "input", "error")
              }
            case Array("getActiveEffects") =>
              configuration.synchronized {
                val names = getActiveBehaviors.map(_.getNameHint).filterNot(Strings.isNullOrEmpty)
                val joined = "{" + names.map(_.replace(',', '_').replace('"', '_')).mkString(",") + "}"
                respond(sender, "effects", joined)
              }
            case _ => // Ignore.
          }
        case _ => // Not for us.
      }
    }
  }

  def respond(endpoint: WirelessEndpoint, data: Any*): Unit = {
    queuedCommand = Option(() => {
      if (responsePort > 0) {
        val cost = Settings.get.wirelessCostPerRange(Tier.Two) * CommandRange
        val epsilon = 0.1
        if (changeBuffer(-cost) > -epsilon) {
          val packet = api.Network.newPacket(uuid, null, responsePort, (Iterable("nanomachines") ++ data.map(_.asInstanceOf[AnyRef])).toArray)
          api.Network.sendWirelessPacket(this, CommandRange, packet)
        }
      }
    })
    commandDelay = (Settings.get.nanomachinesCommandDelay * 20).toInt
  }

  // ----------------------------------------------------------------------- //

  override def reconfigure() = {
    if (isServer) configuration.synchronized {
      configuration.reconfigure()
      activeBehaviorsDirty = true

      player match {
        case playerMP: ServerPlayer if playerMP.connection != null =>
          player.addEffect(new EffectInstance(Effects.BLINDNESS, 100))
          player.addEffect(new EffectInstance(Effects.POISON, 150))
          player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 200))
          changeBuffer(-Settings.get.nanomachineReconfigureCost)

          hasSentConfiguration = false
        case _ => // We're still setting up / loading.
      }
    }
    this
  }

  override def getTotalInputCount: Int = configuration.synchronized(configuration.triggers.length)

  override def getSafeActiveInputs: Int = Settings.get.nanomachinesSafeInputsActive

  override def getMaxActiveInputs: Int = Settings.get.nanomachinesMaxInputsActive

  override def getInput(index: Int): Boolean = configuration.synchronized(configuration.triggers(index).isActive)

  override def setInput(index: Int, value: Boolean): Boolean = {
    isServer && configuration.synchronized {
      (!value || configuration.triggers.count(_.isActive) < Settings.get.nanomachinesMaxInputsActive) && {
        configuration.triggers(index).isActive = value
        activeBehaviorsDirty = true
        true
      }
    }
  }

  override def getActiveBehaviors: lang.Iterable[Behavior] = configuration.synchronized {
    cleanActiveBehaviors(DisableReason.InputChanged)
    activeBehaviors
  }

  override def getInputCount(behavior: Behavior): Int = configuration.synchronized(configuration.inputs(behavior))

  // ----------------------------------------------------------------------- //

  override def getLocalBuffer: Double = storedEnergy

  override def getLocalBufferSize: Double = Settings.get.bufferNanomachines

  override def changeBuffer(delta: Double): Double = {
    if (isClient) delta
    else if (delta < 0 && (Settings.get.ignorePower || player.isCreative)) 0.0
    else {
      val newValue = storedEnergy + delta
      storedEnergy = math.min(math.max(newValue, 0), getLocalBufferSize)
      newValue - storedEnergy
    }
  }

  // ----------------------------------------------------------------------- //

  def update(): Unit = {
    if (!player.isAlive) {
      return
    }

    if (isServer) {
      if (commandDelay > 0) {
        commandDelay -= 1
        if (commandDelay == 0) {
          queuedCommand.foreach(_ ())
          queuedCommand = None
        }
      }

      // Handle dimension changes, the robust way (because when logging in,
      // load is called while the world is still set to the overworld, but
      // no dimension change event is fired if the player actually logged
      // out in another dimension... yay)
      if (player.level.dimension != previousDimension) {
        api.Network.leaveWirelessNetwork(this, previousDimension)
        api.Network.joinWirelessNetwork(this)
        previousDimension = player.level.dimension
      }
      else {
        api.Network.updateWirelessNetwork(this)
      }
    }

    var hasPower = getLocalBuffer > 0 || Settings.get.ignorePower
    lazy val active = getActiveBehaviors.toIterable // Wrap once.
    lazy val activeInputs = configuration.triggers.count(_.isActive)

    if (hasPower != hadPower) {
      if (!hasPower) {
        active.foreach(_.onDisable(DisableReason.OutOfEnergy)) // This may change our energy buffer.
        hasPower = getLocalBuffer > 0 || Settings.get.ignorePower
      }
      else active.foreach(_.onEnable())
    }

    if (hasPower) {
      active.foreach(_.update())

      if (isServer) {
        if (player.level.getGameTime % Settings.get.tickFrequency == 0) {
          changeBuffer(-Settings.get.nanomachineCost * Settings.get.tickFrequency * (activeInputs + 0.5))
          PacketSender.sendNanomachinePower(player)
        }

        val overload = activeInputs - getSafeActiveInputs
        if (!player.isCreative && overload > 0 && player.level.getGameTime % 20 == 0) {
          player.hurt(OverloadDamage, overload)
        }
      }

      if (isClient && Settings.get.enableNanomachinePfx) {
        val energyRatio = getLocalBuffer / (getLocalBufferSize + 1)
        val triggerRatio = activeInputs / (configuration.triggers.length + 1)
        val intensity = (energyRatio + triggerRatio) * 0.25
        PlayerUtils.spawnParticleAround(player, ParticleTypes.PORTAL, intensity)
      }
    }

    if (isServer) {
      // Send new power state, if it changed.
      if (hadPower != hasPower) {
        PacketSender.sendNanomachinePower(player)
      }

      // Send a full sync every now and then, e.g. for other players coming
      // closer that weren't there to get the initial info for an enabled
      // input.
      if (!hasSentConfiguration || player.level.getGameTime % FullSyncInterval == 0) {
        hasSentConfiguration = true
        PacketSender.sendNanomachineConfiguration(player)
      }
    }

    hadPower = hasPower
  }

  def reset(): Unit = {
    configuration.synchronized {
      for (index <- 0 until getTotalInputCount) {
        configuration.triggers(index).isActive = false
        activeBehaviorsDirty = true
      }
      cleanActiveBehaviors(DisableReason.Default)
    }
  }

  def dispose(): Unit = {
    reset()
    if (isServer) {
      api.Network.leaveWirelessNetwork(this)
    }
  }

  def debug(): Unit = {
    if (isServer) {
      configuration.debug()
      activeBehaviorsDirty = true
    }
  }

  def print(): Unit = {
    if (isServer) {
      configuration.print(player)
    }
  }

  // ----------------------------------------------------------------------- //

  def saveData(nbt: CompoundTag): Unit = configuration.synchronized {
    nbt.putString("uuid", uuid)
    nbt.putInt("port", responsePort)
    nbt.putDouble("energy", storedEnergy)
    nbt.setNewCompoundTag("configuration", configuration.saveData)
  }

  def loadData(nbt: CompoundTag): Unit = configuration.synchronized {
    uuid = nbt.getString("uuid")
    responsePort = nbt.getInt("port")
    storedEnergy = nbt.getDouble("energy")
    configuration.loadData(nbt.getCompound("configuration"))
    activeBehaviorsDirty = true
  }

  // ----------------------------------------------------------------------- //

  private def isClient = world.isClientSide

  private def isServer = !isClient

  private def cleanActiveBehaviors(reason: DisableReason): Unit = {
    if (activeBehaviorsDirty) {
      configuration.synchronized(if (activeBehaviorsDirty) {
        val newBehaviors = configuration.behaviors.filter(_.isActive).map(_.behavior)
        val addedBehaviors = newBehaviors.clone --= activeBehaviors
        val removedBehaviors = activeBehaviors.clone --= newBehaviors
        activeBehaviors.clear()
        activeBehaviors ++= newBehaviors
        activeBehaviorsDirty = false
        addedBehaviors.foreach(_.onEnable())
        removedBehaviors.foreach(_.onDisable(reason))

        if (isServer) {
          PacketSender.sendNanomachineInputs(player)
        }
      })
    }
  }
}
