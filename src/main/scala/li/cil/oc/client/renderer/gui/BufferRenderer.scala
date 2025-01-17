package li.cil.oc.client.renderer.gui

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import li.cil.oc.api
import li.cil.oc.client.Textures
import li.cil.oc.util.RenderState
import net.minecraft.client.renderer.MultiBufferSource
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.math.Matrix4f
import org.lwjgl.opengl.GL11

object BufferRenderer {
  val margin = 7

  val innerMargin = 1

  def drawBackground(stack: PoseStack, bufferWidth: Int, bufferHeight: Int, forRobot: Boolean = false) = {
    RenderState.checkError(getClass.getName + ".drawBackground: entering (aka: wasntme)")

    val innerWidth = innerMargin * 2 + bufferWidth
    val innerHeight = innerMargin * 2 + bufferHeight

    val t = Tesselator.getInstance
    val r = t.getBuilder
    Textures.bind(Textures.GUI.Borders)
    r.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

    val margin = if (forRobot) 2 else 7
    val (c0, c1, c2, c3) = if (forRobot) (5, 7, 9, 11) else (0, 7, 9, 16)

    // Top border (left corner, middle bar, right corner).
    drawQuad(stack.last.pose, r,
      0, 0, margin.toFloat, margin.toFloat,
      c0.toFloat, c0.toFloat, c1.toFloat, c1.toFloat)
    drawQuad(stack.last.pose, r,
      margin.toFloat, 0, innerWidth.toFloat, margin.toFloat,
      c1 + 0.25f, c0.toFloat, c2 - 0.25f, c1.toFloat)
    drawQuad(stack.last.pose, r,
      margin + innerWidth.toFloat, 0, margin.toFloat, margin.toFloat,
      c2.toFloat, c0.toFloat, c3.toFloat, c1.toFloat)

    // Middle area (left bar, screen background, right bar).
    drawQuad(stack.last.pose, r,
      0, margin.toFloat, margin.toFloat, innerHeight.toFloat,
      c0.toFloat, c1 + 0.25f, c1.toFloat, c2 - 0.25f)
    drawQuad(stack.last.pose, r,
      margin.toFloat, margin.toFloat, innerWidth.toFloat, innerHeight.toFloat,
      c1 + 0.25f, c1 + 0.25f, c2 - 0.25f, c2 - 0.25f)
    drawQuad(stack.last.pose, r,
      margin + innerWidth.toFloat, margin.toFloat, margin.toFloat, innerHeight.toFloat,
      c2.toFloat, c1 + 0.25f, c3.toFloat, c2 - 0.25f)

    // Bottom border (left corner, middle bar, right corner).
    drawQuad(stack.last.pose, r,
      0, margin + innerHeight.toFloat, margin.toFloat, margin.toFloat,
      c0.toFloat, c2.toFloat, c1.toFloat, c3.toFloat)
    drawQuad(stack.last.pose, r,
      margin.toFloat, margin + innerHeight.toFloat, innerWidth.toFloat, margin.toFloat,
      c1 + 0.25f, c2.toFloat, c2 - 0.25f, c3.toFloat)
    drawQuad(stack.last.pose, r,
      margin + innerWidth.toFloat, margin + innerHeight.toFloat, margin.toFloat, margin.toFloat,
      c2.toFloat, c2.toFloat, c3.toFloat, c3.toFloat)

    t.end()

    RenderState.checkError(getClass.getName + ".drawBackground: leaving")
  }

  private def drawQuad(matrix: Matrix4f, builder: VertexConsumer, x: Float, y: Float, w: Float, h: Float, u1: Float, v1: Float, u2: Float, v2: Float) = {
    val u1f = u1 / 16f
    val u2f = u2 / 16f
    val v1f = v1 / 16f
    val v2f = v2 / 16f
    builder.vertex(matrix, x, y + h, 0).uv(u1f, v2f).endVertex()
    builder.vertex(matrix, x + w, y + h, 0).uv(u2f, v2f).endVertex()
    builder.vertex(matrix, x+ w, y, 0).uv(u2f, v1f).endVertex()
    builder.vertex(matrix, x, y, 0).uv(u1f, v1f).endVertex()
  }

  def drawText(stack: PoseStack, screen: api.internal.TextBuffer) = screen.renderText(stack)
}
