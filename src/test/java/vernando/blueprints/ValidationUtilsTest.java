package vernando.blueprints;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for ValidationUtils class.
 * Tests file validation and input validation logic.
 */
@DisplayName("ValidationUtils Tests")
class ValidationUtilsTest {

    // ========== isImageFile Tests ==========

    @ParameterizedTest
    @ValueSource(strings = {
        "image.jpg", "image.jpeg", "image.png", "image.gif", "image.bmp",
        "image.tiff", "image.tif", "image.webp", "image.JPG", "image.PNG",
        "IMAGE.JPG", "Photo.JPEG"
    })
    @DisplayName("isImageFile should return true for common image formats")
    void testIsImageFile_CommonFormats(String filename) {
        assertThat(ValidationUtils.isImageFile(filename)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "image.cr2", "image.nef", "image.arw", "image.dng", "image.orf",
        "image.rw2", "image.pef", "image.raw"
    })
    @DisplayName("isImageFile should return true for RAW formats")
    void testIsImageFile_RawFormats(String filename) {
        assertThat(ValidationUtils.isImageFile(filename)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "image.psd", "image.psb", "image.ai", "image.eps"
    })
    @DisplayName("isImageFile should return true for Adobe formats")
    void testIsImageFile_AdobeFormats(String filename) {
        assertThat(ValidationUtils.isImageFile(filename)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "image.svg", "image.svgz"
    })
    @DisplayName("isImageFile should return true for vector formats")
    void testIsImageFile_VectorFormats(String filename) {
        assertThat(ValidationUtils.isImageFile(filename)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "image.ico", "image.cur", "image.pcx", "image.ppm"
    })
    @DisplayName("isImageFile should return true for other formats")
    void testIsImageFile_OtherFormats(String filename) {
        assertThat(ValidationUtils.isImageFile(filename)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "image.hdr", "image.exr", "image.pfm", "image.rgbe"
    })
    @DisplayName("isImageFile should return true for HDR formats")
    void testIsImageFile_HdrFormats(String filename) {
        assertThat(ValidationUtils.isImageFile(filename)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "image.jpe", "image.jfif"
    })
    @DisplayName("isImageFile should return true for JPEG variants")
    void testIsImageFile_JpegVariants(String filename) {
        assertThat(ValidationUtils.isImageFile(filename)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "document.txt", "document.doc", "document.pdf", "script.js",
        "style.css", "data.json", "archive.zip", "program.exe"
    })
    @DisplayName("isImageFile should return false for non-image formats")
    void testIsImageFile_NonImageFormats(String filename) {
        assertThat(ValidationUtils.isImageFile(filename)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("isImageFile should return false for null and empty strings")
    void testIsImageFile_NullAndEmpty(String filename) {
        assertThat(ValidationUtils.isImageFile(filename)).isFalse();
    }

    @Test
    @DisplayName("isImageFile should return false for filename without extension")
    void testIsImageFile_NoExtension() {
        assertThat(ValidationUtils.isImageFile("imagefile")).isFalse();
        assertThat(ValidationUtils.isImageFile("noextension")).isFalse();
    }

    @Test
    @DisplayName("isImageFile should return false for filename ending with dot")
    void testIsImageFile_EndsWithDot() {
        assertThat(ValidationUtils.isImageFile("image.")).isFalse();
        assertThat(ValidationUtils.isImageFile("file.")).isFalse();
    }

    @Test
    @DisplayName("isImageFile should handle filenames with multiple dots")
    void testIsImageFile_MultipleDots() {
        assertThat(ValidationUtils.isImageFile("my.image.file.jpg")).isTrue();
        assertThat(ValidationUtils.isImageFile("my.document.file.txt")).isFalse();
    }

    @Test
    @DisplayName("isImageFile should handle full paths")
    void testIsImageFile_FullPath() {
        assertThat(ValidationUtils.isImageFile("/path/to/image.png")).isTrue();
        assertThat(ValidationUtils.isImageFile("C:\\Users\\name\\photo.jpg")).isTrue();
        assertThat(ValidationUtils.isImageFile("/path/to/document.txt")).isFalse();
    }

    // ========== isImageFile with dynamic formats Tests ==========

    @Test
    @DisplayName("isImageFile with dynamic formats should check static first")
    void testIsImageFile_DynamicFormats_StaticFirst() {
        Set<String> dynamicFormats = new HashSet<>();
        dynamicFormats.add("xyz");

        // Should still recognize standard formats
        assertThat(ValidationUtils.isImageFile("image.jpg", dynamicFormats)).isTrue();
        assertThat(ValidationUtils.isImageFile("image.png", dynamicFormats)).isTrue();
    }

    @Test
    @DisplayName("isImageFile with dynamic formats should recognize custom formats")
    void testIsImageFile_DynamicFormats_Custom() {
        Set<String> dynamicFormats = new HashSet<>();
        dynamicFormats.add("custom");
        dynamicFormats.add("special");

        assertThat(ValidationUtils.isImageFile("image.custom", dynamicFormats)).isTrue();
        assertThat(ValidationUtils.isImageFile("image.special", dynamicFormats)).isTrue();
        assertThat(ValidationUtils.isImageFile("image.unknown", dynamicFormats)).isFalse();
    }

    @Test
    @DisplayName("isImageFile with null dynamic formats should work")
    void testIsImageFile_DynamicFormats_Null() {
        assertThat(ValidationUtils.isImageFile("image.jpg", null)).isTrue();
        assertThat(ValidationUtils.isImageFile("image.unknown", null)).isFalse();
    }

    @Test
    @DisplayName("isImageFile with empty dynamic formats should work")
    void testIsImageFile_DynamicFormats_Empty() {
        Set<String> emptySet = new HashSet<>();
        assertThat(ValidationUtils.isImageFile("image.jpg", emptySet)).isTrue();
        assertThat(ValidationUtils.isImageFile("image.unknown", emptySet)).isFalse();
    }

    // ========== getFileExtension Tests ==========

    @Test
    @DisplayName("getFileExtension should extract extension from filename")
    void testGetFileExtension_Simple() {
        assertThat(ValidationUtils.getFileExtension("file.txt")).isEqualTo("txt");
        assertThat(ValidationUtils.getFileExtension("image.jpg")).isEqualTo("jpg");
        assertThat(ValidationUtils.getFileExtension("document.pdf")).isEqualTo("pdf");
    }

    @Test
    @DisplayName("getFileExtension should handle uppercase extensions")
    void testGetFileExtension_Uppercase() {
        assertThat(ValidationUtils.getFileExtension("file.TXT")).isEqualTo("txt");
        assertThat(ValidationUtils.getFileExtension("IMAGE.JPG")).isEqualTo("jpg");
        assertThat(ValidationUtils.getFileExtension("Document.PDF")).isEqualTo("pdf");
    }

    @Test
    @DisplayName("getFileExtension should handle multiple dots")
    void testGetFileExtension_MultipleDots() {
        assertThat(ValidationUtils.getFileExtension("my.file.name.txt")).isEqualTo("txt");
        assertThat(ValidationUtils.getFileExtension("archive.tar.gz")).isEqualTo("gz");
    }

    @Test
    @DisplayName("getFileExtension should return null for no extension")
    void testGetFileExtension_NoExtension() {
        assertThat(ValidationUtils.getFileExtension("filename")).isNull();
        assertThat(ValidationUtils.getFileExtension("noextension")).isNull();
    }

    @Test
    @DisplayName("getFileExtension should return null for filename ending with dot")
    void testGetFileExtension_EndsWithDot() {
        assertThat(ValidationUtils.getFileExtension("file.")).isNull();
        assertThat(ValidationUtils.getFileExtension("image.")).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("getFileExtension should return null for null and empty strings")
    void testGetFileExtension_NullAndEmpty(String filename) {
        assertThat(ValidationUtils.getFileExtension(filename)).isNull();
    }

    @Test
    @DisplayName("getFileExtension should handle full paths")
    void testGetFileExtension_FullPath() {
        assertThat(ValidationUtils.getFileExtension("/path/to/file.txt")).isEqualTo("txt");
        assertThat(ValidationUtils.getFileExtension("C:\\Users\\name\\image.jpg")).isEqualTo("jpg");
    }

    // ========== getSupportedExtensions Tests ==========

    @Test
    @DisplayName("getSupportedExtensions should return non-empty set")
    void testGetSupportedExtensions_NotEmpty() {
        Set<String> extensions = ValidationUtils.getSupportedExtensions();
        assertThat(extensions).isNotEmpty();
    }

    @Test
    @DisplayName("getSupportedExtensions should contain common formats")
    void testGetSupportedExtensions_CommonFormats() {
        Set<String> extensions = ValidationUtils.getSupportedExtensions();
        assertThat(extensions).contains("jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp");
    }

    @Test
    @DisplayName("getSupportedExtensions should contain RAW formats")
    void testGetSupportedExtensions_RawFormats() {
        Set<String> extensions = ValidationUtils.getSupportedExtensions();
        assertThat(extensions).contains("cr2", "nef", "arw", "dng", "raw");
    }

    @Test
    @DisplayName("getSupportedExtensions should contain variant formats")
    void testGetSupportedExtensions_Variants() {
        Set<String> extensions = ValidationUtils.getSupportedExtensions();
        assertThat(extensions).contains("jpe", "jfif");
    }

    // ========== isSupportedExtension Tests ==========

    @ParameterizedTest
    @ValueSource(strings = {"jpg", "png", "gif", "bmp", "webp", "tiff"})
    @DisplayName("isSupportedExtension should return true for common formats")
    void testIsSupportedExtension_CommonFormats(String extension) {
        assertThat(ValidationUtils.isSupportedExtension(extension)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"JPG", "PNG", "GIF", "BMP"})
    @DisplayName("isSupportedExtension should be case-insensitive")
    void testIsSupportedExtension_CaseInsensitive(String extension) {
        assertThat(ValidationUtils.isSupportedExtension(extension)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"txt", "doc", "pdf", "exe", "zip"})
    @DisplayName("isSupportedExtension should return false for non-image formats")
    void testIsSupportedExtension_NonImageFormats(String extension) {
        assertThat(ValidationUtils.isSupportedExtension(extension)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("isSupportedExtension should return false for null and empty strings")
    void testIsSupportedExtension_NullAndEmpty(String extension) {
        assertThat(ValidationUtils.isSupportedExtension(extension)).isFalse();
    }

    // ========== isValidFloat Tests ==========

    @ParameterizedTest
    @ValueSource(strings = {"1.0", "0.5", "-1.5", "123.456", "0", "1", "-1", "3.14159"})
    @DisplayName("isValidFloat should return true for valid floats")
    void testIsValidFloat_Valid(String value) {
        assertThat(ValidationUtils.isValidFloat(value)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "one", "1.2.3", "12a", "a12"})
    @DisplayName("isValidFloat should return false for invalid floats")
    void testIsValidFloat_Invalid(String value) {
        assertThat(ValidationUtils.isValidFloat(value)).isFalse();
    }

    @Test
    @DisplayName("isValidFloat should handle special float values")
    void testIsValidFloat_SpecialValues() {
        // NaN and Infinity are technically valid floats in Java
        assertThat(ValidationUtils.isValidFloat("NaN")).isTrue();
        assertThat(ValidationUtils.isValidFloat("Infinity")).isTrue();
        assertThat(ValidationUtils.isValidFloat("-Infinity")).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("isValidFloat should return false for null and empty strings")
    void testIsValidFloat_NullAndEmpty(String value) {
        assertThat(ValidationUtils.isValidFloat(value)).isFalse();
    }

    @Test
    @DisplayName("isValidFloat should handle scientific notation")
    void testIsValidFloat_ScientificNotation() {
        assertThat(ValidationUtils.isValidFloat("1e10")).isTrue();
        assertThat(ValidationUtils.isValidFloat("1.5e-5")).isTrue();
    }

    // ========== isValidInteger Tests ==========

    @ParameterizedTest
    @ValueSource(strings = {"1", "0", "-1", "123", "-456", "999999"})
    @DisplayName("isValidInteger should return true for valid integers")
    void testIsValidInteger_Valid(String value) {
        assertThat(ValidationUtils.isValidInteger(value)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.0", "0.5", "abc", "one", "12a", "a12", "1.2.3"})
    @DisplayName("isValidInteger should return false for invalid integers")
    void testIsValidInteger_Invalid(String value) {
        assertThat(ValidationUtils.isValidInteger(value)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("isValidInteger should return false for null and empty strings")
    void testIsValidInteger_NullAndEmpty(String value) {
        assertThat(ValidationUtils.isValidInteger(value)).isFalse();
    }

    @Test
    @DisplayName("isValidInteger should reject floats")
    void testIsValidInteger_RejectFloat() {
        assertThat(ValidationUtils.isValidInteger("1.0")).isFalse();
        assertThat(ValidationUtils.isValidInteger("123.456")).isFalse();
    }
}
