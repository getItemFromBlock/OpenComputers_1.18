package li.cil.oc.client

import li.cil.oc.Settings
import li.cil.oc.common.CompressedPacketBuilder
import li.cil.oc.common.PacketType
import li.cil.oc.common.SimplePacketBuilder
import li.cil.oc.common.container
import li.cil.oc.common.entity.Drone
import li.cil.oc.common.tileentity._
import li.cil.oc.common.tileentity.traits.Computer
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.world.entity.player.Player
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource

object PacketSender {
  // Timestamp after which the next clipboard message may be sent. Used to
  // avoid spamming large packets on key repeat.
  protected var clipboardCooldown = 0L

  def sendComputerPower(computer: container.Case, power: Boolean): Unit = {
    val pb = new SimplePacketBuilder(PacketType.ComputerPower)

    pb.writeInt(computer.containerId)
    pb.writeBoolean(power)

    pb.sendToServer()
  }

  def sendRobotPower(robot: container.Robot, power: Boolean): Unit = {
    val pb = new SimplePacketBuilder(PacketType.ComputerPower)

    pb.writeInt(robot.containerId)
    pb.writeBoolean(power)

    pb.sendToServer()
  }

  def sendDriveMode(unmanaged: Boolean): Unit = {
    val pb = new SimplePacketBuilder(PacketType.DriveMode)

    pb.writeBoolean(unmanaged)

    pb.sendToServer()
  }

  def sendDriveLock(): Unit = {
    val pb = new SimplePacketBuilder(PacketType.DriveLock)

    pb.sendToServer()
  }

  def sendDronePower(drone: container.Drone, power: Boolean): Unit = {
    val pb = new SimplePacketBuilder(PacketType.DronePower)

    pb.writeInt(drone.containerId)
    pb.writeBoolean(power)

    pb.sendToServer()
  }

  def sendKeyDown(address: String, char: Char, code: Int): Unit = {
    val pb = new SimplePacketBuilder(PacketType.KeyDown)

    pb.writeUTF(address)
    pb.writeChar(char)
    pb.writeInt(code)

    pb.sendToServer()
  }

  def sendKeyUp(address: String, char: Char, code: Int): Unit = {
    val pb = new SimplePacketBuilder(PacketType.KeyUp)

    pb.writeUTF(address)
    pb.writeChar(char)
    pb.writeInt(code)

    pb.sendToServer()
  }

  def sendTextInput(address: String, codePt: Int): Unit = {
    val pb = new SimplePacketBuilder(PacketType.TextInput)

    pb.writeUTF(address)
    pb.writeInt(codePt)

    pb.sendToServer()
  }

  def sendClipboard(address: String, value: String): Unit = {
    if (value != null && !value.isEmpty) {
      if (value.length > 64 * 1024 || System.currentTimeMillis() < clipboardCooldown) {
        val handler = Minecraft.getInstance.getSoundManager
        handler.play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HARP, 1, 1))
      }
      else {
        clipboardCooldown = System.currentTimeMillis() + value.length / 10
        for (part <- value.grouped(16 * 1024)) {
          val pb = new CompressedPacketBuilder(PacketType.Clipboard)

          pb.writeUTF(address)
          pb.writeUTF(part)

          pb.sendToServer()
        }
      }
    }
  }

  def sendMachineItemStateRequest(stack: ItemStack): Unit = {
    val pb = new SimplePacketBuilder(PacketType.MachineItemStateRequest)

    pb.writeItemStack(stack)

    pb.sendToServer()
  }

  def sendMouseClick(address: String, x: Double, y: Double, drag: Boolean, button: Int): Unit = {
    val pb = new SimplePacketBuilder(PacketType.MouseClickOrDrag)

    pb.writeUTF(address)
    pb.writeFloat(x.toFloat)
    pb.writeFloat(y.toFloat)
    pb.writeBoolean(drag)
    pb.writeByte(button.toByte)

    pb.sendToServer()
  }

  def sendMouseScroll(address: String, x: Double, y: Double, scroll: Int): Unit = {
    val pb = new SimplePacketBuilder(PacketType.MouseScroll)

    pb.writeUTF(address)
    pb.writeFloat(x.toFloat)
    pb.writeFloat(y.toFloat)
    pb.writeByte(scroll)

    pb.sendToServer()
  }

  def sendMouseUp(address: String, x: Double, y: Double, button: Int): Unit = {
    val pb = new SimplePacketBuilder(PacketType.MouseUp)

    pb.writeUTF(address)
    pb.writeFloat(x.toFloat)
    pb.writeFloat(y.toFloat)
    pb.writeByte(button.toByte)

    pb.sendToServer()
  }

  def sendCopyToAnalyzer(address: String, line: Int): Unit = {
    val pb = new SimplePacketBuilder(PacketType.CopyToAnalyzer)

    pb.writeUTF(address)
    pb.writeInt(line)

    pb.sendToServer()
  }

  def sendMultiPlace(): Unit = {
    val pb = new SimplePacketBuilder(PacketType.MultiPartPlace)
    pb.sendToServer()
  }

  def sendPetVisibility(): Unit = {
    val pb = new SimplePacketBuilder(PacketType.PetVisibility)

    pb.writeBoolean(!Settings.get.hideOwnPet)

    pb.sendToServer()
  }

  def sendRackMountableMapping(rack: container.Rack, mountableIndex: Int, nodeIndex: Int, side: Option[Direction]): Unit = {
    val pb = new SimplePacketBuilder(PacketType.RackMountableMapping)

    pb.writeInt(rack.containerId)
    pb.writeInt(mountableIndex)
    pb.writeInt(nodeIndex)
    pb.writeDirection(side)

    pb.sendToServer()
  }

  def sendRackRelayState(rack: container.Rack, enabled: Boolean): Unit = {
    val pb = new SimplePacketBuilder(PacketType.RackRelayState)

    pb.writeInt(rack.containerId)
    pb.writeBoolean(enabled)

    pb.sendToServer()
  }

  def sendRobotAssemblerStart(assembler: container.Assembler): Unit = {
    val pb = new SimplePacketBuilder(PacketType.RobotAssemblerStart)

    pb.writeInt(assembler.containerId)

    pb.sendToServer()
  }

  def sendRobotStateRequest(dimension: ResourceLocation, x: Int, y: Int, z: Int): Unit = {
    val pb = new SimplePacketBuilder(PacketType.RobotStateRequest)

    pb.writeUTF(dimension.toString)
    pb.writeInt(x)
    pb.writeInt(y)
    pb.writeInt(z)

    pb.sendToServer()
  }

  def sendServerPower(server: container.Server, mountableIndex: Int, power: Boolean): Unit = {
    val pb = new SimplePacketBuilder(PacketType.ServerPower)

    pb.writeInt(server.containerId)
    pb.writeInt(mountableIndex)
    pb.writeBoolean(power)

    pb.sendToServer()
  }

  def sendTextBufferInit(address: String): Unit = {
    val pb = new SimplePacketBuilder(PacketType.TextBufferInit)

    pb.writeUTF(address)

    pb.sendToServer()
  }

  def sendWaypointLabel(t: Waypoint): Unit = {
    val pb = new SimplePacketBuilder(PacketType.WaypointLabel)

    pb.writeTileEntity(t)
    pb.writeUTF(t.label)

    pb.sendToServer()
  }
}
