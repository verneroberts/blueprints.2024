package vernando.blueprints;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
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
		int width = client.getWindow().getScaledWidth();

		// save the current blur setting then set it to 0
		this.blurSetting = client.options.getMenuBackgroundBlurrinessValue();
		client.options.getMenuBackgroundBlurriness().setValue(0);

		// add a grid of input boxes on the right side of the screen
		// these boxes are used to set the position, rotation, scale, and alpha of the blueprint
		int rowHeight = 17;
		int columnWidth = 18;
		int columnPadding = 5;
		int rowPadding = 5;
		int startX = width - (columnWidth + columnPadding) * 3;
		int startY = 20;
		
		// position
		NumberFieldWidget posX = new NumberFieldWidget(textRenderer, startX, startY, columnWidth, rowHeight, blueprint.getPosX(), (v) -> blueprint.setPosX(v), "X Position");
		addDrawable(posX);
		inputBoxes.add(posX);
		startX += columnWidth + columnPadding;

		NumberFieldWidget posY = new NumberFieldWidget(textRenderer, startX, startY, columnWidth, rowHeight, blueprint.getPosY(), (v) -> blueprint.setPosY(v), "Y Position");
		addDrawable(posY);
		inputBoxes.add(posY);
		startX += columnWidth + columnPadding;

		NumberFieldWidget posZ = new NumberFieldWidget(textRenderer, startX, startY, columnWidth, rowHeight, blueprint.getPosZ(), (v) -> blueprint.setPosZ(v), "Z Position");
		addDrawable(posZ);
		inputBoxes.add(posZ);
		startY += rowHeight + rowPadding;
		startX = width - (columnWidth + columnPadding) * 3;

		// rotation
		NumberFieldWidget rotX = new NumberFieldWidget(textRenderer, startX, startY, columnWidth, rowHeight, blueprint.getRotationX(), (v) -> blueprint.setRotationX(v), "X Rotation");
		addDrawable(rotX);
		inputBoxes.add(rotX);
		startX += columnWidth + columnPadding;

		NumberFieldWidget rotY = new NumberFieldWidget(textRenderer, startX, startY, columnWidth, rowHeight, blueprint.getRotationY(), (v) -> blueprint.setRotationY(v), "Y Rotation");
		addDrawable(rotY);
		inputBoxes.add(rotY);
		startX += columnWidth + columnPadding;

		NumberFieldWidget rotZ = new NumberFieldWidget(textRenderer, startX, startY, columnWidth, rowHeight, blueprint.getRotationZ(), (v) -> blueprint.setRotationZ(v), "Z Rotation");
		addDrawable(rotZ);
		inputBoxes.add(rotZ);
		startY += rowHeight + rowPadding;
		startX = width - (columnWidth + columnPadding) * 3;

		// scale
		NumberFieldWidget scaleX = new NumberFieldWidget(textRenderer, startX, startY, columnWidth, rowHeight, blueprint.getScaleX(), (v) -> blueprint.setScaleX(v), "X Scale");
		addDrawable(scaleX);
		inputBoxes.add(scaleX);
		startX += columnWidth + columnPadding;

		NumberFieldWidget scaleY = new NumberFieldWidget(textRenderer, startX, startY, columnWidth, rowHeight, blueprint.getScaleY(), (v) -> blueprint.setScaleY(v), "Y Scale");
		addDrawable(scaleY);
		inputBoxes.add(scaleY);
		startX += columnWidth + columnPadding;

		// alpha
		NumberFieldWidget alpha = new NumberFieldWidget(textRenderer, startX, startY, columnWidth, rowHeight, blueprint.getAlpha(), (v) -> blueprint.setAlpha(v), "Alpha");
		addDrawable(alpha);
		inputBoxes.add(alpha);
		startY += rowHeight + rowPadding;
		startX = width - (columnWidth + columnPadding) * 3;


	}	

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
	
		context.drawTextWithShadow(textRenderer, Text.literal(blueprint.getName()), 20, 5, 0xffffff);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {			
		
		float scrollAmount = (float)verticalAmount * 0.1f;
		if (Screen.hasControlDown() && Screen.hasShiftDown()) {
			scrollAmount *= 50f;
		} else {
			if (Screen.hasShiftDown()) {
				scrollAmount *= 0.01f;
			}
			if (Screen.hasControlDown()) {
				scrollAmount *= 10f;
			}
		}

		// work out which input box is being scrolled over and adjust the value accordingly	
		for (NumberFieldWidget input : inputBoxes) {
			if (input.isHovered()) {
				float newValue = input.getValue() + scrollAmount;
				input.setValue(newValue);
				return true;
			}
		}	
		
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}


}