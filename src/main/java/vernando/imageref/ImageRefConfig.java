package vernando.imageref;

import com.google.common.collect.Lists;
import eu.midnightdust.lib.config.MidnightConfig;
import eu.midnightdust.lib.config.MidnightConfig.Entry;

import java.util.List;


public class ImageRefConfig extends MidnightConfig {

    public enum ShowHideEnum {
        SHOW, HIDE
    }
    @Entry(category = "global", name = "Show/Hide") public static ShowHideEnum show = ShowHideEnum.SHOW;
    @Entry(category = "global", name = "Position X") public static double positionX = 0d;
    @Entry(category = "global", name = "Position Y") public static double positionY = 0d;
    @Entry(category = "global", name = "Position Z") public static double positionZ = 0d;
    @Entry(category = "global", name = "Scale X") public static double scaleX = 1d;
    @Entry(category = "global", name = "Scale Y") public static double scaleY = 1d;
    @Entry(category = "global", name = "Scale Z") public static double scaleZ = 1d;
    @Entry(category = "global", name = "Rotation X", isSlider = true, min = 0, max = 1f, precision = 1000) public static double rotationX = 0d;
    @Entry(category = "global", name = "Rotation Y", isSlider = true, min = 0, max = 1f, precision = 1000) public static double rotationY = 0d;
    @Entry(category = "global", name = "Rotation Z", isSlider = true, min = 0, max = 1f, precision = 1000) public static double rotationZ = 0d;
    @Entry(category = "global", name = "Image Path") public static String imagePath = "./test.png"; 
    

    public static int imposter = 16777215; // - Entries without an @Entry or @Comment annotation are ignored
}