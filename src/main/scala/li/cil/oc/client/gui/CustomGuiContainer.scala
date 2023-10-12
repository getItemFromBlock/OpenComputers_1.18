package li.cil.oc.client.gui

import java.util

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.systems.RenderSystem
import li.cil.oc.client.gui.widget.WidgetContainer
import li.cil.oc.util.RenderState
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.MultiBufferSource
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.vertex.Tesselator
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.locale.Language
import net.minecraft.network.chat.TextComponent

import scala.collection.convert.ImplicitConversionsToScala._

// Workaround because certain other mods *cough*TMI*cough* do base class
// transformations that break things! Such fun. Many annoyed. And yes, this
// is a common issue, have a look at EnderIO and Enchanting Plus. They have
// to work around this, too.
//
// getItemFromBlock's note : I am going to assume that this is no longer relevant, might add back later
//
abstract class CustomGuiContainer[C <: AbstractContainerMenu](val inventoryContainer: C, inv: Inventory, title: Component)
  extends AbstractContainerScreen(inventoryContainer, inv, title) with WidgetContainer {

  override def windowX = leftPos

  override def windowY = topPos

  override def windowZ = getBlitOffset

  override def isPauseScreen = false

  protected def add[T](list: util.List[T], value: Any) = list.add(value.asInstanceOf[T])

  // Pretty much Scalaified copy-pasta from base-class.
  /*
  override def renderWrappedToolTip(stack: PoseStack, text: util.List[_ <: FormattedText], x: Int, y: Int, font: Font): Unit = {
    copiedDrawHoveringText0(stack, text, x, y, font)
  }
  protected def isPointInRegion(rectX: Int, rectY: Int, rectWidth: Int, rectHeight: Int, pointX: Int, pointY: Int): Boolean =
    pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1

  protected def copiedDrawHoveringText(stack: PoseStack, lines: util.List[String], x: Int, y: Int, font: Font): Unit = {
    val text = new util.ArrayList[TextComponent]()
    for (line <- lines) {
      text.add(new TextComponent(line))
    }
    copiedDrawHoveringText0(stack, text, x, y, font)
  }

  protected def copiedDrawHoveringText0(stack: PoseStack, text: util.List[_ <: FormattedText], x: Int, y: Int, font: Font): Unit = {
    if (!text.isEmpty) {
      RenderSystem.disableRescaleNormal()
      Lighting.turnOff()
      RenderSystem.disableLighting()
      RenderSystem.disableDepthTest()

      val textWidth = text.map(line => font.width(line)).max

      var posX = x + 12
      var posY = y - 12
      var textHeight = 8
      if (text.size > 1) {
        textHeight += 2 + (text.size - 1) * 10
      }
      if (posX + textWidth > width) {
        posX -= 28 + textWidth
      }
      if (posY + textHeight + 6 > height) {
        posY = height - textHeight - 6
      }

      setBlitOffset(300)
      itemRenderer.blitOffset = 300f
      val bg = 0xF0100010
      fillGradient(stack, posX - 3, posY - 4, posX + textWidth + 3, posY - 3, bg, bg)
      fillGradient(stack, posX - 3, posY + textHeight + 3, posX + textWidth + 3, posY + textHeight + 4, bg, bg)
      fillGradient(stack, posX - 3, posY - 3, posX + textWidth + 3, posY + textHeight + 3, bg, bg)
      fillGradient(stack, posX - 4, posY - 3, posX - 3, posY + textHeight + 3, bg, bg)
      fillGradient(stack, posX + textWidth + 3, posY - 3, posX + textWidth + 4, posY + textHeight + 3, bg, bg)
      val color1 = 0x505000FF
      val color2 = (color1 & 0x00FEFEFE) >> 1 | (color1 & 0xFF000000)
      fillGradient(stack, posX - 3, posY - 3 + 1, posX - 3 + 1, posY + textHeight + 3 - 1, color1, color2)
      fillGradient(stack, posX + textWidth + 2, posY - 3 + 1, posX + textWidth + 3, posY + textHeight + 3 - 1, color1, color2)
      fillGradient(stack, posX - 3, posY - 3, posX + textWidth + 3, posY - 3 + 1, color1, color1)
      fillGradient(stack, posX - 3, posY + textHeight + 2, posX + textWidth + 3, posY + textHeight + 3, color2, color2)

      stack.pushPose()
      stack.translate(0, 0, 400)
      val buffer = MultiBufferSource.immediate(Tesselator.getInstance.getBuilder())
      for ((line, index) <- text.zipWithIndex) {
        font.drawInBatch(Language.getInstance.getVisualOrder(line), posX, posY, -1, true, stack.last.pose, buffer, false, 0, 15728880)
        if (index == 0) {
          posY += 2
        }
        posY += 10
      }
      buffer.endBatch()
      stack.popPose()
      setBlitOffset(0)
      itemRenderer.blitOffset = 0f

      RenderSystem.enableLighting()
      RenderSystem.enableDepthTest()
      Lighting.turnBackOn()
      RenderSystem.enableRescaleNormal()
    }
  }

  override def fillGradient(stack: PoseStack, left: Int, top: Int, right: Int, bottom: Int, startColor: Int, endColor: Int): Unit = {
    super.fillGradient(stack, left, top, right, bottom, startColor, endColor)
    RenderState.makeItBlend()
  }

  */
  override def render(stack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    this.renderBackground(stack)
    super.render(stack, mouseX, mouseY, partialTicks)
    this.renderTooltip(stack, mouseX, mouseY)
  }
}
