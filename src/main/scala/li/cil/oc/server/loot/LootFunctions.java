package li.cil.oc.server.loot;

import li.cil.oc.OpenComputers;
import net.minecraft.core.Registry;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.loot.functions.ILootFunction;

public final class LootFunctions {
    public static final ResourceLocation DYN_ITEM_DATA = new ResourceLocation(OpenComputers.ID(), "item_data");
    public static final ResourceLocation DYN_VOLATILE_CONTENTS = new ResourceLocation(OpenComputers.ID(), "volatile_contents");

    public static final LootItemFunctionType SET_COLOR = register("set_color", new SetColor.Serializer());
    public static final LootItemFunctionType COPY_COLOR = register("copy_color", new CopyColor.Serializer());

    private static LootItemFunctionType register(String name, ILootSerializer<? extends ILootFunction> serializer) {
        LootItemFunctionType type = new LootItemFunctionType(serializer);
        Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(OpenComputers.ID(), name), type);
        return type;
    }

    public static final void init() {
        // No registry events or ObjectHolder - this is to load the class.
    }

    private LootFunctions() {
    }
}
