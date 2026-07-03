package vernando.blueprints;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class BlueprintsConfigScreen extends Screen {
  private List<Blueprint> blueprints;
  private List<Blueprint> filteredBlueprints;
  private final List<Button> tileButtons = new ArrayList<>();
  private String searchQuery = "";
  private EditBox searchBox;
  private int imagesPerRow = 5;
  private int imageWidth = 100;
  private int imageHeight = 100;
  private int pageOffset = 0;
  private int rowsPerPage = 3;

  protected BlueprintsConfigScreen(List<Blueprint> blueprints) {
    super(Component.literal(Main.MOD_NAME + " Config"));
    this.blueprints = blueprints;
  }

  @Override
  protected void init() {
    imagesPerRow = Settings.getImagesPerRow();
    if (imagesPerRow < 5 || imagesPerRow > 20) {
      imagesPerRow = 5;
    }

    // main config section
    addRenderableWidget(
        Button.builder(Component.literal("Reload"), b -> {
          blueprints = BlueprintsManager.getInstance().scanFileSystemForImages();
          pageOffset = 0;
          rebuildWidgets();
        })
            .bounds(10, 10, 60, 20)
            .build());

    // renderThroughBlocks
    addRenderableWidget(
        Button
            .builder(Component.literal(Settings.getRenderThroughBlocks() ? "Mode: Render all" : "Mode: Render visible"),
                b -> {
                  Settings.setRenderThroughBlocks(!Settings.getRenderThroughBlocks());
                  b.setMessage(Settings.getRenderThroughBlocks() ? Component.literal("Mode: Render all")
                      : Component.literal("Mode: Render visible"));
                })
            .bounds(80, 10, 140, 20)
            .build());

    addRenderableWidget(
        Button.builder(Component.literal("Open Folder"), b -> {
          Util.openFolder(Util.getPerWorldDimensionConfigPath());
        })
            .bounds(width - 200, 10, 80, 20)
            .build());

    addRenderableWidget(
        Button.builder(Component.literal("Close"), b -> {
          minecraft.gui.setScreen(null);
        })
            .bounds(width - 100, 10, 50, 20)
            .build());

    imageWidth = (width - 20) / imagesPerRow;
    imageHeight = imageWidth;
    rowsPerPage = (height - 50) / imageHeight;

    // search box in the bottom left, filters the grid as you type
    int searchWidth = Math.max(60, Math.min(140, width - 265));
    searchBox = new EditBox(font, 10, height - 25, searchWidth, 20, Component.literal("Search"));
    searchBox.setHint(Component.literal("Search..."));
    searchBox.setValue(searchQuery);
    searchBox.setResponder(query -> {
      if (!query.equals(searchQuery)) {
        searchQuery = query;
        pageOffset = 0;
        refreshBlueprintTiles();
      }
    });
    addRenderableWidget(searchBox);

    // add page buttons in the bottom right with the page number in the middle
    addRenderableWidget(
        Button.builder(Component.literal("<"), b -> {
          pageOffset = Math.max(0, pageOffset - 1);
          refreshBlueprintTiles();
        })
            .bounds(width - 135, height - 25, 20, 20)
            .build());

    addRenderableWidget(
        Button.builder(Component.literal(">"), b -> {
          pageOffset = Math.min(maxPageOffset(), pageOffset + 1);
          refreshBlueprintTiles();
        })
            .bounds(width - 35, height - 25, 20, 20)
            .build());

    // add 'images per row' button that cycles between 5, 10, 15, 20
    addRenderableWidget(
        Button.builder(Component.literal("Images per row: " + imagesPerRow), b -> {
          Settings.setImagesPerRow(imagesPerRow % 20 + 5);
          pageOffset = 0;
          rebuildWidgets();
        })
            .bounds(width - 250, height - 25, 110, 20)
            .build());

    // Add invisible buttons for each blueprint tile
    refreshBlueprintTiles();

  }

  /** Recompute the filtered list and rebuild only the blueprint tile buttons. */
  private void refreshBlueprintTiles() {
    for (Button button : tileButtons) {
      removeWidget(button);
    }
    tileButtons.clear();

    filteredBlueprints = new ArrayList<>();
    String query = searchQuery.trim().toLowerCase();
    for (Blueprint blueprint : blueprints) {
      if (query.isEmpty() || blueprint.getName().toLowerCase().contains(query)) {
        filteredBlueprints.add(blueprint);
      }
    }

    pageOffset = Math.min(pageOffset, maxPageOffset());
    addBlueprintButtons();
  }

  private int maxPageOffset() {
    if (filteredBlueprints == null || rowsPerPage <= 0 || imagesPerRow <= 0) {
      return 0;
    }
    return Math.max(0, (filteredBlueprints.size() - 1) / imagesPerRow / rowsPerPage);
  }

  private void addBlueprintButtons() {
    for (int index = 0; index < filteredBlueprints.size(); index++) {
      Blueprint blueprint = filteredBlueprints.get(index);
      if (!isImageInView(index))
        continue;

      int[] pos = getImagePosition(index);

      Button blueprintButton = Button.builder(Component.empty(), button -> {
        BlueprintsHud.getInstance().setSelectedBlueprint(blueprint);

        if (Util.isCtrlDown()) {
          blueprint.setVisible(!blueprint.isVisible());
        } else {
          minecraft.gui.setScreen(new BlueprintConfigScreen(blueprint, this));
        }
      })
      .bounds(pos[0], pos[1], imageWidth - 3, imageHeight - 3)
      .build();

      tileButtons.add(blueprintButton);
      addRenderableWidget(blueprintButton);
    }
  }

  @Override
  public void onFilesDrop(List<Path> paths) {
    int imported = Util.importImageFiles(paths);
    if (imported > 0) {
      blueprints = BlueprintsManager.getInstance().scanFileSystemForImages();
      rebuildWidgets();
    }
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    super.extractRenderState(context, mouseX, mouseY, delta);

    try {

      // grid of blueprints
      for (int index = 0; index < filteredBlueprints.size(); index++) {
        Blueprint blueprint = filteredBlueprints.get(index);

        if (!isImageInView(index))
          continue;
        int[] pos = getImagePosition(index);
        int x = pos[0];
        int y = pos[1];

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
      }

      context.text(font, "Path: " + Util.getPerWorldDimensionConfigPath() + " (drop images here to import)", 10, 35, 0xffffff);
      context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(Main.MOD_ID, "icon.png"), width - 45, 10, 0.0f, 0.0f, 30, 30, 30, 30);

      // render page number between buttons at the bottom
      if (rowsPerPage > 0 && imagesPerRow > 0) {
        context.text(font,
            "Page " + (pageOffset + 1) + " of " + (maxPageOffset() + 1),
            width - 110,
            height - 20, 0xffffff);
      }
    } catch (Exception e) {
      Main.LOGGER.error("Failed to render blueprints config screen", e);
    }
  }

  private boolean isImageInView(int index) {
    index -= pageOffset * imagesPerRow * rowsPerPage;
    return index >= 0 && index < imagesPerRow * rowsPerPage;
  }

  private int[] getImagePosition(int index) {
    index -= pageOffset * imagesPerRow * rowsPerPage;
    int x = 10 + (index % imagesPerRow) * imageWidth;
    int y = 50 + (index / imagesPerRow) * imageHeight;
    return new int[] { x, y };
  }


}
