package li.cil.oc.server.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import li.cil.oc.api.internal.Colored;
import li.cil.oc.util.ItemColorizer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public final class CopyColor extends LootItemFunction {
    private CopyColor(LootItemConditions[] conditions) {
        super(conditions);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootFunctions.COPY_COLOR;
    }

    public static class Builder extends LootItemFunction.Builder<Builder> {
        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public ILootFunction build() {
            return new CopyColor(getConditions());
        }
    }

    public static Builder copyColor() {
        return new Builder();
    }

    @Override
    public ItemStack run(ItemStack stack, LootContext ctx) {
        if (stack.isEmpty()) return stack;
        BlockEntity te = ctx.getParamOrNull(LootParameters.BLOCK_ENTITY);
        if (te != null && te instanceof Colored) {
            // Can't use capability because it's already invalid - block breaks before drops are calculated.
            ItemColorizer.setColor(stack, ((Colored) te).getColor());
        }
        else ItemColorizer.removeColor(stack);
        return stack;
    }

    public static class Serializer extends LootItemFunction.Serializer<CopyColor> {
        @Override
        public CopyColor deserialize(JsonObject src, JsonDeserializationContext ctx, LootItemConditions[] conditions) {
            return new CopyColor(conditions);
        }
    }
}
