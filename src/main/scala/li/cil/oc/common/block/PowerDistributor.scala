package li.cil.oc.common.block

import li.cil.oc.common.tileentity
import net.minecraft.block.AbstractBlock.Properties
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level

class PowerDistributor(props: Properties) extends SimpleBlock(props) {
  override def newBlockEntity(world: BlockGetter) = new tileentity.PowerDistributor(tileentity.TileEntityTypes.POWER_DISTRIBUTOR)
}

