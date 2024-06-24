package vernando.blueprints;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

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
            .dimensions(10, 10, 60, 20)
            .build());

    // renderThroughBlocks
    addDrawableChild(
        ButtonWidget.builder(Text.literal(main.getRenderThroughBlocks() ? "Mode: Render all" : "Mode: Render visible"), b -> {
          main.setRenderThroughBlocks(!main.getRenderThroughBlocks());
          b.setMessage(main.getRenderThroughBlocks() ? Text.literal("Mode: Render all") : Text.literal("Mode: Render visible"));
          client.setScreen(new MainConfigScreen(blueprints, main));
        })
            .dimensions(80, 10, 140, 20)
            .build());

    // blueprint list
    blueprints.forEach(blueprint -> {
      addDrawableChild(ButtonWidget.builder(Text.literal("Configure"), b -> {
        client.setScreen(new BlueprintConfigScreen(blueprint, this));
      })
          .dimensions(width - 75, 50 + blueprints.indexOf(blueprint) * 24, 60, 20)
          .build());
    });
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);

    context.drawTextWithShadow(textRenderer, "Path: " + Util.GetPerWorldDimensionConfigPath(), 10, 35, 0xffffff);

    blueprints.forEach((blueprint) -> {
      int y = 50 + blueprints.indexOf(blueprint) * 24;
      List<OrderedText> lines = textRenderer.wrapLines(StringVisitable.plain(blueprint.getName()), width - 80 - 40);
      context.drawTextWithShadow(textRenderer, lines.get(0), 40, y + 5, 0xffffff);
      blueprint.renderThumbnail(context, 10, y - 3, 28, 22);
    });

    context.drawTexture(Identifier.of(Main.MOD_ID, "icon.png"), width-45, 10, 0, 0, 30, 30, 128, 128);
  }
}