package li.cil.oc.client.gui.widget

import com.mojang.blaze3d.vertex.PoseStack

import scala.collection.mutable

@Deprecated
trait WidgetContainer {
  protected val widgets = mutable.ArrayBuffer.empty[Widget]

  def addCustomWidget[T <: Widget](widget: T) = {
    widgets += widget
    widget.owner = this
    widget
  }

  def windowX = 0

  def windowY = 0

  def windowZ = 0f

  def drawWidgets(stack: PoseStack): Unit = {
    widgets.foreach(_.draw(stack))
  }
}
