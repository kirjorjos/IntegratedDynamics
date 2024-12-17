package org.cyclops.integrateddynamics.gametest;

import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IAspectVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspect;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectWrite;
import org.cyclops.integrateddynamics.api.part.write.IPartStateWriter;
import org.cyclops.integrateddynamics.api.part.write.IPartTypeWriter;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.item.AspectVariableFacade;
import org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElement;
import org.cyclops.integrateddynamics.part.PartTypePanelDisplay;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Objects;

/**
 * @author rubensworks
 */
public class GameTestHelpersIntegratedDynamics {

    public static void assertValueEqual(IValue value1, IValue value2) {
        if (!Objects.equals(value1, value2)) {
            throw new GameTestAssertException("Value is incorrect");
        }
    }

    public static ItemStack createVariableForOperator(Level level, IOperator operator, int[] variableIds) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_VARIABLE);
        return registry.writeVariableFacadeItem(true, itemStack, Operators.REGISTRY,
                new OperatorLPElement.OperatorVariableFacadeFactory(operator, variableIds), level, null, RegistryEntries.BLOCK_LOGIC_PROGRAMMER.get().defaultBlockState());
    }

    public static IVariableFacade getVariableFacade(Level level, ItemStack itemStack) {
        return RegistryEntries.ITEM_VARIABLE.get().getVariableFacade(ValueDeseralizationContext.of(level), itemStack);
    }

    public static ItemStack createVariableFromReader(Level level, PartPos partPos, final IAspect aspect) {
        return createVariableFromReader(level, aspect, PartHelpers.getPart(partPos).getState());
    }

    public static ItemStack createVariableFromReader(Level level, final IAspect aspect, IPartState partState) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableFacadeHandlerRegistry.class);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_VARIABLE);
        return registry.writeVariableFacadeItem(true, itemStack, Aspects.REGISTRY, new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<>() {
            @Override
            public IAspectVariableFacade create(boolean generateId) {
                return new AspectVariableFacade(generateId, partState.getId(), aspect);
            }

            @Override
            public IAspectVariableFacade create(int id) {
                return new AspectVariableFacade(id, partState.getId(), aspect);
            }
        }, level, null, null);
    }

    public static void placeVariableInWriter(Level level, PartPos partPos, final IAspectWrite<ValueTypeInteger.ValueInteger, ValueTypeInteger> aspect, ItemStack variableAspect) {
        PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(partPos);
        IPartTypeWriter<?, ?> part = (IPartTypeWriter<?, ?>) partStateHolder.getPart();
        IPartStateWriter<?> state = (IPartStateWriter<?>) partStateHolder.getState();

        // Find aspect index
        int aspectIndex = -1;
        for (int i = 0; i < part.getWriteAspects().size(); i++) {
            if (part.getWriteAspects().get(i) == aspect) {
                aspectIndex = i;
            }
        }
        if (aspectIndex < 0) {
            throw new GameTestAssertException("Aspect " + aspect + " not found");
        }

        // Insert variable
        state.getInventory().setItem(aspectIndex, variableAspect);

        // Activate aspect
        ((IPartTypeWriter) part).updateActivation(PartTarget.fromCenter(partPos), state, null);
    }

    public static Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> placeVariableInDisplayPanel(Level level, PartPos partPos, ItemStack variableAspect) {
        PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(partPos);
        PartTypePanelDisplay part = (PartTypePanelDisplay) partStateHolder.getPart();
        PartTypePanelDisplay.State state = (PartTypePanelDisplay.State) partStateHolder.getState();

        // Insert variable
        state.getInventory().setItem(0, variableAspect);

        return Pair.of(part, state);
    }

}
