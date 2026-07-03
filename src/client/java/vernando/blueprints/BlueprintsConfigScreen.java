package vernando.blueprints;

import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class BlueprintsConfigScreen extends Screen {
  private ArrayList<Blueprint> blueprints;
  private ArrayList<Blueprint> filteredBlueprints;
  private ArrayList<Button> tileButtons = new ArrayList<>();
  private Main main;
  private String searchQuery = "";
  private EditBox searchBox;
  private int imagesPerRow = 5;
  private int imageWidth = 100;
  private int imageHeight = 100;
  private int pageOffset = 0;
  private int rowsPerPage = 3;

  protected BlueprintsConfigScreen(Main main, ArrayList<Blueprint> blueprints) {
    super(Component.literal(Main.MOD_NAME + " Config"));
    this.blueprints = blueprints;
    this.main = main;
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
          b.setMessage(Component.literal("..."));
          blueprints = BlueprintsManager.getInstance().ScanFileSystemForImages();
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
    Util.OpenFolder(Util.GetPerWorldDimensionConfigPath());
    })
    .bounds(width - 200, 10, 80, 20)
    .build());

    addRenderableWidget(
        Button.builder(Component.literal("Close"), b -> {
          minecraft.setScreen(null);
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

    // add 'images per page' button that cycles between 5, 10, 15, 20
    addRenderableWidget(
        Button.builder(Component.literal("Images per row: " + imagesPerRow), b -> {
          imagesPerRow = imagesPerRow % 20 + 5;
          imageWidth = (width - 20) / imagesPerRow;
          imageHeight = imageWidth;
          rowsPerPage = (height - 50) / imageHeight;
          pageOffset = 0;
          b.setMessage(Component.literal("Images per row: " + imagesPerRow));

          // save config
          Settings.setImagesPerRow(imagesPerRow);

          refreshBlueprintTiles();
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
    for (Blueprint blueprint : filteredBlueprints) {
      int index = filteredBlueprints.indexOf(blueprint);
      if (!isImageInView(index))
        continue;

      int[] pos = getImagePostion(index);
      int x = pos[0];
      int y = pos[1];

      int buttonWidth = imageWidth - 3;
      int buttonHeight = imageHeight - 3;
      int buttonX = x;
      int buttonY = y;
      Button blueprintButton = Button.builder(Component.empty(), button -> {
        BlueprintsHud.getInstance().setSelectedBlueprint(blueprint);

        Minecraft client = Minecraft.getInstance();
        boolean ctrl = GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                      GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;

        if (ctrl) {
          blueprint.setVisible(!blueprint.isVisible());
        } else {
          client.setScreen(new BlueprintConfigScreen(blueprint, this));
        }
      })
      .bounds(buttonX, buttonY, buttonWidth, buttonHeight)
      .build();

      tileButtons.add(blueprintButton);
      addRenderableWidget(blueprintButton);
    }
  }

  @Override
  public void onFilesDrop(List<Path> paths) {
    int imported = Util.ImportImageFiles(paths);
    if (imported > 0) {
      blueprints = BlueprintsManager.getInstance().ScanFileSystemForImages();
      rebuildWidgets();
    }
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    super.extractRenderState(context, mouseX, mouseY, delta);

    try {

      // grid of blueprints
      filteredBlueprints.forEach((blueprint) -> {
        int index = filteredBlueprints.indexOf(blueprint);

        if (!isImageInView(index))
          return;
        int[] pos = getImagePostion(index);
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
      });

      context.text(font, "Path: " + Util.GetPerWorldDimensionConfigPath() + " (drop images here to import)", 10, 35, 0xffffff);
      context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(Main.MOD_ID, "icon.png"), width - 45, 10, 0.0f, 0.0f, 30, 30, 30, 30);

      // render page number between buttons at the bottom
      if (rowsPerPage > 0 && imagesPerRow > 0) {
        context.text(font,
            "Page " + (pageOffset + 1) + " of " + (maxPageOffset() + 1),
            width - 110,
            height - 20, 0xffffff);
      }
    } catch (Exception e) {
      e.printStackTrace();
      Main.LOGGER.error(e.getMessage());
    }
  }

  private boolean isImageInView(int index) {
    index -= pageOffset * imagesPerRow * rowsPerPage;
    if (index < 0 || index >= imagesPerRow * rowsPerPage)
      return false;

    return true;
  }

  private int[] getImagePostion(int index) {
    index -= pageOffset * imagesPerRow * rowsPerPage;
    int x = 10 + (index % imagesPerRow) * imageWidth;
    int y = 50 + (index / imagesPerRow) * imageHeight;
    return new int[] { x, y };
  }


}
