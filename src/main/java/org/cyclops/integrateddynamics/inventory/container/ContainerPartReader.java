package org.cyclops.integrateddynamics.inventory.container;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import org.cyclops.cyclopscore.inventory.IGuiContainerProvider;
import org.cyclops.integrateddynamics.core.inventory.container.ContainerMultipart;
import org.cyclops.integrateddynamics.core.part.IPartContainer;
import org.cyclops.integrateddynamics.core.part.IPartState;
import org.cyclops.integrateddynamics.core.part.PartTarget;
import org.cyclops.integrateddynamics.core.part.aspect.IAspectRead;
import org.cyclops.integrateddynamics.core.part.read.IPartTypeReader;

/**
 * Container for reader parts.
 * @author rubensworks
 */
public class ContainerPartReader<P extends IPartTypeReader<P, S> & IGuiContainerProvider, S extends IPartState<P>>
        extends ContainerMultipart<P, S, IAspectRead> {
    /**
     * Make a new instance.
     * @param partTarget    The target.
     * @param player        The player.
     * @param partContainer The part container.
     * @param partType      The part type.
     * @param partState     The part state.
     */
    public ContainerPartReader(EntityPlayer player, PartTarget partTarget, IPartContainer partContainer, P partType, S partState) {
        super(player, partTarget, partContainer, partType, partState, Lists.newArrayList(partType.getReadAspects()));
    }

}
