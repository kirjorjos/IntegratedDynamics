package org.cyclops.integrateddynamics.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.cyclops.integrateddynamics.Capabilities;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHolder;
import org.cyclops.integrateddynamics.client.render.blockentity.ItemStackBlockEntityVariableRender;
import org.cyclops.integrateddynamics.core.item.VariableFacadeHandlerRegistry;

import java.util.List;
import java.util.function.Consumer;

/**
 * Item for storing variable references.
 * @author rubensworks
 */
public class ItemVariable extends Item {

    public ItemVariable(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
        IVariableFacade variableFacade = getVariableFacade(ValueDeseralizationContext.ofClient(), itemStack);
        variableFacade.appendHoverText(list, context);
        if (variableFacade != VariableFacadeHandlerRegistry.DUMMY_FACADE && Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
            list.add(Component.translatable("item.integrateddynamics.variable.warning"));
        }
        super.appendHoverText(itemStack, context, list, flag);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        IVariableFacade variableFacade = getVariableFacade(ValueDeseralizationContext.ofAllEnabled(), itemStack);
        String label;
        if(variableFacade.isValid() && (label = variableFacade.getLabel()) != null) {
            return Component.literal(label)
                    .withStyle(ChatFormatting.ITALIC);
        }
        return super.getName(itemStack);
    }

    public IVariableFacade getVariableFacade(ValueDeseralizationContext valueDeseralizationContext, ItemStack itemStack) {
        IVariableFacadeHolder holder = itemStack.getCapability(Capabilities.VariableFacade.ITEM);
        if (holder != null) {
            return holder.getVariableFacade(valueDeseralizationContext);
        }
        return VariableFacadeHandlerRegistry.DUMMY_FACADE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new ItemStackBlockEntityVariableRender();
            }
        });
    }
}
