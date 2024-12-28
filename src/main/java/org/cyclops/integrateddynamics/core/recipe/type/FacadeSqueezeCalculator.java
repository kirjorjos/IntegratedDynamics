package org.cyclops.integrateddynamics.core.recipe.type;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class FacadeSqueezeCalculator {

    public static ItemStack getOutputItems(ItemStack inputItem) {

        CompoundTag nbt = inputItem.getOrCreateTag();
        ResourceLocation resourceLocation = new ResourceLocation(nbt.getCompound("block").getString("Name"));
        Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);

        if (resourceLocation.toString().equals("minecraft:")) return ItemStack.EMPTY;	//invalid NBT, block not encoded or empty facade

        return new ItemStack(item);
    }
}
