package vernando.imageref;
import eu.midnightdust.lib.config.MidnightConfig;

public class ImageRefConfig extends MidnightConfig {

    public enum ShowHideEnum {
        SHOW, HIDE
    }
    @Entry(category = "global", name = "Render Through Blocks") public static boolean renderThroughBlocks = false;
    @Entry(category = "global", name = "Show/Hide") public static ShowHideEnum show = ShowHideEnum.SHOW;
    @Entry(category = "global", name = "Position X") public static float positionX = 0f;
    @Entry(category = "global", name = "Position Y") public static float positionY = 0f;
    @Entry(category = "global", name = "Position Z") public static float positionZ = 0f;
    @Entry(category = "global", name = "Scale X") public static float scaleX = 1f;
    @Entry(category = "global", name = "Scale Y") public static float scaleY = 1f;
    @Entry(category = "global", name = "Rotation X", isSlider = true, min = 0, max = 360f, precision = 1000) public static float rotationX = 0f;
    @Entry(category = "global", name = "Rotation Y", isSlider = true, min = 0, max = 360f, precision = 1000) public static float rotationY = 0f;
    @Entry(category = "global", name = "Rotation Z", isSlider = true, min = 0, max = 360f, precision = 1000) public static float rotationZ = 0f;
    @Entry(category = "global", name = "Alpha", isSlider = true, min = 0, max = 1f, precision = 1000) public static float alpha = 1f;
    @Entry(category = "global", name = "Image Path") public static String imagePath = "master-pnp-habshaer-fl-fl0700-fl0701-photos-577579pu.png"; 
    

    public static int imposter = 16777215; // - Entries without an @Entry or @Comment annotation are ignored
}