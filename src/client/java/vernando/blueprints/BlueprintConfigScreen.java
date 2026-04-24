package vernando.blueprints;

import java.util.ArrayList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class BlueprintConfigScreen extends Screen {
	private Blueprint blueprint;
	private Screen parent;
	private Integer blurSetting;
	private ArrayList<NumberFieldWidget> inputBoxes;
	int rowHeight = 17;
	int rowPadding = 2;
	int columnWidth = 60;
	int columnPadding = 4;

	int c0x = 5;
	int c1x = 18;
	int c2x = c1x + columnWidth + columnPadding;

	private int row(int rowNumber) {
		return rowNumber * (rowHeight + rowPadding);
	}

	protected BlueprintConfigScreen(Blueprint blueprint, Screen parent) {
		super(Component.literal(Main.MOD_NAME + " " + blueprint.getName() + " Config"));
		this.blueprint = blueprint;
		this.parent = parent;
		this.inputBoxes = new ArrayList<NumberFieldWidget>();
	}

	@Override
	public void onClose() {
		blueprint.SaveConfig();
		minecraft.setScreen(parent);
		// restore blur setting
		minecraft.options.menuBackgroundBlurriness().set(blurSetting);

	}
	@Override
	protected void init() {
		int width = minecraft.getWindow().getGuiScaledWidth();

		// make sure the blueprint is visible if we are here
		if (!blueprint.isVisible()) {
			blueprint.setVisibility(true);
		}

		// save the current blur setting then set it to 0
		this.blurSetting = minecraft.options.getMenuBackgroundBlurriness();
		minecraft.options.menuBackgroundBlurriness().set(0);

		NumberFieldWidget posX = new NumberFieldWidget(font, c1x, row(2), columnWidth, rowHeight,
				blueprint.getPosX(), (v) -> blueprint.setPosX(v), "X Position", 1);
		NumberFieldWidget posY = new NumberFieldWidget(font, c1x, row(3), columnWidth, rowHeight,
				blueprint.getPosY(), (v) -> blueprint.setPosY(v), "Y Position", 1);
		NumberFieldWidget posZ = new NumberFieldWidget(font, c1x, row(4), columnWidth, rowHeight,
				blueprint.getPosZ(), (v) -> blueprint.setPosZ(v), "Z Position", 1);
		NumberFieldWidget rotX = new NumberFieldWidget(font, c2x, row(2), columnWidth, rowHeight,
				blueprint.getRotationX(), (v) -> blueprint.setRotationX(v), "X Rotation", 1);
		NumberFieldWidget rotY = new NumberFieldWidget(font, c2x, row(3), columnWidth, rowHeight,
				blueprint.getRotationY(), (v) -> blueprint.setRotationY(v), "Y Rotation", 1);
		NumberFieldWidget rotZ = new NumberFieldWidget(font, c2x, row(4), columnWidth, rowHeight,
				blueprint.getRotationZ(), (v) -> blueprint.setRotationZ(v), "Z Rotation", 1);
		NumberFieldWidget scaleX = new NumberFieldWidget(font, c1x, row(7), columnWidth, rowHeight,
				blueprint.getScaleX(), (v) -> blueprint.setScaleX(v), "Width", 1);
		NumberFieldWidget scaleY = new NumberFieldWidget(font, c1x, row(8), columnWidth, rowHeight,
				blueprint.getScaleY(), (v) -> blueprint.setScaleY(v), "Height", 1);
		NumberFieldWidget alpha = new NumberFieldWidget(font, c2x, row(7), columnWidth, rowHeight,
				blueprint.getAlpha(), (v) -> blueprint.setAlpha(v), "Alpha", 0.1f)
				.setMaxValue(1f)
				.setMinValue(0f);

		inputBoxes.add(posX);
		inputBoxes.add(posY);
		inputBoxes.add(posZ);
		inputBoxes.add(rotX);
		inputBoxes.add(rotY);
		inputBoxes.add(rotZ);
		inputBoxes.add(scaleX);
		inputBoxes.add(scaleY);
		inputBoxes.add(alpha);
		addRenderableWidget(posX);
		addRenderableWidget(posY);
		addRenderableWidget(posZ);
		addRenderableWidget(rotX);
		addRenderableWidget(rotY);
		addRenderableWidget(rotZ);
		addRenderableWidget(scaleX);
		addRenderableWidget(scaleY);
		addRenderableWidget(alpha);
		// Add text labels as widgets
		int textOffset = rowHeight / 2 - 4 - 4; // Move up by 4 additional pixels
		
		// Title/blueprint name
		addRenderableWidget(new StringWidget(c1x, row(0) + textOffset, this.font.width(blueprint.getName()), rowHeight, Component.literal(blueprint.getName()), this.font));
		
		// Section headers
		addRenderableWidget(new StringWidget(c1x + 4, 5 + row(1) + textOffset, this.font.width("Position"), rowHeight, Component.literal("Position"), this.font));
		addRenderableWidget(new StringWidget(c2x + 4, 5 + row(1) + textOffset, this.font.width("Rotation"), rowHeight, Component.literal("Rotation"), this.font));
		addRenderableWidget(new StringWidget(c1x + 4, 5 + row(6) + textOffset, this.font.width("Scale"), rowHeight, Component.literal("Scale"), this.font));
		addRenderableWidget(new StringWidget(c2x + 4, 5 + row(6) + textOffset, this.font.width("Alpha"), rowHeight, Component.literal("Alpha"), this.font));
		
		// Axis labels
		addRenderableWidget(new StringWidget(c0x, row(2) + textOffset, this.font.width("X"), rowHeight, Component.literal("X"), this.font));
		addRenderableWidget(new StringWidget(c0x, row(3) + textOffset, this.font.width("Y"), rowHeight, Component.literal("Y"), this.font));
		addRenderableWidget(new StringWidget(c0x, row(4) + textOffset, this.font.width("Z"), rowHeight, Component.literal("Z"), this.font));
		addRenderableWidget(new StringWidget(c0x, row(7) + textOffset, this.font.width("W"), rowHeight, Component.literal("W"), this.font));
		addRenderableWidget(new StringWidget(c0x, row(8) + textOffset, this.font.width("H"), rowHeight, Component.literal("H"), this.font));

		addRenderableWidget(
				Button.builder(Component.literal("To Player"), b -> {
					// round position to nearest 10th
					float x = (float) Math.round(minecraft.player.getX() * 10) / 10;
					float y = (float) Math.round(minecraft.player.getY() * 10) / 10;
					float z = (float) Math.round(minecraft.player.getZ() * 10) / 10;
					blueprint.SetPosition(x,y,z);
							posX.setValue(blueprint.getPosX());
							posY.setValue(blueprint.getPosY());
							posZ.setValue(blueprint.getPosZ());		
				})
						.bounds(c1x, row(5), columnWidth, rowHeight)
						.build());

		addRenderableWidget(
				Button.builder(Component.literal("Reset"), b -> {
					blueprint.setRotationX(0);
					blueprint.setRotationY(0);
					blueprint.setRotationZ(0);
					rotX.setValue(0);
					rotY.setValue(0);
					rotZ.setValue(0);
				})
						.bounds(c2x, row(5), columnWidth, rowHeight)
						.build());

		addRenderableWidget(
				Button.builder(Component.literal("Reset"), b -> {
					blueprint.ResetScale();
					scaleX.setValue(blueprint.getScaleX());
					scaleY.setValue(blueprint.getScaleY());
				})
						.bounds(c1x, row(9), columnWidth, rowHeight)
						.build());

		addRenderableWidget(
				Button.builder(Component.literal("Calibrate"), b -> {
					minecraft.setScreen(new BlueprintCalibrationScreen(blueprint, this));
				})
						.bounds(c2x, row(9), columnWidth, rowHeight)
						.build());

				addRenderableWidget(
				Button.builder(Component.literal("Done"), b -> {
					this.onClose();
				})
						.bounds(width - columnWidth - columnPadding, this.height - rowHeight - rowPadding, columnWidth,
								rowHeight)
						.build());
	}	
		@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
	    super.extractRenderState(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		for (NumberFieldWidget input : inputBoxes) {
			if (input.isHovered()) {
				Minecraft client = Minecraft.getInstance();
				boolean shift = GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
					          GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
				boolean ctrl = GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
					         GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
				if (verticalAmount > 0) {
					input.increment(shift, ctrl);
				} else {
					input.decrement(shift, ctrl);
				}				
				return true;
			}
		}
		if (mouseX > c0x && mouseX < c0x + columnWidth && mouseY > row(6) && mouseY < row(6) + rowHeight) {
			for (NumberFieldWidget input : inputBoxes) {
				if (input == inputBoxes.get(6) || input == inputBoxes.get(7)) {
					Minecraft client = Minecraft.getInstance();
					boolean shift = GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
						          GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
					boolean ctrl = GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
						         GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
					if (verticalAmount > 0) {
						input.increment(shift, ctrl);
					} else {
						input.decrement(shift, ctrl);
					}
				}
			}
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

}