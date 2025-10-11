package vernando.blueprints;

/**
 * Mathematical utility functions for blueprint transformations.
 * All methods are pure functions with no external dependencies, making them highly testable.
 */
public class MathUtils {

    /**
     * Normalizes an angle to the range [0, 360).
     *
     * @param angle The angle in degrees
     * @return The normalized angle in the range [0, 360)
     */
    public static float normalizeAngle(float angle) {
        // Use modulo to handle large values efficiently
        angle = angle % 360;

        // Handle negative values
        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    /**
     * Normalizes yaw to the range [-180, 180].
     * This is useful for camera yaw values that can grow infinitely when spinning in one direction.
     *
     * @param yaw The yaw angle in degrees
     * @return The normalized yaw in the range [-180, 180]
     */
    public static float normalizeYaw(float yaw) {
        while (yaw < -180) {
            yaw += 360;
        }
        while (yaw > 180) {
            yaw -= 360;
        }
        return yaw;
    }

    /**
     * Calculates the nudge amount based on modifier keys.
     *
     * @param baseAmount The base amount to nudge
     * @param multiply True if shift key is pressed (multiplies by 10 or 90/100 depending on context)
     * @param finetune True if ctrl key is pressed (divides by 10 or multiplies by 180 when both pressed)
     * @param context The context for the calculation (ROTATION, POSITION, SCALE, ALPHA)
     * @return The calculated nudge amount
     */
    public static float calculateNudgeAmount(float baseAmount, boolean multiply, boolean finetune, NudgeContext context) {
        if (finetune && multiply) {
            // Both keys pressed
            switch (context) {
                case ROTATION:
                    return baseAmount * 180;
                case POSITION:
                case SCALE:
                case ALPHA:
                    return baseAmount * 100f;
                default:
                    return baseAmount;
            }
        } else if (finetune) {
            // Only ctrl pressed - fine adjustment
            return baseAmount / 10f;
        } else if (multiply) {
            // Only shift pressed - large adjustment
            switch (context) {
                case ROTATION:
                    return baseAmount * 90;
                case POSITION:
                case SCALE:
                case ALPHA:
                    return baseAmount * 10;
                default:
                    return baseAmount;
            }
        }

        return baseAmount;
    }

    /**
     * Context for nudge amount calculations.
     */
    public enum NudgeContext {
        ROTATION,
        POSITION,
        SCALE,
        ALPHA
    }

    /**
     * Clamps a value to the range [0, 1].
     *
     * @param value The value to clamp
     * @return The clamped value in the range [0, 1]
     */
    public static float clampAlpha(float value) {
        if (value > 1) {
            return 1;
        }
        if (value < 0) {
            return 0;
        }
        return value;
    }

    /**
     * Clamps a value between a minimum and maximum.
     *
     * @param value The value to clamp
     * @param min The minimum value (can be null for no minimum)
     * @param max The maximum value (can be null for no maximum)
     * @return The clamped value
     */
    public static float clamp(float value, Float min, Float max) {
        if (max != null && value > max) {
            return max;
        }
        if (min != null && value < min) {
            return min;
        }
        return value;
    }

    /**
     * Rounds a value to a specified number of decimal places.
     *
     * @param value The value to round
     * @param precision The number of decimal places
     * @return The rounded value
     */
    public static float roundToPrecision(float value, int precision) {
        if (precision < 0) {
            precision = 0;
        }

        float multiplier = (float) Math.pow(10, precision);
        return Math.round(value * multiplier) / multiplier;
    }

    /**
     * Calculates the increment/decrement amount for number field widgets.
     *
     * @param standardDelta The standard delta value
     * @param shift True if shift key is pressed (10x multiplier)
     * @param ctrl True if ctrl key is pressed (0.1x multiplier)
     * @return The calculated delta
     */
    public static float calculateFieldDelta(float standardDelta, boolean shift, boolean ctrl) {
        float delta = standardDelta;

        if (shift && ctrl) {
            // Both pressed: 100x
            delta *= 100;
        } else {
            if (shift) {
                // Shift only: 10x
                delta *= 10;
            }
            if (ctrl) {
                // Ctrl only: 0.1x
                delta *= 0.1;
            }
        }

        return delta;
    }

    /**
     * Extracts the filename from a full path.
     *
     * @param path The full path
     * @return The filename (last component of the path)
     */
    public static String getFilenameFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        String[] parts = path.split("[/\\\\]");
        return parts.length > 0 ? parts[parts.length - 1] : "";
    }
}
