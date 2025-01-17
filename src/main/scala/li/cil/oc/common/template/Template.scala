package li.cil.oc.common.template

import li.cil.oc.Constants
import li.cil.oc.Localization
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import li.cil.oc.util.StackOption
import li.cil.oc.util.StackOption._
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component
import org.apache.commons.lang3.tuple

import scala.collection.mutable

abstract class Template {
  protected val suggestedComponents = Array(
    "BIOS" -> hasComponent(Constants.ItemName.EEPROM) _,
    "Screen" -> hasComponent(Constants.BlockName.ScreenTier1) _,
    "Keyboard" -> hasComponent(Constants.BlockName.Keyboard) _,
    "GraphicsCard" -> ((inventory: Container) => Array(
      Constants.ItemName.APUCreative,
      Constants.ItemName.APUTier1,
      Constants.ItemName.APUTier2,
      Constants.ItemName.GraphicsCardTier1,
      Constants.ItemName.GraphicsCardTier2,
      Constants.ItemName.GraphicsCardTier3).
      exists(name => hasComponent(name)(inventory))),
    "Inventory" -> hasInventory _,
    "OS" -> hasFileSystem _)

  protected def hostClass: Class[_ <: api.network.EnvironmentHost]

  protected def validateComputer(inventory: Container): Array[AnyRef] = {
    val hasCase = caseTier(inventory) != Tier.None
    val hasCPU = this.hasCPU(inventory)
    val hasRAM = this.hasRAM(inventory)
    val requiresRAM = this.requiresRAM(inventory)
    val complexity = this.complexity(inventory)
    val maxComplexity = this.maxComplexity(inventory)

    val valid = hasCase && hasCPU && (hasRAM || !requiresRAM) && complexity <= maxComplexity

    val progress =
      if (!hasCPU) Localization.Assembler.InsertCPU
      else if (!hasRAM && requiresRAM) Localization.Assembler.InsertRAM
      else Localization.Assembler.Complexity(complexity, maxComplexity)

    val warnings = mutable.ArrayBuffer.empty[Component]
    for ((name, check) <- suggestedComponents) {
      if (!check(inventory)) {
        warnings += Localization.Assembler.Warning(name)
      }
    }
    if (warnings.nonEmpty) {
      warnings.prepend(Localization.Assembler.Warnings)
    }

    Array(valid: java.lang.Boolean, progress, warnings.toArray)
  }

  protected def exists(inventory: Container, p: ItemStack => Boolean) = {
    (0 until inventory.getContainerSize).exists(slot => StackOption(inventory.getItem(slot)) match {
      case SomeStack(stack) => p(stack)
      case _ => false
    })
  }

  protected def hasCPU(inventory: Container) = exists(inventory, api.Driver.driverFor(_, hostClass) match {
    case _: api.driver.item.Processor => true
    case _ => false
  })

  protected def hasRAM(inventory: Container) = exists(inventory, api.Driver.driverFor(_, hostClass) match {
    case _: api.driver.item.Memory => true
    case _ => false
  })

  protected def requiresRAM(inventory: Container) = !(0 until inventory.getContainerSize).
    map(inventory.getItem).
    exists(stack => api.Driver.driverFor(stack, hostClass) match {
      case driver: api.driver.item.Processor =>
        val architecture = driver.architecture(stack)
        architecture != null && architecture.getAnnotation(classOf[api.machine.Architecture.NoMemoryRequirements]) != null
      case _ => false
    })

  protected def hasComponent(name: String)(inventory: Container) = exists(inventory, stack => Option(api.Items.get(stack)) match {
    case Some(descriptor) => descriptor.name == name
    case _ => false
  })

  protected def hasInventory(inventory: Container) = exists(inventory, api.Driver.driverFor(_, hostClass) match {
    case _: api.driver.item.Inventory => true
    case _ => false
  })

  protected def hasFileSystem(inventory: Container) = exists(inventory, stack => Option(api.Driver.driverFor(stack, hostClass)) match {
    case Some(driver) => driver.slot(stack) == Slot.Floppy || driver.slot(stack) == Slot.HDD
    case _ => false
  })

  protected def complexity(inventory: Container) = {
    var acc = 0
    for (slot <- 1 until inventory.getContainerSize) {
      val stack = inventory.getItem(slot)
      acc += (Option(api.Driver.driverFor(stack, hostClass)) match {
        case Some(driver: api.driver.item.Processor) => 0 // CPUs are exempt, since they control the limit.
        case Some(driver: api.driver.item.Container) => (1 + driver.tier(stack)) * 2
        case Some(driver) if driver.slot(stack) != Slot.EEPROM => 1 + driver.tier(stack)
        case _ => 0
      })
    }
    acc
  }

  protected def maxComplexity(inventory: Container) = {
    val caseTier = this.caseTier(inventory)
    val cpuTier = (0 until inventory.getContainerSize).foldRight(0)((slot, acc) => {
      val stack = inventory.getItem(slot)
      acc + (api.Driver.driverFor(stack, hostClass) match {
        case processor: api.driver.item.Processor => processor.tier(stack)
        case _ => 0
      })
    })
    if (caseTier >= Tier.One && cpuTier >= Tier.One) {
      Settings.deviceComplexityByTier(caseTier) - (math.min(2, caseTier) - cpuTier) * 6
    }
    else 0
  }

  protected def caseTier(inventory: Container): Int

  protected def toPair(t: (String, Int)): tuple.Pair[String, java.lang.Integer] =
    if (t == null) null
    else tuple.Pair.of(t._1, t._2)
}
