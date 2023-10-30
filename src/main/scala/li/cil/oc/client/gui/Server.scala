package li.cil.oc.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.systems.RenderSystem
import li.cil.oc.Localization
import li.cil.oc.client.Textures
import li.cil.oc.client.{PacketSender => ClientPacketSender}
import li.cil.oc.common.container
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.world.entity.player.Inventory
import net.minecraft.network.chat.Component

import scala.collection.JavaConverters.asJavaCollection

class Server(state: container.Server, playerInventory: Inventory, name: Component)
  extends DynamicGuiContainer(state, playerInventory, name)
  with traits.LockedHotbar[container.Server] {

  protected var powerButton: ImageButton = _

  override def lockedStack = inventoryContainer.stack

  override def render(stack: PoseStack, mouseX: Int, mouseY: Int, dt: Float): Unit = {
    powerButton.visible = !inventoryContainer.isItem
    powerButton.toggled = inventoryContainer.isRunning
    super.render(stack, mouseX, mouseY, dt)
  }

  override protected def init(): Unit = {
    super.init()
    powerButton = new ImageButton(leftPos + 48, topPos + 33, 18, 18, new Button.OnPress {
      override def onPress(b: Button) = if (inventoryContainer.rackSlot >= 0) {
        ClientPacketSender.sendServerPower(inventoryContainer, inventoryContainer.rackSlot, !inventoryContainer.isRunning)
      }
    }, Textures.GUI.ButtonPower, canToggle = true)
    addRenderableWidget(powerButton)
  }

  override def drawSecondaryForegroundLayer(stack: PoseStack, mouseX: Int, mouseY: Int): Unit = {
    super.drawSecondaryForegroundLayer(stack, mouseX, mouseY)
    if (powerButton.isMouseOver(mouseX, mouseY)) {
      val tooltip = new java.util.ArrayList[String]
      tooltip.addAll(asJavaCollection(if (inventoryContainer.isRunning) Localization.Computer.TurnOff.linesIterator.toIterable else Localization.Computer.TurnOn.linesIterator.toIterable))
      copiedDrawHoveringText(stack, tooltip, mouseX - leftPos, mouseY - topPos, font)
    }
  }

  override def drawSecondaryBackgroundLayer(stack: PoseStack): Unit = {
    // RenderSystem.color3f(1, 1, 1)
    Textures.bind(Textures.GUI.Server)
    blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight)
  }
}
