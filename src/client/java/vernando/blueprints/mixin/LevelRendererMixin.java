package vernando.blueprints.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.LevelRenderer;
import vernando.blueprints.Main;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

	/**
	 * Draw blueprints right after the level frame graph has executed (terrain,
	 * clouds, weather all done). Drawing any earlier lets the clouds pass, which
	 * runs after the main pass, paint over blueprints. Must run before render()
	 * pops the model-view stack (which happens right after execute), or the view
	 * matrix is gone and blueprints render fixed to the camera.
	 */
	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;execute(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder$Inspector;)V",
			shift = At.Shift.AFTER
		)
	)
	private void blueprints$afterLevelRender(CallbackInfo ci) {
		Main.renderAfterLevel();
	}
}
