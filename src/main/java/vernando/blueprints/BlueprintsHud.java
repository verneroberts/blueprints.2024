package vernando.blueprints;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import vernando.blueprints.Util.Direction;

public class BlueprintsHud {
    private Blueprint selectedBlueprint;

    private boolean isEnabled;

    private static BlueprintsHud instance;
    public static BlueprintsHud getInstance() {
        if (instance == null) {
            instance = new BlueprintsHud();
        }
        return instance;
    }
    
    public void render(WorldRenderContext context) {     
    }

    public void setSelectedBlueprint(Blueprint blueprint) {
        MinecraftClient client = MinecraftClient.getInstance();
        selectedBlueprint = blueprint;
        client.inGameHud.setOverlayMessage(Text.literal(selectedBlueprint.getName()), false);            
    }

    public void push() {
        pushPull(-1);
    }

    public void pull() {
        pushPull(1);
    }

    private void pushPull(int i) {
        if (!isEnabled) {
            return;
        }
        
        if (selectedBlueprint != null) {
            Direction facingDirection = Util.PlayerFacingDirection(true);
            if (facingDirection == Direction.UP)
                facingDirection = Direction.DOWN;
            else if (facingDirection == Direction.DOWN)
                facingDirection = Direction.UP;

            selectedBlueprint.NudgePosition(facingDirection, i, false, false);
        }
    }

    public void enable() {
        isEnabled = true;
    }

    public void disable() {
        isEnabled = false;
    }
}
