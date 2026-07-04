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
	 * Draw blueprints after the level frame graph has executed (terrain, clouds,
	 * weather all done). Drawing any earlier lets the clouds pass, which runs after
	 * the main pass, paint over blueprints.
	 */
	@Inject(method = "render", at = @At("TAIL"))
	private void blueprints$afterLevelRender(CallbackInfo ci) {
		Main.renderAfterLevel();
	}
}
