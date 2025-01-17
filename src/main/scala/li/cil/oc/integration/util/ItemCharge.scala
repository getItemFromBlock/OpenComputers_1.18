package li.cil.oc.integration.util

import java.lang.reflect.Method

import li.cil.oc.common.IMC
import net.minecraft.world.item.ItemStack

import scala.collection.mutable

object ItemCharge {
  private val chargers = mutable.LinkedHashSet.empty[(Method, Method)]

  def add(canCharge: Method, charge: Method): Unit = chargers += ((canCharge, charge))

  def canCharge(stack: ItemStack): Boolean = !stack.isEmpty && chargers.exists(charger => IMC.tryInvokeStatic(charger._1, stack)(false))

  // Returns the amount of the delta that could not be applied.
  def charge(stack: ItemStack, amount: Double): Double = {
    if (!stack.isEmpty) chargers.find(charger => IMC.tryInvokeStatic(charger._1, stack)(false)) match {
      case Some(charger) => IMC.tryInvokeStatic(charger._2, stack, Double.box(amount), java.lang.Boolean.FALSE)(amount)
      case _ => amount
    }
    else amount
  }
}
