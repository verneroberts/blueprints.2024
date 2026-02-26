package vernando.blueprints.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import vernando.blueprints.BlueprintsHud;
import vernando.blueprints.BlueprintsManager;
import vernando.blueprints.Settings;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(at = @At("TAIL"), method = "render")
    private void onRender(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f matrix1, Matrix4f matrix2, Matrix4f matrix3, GpuBufferSlice bufferSlice, Vector4f vector, boolean bool2, CallbackInfo ci) {
        // Only handle render-all mode here at TAIL.
        // Render-visible mode is handled via WorldRenderEvents.AFTER_TRANSLUCENT in Main,
        // where the world depth buffer is still active.
        if (vernando.blueprints.Main.isVisible() && Settings.getRenderThroughBlocks()) {
            BlueprintsManager blueprintManager = BlueprintsManager.getInstance();

            if (blueprintManager.blueprints != null) {
                MatrixStack matrices = new MatrixStack();

                blueprintManager.blueprints.stream()
                    .sorted((a, b) -> {
                        double distanceA = a.getDistanceFromCamera(camera);
                        double distanceB = b.getDistanceFromCamera(camera);
                        return Double.compare(distanceB, distanceA); // Furthest first
                    })
                    .forEach(blueprint -> blueprint.render(matrices, camera, true, true));

                BlueprintsHud.getInstance().render(matrices, camera);
            }
        }
    }
}