package vernando.blueprints;

import java.util.function.Consumer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class NumberFieldWidget extends TextFieldWidget {
    private float value;
    private Consumer<Float> changedListener;
    
    public NumberFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, float value, Consumer<Float> changedListener, String tooltip) {
        super(textRenderer, x, y, width, height, Text.literal(String.valueOf(value)));
        this.value = value;
        this.setTooltip(Tooltip.of(Text.literal(tooltip)));

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
    
    public void setValue(float value) {
        this.value = value;
        this.setText(String.valueOf(value));   
        this.changedListener.accept(this.value);     
    }
    
    
    
}
