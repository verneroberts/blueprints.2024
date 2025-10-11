package vernando.blueprints;

import net.minecraft.util.math.Direction.Axis;

/**
 * Pure transformation utilities for blueprint operations.
 * All methods are pure functions that take inputs and return outputs without side effects,
 * making them highly testable and composable.
 */
public class TransformUtils {

    /**
     * Calculates a new rotation value after applying a nudge.
     *
     * @param currentRotation The current rotation value in degrees
     * @param amount The base amount to nudge by
     * @param multiply True if shift key is pressed (90x multiplier)
     * @param finetune True if ctrl key is pressed (0.1x multiplier, or 180x when combined with shift)
     * @return The new rotation value, normalized to [0, 360)
     */
    public static float nudgeRotation(float currentRotation, float amount, boolean multiply, boolean finetune) {
        float adjustedAmount = MathUtils.calculateNudgeAmount(amount, multiply, finetune, MathUtils.NudgeContext.ROTATION);
        float newRotation = currentRotation + adjustedAmount;
        return MathUtils.normalizeAngle(newRotation);
    }

    /**
     * Calculates a new position value after applying a nudge.
     *
     * @param currentPosition The current position value
     * @param direction The direction to move (affects sign of movement)
     * @param amount The base amount to nudge by
     * @param multiply True if shift key is pressed (10x multiplier)
     * @param finetune True if ctrl key is pressed (0.1x multiplier, or 100x when combined with shift)
     * @return The new position value
     */
    public static float nudgePosition(float currentPosition, Util.Direction direction, float amount, boolean multiply, boolean finetune) {
        float adjustedAmount = MathUtils.calculateNudgeAmount(amount, multiply, finetune, MathUtils.NudgeContext.POSITION);

        // Apply direction-specific sign
        switch (direction) {
            case UP:
            case EAST:
            case SOUTH:
                return currentPosition + adjustedAmount;
            case DOWN:
            case WEST:
            case NORTH:
                return currentPosition - adjustedAmount;
            default:
                return currentPosition;
        }
    }

    /**
     * Represents a 3D position with x, y, z coordinates.
     */
    public static class Position {
        public final float x;
        public final float y;
        public final float z;

        public Position(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /**
     * Calculates a new 3D position after applying a directional nudge.
     *
     * @param current The current position
     * @param direction The direction to move
     * @param amount The base amount to nudge by
     * @param multiply True if shift key is pressed (10x multiplier)
     * @param finetune True if ctrl key is pressed (0.1x multiplier, or 100x when combined with shift)
     * @return The new position
     */
    public static Position nudgePosition3D(Position current, Util.Direction direction, float amount, boolean multiply, boolean finetune) {
        float adjustedAmount = MathUtils.calculateNudgeAmount(amount, multiply, finetune, MathUtils.NudgeContext.POSITION);

        float newX = current.x;
        float newY = current.y;
        float newZ = current.z;

        switch (direction) {
            case UP:
                newY += adjustedAmount;
                break;
            case DOWN:
                newY -= adjustedAmount;
                break;
            case EAST:
                newX += adjustedAmount;
                break;
            case WEST:
                newX -= adjustedAmount;
                break;
            case NORTH:
                newZ -= adjustedAmount;
                break;
            case SOUTH:
                newZ += adjustedAmount;
                break;
        }

        return new Position(newX, newY, newZ);
    }

    /**
     * Calculates a new alpha value after applying a nudge, clamped to [0, 1].
     *
     * @param currentAlpha The current alpha value
     * @param amount The base amount to nudge by (can be negative to decrease)
     * @param multiply True if shift key is pressed (10x multiplier)
     * @param finetune True if ctrl key is pressed (0.1x multiplier, or 100x when combined with shift)
     * @return The new alpha value, clamped to [0, 1]
     */
    public static float nudgeAlpha(float currentAlpha, float amount, boolean multiply, boolean finetune) {
        float adjustedAmount = MathUtils.calculateNudgeAmount(amount, multiply, finetune, MathUtils.NudgeContext.ALPHA);
        float newAlpha = currentAlpha + adjustedAmount;
        return MathUtils.clampAlpha(newAlpha);
    }

    /**
     * Calculates a new scale value after applying a nudge.
     * Note: Scale values can be negative (for mirroring) and are not clamped.
     *
     * @param currentScale The current scale value
     * @param amount The base amount to nudge by (can be negative to decrease)
     * @param multiply True if shift key is pressed (10x multiplier)
     * @param finetune True if ctrl key is pressed (0.1x multiplier, or 100x when combined with shift)
     * @return The new scale value
     */
    public static float nudgeScale(float currentScale, float amount, boolean multiply, boolean finetune) {
        float adjustedAmount = MathUtils.calculateNudgeAmount(amount, multiply, finetune, MathUtils.NudgeContext.SCALE);
        return currentScale + adjustedAmount;
    }

    /**
     * Represents 3D rotation with x, y, z angles in degrees.
     */
    public static class Rotation {
        public final float x;
        public final float y;
        public final float z;

        public Rotation(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /**
     * Calculates a new 3D rotation after applying a nudge to a specific axis.
     *
     * @param current The current rotation
     * @param axis The axis to rotate around (X, Y, or Z)
     * @param amount The base amount to nudge by
     * @param multiply True if shift key is pressed (90x multiplier)
     * @param finetune True if ctrl key is pressed (0.1x multiplier, or 180x when combined with shift)
     * @return The new rotation with the specified axis updated and normalized
     */
    public static Rotation nudgeRotation3D(Rotation current, Axis axis, float amount, boolean multiply, boolean finetune) {
        float newX = current.x;
        float newY = current.y;
        float newZ = current.z;

        switch (axis) {
            case X:
                newX = nudgeRotation(current.x, amount, multiply, finetune);
                break;
            case Y:
                newY = nudgeRotation(current.y, amount, multiply, finetune);
                break;
            case Z:
                newZ = nudgeRotation(current.z, amount, multiply, finetune);
                break;
        }

        return new Rotation(newX, newY, newZ);
    }

    /**
     * Represents 2D scale with x and y values.
     */
    public static class Scale {
        public final float x;
        public final float y;

        public Scale(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Calculates a new 2D scale after applying a nudge to a specific axis.
     *
     * @param current The current scale
     * @param axis The axis to scale (X or Y, Z is ignored)
     * @param amount The base amount to nudge by
     * @param multiply True if shift key is pressed (10x multiplier)
     * @param finetune True if ctrl key is pressed (0.1x multiplier, or 100x when combined with shift)
     * @return The new scale with the specified axis updated
     */
    public static Scale nudgeScale2D(Scale current, Axis axis, float amount, boolean multiply, boolean finetune) {
        float newX = current.x;
        float newY = current.y;

        switch (axis) {
            case X:
                newX = nudgeScale(current.x, amount, multiply, finetune);
                break;
            case Y:
                newY = nudgeScale(current.y, amount, multiply, finetune);
                break;
            case Z:
                // Z axis not implemented for 2D scale
                break;
        }

        return new Scale(newX, newY);
    }
}
