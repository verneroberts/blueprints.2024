package vernando.blueprints.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
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
            RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
                .withCull(false)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withLocation(Identifier.fromNamespaceAndPath("blueprints", "pipeline/blueprint_visible"))
                .build()
        );
        BlueprintPipelines.BLUEPRINT_ALL = register(
            RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                .withDepthStencilState(Optional.empty())
                .withCull(false)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withLocation(Identifier.fromNamespaceAndPath("blueprints", "pipeline/blueprint_all"))
                .build()
        );
    }
}
