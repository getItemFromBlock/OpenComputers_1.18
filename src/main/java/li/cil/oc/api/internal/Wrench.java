package li.cil.oc.api.internal;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Implemented on items that are wrench-like tools.
 */
public interface Wrench {
    /**
     * Called when the wrench is used.
     * <br>
     * This is called in two scenarios, once when testing whether the wrench
     * can be used on a certain block, in which case the <tt>simulate</tt>
     * argument will be <tt>true</tt>, and once when actually used on a block,
     * in which case the <tt>simulate</tt> argument will be <tt>false</tt>,
     * allowing the tool to damage itself, for example.
     * <br>
     * This is usually called from blocks' activation logic.
     *
     * @param player   the player using the tool
     * @param world    the world containing the block the wrench is used on.
     * @param pos      the position of the block.
     * @param simulate whether to simulate the usage.
     * @return whether the wrench can be used on the block.
     */
    boolean useWrenchOnBlock(Player player, Level world, BlockPos pos, boolean simulate);
}
