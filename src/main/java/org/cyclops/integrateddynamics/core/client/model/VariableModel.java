package org.cyclops.integrateddynamics.core.client.model;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Model for a variant of a variable item.
 * @author rubensworks
 */
public class VariableModel implements UnbakedModel, IUnbakedGeometry<VariableModel> {

    private final BlockModel base;

    public VariableModel(BlockModel base) {
        this.base = base;
    }

    public void loadSubModels(List<ResourceLocation> subModels) {
        for(IVariableModelProvider provider : VariableModelProviders.REGISTRY.getProviders()) {
            provider.loadModels(subModels);
        }
    }

    public static void addAdditionalModels(ImmutableSet.Builder<ResourceLocation> builder) {
        for(IVariableModelProvider<?> provider : VariableModelProviders.REGISTRY.getProviders()) {
            builder.addAll(provider.getDependencies());
        }
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        if(base.getParentLocation() == null || base.getParentLocation().getPath().startsWith("builtin/")) {
            return Collections.emptyList();
        }
        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();
        builder.add(base.getParentLocation());
        addAdditionalModels(builder);
        return builder.build();
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
        base.resolveParents(resolver);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        return bake(baker, spriteGetter, modelState);
    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState) {
        Material textureName = base.getMaterial("layer0");
        BlockModel itemModel = ModelHelpers.MODEL_GENERATOR.generateBlockModel(spriteGetter, base);
        SimpleBakedModel.Builder builder = (new SimpleBakedModel.Builder(itemModel, itemModel.getOverrides(bakery, itemModel, spriteGetter), false));
        itemModel.textureMap.put("layer0", Either.left(textureName));
        TextureAtlasSprite textureAtlasSprite = spriteGetter.apply(textureName);
        builder.particle(textureAtlasSprite);
        for (BakedQuad bakedQuad : UnbakedGeometryHelper.bakeElements(UnbakedGeometryHelper.createUnbakedItemElements(0, textureAtlasSprite, ExtraFaceData.DEFAULT), $ -> textureAtlasSprite, modelState)) {
            builder.addUnculledFace(bakedQuad);
        }
        BakedModel baseModel = builder.build();
        VariableModelBaked bakedModel = new VariableModelBaked(baseModel);

        for(IVariableModelProvider provider : VariableModelProviders.REGISTRY.getProviders()) {
            bakedModel.setSubModels(provider, provider.bakeOverlayModels(bakery, spriteGetter, modelState, this.base.getParentLocation()));
        }

        return bakedModel;
    }
}
