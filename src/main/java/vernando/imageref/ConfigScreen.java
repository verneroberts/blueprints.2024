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
public class ConfigScreen extends Screen {
  private ArrayList<ReferenceImage> referenceImages;

  protected ConfigScreen(ArrayList<ReferenceImage> referenceImages) {
    super(Text.literal(ImageRef.MOD_NAME + " Config"));
    this.referenceImages = referenceImages;
  }
 
  @Override
  protected void init() {
    referenceImages.forEach((referenceImage) -> {

      ButtonWidget button = ButtonWidget.builder(Text.literal("Configure"), b -> {
        referenceImage.ToggleAlpha();
      })
        .dimensions(width-80, referenceImages.indexOf(referenceImage)*20 , 60, 20)
        .build();
      addDrawableChild(button);
    });
  }

   @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);

    referenceImages.forEach((referenceImage) -> {
      context.drawTextWithShadow(textRenderer, Text.literal(referenceImage.getName()), 20, 10+referenceImages.indexOf(referenceImage)*20, 0xffffff);
    });
  }
}