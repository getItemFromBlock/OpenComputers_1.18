package li.cil.oc.client.gui

import java.util.ArrayList
import java.util.List
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.systems.RenderSystem
import li.cil.oc.Localization
import li.cil.oc.client.Textures
import li.cil.oc.common
import li.cil.oc.common.container.ComponentSlot
import li.cil.oc.common.container.Player
import li.cil.oc.integration.Mods
import li.cil.oc.integration.jei.ModJEI
import li.cil.oc.integration.util.ItemSearch
import li.cil.oc.util.RenderState
import li.cil.oc.util.StackOption
import li.cil.oc.util.StackOption._
import net.minecraft.client.gui.GuiComponent
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.client.gui.Font
import org.lwjgl.opengl.GL11

import scala.collection.convert.ImplicitConversionsToJava._
import scala.collection.convert.ImplicitConversionsToScala._

// getItemFromBlock's note : See CustomGuiContainer.scala for more infos

abstract class DynamicGuiContainer[C <: AbstractContainerMenu](val inventoryContainer: C, inv: Inventory, title: Component)
  extends AbstractContainerScreen(inventoryContainer, inv, title) {

  protected var hoveredStackNEI: StackOption = EmptyStack

  override protected def init(): Unit = {
    super.init()
    // imageHeight is set in the body of the extending class, so it's not available in ours.
    inventoryLabelY = imageHeight - 96 + 2
  }

  protected def drawSecondaryForegroundLayer(stack: PoseStack, mouseX: Int, mouseY: Int): Unit = {}

  override protected def renderLabels(stack: PoseStack, mouseX: Int, mouseY: Int): Unit = {
    super.renderLabels(stack, mouseX, mouseY)
    RenderState.pushAttrib()

    drawSecondaryForegroundLayer(stack, mouseX, mouseY)

    for (slot <- 0 until menu.slots.size()) {
      drawSlotHighlight(stack, menu.getSlot(slot))
    }

    RenderState.popAttrib()
  }

  protected def drawSecondaryBackgroundLayer(stack: PoseStack): Unit = {}

  override protected def renderBg(stack: PoseStack, dt: Float, mouseX: Int, mouseY: Int): Unit = {
    //RenderSystem.color4f(1, 1, 1, 1)
    Textures.bind(Textures.GUI.Background)
    blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight)
    drawSecondaryBackgroundLayer(stack)

    RenderState.makeItBlend()
    //RenderSystem.disableLighting()

    drawInventorySlots(stack)
  }

  protected def drawInventorySlots(stack: PoseStack): Unit = {
    stack.pushPose()
    stack.translate(leftPos, topPos, 0)
    RenderSystem.disableDepthTest()
    for (slot <- 0 until menu.slots.size()) {
      drawSlotInventory(stack, menu.getSlot(slot))
    }
    RenderSystem.enableDepthTest()
    stack.popPose()
    RenderState.makeItBlend()
  }

  override def render(stack: PoseStack, mouseX: Int, mouseY: Int, dt: Float): Unit = {
    hoveredStackNEI = ItemSearch.hoveredStack(this, mouseX, mouseY)

    super.render(stack, mouseX, mouseY, dt)
  }

  protected def drawSlotInventory(stack: PoseStack, slot: Slot): Unit = {
    RenderSystem.enableBlend()
    slot match {
      case component: ComponentSlot if component.slot == common.Slot.None || component.tier == common.Tier.None =>
        if (!slot.hasItem && slot.x >= 0 && slot.y >= 0 && component.tierIcon != null) {
          drawDisabledSlot(stack, component)
        }
      case _ =>
        setBlitOffset(getBlitOffset + 1)
        if (!isInPlayerInventory(slot)) {
          drawSlotBackground(stack, slot.x - 1, slot.y - 1)
        }
        if (!slot.hasItem) {
          slot match {
            case component: ComponentSlot =>
              if (component.tierIcon != null) {
                Textures.bind(component.tierIcon)
                GuiComponent.blit(stack, slot.x, slot.y, getBlitOffset, 0, 0, 16, 16, 16, 16)
              }
              if (component.hasBackground) {
                Textures.bind(component.getBackgroundLocation)
                GuiComponent.blit(stack, slot.x, slot.y, getBlitOffset, 0, 0, 16, 16, 16, 16)
              }
            case _ =>
          }
          setBlitOffset(getBlitOffset - 1)
        }
    }
    RenderSystem.disableBlend()
  }

  protected def drawSlotHighlight(matrix: PoseStack, slot: Slot): Unit = {
    if (minecraft.player.inventoryMenu.getCarried.isEmpty) slot match {
      case component: ComponentSlot if component.slot == common.Slot.None || component.tier == common.Tier.None => // Ignore.
      case _ =>
        val currentIsInPlayerInventory = isInPlayerInventory(slot)
        val drawHighlight = hoveredSlot match {
          case hovered: Slot =>
            val hoveredIsInPlayerInventory = isInPlayerInventory(hovered)
            (currentIsInPlayerInventory != hoveredIsInPlayerInventory) &&
              ((currentIsInPlayerInventory && slot.hasItem && isSelectiveSlot(hovered) && hovered.mayPlace(slot.getItem)) ||
                (hoveredIsInPlayerInventory && hovered.hasItem && isSelectiveSlot(slot) && slot.mayPlace(hovered.getItem)))
          case _ => hoveredStackNEI match {
            case SomeStack(stack) => !currentIsInPlayerInventory && isSelectiveSlot(slot) && slot.mayPlace(stack)
            case _ => false
          }
        }
        if (drawHighlight) {
          setBlitOffset(getBlitOffset + 100)
          fillGradient(matrix,
            slot.x, slot.y,
            slot.x + 16, slot.y + 16,
            0x80FFFFFF, 0x80FFFFFF)
          setBlitOffset(getBlitOffset - 100)
        }
    }
  }

  private def isSelectiveSlot(slot: Slot) = slot match {
    case component: ComponentSlot => component.slot != common.Slot.Any && component.slot != common.Slot.Tool
    case _ => false
  }

  protected def drawDisabledSlot(stack: PoseStack, slot: ComponentSlot): Unit = {
    //RenderSystem.color4f(1, 1, 1, 1)
    Textures.bind(slot.tierIcon)
    GuiComponent.blit(stack, slot.x, slot.y, getBlitOffset, 0, 0, 16, 16, 16, 16)
  }

  protected def drawSlotBackground(stack: PoseStack, x: Int, y: Int): Unit = {
    //RenderSystem.color4f(1, 1, 1, 1)
    Textures.bind(Textures.GUI.Slot)
    val t = Tesselator.getInstance
    val r = t.getBuilder
    r.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
    r.vertex(stack.last.pose, x.toFloat, y.toFloat + 18, getBlitOffset.toFloat + 1).uv(0, 1).endVertex()
    r.vertex(stack.last.pose, x.toFloat + 18, y.toFloat + 18, getBlitOffset.toFloat + 1).uv(1, 1).endVertex()
    r.vertex(stack.last.pose, x.toFloat + 18, y.toFloat, getBlitOffset.toFloat + 1).uv(1, 0).endVertex()
    r.vertex(stack.last.pose, x.toFloat, y.toFloat, getBlitOffset.toFloat + 1).uv(0, 0).endVertex()
    t.end()
  }

  private def isInPlayerInventory(slot: Slot) = inventoryContainer match {
    case player: Player => slot.container == player.playerInventory
    case _ => false
  }

  protected def copiedDrawHoveringText(stack: PoseStack, lines: List[String], x: Int, y: Int, font: Font): Unit = {
    val text = new ArrayList[TextComponent]()
    for (line <- lines) {
      text.add(new TextComponent(line))
    }
    renderComponentTooltip(stack, text, x, y, font)
  }

  protected def isPointInRegion(rectX: Int, rectY: Int, rectWidth: Int, rectHeight: Int, pointX: Int, pointY: Int): Boolean =
    pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1
}
