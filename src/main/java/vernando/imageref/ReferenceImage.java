package vernando.imageref;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.io.File;
import java.io.FileInputStream;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

public class ReferenceImage {	

	private NativeImageBackedTexture texture;
	private Identifier textureId;
		
	public float scaleX;
	public float scaleY;
	public float positionX;
	public float positionY;
	public float positionZ;
	public float rotationX;
	public float rotationY;
	public float rotationZ;
	public float alpha;

	public void LoadConfig() {
		scaleX = Config.scaleX = 1.0f;
		scaleY = Config.scaleY = 1.0f;
		positionX = Config.positionX;
		positionY = Config.positionY;
		positionZ = Config.positionZ;
		rotationX = Config.rotationX = 0.0f;
		rotationY = Config.rotationY = 0.0f;
		rotationZ = Config.rotationZ = 0.0f;
		alpha = Config.alpha = 1.0f;
	}

	public void SaveConfig() {
		Config.scaleX = scaleX;
		Config.scaleY = scaleY;
		Config.positionX = positionX;
		Config.positionY = positionY;
		Config.positionZ = positionZ;
		Config.rotationX = rotationX;
		Config.rotationY = rotationY;
		Config.rotationZ = rotationZ;
		Config.alpha = alpha;
	}	

	public void registerTexture(MinecraftClient client, String texturePath, String id) {
		try {
			ImageRef.LOGGER.info("Loading image: " + texturePath + " as " + id);
			NativeImage image = NativeImage.read(new FileInputStream(new File(texturePath)));
			texture = new NativeImageBackedTexture(image);		
			textureId = new Identifier(ImageRef.MOD_ID, id);
			ImageRef.LOGGER.info("Registering texture: " + textureId);			
			client.getTextureManager().registerTexture(textureId, texture);		
			
		} catch (Exception e) {
			ImageRef.LOGGER.error("Failed to load image: " + texturePath);
			ImageRef.LOGGER.error(e.getMessage());
			return;
		}
	}

	public void render(WorldRenderContext context, Boolean renderThroughBlocks) {		
		if (texture == null) {
			return;
		}

		Camera camera = context.camera();
		Vec3d targetPosition = new Vec3d(positionX, positionY, positionZ);
		Vec3d transformedPosition = targetPosition.subtract(camera.getPos());

		MatrixStack matrixStack = new MatrixStack();
		matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
		matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);

		matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationX));
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationY));
		matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationZ));

		Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		if (alpha < 1f) {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
		// add vertices in a rectangle from -scale to +scale
		buffer.vertex(positionMatrix, -scaleX, scaleY, 0).color(1f, 1f, 1f, alpha).texture(0f, 0f).next();
		buffer.vertex(positionMatrix, -scaleX, -scaleY, 0).color(1f, 1f, 1f, alpha).texture(0f, 1f).next();
		buffer.vertex(positionMatrix, scaleX, -scaleY, 0).color(1f, 1f, 1f, alpha).texture(1f, 1f).next();
		buffer.vertex(positionMatrix, scaleX, scaleY, 0).color(1f, 1f, 1f, alpha).texture(1f, 0f).next();

		RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
		RenderSystem.setShaderTexture(0, textureId);
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

		if (renderThroughBlocks) {
			RenderSystem.disableCull();
			RenderSystem.depthFunc(GL11.GL_ALWAYS);
		}

		tessellator.draw();

		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.enableCull();
		matrixStack.pop();
	}

    public void renderThumbnail(DrawContext drawContext) {
		if (texture == null) {
			return;
		}

		// Get the transformation matrix from the matrix stack, alongside the tessellator instance and a new buffer builder.
		Matrix4f transformationMatrix = drawContext.getMatrices().peek().getPositionMatrix();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
		buffer.vertex(transformationMatrix, 20, 20, 0).color(1f, 1f, 1f, 1f).texture(0f, 0f).next();
		buffer.vertex(transformationMatrix, 20, 60, 0).color(1f, 1f, 1f, 1f).texture(0f, 1f).next();
		buffer.vertex(transformationMatrix, 60, 60, 0).color(1f, 1f, 1f, 1f).texture(1f, 1f).next();
		buffer.vertex(transformationMatrix, 60, 20, 0).color(1f, 1f, 1f, 1f).texture(1f, 0f).next();

		RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
		RenderSystem.setShaderTexture(0, textureId);
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

		tessellator.draw();
	 }
}