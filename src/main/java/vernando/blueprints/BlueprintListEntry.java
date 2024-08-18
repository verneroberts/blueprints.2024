package vernando.blueprints;

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;

public class BlueprintListEntry extends ElementListWidget.Entry<BlueprintListEntry> {
    private final Blueprint blueprint;
    private final TextRenderer textRenderer;
    private final MinecraftClient client;
    private final Screen parent;

    public BlueprintListEntry(Blueprint blueprint, MinecraftClient client, Screen parent) {
        this.blueprint = blueprint;        
        this.textRenderer = client.textRenderer;
        this.client = client;
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float tickDelta) {

        List<OrderedText> lines = textRenderer.wrapLines(StringVisitable.plain(blueprint.getName()),
                entryWidth-40);
        context.drawTextWithShadow(textRenderer, lines.get(0), x + 40, y + 5, 0xffffff);
        blueprint.renderThumbnail(context, x+ 10, y - 3, 28, 22, true);

        if (hovered) {
            context.fillGradient(x, y - 3, x + entryWidth, y + entryHeight, -2130706433, -2130706433);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        client.setScreen(new BlueprintConfigScreen(blueprint, parent));
        return false;
    }

    @Override
    public List<? extends Element> children() {
        return List.of();
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of();
    }

}
