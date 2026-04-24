package vernando.blueprints;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class BlueprintCalibrationScreen extends Screen {
    private Blueprint blueprint;
    private Screen parent;

    // Calibration points in image-relative coordinates (0.0 to 1.0)
    private Float point1X = null;
    private Float point1Y = null;
    private Float point2X = null;
    private Float point2Y = null;

    // Mouse state tracking
    private Integer draggingPoint = null;
    private boolean wasMousePressed = false;
    private boolean wasMiddleMousePressed = false;
    private int lastPanMouseX = 0;
    private int lastPanMouseY = 0;

    // Zoom and pan
    private float zoom = 1.0f;
    private float panX = 0.0f;
    private float panY = 0.0f;

    // Input field for block distance
    private EditBox distanceField;

    // UI constants
    private static final int POINT_RADIUS = 8;
    private static final int CROSSHAIR_SIZE = 4;
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 10.0f;
    private static final float ZOOM_SPEED = 0.1f;

    protected BlueprintCalibrationScreen(Blueprint blueprint, Screen parent) {
        super(Component.literal("Calibrate: " + blueprint.getName()));
        this.blueprint = blueprint;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 80;
        int buttonHeight = 20;
        int padding = 5;

        // Distance input field with label - responsive sizing
        int fieldWidth = Math.min(120, width / 4);
        int fieldX = width / 2 - fieldWidth / 2;
        distanceField = new EditBox(font,
            fieldX, height - 35, fieldWidth, 20, Component.literal("Distance in blocks"));
        distanceField.setHint(Component.literal("Enter blocks"));
        distanceField.setMaxLength(10);
        distanceField.setValue("1.0");
        addRenderableWidget(distanceField);

        if (width > 600) {
            // Wide screen: buttons on sides, field in middle
            // Reset on left
            addRenderableWidget(
                Button.builder(Component.literal("Reset"), b -> {
                    point1X = null;
                    point1Y = null;
                    point2X = null;
                    point2Y = null;
                    draggingPoint = null;
                    zoom = 1.0f;
                    panX = 0.0f;
                    panY = 0.0f;
                })
                .bounds(padding, height - 35, buttonWidth, buttonHeight)
                .build());

            // Cancel to the right of field
            int cancelX = fieldX + fieldWidth + 10;
            addRenderableWidget(
                Button.builder(Component.literal("Cancel"), b -> {
                    this.onClose();
                })
                .bounds(cancelX, height - 35, buttonWidth, buttonHeight)
                .build());

            // Apply on right
            addRenderableWidget(
                Button.builder(Component.literal("Apply"), b -> {
                    applyCalibration();
                })
                .bounds(width - buttonWidth - padding, height - 35, buttonWidth, buttonHeight)
                .build());
        } else {
            // Narrow screen: stack buttons vertically or use smaller buttons
            int smallButtonWidth = (width - padding * 4) / 3;
            int row1Y = height - 60;
            int row2Y = height - 35;

            // Reset button
            addRenderableWidget(
                Button.builder(Component.literal("Reset"), b -> {
                    point1X = null;
                    point1Y = null;
                    point2X = null;
                    point2Y = null;
                    draggingPoint = null;
                    zoom = 1.0f;
                    panX = 0.0f;
                    panY = 0.0f;
                })
                .bounds(padding, row1Y, smallButtonWidth, buttonHeight)
                .build());

            // Cancel button
            addRenderableWidget(
                Button.builder(Component.literal("Cancel"), b -> {
                    this.onClose();
                })
                .bounds(padding + smallButtonWidth + padding, row1Y, smallButtonWidth, buttonHeight)
                .build());

            // Apply button
            addRenderableWidget(
                Button.builder(Component.literal("Apply"), b -> {
                    applyCalibration();
                })
                .bounds(padding + 2 * (smallButtonWidth + padding), row1Y, smallButtonWidth, buttonHeight)
                .build());

            // Adjust field position for narrow screens
            distanceField.setPosition(fieldX, row2Y);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // Draw background
        extractBackground(context, mouseX, mouseY, delta);

        // Handle mouse input
        handleMouseInput(mouseX, mouseY);

        // Calculate image display bounds
        int imageDisplayWidth = width;
        int imageDisplayHeight = height - 60; // Leave space for controls at bottom

        // Get texture dimensions
        int textureWidth = blueprint.getTextureWidth();
        int textureHeight = blueprint.getTextureHeight();
        if (textureWidth == 0 || textureHeight == 0) {
            textureWidth = 100;
            textureHeight = 100;
        }

        // Calculate scaled dimensions
        float scale = Math.min((float)imageDisplayWidth / textureWidth,
                              (float)imageDisplayHeight / textureHeight) * zoom;
        int scaledWidth = (int)(textureWidth * scale);
        int scaledHeight = (int)(textureHeight * scale);

        // Center the image with pan offset
        int imageX = (width - scaledWidth) / 2 + (int)panX;
        int imageY = (imageDisplayHeight - scaledHeight) / 2 + (int)panY;

        // Draw the blueprint image
        if (blueprint.textureId != null) {
            context.blit(RenderPipelines.GUI_TEXTURED, blueprint.textureId,
                imageX, imageY, 0, 0, scaledWidth, scaledHeight, scaledWidth, scaledHeight);
        }

        // Convert image-relative coordinates to screen coordinates for rendering
        Integer screenPoint1X = null, screenPoint1Y = null;
        Integer screenPoint2X = null, screenPoint2Y = null;

        if (point1X != null && point1Y != null) {
            screenPoint1X = imageX + (int)(point1X * scaledWidth);
            screenPoint1Y = imageY + (int)(point1Y * scaledHeight);
            drawCalibrationPoint(context, screenPoint1X, screenPoint1Y, 1);
        }

        if (point2X != null && point2Y != null) {
            screenPoint2X = imageX + (int)(point2X * scaledWidth);
            screenPoint2Y = imageY + (int)(point2Y * scaledHeight);
            drawCalibrationPoint(context, screenPoint2X, screenPoint2Y, 2);
        }

        if (screenPoint1X != null && screenPoint1Y != null && screenPoint2X != null && screenPoint2Y != null) {
            drawCalibrationLine(context, screenPoint1X, screenPoint1Y, screenPoint2X, screenPoint2Y);

            // Calculate and display pixel distance (in screen pixels)
            double pixelDistance = Math.sqrt(
                Math.pow(screenPoint2X - screenPoint1X, 2) + Math.pow(screenPoint2Y - screenPoint1Y, 2));
            String distanceText = String.format("Distance: %.1f pixels", pixelDistance);
            context.text(font, distanceText, 10, 30, 0xFFFFFF);
        }

        // Draw instructions at the top
        int instructionY = 10;
        context.text(font, "Instructions:", 10, instructionY, 0xFFFFFF00); // Yellow
        instructionY += 12;
        context.text(font, "1. Scroll to zoom, middle-click and drag to pan", 10, instructionY, 0xFFAAAAAA);
        instructionY += 10;
        context.text(font, "2. Click two points on the image with a known distance", 10, instructionY, 0xFFAAAAAA);
        instructionY += 10;
        context.text(font, "3. Enter the distance in blocks and click Apply", 10, instructionY, 0xFFAAAAAA);

        // Draw current step instructions at bottom
        String stepInstructions;
        if (point1X == null) {
            stepInstructions = "Click to place first point";
        } else if (point2X == null) {
            stepInstructions = "Click to place second point";
        } else {
            stepInstructions = "Drag points to adjust, or enter distance and click Apply";
        }
        context.text(font, stepInstructions, 10, height - 55, 0xFFFFFFFF);

        // Draw distance field label (if there's enough space)
        if (width > 400) {
            String distanceLabel = "Distance (blocks):";
            int labelX = distanceField.getX() - font.width(distanceLabel) - 5;
            int labelY = distanceField.getY() + 4; // Vertically center with field
            if (labelX > 10) { // Only draw if there's space
                context.text(font, distanceLabel, labelX, labelY, 0xFFFFFF);
            }
        }

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    private void handleMouseInput(int mouseX, int mouseY) {
        Minecraft client = Minecraft.getInstance();
        long windowHandle = client.getWindow().handle();
        boolean isMousePressed = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean isMiddleMousePressed = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

        // Get current image bounds for coordinate conversion
        int imageDisplayWidth = width;
        int imageDisplayHeight = height - 60;
        int textureWidth = blueprint.getTextureWidth();
        int textureHeight = blueprint.getTextureHeight();
        if (textureWidth == 0 || textureHeight == 0) {
            textureWidth = 100;
            textureHeight = 100;
        }
        float scale = Math.min((float)imageDisplayWidth / textureWidth,
                              (float)imageDisplayHeight / textureHeight) * zoom;
        int scaledWidth = (int)(textureWidth * scale);
        int scaledHeight = (int)(textureHeight * scale);
        int imageX = (width - scaledWidth) / 2 + (int)panX;
        int imageY = (imageDisplayHeight - scaledHeight) / 2 + (int)panY;

        // Handle middle mouse panning
        if (isMiddleMousePressed) {
            if (wasMiddleMousePressed) {
                // Continue panning
                int deltaX = mouseX - lastPanMouseX;
                int deltaY = mouseY - lastPanMouseY;
                panX += deltaX;
                panY += deltaY;
            }
            lastPanMouseX = mouseX;
            lastPanMouseY = mouseY;
        }

        // Convert image-relative points to screen coordinates for hit testing
        Integer screenPoint1X = null, screenPoint1Y = null;
        Integer screenPoint2X = null, screenPoint2Y = null;
        if (point1X != null && point1Y != null) {
            screenPoint1X = imageX + (int)(point1X * scaledWidth);
            screenPoint1Y = imageY + (int)(point1Y * scaledHeight);
        }
        if (point2X != null && point2Y != null) {
            screenPoint2X = imageX + (int)(point2X * scaledWidth);
            screenPoint2Y = imageY + (int)(point2Y * scaledHeight);
        }

        // Detect mouse click (press transition)
        if (isMousePressed && !wasMousePressed) {
            // Mouse button just pressed
            if (mouseY < height - 60) { // Only handle clicks in image area
                // Check if clicking near an existing point
                if (screenPoint1X != null && screenPoint1Y != null && isNearPoint(mouseX, mouseY, screenPoint1X, screenPoint1Y)) {
                    draggingPoint = 1;
                } else if (screenPoint2X != null && screenPoint2Y != null && isNearPoint(mouseX, mouseY, screenPoint2X, screenPoint2Y)) {
                    draggingPoint = 2;
                } else {
                    // Place new point - convert screen coords to image-relative coords
                    if (mouseX >= imageX && mouseX <= imageX + scaledWidth &&
                        mouseY >= imageY && mouseY <= imageY + scaledHeight) {
                        float relX = (float)(mouseX - imageX) / scaledWidth;
                        float relY = (float)(mouseY - imageY) / scaledHeight;

                        if (point1X == null) {
                            point1X = relX;
                            point1Y = relY;
                        } else if (point2X == null) {
                            point2X = relX;
                            point2Y = relY;
                        }
                    }
                }
            }
        }

        // Handle dragging - convert screen coords to image-relative coords
        if (isMousePressed && draggingPoint != null && mouseY < height - 60) {
            if (mouseX >= imageX && mouseX <= imageX + scaledWidth &&
                mouseY >= imageY && mouseY <= imageY + scaledHeight) {
                float relX = (float)(mouseX - imageX) / scaledWidth;
                float relY = (float)(mouseY - imageY) / scaledHeight;

                if (draggingPoint == 1) {
                    point1X = relX;
                    point1Y = relY;
                } else if (draggingPoint == 2) {
                    point2X = relX;
                    point2Y = relY;
                }
            }
        }

        // Detect mouse release
        if (!isMousePressed && wasMousePressed) {
            draggingPoint = null;
        }

        wasMousePressed = isMousePressed;
        wasMiddleMousePressed = isMiddleMousePressed;
    }

    private void drawCalibrationPoint(GuiGraphicsExtractor context, int x, int y, int pointNumber) {
        // Draw hollow circle (ring) instead of filled circle
        // Black outer ring
        drawCircleOutline(context, x, y, POINT_RADIUS, 2, 0xFF000000);
        // White inner ring
        drawCircleOutline(context, x, y, POINT_RADIUS - 1, 1, 0xFFFFFFFF);

        // Draw crosshairs - thinner and with gap in center so you can see the pixel
        int gapSize = 3; // Gap in the center

        // Horizontal crosshair (left side)
        context.fill(x - POINT_RADIUS - CROSSHAIR_SIZE, y - 1,
                    x - gapSize, y, 0xFF000000);
        context.fill(x - POINT_RADIUS - CROSSHAIR_SIZE, y,
                    x - gapSize, y + 1, 0xFFFFFFFF);

        // Horizontal crosshair (right side)
        context.fill(x + gapSize, y - 1,
                    x + POINT_RADIUS + CROSSHAIR_SIZE, y, 0xFF000000);
        context.fill(x + gapSize, y,
                    x + POINT_RADIUS + CROSSHAIR_SIZE, y + 1, 0xFFFFFFFF);

        // Vertical crosshair (top side)
        context.fill(x - 1, y - POINT_RADIUS - CROSSHAIR_SIZE,
                    x, y - gapSize, 0xFF000000);
        context.fill(x, y - POINT_RADIUS - CROSSHAIR_SIZE,
                    x + 1, y - gapSize, 0xFFFFFFFF);

        // Vertical crosshair (bottom side)
        context.fill(x - 1, y + gapSize,
                    x, y + POINT_RADIUS + CROSSHAIR_SIZE, 0xFF000000);
        context.fill(x, y + gapSize,
                    x + 1, y + POINT_RADIUS + CROSSHAIR_SIZE, 0xFFFFFFFF);

        // Draw point number outside the circle
        String label = String.valueOf(pointNumber);
        int labelWidth = font.width(label);
        int labelY = y - POINT_RADIUS - CROSSHAIR_SIZE - font.lineHeight - 2;
        context.text(font, label,
            x - labelWidth / 2, labelY, 0xFFFFFF);
    }

    private void drawCircleOutline(GuiGraphicsExtractor context, int cx, int cy, int radius, int thickness, int color) {
        // Draw a circle outline by drawing a square approximation
        // Top and bottom arcs
        for (int dx = -radius; dx <= radius; dx++) {
            int dy = (int)Math.sqrt(radius * radius - dx * dx);
            // Top arc
            for (int t = 0; t < thickness; t++) {
                context.fill(cx + dx, cy - dy - t, cx + dx + 1, cy - dy - t + 1, color);
            }
            // Bottom arc
            for (int t = 0; t < thickness; t++) {
                context.fill(cx + dx, cy + dy + t, cx + dx + 1, cy + dy + t + 1, color);
            }
        }
        // Left and right arcs
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int)Math.sqrt(radius * radius - dy * dy);
            // Left arc
            for (int t = 0; t < thickness; t++) {
                context.fill(cx - dx - t, cy + dy, cx - dx - t + 1, cy + dy + 1, color);
            }
            // Right arc
            for (int t = 0; t < thickness; t++) {
                context.fill(cx + dx + t, cy + dy, cx + dx + t + 1, cy + dy + 1, color);
            }
        }
    }

    private void drawCalibrationLine(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2) {
        // Draw connecting line with black border for visibility on any background
        // Stop at the edge of the circles instead of going through them
        int dx = x2 - x1;
        int dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length > 0) {
            // Calculate shortened start and end points to stop at circle edges
            double angle = Math.atan2(dy, dx);
            int lineMargin = POINT_RADIUS + 2; // Stop at the outer edge of the circle

            int startX = x1 + (int)(Math.cos(angle) * lineMargin);
            int startY = y1 + (int)(Math.sin(angle) * lineMargin);
            int endX = x2 - (int)(Math.cos(angle) * lineMargin);
            int endY = y2 - (int)(Math.sin(angle) * lineMargin);

            // Recalculate line segment
            int newDx = endX - startX;
            int newDy = endY - startY;
            double newLength = Math.sqrt(newDx * newDx + newDy * newDy);

            if (newLength > 0) {
                int segments = Math.max(10, (int)newLength);

                // Draw black border (larger)
                for (int i = 0; i <= segments; i++) {
                    int px = startX + (int)(newDx * i / segments);
                    int py = startY + (int)(newDy * i / segments);
                    context.fill(px - 2, py - 2, px + 2, py + 2, 0xFF000000);
                }

                // Draw white line on top
                for (int i = 0; i <= segments; i++) {
                    int px = startX + (int)(newDx * i / segments);
                    int py = startY + (int)(newDy * i / segments);
                    context.fill(px - 1, py - 1, px + 1, py + 1, 0xFFFFFFFF);
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Only zoom if not over UI elements
        if (mouseY < height - 60) {
            float oldZoom = zoom;
            zoom += (float)verticalAmount * ZOOM_SPEED;
            zoom = Mth.clamp(zoom, MIN_ZOOM, MAX_ZOOM);

            // Adjust pan to zoom towards mouse position
            float zoomFactor = zoom / oldZoom;
            float mouseXRelative = (float)mouseX - width / 2 - panX;
            float mouseYRelative = (float)mouseY - (height - 60) / 2 - panY;

            panX = (float)mouseX - width / 2 - mouseXRelative * zoomFactor;
            panY = (float)mouseY - (height - 60) / 2 - mouseYRelative * zoomFactor;

            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private boolean isNearPoint(int x, int y, int px, int py) {
        int dx = x - px;
        int dy = y - py;
        return (dx * dx + dy * dy) <= (POINT_RADIUS + 5) * (POINT_RADIUS + 5);
    }

    private void applyCalibration() {
        if (point1X == null || point1Y == null || point2X == null || point2Y == null) {
            return;
        }

        String distanceText = distanceField.getValue();
        if (distanceText == null || distanceText.trim().isEmpty()) {
            return;
        }

        try {
            float blockDistance = Float.parseFloat(distanceText.trim());
            if (blockDistance <= 0) {
                return;
            }

            // Get current texture dimensions
            int textureWidth = blueprint.getTextureWidth();
            int textureHeight = blueprint.getTextureHeight();
            if (textureWidth == 0 || textureHeight == 0) {
                return;
            }

            // Calculate distance between points in image-relative coordinates (0-1 range)
            double relativeDistance = Math.sqrt(
                Math.pow(point2X - point1X, 2) + Math.pow(point2Y - point1Y, 2));

            if (relativeDistance == 0) {
                return;
            }

            // Convert relative distance to texture pixels
            // Since relative coords are 0-1, we need to scale by texture dimensions
            // Taking into account that x and y might have different scales
            double dx = (point2X - point1X) * textureWidth;
            double dy = (point2Y - point1Y) * textureHeight;
            double texturePixelDistance = Math.sqrt(dx * dx + dy * dy);

            // Calculate the scale factor: blocks per texture pixel
            float blocksPerTexturePixel = (float)(blockDistance / texturePixelDistance);

            // Apply this scale to the blueprint
            // The blueprint's scale is in world units, where 1 scale unit = some world units
            // We need to set scaleX and scaleY such that the blueprint appears at the correct size
            float newScaleX = blocksPerTexturePixel * textureWidth / 2;
            float newScaleY = blocksPerTexturePixel * textureHeight / 2;

            blueprint.setScaleX(newScaleX);
            blueprint.setScaleY(newScaleY);
            blueprint.SaveConfig();

            this.onClose();
        } catch (NumberFormatException e) {
            // Invalid input, do nothing
        }
    }
}
