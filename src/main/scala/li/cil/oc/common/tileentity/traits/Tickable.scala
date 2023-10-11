package li.cil.oc.common.tileentity.traits

import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.core.BlockPos
import net.minecraft.world.Level
import net.minecraft.world.level.block.BaseEntityBlock

trait Tickable extends BaseEntityBlock
{

    override def newBlockEntity(pos: BlockPos, state: BlockState): Tickable =
    {
        new Tickable(pos, state)
    }

    override def getTicker(level: Level, state: BlockState, `type`: BlockEntityType[Tickable]): BlockEntityTicker[Tickable] =
    {
        if (level.isClientSide()) null else createTickerHelper(`type`, Tickable.get, Tickable.updateEntity)
    }
}
