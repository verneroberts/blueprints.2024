package vernando.blueprints;

import vernando.blueprints.Util.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

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

    public void setSelectedBlueprint(Blueprint blueprint) {
        selectedBlueprint = blueprint;
    }

    public void push() {
        pushPull(0.1f);
    }

    public void pull() {
        pushPull(-0.1f);
    }

    private void pushPull(float amount) {
        if (!isEnabled || selectedBlueprint == null) {
            return;
        }

        Direction facingDirection = Util.playerFacingDirection(true);
        if (facingDirection == Direction.UP)
            facingDirection = Direction.DOWN;
        else if (facingDirection == Direction.DOWN)
            facingDirection = Direction.UP;

        selectedBlueprint.nudgePosition(facingDirection, amount, Util.isShiftDown(), Util.isCtrlDown());
    }

    public void enable() {
        if (!isEnabled) {
            isEnabled = true;
            if (selectedBlueprint != null) {
                Minecraft.getInstance().gui.setOverlayMessage(Component.literal(selectedBlueprint.getName()), false);
            }
        }
    }

    public void disable() {
        isEnabled = false;
    }
}
