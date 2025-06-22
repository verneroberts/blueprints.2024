package vernando.blueprints;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

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
		super(Text.literal(Main.MOD_NAME + " " + blueprint.getName() + " Config"));
		this.blueprint = blueprint;
		this.parent = parent;
		this.inputBoxes = new ArrayList<NumberFieldWidget>();
	}

	@Override
	public void close() {
		blueprint.SaveConfig();
		client.setScreen(parent);
		// restore blur setting
		client.options.getMenuBackgroundBlurriness().setValue(blurSetting);

	}
	@Override
	protected void init() {
		System.out.println("BlueprintConfigScreen.init() called");
		System.out.println("Screen dimensions: " + this.width + "x" + this.height);
		
		int width = client.getWindow().getScaledWidth();
		System.out.println("Window scaled width: " + width);

		// make sure the blueprint is visible if we are here
		if (!blueprint.isVisible()) {
			blueprint.setVisibility(true);
		}

		// save the current blur setting then set it to 0
		this.blurSetting = client.options.getMenuBackgroundBlurrinessValue();
		client.options.getMenuBackgroundBlurriness().setValue(0);

		NumberFieldWidget posX = new NumberFieldWidget(textRenderer, c1x, row(2), columnWidth, rowHeight,
				blueprint.getPosX(), (v) -> blueprint.setPosX(v), "X Position", 1);
		NumberFieldWidget posY = new NumberFieldWidget(textRenderer, c1x, row(3), columnWidth, rowHeight,
				blueprint.getPosY(), (v) -> blueprint.setPosY(v), "Y Position", 1);
		NumberFieldWidget posZ = new NumberFieldWidget(textRenderer, c1x, row(4), columnWidth, rowHeight,
				blueprint.getPosZ(), (v) -> blueprint.setPosZ(v), "Z Position", 1);
		NumberFieldWidget rotX = new NumberFieldWidget(textRenderer, c2x, row(2), columnWidth, rowHeight,
				blueprint.getRotationX(), (v) -> blueprint.setRotationX(v), "X Rotation", 1);
		NumberFieldWidget rotY = new NumberFieldWidget(textRenderer, c2x, row(3), columnWidth, rowHeight,
				blueprint.getRotationY(), (v) -> blueprint.setRotationY(v), "Y Rotation", 1);
		NumberFieldWidget rotZ = new NumberFieldWidget(textRenderer, c2x, row(4), columnWidth, rowHeight,
				blueprint.getRotationZ(), (v) -> blueprint.setRotationZ(v), "Z Rotation", 1);
		NumberFieldWidget scaleX = new NumberFieldWidget(textRenderer, c1x, row(7), columnWidth, rowHeight,
				blueprint.getScaleX(), (v) -> blueprint.setScaleX(v), "Width", 1);
		NumberFieldWidget scaleY = new NumberFieldWidget(textRenderer, c1x, row(8), columnWidth, rowHeight,
				blueprint.getScaleY(), (v) -> blueprint.setScaleY(v), "Height", 1);
		NumberFieldWidget alpha = new NumberFieldWidget(textRenderer, c2x, row(7), columnWidth, rowHeight,
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
		addDrawableChild(posX);
		addDrawableChild(posY);
		addDrawableChild(posZ);
		addDrawableChild(rotX);
		addDrawableChild(rotY);
		addDrawableChild(rotZ);
		addDrawableChild(scaleX);
		addDrawableChild(scaleY);
		addDrawableChild(alpha);
		// Add text labels as widgets
		int textOffset = rowHeight / 2 - 4 - 4; // Move up by 4 additional pixels
		
		// Title/blueprint name
		addDrawableChild(new TextWidget(c1x, row(0) + textOffset, this.textRenderer.getWidth(blueprint.getName()), rowHeight, Text.literal(blueprint.getName()), this.textRenderer));
		
		// Section headers
		addDrawableChild(new TextWidget(c1x + 4, 5 + row(1) + textOffset, this.textRenderer.getWidth("Position"), rowHeight, Text.literal("Position"), this.textRenderer));
		addDrawableChild(new TextWidget(c2x + 4, 5 + row(1) + textOffset, this.textRenderer.getWidth("Rotation"), rowHeight, Text.literal("Rotation"), this.textRenderer));
		addDrawableChild(new TextWidget(c1x + 4, 5 + row(6) + textOffset, this.textRenderer.getWidth("Scale"), rowHeight, Text.literal("Scale"), this.textRenderer));
		addDrawableChild(new TextWidget(c2x + 4, 5 + row(6) + textOffset, this.textRenderer.getWidth("Alpha"), rowHeight, Text.literal("Alpha"), this.textRenderer));
		
		// Axis labels
		addDrawableChild(new TextWidget(c0x, row(2) + textOffset, this.textRenderer.getWidth("X"), rowHeight, Text.literal("X"), this.textRenderer));
		addDrawableChild(new TextWidget(c0x, row(3) + textOffset, this.textRenderer.getWidth("Y"), rowHeight, Text.literal("Y"), this.textRenderer));
		addDrawableChild(new TextWidget(c0x, row(4) + textOffset, this.textRenderer.getWidth("Z"), rowHeight, Text.literal("Z"), this.textRenderer));
		addDrawableChild(new TextWidget(c0x, row(7) + textOffset, this.textRenderer.getWidth("W"), rowHeight, Text.literal("W"), this.textRenderer));
		addDrawableChild(new TextWidget(c0x, row(8) + textOffset, this.textRenderer.getWidth("H"), rowHeight, Text.literal("H"), this.textRenderer));

		addDrawableChild(
				ButtonWidget.builder(Text.literal("To Player"), b -> {
					// round position to nearest 10th
					float x = (float) Math.round(client.player.getX() * 10) / 10;
					float y = (float) Math.round(client.player.getY() * 10) / 10;
					float z = (float) Math.round(client.player.getZ() * 10) / 10;
					blueprint.SetPosition(x,y,z);
							posX.setValue(blueprint.getPosX());
							posY.setValue(blueprint.getPosY());
							posZ.setValue(blueprint.getPosZ());		
				})
						.dimensions(c1x, row(5), columnWidth, rowHeight)
						.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Reset"), b -> {
					blueprint.setRotationX(0);
					blueprint.setRotationY(0);
					blueprint.setRotationZ(0);
					rotX.setValue(0);
					rotY.setValue(0);
					rotZ.setValue(0);
				})
						.dimensions(c2x, row(5), columnWidth, rowHeight)
						.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Reset"), b -> {
					blueprint.ResetScale();
					scaleX.setValue(blueprint.getScaleX());
					scaleY.setValue(blueprint.getScaleY());
				})
						.dimensions(c1x, row(9), columnWidth, rowHeight)
						.build());
				addDrawableChild(
				ButtonWidget.builder(Text.literal("Done"), b -> {
					this.close();
				})
						.dimensions(width - columnWidth - columnPadding, this.height - rowHeight - rowPadding, columnWidth,
								rowHeight)
						.build());
	}	
		@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {		
	    super.render(context, mouseX, mouseY, delta);
		// Text labels are now handled by TextWidget instances, no manual rendering needed
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {

		// work out which input box is being scrolled over and adjust the value accordingly
		for (NumberFieldWidget input : inputBoxes) {
			if (input.isHovered()) {
				if (verticalAmount > 0) {
					input.increment(Screen.hasShiftDown(), Screen.hasControlDown());
				} else {
					input.decrement(Screen.hasShiftDown(), Screen.hasControlDown());
				}				
				return true;
			}
		}

		// special case for scale - if the mouse is over the label, adjust both values
		if (mouseX > c0x && mouseX < c0x + columnWidth && mouseY > row(6) && mouseY < row(6) + rowHeight) {
			for (NumberFieldWidget input : inputBoxes) {
				if (input == inputBoxes.get(6) || input == inputBoxes.get(7)) {
					if (verticalAmount > 0) {
						input.increment(Screen.hasShiftDown(), Screen.hasControlDown());
					} else {
						input.decrement(Screen.hasShiftDown(), Screen.hasControlDown());
					}
				}
			}
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

}