package li.cil.oc.client.gui.widget

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.components.Widget
import li.cil.oc.client.Textures
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import net.minecraft.network.chat.TextComponent
import org.lwjgl.opengl.GL11

class ProgressBar(val x: Int, val y: Int) extends net.minecraft.client.gui.components.AbstractWidget(x, y, 140, 12, TextComponent.EMPTY) {

  override def width = 140

  override def height = 12

  def barTexture = Textures.GUI.Bar

  var level = 0.0

  override def render(stack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    if (level > 0) {
      val u0 = 0
      val u1 = level.toFloat
      val v0 = 0
      val v1 = 1
      val w = (width * level).toFloat

      Textures.bind(barTexture)
      val t = Tesselator.getInstance
      val r = t.getBuilder
      r.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
      r.vertex(stack.last.pose, x.toFloat, y.toFloat, 0).uv(u0.toFloat, v0.toFloat).endVertex()
      r.vertex(stack.last.pose, x.toFloat, y.toFloat + height, 0).uv(u0.toFloat, v1.toFloat).endVertex()
      r.vertex(stack.last.pose, x.toFloat + w, y.toFloat + height, 0).uv(u1.toFloat, v1.toFloat).endVertex()
      r.vertex(stack.last.pose, x.toFloat + w, y.toFloat, 0).uv(u1.toFloat, v0.toFloat).endVertex()
      r.end()
      BufferUploader.end(r)
    }
  }
}
