package vernando.blueprints;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
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
        selectedBlueprint = blueprint;
        // MinecraftClient client = MinecraftClient.getInstance();
        // client.inGameHud.setOverlayMessage(Text.literal(selectedBlueprint.getName()), false);            
    }

    public void push() {
        pushPull(0.1f);
    }

    public void pull() {
        pushPull(-0.1f);
    }

    private void pushPull(float i) {
        if (!isEnabled) {
            return;
        }
        
        if (selectedBlueprint != null) {
            Direction facingDirection = Util.PlayerFacingDirection(true);
            if (facingDirection == Direction.UP)
                facingDirection = Direction.DOWN;
            else if (facingDirection == Direction.DOWN)
                facingDirection = Direction.UP;

            selectedBlueprint.NudgePosition(facingDirection, i, Screen.hasShiftDown(), Screen.hasControlDown());
        }
    }

    public void enable() {
        if (!isEnabled) {
            isEnabled = true;
            if (selectedBlueprint != null) {
                MinecraftClient client = MinecraftClient.getInstance();
                client.inGameHud.setOverlayMessage(Text.literal(selectedBlueprint.getName()), false);            
            }
        }
    }

    public void disable() {
        isEnabled = false;
    }
}
