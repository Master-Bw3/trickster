package dev.enjarai.trickster.render.fragment;

import dev.enjarai.trickster.Trickster;


import dev.enjarai.trickster.render.fleck.LineFleckRenderer;
import dev.enjarai.trickster.render.fleck.SpellFleckRenderer;
import dev.enjarai.trickster.render.fleck.TextFleckRenderer;
import dev.enjarai.trickster.spell.Fragment;
import dev.enjarai.trickster.spell.SpellPart;
import dev.enjarai.trickster.spell.fragment.FragmentType;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

public interface FragmentRenderer<T extends Fragment> {
    RegistryKey<Registry<FragmentRenderer<?>>> REGISTRY_KEY = RegistryKey.ofRegistry(Trickster.id("fragment_renderer"));
    Registry<FragmentRenderer<?>> REGISTRY = FabricRegistryBuilder.createSimple(REGISTRY_KEY).buildAndRegister();

    PatternRenderer PATTERN = register(FragmentType.PATTERN, new PatternRenderer());

    static <T extends FragmentRenderer<F>, F extends Fragment> T register(FragmentType<F> type, T renderer) {
        return Registry.register(REGISTRY, FragmentType.REGISTRY.getId(type), renderer);
    }

    static void register() {}

    void render(T fragment, MatrixStack matrices, VertexConsumerProvider vertexConsumers, float x, float y, float size, float alpha);
}
