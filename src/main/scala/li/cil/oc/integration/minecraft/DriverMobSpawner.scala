package li.cil.oc.integration.minecraft

import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.NamedBlock
import li.cil.oc.api.machine.Arguments
import li.cil.oc.api.machine.Callback
import li.cil.oc.api.machine.Context
import li.cil.oc.api.network.ManagedEnvironment
import li.cil.oc.api.prefab.DriverSidedTileEntity
import li.cil.oc.integration.ManagedTileEntityEnvironment
import li.cil.oc.util.ResultWrapper.result
import net.minecraft.world.level.block.Block
import net.minecraft.block.Blocks
import net.minecraft.world.item.ItemStack
import net.minecraft.tileentity.MobSpawnerTileEntity
import net.minecraft.core.Direction
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

object DriverMobSpawner extends DriverSidedTileEntity {
  override def getTileEntityClass: Class[_] = classOf[MobSpawnerTileEntity]

  override def createEnvironment(world: Level, pos: BlockPos, side: Direction): ManagedEnvironment =
    new Environment(world.getBlockEntity(pos).asInstanceOf[MobSpawnerTileEntity])

  final class Environment(tileEntity: MobSpawnerTileEntity) extends ManagedTileEntityEnvironment[MobSpawnerTileEntity](tileEntity, "mob_spawner") with NamedBlock {
    override def preferredName = "mob_spawner"

    override def priority = 0

    @Callback(doc = "function():string -- Get the name of the entity that is being spawned by this spawner.")
    def getSpawningMobName(context: Context, args: Arguments): Array[AnyRef] = {
      result(tileEntity.getSpawner.getEntityId)
    }
  }

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] = {
      if (!stack.isEmpty && Block.byItem(stack.getItem) == Blocks.SPAWNER)
        classOf[Environment]
      else null
    }
  }

}
