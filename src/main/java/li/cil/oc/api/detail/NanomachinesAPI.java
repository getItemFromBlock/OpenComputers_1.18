package li.cil.oc.api.detail;

import li.cil.oc.api.nanomachines.BehaviorProvider;
import li.cil.oc.api.nanomachines.Controller;
import net.minecraft.world.entity.player.Player;

public interface NanomachinesAPI {
    /**
     * Register a new behavior provider.
     * <br>
     * When a controller is reconfigured it will draw behaviors from all
     * registered providers and build a new random connection graph to
     * those behaviors.
     *
     * @param provider the provider to add.
     */
    void addProvider(BehaviorProvider provider);

    /**
     * Get a list of all currently registered providers.
     *
     * @return the list of all currently registered providers.
     */
    Iterable<BehaviorProvider> getProviders();

    /**
     * Check whether a player has a nanomachine controller installed.
     *
     * @param player the player to check for.
     * @return <tt>true</tt> if the player has a controller, <tt>false</tt> otherwise.
     */
    boolean hasController(Player player);

    /**
     * Get the nanomachine controller of the specified player.
     * <br>
     * If the player has a controller installed, this will initialize the
     * controller if it has not already been loaded. If the player has no
     * controller, this will return <tt>null</tt>.
     *
     * @param player the player to get the controller for.
     * @return the controller for the specified player.
     */
    Controller getController(Player player);

    /**
     * Install a controller for the specified player if it doesn't already
     * have one.
     * <br>
     * This will also initialize the controller if it has not already been
     * initialized.
     *
     * @param player the player to install a nanomachine controller for.
     * @return the controller for the specified player.
     */
    Controller installController(Player player);

    /**
     * Uninstall a controller from the specified player if it has one.
     * <br>
     * This will disable all active behaviors before disposing the controller.
     *
     * @param player the player to uninstall a nanomachine controller from.
     */
    void uninstallController(Player player);
}
