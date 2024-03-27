package vernando.blueprints;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction.Axis;
import vernando.blueprints.Util.Direction;
import net.minecraft.client.MinecraftClient;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BlueprintConfigScreen extends Screen {
	private Blueprint blueprint;
	private Screen parent;

	protected BlueprintConfigScreen(Blueprint blueprint, Screen parent) {
		super(Text.literal(Main.MOD_NAME + " " + blueprint.getName() + " Config"));
		this.blueprint = blueprint;
		this.parent = parent;
	}

	@Override
	public void close() {
		blueprint.SaveConfig();
		client.setScreen(parent);
	}
	int rowHeight = 17;
	int columnWidth = 18;
	int buttonWidth = 15;
	int buttonHeight = 15;
	int startY = 0;
	int panelWidthPadding = 3;
	int panelWidth = 3 * columnWidth - (columnWidth - buttonWidth);
	private boolean shiftPressed;
	private boolean ctrlPressed;


	@Override
	protected void init() {
		int width = client.getWindow().getScaledWidth();
		int startX = width - panelWidth - 3;

		MinecraftClient client = MinecraftClient.getInstance();

		addDrawableChild(
				ButtonWidget.builder(Text.literal("\u25B2"), b -> {
					blueprint.NudgePosition(Direction.UP, 0.1f, shiftPressed, ctrlPressed);
				})
			.dimensions(startX + 1 * columnWidth, startY + 1 * rowHeight, buttonWidth, buttonHeight)
			.build());
		
		addDrawableChild(
				ButtonWidget.builder(Text.literal("\u25BC"), b -> {
					blueprint.NudgePosition(Direction.DOWN, 0.1f, shiftPressed, ctrlPressed);
				})
			.dimensions(startX + 1 * columnWidth, startY + 2 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("\u25C0"), b -> {
					Direction directionFacing = Util.PlayerFacingDirection();
					switch (directionFacing) {
						case NORTH:
							blueprint.NudgePosition(Direction.WEST, 0.1f, shiftPressed, ctrlPressed);
							break;
						case EAST:
							blueprint.NudgePosition(Direction.NORTH, 0.1f, shiftPressed, ctrlPressed);
							break;
						case SOUTH:
							blueprint.NudgePosition(Direction.EAST, 0.1f, shiftPressed, ctrlPressed);
							break;
						case WEST:
							blueprint.NudgePosition(Direction.SOUTH, 0.1f, shiftPressed, ctrlPressed);
							break;
						default:
							break;
					}
				})
			.dimensions(startX + 0 * columnWidth, (int)(startY + 1.5 * rowHeight), buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("\u25B6"), b -> {
					Direction directionFacing = Util.PlayerFacingDirection();
					switch (directionFacing) {
						case NORTH:
							blueprint.NudgePosition(Direction.EAST, 0.1f, shiftPressed, ctrlPressed);
							break;
						case EAST:
							blueprint.NudgePosition(Direction.SOUTH, 0.1f, shiftPressed, ctrlPressed);
							break;
						case SOUTH:
							blueprint.NudgePosition(Direction.WEST, 0.1f, shiftPressed, ctrlPressed);
							break;
						case WEST:
							blueprint.NudgePosition(Direction.NORTH, 0.1f, shiftPressed, ctrlPressed);
							break;
						default:
							break;
					}
				})
			.dimensions(startX + 2 * columnWidth, (int)(startY + 1.5 * rowHeight), buttonWidth, buttonHeight)
			.build());

			addDrawableChild(
				ButtonWidget.builder(Text.literal("Reset"), b -> {
					 blueprint.SetPosition((float) client.player.getX(), (float) client.player.getY(), (float) client.player.getZ());
				})
			.dimensions(startX + 0 * columnWidth, startY + 3 * rowHeight, panelWidth, buttonHeight)
			.build());


		addDrawableChild(
				ButtonWidget.builder(Text.literal("X+"), b -> {
					blueprint.NudgeRotation(Axis.X, 1, shiftPressed, ctrlPressed);
				})
			.dimensions(startX + 0 * columnWidth, startY + 5 * rowHeight, buttonWidth, buttonHeight)
			.build());
		
		addDrawableChild(
				ButtonWidget.builder(Text.literal("X-"), b -> {
					blueprint.NudgeRotation(Axis.X, -1, shiftPressed, ctrlPressed);
				})
			.dimensions(startX + 0 * columnWidth, startY + 6 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Y+"), b -> {
					blueprint.NudgeRotation(Axis.Y, 1, shiftPressed, ctrlPressed);
				})
			.dimensions(startX + 1 * columnWidth, startY + 5 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Y-"), b -> {
					blueprint.NudgeRotation(Axis.Y, -1, shiftPressed, ctrlPressed);
				})
			.dimensions(startX + 1 * columnWidth, startY + 6 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Z+"), b -> {
					blueprint.NudgeRotation(Axis.Z, 1, shiftPressed, ctrlPressed);
				})
			.dimensions(startX + 2 * columnWidth, startY + 5 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Z-"), b -> {
					blueprint.NudgeRotation(Axis.Z, -1, shiftPressed, ctrlPressed);
				})
			.dimensions(startX + 2 * columnWidth, startY + 6 * rowHeight, buttonWidth, buttonHeight)
			.build());

			addDrawableChild(
				ButtonWidget.builder(Text.literal("Reset"), b -> {
					blueprint.rotationX = 0;
					blueprint.rotationY = 0;
					blueprint.rotationZ = 0;
				})
			.dimensions(startX + 0 * columnWidth, startY + 7 * rowHeight, panelWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("X+"), b -> {
					blueprint.NudgeScale(Axis.X, 0.1f, shiftPressed, ctrlPressed);
				})
			.dimensions(startX + 0 * columnWidth, startY + 9 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("X-"), b -> {
					blueprint.NudgeScale(Axis.X, -0.1f, shiftPressed, ctrlPressed);
				})
			.dimensions(startX + 0 * columnWidth, startY + 10 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Y+"), b -> {
					blueprint.NudgeScale(Axis.Y, 0.1f, shiftPressed, ctrlPressed);
				})
			.dimensions(startX + 1 * columnWidth, startY + 9 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Y-"), b -> {
					blueprint.NudgeScale(Axis.Y, -0.1f, shiftPressed, ctrlPressed);
				})
			.dimensions(startX + 1 * columnWidth, startY + 10 * rowHeight, buttonWidth, buttonHeight)
			.build());
			
		addDrawableChild(
				ButtonWidget.builder(Text.literal("A-"), b -> {
					blueprint.NudgeAlpha(-0.1f, shiftPressed, ctrlPressed);
				})
			.dimensions(startX + 0 * columnWidth, startY + 12 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("A+"), b -> {
					blueprint.NudgeAlpha(0.1f, shiftPressed, ctrlPressed);
				})
			.dimensions(startX + 1 * columnWidth, startY + 12 * rowHeight, buttonWidth, buttonHeight)
			.build());
			
		addDrawableChild(
				ButtonWidget.builder(Text.literal(blueprint.isVisible() ? "Hide" : "Show"), b -> {
					blueprint.ToggleVisibility();
					if (blueprint.isVisible()) {
						b.setMessage(Text.literal("Hide"));
					} else {
						b.setMessage(Text.literal("Show"));
					}
				})
			.dimensions(startX + 0 * columnWidth, startY + 13 * rowHeight, panelWidth, buttonHeight)
			.build());


	}

	@Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 340) {
			shiftPressed = true;
		}
		if (keyCode == 341) {
			ctrlPressed = true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 340) {
			shiftPressed = false;
		}
		if (keyCode == 341) {
			ctrlPressed = false;
		}
		return super.keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		int width = client.getWindow().getScaledWidth();
		int startX = width - panelWidth;
		int textYOffset = 5;

		context.drawTextWithShadow(textRenderer, Text.literal(blueprint.getName()), 20, textYOffset, 0xffffff);

		context.drawTextWithShadow(textRenderer, Text.literal("Position"), startX, startY + textYOffset, 0xffffff);
		context.drawTextWithShadow(textRenderer, Text.literal("Rotation"), startX, startY + 4 * rowHeight + textYOffset, 0xffffff);
		context.drawTextWithShadow(textRenderer, Text.literal("Scale"), startX, startY + 8 * rowHeight + textYOffset, 0xffffff);
		context.drawTextWithShadow(textRenderer, Text.literal("Alpha"), startX, startY + 11 * rowHeight + textYOffset, 0xffffff);
	}
}