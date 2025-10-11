package vernando.blueprints;

import net.minecraft.util.math.Direction.Axis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Comprehensive tests for TransformUtils transformation functions.
 * Tests complete transformation behaviors at the appropriate abstraction level.
 */
@DisplayName("TransformUtils Tests")
class TransformUtilsTest {

    // ========== nudgeRotation Tests ==========

    @Test
    @DisplayName("nudgeRotation should apply base amount with no modifiers")
    void testNudgeRotation_NoModifiers() {
        float result = TransformUtils.nudgeRotation(45f, 15f, false, false);
        assertThat(result).isEqualTo(60f);
    }

    @Test
    @DisplayName("nudgeRotation should apply 90x multiplier with shift")
    void testNudgeRotation_ShiftModifier() {
        float result = TransformUtils.nudgeRotation(0f, 1f, true, false);
        assertThat(result).isEqualTo(90f);
    }

    @Test
    @DisplayName("nudgeRotation should apply 0.1x multiplier with ctrl")
    void testNudgeRotation_CtrlModifier() {
        float result = TransformUtils.nudgeRotation(0f, 10f, false, true);
        assertThat(result).isEqualTo(1f);
    }

    @Test
    @DisplayName("nudgeRotation should apply 180x multiplier with both modifiers")
    void testNudgeRotation_BothModifiers() {
        float result = TransformUtils.nudgeRotation(0f, 1f, true, true);
        assertThat(result).isEqualTo(180f);
    }

    @Test
    @DisplayName("nudgeRotation should wrap angles >= 360 to [0, 360)")
    void testNudgeRotation_WrapOver360() {
        float result = TransformUtils.nudgeRotation(350f, 20f, false, false);
        assertThat(result).isEqualTo(10f); // 370 wraps to 10
    }

    @Test
    @DisplayName("nudgeRotation should wrap negative angles to [0, 360)")
    void testNudgeRotation_WrapNegative() {
        float result = TransformUtils.nudgeRotation(10f, -20f, false, false);
        assertThat(result).isEqualTo(350f); // -10 wraps to 350
    }

    @Test
    @DisplayName("nudgeRotation should handle 90-degree snap with shift")
    void testNudgeRotation_90DegreeSnap() {
        float result = TransformUtils.nudgeRotation(45f, 1f, true, false);
        assertThat(result).isEqualTo(135f); // 45 + 90
    }

    @Test
    @DisplayName("nudgeRotation should handle 180-degree snap with both modifiers")
    void testNudgeRotation_180DegreeSnap() {
        float result = TransformUtils.nudgeRotation(45f, 1f, true, true);
        assertThat(result).isEqualTo(225f); // 45 + 180
    }

    // ========== nudgeRotation3D Tests ==========

    @ParameterizedTest
    @EnumSource(Axis.class)
    @DisplayName("nudgeRotation3D should update only the specified axis")
    void testNudgeRotation3D_IndependentAxes(Axis axis) {
        TransformUtils.Rotation current = new TransformUtils.Rotation(10f, 20f, 30f);
        TransformUtils.Rotation result = TransformUtils.nudgeRotation3D(current, axis, 15f, false, false);

        switch (axis) {
            case X:
                assertThat(result.x).isEqualTo(25f);
                assertThat(result.y).isEqualTo(20f);
                assertThat(result.z).isEqualTo(30f);
                break;
            case Y:
                assertThat(result.x).isEqualTo(10f);
                assertThat(result.y).isEqualTo(35f);
                assertThat(result.z).isEqualTo(30f);
                break;
            case Z:
                assertThat(result.x).isEqualTo(10f);
                assertThat(result.y).isEqualTo(20f);
                assertThat(result.z).isEqualTo(45f);
                break;
        }
    }

    @Test
    @DisplayName("nudgeRotation3D should apply modifiers correctly")
    void testNudgeRotation3D_WithModifiers() {
        TransformUtils.Rotation current = new TransformUtils.Rotation(0f, 0f, 0f);

        // Test shift modifier (90x)
        TransformUtils.Rotation result1 = TransformUtils.nudgeRotation3D(current, Axis.X, 1f, true, false);
        assertThat(result1.x).isEqualTo(90f);

        // Test both modifiers (180x)
        TransformUtils.Rotation result2 = TransformUtils.nudgeRotation3D(current, Axis.Y, 1f, true, true);
        assertThat(result2.y).isEqualTo(180f);
    }

    // ========== nudgePosition Tests ==========

    @ParameterizedTest
    @CsvSource({
        "UP, 100.0, 110.0",
        "DOWN, 100.0, 90.0"
    })
    @DisplayName("nudgePosition should move vertically in UP/DOWN directions")
    void testNudgePosition_Vertical(String directionName, float startY, float expectedY) {
        Util.Direction direction = Util.Direction.valueOf(directionName);
        float result = TransformUtils.nudgePosition(startY, direction, 10f, false, false);
        assertThat(result).isEqualTo(expectedY);
    }

    @ParameterizedTest
    @CsvSource({
        "EAST, 50.0, 60.0",
        "WEST, 50.0, 40.0"
    })
    @DisplayName("nudgePosition should move horizontally in EAST/WEST directions")
    void testNudgePosition_Horizontal_X(String directionName, float startX, float expectedX) {
        Util.Direction direction = Util.Direction.valueOf(directionName);
        float result = TransformUtils.nudgePosition(startX, direction, 10f, false, false);
        assertThat(result).isEqualTo(expectedX);
    }

    @ParameterizedTest
    @CsvSource({
        "NORTH, 50.0, 40.0",
        "SOUTH, 50.0, 60.0"
    })
    @DisplayName("nudgePosition should move horizontally in NORTH/SOUTH directions")
    void testNudgePosition_Horizontal_Z(String directionName, float startZ, float expectedZ) {
        Util.Direction direction = Util.Direction.valueOf(directionName);
        float result = TransformUtils.nudgePosition(startZ, direction, 10f, false, false);
        assertThat(result).isEqualTo(expectedZ);
    }

    @Test
    @DisplayName("nudgePosition should apply 10x multiplier with shift")
    void testNudgePosition_ShiftModifier() {
        float result = TransformUtils.nudgePosition(0f, Util.Direction.UP, 1f, true, false);
        assertThat(result).isEqualTo(10f);
    }

    @Test
    @DisplayName("nudgePosition should apply 0.1x multiplier with ctrl")
    void testNudgePosition_CtrlModifier() {
        float result = TransformUtils.nudgePosition(0f, Util.Direction.EAST, 10f, false, true);
        assertThat(result).isCloseTo(1f, within(0.01f));
    }

    @Test
    @DisplayName("nudgePosition should apply 100x multiplier with both modifiers")
    void testNudgePosition_BothModifiers() {
        float result = TransformUtils.nudgePosition(0f, Util.Direction.SOUTH, 1f, true, true);
        assertThat(result).isEqualTo(100f);
    }

    // ========== nudgePosition3D Tests ==========

    @Test
    @DisplayName("nudgePosition3D should update correct coordinate for each direction")
    void testNudgePosition3D_AllDirections() {
        TransformUtils.Position start = new TransformUtils.Position(10f, 20f, 30f);

        // UP: increases Y
        TransformUtils.Position up = TransformUtils.nudgePosition3D(start, Util.Direction.UP, 5f, false, false);
        assertThat(up).matches(p -> p.x == 10f && p.y == 25f && p.z == 30f);

        // DOWN: decreases Y
        TransformUtils.Position down = TransformUtils.nudgePosition3D(start, Util.Direction.DOWN, 5f, false, false);
        assertThat(down).matches(p -> p.x == 10f && p.y == 15f && p.z == 30f);

        // EAST: increases X
        TransformUtils.Position east = TransformUtils.nudgePosition3D(start, Util.Direction.EAST, 5f, false, false);
        assertThat(east).matches(p -> p.x == 15f && p.y == 20f && p.z == 30f);

        // WEST: decreases X
        TransformUtils.Position west = TransformUtils.nudgePosition3D(start, Util.Direction.WEST, 5f, false, false);
        assertThat(west).matches(p -> p.x == 5f && p.y == 20f && p.z == 30f);

        // SOUTH: increases Z
        TransformUtils.Position south = TransformUtils.nudgePosition3D(start, Util.Direction.SOUTH, 5f, false, false);
        assertThat(south).matches(p -> p.x == 10f && p.y == 20f && p.z == 35f);

        // NORTH: decreases Z
        TransformUtils.Position north = TransformUtils.nudgePosition3D(start, Util.Direction.NORTH, 5f, false, false);
        assertThat(north).matches(p -> p.x == 10f && p.y == 20f && p.z == 25f);
    }

    @Test
    @DisplayName("nudgePosition3D should apply modifiers correctly")
    void testNudgePosition3D_WithModifiers() {
        TransformUtils.Position start = new TransformUtils.Position(0f, 0f, 0f);

        // Shift: 10x
        TransformUtils.Position result1 = TransformUtils.nudgePosition3D(start, Util.Direction.UP, 1f, true, false);
        assertThat(result1.y).isEqualTo(10f);

        // Both: 100x
        TransformUtils.Position result2 = TransformUtils.nudgePosition3D(start, Util.Direction.EAST, 1f, true, true);
        assertThat(result2.x).isEqualTo(100f);
    }

    // ========== nudgeAlpha Tests ==========

    @Test
    @DisplayName("nudgeAlpha should increase alpha by base amount")
    void testNudgeAlpha_Increase() {
        float result = TransformUtils.nudgeAlpha(0.5f, 0.1f, false, false);
        assertThat(result).isCloseTo(0.6f, within(0.01f));
    }

    @Test
    @DisplayName("nudgeAlpha should decrease alpha with negative amount")
    void testNudgeAlpha_Decrease() {
        float result = TransformUtils.nudgeAlpha(0.5f, -0.1f, false, false);
        assertThat(result).isCloseTo(0.4f, within(0.01f));
    }

    @Test
    @DisplayName("nudgeAlpha should clamp alpha at 1.0")
    void testNudgeAlpha_ClampAtMax() {
        float result = TransformUtils.nudgeAlpha(0.9f, 0.5f, false, false);
        assertThat(result).isEqualTo(1.0f);
    }

    @Test
    @DisplayName("nudgeAlpha should clamp alpha at 0.0")
    void testNudgeAlpha_ClampAtMin() {
        float result = TransformUtils.nudgeAlpha(0.1f, -0.5f, false, false);
        assertThat(result).isEqualTo(0.0f);
    }

    @Test
    @DisplayName("nudgeAlpha should apply modifiers correctly")
    void testNudgeAlpha_WithModifiers() {
        // Shift: 10x
        float result1 = TransformUtils.nudgeAlpha(0.5f, 0.01f, true, false);
        assertThat(result1).isCloseTo(0.6f, within(0.01f));

        // Ctrl: 0.1x
        float result2 = TransformUtils.nudgeAlpha(0.5f, 1f, false, true);
        assertThat(result2).isCloseTo(0.6f, within(0.01f));

        // Both: 100x (but will clamp)
        float result3 = TransformUtils.nudgeAlpha(0.5f, 0.01f, true, true);
        assertThat(result3).isEqualTo(1.0f); // 0.5 + (0.01 * 100) = 1.5, clamped to 1.0
    }

    @Test
    @DisplayName("nudgeAlpha should handle boundary transitions")
    void testNudgeAlpha_BoundaryTransitions() {
        // Just below max
        assertThat(TransformUtils.nudgeAlpha(0.99f, 0.005f, false, false)).isCloseTo(0.995f, within(0.001f));

        // Exactly at max
        assertThat(TransformUtils.nudgeAlpha(1.0f, 0.1f, false, false)).isEqualTo(1.0f);

        // Just above min
        assertThat(TransformUtils.nudgeAlpha(0.01f, -0.005f, false, false)).isCloseTo(0.005f, within(0.001f));

        // Exactly at min
        assertThat(TransformUtils.nudgeAlpha(0.0f, -0.1f, false, false)).isEqualTo(0.0f);
    }

    // ========== nudgeScale Tests ==========

    @Test
    @DisplayName("nudgeScale should increase scale by base amount")
    void testNudgeScale_Increase() {
        float result = TransformUtils.nudgeScale(1.0f, 0.5f, false, false);
        assertThat(result).isEqualTo(1.5f);
    }

    @Test
    @DisplayName("nudgeScale should decrease scale with negative amount")
    void testNudgeScale_Decrease() {
        float result = TransformUtils.nudgeScale(2.0f, -0.5f, false, false);
        assertThat(result).isEqualTo(1.5f);
    }

    @Test
    @DisplayName("nudgeScale should allow negative scale values for mirroring")
    void testNudgeScale_Negative() {
        float result = TransformUtils.nudgeScale(0.5f, -1.0f, false, false);
        assertThat(result).isEqualTo(-0.5f);
    }

    @Test
    @DisplayName("nudgeScale should apply 10x multiplier with shift")
    void testNudgeScale_ShiftModifier() {
        float result = TransformUtils.nudgeScale(1.0f, 0.1f, true, false);
        assertThat(result).isEqualTo(2.0f); // 1.0 + (0.1 * 10)
    }

    @Test
    @DisplayName("nudgeScale should apply 0.1x multiplier with ctrl")
    void testNudgeScale_CtrlModifier() {
        float result = TransformUtils.nudgeScale(1.0f, 1.0f, false, true);
        assertThat(result).isCloseTo(1.1f, within(0.01f));
    }

    @Test
    @DisplayName("nudgeScale should apply 100x multiplier with both modifiers")
    void testNudgeScale_BothModifiers() {
        float result = TransformUtils.nudgeScale(1.0f, 0.01f, true, true);
        assertThat(result).isEqualTo(2.0f); // 1.0 + (0.01 * 100)
    }

    @Test
    @DisplayName("nudgeScale should not clamp values")
    void testNudgeScale_NoClamp() {
        // Large positive value
        float result1 = TransformUtils.nudgeScale(10.0f, 5.0f, false, false);
        assertThat(result1).isEqualTo(15.0f);

        // Large negative value
        float result2 = TransformUtils.nudgeScale(-10.0f, -5.0f, false, false);
        assertThat(result2).isEqualTo(-15.0f);
    }

    // ========== nudgeScale2D Tests ==========

    @Test
    @DisplayName("nudgeScale2D should update X axis independently")
    void testNudgeScale2D_XAxis() {
        TransformUtils.Scale current = new TransformUtils.Scale(1.0f, 2.0f);
        TransformUtils.Scale result = TransformUtils.nudgeScale2D(current, Axis.X, 0.5f, false, false);

        assertThat(result.x).isEqualTo(1.5f);
        assertThat(result.y).isEqualTo(2.0f);
    }

    @Test
    @DisplayName("nudgeScale2D should update Y axis independently")
    void testNudgeScale2D_YAxis() {
        TransformUtils.Scale current = new TransformUtils.Scale(1.0f, 2.0f);
        TransformUtils.Scale result = TransformUtils.nudgeScale2D(current, Axis.Y, 0.5f, false, false);

        assertThat(result.x).isEqualTo(1.0f);
        assertThat(result.y).isEqualTo(2.5f);
    }

    @Test
    @DisplayName("nudgeScale2D should ignore Z axis")
    void testNudgeScale2D_ZAxis_NoOp() {
        TransformUtils.Scale current = new TransformUtils.Scale(1.0f, 2.0f);
        TransformUtils.Scale result = TransformUtils.nudgeScale2D(current, Axis.Z, 5.0f, false, false);

        // Z axis not implemented, values should remain unchanged
        assertThat(result.x).isEqualTo(1.0f);
        assertThat(result.y).isEqualTo(2.0f);
    }

    @Test
    @DisplayName("nudgeScale2D should apply modifiers correctly")
    void testNudgeScale2D_WithModifiers() {
        TransformUtils.Scale current = new TransformUtils.Scale(1.0f, 1.0f);

        // Shift: 10x
        TransformUtils.Scale result1 = TransformUtils.nudgeScale2D(current, Axis.X, 0.1f, true, false);
        assertThat(result1.x).isEqualTo(2.0f);

        // Both: 100x
        TransformUtils.Scale result2 = TransformUtils.nudgeScale2D(current, Axis.Y, 0.01f, true, true);
        assertThat(result2.y).isEqualTo(2.0f);
    }

    // ========== Scenario-Based Integration Tests ==========

    @Test
    @DisplayName("Scenario: Rotating a blueprint 90 degrees at a time with shift")
    void testScenario_90DegreeRotation() {
        TransformUtils.Rotation rotation = new TransformUtils.Rotation(0f, 0f, 0f);

        // Rotate Y axis 4 times by 90 degrees (full circle)
        rotation = TransformUtils.nudgeRotation3D(rotation, Axis.Y, 1f, true, false); // 90
        assertThat(rotation.y).isEqualTo(90f);

        rotation = TransformUtils.nudgeRotation3D(rotation, Axis.Y, 1f, true, false); // 180
        assertThat(rotation.y).isEqualTo(180f);

        rotation = TransformUtils.nudgeRotation3D(rotation, Axis.Y, 1f, true, false); // 270
        assertThat(rotation.y).isEqualTo(270f);

        rotation = TransformUtils.nudgeRotation3D(rotation, Axis.Y, 1f, true, false); // 360 -> 0
        assertThat(rotation.y).isEqualTo(0f);
    }

    @Test
    @DisplayName("Scenario: Fine-tuning alpha from opaque to transparent")
    void testScenario_AlphaFadeOut() {
        float alpha = 1.0f;

        // Fine-tune decrease by 0.1 five times
        for (int i = 0; i < 5; i++) {
            alpha = TransformUtils.nudgeAlpha(alpha, -1f, false, true); // -1 * 0.1 = -0.1
        }

        assertThat(alpha).isCloseTo(0.5f, within(0.01f));
    }

    @Test
    @DisplayName("Scenario: Moving blueprint in a square path")
    void testScenario_SquareMovement() {
        TransformUtils.Position pos = new TransformUtils.Position(0f, 0f, 0f);

        // Move EAST 10 units
        pos = TransformUtils.nudgePosition3D(pos, Util.Direction.EAST, 10f, false, false);
        assertThat(pos).matches(p -> p.x == 10f && p.y == 0f && p.z == 0f);

        // Move SOUTH 10 units
        pos = TransformUtils.nudgePosition3D(pos, Util.Direction.SOUTH, 10f, false, false);
        assertThat(pos).matches(p -> p.x == 10f && p.y == 0f && p.z == 10f);

        // Move WEST 10 units (back to X=0)
        pos = TransformUtils.nudgePosition3D(pos, Util.Direction.WEST, 10f, false, false);
        assertThat(pos).matches(p -> p.x == 0f && p.y == 0f && p.z == 10f);

        // Move NORTH 10 units (back to origin)
        pos = TransformUtils.nudgePosition3D(pos, Util.Direction.NORTH, 10f, false, false);
        assertThat(pos).matches(p -> p.x == 0f && p.y == 0f && p.z == 0f);
    }

    @Test
    @DisplayName("Scenario: Scaling up then back down")
    void testScenario_ScaleUpDown() {
        TransformUtils.Scale scale = new TransformUtils.Scale(1.0f, 1.0f);

        // Scale up by 0.5
        scale = TransformUtils.nudgeScale2D(scale, Axis.X, 0.5f, false, false);
        scale = TransformUtils.nudgeScale2D(scale, Axis.Y, 0.5f, false, false);
        assertThat(scale.x).isEqualTo(1.5f);
        assertThat(scale.y).isEqualTo(1.5f);

        // Scale back down by 0.5
        scale = TransformUtils.nudgeScale2D(scale, Axis.X, -0.5f, false, false);
        scale = TransformUtils.nudgeScale2D(scale, Axis.Y, -0.5f, false, false);
        assertThat(scale.x).isEqualTo(1.0f);
        assertThat(scale.y).isEqualTo(1.0f);
    }
}
