package org.cyclops.integrateddynamics.item;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.item.ItemBlockMetadata;
import org.cyclops.integrateddynamics.api.block.cable.ICableFakeable;
import org.cyclops.integrateddynamics.block.BlockCable;

import java.util.List;

/**
 * The item for the cable.
 * @author rubensworks
 */
public class ItemBlockCable extends ItemBlockMetadata {

    private static final List<IUseAction> USE_ACTIONS = Lists.newLinkedList();

    /**
     * Make a new instance.
     *
     * @param block The blockState instance.
     */
    public ItemBlockCable(Block block) {
        super(block);
    }

    /**
     * Register a use action for the cable item.
     * @param useAction The use action.
     */
    public static void addUseAction(IUseAction useAction) {
        USE_ACTIONS.add(useAction);
    }

    protected boolean checkCableAt(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block instanceof ICableFakeable && !((ICableFakeable) block).isRealCable(world, pos);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player,
                                       ItemStack stack) {
        BlockPos target = pos.offset(side);
        // First check if the target is an unreal cable.
        if(checkCableAt(world, pos)) return true;
        // Then check if the target is covered by an unreal cable at the given side.
        if(checkCableAt(world, target)) return true;
        // Skips client-side entity collision detection for placing cables.
        return world.getBlockState(target).getBlock().isReplaceable(world, pos);
    }

    protected boolean attempItemUseTarget(ItemStack stack, World world, BlockPos pos, BlockCable blockCable) {
        Block block = world.getBlockState(pos).getBlock();
        if(!block.isAir(world, pos)) {
            if (block instanceof ICableFakeable) {
                ICableFakeable cable = (ICableFakeable) block;
                if (!cable.isRealCable(world, pos)) {
                    cable.setRealCable(world, pos, true);
                    return true;
                }
            }
            for (IUseAction useAction : USE_ACTIONS) {
                if (useAction.attempItemUseTarget(stack, world, pos, blockCable)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void afterItemUse(ItemStack stack, World world, BlockPos pos, BlockCable blockCable, boolean calledSuper) {
        if(!calledSuper) {
            playPlaceSound(world, pos);
            --stack.stackSize;
        }
        blockCable.setDisableCollisionBox(false);
    }

    public static void playPlaceSound(World world, BlockPos pos) {
        Block block = BlockCable.getInstance();
        world.playSoundEffect((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F),
                block.stepSound.getPlaceSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F);
    }

    public static void playBreakSound(World world, BlockPos pos, IBlockState blockState) {
        world.playAuxSFX(2001, pos, Block.getStateId(blockState));
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side,
                             float hitX, float hitY, float hitZ) {
        // Skips server-side entity collision detection for placing cables.
        // We temporary disable the collision box of the cable so that it can be placed even if an entity is in the way.
        BlockCable blockCable = (BlockCable) block;
        blockCable.setDisableCollisionBox(true);

        // Avoid regular block placement when the target is an unreal cable.
        if(attempItemUseTarget(stack, worldIn, pos, blockCable)) {
            afterItemUse(stack, worldIn, pos, blockCable, false);
            return true;
        }

        // Change pos and side when we are targeting a block that is blocked by an unreal cable, so we want to target
        // the unreal cable.
        if(attempItemUseTarget(stack, worldIn, pos.offset(side), blockCable)) {
            afterItemUse(stack, worldIn, pos, blockCable, false);
            return true;
        }

        boolean ret = super.onItemUse(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);
        afterItemUse(stack, worldIn, pos, blockCable, true);
        return ret;
    }

    public static interface IUseAction {

        /**
         * Attempt to use the given item .
         * @param itemStack The item stack that is being used.
         * @param world The world.
         * @param pos The position.
         * @param blockCable The cable block instance.
         * @return If the use action was applied.
         */
        public boolean attempItemUseTarget(ItemStack itemStack, World world, BlockPos pos, BlockCable blockCable);

    }

}
