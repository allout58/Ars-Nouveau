package com.hollingsworth.arsnouveau.setup;

import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.api.recipe.GlyphPressRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class RecipeRegistry {
    public static final IRecipeType<GlyphPressRecipe> GLYPH_TYPE = new RecipeType();
    public static final IRecipeSerializer<GlyphPressRecipe> PRESS_SERIALIZER = new GlyphPressRecipe.Serializer();

    public static void register(RegistryEvent.Register<IRecipeSerializer<?>> evt) {
        Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(ArsNouveau.MODID, "glyph_press"), GLYPH_TYPE);
        evt.getRegistry().register(PRESS_SERIALIZER.setRegistryName(new ResourceLocation(ArsNouveau.MODID, "glyph_press")));
    }

    private static class RecipeType<T extends IRecipe<?>> implements IRecipeType<T> {
        @Override
        public String toString() {
            return Registry.RECIPE_TYPE.getKey(this).toString();
        }
    }
}
