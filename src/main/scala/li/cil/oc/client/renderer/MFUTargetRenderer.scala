package li.cil.oc.client.renderer

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexConsumer
import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.util.BlockPosition
import li.cil.oc.util.RenderState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.InteractionHand
import net.minecraft.resources.ResourceLocation
import com.mojang.math.Vector4f
import com.mojang.math.Matrix4f
import net.minecraftforge.client.event.RenderLevelStageEvent
import net.minecraft.nbt.Tag
import net.minecraftforge.eventbus.api.SubscribeEvent

object MFUTargetRenderer {
  private val (drawRed, drawGreen, drawBlue) = (0.0f, 1.0f, 0.0f)

  private lazy val mfu = api.Items.get(Constants.ItemName.MFU)

  @SubscribeEvent
  def onRenderLevelStageEvent(e: RenderLevelStageEvent): Unit = {
    // TODO : check if this is the correct stage, because old class was using RenderWorldLastEvent which does not completly translate to this
    if (e.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
      val mc = Minecraft.getInstance
      val player = mc.player
      if (player == null) return
      player.getItemInHand(InteractionHand.MAIN_HAND) match {
        case stack: ItemStack if api.Items.get(stack) == mfu && stack.hasTag =>
          val data = stack.getTag
          if (data.contains(Settings.namespace + "coord", Tag.TAG_INT_ARRAY)) {
            val dimension = new ResourceLocation(data.getString(Settings.namespace + "dimension"))
            if (!player.level.dimension.location.equals(dimension)) return
            val Array(x, y, z, side) = data.getIntArray(Settings.namespace + "coord")
            if (player.distanceToSqr(x, y, z) > 64 * 64) return
  
            val bounds = BlockPosition(x, y, z).bounds.inflate(0.1, 0.1, 0.1)
  
            RenderState.checkError(getClass.getName + ".onRenderLevelStageEvent: entering (aka: wasntme)")
  
            val matrix = e.getPoseStack
            matrix.pushPose()
            val camPos = Minecraft.getInstance.gameRenderer.getMainCamera.getPosition
            matrix.translate(-camPos.x, -camPos.y, -camPos.z)
  
            RenderSystem.disableDepthTest() // Default state for depth test is disabled, but it's enabled here so we have to change it manually.
            val buffer = Minecraft.getInstance.renderBuffers.bufferSource
            drawBox(matrix.last.pose, buffer.getBuffer(RenderTypes.MFU_LINES), bounds.minX.toFloat, bounds.minY.toFloat, bounds.minZ.toFloat,
              bounds.maxX.toFloat, bounds.maxY.toFloat, bounds.maxZ.toFloat, drawRed, drawGreen, drawBlue)
            drawFace(matrix.last.pose, buffer.getBuffer(RenderTypes.MFU_QUADS), bounds.minX.toFloat, bounds.minY.toFloat, bounds.minZ.toFloat,
              bounds.maxX.toFloat, bounds.maxY.toFloat, bounds.maxZ.toFloat, side, drawRed, drawGreen, drawBlue)
            buffer.endBatch()
  
            matrix.popPose()
  
            RenderState.checkError(getClass.getName + ".onRenderLevelStageEvent: leaving")
          }
        case _ => // Nothing
      }
    }
  }

  def drawBox(matrix: Matrix4f, builder: VertexConsumer, minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, r: Float, g: Float, b: Float): Unit = {
    // Bottom square.
    builder.vertex(matrix, minX, minY, minZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, minX, minY, maxZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, minX, minY, maxZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, maxX, minY, maxZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, maxX, minY, maxZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, maxX, minY, minZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, maxX, minY, minZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, minX, minY, minZ).color(r, g, b, 0.5f).endVertex()

    // Vertical bars.
    builder.vertex(matrix, minX, minY, minZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, minX, maxY, minZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, maxX, minY, minZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, maxX, maxY, minZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, maxX, minY, maxZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, minX, minY, maxZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, minX, maxY, maxZ).color(r, g, b, 0.5f).endVertex()

    // Top square.
    builder.vertex(matrix, maxX, maxY, minZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, minX, maxY, maxZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, minX, maxY, maxZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, minX, maxY, minZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, minX, maxY, minZ).color(r, g, b, 0.5f).endVertex()
    builder.vertex(matrix, maxX, maxY, minZ).color(r, g, b, 0.5f).endVertex()
  }

  private def drawFace(matrix: Matrix4f, builder: VertexConsumer, minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float, side: Int, r: Float, g: Float, b: Float): Unit = {
    side match {
      case 0 => // Down
        builder.vertex(matrix, minX, minY, minZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, minX, minY, maxZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, maxX, minY, maxZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, maxX, minY, minZ).color(r, g, b, 0.25f).endVertex()
      case 1 => // Up
        builder.vertex(matrix, maxX, maxY, minZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, minX, maxY, maxZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, minX, maxY, minZ).color(r, g, b, 0.25f).endVertex()
      case 2 => // North
        builder.vertex(matrix, minX, minY, minZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, maxX, minY, minZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, maxX, maxY, minZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, minX, maxY, minZ).color(r, g, b, 0.25f).endVertex()
      case 3 => // South
        builder.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, maxX, minY, maxZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, minX, minY, maxZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, minX, maxY, maxZ).color(r, g, b, 0.25f).endVertex()
      case 4 => // East
        builder.vertex(matrix, minX, minY, minZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, minX, maxY, minZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, minX, maxY, maxZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, minX, minY, maxZ).color(r, g, b, 0.25f).endVertex()
      case 5 => // West
        builder.vertex(matrix, maxX, minY, minZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, maxX, minY, maxZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, 0.25f).endVertex()
        builder.vertex(matrix, maxX, maxY, minZ).color(r, g, b, 0.25f).endVertex()
      case _ => // WTF?
    }
  }

}
