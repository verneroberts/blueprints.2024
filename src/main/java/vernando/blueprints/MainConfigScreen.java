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
      ButtonWidget.builder(Text.literal("Rescan folder"), b -> {
        main.ScanFileSystemForImages();
      })
        .dimensions(20, 20, 60, 20)
        .build()
    );

    // renderThroughBlocks
    addDrawableChild(
      ButtonWidget.builder(Text.literal("Render through blocks"), b -> {
        main.setRenderThroughBlocks(!main.getRenderThroughBlocks());
      })
        .dimensions(20, 40, 60, 20)
        .build()
    );

    // blueprint list
    blueprints.forEach(blueprint -> {
      ButtonWidget button = ButtonWidget.builder(Text.literal("Configure"), b -> {
        // launch blueprint config screen
        client.setScreen(new BlueprintConfigScreen(blueprint, this));
      })
        .dimensions(width-80, blueprints.indexOf(blueprint)*20 , 60, 20)
        .build();
      addDrawableChild(button);
    });
  }

   @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);

    blueprints.forEach((blueprint) -> {
      context.drawTextWithShadow(textRenderer, Text.literal(blueprint.getName()), 20, 10+blueprints.indexOf(blueprint)*20, 0xffffff);
      context.drawTexture(blueprint.textureId, 20, 20+blueprints.indexOf(blueprint)*20, 0, 0, 16, 16);
    });
  }
}