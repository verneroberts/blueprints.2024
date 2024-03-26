package vernando.imageref;
import eu.midnightdust.lib.config.MidnightConfig;

public class Config extends MidnightConfig {

    public enum ShowHideEnum {
        SHOW, HIDE
    }
    @Entry(name = "Render Through Blocks") public static boolean renderThroughBlocks = false;    

    public static int imposter = 16777215; // - Entries without an @Entry or @Comment annotation are ignored
}