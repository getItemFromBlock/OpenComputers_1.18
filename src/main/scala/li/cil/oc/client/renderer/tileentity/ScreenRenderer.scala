package li.cil.oc.client.renderer.tileentity

import java.util.function.Function

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.IVertexBuilder
import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.detail.ItemInfo
import li.cil.oc.client.Textures
import li.cil.oc.client.renderer.RenderTypes
import li.cil.oc.common.tileentity.Screen
import li.cil.oc.integration.util.Wrench
import li.cil.oc.util.RenderState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
import net.minecraft.world.item.ItemStack
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import com.mojang.math.Vector3f

object ScreenRenderer extends Function[BlockEntityRenderDispatcher, ScreenRenderer] {
  override def apply(dispatch: BlockEntityRenderDispatcher) = new ScreenRenderer(dispatch)
}

class ScreenRenderer(dispatch: BlockEntityRenderDispatcher) extends BlockEntityRenderer[Screen](dispatch) {
  private val maxRenderDistanceSq = Settings.get.maxScreenTextRenderDistance * Settings.get.maxScreenTextRenderDistance

  private val fadeDistanceSq = Settings.get.screenTextFadeStartDistance * Settings.get.screenTextFadeStartDistance

  private val fadeRatio = 1.0 / (maxRenderDistanceSq - fadeDistanceSq)

  private var screen: Screen = null

  // ----------------------------------------------------------------------- //
  // Rendering
  // ----------------------------------------------------------------------- //

  override def render(screen: Screen, dt: Float, stack: PoseStack, buffer: MultiBufferSource, light: Int, overlay: Int) {
    RenderState.checkError(getClass.getName + ".render: entering (aka: wasntme)")

    this.screen = screen
    if (!screen.isOrigin) {
      return
    }

    val distance = playerDistanceSq() / math.min(screen.width, screen.height)
    if (distance > maxRenderDistanceSq) {
      return
    }

    val eye_pos = Minecraft.getInstance.player.getEyePosition(dt)
    val eye_delta: Double = screen.getBlockPos.getY - eye_pos.y

    // Crude check whether screen text can be seen by the local player based
    // on the player's position -> angle relative to screen.
    val screenFacing = screen.facing.getOpposite
    val x = screen.getBlockPos.getX - eye_pos.x
    val z = screen.getBlockPos.getZ - eye_pos.z
    if (screenFacing.getStepX * (x + 0.5) + screenFacing.getStepY * (eye_delta + 0.5) + screenFacing.getStepZ * (z + 0.5) < 0) {
      return
    }

    // RenderSystem.color4f(1, 1, 1, 1)

    stack.pushPose()

    stack.translate(0.5, 0.5, 0.5)

    RenderState.checkError(getClass.getName + ".render: setup")

    drawOverlay(stack, buffer.getBuffer(RenderTypes.BLOCK_OVERLAY))

    RenderState.checkError(getClass.getName + ".render: overlay")

    val alpha = if (distance > fadeDistanceSq) math.max(0, 1 - ((distance - fadeDistanceSq) * fadeRatio).toFloat) else 1f

    RenderState.checkError(getClass.getName + ".render: fade")

    if (screen.buffer.isRenderingEnabled) {
      val profiler = Minecraft.getInstance.getProfiler
      profiler.push("opencomputers:screen_text")
      draw(stack, alpha, buffer)
      profiler.pop()
    }

    stack.popPose()

    RenderState.checkError(getClass.getName + ".render: leaving")
  }

  private def transform(stack: PoseStack) {
    screen.yaw match {
      case Direction.WEST => stack.mulPose(Vector3f.YP.rotationDegrees(-90))
      case Direction.NORTH => stack.mulPose(Vector3f.YP.rotationDegrees(180))
      case Direction.EAST => stack.mulPose(Vector3f.YP.rotationDegrees(90))
      case _ => // No yaw.
    }
    screen.pitch match {
      case Direction.DOWN => stack.mulPose(Vector3f.XP.rotationDegrees(90))
      case Direction.UP => stack.mulPose(Vector3f.XP.rotationDegrees(-90))
      case _ => // No pitch.
    }

    // Fit area to screen (bottom left = bottom left).
    stack.translate(-0.5f, -0.5f, 0.5f)
    stack.translate(0, screen.height, 0)

    // Flip text upside down.
    stack.scale(1, -1, 1)
  }

  private def isScreen(stack: ItemStack): Boolean = api.Items.get(stack) match {
    case i: ItemInfo => i.block() match {
      case _: li.cil.oc.common.block.Screen => true
      case _ => false
    }
    case _ => false
  }

  private def drawOverlay(matrix: PoseStack, r: IVertexBuilder) = if (screen.facing == Direction.UP || screen.facing == Direction.DOWN) {
    // Show up vector overlay when holding same screen block.
    val stack = Minecraft.getInstance.player.getItemInHand(Hand.MAIN_HAND)
    if (!stack.isEmpty) {
      if (Wrench.holdsApplicableWrench(Minecraft.getInstance.player, screen.getBlockPos) || isScreen(stack)) {
        matrix.pushPose()
        transform(matrix)
        matrix.translate(screen.width / 2f - 0.5f, screen.height / 2f - 0.5f, 0.05f)

        val icon = Textures.getSprite(Textures.Block.ScreenUpIndicator)
        r.vertex(matrix.last.pose, 0, 1, 0).uv(icon.getU0, icon.getV1).endVertex()
        r.vertex(matrix.last.pose, 1, 1, 0).uv(icon.getU1, icon.getV1).endVertex()
        r.vertex(matrix.last.pose, 1, 0, 0).uv(icon.getU1, icon.getV0).endVertex()
        r.vertex(matrix.last.pose, 0, 0, 0).uv(icon.getU0, icon.getV0).endVertex()

        matrix.popPose()
      }
    }
  }

  private def draw(stack: PoseStack, alpha: Float, buffer: MultiBufferSource) {
    RenderState.checkError(getClass.getName + ".draw: entering (aka: wasntme)")

    val sx = screen.width
    val sy = screen.height
    val tw = sx * 16f
    val th = sy * 16f

    transform(stack)

    // Offset from border.
    stack.translate(sx * 2.25f / tw, sy * 2.25f / th, 0)

    // Inner size (minus borders).
    val isx = sx - (4.5f / 16)
    val isy = sy - (4.5f / 16)

    // Scale based on actual buffer size.
    val sizeX = screen.buffer.renderWidth
    val sizeY = screen.buffer.renderHeight
    val scaleX = isx / sizeX
    val scaleY = isy / sizeY
    if (true) {
      if (scaleX > scaleY) {
        stack.translate(sizeX * 0.5f * (scaleX - scaleY), 0, 0)
        stack.scale(scaleY, scaleY, 1)
      }
      else {
        stack.translate(0, sizeY * 0.5f * (scaleY - scaleX), 0)
        stack.scale(scaleX, scaleX, 1)
      }
    }
    else {
      // Stretch to fit.
      stack.scale(scaleX, scaleY, 1)
    }

    // Slightly offset the text so it doesn't clip into the screen.
    stack.translate(0, 0, 0.01)

    RenderState.checkError(getClass.getName + ".draw: setup")

    // Render the actual text.
    screen.buffer.renderText(stack)

    RenderState.checkError(getClass.getName + ".draw: text")
  }

  private def playerDistanceSq() = {
    val player = Minecraft.getInstance.player
    val bounds = screen.getRenderBoundingBox

    val px = player.getX
    val py = player.getY
    val pz = player.getZ

    val ex = bounds.maxX - bounds.minX
    val ey = bounds.maxY - bounds.minY
    val ez = bounds.maxZ - bounds.minZ
    val cx = bounds.minX + ex * 0.5
    val cy = bounds.minY + ey * 0.5
    val cz = bounds.minZ + ez * 0.5
    val dx = px - cx
    val dy = py - cy
    val dz = pz - cz

    (if (dx < -ex) {
      val d = dx + ex
      d * d
    }
    else if (dx > ex) {
      val d = dx - ex
      d * d
    }
    else 0) + (if (dy < -ey) {
      val d = dy + ey
      d * d
    }
    else if (dy > ey) {
      val d = dy - ey
      d * d
    }
    else 0) + (if (dz < -ez) {
      val d = dz + ez
      d * d
    }
    else if (dz > ez) {
      val d = dz - ez
      d * d
    }
    else 0)
  }
}
