package org.cyclops.integrateddynamics.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.RegistryEntries;

/**
 * Config for the {@link BlockEntityProxy}.
 * @author rubensworks
 *
 */
public class BlockEntityProxyConfig extends BlockEntityConfig<BlockEntityProxy> {

    public BlockEntityProxyConfig() {
        super(
                IntegratedDynamics._instance,
                "proxy",
                (eConfig) -> new BlockEntityType<>(BlockEntityProxy::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_PROXY.get()), null)
        );
        IntegratedDynamics._instance.getModEventBus().addListener(this::registerCapability);
    }

    protected void registerCapability(RegisterCapabilitiesEvent event) {
        BlockEntityProxy.registerProxyCapabilities(event, getInstance());
    }

}
