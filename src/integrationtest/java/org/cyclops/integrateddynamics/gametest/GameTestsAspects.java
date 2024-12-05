package org.cyclops.integrateddynamics.gametest;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.Reference;
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
import org.cyclops.integrateddynamics.blockentity.BlockEntityVariablestore;
import org.cyclops.integrateddynamics.core.block.IgnoredBlockStatus;
import org.cyclops.integrateddynamics.core.evaluate.operator.Operators;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeBoolean;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.item.AspectVariableFacade;
import org.cyclops.integrateddynamics.core.logicprogrammer.OperatorLPElement;
import org.cyclops.integrateddynamics.core.part.PartTypes;
import org.cyclops.integrateddynamics.part.PartTypePanelDisplay;
import org.cyclops.integrateddynamics.part.aspect.Aspects;

import java.util.Objects;

@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsAspects {

    public static final String TEMPLATE_EMPTY = "empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(2, 0, 2);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsRedstoneReaderToWriter(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Place redstone writer
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));

        // Produce a redstone signal
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west().west(), Blocks.REDSTONE_TORCH);

        // Writer redstone signal from redstone reader to variable card
        ItemStack variableAspect = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);

        // Place variable in writer
        placeVariableInWriter(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST), Aspects.Write.Redstone.INTEGER, variableAspect);

        // Place redstone wire next to redstone writer
        helper.setBlock(POS.east().east(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.east().east().east(), Blocks.REDSTONE_WIRE);

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(POS.east().east(), RedStoneWireBlock.WEST, RedstoneSide.SIDE);
            helper.assertBlockProperty(POS.east().east(), RedStoneWireBlock.POWER, 14);

            IPartStateWriter partStateWriter = (IPartStateWriter) PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)).getState();
            helper.assertFalse(partStateWriter.isDeactivated(), "Writer is deactivated");
            helper.assertValueEqual(
                    PartTypes.REDSTONE_WRITER.getBlockState(PartHelpers.getPartContainerChecked(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)), Direction.EAST).getValue(IgnoredBlockStatus.STATUS),
                    IgnoredBlockStatus.Status.ACTIVE,
                    "Block status is incorrect"
            );
            helper.assertValueEqual(partStateWriter.getActiveAspect(), Aspects.Write.Redstone.INTEGER, "Active aspect is incorrect");
            helper.assertTrue(partStateWriter.getErrors(Aspects.Write.Redstone.INTEGER).isEmpty(), "Active aspect has errors");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsNothingToWriter(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone writer
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));

        // Place redstone wire next to redstone writer
        helper.setBlock(POS.east().east(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.east().east().east(), Blocks.REDSTONE_WIRE);

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(POS.east().east(), RedStoneWireBlock.WEST, RedstoneSide.SIDE);
            helper.assertBlockProperty(POS.east().east(), RedStoneWireBlock.POWER, 0);

            IPartStateWriter partStateWriter = (IPartStateWriter) PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)).getState();
            helper.assertFalse(partStateWriter.isDeactivated(), "Writer is deactivated");
            helper.assertValueEqual(
                    PartTypes.REDSTONE_WRITER.getBlockState(PartHelpers.getPartContainerChecked(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)), Direction.EAST).getValue(IgnoredBlockStatus.STATUS),
                    IgnoredBlockStatus.Status.INACTIVE,
                    "Block status is incorrect"
            );
            helper.assertTrue(partStateWriter.getActiveAspect() == null, "Active aspect is incorrect");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsRedstoneReaderToWriterDisconnected(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east().east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Place redstone writer
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east().east()), Direction.EAST, PartTypes.REDSTONE_WRITER, new ItemStack(PartTypes.REDSTONE_WRITER.getItem()));

        // Produce a redstone signal
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west().west(), Blocks.REDSTONE_TORCH);

        // Writer redstone signal from redstone reader to variable card
        ItemStack variableAspect = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);

        // Place variable in writer
        placeVariableInWriter(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east().east()), Direction.EAST), Aspects.Write.Redstone.INTEGER, variableAspect);

        // Place redstone wire next to redstone writer
        helper.setBlock(POS.east().east().east(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.east().east().east().east(), Blocks.REDSTONE_WIRE);

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(POS.east().east().east(), RedStoneWireBlock.WEST, RedstoneSide.SIDE);
            helper.assertBlockProperty(POS.east().east().east(), RedStoneWireBlock.POWER, 0);

            IPartStateWriter partStateWriter = (IPartStateWriter) PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east().east()), Direction.EAST)).getState();
            IPartState partStateReader = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST)).getState();
            helper.assertTrue(partStateWriter.isDeactivated(), "Writer is not deactivated");
            helper.assertValueEqual(
                    PartTypes.REDSTONE_WRITER.getBlockState(PartHelpers.getPartContainerChecked(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east().east()), Direction.EAST)), Direction.EAST).getValue(IgnoredBlockStatus.STATUS),
                    IgnoredBlockStatus.Status.ERROR,
                    "Block status is incorrect"
            );
            helper.assertValueEqual(partStateWriter.getActiveAspect(), Aspects.Write.Redstone.INTEGER, "Active aspect is incorrect");
            helper.assertValueEqual(partStateWriter.getErrors(Aspects.Write.Redstone.INTEGER), Lists.newArrayList(
                    Component.translatable(L10NValues.VARIABLE_ERROR_PARTNOTINNETWORK, Integer.toString(partStateReader.getId()))
            ), "Active aspect errors do not match");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsRedstoneReaderToDisplay(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone reader
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Place display panel
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.DISPLAY_PANEL, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));

        // Produce a redstone signal
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west().west(), Blocks.REDSTONE_TORCH);

        // Writer redstone signal from redstone reader to variable card
        ItemStack variableAspect = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);

        // Place variable in writer
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = placeVariableInDisplayPanel(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST), variableAspect);

        helper.succeedWhen(() -> {
            helper.assertFalse(partAndState.getRight().isDeactivated(), "Display panel is deactivated");
            helper.assertValueEqual(
                    PartTypes.DISPLAY_PANEL.getBlockState(PartHelpers.getPartContainerChecked(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)), Direction.EAST).getValue(IgnoredBlockStatus.STATUS),
                    IgnoredBlockStatus.Status.ACTIVE,
                    "Block status is incorrect"
            );
            helper.assertTrue(partAndState.getRight().getGlobalErrors().isEmpty(), "Display panel has errors");
            assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeInteger.ValueInteger.of(14));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsNothingToDisplay(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place display panel
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.EAST, PartTypes.DISPLAY_PANEL, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));

        helper.succeedWhen(() -> {
            PartTypePanelDisplay.State partState = (PartTypePanelDisplay.State) PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.EAST)).getState();
            helper.assertFalse(partState.isDeactivated(), "Display panel is deactivated");
            helper.assertValueEqual(
                    PartTypes.DISPLAY_PANEL.getBlockState(PartHelpers.getPartContainerChecked(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.EAST)), Direction.EAST).getValue(IgnoredBlockStatus.STATUS),
                    IgnoredBlockStatus.Status.INACTIVE,
                    "Block status is incorrect"
            );
            assertValueEqual(partState.getDisplayValue(), null);
            helper.assertValueEqual(partState.getGlobalErrors(), Lists.newArrayList(), "Display panel errors do not match");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsBlankVariableToDisplay(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());

        // Place display panel
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.EAST, PartTypes.DISPLAY_PANEL, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));

        // Place variable in writer
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = placeVariableInDisplayPanel(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.EAST), new ItemStack(RegistryEntries.ITEM_VARIABLE));

        helper.succeedWhen(() -> {
            helper.assertFalse(partAndState.getRight().isDeactivated(), "Display panel is deactivated");
            helper.assertValueEqual(
                    PartTypes.DISPLAY_PANEL.getBlockState(PartHelpers.getPartContainerChecked(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.EAST)), Direction.EAST).getValue(IgnoredBlockStatus.STATUS),
                    IgnoredBlockStatus.Status.ACTIVE,
                    "Block status is incorrect"
            );
            assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeBoolean.ValueBoolean.of(true));
            helper.assertValueEqual(partAndState.getRight().getGlobalErrors(), Lists.newArrayList(), "Display panel errors do not match");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsRedstoneReaderAddOperatorToDisplay(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone readers
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Place display panel
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.DISPLAY_PANEL, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));

        // Place variable store
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north());

        // Produce a redstone signals
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west().west(), Blocks.REDSTONE_TORCH);
        helper.setBlock(POS.south(), Blocks.REDSTONE_BLOCK);

        // Writer redstone signal from redstone readers to variable card
        ItemStack variableAspect1 = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);
        ItemStack variableAspect2 = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH), Aspects.Read.Redstone.INTEGER_VALUE);

        // Insert redstone signal variable in variable store
        variableStore.getInventory().setItem(0, variableAspect1);
        variableStore.getInventory().setItem(1, variableAspect2);

        // Create variable card for + operator on variable aspect
        ItemStack variableAdded = createVariableForOperator(helper.getLevel(), Operators.ARITHMETIC_ADDITION, new int[]{
                getVariableFacade(helper.getLevel(), variableAspect1).getId(),
                getVariableFacade(helper.getLevel(), variableAspect2).getId()
        });

        // Place variable in writer
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = placeVariableInDisplayPanel(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST), variableAdded);

        helper.succeedWhen(() -> {
            assertValueEqual(partAndState.getRight().getDisplayValue(), ValueTypeInteger.ValueInteger.of(29));
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsRedstoneReaderAddOperatorToDisplayIncompleteVariableStore(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone readers
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Place display panel
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.DISPLAY_PANEL, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));

        // Place variable store
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north());

        // Produce a redstone signals
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west().west(), Blocks.REDSTONE_TORCH);
        helper.setBlock(POS.south(), Blocks.REDSTONE_BLOCK);

        // Writer redstone signal from redstone readers to variable card
        ItemStack variableAspect1 = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);
        ItemStack variableAspect2 = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH), Aspects.Read.Redstone.INTEGER_VALUE);

        // Insert redstone signal variable in variable store
        variableStore.getInventory().setItem(0, variableAspect1);
        // variableAspect2 not added in variable store!

        // Create variable card for + operator on variable aspect
        ItemStack variableAdded = createVariableForOperator(helper.getLevel(), Operators.ARITHMETIC_ADDITION, new int[]{
                getVariableFacade(helper.getLevel(), variableAspect1).getId(),
                getVariableFacade(helper.getLevel(), variableAspect2).getId()
        });

        // Place variable in writer
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = placeVariableInDisplayPanel(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST), variableAdded);

        helper.succeedWhen(() -> {
            helper.assertFalse(partAndState.getRight().isDeactivated(), "Display panel is deactivated");
            helper.assertValueEqual(
                    PartTypes.DISPLAY_PANEL.getBlockState(PartHelpers.getPartContainerChecked(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)), Direction.EAST).getValue(IgnoredBlockStatus.STATUS),
                    IgnoredBlockStatus.Status.ERROR,
                    "Block status is incorrect"
            );
            assertValueEqual(partAndState.getRight().getDisplayValue(), null);
            helper.assertValueEqual(partAndState.getRight().getGlobalErrors(), Lists.newArrayList(
                    Component.translatable(L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK, Integer.toString(getVariableFacade(helper.getLevel(), variableAspect2).getId()))
            ), "Display panel errors do not match");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsRedstoneReaderAddOperatorToDisplayFullyDisconnected(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east().east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone readers
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Place display panel
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east().east()), Direction.EAST, PartTypes.DISPLAY_PANEL, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));

        // Place variable store
        helper.setBlock(POS.north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north());

        // Produce a redstone signals
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west().west(), Blocks.REDSTONE_TORCH);
        helper.setBlock(POS.south(), Blocks.REDSTONE_BLOCK);

        // Writer redstone signal from redstone readers to variable card
        ItemStack variableAspect1 = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);
        ItemStack variableAspect2 = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH), Aspects.Read.Redstone.INTEGER_VALUE);

        // Insert redstone signal variable in variable store
        variableStore.getInventory().setItem(0, variableAspect1);
        variableStore.getInventory().setItem(1, variableAspect2);

        // Create variable card for + operator on variable aspect
        ItemStack variableAdded = createVariableForOperator(helper.getLevel(), Operators.ARITHMETIC_ADDITION, new int[]{
                getVariableFacade(helper.getLevel(), variableAspect1).getId(),
                getVariableFacade(helper.getLevel(), variableAspect2).getId()
        });

        // Place variable in writer
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = placeVariableInDisplayPanel(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east().east()), Direction.EAST), variableAdded);

        helper.succeedWhen(() -> {
            helper.assertFalse(partAndState.getRight().isDeactivated(), "Display panel is deactivated");
            helper.assertValueEqual(
                    PartTypes.DISPLAY_PANEL.getBlockState(PartHelpers.getPartContainerChecked(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east().east()), Direction.EAST)), Direction.EAST).getValue(IgnoredBlockStatus.STATUS),
                    IgnoredBlockStatus.Status.ERROR,
                    "Block status is incorrect"
            );
            assertValueEqual(partAndState.getRight().getDisplayValue(), null);
            helper.assertValueEqual(partAndState.getRight().getGlobalErrors(), Lists.newArrayList(
                    Component.translatable(L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK, Integer.toString(getVariableFacade(helper.getLevel(), variableAspect1).getId())),
                    Component.translatable(L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK, Integer.toString(getVariableFacade(helper.getLevel(), variableAspect2).getId()))
            ), "Display panel errors do not match");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsRedstoneReaderAddOperatorToDisplayReadersDisconnected(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east().east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone readers
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Place display panel
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east().east()), Direction.EAST, PartTypes.DISPLAY_PANEL, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));

        // Place variable store
        helper.setBlock(POS.east().east().north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.east().east().north());

        // Produce a redstone signals
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west().west(), Blocks.REDSTONE_TORCH);
        helper.setBlock(POS.south(), Blocks.REDSTONE_BLOCK);

        // Writer redstone signal from redstone readers to variable card
        ItemStack variableAspect1 = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);
        ItemStack variableAspect2 = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH), Aspects.Read.Redstone.INTEGER_VALUE);

        // Insert redstone signal variable in variable store
        variableStore.getInventory().setItem(0, variableAspect1);
        variableStore.getInventory().setItem(1, variableAspect2);

        // Create variable card for + operator on variable aspect
        ItemStack variableAdded = createVariableForOperator(helper.getLevel(), Operators.ARITHMETIC_ADDITION, new int[]{
                getVariableFacade(helper.getLevel(), variableAspect1).getId(),
                getVariableFacade(helper.getLevel(), variableAspect2).getId()
        });

        // Place variable in writer
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = placeVariableInDisplayPanel(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east().east()), Direction.EAST), variableAdded);

        helper.succeedWhen(() -> {
            helper.assertFalse(partAndState.getRight().isDeactivated(), "Display panel is deactivated");
            helper.assertValueEqual(
                    PartTypes.DISPLAY_PANEL.getBlockState(PartHelpers.getPartContainerChecked(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east().east()), Direction.EAST)), Direction.EAST).getValue(IgnoredBlockStatus.STATUS),
                    IgnoredBlockStatus.Status.ERROR,
                    "Block status is incorrect"
            );
            assertValueEqual(partAndState.getRight().getDisplayValue(), null);
            IPartState partStateReader1 = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST)).getState();
            IPartState partStateReader2 = PartHelpers.getPart(PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH)).getState();
            helper.assertValueEqual(partAndState.getRight().getGlobalErrors(), Lists.newArrayList(
                    Component.translatable(L10NValues.VARIABLE_ERROR_PARTNOTINNETWORK, Integer.toString(partStateReader1.getId())),
                    Component.translatable(L10NValues.VARIABLE_ERROR_PARTNOTINNETWORK, Integer.toString(partStateReader2.getId()))
            ), "Display panel errors do not match");
        });
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAspectsRedstoneReaderAddOperatorToDisplayVariableStoreDisconnected(GameTestHelper helper) {
        // Place cable
        helper.setBlock(POS, RegistryEntries.BLOCK_CABLE.value());
        helper.setBlock(POS.east(), RegistryEntries.BLOCK_CABLE.value());

        // Place redstone readers
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.WEST, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH, PartTypes.REDSTONE_READER, new ItemStack(PartTypes.REDSTONE_READER.getItem()));

        // Place display panel
        PartHelpers.addPart(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST, PartTypes.DISPLAY_PANEL, new ItemStack(PartTypes.DISPLAY_PANEL.getItem()));

        // Place variable store
        helper.setBlock(POS.north().north(), RegistryEntries.BLOCK_VARIABLE_STORE.get());
        BlockEntityVariablestore variableStore = helper.getBlockEntity(POS.north().north());

        // Produce a redstone signals
        helper.setBlock(POS.west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west(), Blocks.REDSTONE_WIRE);
        helper.setBlock(POS.west().west().west(), Blocks.REDSTONE_TORCH);
        helper.setBlock(POS.south(), Blocks.REDSTONE_BLOCK);

        // Writer redstone signal from redstone readers to variable card
        ItemStack variableAspect1 = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.WEST), Aspects.Read.Redstone.INTEGER_VALUE);
        ItemStack variableAspect2 = createVariableFromReader(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS), Direction.SOUTH), Aspects.Read.Redstone.INTEGER_VALUE);

        // Insert redstone signal variable in variable store
        variableStore.getInventory().setItem(0, variableAspect1);
        variableStore.getInventory().setItem(1, variableAspect2);

        // Create variable card for + operator on variable aspect
        ItemStack variableAdded = createVariableForOperator(helper.getLevel(), Operators.ARITHMETIC_ADDITION, new int[]{
                getVariableFacade(helper.getLevel(), variableAspect1).getId(),
                getVariableFacade(helper.getLevel(), variableAspect2).getId()
        });

        // Place variable in writer
        Pair<PartTypePanelDisplay, PartTypePanelDisplay.State> partAndState = placeVariableInDisplayPanel(helper.getLevel(), PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST), variableAdded);

        helper.succeedWhen(() -> {
            helper.assertFalse(partAndState.getRight().isDeactivated(), "Display panel is deactivated");
            helper.assertValueEqual(
                    PartTypes.DISPLAY_PANEL.getBlockState(PartHelpers.getPartContainerChecked(PartPos.of(helper.getLevel(), helper.absolutePos(POS.east()), Direction.EAST)), Direction.EAST).getValue(IgnoredBlockStatus.STATUS),
                    IgnoredBlockStatus.Status.ERROR,
                    "Block status is incorrect"
            );
            assertValueEqual(partAndState.getRight().getDisplayValue(), null);
            helper.assertValueEqual(partAndState.getRight().getGlobalErrors(), Lists.newArrayList(
                    Component.translatable(L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK, Integer.toString(getVariableFacade(helper.getLevel(), variableAspect1).getId())),
                    Component.translatable(L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK, Integer.toString(getVariableFacade(helper.getLevel(), variableAspect2).getId()))
            ), "Display panel errors do not match");
        });
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

}
