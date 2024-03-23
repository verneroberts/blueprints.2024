package vernando.imageref;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.systems.RenderSystem;

import eu.midnightdust.lib.config.MidnightConfig;

public class ImageRef implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("image-ref");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		// Register the config
		ImageRefConfig.init("image-ref", ImageRefConfig.class);
		// screen = MidnightConfig.getScreen(parent, "image-ref");

		WorldRenderEvents.END.register(context -> {
			Boolean renderThroughBlocks = true;
			float scaleX = 20f;
			float scaleY = 20f;
			float x = -16f;
			float y = 63f;
			float z = 16f;
			float rotationX = 1f;
			float rotationY = 1f;
			float rotationZ = 1f;
			float alpha = 0.5f;

			Camera camera = context.camera();
			Vec3d targetPosition = new Vec3d(x, y, z);
			Vec3d transformedPosition = targetPosition.subtract(camera.getPos());			

			MatrixStack matrixStack = new MatrixStack();			
			matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
			matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);

			matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationX));
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationY));
			matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationZ));
			
			// matrixStack.translate(40, 40, 0);
			// matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((System.currentTimeMillis() % 5000) / 5000f * 360f));
			// matrixStack.translate(-40, -40, 0);

			Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			if (alpha < 1f) {
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);									
			}

			buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
			buffer.vertex(positionMatrix, 0, scaleY, 0).color(1f, 1f, 1f, 1f).texture(0f, 0f).next();
			buffer.vertex(positionMatrix, 0, 0, 0).color(1f, 1f, 1f, 1f).texture(0f, 1f).next();
			buffer.vertex(positionMatrix, scaleX, 0, 0).color(1f, 1f, 1f, 1f).texture(1f, 1f).next();
			buffer.vertex(positionMatrix, scaleX, scaleY, 0).color(1f, 1f, 1f, 1f).texture(1f, 0f).next();
		
			RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
			RenderSystem.setShaderTexture(0, new Identifier("image-ref", "master-pnp-habshaer-fl-fl0700-fl0701-photos-577579pu.png"));
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

			if (renderThroughBlocks) {
				RenderSystem.disableCull();
				RenderSystem.depthFunc(GL11.GL_ALWAYS);
			}

			tessellator.draw();

			RenderSystem.depthFunc(GL11.GL_LEQUAL);
			RenderSystem.enableCull();
			matrixStack.pop();

		});
	}
}