package vernando.blueprints.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;

import org.slf4j.LoggerFactory;
import vernando.blueprints.BlueprintPipelines;

@Mixin(RenderPipelines.class)
public class RenderPipelinesMixin {

    @Shadow
    private static RenderPipeline register(RenderPipeline pipeline) {
        throw new AssertionError();
    }

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void onStaticInit(CallbackInfo ci) {
        LoggerFactory.getLogger("blueprints").info("[Blueprints] Registering BLUEPRINT_WORLD pipeline");
        BlueprintPipelines.BLUEPRINT_WORLD = register(
            RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
                .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                .withDepthWrite(true)
                .withCull(false)
                .withBlend(BlendFunction.TRANSLUCENT)
                .withLocation(Identifier.of("blueprints", "pipeline/blueprint_visible"))
                .build()
        );
    }
}
