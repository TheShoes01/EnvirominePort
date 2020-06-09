package envirominePort.trackers.properties.helpers;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public interface PropertyBase {

    public abstract String categoryName();

    public abstract String categoryDescription();

    public abstract void LoadProperty(Configuration cofig, String category);

    public abstract void SaveProperty(Configuration confic, String category);

    public abstract void GenDefaults();

    public abstract File GetDefaultFile();

    public abstract void generateEmpty(Configuration config, Object obj);

    public abstract boolean useCustomConfigs();

    public abstract void customLoad();
}
