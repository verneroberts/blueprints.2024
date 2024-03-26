package vernando.imageref;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import vernando.imageref.Util.Direction;
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
    client.setScreen(parent);
  }
 
  @Override
  protected void init() {
	int width = client.getWindow().getScaledWidth();

	addDrawableChild(
	ButtonWidget.builder(Text.literal("Nudge Down"), b -> {
		blueprint.NudgePosition(Direction.DOWN, false);
	})
		.dimensions(width-80, 40, 60, 20)
		.build()
	);	  

	addDrawableChild(
	ButtonWidget.builder(Text.literal("Nudge Up"), b -> {
		blueprint.NudgePosition(Direction.UP, false);
	})
		.dimensions(width-80, 60, 60, 20)
		.build()
	);

	addDrawableChild(
	ButtonWidget.builder(Text.literal("Nudge Left"), b -> {
		boolean multiply = false;
		Direction directionFacing = Util.PlayerFacingDirection();
		switch (directionFacing) {
			case NORTH:
				blueprint.NudgePosition(Direction.WEST, multiply);
				break;
			case EAST:
				blueprint.NudgePosition(Direction.NORTH, multiply);
				break;
			case SOUTH:
				blueprint.NudgePosition(Direction.EAST, multiply);
				break;
			case WEST:
				blueprint.NudgePosition(Direction.SOUTH, multiply);
				break;
			default:
				break;
		}
	})
		.dimensions(width-80, 80, 60, 20)
		.build()
	);

	addDrawableChild(
	ButtonWidget.builder(Text.literal("Nudge Right"), b -> {
		boolean multiply = false;
		Direction directionFacing = Util.PlayerFacingDirection();
		switch (directionFacing) {
			case NORTH:
				blueprint.NudgePosition(Direction.EAST, multiply);
				break;
			case EAST:
				blueprint.NudgePosition(Direction.SOUTH, multiply);
				break;
			case SOUTH:
				blueprint.NudgePosition(Direction.WEST, multiply);
				break;
			case WEST:
				blueprint.NudgePosition(Direction.NORTH, multiply);
				break;
			default:
				break;
		}
	})
		.dimensions(width-80, 100, 60, 20)
		.build()
	);

		
	  
/* 
    			Boolean multiply = keyNudgeMultiply.isPressed();
			while (keyNudgeDown.wasPressed()) {	
				activeReferenceImage.NudgePosition(Direction.DOWN, multiply);
			}
			while (keyNudgeUp.wasPressed()) {
				activeReferenceImage.NudgePosition(Direction.UP, multiply);
			}
			while (keyNudgeLeft.wasPressed()) {
				switch (directionFacing) {
					case NORTH:
						activeReferenceImage.NudgePosition(Direction.WEST, multiply);
						break;
					case EAST:
						activeReferenceImage.NudgePosition(Direction.NORTH, multiply);
						break;
					case SOUTH:
						activeReferenceImage.NudgePosition(Direction.EAST, multiply);
						break;
					case WEST:
						activeReferenceImage.NudgePosition(Direction.SOUTH, multiply);
						break;
					default:
						break;
				}
			}
			while (keyNudgeRight.wasPressed()) {
				switch (directionFacing) {
					case NORTH:
						activeReferenceImage.NudgePosition(Direction.EAST, multiply);
						break;
					case EAST:
						activeReferenceImage.NudgePosition(Direction.SOUTH, multiply);
						break;
					case SOUTH:
						activeReferenceImage.NudgePosition(Direction.WEST, multiply);
						break;
					case WEST:
						activeReferenceImage.NudgePosition(Direction.NORTH, multiply);
						break;
					default:
						break;
				}
			}
			while (keyScaleXUp.wasPressed()) {
				activeReferenceImage.NudgeScale(Axis.X, 1, multiply);
			}
			while (keyScaleXDown.wasPressed()) {
				activeReferenceImage.NudgeScale(Axis.X, -1, multiply);
			}
			while (keyScaleYUp.wasPressed()) {
				activeReferenceImage.NudgeScale(Axis.Y, 1, multiply);
			}
			while (keyScaleYDown.wasPressed()) {
				activeReferenceImage.NudgeScale(Axis.Y, -1, multiply);
			}

			while (keyRenderThroughBlocks.wasPressed()) {
				Config.renderThroughBlocks = !Config.renderThroughBlocks;
				client.player.sendMessage(Text.of("[" + MOD_NAME + "] Render Through Blocks: " + Config.renderThroughBlocks), false);
			}
			while (keySetPositionToPlayer.wasPressed()) {
				activeReferenceImage.SetPosition((float) client.player.getX(), (float) client.player.getY(), (float) client.player.getZ());
				client.player.sendMessage(Text.of("[" + MOD_NAME + "] Changed position to current player position: " + client.player.getX() + ", " + client.player.getY() + ", " + client.player.getZ()), false);
			}			
			while (keyCycleNextImage.wasPressed()) {
				int index = referenceImages.indexOf(activeReferenceImage);
				index++;
				if (index >= referenceImages.size()) {
					index = 0;
				}
				if (referenceImages.size() == 0) {
					activeReferenceImage = null;
					return;
				}
				activeReferenceImage = referenceImages.get(index);			
				thumbnailDisplayTimer = 20f;	
			}
			while (keyCycleOrientation.wasPressed()) {
				// rotate clockwise based on the direction the player is facing
				switch (directionFacing) {
					case NORTH:
						activeReferenceImage.NudgeRotation(Axis.Y, multiply);
						break;
					case EAST:
						activeReferenceImage.NudgeRotation(Axis.X, multiply);
						break;
					case SOUTH:
						activeReferenceImage.NudgeRotation(Axis.Y, multiply);
						break;
					case WEST:
						activeReferenceImage.NudgeRotation(Axis.X, multiply);
						break;
					case UP: 
						activeReferenceImage.NudgeRotation(Axis.Z, multiply);
						break;
					case DOWN:
						activeReferenceImage.NudgeRotation(Axis.Z, multiply);
						break;				
				}
			}

			while (keyToggleAlpha.wasPressed()) {
				activeReferenceImage.ToggleAlpha();
			}
*/
  }

  

   @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);

      context.drawTextWithShadow(textRenderer, Text.literal(blueprint.getName()), 20, 20, 0xffffff);
  }
}