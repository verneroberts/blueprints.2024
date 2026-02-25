package vernando.blueprints.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

import vernando.blueprints.BlueprintPipelines;

@Mixin(RenderPipelines.class)
public class RenderPipelinesMixin {

    @Shadow
    private static RenderPipeline register(RenderPipeline pipeline) {
        throw new AssertionError();
    }

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void onStaticInit(CallbackInfo ci) {
        BlueprintPipelines.BLUEPRINT_WORLD = register(
            RenderPipeline.builder()
                .withUniform("Transforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withVertexShader("core/position_tex_color")
                .withFragmentShader("core/position_tex_color")
                .withSampler("Sampler0")
                .withBlend(BlendFunction.TRANSLUCENT)
                .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                .withDepthWrite(false)
                .withLocation(Identifier.of("blueprints", "pipeline/blueprint_visible"))
                .build()
        );
    }
}
