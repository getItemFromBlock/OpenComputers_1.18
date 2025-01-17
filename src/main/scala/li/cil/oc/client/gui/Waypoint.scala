package li.cil.oc.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.systems.RenderSystem
import li.cil.oc.client.PacketSender
import li.cil.oc.client.Textures
import li.cil.oc.common.tileentity
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.KeyMapping
import net.minecraft.network.chat.TextComponent
import org.lwjgl.glfw.GLFW

class Waypoint(val waypoint: tileentity.Waypoint) extends net.minecraft.client.gui.screens.Screen(TextComponent.EMPTY) {
  val imageWidth = 176
  val imageHeight = 24
  var leftPos = 0
  var topPos = 0
  passEvents = false

  var textField: EditBox = _

  override def tick(): Unit = {
    super.tick()
    textField.tick()
    if (minecraft.player.distanceToSqr(waypoint.x + 0.5, waypoint.y + 0.5, waypoint.z + 0.5) > 64) {
      onClose()
    }
  }

  override def isPauseScreen(): Boolean = false

  override protected def init(): Unit = {
    super.init()
    minecraft.mouseHandler.releaseMouse()
    KeyMapping.releaseAll()
    leftPos = (width - imageWidth) / 2
    topPos = (height - imageHeight) / 2

    textField = new EditBox(font, leftPos + 7, topPos + 8, 164 - 12, 12, TextComponent.EMPTY) {
      override def keyPressed(keyCode: Int, scanCode: Int, mods: Int): Boolean = {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
          val label = textField.getValue.take(32)
          if (label != waypoint.label) {
            waypoint.label = label
            PacketSender.sendWaypointLabel(waypoint)
            onClose()
          }
          return true
        }
        super.keyPressed(keyCode, scanCode, mods)
      }
    }
    textField.setMaxLength(32)
    textField.setBordered(false)
    textField.setCanLoseFocus(false)
    textField.setTextColor(0xFFFFFF)
    textField.setValue(waypoint.label)
    addWidget(textField)

    setInitialFocus(textField)
    minecraft.keyboardHandler.setSendRepeatsToGui(true)
  }

  override def removed(): Unit = {
    minecraft.keyboardHandler.setSendRepeatsToGui(false)
    super.removed()
  }

  override def render(stack: PoseStack, mouseX: Int, mouseY: Int, dt: Float): Unit = {
    super.render(stack, mouseX, mouseY, dt)
    // RenderSystem.color3f(1, 1, 1) // Required under Linux.
    Textures.bind(Textures.GUI.Waypoint)
    blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight)
    textField.render(stack, mouseX, mouseY, dt)
  }
}
