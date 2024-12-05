package org.cyclops.integrateddynamics.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.extendedconfig.BlockClientConfig;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.item.ItemBlockCable;
import org.jetbrains.annotations.Nullable;

/**
 * Config for {@link BlockCable}.
 * @author rubensworks
 */
public class BlockCableConfig extends BlockConfigCommon<IntegratedDynamics> {

    @ConfigurablePropertyCommon(category = "machine", comment = "If cable shapes should be determined dynamically. Disable this if FPS issues would occur.", minimalValue = 0)
    public static boolean dynamicShape = true;

    public BlockCableConfig() {
        super(
                IntegratedDynamics._instance,
                "cable",
                eConfig -> new BlockCable(Block.Properties.of()
                        .strength(BlockCable.BLOCK_HARDNESS)
                        .forceSolidOn()
                        .sound(SoundType.METAL)
                        .isRedstoneConductor((blockState, world, pos) -> false)),
                (eConfig, block) -> new ItemBlockCable(block, new Item.Properties())
        );
    }

    @Override
    @Nullable
    public BlockClientConfig<IntegratedDynamics> constructBlockClientConfig() {
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            return new BlockCableClientConfig(this);
        }
        return null;
    }

}
