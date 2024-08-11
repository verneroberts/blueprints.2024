package vernando.blueprints;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ElementListWidget;

public class BlueprintListWidget extends ElementListWidget<BlueprintListEntry> {
    public BlueprintListWidget(MinecraftClient client, int width, int height, int top, int itemHeight) {
        super(client, width, height, top, itemHeight);        
    }

    @Override
    protected int addEntry(BlueprintListEntry entry) {
        return super.addEntry(entry);
    }
}
