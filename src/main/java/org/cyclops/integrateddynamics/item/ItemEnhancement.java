package org.cyclops.integrateddynamics.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.cyclops.integrateddynamics.GeneralConfig;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;

import java.util.List;
import java.util.Objects;

/**
 * An enhancement item.
 * @author rubensworks
 */
public class ItemEnhancement extends Item {

    private final Type type;

    public ItemEnhancement(Type type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public <P extends IPartType<P, S>, S extends IPartState<P>> InteractionResult applyEnhancement(IPartType<P, S> partType, IPartState<P> partState, ItemStack itemStack, Player player, InteractionHand hand) {
        switch (this.type) {
            case OFFSET -> {
                if (partType.supportsOffsets()) {
                    int value = getEnhancementValue(itemStack);
                    int newValue = partState.getMaxOffset() + value;
                    if (newValue < GeneralConfig.maxPartOffset) {
                        if (!player.level().isClientSide()) {
                            partState.setMaxOffset(newValue);
                        }
                        itemStack.shrink(1);
                        player.displayClientMessage(Component.translatable("item.integrateddynamics.enhancement_offset.increased", newValue), true);
                        return InteractionResult.SUCCESS;
                    }
                    player.displayClientMessage(Component.translatable("item.integrateddynamics.enhancement_offset.limit", GeneralConfig.maxPartOffset), true);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    public int getEnhancementValue(ItemStack itemStack) {
        return Objects.requireNonNullElse(itemStack.get(RegistryEntries.DATACOMPONENT_PART_ENHANCEMENT), 0);
    }

    public void setEnhancementValue(ItemStack itemStack, int value) {
        itemStack.set(RegistryEntries.DATACOMPONENT_PART_ENHANCEMENT, value);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable("item.integrateddynamics.enhancement_offset.tooltip", getEnhancementValue(itemStack)).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(itemStack, context, list, flag);
    }

    public static enum Type {
        OFFSET
    }

}
