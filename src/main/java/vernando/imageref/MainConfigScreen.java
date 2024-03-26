package vernando.imageref;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MainConfigScreen extends Screen {
  private ArrayList<Blueprint> blueprints;

  protected MainConfigScreen(ArrayList<Blueprint> blueprints) {
    super(Text.literal(Main.MOD_NAME + " Config"));
    this.blueprints = blueprints;
  }
 
  @Override
  protected void init() {

    // main config section


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
    });
  }
}