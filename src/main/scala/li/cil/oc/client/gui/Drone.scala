package li.cil.oc.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.systems.RenderSystem
import li.cil.oc.Localization
import li.cil.oc.client.Textures
import li.cil.oc.client.gui.widget.ProgressBar
import li.cil.oc.client.renderer.TextBufferRenderCache
import li.cil.oc.client.renderer.font.TextBufferRenderData
import li.cil.oc.client.{PacketSender => ClientPacketSender}
import li.cil.oc.common.container
import li.cil.oc.util.PackedColor
import li.cil.oc.util.RenderState
import li.cil.oc.util.TextBuffer
import net.minecraft.client.gui.components.Button
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import net.minecraft.world.entity.player.Inventory
import net.minecraft.network.chat.Component
import org.lwjgl.opengl.GL11

import scala.collection.JavaConverters.asJavaCollection
import scala.collection.convert.ImplicitConversionsToJava._

class Drone(state: container.Drone, playerInventory: Inventory, name: Component)
  extends DynamicGuiContainer(state, playerInventory, name)
  with traits.DisplayBuffer {

  imageWidth = 176
  imageHeight = 148

  protected var powerButton: ImageButton = _

  private val buffer = new TextBuffer(20, 2, new PackedColor.SingleBitFormat(0x33FF33))
  private val bufferRenderer = new TextBufferRenderData {
    private var _dirty = true

    override def dirty = _dirty

    override def dirty_=(value: Boolean) = _dirty = value

    override def data = buffer

    override def viewport: (Int, Int) = buffer.size
  }

  override protected val bufferX = 9
  override protected val bufferY = 9
  override protected val bufferColumns = 80
  override protected val bufferRows = 16

  private val inventoryX = 97
  private val inventoryY = 7

  private val power = addRenderableWidget(new ProgressBar(28, 48))

  private val selectionSize = 20
  private val selectionsStates = 17
  private val selectionStepV = 1 / selectionsStates.toFloat

  override def render(stack: PoseStack, mouseX: Int, mouseY: Int, dt: Float): Unit = {
    powerButton.toggled = inventoryContainer.isRunning
    bufferRenderer.dirty = inventoryContainer.statusText.linesIterator.zipWithIndex.exists {
      case (line, i) => buffer.set(0, i, line, vertical = false)
    }
    super.render(stack, mouseX, mouseY, dt)
  }

  override protected def init(): Unit = {
    super.init()
    powerButton = new ImageButton(leftPos + 7, topPos + 45, 18, 18, new Button.OnPress {
      override def onPress(b: Button) = ClientPacketSender.sendDronePower(inventoryContainer, !inventoryContainer.isRunning)
    }, Textures.GUI.ButtonPower, canToggle = true)
    addRenderableWidget(powerButton)
  }

  override protected def drawBuffer(stack: PoseStack): Unit = {
    stack.translate(bufferX, bufferY, 0)
    RenderState.disableEntityLighting()
    RenderState.makeItBlend()
    stack.scale(scale.toFloat, scale.toFloat, 1)
    RenderState.pushAttrib()
    RenderSystem.depthMask(false)
    //RenderSystem.color3f(0.5f, 0.5f, 1f)
    TextBufferRenderCache.render(stack, bufferRenderer)
    RenderState.popAttrib()
  }

  override protected def changeSize(w: Double, h: Double) = 2.0

  override protected def renderLabels(stack: PoseStack, mouseX: Int, mouseY: Int) =
    drawSecondaryForegroundLayer(stack, mouseX, mouseY)

  override protected def drawSecondaryForegroundLayer(stack: PoseStack, mouseX: Int, mouseY: Int): Unit = {
    drawBufferLayer(stack)
    RenderState.pushAttrib()
    if (isPointInRegion(power.x, power.y, power.width, power.height, mouseX - leftPos, mouseY - topPos)) {
      val tooltip = new java.util.ArrayList[String]
      val format = Localization.Computer.Power + ": %d%% (%d/%d)"
      tooltip.add(format.format(
        inventoryContainer.globalBuffer * 100 / math.max(inventoryContainer.globalBufferSize, 1),
        inventoryContainer.globalBuffer, inventoryContainer.globalBufferSize))
      copiedDrawHoveringText(stack, tooltip, mouseX - leftPos, mouseY - topPos, font)
    }
    if (powerButton.isMouseOver(mouseX, mouseY)) {
      val tooltip = new java.util.ArrayList[String]
      tooltip.addAll(asJavaCollection(if (inventoryContainer.isRunning) Localization.Computer.TurnOff.linesIterator.toIterable else Localization.Computer.TurnOn.linesIterator.toIterable))
      copiedDrawHoveringText(stack, tooltip, mouseX - leftPos, mouseY - topPos, font)
    }
    RenderState.popAttrib()
  }

  override protected def renderBg(stack: PoseStack, dt: Float, mouseX: Int, mouseY: Int): Unit = {
    //RenderSystem.color3f(1, 1, 1)
    Textures.bind(Textures.GUI.Drone)
    blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight)
    power.level = inventoryContainer.globalBuffer.toFloat / math.max(inventoryContainer.globalBufferSize.toFloat, 1.0f)
    //drawWidgets(stack)
    if (inventoryContainer.otherInventory.getContainerSize > 0) {
      drawSelection(stack)
    }

    drawInventorySlots(stack)
  }

  // No custom slots, we just extend DynamicGuiContainer for the highlighting.
  override protected def drawSlotBackground(stack: PoseStack, x: Int, y: Int): Unit = {}

  private def drawSelection(stack: PoseStack): Unit = {
    val slot = inventoryContainer.selectedSlot
    if (slot >= 0 && slot < 16) {
      Textures.bind(Textures.GUI.RobotSelection)
      val now = System.currentTimeMillis() % 1000 / 1000.0f
      val offsetV = (now * selectionsStates).toInt * selectionStepV
      val x = leftPos + inventoryX - 1 + (slot % 4) * (selectionSize - 2)
      val y = topPos + inventoryY - 1 + (slot / 4) * (selectionSize - 2)

      val t = Tesselator.getInstance
      val r = t.getBuilder
      r.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
      r.vertex(stack.last.pose, x.toFloat, y.toFloat, getBlitOffset.toFloat).uv(0, offsetV).endVertex()
      r.vertex(stack.last.pose, x.toFloat, y.toFloat + selectionSize, getBlitOffset.toFloat).uv(0, offsetV + selectionStepV).endVertex()
      r.vertex(stack.last.pose, x.toFloat + selectionSize, y.toFloat + selectionSize, getBlitOffset.toFloat).uv(1, offsetV + selectionStepV).endVertex()
      r.vertex(stack.last.pose, x.toFloat + selectionSize, y.toFloat, getBlitOffset.toFloat).uv(1, offsetV).endVertex()
      t.end()
    }
  }
}
