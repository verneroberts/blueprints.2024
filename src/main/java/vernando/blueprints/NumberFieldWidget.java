package vernando.blueprints;

import java.util.function.Consumer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class NumberFieldWidget extends TextFieldWidget {
    private float value;
    private float standardDelta;
    private Consumer<Float> changedListener;
    private Float maxValue;
    private Float minValue;
    
    public NumberFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, float value, Consumer<Float> changedListener, String tooltip, float standardDelta) {
        super(textRenderer, x, y, width, height, Text.literal(String.valueOf(value)));
        this.value = value;
        this.standardDelta = standardDelta;
        this.setTooltip(Tooltip.of(Text.literal(tooltip)));
        this.setText(String.valueOf(value));   
        this.setCursorToStart(false);

        this.changedListener = changedListener;

        setChangedListener((s) -> {
            try {
                float newValue = Float.parseFloat(s);                
                this.value = newValue;
                changedListener.accept(this.value);
                
            } catch (NumberFormatException e) {
                // do nothing
            }
        });
    }
    
    public float getValue() {
        return value;
    }

    public NumberFieldWidget setMaxValue(Float maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public NumberFieldWidget setMinValue(Float minValue) {
        this.minValue = minValue;
        return this;
    }
    
    public NumberFieldWidget setValue(float value) {

        if (maxValue != null && value > maxValue) {
            value = maxValue;
        }

        if (minValue != null && value < minValue) {
            value = minValue;
        }

        this.value = value;
        this.setText(String.valueOf(value));   
        this.setCursorToStart(false);
        this.changedListener.accept(this.value);     
        return this;
    }
    
    public NumberFieldWidget increment(boolean shift, boolean ctrl) {
        float x = standardDelta;
        if (shift && ctrl) {
            x *= 100;
        } else {
            if (shift) {
                x *= 10;
            }
            if (ctrl) {
                x *= 0.1;
            }
        }

        this.setValue(this.value + x);
        return this;
    }

    public NumberFieldWidget decrement(boolean shift, boolean ctrl) {
        float x = standardDelta;
        if (shift && ctrl) {
            x *= 100;
        } else {
            if (shift) {
                x *= 10;
            }
            if (ctrl) {
                x *= 0.1;
            }
        }

        this.setValue(this.value - x);
        return this;
    }


    
}
