package li.cil.oc.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.systems.RenderSystem
import li.cil.oc.client.Textures
import li.cil.oc.common.Tier
import li.cil.oc.common.container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.network.chat.Component

class Database(state: container.Database, playerInventory: Inventory, name: Component)
  extends DynamicGuiContainer(state, playerInventory, name)
  with traits.LockedHotbar[container.Database] {

  imageHeight = 256

  override def lockedStack = inventoryContainer.container

  override protected def renderLabels(stack: PoseStack, mouseX: Int, mouseY: Int) =
    drawSecondaryForegroundLayer(stack, mouseX, mouseY)

  override def drawSecondaryForegroundLayer(stack: PoseStack, mouseX: Int, mouseY: Int): Unit = {}

  override protected def renderBg(stack: PoseStack, dt: Float, mouseX: Int, mouseY: Int): Unit = {
    //RenderSystem.color4f(1, 1, 1, 1)

    if (inventoryContainer.tier == Tier.One)
    {
      Textures.bind(Textures.GUI.Database)
      blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight)
    }
    else if (inventoryContainer.tier == Tier.Two) {
      Textures.bind(Textures.GUI.Database1)
      blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight)
    }
    else {
      Textures.bind(Textures.GUI.Database2)
      blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight)
    }
  }
}
