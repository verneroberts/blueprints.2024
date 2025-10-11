package vernando.blueprints;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import vernando.blueprints.Util.Direction;
import org.lwjgl.glfw.GLFW;

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
    
    public void render(MatrixStack matrices, Camera camera) {
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

            MinecraftClient client = MinecraftClient.getInstance();
            boolean shift = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                          GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            boolean ctrl = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                         GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
            selectedBlueprint.NudgePosition(facingDirection, i, shift, ctrl);
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
