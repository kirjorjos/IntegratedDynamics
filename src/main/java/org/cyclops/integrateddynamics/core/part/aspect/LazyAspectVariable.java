package org.cyclops.integrateddynamics.core.part.aspect;

import lombok.Getter;
import lombok.NonNull;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.VariableAdapter;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.IPartState;
import org.cyclops.integrateddynamics.api.part.IPartType;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.api.part.PartTarget;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.api.part.aspect.IAspectVariable;
import org.cyclops.integrateddynamics.api.part.aspect.property.IAspectProperties;
import org.cyclops.integrateddynamics.core.helper.L10NValues;

import java.util.function.Supplier;

/**
 * Variable for a specific aspect from a part that calculates its target value only maximum once per ticking interval.
 * No calculations will be done if the value of this variable is not called.
 * @author rubensworks
 */
public abstract class LazyAspectVariable<V extends IValue> extends VariableAdapter<V> implements IAspectVariable<V> {

    @Getter private final IValueType<V> type;
    private final Supplier<PartTarget> targetSupplier;
    @Getter private final IAspectRead<V, ?> aspect;
    @NonNull private V value;
    private IAspectProperties cachedProperties = null;

    private boolean isGettingValue = false;

    public LazyAspectVariable(IValueType<V> type, Supplier<PartTarget> targetSupplier, IAspectRead<V, ?> aspect) {
        this.type = type;
        this.targetSupplier = targetSupplier;
        this.aspect = aspect;
    }

    @Deprecated // TODO: rm in next major
    public LazyAspectVariable(IValueType<V> type, PartTarget target, IAspectRead<V, ?> aspect) {
        this(type, () -> target, aspect);
    }

    public PartTarget getTarget() {
        return targetSupplier.get();
    }

    @Override
    public void invalidate() {
        if (value != null) {
            value = null;
            cachedProperties = null;
            super.invalidate();
        }
    }

    @Override
    public V getValue() throws EvaluationException {
        if(value == null) {
            if(this.isGettingValue) {
                throw new EvaluationException(Component.translatable(L10NValues.VARIABLE_ERROR_RECURSION,
                        Component.translatable(getAspect().getTranslationKey())));
            }
            this.isGettingValue = true;
            try {
                this.value = getValueLazy();
            } catch (EvaluationException e) {
                this.isGettingValue = false;
                throw e;
            }
            this.isGettingValue = false;
        }
        return this.value;
    }

    protected IAspectProperties getAspectProperties() {
        if(cachedProperties == null && getAspect().hasProperties()) {
            PartPos pos = getTarget().getCenter();
            Pair<IPartType, IPartState> partData = PartPos.getPartData(pos);
            if (partData != null) {
                cachedProperties = getAspect().getProperties(partData.getLeft(), getTarget(), partData.getRight());
            }
        }
        return cachedProperties;
    }

    /**
     * Calculate the current value for this variable.
     * It will only be called when required.
     * @return The current value of this variable.
     * @throws EvaluationException If evaluation has gone wrong.
     */
    public abstract V getValueLazy() throws EvaluationException;

}
