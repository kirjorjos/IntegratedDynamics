package org.cyclops.integrateddynamics.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.cyclops.cyclopscore.client.particle.ParticleBlurData;
import org.cyclops.integrateddynamics.Reference;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsNetwork {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(1, 1, 1);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkSingle(GameTestHelper helper) {
        // Place cables directly
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.south(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.south().east(), RegistryEntries.BLOCK_CABLE.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south().east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");
            helper.assertTrue(network3 == network4, "Networks of connected cables are not equal");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkSingleByPlayer(GameTestHelper helper) {
        // Place cables as player
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.BLOCK_CABLE.value(), 4);
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);

        helper.placeAt(player, itemStack, POS.south(), Direction.NORTH);
        helper.placeAt(player, itemStack, POS.south().south(), Direction.NORTH);
        helper.placeAt(player, itemStack, POS.south().east(), Direction.NORTH);
        helper.placeAt(player, itemStack, POS.south().south().east(), Direction.NORTH);

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south()), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.east()), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.south().east()), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of connected cables are not equal");
            helper.assertTrue(network3 == network4, "Networks of connected cables are not equal");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkTwo(GameTestHelper helper) {
        // Player two networks with empty space inbetween
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(1, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(3, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(4, 0, 0), RegistryEntries.BLOCK_CABLE.value());

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0)), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0)), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(4, 0, 0)), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of disconnected cables are equal");
            helper.assertTrue(network3 == network4, "Networks of connected cables are not equal");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkTwoDisconnectedByWrench(GameTestHelper helper) {
        // Place two networks directly next to each other
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(1, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(2, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(3, 0, 0), RegistryEntries.BLOCK_CABLE.value());

        // And disconnect them using the wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_WRENCH.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.setPos(helper.absolutePos(POS.offset(1, 0, 0)).getCenter().add(0.25, -1.5, -0.5));
        helper.getBlockState(POS.offset(1, 0, 0)).useWithoutItem(helper.getLevel(), player,
                new BlockHitResult(
                        helper.absolutePos(POS.offset(1, 0, 0)).getCenter(),
                        Direction.NORTH,
                        helper.absolutePos(POS.offset(1, 0, 0)),
                        false)
        );

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0)), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(2, 0, 0)), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0)), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 != network3, "Networks of wrench-disconnected cables are equal");
            helper.assertTrue(network3 == network4, "Networks of connected cables are not equal");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testNetworkTwoDisconnectedAndReconnectedByWrench(GameTestHelper helper) {
        // Place two networks directly next to each other
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(1, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(2, 0, 0), RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.offset(3, 0, 0), RegistryEntries.BLOCK_CABLE.value());

        // And disconnect them using the wrench
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack itemStack = new ItemStack(RegistryEntries.ITEM_WRENCH.value());
        player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        player.setPos(helper.absolutePos(POS.offset(1, 0, 0)).getCenter().add(0.25, -1.5, -0.5));
        helper.getBlockState(POS.offset(1, 0, 0)).useWithoutItem(helper.getLevel(), player,
                new BlockHitResult(
                        helper.absolutePos(POS.offset(1, 0, 0)).getCenter(),
                        Direction.NORTH,
                        helper.absolutePos(POS.offset(1, 0, 0)),
                        false)
        );

        // And reconnect them using the wrench
        player.setPos(helper.absolutePos(POS.offset(1, 0, 0)).getCenter().add(0.25, -1.5, 0));
        player.setYRot(90);
        helper.getLevel().sendParticles(new ParticleBlurData(1, 1, 1, 1, 100), player.position().x, player.position().y + player.getEyeHeight(), player.position().z, 10, 0, 0, 0, 0); // TODO: for debugging
        helper.getBlockState(POS.offset(1, 0, 0)).useWithoutItem(helper.getLevel(), player,
                new BlockHitResult(
                        helper.absolutePos(POS.offset(1, 0, 0)).getCenter(),
                        Direction.EAST,
                        helper.absolutePos(POS.offset(1, 0, 0)),
                        false)
        );

        helper.succeedWhen(() -> {
            INetwork network1 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS), null);
            INetwork network2 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(1, 0, 0)), null);
            INetwork network3 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(2, 0, 0)), null);
            INetwork network4 = NetworkHelpers.getNetworkChecked(helper.getLevel(), helper.absolutePos(POS.offset(3, 0, 0)), null);
            helper.assertTrue(network1 == network2, "Networks of connected cables are not equal");
            helper.assertTrue(network2 == network3, "Networks of wrench-reconnected cables are equal");
            helper.assertTrue(network3 == network4, "Networks of connected cables are not equal");
        });
    }

}
