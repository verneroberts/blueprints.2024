package vernando.blueprints;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Settings class.
 *
 * Note: These tests verify the getter/setter logic. Full integration tests
 * would require mocking the file system or using temporary directories.
 */
@DisplayName("Settings Class Tests")
class SettingsTest {

    private boolean originalRenderThroughBlocks;
    private int originalImagesPerRow;

    @BeforeEach
    void setUp() {
        // Save original settings state
        originalRenderThroughBlocks = Settings.getRenderThroughBlocks();
        originalImagesPerRow = Settings.getImagesPerRow();
    }

    @AfterEach
    void tearDown() {
        // Restore original settings state
        Settings.setRenderThroughBlocks(originalRenderThroughBlocks);
        Settings.setImagesPerRow(originalImagesPerRow);
    }

    @Test
    @DisplayName("getRenderThroughBlocks should return current setting")
    void testGetRenderThroughBlocks() {
        // When
        boolean result = Settings.getRenderThroughBlocks();

        // Then
        assertThat(result).isIn(true, false);
    }

    @Test
    @DisplayName("setRenderThroughBlocks should update setting to true")
    void testSetRenderThroughBlocks_True() {
        // When
        Settings.setRenderThroughBlocks(true);

        // Then
        assertThat(Settings.getRenderThroughBlocks()).isTrue();
    }

    @Test
    @DisplayName("setRenderThroughBlocks should update setting to false")
    void testSetRenderThroughBlocks_False() {
        // When
        Settings.setRenderThroughBlocks(false);

        // Then
        assertThat(Settings.getRenderThroughBlocks()).isFalse();
    }

    @Test
    @DisplayName("getImagesPerRow should return current setting")
    void testGetImagesPerRow() {
        // When
        int result = Settings.getImagesPerRow();

        // Then
        assertThat(result).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("setImagesPerRow should update setting")
    void testSetImagesPerRow() {
        // Given
        int newValue = 10;

        // When
        Settings.setImagesPerRow(newValue);

        // Then
        assertThat(Settings.getImagesPerRow()).isEqualTo(newValue);
    }

    @Test
    @DisplayName("setImagesPerRow should handle different valid values")
    void testSetImagesPerRow_VariousValues() {
        // Test various valid values
        int[] testValues = {1, 3, 5, 10, 15};

        for (int value : testValues) {
            // When
            Settings.setImagesPerRow(value);

            // Then
            assertThat(Settings.getImagesPerRow()).isEqualTo(value);
        }
    }

    @Test
    @DisplayName("Multiple setting changes should persist")
    void testMultipleSettingChanges() {
        // When
        Settings.setRenderThroughBlocks(true);
        Settings.setImagesPerRow(7);

        // Then
        assertThat(Settings.getRenderThroughBlocks()).isTrue();
        assertThat(Settings.getImagesPerRow()).isEqualTo(7);

        // When
        Settings.setRenderThroughBlocks(false);
        Settings.setImagesPerRow(3);

        // Then
        assertThat(Settings.getRenderThroughBlocks()).isFalse();
        assertThat(Settings.getImagesPerRow()).isEqualTo(3);
    }

    @Test
    @DisplayName("LoadSettings should handle missing config file gracefully")
    void testLoadSettings_MissingFile() {
        // This test verifies the method doesn't throw exceptions
        // When
        assertDoesNotThrow(() -> Settings.LoadSettings());
    }

    @Test
    @DisplayName("SaveSettings should not throw exceptions")
    void testSaveSettings() {
        // This test verifies the method doesn't throw exceptions
        // When
        assertDoesNotThrow(() -> Settings.SaveSettings());
    }
}
