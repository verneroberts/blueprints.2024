package vernando.blueprints;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Comprehensive tests for MathUtils class.
 * Tests all mathematical calculations and edge cases.
 */
@DisplayName("MathUtils Tests")
class MathUtilsTest {

    // ========== normalizeAngle Tests ==========

    @Test
    @DisplayName("normalizeAngle should handle angles in range [0, 360)")
    void testNormalizeAngle_InRange() {
        assertThat(MathUtils.normalizeAngle(0)).isEqualTo(0);
        assertThat(MathUtils.normalizeAngle(45)).isEqualTo(45);
        assertThat(MathUtils.normalizeAngle(180)).isEqualTo(180);
        assertThat(MathUtils.normalizeAngle(359.9f)).isEqualTo(359.9f);
    }

    @Test
    @DisplayName("normalizeAngle should wrap angles >= 360")
    void testNormalizeAngle_OverRange() {
        assertThat(MathUtils.normalizeAngle(360)).isEqualTo(0);
        assertThat(MathUtils.normalizeAngle(361)).isEqualTo(1);
        assertThat(MathUtils.normalizeAngle(720)).isEqualTo(0);
        assertThat(MathUtils.normalizeAngle(1080)).isEqualTo(0);
    }

    @Test
    @DisplayName("normalizeAngle should wrap negative angles")
    void testNormalizeAngle_Negative() {
        assertThat(MathUtils.normalizeAngle(-1)).isEqualTo(359);
        assertThat(MathUtils.normalizeAngle(-90)).isEqualTo(270);
        assertThat(MathUtils.normalizeAngle(-360)).isEqualTo(0);
        assertThat(MathUtils.normalizeAngle(-720)).isEqualTo(0);
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0",
        "180, 180",
        "360, 0",
        "450, 90",
        "720, 0",
        "-1, 359",
        "-90, 270",
        "-180, 180",
        "-360, 0"
    })
    @DisplayName("normalizeAngle parameterized tests")
    void testNormalizeAngle_Parameterized(float input, float expected) {
        assertThat(MathUtils.normalizeAngle(input)).isCloseTo(expected, within(0.01f));
    }

    // ========== normalizeYaw Tests ==========

    @Test
    @DisplayName("normalizeYaw should handle angles in range [-180, 180]")
    void testNormalizeYaw_InRange() {
        assertThat(MathUtils.normalizeYaw(0)).isEqualTo(0);
        assertThat(MathUtils.normalizeYaw(45)).isEqualTo(45);
        assertThat(MathUtils.normalizeYaw(180)).isEqualTo(180);
        assertThat(MathUtils.normalizeYaw(-180)).isEqualTo(-180);
        assertThat(MathUtils.normalizeYaw(-45)).isEqualTo(-45);
    }

    @Test
    @DisplayName("normalizeYaw should wrap angles > 180")
    void testNormalizeYaw_OverRange() {
        assertThat(MathUtils.normalizeYaw(181)).isEqualTo(-179);
        assertThat(MathUtils.normalizeYaw(270)).isEqualTo(-90);
        assertThat(MathUtils.normalizeYaw(360)).isEqualTo(0);
        assertThat(MathUtils.normalizeYaw(540)).isEqualTo(180); // 540 wraps to 180, not -180
    }

    @Test
    @DisplayName("normalizeYaw should wrap angles < -180")
    void testNormalizeYaw_UnderRange() {
        assertThat(MathUtils.normalizeYaw(-181)).isEqualTo(179);
        assertThat(MathUtils.normalizeYaw(-270)).isEqualTo(90);
        assertThat(MathUtils.normalizeYaw(-360)).isEqualTo(0);
        assertThat(MathUtils.normalizeYaw(-540)).isEqualTo(-180);
    }

    // ========== calculateNudgeAmount Tests ==========
    // Note: This low-level utility is comprehensively tested through TransformUtilsTest.
    // These tests verify the basic multiplier logic for each context.

    @ParameterizedTest
    @CsvSource({
        "ROTATION, false, false, 1.0",
        "ROTATION, true, false, 90.0",
        "ROTATION, false, true, 0.1",
        "ROTATION, true, true, 180.0",
        "POSITION, false, false, 1.0",
        "POSITION, true, false, 10.0",
        "POSITION, false, true, 0.1",
        "POSITION, true, true, 100.0",
        "SCALE, true, false, 10.0",
        "ALPHA, true, false, 10.0"
    })
    @DisplayName("calculateNudgeAmount should apply correct multipliers based on context and modifiers")
    void testCalculateNudgeAmount(MathUtils.NudgeContext context, boolean multiply, boolean finetune, float expectedMultiplier) {
        float base = 1.0f;
        float result = MathUtils.calculateNudgeAmount(base, multiply, finetune, context);
        assertThat(result).isEqualTo(expectedMultiplier);
    }

    // ========== clampAlpha Tests ==========

    @Test
    @DisplayName("clampAlpha should clamp values above 1")
    void testClampAlpha_Above() {
        assertThat(MathUtils.clampAlpha(1.5f)).isEqualTo(1.0f);
        assertThat(MathUtils.clampAlpha(2.0f)).isEqualTo(1.0f);
        assertThat(MathUtils.clampAlpha(100.0f)).isEqualTo(1.0f);
    }

    @Test
    @DisplayName("clampAlpha should clamp values below 0")
    void testClampAlpha_Below() {
        assertThat(MathUtils.clampAlpha(-0.5f)).isEqualTo(0.0f);
        assertThat(MathUtils.clampAlpha(-1.0f)).isEqualTo(0.0f);
        assertThat(MathUtils.clampAlpha(-100.0f)).isEqualTo(0.0f);
    }

    @Test
    @DisplayName("clampAlpha should preserve values in range [0, 1]")
    void testClampAlpha_InRange() {
        assertThat(MathUtils.clampAlpha(0.0f)).isEqualTo(0.0f);
        assertThat(MathUtils.clampAlpha(0.5f)).isEqualTo(0.5f);
        assertThat(MathUtils.clampAlpha(1.0f)).isEqualTo(1.0f);
    }

    // ========== clamp Tests ==========

    @Test
    @DisplayName("clamp with both min and max should work correctly")
    void testClamp_BothBounds() {
        assertThat(MathUtils.clamp(5.0f, 0.0f, 10.0f)).isEqualTo(5.0f);
        assertThat(MathUtils.clamp(-5.0f, 0.0f, 10.0f)).isEqualTo(0.0f);
        assertThat(MathUtils.clamp(15.0f, 0.0f, 10.0f)).isEqualTo(10.0f);
    }

    @Test
    @DisplayName("clamp with null min should only apply max")
    void testClamp_NullMin() {
        assertThat(MathUtils.clamp(5.0f, null, 10.0f)).isEqualTo(5.0f);
        assertThat(MathUtils.clamp(-100.0f, null, 10.0f)).isEqualTo(-100.0f);
        assertThat(MathUtils.clamp(15.0f, null, 10.0f)).isEqualTo(10.0f);
    }

    @Test
    @DisplayName("clamp with null max should only apply min")
    void testClamp_NullMax() {
        assertThat(MathUtils.clamp(5.0f, 0.0f, null)).isEqualTo(5.0f);
        assertThat(MathUtils.clamp(-5.0f, 0.0f, null)).isEqualTo(0.0f);
        assertThat(MathUtils.clamp(100.0f, 0.0f, null)).isEqualTo(100.0f);
    }

    @Test
    @DisplayName("clamp with both null should return value unchanged")
    void testClamp_BothNull() {
        assertThat(MathUtils.clamp(5.0f, null, null)).isEqualTo(5.0f);
        assertThat(MathUtils.clamp(-100.0f, null, null)).isEqualTo(-100.0f);
        assertThat(MathUtils.clamp(100.0f, null, null)).isEqualTo(100.0f);
    }

    // ========== roundToPrecision Tests ==========

    @Test
    @DisplayName("roundToPrecision with 0 precision should round to whole numbers")
    void testRoundToPrecision_Zero() {
        assertThat(MathUtils.roundToPrecision(1.4f, 0)).isEqualTo(1.0f);
        assertThat(MathUtils.roundToPrecision(1.5f, 0)).isEqualTo(2.0f);
        assertThat(MathUtils.roundToPrecision(1.9f, 0)).isEqualTo(2.0f);
    }

    @Test
    @DisplayName("roundToPrecision with 1 precision should round to 1 decimal place")
    void testRoundToPrecision_One() {
        assertThat(MathUtils.roundToPrecision(1.44f, 1)).isEqualTo(1.4f);
        assertThat(MathUtils.roundToPrecision(1.45f, 1)).isEqualTo(1.5f);
        assertThat(MathUtils.roundToPrecision(1.49f, 1)).isEqualTo(1.5f);
    }

    @Test
    @DisplayName("roundToPrecision with 2 precision should round to 2 decimal places")
    void testRoundToPrecision_Two() {
        assertThat(MathUtils.roundToPrecision(1.444f, 2)).isCloseTo(1.44f, within(0.01f));
        assertThat(MathUtils.roundToPrecision(1.445f, 2)).isCloseTo(1.45f, within(0.01f));
        assertThat(MathUtils.roundToPrecision(1.449f, 2)).isCloseTo(1.45f, within(0.01f));
    }

    @Test
    @DisplayName("roundToPrecision with negative precision should use 0")
    void testRoundToPrecision_Negative() {
        assertThat(MathUtils.roundToPrecision(1.5f, -1)).isEqualTo(2.0f);
        assertThat(MathUtils.roundToPrecision(1.4f, -5)).isEqualTo(1.0f);
    }

    // ========== calculateFieldDelta Tests ==========

    @Test
    @DisplayName("calculateFieldDelta with no modifiers should return standard delta")
    void testCalculateFieldDelta_NoModifiers() {
        assertThat(MathUtils.calculateFieldDelta(1.0f, false, false)).isEqualTo(1.0f);
        assertThat(MathUtils.calculateFieldDelta(0.5f, false, false)).isEqualTo(0.5f);
    }

    @Test
    @DisplayName("calculateFieldDelta with shift should return 10x")
    void testCalculateFieldDelta_Shift() {
        assertThat(MathUtils.calculateFieldDelta(1.0f, true, false)).isEqualTo(10.0f);
        assertThat(MathUtils.calculateFieldDelta(0.5f, true, false)).isEqualTo(5.0f);
    }

    @Test
    @DisplayName("calculateFieldDelta with ctrl should return 0.1x")
    void testCalculateFieldDelta_Ctrl() {
        assertThat(MathUtils.calculateFieldDelta(1.0f, false, true)).isCloseTo(0.1f, within(0.001f));
        assertThat(MathUtils.calculateFieldDelta(10.0f, false, true)).isCloseTo(1.0f, within(0.001f));
    }

    @Test
    @DisplayName("calculateFieldDelta with both modifiers should return 100x")
    void testCalculateFieldDelta_Both() {
        assertThat(MathUtils.calculateFieldDelta(1.0f, true, true)).isEqualTo(100.0f);
        assertThat(MathUtils.calculateFieldDelta(0.5f, true, true)).isEqualTo(50.0f);
    }

    // ========== getFilenameFromPath Tests ==========

    @Test
    @DisplayName("getFilenameFromPath should extract filename from Unix path")
    void testGetFilenameFromPath_Unix() {
        assertThat(MathUtils.getFilenameFromPath("/path/to/file.txt")).isEqualTo("file.txt");
        assertThat(MathUtils.getFilenameFromPath("/path/to/dir/image.png")).isEqualTo("image.png");
    }

    @Test
    @DisplayName("getFilenameFromPath should extract filename from Windows path")
    void testGetFilenameFromPath_Windows() {
        assertThat(MathUtils.getFilenameFromPath("C:\\path\\to\\file.txt")).isEqualTo("file.txt");
        assertThat(MathUtils.getFilenameFromPath("C:\\Users\\name\\image.png")).isEqualTo("image.png");
    }

    @Test
    @DisplayName("getFilenameFromPath should handle mixed path separators")
    void testGetFilenameFromPath_Mixed() {
        assertThat(MathUtils.getFilenameFromPath("C:/path\\to/file.txt")).isEqualTo("file.txt");
    }

    @Test
    @DisplayName("getFilenameFromPath should handle filename without path")
    void testGetFilenameFromPath_NoPath() {
        assertThat(MathUtils.getFilenameFromPath("file.txt")).isEqualTo("file.txt");
        assertThat(MathUtils.getFilenameFromPath("image.png")).isEqualTo("image.png");
    }

    @Test
    @DisplayName("getFilenameFromPath should handle null and empty strings")
    void testGetFilenameFromPath_NullEmpty() {
        assertThat(MathUtils.getFilenameFromPath(null)).isEmpty();
        assertThat(MathUtils.getFilenameFromPath("")).isEmpty();
    }

    @Test
    @DisplayName("getFilenameFromPath should handle path with many components")
    void testGetFilenameFromPath_MultipleComponents() {
        assertThat(MathUtils.getFilenameFromPath("/a/b/c/d/e/file.txt")).isEqualTo("file.txt");
        assertThat(MathUtils.getFilenameFromPath("C:\\a\\b\\c\\d\\file.txt")).isEqualTo("file.txt");
    }
}
