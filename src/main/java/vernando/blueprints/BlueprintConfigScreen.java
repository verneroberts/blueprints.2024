package vernando.blueprints;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction.Axis;
import vernando.blueprints.Util.Direction;
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

	@Override
	protected void init() {
		int width = client.getWindow().getScaledWidth();
		int panelWidth = 60;
		int startX = width - panelWidth;
		int startY = 10;
		int rowHeight = 10;
		int columnWidth = 10;
		int buttonWidth = 10;
		int buttonHeight = 10;

		boolean shiftHeld = false;

		addDrawableChild(
				ButtonWidget.builder(Text.literal("\u25B2"), b -> {
					blueprint.NudgePosition(Direction.UP, shiftHeld);
				})
			.dimensions(startX + 1 * columnWidth, startY + 1 * rowHeight, buttonWidth, buttonHeight)
			.build());
		
		addDrawableChild(
				ButtonWidget.builder(Text.literal("\u25BC"), b -> {
					blueprint.NudgePosition(Direction.DOWN, shiftHeld);
				})
			.dimensions(startX + 1 * columnWidth, startY + 2 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("\u25C0"), b -> {
					Direction directionFacing = Util.PlayerFacingDirection();
					switch (directionFacing) {
						case NORTH:
							blueprint.NudgePosition(Direction.WEST, shiftHeld);
							break;
						case EAST:
							blueprint.NudgePosition(Direction.NORTH, shiftHeld);
							break;
						case SOUTH:
							blueprint.NudgePosition(Direction.EAST, shiftHeld);
							break;
						case WEST:
							blueprint.NudgePosition(Direction.SOUTH, shiftHeld);
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
							blueprint.NudgePosition(Direction.EAST, shiftHeld);
							break;
						case EAST:
							blueprint.NudgePosition(Direction.SOUTH, shiftHeld);
							break;
						case SOUTH:
							blueprint.NudgePosition(Direction.WEST, shiftHeld);
							break;
						case WEST:
							blueprint.NudgePosition(Direction.NORTH, shiftHeld);
							break;
						default:
							break;
					}
				})
			.dimensions(startX + 2 * columnWidth, (int)(startY + 1.5 * rowHeight), buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("X+"), b -> {
					blueprint.NudgeRotation(Axis.X, shiftHeld);
				})
			.dimensions(startX + 0 * columnWidth, startY + 4 * rowHeight, buttonWidth, buttonHeight)
			.build());
		
		addDrawableChild(
				ButtonWidget.builder(Text.literal("X-"), b -> {
					blueprint.NudgeRotation(Axis.X, shiftHeld);
				})
			.dimensions(startX + 0 * columnWidth, startY + 5 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Y+"), b -> {
					blueprint.NudgeRotation(Axis.Y, shiftHeld);
				})
			.dimensions(startX + 1 * columnWidth, startY + 4 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Y-"), b -> {
					blueprint.NudgeRotation(Axis.Y, shiftHeld);
				})
			.dimensions(startX + 1 * columnWidth, startY + 5 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Z+"), b -> {
					blueprint.NudgeRotation(Axis.Z, shiftHeld);
				})
			.dimensions(startX + 2 * columnWidth, startY + 4 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Z-"), b -> {
					blueprint.NudgeRotation(Axis.Z, shiftHeld);
				})
			.dimensions(startX + 2 * columnWidth, startY + 5 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("X+"), b -> {
					blueprint.NudgeScale(Axis.X, 1, shiftHeld);
				})
			.dimensions(startX + 0 * columnWidth, startY + 7 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("X-"), b -> {
					blueprint.NudgeScale(Axis.X, -1, shiftHeld);
				})
			.dimensions(startX + 0 * columnWidth, startY + 8 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Y+"), b -> {
					blueprint.NudgeScale(Axis.Y, 1, shiftHeld);
				})
			.dimensions(startX + 1 * columnWidth, startY + 7 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Y-"), b -> {
					blueprint.NudgeScale(Axis.Y, -1, shiftHeld);
				})
			.dimensions(startX + 1 * columnWidth, startY + 8 * rowHeight, buttonWidth, buttonHeight)
			.build());
			
		addDrawableChild(
				ButtonWidget.builder(Text.literal("A-"), b -> {
					blueprint.alpha -= 0.1;
				})
			.dimensions(startX + 0 * columnWidth, startY + 10 * rowHeight, buttonWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("A+"), b -> {
					blueprint.alpha += 0.1;
				})
			.dimensions(startX + 1 * columnWidth, startY + 10 * rowHeight, buttonWidth, buttonHeight)
			.build());
			
		addDrawableChild(
				ButtonWidget.builder(Text.literal("Reset Position"), b -> {
					 blueprint.SetPosition((float) client.player.getX(), (float) client.player.getY(), (float) client.player.getZ());
				})
			.dimensions(startX + 0 * columnWidth, startY + 12 * rowHeight, panelWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Toggle Visibility"), b -> {
					blueprint.ToggleVisibility();
				})
			.dimensions(startX + 0 * columnWidth, startY + 13 * rowHeight, panelWidth, buttonHeight)
			.build());

		addDrawableChild(
				ButtonWidget.builder(Text.literal("Reset Rotation"), b -> {
					blueprint.rotationX = 0;
					blueprint.rotationY = 0;
					blueprint.rotationZ = 0;
				})
			.dimensions(startX + 0 * columnWidth, startY + 14 * rowHeight, panelWidth, buttonHeight)
			.build());

			addDrawableChild(
				ButtonWidget.builder(Text.literal("Close"), b -> {
					close();
				})
			.dimensions(startX + 0 * columnWidth, startY + 15 * rowHeight, panelWidth, buttonHeight)
			.build());

	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		context.drawTextWithShadow(textRenderer, Text.literal(blueprint.getName()), 20, 20, 0xffffff);
	}
}