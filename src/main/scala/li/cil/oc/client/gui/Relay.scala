package li.cil.oc.client.gui

import java.text.DecimalFormat

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.systems.RenderSystem
import li.cil.oc.Localization
import li.cil.oc.client.Textures
import li.cil.oc.common.container
import net.minecraft.client.Minecraft
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.BufferUploader
import net.minecraft.client.renderer.Rect2i
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import net.minecraft.world.entity.player.Inventory
import net.minecraft.network.chat.Component
import org.lwjgl.opengl.GL11

class Relay(state: container.Relay, playerInventory: Inventory, name: Component)
  extends DynamicGuiContainer(state, playerInventory, name) {

  private val format = new DecimalFormat("#.##hz")

  val tabPosition = new Rect2i(imageWidth, 10, 23, 26)

  override protected def drawSecondaryBackgroundLayer(stack: PoseStack): Unit = {
    super.drawSecondaryBackgroundLayer(stack)

    // Tab background.
    // RenderSystem.color4f(1, 1, 1, 1)
    Textures.bind(Textures.GUI.UpgradeTab)
    val x = leftPos + tabPosition.getX
    val y = topPos + tabPosition.getY
    val w = tabPosition.getWidth
    val h = tabPosition.getHeight
    val t = Tesselator.getInstance
    val r = t.getBuilder
    r.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
    r.vertex(stack.last.pose, x.toFloat, y.toFloat + h, getBlitOffset.toFloat).uv(0, 1).endVertex()
    r.vertex(stack.last.pose, x.toFloat + w, y.toFloat + h, getBlitOffset.toFloat).uv(1, 1).endVertex()
    r.vertex(stack.last.pose, x.toFloat + w, y.toFloat, getBlitOffset.toFloat).uv(1, 0).endVertex()
    r.vertex(stack.last.pose, x.toFloat, y.toFloat, getBlitOffset.toFloat).uv(0, 0).endVertex()
    r.end()
    BufferUploader.end(r)
  }

  override def mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean = {
    // So MC doesn't throw away the item in the upgrade slot when we're trying to pick it up...
    val originalWidth = imageWidth
    try {
      imageWidth += tabPosition.getWidth
      super.mouseClicked(mouseX, mouseY, button)
    }
    finally {
      imageWidth = originalWidth
    }
  }

  override def mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean = {
    // So MC doesn't throw away the item in the upgrade slot when we're trying to pick it up...
    val originalWidth = imageWidth
    try {
      imageWidth += tabPosition.getWidth
      super.mouseReleased(mouseX, mouseY, button)
    }
    finally {
      imageWidth = originalWidth
    }
  }

  override def drawSecondaryForegroundLayer(stack: PoseStack, mouseX: Int, mouseY: Int): Unit = {
    super.drawSecondaryForegroundLayer(stack, mouseX, mouseY)

    font.draw(stack,
      Localization.Switch.TransferRate,
      14, 20, 0x404040)
    font.draw(stack,
      Localization.Switch.PacketsPerCycle,
      14, 39, 0x404040)
    font.draw(stack,
      Localization.Switch.QueueSize,
      14, 58, 0x404040)

    font.draw(stack,
      format.format(20f / inventoryContainer.relayDelay),
      108, 20, 0x404040)
    font.draw(stack,
      inventoryContainer.packetsPerCycleAvg + " / " + inventoryContainer.relayAmount,
      108, 39, thresholdBasedColor(inventoryContainer.packetsPerCycleAvg, math.ceil(inventoryContainer.relayAmount / 2f).toInt, inventoryContainer.relayAmount))
    font.draw(stack,
      inventoryContainer.queueSize + " / " + inventoryContainer.maxQueueSize,
      108, 58, thresholdBasedColor(inventoryContainer.queueSize, inventoryContainer.maxQueueSize / 2, inventoryContainer.maxQueueSize))
  }

  private def thresholdBasedColor(value: Int, yellow: Int, red: Int) = {
    if (value < yellow) 0x009900
    else if (value < red) 0x999900
    else 0x990000
  }
}
