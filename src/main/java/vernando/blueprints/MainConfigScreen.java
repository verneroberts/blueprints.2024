package vernando.blueprints;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MainConfigScreen extends Screen {
  private ArrayList<Blueprint> blueprints;
  private Main main;

  protected MainConfigScreen(ArrayList<Blueprint> blueprints, Main main) {
    super(Text.literal(Main.MOD_NAME + " Config"));
    this.blueprints = blueprints;
    this.main = main;
  }

  @Override
  protected void init() {

    // main config section
    addDrawableChild(
        ButtonWidget.builder(Text.literal("Reload"), b -> {
          main.ScanFileSystemForImages();
          b.setFocused(false);
        })
            .dimensions(20, 10, 60, 20)
            .build());

    // renderThroughBlocks
    addDrawableChild(
        ButtonWidget.builder(Text.literal(main.getRenderThroughBlocks() ? "Mode: Render all" : "Mode: Render visible"), b -> {
          main.setRenderThroughBlocks(!main.getRenderThroughBlocks());
          b.setMessage(main.getRenderThroughBlocks() ? Text.literal("Mode: Render all") : Text.literal("Mode: Render visible"));
          b.setFocused(false);
        })
            .dimensions(90, 10, 140, 20)
            .build());

    // blueprint list
    blueprints.forEach(blueprint -> {
      addDrawableChild(ButtonWidget.builder(Text.literal("Configure"), b -> {
        client.setScreen(new BlueprintConfigScreen(blueprint, this));
      })
          .dimensions(width - 80, 40 + blueprints.indexOf(blueprint) * 24, 60, 20)
          .build());
    });
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);

    blueprints.forEach((blueprint) -> {
      int y = 40 + blueprints.indexOf(blueprint) * 24;
      context.drawTextWithShadow(textRenderer, Text.literal(blueprint.getName()), 50, y + 10, 0xffffff);
      blueprint.renderThumbnail(context, 20, y + 1, 18, 18);
    });
  }
}