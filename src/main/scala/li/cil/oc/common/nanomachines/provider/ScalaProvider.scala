package li.cil.oc.common.nanomachines.provider

import li.cil.oc.api.nanomachines.Behavior
import li.cil.oc.api.prefab.AbstractProvider
import net.minecraft.world.entity.player.Player

import scala.collection.JavaConverters.asJavaIterable
import scala.collection.convert.ImplicitConversionsToJava._

abstract class ScalaProvider(id: String) extends AbstractProvider(id) {
  def createScalaBehaviors(player: Player): Iterable[Behavior]

  override def createBehaviors(player: Player): java.lang.Iterable[Behavior] = asJavaIterable(createScalaBehaviors(player))
}
