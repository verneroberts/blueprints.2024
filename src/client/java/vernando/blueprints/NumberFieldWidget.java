package vernando.blueprints;

import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class NumberFieldWidget extends EditBox {
    private float value;
    private float standardDelta;
    private Consumer<Float> changedListener;
    private Float maxValue;
    private Float minValue;
    private int precision=1;
    
    public NumberFieldWidget(Font textRenderer, int x, int y, int width, int height, float value, Consumer<Float> changedListener, String tooltip, float standardDelta) {
        super(textRenderer, x, y, width, height, Component.literal(String.valueOf(value)));
        this.value = value;
        this.standardDelta = standardDelta;
        this.setTooltip(Tooltip.create(Component.literal(tooltip)));
        this.setValue(String.valueOf(value));   
        this.moveCursorToStart(false);

        this.changedListener = changedListener;

        setResponder((s) -> {
            try {
                float newValue = Float.parseFloat(s);                
                this.value = newValue;
                changedListener.accept(this.value);
                
            } catch (NumberFormatException e) {
                // do nothing
            }
        });
    }
    
    public float getFloatValue() {
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

    public NumberFieldWidget setStandardDelta(float standardDelta) {
        this.standardDelta = standardDelta;
        return this;
    }

    public NumberFieldWidget setPrecision(int precision) {
        this.precision = precision;
        return this;
    }
    
    public NumberFieldWidget setValue(float value) {
        value = MathUtils.roundToPrecision(value, precision);
        value = MathUtils.clamp(value, minValue, maxValue);

        this.value = value;
        this.setValue(String.valueOf(value));
        this.moveCursorToStart(false);
        this.changedListener.accept(this.value);
        return this;
    }
    
    public NumberFieldWidget increment(boolean shift, boolean ctrl) {
        float delta = MathUtils.calculateFieldDelta(standardDelta, shift, ctrl);
        this.setValue(this.value + delta);
        return this;
    }

    public NumberFieldWidget decrement(boolean shift, boolean ctrl) {
        float delta = MathUtils.calculateFieldDelta(standardDelta, shift, ctrl);
        this.setValue(this.value - delta);
        return this;
    }


    
}
