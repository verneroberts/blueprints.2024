package vernando.blueprints;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.ArrayList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MainConfigScreen extends Screen {
  private ArrayList<Blueprint> blueprints;
  private Main main;
  private int imagesPerRow = 5;
  private int imageWidth = 100;
  private int imageHeight = 100;
  private int pageOffset = 0;
  private int rowsPerPage = 3;
  private boolean shiftPressed;
  private boolean ctrlPressed;

  protected MainConfigScreen(ArrayList<Blueprint> blueprints, Main main) {
    super(Text.literal(Main.MOD_NAME + " Config"));
    this.blueprints = blueprints;
    this.main = main;
  }

  @Override
  protected void init() {
    imagesPerRow = main.getImagesPerRow();
    if (imagesPerRow < 5 || imagesPerRow > 20) {
      imagesPerRow = 5;
    }

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
        ButtonWidget
            .builder(Text.literal(main.getRenderThroughBlocks() ? "Mode: Render all" : "Mode: Render visible"), b -> {
              main.setRenderThroughBlocks(!main.getRenderThroughBlocks());
              b.setMessage(main.getRenderThroughBlocks() ? Text.literal("Mode: Render all")
                  : Text.literal("Mode: Render visible"));
              client.setScreen(new MainConfigScreen(blueprints, main));
            })
            .dimensions(80, 10, 140, 20)
            .build());

    // addDrawableChild(
    // ButtonWidget.builder(Text.literal("Open Folder"), b -> {
    // Util.OpenFolder(Util.GetPerWorldDimensionConfigPath());
    // })
    // .dimensions(width - 200, 10, 80, 20)
    // .build());

    addDrawableChild(
        ButtonWidget.builder(Text.literal("Close"), b -> {
          client.setScreen(null);
        })
            .dimensions(width - 100, 10, 50, 20)
            .build());

    imageWidth = (width - 20) / imagesPerRow;
    imageHeight = imageWidth;
    rowsPerPage = (height - 50) / imageHeight;

    // add page buttons in the bottom right with the page number in the middle
    addDrawableChild(
        ButtonWidget.builder(Text.literal("<"), b -> {
          pageOffset = Math.max(0, pageOffset - 1);
        })
            .dimensions(width - 135, height - 25, 20, 20)
            .build());

    addDrawableChild(
        ButtonWidget.builder(Text.literal(">"), b -> {
          pageOffset = Math.min((blueprints.size() - 1) / imagesPerRow / rowsPerPage, pageOffset + 1);
        })
            .dimensions(width - 35, height - 25, 20, 20)
            .build());

    // add 'images per page' button that cycles between 5, 10, 15, 20
    addDrawableChild(
        ButtonWidget.builder(Text.literal("Images per page: " + imagesPerRow), b -> {
          imagesPerRow = imagesPerRow % 20 + 5;
          imageWidth = (width - 20) / imagesPerRow;
          imageHeight = imageWidth;
          rowsPerPage = (height - 50) / imageHeight;
          pageOffset = 0;
          b.setMessage(Text.literal("Images per page: " + imagesPerRow));

          // save config
          main.setImagesPerRow(imagesPerRow);
          main.SaveSettings();

        })
            .dimensions(width - 250, height - 25, 110, 20)
            .build());
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    super.render(context, mouseX, mouseY, delta);

    try {

      // grid of blueprints
      blueprints.forEach((blueprint) -> {
        int index = blueprints.indexOf(blueprint);
        if (index < pageOffset * imagesPerRow || index >= (pageOffset + rowsPerPage) * imagesPerRow) {
          return;
        }
        index -= pageOffset * imagesPerRow;
        int x = 10 + (index % imagesPerRow) * imageWidth;
        int y = 50 + (index / imagesPerRow) * imageHeight;

        // if the mouse is over, draw a background rectangle
        if (mouseX >= x && mouseX <= x + imageWidth && mouseY >= y && mouseY <= y + imageHeight) {
          context.fill(x, y, x + imageWidth, y + imageHeight, 0x80ffffff);
          x += 2;
          y += 2;
        }

        blueprint.renderThumbnail(context, x, y, imageWidth - 4, imageHeight - 4, true);

        if (!blueprint.isVisible()) {
          context.fill(x, y, x + imageWidth - 4, y + imageHeight - 4, 0x80000000);
        }
      });

      context.drawTextWithShadow(textRenderer, "Path: " + Util.GetPerWorldDimensionConfigPath(), 10, 35, 0xffffff);
      context.drawTexture(Identifier.of(Main.MOD_ID, "icon.png"), width - 45, 10, 0, 0, 30, 30, 30, 30);

      // render page number between buttons at the bottom
      if (rowsPerPage > 0 && imagesPerRow > 0) {
        context.drawTextWithShadow(textRenderer,
            "Page " + (pageOffset + 1) + " of " + ((blueprints.size() - 1) / imagesPerRow / rowsPerPage + 1),
            width - 110,
            height - 20, 0xffffff);
      }
    } catch (Exception e) {
      e.printStackTrace();
      Main.LOGGER.error(e.getMessage());
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button == 0) {
      blueprints.forEach((blueprint) -> {
        int index = blueprints.indexOf(blueprint);
        if (index < pageOffset * imagesPerRow || index >= (pageOffset + rowsPerPage) * imagesPerRow) {
          return;
        }
        index -= pageOffset * imagesPerRow;
        int x = 10 + (index % imagesPerRow) * imageWidth;
        int y = 50 + (index / imagesPerRow) * imageHeight;
        if (mouseX >= x && mouseX <= x + imageWidth && mouseY >= y && mouseY <= y + imageHeight) {

          // if ctrl is clicked, the toggle visibility
          if (ctrlPressed) {
            blueprint.setVisible(!blueprint.isVisible());
            return;
          }

          client.setScreen(new BlueprintConfigScreen(blueprint, this));
        }
      });
    }
    return super.mouseClicked(mouseX, mouseY, button);
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
}