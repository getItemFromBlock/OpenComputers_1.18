package li.cil.oc.common.template

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.api.internal
import li.cil.oc.api.internal.Microcontroller
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import li.cil.oc.common.item.data.MicrocontrollerData
import li.cil.oc.util.ItemUtils
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack

import scala.collection.JavaConverters.asJavaIterable
import scala.collection.convert.ImplicitConversionsToJava._

object MicrocontrollerTemplate extends Template {
  override protected val suggestedComponents = Array(
    "BIOS" -> hasComponent("eeprom") _)

  override protected def hostClass: Class[Microcontroller] = classOf[internal.Microcontroller]

  def selectTier1(stack: ItemStack): Boolean = api.Items.get(stack) == api.Items.get(Constants.ItemName.MicrocontrollerCaseTier1)

  def selectTier2(stack: ItemStack): Boolean = api.Items.get(stack) == api.Items.get(Constants.ItemName.MicrocontrollerCaseTier2)

  def selectTierCreative(stack: ItemStack): Boolean = api.Items.get(stack) == api.Items.get(Constants.ItemName.MicrocontrollerCaseCreative)

  def validate(inventory: Container): Array[AnyRef] = validateComputer(inventory)

  def assemble(inventory: Container): Array[Object] = {
    val items = (0 until inventory.getContainerSize).map(inventory.getItem)
    val data = new MicrocontrollerData()
    data.tier = caseTier(inventory)
    data.components = items.drop(1).filter(!_.isEmpty).toArray
    data.storedEnergy = Settings.get.bufferMicrocontroller.toInt
    val stack = data.createItemStack()
    val energy = Settings.get.microcontrollerBaseCost + complexity(inventory) * Settings.get.microcontrollerComplexityCost

    Array(stack, Double.box(energy))
  }

  def selectDisassembler(stack: ItemStack): Boolean = api.Items.get(stack) == api.Items.get(Constants.BlockName.Microcontroller)

  def disassemble(stack: ItemStack, ingredients: Array[ItemStack]): Array[ItemStack] = {
    val info = new MicrocontrollerData(stack)
    val itemName = Constants.ItemName.MicrocontrollerCase(info.tier)

    Array(api.Items.get(itemName).createItemStack(1)) ++ info.components
  }

  def register() {
    // Tier 1
    api.IMC.registerAssemblerTemplate(
      "Microcontroller (Tier 1)",
      "li.cil.oc.common.template.MicrocontrollerTemplate.selectTier1",
      "li.cil.oc.common.template.MicrocontrollerTemplate.validate",
      "li.cil.oc.common.template.MicrocontrollerTemplate.assemble",
      hostClass,
      null,
      Array(
        Tier.Two
      ),
      asJavaIterable(Iterable(
        (Slot.Card, Tier.One),
        (Slot.Card, Tier.One),
        null,
        (Slot.CPU, Tier.One),
        (Slot.Memory, Tier.One),
        null,
        (Slot.EEPROM, Tier.Any)
      ).map(toPair)))

    // Tier 2
    api.IMC.registerAssemblerTemplate(
      "Microcontroller (Tier 2)",
      "li.cil.oc.common.template.MicrocontrollerTemplate.selectTier2",
      "li.cil.oc.common.template.MicrocontrollerTemplate.validate",
      "li.cil.oc.common.template.MicrocontrollerTemplate.assemble",
      hostClass,
      null,
      Array(
        Tier.Three
      ),
      asJavaIterable(Iterable(
        (Slot.Card, Tier.Two),
        (Slot.Card, Tier.One),
        null,
        (Slot.CPU, Tier.One),
        (Slot.Memory, Tier.One),
        (Slot.Memory, Tier.One),
        (Slot.EEPROM, Tier.Any)
      ).map(toPair)))

    // Creative
    api.IMC.registerAssemblerTemplate(
      "Microcontroller (Creative)",
      "li.cil.oc.common.template.MicrocontrollerTemplate.selectTierCreative",
      "li.cil.oc.common.template.MicrocontrollerTemplate.validate",
      "li.cil.oc.common.template.MicrocontrollerTemplate.assemble",
      hostClass,
      null,
      Array(
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three
      ),
      asJavaIterable(Iterable(
        (Slot.Card, Tier.Three),
        (Slot.Card, Tier.Three),
        (Slot.Card, Tier.Three),
        (Slot.CPU, Tier.Three),
        (Slot.Memory, Tier.Three),
        (Slot.Memory, Tier.Three),
        (Slot.EEPROM, Tier.Any)
      ).map(toPair)))

    // Disassembler
    api.IMC.registerDisassemblerTemplate(
      "Microcontroller",
      "li.cil.oc.common.template.MicrocontrollerTemplate.selectDisassembler",
      "li.cil.oc.common.template.MicrocontrollerTemplate.disassemble")
  }

  override protected def maxComplexity(inventory: Container): Int =
    if (caseTier(inventory) == Tier.Two) 5
    else if (caseTier(inventory) == Tier.Four) 9001 // Creative
    else 4

  override protected def caseTier(inventory: Container): Int = ItemUtils.caseTier(inventory.getItem(0))
}
