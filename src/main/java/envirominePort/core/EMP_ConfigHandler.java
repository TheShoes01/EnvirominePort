package envirominePort.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.logging.log4j.Level;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import envirominePort.client.gui.hud.HUDRegistry;

import envirominePort.handlers.ObjectHandler;
import envirominePort.handlers.Legacy.LegacyHandler;
import envirominePort.trackers.properties.ArmorProperties;
import envirominePort.trackers.properties.BiomeProperties;
import envirominePort.trackers.properties.BlockProperties;
import envirominePort.trackers.properties.CaveBaseProperties;
import envirominePort.trackers.properties.CaveGenProperties;
import envirominePort.trackers.properties.CaveSpawnProperties;
import envirominePort.trackers.properties.DimensionProperties;
import envirominePort.trackers.properties.EntityProperties;
import envirominePort.trackers.properties.ItemProperties;
import envirominePort.trackers.properties.RotProperties;
import envirominePort.trackers.properties.StabilityType;

import envirominePort.trackers.properties.helpers.PropertyBase;
import envirominePort.utils.ModIdentification;
import envirominePort.world.EMP_WorldData;

public class EMP_ConfigHandler {
    public static String configPath = "config/envirominePort/";
    public static String customPath = "CustomProperties/";

    public static String profilePath = configPath + "profiles/";
    public static String defaultProfile = profilePath + "default/";

    public static final String CONFIG_VERSION = "1.0.0";

    public static String loadedProfile = defaultProfile;

    static HashMap<String, PropertyBase> propTypes;
    public static HashMap<String, PropertyBase> globalTypes;

    public static List loadedConfigs = new ArrayList();

    static {
        propTypes = new HashMap<String, PropertyBase>();

        propTypes.put(BiomeProperties.base.categoryName(), BiomeProperties.base);
        propTypes.put(ArmorProperties.base.categoryName(), ArmorProperties.base);
        propTypes.put(BlockProperties.base.categoryName(), BlockProperties.base);
        propTypes.put(DimensionProperties.base.categoryName(), DimensionProperties.base);
        propTypes.put(EntityProperties.base.categoryName(), EntityProperties.base);
        propTypes.put(ItemProperties.base.categoryName(), ItemProperties.base);
        propTypes.put(RotProperties.base.categoryName(), RotProperties.base);

        globalTypes = new HashMap<String, PropertyBase>();
        globalTypes.put(CaveGenProperties.base.categoryName(), CaveGenProperties.base);
        globalTypes.put(CaveSpawnProperties.base.categoryName(), CaveSpawnProperties.base);
        globalTypes.put(CaveBaseProperties.base.categoryName(), CaveBaseProperties.base);
    }

    public static void initProfile() {
        String profile = EMP_Settings.profileSelected;

        System.out.println("LOADING PROFILE: " + profile);

        File profileDir = new File(profilePath + profile + "/" + customPath);

        if (!profileDir.exists()) {
            try {
                profileDir.mkdirs();
            } catch (Exception e) {
                EnviroMinePort.logger.log(Level.ERROR, "Unable to create directories for profiles", e);
            }
        }

        if (!profileDir.exists()) {
            EnviroMinePort.logger.log(level.ERROR, "Failed to load Profile:" + profile + ". Loading Default");
            profileDir = new File(defaultProfile + customPath);
            loadedProfile = defaultProfile;
        } else {
            loadedProfile = profilePath + profile + "/";
            EnviroMinePort.logger.log(Level.INFO, "Loading Profile: " + profile);
        }

        File ProfileSettings = new File(loadedProfile + profile + "_Settings.cfg");
        loadProfileConfig(ProfileSettings);

        loadHudItems();

        StabilityType.base.GenDefaults();
        StabilityType.base.customLoad();

        if (EMP_Settings.genDefaults) {
            loadDefaultProperties();
        }

        File[] customFiles = GetFileList(loadedProfile + customPath);
        for (int i = 0; i < customFiles.length; i++) {
            LoadCustomObjects(customFiles[i]);
        }

        Iterator<PropertyBase> iterator = propTypes.values().iterator();

        while (iterator.hasNext()) {
            PropertyBase props = iterator.next();

            if (!props.useCustomConfigs()) {
                props.customLoad();
            }
        }

        EnviroMine.logger.log(Level.INFO, "Loaded " + EMP_Settings.stabilityTypes.size() + " stability types");
        EnviroMine.logger.log(Level.INFO, "Loaded " + EMP_Settings.armorProperties.size() + " armor properties");
        EnviroMine.logger.log(Level.INFO, "Loaded " + EMP_Settings.blockProperties.size() + " block properties");
        EnviroMine.logger.log(Level.INFO, "Loaded " + EMP_Settings.livingProperties.size() + " entity properties");
        EnviroMine.logger.log(Level.INFO, "Loaded " + EMP_Settings.itemProperties.size() + " item properties");
        EnviroMine.logger.log(Level.INFO, "Loaded " + EMP_Settings.rotProperties.size() + " rot properties");
        EnviroMine.logger.log(Level.INFO, "Loaded " + EMP_Settings.biomeProperties.size() + " biome properties");
        EnviroMine.logger.log(Level.INFO, "Loaded " + EMP_Settings.dimensionProperties.size() + " dimension properties");
        EnviroMine.logger.log(Level.INFO, "Loaded " + EMP_Settings.caveGenProperties.size() + " cave ore properties");
        EnviroMine.logger.log(Level.INFO, "Loaded " + EMP_Settings.caveSpawnProperties.size() + " cave entity properties");
    }

    public static void loadHudItems() {
        if (!EMP_Settings.enableAirQ && HUDRegistry.isActiveHudItem(HUDRegistry.getHudItemByID(3))) {
            HUDRegistry.disableHudItem(HUDRegistry.getHudItemByID(3));
        }
        if(!EMP_Settings.enableBodyTemp && HUDRegistry.isActiveHudItem(HUDRegistry.getHudItemByID(0))) {
            HUDRegistry.disableHudItem(HUDRegistry.getHudItemByID(0));
        }
        if(!EMP_Settings.enableHydrate && HUDRegistry.isActiveHudItem(HUDRegistry.getHudItemByID(1))) {
            HUDRegistry.disableHudItem(HUDRegistry.getHudItemByID(1));
        }
        if(!EMP_Settings.enableSanity && HUDRegistry.isActiveHudItem(HUDRegistry.getHudItemByID(2))) {
            HUDRegistry.disableHudItem(HUDRegistry.getHudItemByID(2));
        }
    }

    public static int initConfig() {
        EnviroMinePort.logger.log(Level.INFO, "Loading configs...");

        File configFile = new File(configPath + "Global_settings.cfg");
        loadGlobalConfig(configFile);

        Iterator<PropertyBase> iterator = globalTypes.values().iterator();

        while (iterator.hasNext()) {
            PropertyBase props = iterator.next();

            if (!props.useCustomConfigs()) {
                props.customLoad();
            }
        }

        int Total = EMP_Settings.armorProperies.size() + EMP_Settings.blockProperties.size() + EMP_Settings.livingProperties.size() + EMP_Settings.itemProperties.size() + EMP_Settings.biomeProperties.size() + EMP_Settings.dimensionProperties.size() + EMP_Settings.caveGenProperties.size() + EMP_Settings.caveSpawnProperties.size();

        return Total;
    }

    private static void loadGlobalConfig(File file) {
        Configuration config;
        try {
            config = new Configuration(file, true);
        } catch (Exception e) {
            EnviroMinePort.logger.log(Level.WARN, "Failed to load main configuration file!", e);
            return;
        }

        config.load();

        EMP_Settings.shaftGen = config.get("World Generation", "Enable Village MineShafts", EMP_Settings.shaftGen, "Generates mineshafts in villages").getBoolean(EMP_Settings.shaftGen);
        EMP_Settings.oldMineGen = config.get("World Generation", "Enable New Abandoned Mineshafts", EMP_Settings.oldMineGen, "Generates massive abandoned mineshafts (size doesn't gause lag) (This Overrides all Dimensions. Check Custom Dimension properties if you want to set it only for certain Dimensions.)").getBoolean(EMP_Settings.oldMineGen);
        EMP_Settings.gasGen = config.get("World Generation", "Generate Gases", EMP_Settings.gasGen).getBoolean(EMP_Settings.gasGen);

        config.get("Do not Edit", "Ccurrent Config Version", CONFIG_VERSION).getString();

        EMP_Settings.updateCheck = config.get(Configuration.CATEGORY_GENERAL, "Check For Updates", EMP_Settings.updateCheck).getBoolean(EMP_Settings.updateCheck);
        EMP_Settings.noNausea = config.get(Configuration.CATEGORY_GENERAL, "Blindness instead of Nausea", EMP_Settings.noNausea).getBoolean(EMP_Settings.noNausea);
        EMP_Settings.keepStatus = config.get(Configuration.CATEGORY_GENERAL, "Keep statuses on death", EMP_Settings.keepStatus).getBoolean(EMP_Settings.keepStatus);
        EMP_Settings.renderGear = config.get(Configuration.CATEGORY_GENERAL, "Render Gear", EMP_Settings.renderGear ,"Render 3d gear worn on player. Must reload game to take effect").getBoolean(EMP_Settings.renderGear);
        EMP_Settings.finiteWater = config.get(Configuration.CATEGORY_GENERAL, "Finite Water", EMP_Settings.finiteWater).getBoolean(EMP_Settings.finiteWater);

        String PhySetCat = "Physics";

        int minPhysInterval = 6;
        EMP_Settings.spreadIce = config.get(PhySetCat, "Large Ice Cracking", EMP_Settings.spreadIce, "Setting Large Ice Ccracking to true can cause Massive Lag").getBoolean(EMP_Settings.spreadIce);
        EMP_Settings.updateCap = config.get(PhySetCat, "Consecutive Physics Update Cap", EMP_Settings.updateCap, "This will change maximum number of blocks that can be updated with physics at a time. - 1 = Unlimited").getInt(EMP_Settings.updateCap);
        EMP_Settings.physInterval = getConfigIntWithMinInt(config.get(PhySetCat, "Physics Interval", minPhysInterval, "The number of ticks between physics update passes (must be " + minPhysInterval + " or more)"), minPhysInterval);
        EMP_Settings.worldDelay = config.get(PhySetCat, "World Start Delay", EMP_Settings.worldDelay, "How long after world start until the physics system kicks in (DO NOT SET TOO LOW)").getInt(EMP_Settings.worldDelay);
        EMP_Settings.chunkDelay = config.get(PhySetCat, "Chunk Physics Delay", EMP_Settings.chunkDelay, "How long until individual chunk's physics starts after loading (DO NOT SET TOO LOW)").getInt(EMP_Settings.chunkDelay);
        EMP_Settings.physInterval = EMP_Settings.physInterval >= 2 ? EMP_Settings.physInterval : 2;
        EMP_Settings.entityFailsafe = config.get(PhySetCat, "Physics entity fail safe level", EMP_Settings.entityFailsafe, "0 = No action, 1 = Limit to < 100 per 8x8 block area, 2 = Delete excessive entities & Dump physics (EMERGENCY ONLY)").getInt(EMP_Settings.entityFailsafe);

        if (!LegacyHandler.getByKey("ConfigHandlerLegacy").didRun()) {
            //Potion ID's
            EMP_Settings.hypothermiaPotionID = nextAvailPotion(EMP_Settings.hypothermiaPotionID);
            EMP_Settings.heatstrokePotionID = nextAvailPotion(EMP_Settings.heatstrokePotionID);
            EMP_Settings.frostBitePotionID = nextAvailPotion(EMP_Settings.frostBitePotionID);
            EMP_Settings.dehydratePotionID = nextAvailPotion(EMP_Settings.dehydratePotionID);
            EMP_Settings.insanityPotionID = nextAvailPotion(EMP_Settings.insanityPotionID);
        }

        EMP_Settings.hypothermiaPotionID = config.get("Potions", "Hypothermia", EMP_Settings.hypothermiaPotionID).getInt(EMP_Settings.hypothermiaPotionID);
        EMP_Settings.heatstrokePotionID = config.get("Potions", "Heat Stroke", EMP_Settings.heatstrokePotionID).getInt(EMP_Settings.heatstrokePotionID);
        EMP_Settings.frostBitePotionID = config.get("Potions", "Frostbite", EMP_Settings.frostBitePotionID).getInt(EMP_Settings.frostBitePotionID);
        EMP_Settings.dehydratePotionID = config.get("Potions", "Dehydration", EMP_Settings.dehydratePotionID).getInt(EMP_Settings.dehydratePotionID);
        EMP_Settings.insanityPotionID = config.get("Potions", "Insanity", EMP_Settings.insanityPotionID).getInt(EMP_Settings.insanityPotionID);

        EMP_Settings.enableFrostbiteGlobal = config.get("Potions", "Enable Frostbite", EMP_Settings.enableFrostbiteGlobal).getBoolean();;
        EMP_Settings.enableHeatstrokeGlobal = config.get("Potions", "Enable Heat Stroke", EMP_Settings.enableHeatstrokeGlobal).getBoolean();;
        EMP_Settings.enableHypothermiaGlobal = config.get("Potions", "Enable Hypothermia", EMP_Settings.enableHypothermiaGlobal).getBoolean();

        String ConSetCat = "Config";

        EMP_Settings.profileSelected = config.get(ConSetCat, "Profile", EMP_Settings.profileSelected).getString();
        EMP_Settings.profileOverride = config.get(ConSetCat, "Override Profile", EMP_Settings.profileOverride,  "Override Profile. It Can be used for servers to force profiles on servers or modpack. This Overrides any world loaded up. Name is Case Sensitive!").getBoolean(false);
        EMP_Settings.enableConfigOverride = config.get(ConSetCat, "Client Config Override (SMP)", EMP_Settings.enableConfigOverride, "[DISABLED][WIP] Temporarily overrides client configurations with the server's (NETWORK INTESIVE!)").getBoolean(EMP_Settings.enableConfigOverride);

        // Config Gas
        EMP_Settings.noGases = config.get("Gases", "Disable Gases", EMP_Settings.noGases, "Disables all gases and slowly deletes existing pockets").getBoolean(EMP_Settings.noGases);
        EMP_Settings.slowGases = config.get("Gases", "Slow Gases", EMP_Settings.slowGases, "Normal gases will move extremely slowly and reduce TPS lag").getBoolean(EMP_Settings.slowGases);
        EMP_Settings.renderGases = config.get("Gases", "Render normal gas", EMP_Settings.renderGases, "Whether to render gases not normally visible").getBoolean(EMP_Settings.renderGases);
        EMP_Settings.gasTickRate = config.get("Gases", "Gas Tick Rate", EMP_Settings.gasTickRate, "How many ticks between gas updates. Gas fires are 1/4 of this.").getInt(EMP_Settings.gasTickRate);
        EMP_Settings.gasPassLimit = config.get("Gases", "Gas Pass Limit", EMP_Settings.gasPassLimit, "How many gases can be processed in a single pass per chunk (-1 = infinite)").getInt(EMP_Settings.gasPassLimit);
        EMP_Settings.gasWaterLike = config.get("Gases", "Water like spreading", EMP_Settings.gasWaterLike, "Whether gases should spread like water (faster) or even out as much as possible (realistic)").getBoolean(EMP_Settings.gasWaterLike);

        String[] igniteList = config.getStringList("Ignite List", "Gases", ObjectHandler.DefaultIgnitionSources(), "List of Blocks that will ignite flamable gasses.");

        ObjectHandler.LoadIgnitionSources(igniteList);
        config.save();
    }

    private static void loadProfileConfig(File file) {
        Configuration config;
        try {
            config = new Configuration(file, true);
        } catch (Exception e) {
            EnviroMinePort.logger.log(Level.WARN, "Failed to load main configuration file!", e);
            return;
        }

        config.load();

        EMP_Settings.enablePhysics = config.get(Configuration.CATEGORY_GENERAL, "EnablePhysics", EMP_Settings.enablePhysics, "Turn physics On/Off").getBoolean(EMP_Settings.enablePhysics);
        EMP_Settings.enablePhysics = config.get(Configuration.CATEGORY_GENERAL, "Enable Physics", EMP_Settings.enablePhysics, "Turn physics On/Off").getBoolean(EMP_Settings.enablePhysics);
        EMP_Settings.enableLandslide = config.get(Configuration.CATEGORY_GENERAL, "Enable Physics Landslide", EMP_Settings.enableLandslide).getBoolean(EMP_Settings.enableLandslide);
        EMP_Settings.enableSanity = config.get(Configuration.CATEGORY_GENERAL, "Allow Sanity", EMP_Settings.enableSanity).getBoolean(EMP_Settings.enableSanity);
        EMP_Settings.enableHydrate = config.get(Configuration.CATEGORY_GENERAL, "Allow Hydration", EMP_Settings.enableHydrate).getBoolean(EMP_Settings.enableHydrate);
        EMP_Settings.enableBodyTemp = config.get(Configuration.CATEGORY_GENERAL, "Allow Body Temperature", EMP_Settings.enableBodyTemp).getBoolean(EMP_Settings.enableBodyTemp);
        EMP_Settings.enableAirQ = config.get(Configuration.CATEGORY_GENERAL, "Allow Air Quality", EMP_Settings.enableAirQ, "True/False to turn Enviromine Trackers for Sanity, Air Quality, Hydration, and Body Temperature.").getBoolean(EMP_Settings.enableAirQ);
        EMP_Settings.trackNonPlayer = config.get(Configuration.CATEGORY_GENERAL, "Track NonPlayer entities", EMP_Settings.trackNonPlayer, "Track enviromine properties on Non-player entities(mobs & animals)").getBoolean(EMP_Settings.trackNonPlayer);
        EMP_Settings.villageAssist = config.get(Configuration.CATEGORY_GENERAL, "Enable villager assistance", EMP_Settings.villageAssist).getBoolean(EMP_Settings.villageAssist);
        EMP_Settings.foodSpoiling = config.get(Configuration.CATEGORY_GENERAL, "Enable food spoiling", EMP_Settings.foodSpoiling).getBoolean(EMP_Settings.foodSpoiling);
        EMP_Settings.foodRotTime = config.get(Configuration.CATEGORY_GENERAL, "Default spoil time (days)", EMP_Settings.foodRotTime).getInt(EMP_Settings.foodRotTime);
        EMP_Settings.torchesBurn = config.get(Configuration.CATEGORY_GENERAL, "Torches burn", EMP_Settings.torchesBurn).getBoolean(EMP_Settings.torchesBurn);
        EMP_Settings.torchesGoOut = config.get(Configuration.CATEGORY_GENERAL, "Torches go out", EMP_Settings.torchesGoOut).getBoolean(EMP_Settings.torchesGoOut);

        String PhysSetCat = "Physics";
        EMP_Settings.stoneCracks = config.get(PhysSetCat, "Stone Cracks Before Falling", EMP_Settings.stoneCracks).getBoolean(EMP_Settings.stoneCracks);
        EMP_Settings.defaultStability = config.get(PhysSetCat, "Default Stability Type (BlockIDs > 175)", EMP_Settings.defaultStability).getString();

        EMP_Settings.tempMult = config.get("Speed Multipliers", "BodyTemp", EMP_Settings.tempMult).getDouble(EMP_Settings.tempMult);
        EMP_Settings.hydrationMult = config.get("Speed Multipliers", "Hydration", EMP_Settings.hydrationMult).getDouble(EMP_Settings.hydrationMult);
        EMP_Settings.airMult = config.get("Speed Multipliers", "AirQuality", EMP_Settings.airMult).getDouble(EMP_Settings.airMult);
        EMP_Settings.sanityMult = config.get("Speed Multipliers", "Sanity", EMP_Settings.sanityMult).getDouble(EMP_Settings.sanityMult);

        EMP_Settings.tempMult = EMP_Settings.tempMult < 0 ? 0F : EMP_Settings.tempMult;
        EMP_Settings.hydrationMult = EMP_Settings.hydrationMult < 0 ? 0F : EMP_Settings.hydrationMult;
        EMP_Settings.airMult = EMP_Settings.airMult < 0 ? 0F : EMP_Settings.airMult;
        EMP_Settings.sanityMult = EMP_Settings.sanityMult < 0 ? 0F : EMP_Settings.sanityMult;

        String ConSetCat = "Config";
        Property genConfig = config.get(ConSetCat, "Generate Blank Configs", false, "Will attempt to find and generate blank configs for any custom items/blocks/etc loaded before Enviromine. Pack developers are highly encourages to enable this! (Resets back to false after use)");
        if (!EMP_Settings.genConfigs) {
            EMP_Settings.genConfigs = genConfig.getBoolean(false);
        }
        genConfig.set(false);

        Property genDefault = config.get(ConSetCat, "Generate Defaults", true, "Generates EMPs initial default files");
        if (!EMP_Settings.genDefaults) {
            EMP_Settings.genDefaults = genDefault.getBoolean(true);
        }
        genDefault.set(false);

        EMP_Settings.enableConfigOverride = config.get(ConSetCat, "Client Config Override (SMP)", EMP_Settings.enableConfigOverride, "[DISABLED][WIP] Temporarily override client configurations with the server's (NETWORK INTENSIVE!)").getBoolean(EMP_Settings.enableConfigOverride);

        String EarSetCat = "Earthquakes";
        EMP_Settings.enableQuakes = config.get(EarSetCat, "Enable Earthquakes", EMP_Settings.enableQuakes).getBoolean(EMP_Settings.enableQuakes);
        EMP_Settings.quakePhysics = config.get(EarSetCat, "Triggers Physics", EMP_Settings.quakePhysics, "Can cause major lag at times (Requires main physics to be enabled)").getBoolean(EMP_Settings.quakePhysics);
        EMP_Settings.quakeRarity = config.get(EarSetCat, "Rarity", EMP_Settings.quakeRarity).getInt(EMP_Settings.quakeRarity);
        if(EMP_Settings.quakeRarity < 0) {
            EMP_Settings.quakeRarity = 0;
        }

        String eggCat = "Easter Eggs";
        EMP_Settings.thingChance = config.getFloat("Cave Dimension Grue", eggCat, 0.000001F, 0F, 1F, "Chance the (extremely rare) grue in the cave dimension will attack in the dark (ignored on Halloween or Friday 13th)");

        config.save();
    }

    @Deprecated
    private static int getConfigIntWithMinInt(Property prop, int min) {
        if (prop.getInt(min) >= min) {
            return prop.getInt(min);
        } else {
            prop.set(min);
            return min;
        }
    }

    static int nextAvailPotion(int startID) {
        int totalPotions = 0;

        for (Potion potion : Potion.REGISTRY) {
            totalPotions++;
        }

        for (int i = startID; i > 0; i++) {
            if (i == EMP_Settings.hypothermiaPotionID || i == EMP_Settings.heatstrokePotionID || i == EMP_Settings.frostBitePotionID || i == EMP_Settings.dehydratePotionID || i == EMP_Settings.insanityPotionID) {
                continue;
            } else if (i >= totalPotions) {
                return i;
            } else if (Potion.getPotionById(i) == null) {
                return i;
            }
        }

        return startID;
    }

    private static File[] GetFileList(String path) {
        File f = new File(path);
        File[] list = f.listFiles();
        list = list != null ? list : new File[0];

        return list;
    }

    private static boolean isCFGFile(File file) {
        String fileName = file.getName();

        if (file.isHidden()) {
            return false;
        }

        String patternString = "(.*\\.cfg&)";

        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile(patternString);
        matcher = pattern.matcher(fileName);

        String MacCheck = ".DS_Store.cfg";

        if (matcher.matches() && matcher.group(0).toString().toLowerCase().equals(MacCheck.toLowerCase())) {
            return false;
        }

        return matcher.matches();
    }

    public static void CheckDir(File Dir) {
        boolean dirFlag = false;

        if (Dir.exists()) {
            EnviroMinePort.logger.log(Level.INFO, "Dir already exist: " + Dir.getName());
            return;
        }

        try {
            Dir.setWritable(true);
            dirFlag = Dir.mkdirs();
            EnviroMinePort.logger.log(Level.INFO, "Created new Folder " + Dir.getName());
        } catch (Exception e) {
            EnviroMinePort.logger.log(Level.ERROR, "Error occured while creating config directory: " + Dir.getAbsolutePath(), e);
        }

        if (!dirFlag) {
            EnviroMinePort.logger.log(Level.ERROR, "Failed to create config directory: " + Dir.getAbsolutePath());
        }
    }

    private static void LoadCustomObjects(File customFiles) {
        boolean datFile = isCFGFile(customFiles);

        if (datFile) {
            Configuration config;
            try {
                config = new Configuration(customFiles, true);

                config.load();

                List<String> category = new ArrayList<String>();
                Set<String> nameList = config.getCategoryNames();
                Iterator<String> nameListData = nameList.iterator();

                while (nameListData.hasNext()) {
                    category.add(nameListData.next());
                }

                for (int x = 0; x < category.size(); x++) {
                    String CurCat = category.get(x);

                    if (!CurCat.isEmpty() && CurCat.contains(Configuration.CATEGORY_SPLITTER)) {
                        String parent = CurCat.split("\\" + Configuration.Category_SPLITTER)[0];

                        if (propTypes.containsKey(parent) && propTypes.get(parent).useCustomConfigs()) {
                            PropertyBase property = propTypes.get(parent);
                            property.LoadProperty(config, category.get(x));
                        } else {
                            EnviroMinePort.logger.log(Level.WARN, "Failed to laod object " + CurCat);
                        }
                    }
                }

                config.save();

                loadedConfigs.add(config.getConfigFile().getName());
            } catch (Exception e) {
                e.printStackTrace();
                EnviroMinePort.logger.log(Level.ERROR, "FAILED TO LOAD CUSTOM CONFIG: " + customFiles.getName() + "\nNEW SETTINGS WILL BE IGNORED!", e);
            }
        }
    }

    public static ArrayList<String> getSubCategories(Configuration config, String mainCat) {
        ArrayList<String> category = new ArrayList<String>();
        Set<String> nameList = config.getCategoryNames();
        Iterator<String> nameListData = nameList.iterator();

        while (nameListData.hasNext()) {
            String catName = nameListData.next();

            if (catName.startsWith(mainCat + ".")) {
                category.add(catName);
            }
        }

        return category;
    }

    public static String getProfileName() {
        return getProfileName(loadedProfile);
    }

    public static String getProfileName(String profile) {
        return profile.substring(profilePath.length(), profile.length()-1).toUpperCase();
    }

    public static boolean ReloadConfig() {
        try {
            EMP_Settings.armorProperties.clear();
            EMP_Settings.blockProperties.clear();
            EMP_Settings.itemProperties.clear();
            EMP_Settings.livingProperties.clear();
            EMP_Settings.stabilityTypes.clear();
            EMP_Settings.biomeProperties.clear();
            EMP_Settings.dimensionProperties.clear();
            EMP_Settings.rotProperties.clear();
            EMP_Settings.caveGenProperties.clear();
            EMP_Settings.caveSpawnProperties.clear();

            int Total = initConfig();

            initProfile();

            EnviroMinePort.caves.RefreshSpawnList();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static void loadDefaultProperties() {
        Iterator<PropertyBase> iterator = propTypes.values().iterator();

        while(iterator.hasNext()) {
            iterator.next().GenDefaults();
        }
    }

    public static Configuration getConfigFromObj(Object obj) {
        String ModID = ModIdentification.idFromObject(obj);

        File configFile = new File(loadedProfile + customPath + ModID + ".cfg");

        Configuration config;
        try {
            config = new Configuration(configFile, true);
        } catch (NullPointerException e) {
            e.printStackTrace();
            EnviroMinePort.logger.log(Level.WARN, "FAILED TO LOAD Config from OBJECT TO " + ModID + ".CFG");
            return null;
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            EnviroMinePort.logger.log(Level.WARN, "FAILED TO LOAD Config from OBJECT TO " + ModID + ".CFG");
            return null;
        }

        return config;
    }

    public static String SaveMyCustom(Object obj) {
        return SaveMyCustom(obj, null);
    }

    public static String SaveMyCustom(Object obj, Object type) {
        String ModID = ModIdentification.idFrommObject(obj);

        File configFile = new File(loadedProfile + customPath + ModID + ".cfg");

        Configuration config;
        try {
            config = new Configuration(configFile, true);
        } catch (NullPointerException e) {
            e.printStackTrace();
            EnviroMinePort.logger.log(Level.WARN, "FAILED TO SAVE NEW OBJECT TO " + ModID + ".CFG");
            return "Failed to Open " + ModID + ".cfg";
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            EnviroMinePort.logger.log(Level.WARN, "FAILED TO SAVE NEW OBJECT TO " + ModID + ".CFG");
            return "Failed to Open " + ModID + ".cfg";
        }

        config.load();

        String returnValue = "";

        if (obj instanceof Block) {
            BlockProperties.base.generateEmpty(config,obj);
            returnValue = "(Block) Saved to " + ModID + ".cfg on Profile " + getProfileName();
        } else if(obj instanceof Entity) {
            Entity ent = (Entity) obj;
            int id = 0;
            if (ent.getEntityId() > 0) {
                id = ent.getEntityId();
            } else if (EntityRegistry.instance().lookupModSpawn(ent.getClass(), false) != null) {
                id = EntityRegistry.instance().lookupModSpawn(ent.getClass(), false).getModEntityId() + 128;
            } else {
                returnValue = "Failed to add config entry. " + ent.getName() + " has no ID!";
                EnviroMinePort.logger.log(Level.WARN, "Failed to add config entry. " + ent.getName() + " has no ID!");
            }
            EntityProperties.base.generateEmpty(config, id);
            returrnValue = "(Entity) Saved to " + ModID + ".cfg on Profile " + getProfileName();
        } else if (obj instanceof Item && type == null) {
            ItemProperties.base.generateEmpty(config, obj);
            returnValue = "(Item) Saved to " + ModID + ".cfg on Profile " + getProfileName();
        } else if (obj instanceof ItemArmor && type instanceof ArmorProperties) {
            ArmorProperties.base.generateEmpty(config, obj);
            returnValue = "(ItemArmor) Saved to " + ModID + ".cfg on Profile " + getProfileName();
        }

        config.save();

        return returnValue;
    }

    private void removeProperty(Configuration config, String oldCat, String propName) {
        String remove = "Remove";
        config.moveProperty(oldCat, propName, remove);
        config.removeCategory(config.getCategory(remove));
    }
}
