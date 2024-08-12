package vernando.blueprints;

import java.util.List;

import com.mojang.authlib.minecraft.client.MinecraftClient;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class BlueprintListEntry extends ElementListWidget.Entry<BlueprintListEntry> {
    private final Blueprint blueprint;
    private final TextRenderer textRenderer;
    private List<Element> elements;

    public BlueprintListEntry(Blueprint blueprint, TextRenderer textRenderer, int width) {
        this.blueprint = blueprint;
        this.textRenderer = textRenderer;
        this.elements = List.of(
            ButtonWidget.builder(Text.literal("Configure"), b -> {
                
            })
            .dimensions(width - 75, 50 , 60, 20)
            .build());        
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float tickDelta) {

        List<OrderedText> lines = textRenderer.wrapLines(StringVisitable.plain(blueprint.getName()),
                entryWidth - 80 - 40);
        context.drawTextWithShadow(textRenderer, lines.get(0), 40, y + 5, 0xffffff);
        blueprint.renderThumbnail(context, 10, y - 3, 28, 22);

        if (hovered) {
            context.fillGradient(0, y - 3, x + entryWidth, y + entryHeight, -2130706433, -2130706433);
        }
    }

    @Override
    public List<? extends Element> children() {
        return elements;
    }

    @Override
    public List<? extends Selectable> selectableChildren() {
        return List.of();
    }

}
