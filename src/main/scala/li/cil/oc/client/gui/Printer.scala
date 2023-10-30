package li.cil.oc.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.systems.RenderSystem
import li.cil.oc.client.Textures
import li.cil.oc.client.gui.widget.ProgressBar
import li.cil.oc.common.container
import li.cil.oc.common.container.ComponentSlot
import li.cil.oc.util.RenderState
import net.minecraft.world.entity.player.Inventory
import net.minecraft.network.chat.Component

class Printer(state: container.Printer, playerInventory: Inventory, name: Component)
  extends DynamicGuiContainer(state, playerInventory, name) {

  imageWidth = 176
  imageHeight = 166

  private val materialBar = addRenderableWidget(new ProgressBar(40, 21) {
    override def width = 62

    override def height = 12

    override def barTexture = Textures.GUI.PrinterMaterial
  })
  private val inkBar = addRenderableWidget(new ProgressBar(40, 53) {
    override def width = 62

    override def height = 12

    override def barTexture = Textures.GUI.PrinterInk
  })
  private val progressBar = addRenderableWidget(new ProgressBar(105, 20) {
    override def width = 46

    override def height = 46

    override def barTexture = Textures.GUI.PrinterProgress
  })

  override def drawSecondaryForegroundLayer(stack: PoseStack, mouseX: Int, mouseY: Int) = {
    super.drawSecondaryForegroundLayer(stack, mouseX, mouseY)
    RenderState.pushAttrib()
    if (isPointInRegion(materialBar.x, materialBar.y, materialBar.width, materialBar.height, mouseX - leftPos, mouseY - topPos)) {
      val tooltip = new java.util.ArrayList[String]
      tooltip.add(inventoryContainer.amountMaterial + "/" + inventoryContainer.maxAmountMaterial)
      copiedDrawHoveringText(stack, tooltip, mouseX - leftPos, mouseY - topPos, font)
    }
    if (isPointInRegion(inkBar.x, inkBar.y, inkBar.width, inkBar.height, mouseX - leftPos, mouseY - topPos)) {
      val tooltip = new java.util.ArrayList[String]
      tooltip.add(inventoryContainer.amountInk + "/" + inventoryContainer.maxAmountInk)
      copiedDrawHoveringText(stack, tooltip, mouseX - leftPos, mouseY - topPos, font)
    }
    RenderState.popAttrib()
  }

  override def renderBg(stack: PoseStack, dt: Float, mouseX: Int, mouseY: Int): Unit = {
    //RenderSystem.color3f(1, 1, 1)
    Textures.bind(Textures.GUI.Printer)
    blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight)
    materialBar.level = inventoryContainer.amountMaterial / inventoryContainer.maxAmountMaterial.toDouble
    inkBar.level = inventoryContainer.amountInk / inventoryContainer.maxAmountInk.toDouble
    progressBar.level = inventoryContainer.progress
    //drawWidgets(stack)
    drawInventorySlots(stack)
  }

  override protected def drawDisabledSlot(stack: PoseStack, slot: ComponentSlot): Unit = {}
}
