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

    @Inject(at = @At("TAIL"), method = "render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V")
    private void onRender(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f matrix1, Matrix4f matrix2, Matrix4f matrix3, GpuBufferSlice bufferSlice, Vector4f vector, boolean bool2, CallbackInfo ci) {
        if (vernando.blueprints.Main.isVisible()) {
            BlueprintsManager blueprintManager = BlueprintsManager.getInstance();

            if (blueprintManager.blueprints != null) {
                // Create a MatrixStack for rendering (since our Blueprint.render method expects it)
                MatrixStack matrices = new MatrixStack();

                // Create a temporary sorted copy for rendering (furthest to closest)
                blueprintManager.blueprints.stream()
                    .sorted((a, b) -> {
                        double distanceA = a.getDistanceFromCamera(camera);
                        double distanceB = b.getDistanceFromCamera(camera);
                        return Double.compare(distanceB, distanceA); // Furthest first
                    })
                    .forEach(blueprint -> blueprint.render(matrices, camera, Settings.getRenderThroughBlocks(), true));

                BlueprintsHud.getInstance().render(matrices, camera);
            }
        }
    }
}