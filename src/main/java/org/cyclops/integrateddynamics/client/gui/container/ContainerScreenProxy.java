package org.cyclops.integrateddynamics.client.gui.container;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.core.client.gui.ContainerScreenActiveVariableBase;
import org.cyclops.integrateddynamics.inventory.container.ContainerProxy;

/**
 * Gui for the proxy.
 * @author rubensworks
 */
public class ContainerScreenProxy extends ContainerScreenActiveVariableBase<ContainerProxy> {

    private static final int ERROR_X = 110;
    private static final int ERROR_Y = 26;

    public ContainerScreenProxy(ContainerProxy container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/proxy.png");
    }

    @Override
    protected int getBaseYSize() {
        return 189;
    }

    @Override
    protected int getErrorX() {
        return ERROR_X;
    }

    @Override
    protected int getErrorY() {
        return ERROR_Y;
    }
}
