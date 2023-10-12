package li.cil.oc.common.capabilities

import li.cil.oc.api
import li.cil.oc.api.network.Environment
import li.cil.oc.api.network.Message
import li.cil.oc.api.network.Node
import li.cil.oc.api.network.Visibility
import li.cil.oc.integration.Mods
import net.minecraft.nbt.Tag
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.common.util.NonNullSupplier

object CapabilityEnvironment {
  final val ProviderEnvironment = new ResourceLocation(Mods.IDs.OpenComputers, "environment")

  class Provider(val tileEntity: BlockEntity with Environment) extends ICapabilityProvider with NonNullSupplier[Provider] with Environment {
    private val wrapper = LazyOptional.of(this)

    def get = this

    def invalidate() = wrapper.invalidate

    override def getCapability[T](capability: Capability[T], facing: Direction): LazyOptional[T] = {
      if (capability == Capabilities.EnvironmentCapability) wrapper.cast[T]
      else LazyOptional.empty[T]
    }

    override def node = tileEntity.node

    override def onMessage(message: Message) = tileEntity.onMessage(message)

    override def onConnect(node: Node) = tileEntity.onConnect(node)

    override def onDisconnect(node: Node) = tileEntity.onDisconnect(node)
  }

  class DefaultImpl extends Environment {
    override val node = api.Network.newNode(this, Visibility.None).create()

    override def onMessage(message: Message): Unit = {}

    override def onConnect(node: Node): Unit = {}

    override def onDisconnect(node: Node): Unit = {}
  }

  class DefaultStorage extends Capability.IStorage[Environment] {
    override def writeNBT(capability: Capability[Environment], t: Environment, facing: Direction): Tag = {
      val node = t.node
      if (node != null) {
        val nbt = new CompoundTag()
        node.saveData(nbt)
        nbt
      }
      else null
    }

    override def readNBT(capability: Capability[Environment], t: Environment, facing: Direction, nbtBase: Tag): Unit = {
      nbtBase match {
        case nbt: CompoundTag =>
          val node = t.node
          if (node != null) node.loadData(nbt)
        case _ =>
      }
    }
  }

}
