package vernando.blueprints;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Util class.
 *
 * Note: Many methods in Util require Minecraft client context and are difficult to unit test
 * without integration testing. This class demonstrates testing patterns for testable methods.
 */
@DisplayName("Util Class Tests")
class UtilTest {

    @Test
    @DisplayName("GetConfigPath should return valid config path")
    void testGetConfigPath() {
        // When
        String path = Util.GetConfigPath();

        // Then
        assertThat(path).isNotNull();
        assertThat(path).contains("config");
        assertThat(path).contains(Main.MOD_ID);
    }

    @Test
    @DisplayName("IsGifFile should return true for .gif extension")
    void testIsGifFile_WithGifExtension() {
        // Given
        String gifPath = "test/image.gif";

        // When
        boolean result = Util.IsGifFile(gifPath);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("IsGifFile should return true for .GIF extension (case insensitive)")
    void testIsGifFile_WithUppercaseGifExtension() {
        // Given
        String gifPath = "test/IMAGE.GIF";

        // When
        boolean result = Util.IsGifFile(gifPath);

        // Then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"test.png", "test.jpg", "test.jpeg", "test.bmp", "test"})
    @DisplayName("IsGifFile should return false for non-gif files")
    void testIsGifFile_WithNonGifExtension(String filename) {
        // When
        boolean result = Util.IsGifFile(filename);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("GifAnimation should initialize with empty frames")
    void testGifAnimation_Initialization() {
        // When
        Util.GifAnimation animation = new Util.GifAnimation();

        // Then
        assertThat(animation.frames).isNotNull();
        assertThat(animation.frames).isEmpty();
        assertThat(animation.isAnimated).isFalse();
    }

    @Test
    @DisplayName("GifFrame should store image and delay correctly")
    void testGifFrame_Creation() {
        // Given
        int expectedDelay = 100;

        // When
        Util.GifFrame frame = new Util.GifFrame(null, expectedDelay);

        // Then
        assertThat(frame.delayMs).isEqualTo(expectedDelay);
        assertThat(frame.image).isNull();
    }

    @Test
    @DisplayName("GetPerWorldDimensionConfigPath should return valid path")
    void testGetPerWorldDimensionConfigPath() {
        // When
        String path = Util.GetPerWorldDimensionConfigPath();

        // Then
        assertThat(path).isNotNull();
        assertThat(path).contains("config");
        assertThat(path).contains(Main.MOD_ID);
    }

    @Test
    @DisplayName("Direction enum should have all expected values")
    void testDirection_EnumValues() {
        // When
        Util.Direction[] directions = Util.Direction.values();

        // Then
        assertThat(directions).hasSize(6);
        assertThat(directions).contains(
            Util.Direction.NORTH,
            Util.Direction.SOUTH,
            Util.Direction.EAST,
            Util.Direction.WEST,
            Util.Direction.UP,
            Util.Direction.DOWN
        );
    }
}
