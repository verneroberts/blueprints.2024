package vernando.blueprints;

import java.util.List;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;

public class BlueprintListEntry extends ElementListWidget.Entry<BlueprintListEntry> {
    private final Blueprint blueprint;
    private final TextRenderer textRenderer;
    private List<Element> elements;

    public BlueprintListEntry(Blueprint blueprint, TextRenderer textRenderer) {
        this.blueprint = blueprint;
        this.textRenderer = textRenderer;
        this.elements = List.of();
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float tickDelta) {

        List<OrderedText> lines = textRenderer.wrapLines(StringVisitable.plain(blueprint.getName()),
                entryWidth - 80 - 40);
        context.drawTextWithShadow(textRenderer, lines.get(0), 40, y + 5, 0xffffff);
        blueprint.renderThumbnail(context, 10, y - 3, 28, 22);

        if (hovered) {
            context.fillGradient(x, y, x + entryWidth, y + entryHeight, -2130706433, -2130706433);
        }
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
