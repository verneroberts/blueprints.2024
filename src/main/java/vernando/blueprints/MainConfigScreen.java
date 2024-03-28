package vernando.blueprints;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

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
          b.setMessage(Text.literal("..."));
          blueprints = main.ScanFileSystemForImages();
          // refresh entire screen
          client.setScreen(new MainConfigScreen(blueprints, main));
        })
            .dimensions(20, 10, 60, 20)
            .build());

    // renderThroughBlocks
    addDrawableChild(
        ButtonWidget.builder(Text.literal(main.getRenderThroughBlocks() ? "Mode: Render all" : "Mode: Render visible"), b -> {
          main.setRenderThroughBlocks(!main.getRenderThroughBlocks());
          b.setMessage(main.getRenderThroughBlocks() ? Text.literal("Mode: Render all") : Text.literal("Mode: Render visible"));
          client.setScreen(new MainConfigScreen(blueprints, main));
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
      List<OrderedText> lines = textRenderer.wrapLines(StringVisitable.plain(blueprint.getName()), width - 80 - 40);
      context.drawTextWithShadow(textRenderer, lines.get(0), 40, y + 5, 0xffffff);
      blueprint.renderThumbnail(context, 10, y - 3, 28, 22);          
    });
  }
}