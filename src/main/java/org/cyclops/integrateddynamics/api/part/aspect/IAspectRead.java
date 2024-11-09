package org.cyclops.integrateddynamics.api.part.aspect;

import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.part.PartTarget;

import java.util.function.Supplier;

/**
 * An element that can be used inside parts to access a specific aspect of something to read.
 * @author rubensworks
 */
public interface IAspectRead<V extends IValue, T extends IValueType<V>> extends IAspect<V, T> {

    /**
     * Creates a new variable for this aspect.
     * @param target The target for this aspect.
     * @return The variable pointing to the given target.
     */
    @Deprecated // TODO: rm in next major
    public default IAspectVariable<V> createNewVariable(PartTarget target) {
        return this.createNewVariable(() -> target);
    }

    /**
     * Creates a new variable for this aspect.
     * @param targetSupplier The target supplier for this aspect.
     * @return The variable pointing to the given target.
     */
    public IAspectVariable<V> createNewVariable(Supplier<PartTarget> targetSupplier);

    /**
     * @return The update type on which this aspect should invalidate.
     */
    public AspectUpdateType getUpdateType();

}
