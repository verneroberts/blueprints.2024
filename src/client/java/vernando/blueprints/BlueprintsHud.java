package vernando.blueprints;

import vernando.blueprints.Util.Direction;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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
    
    public void render(PoseStack matrices, Camera camera) {
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

            Minecraft client = Minecraft.getInstance();
            boolean shift = GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                          GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            boolean ctrl = GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                         GLFW.glfwGetKey(client.getWindow().handle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
            selectedBlueprint.NudgePosition(facingDirection, i, shift, ctrl);
        }
    }

    public void enable() {
        if (!isEnabled) {
            isEnabled = true;
            if (selectedBlueprint != null) {
                Minecraft client = Minecraft.getInstance();
                client.gui.setOverlayMessage(Component.literal(selectedBlueprint.getName()), false);            
            }
        }
    }

    public void disable() {
        isEnabled = false;
    }
}
