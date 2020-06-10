/*package envirominePort.trackers.properties;

import java.io.File;
import java.util.Iterator;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraft.util.registry.RegistryNamespaced;

import org.apache.logging.log4j.Level;

import envirominePort.core.EMP_ConfigHandler;
import envirominePort.core.EMP_Settings;
import envirominePort.core.EnviroMindPort;
import envirominePort.trackers.properties.helpers.PropertyBase;
import envirominePort.trackers.properties.helpers.SerialisableProperty;
import envirominePort.utils.EnviroUtils;

public class ArmorProperties implements SerialisableProperty, PropertyBase {
    public static final ArmorProperties base = new ArmorProperties();
    static String[] APName;

    public Item item;
    public String name;
    public float nightTemp;
    public float shadeTemp;
    public float sunTemp;
    public float nightMult;
    public float shadeMult;
    public float sunMult;
    public float sanity;
    public float air;
    public boolean allowCamelPack;
    public String loadedFrom;

    public ArmorProperties(NBTTagCompound tags) {
        this.ReadFromNBT(tags);
    }

    public ArmorProperties() {
        if (base != null && base != this) {
            throw new IllegalStateException();
        }
    }

    public ArmorProperties(Item item, String name, float nightTemp, float shadeTemp, float sunTemp, float nightMult, float shadeMult, float sunMult, float sanity, float air, boolean allowCamelPack, String filename) {
        this.item = item;
        this.name = name;
        this.nightTemp = nightTemp;
        this.shadeTemp = shadeTemp;
        this.sunTemp = sunTemp;
        this.nightMult = nightMult;
        this.shadeMult = shadeMult;
        this.sunMult = sunMult;
        this.sanity = sanity;
        this.air = air;
        this.allowCamelPack = allowCamelPack;
        this.loadedFrom = filename;
    }

    public boolean hasProperty(ItemStack stack) {
        return EMP_Settings.armorProperties.containsKey(Item.REGISTRY.getNameForObject(stack.getItem()).toString());
    }

    public ArmorProperties getProperty(ItemStack stack) {
        return EMP_Settings.armorProperties.get(Item.REGISTRY.getNameForObject(stack.getItem()).toString());
    }

    @Override
    public NBTTagCompound WriteToNBT() {
        NBTTagCompound tags = new NBTTagCompound();
        tags.setString("name", Item.REGISTRY.getNameForObject(item).toString());
        tags.setFloat("nightTemp", nightTemp);
        tags.setFloat("shadeTemp", shadeTemp);
        tags.setFloat("sunTemp", sunTemp);
        tags.setFloat("nightMult", nightMult);
        tags.setFloat("shadeMult", shadeMult);
        tags.setFloat("sunMult", sunMult);
        tags.setFloat("sanity", sanity);
        tags.setFloat("air", air);
        tags.setBoolean("allowCamelPack", allowCamelPack);
        return tags;
    }

    @Override
    public void ReadFromNBT(NBTTagCompound tags)
    {
        this.name = tags.getString("name");
        item = (Item)Item.REGISTRY.getObject(new ResourceLocation(this.name));
        this.nightTemp = tags.getFloat("nightTemp");
        this.shadeTemp = tags.getFloat("shadeTemp");
        this.sunTemp = tags.getFloat("sunTemp");
        this.nightMult = tags.getFloat("nightMult");
        this.shadeMult = tags.getFloat("shadeMult");
        this.sunMult = tags.getFloat("sunMult");
        this.sanity = tags.getFloat("sanity");
        this.air = tags.getFloat("air");
        this.allowCamelPack = tags.getBoolean("allowCamelPack");
    }

    @Override
    public String categoryName() {
        return "armor";
    }

    @Override
    public String categoryDescription() {
        return "Modify the effects armor has on entities when worn";
    }

    @Override
    public void LoadProperty(Configuration config, String category) {
        config.setCategoryComment(this.categoryName(), this.categoryDescription());
        String name = config.get(category, APName[0], "").getString();
        float nightTemp = (float)config.get(category, APName[1], 0.00).getDouble(0.00);
        float shadeTemp = (float)config.get(category, APName[2], 0.00).getDouble(0.00);
        float sunTemp = (float)config.get(category, APName[3], 0.00).getDouble(0.00);
        float nightMult = (float)config.get(category, APName[4], 1.00).getDouble(1.00);
        float shadeMult = (float)config.get(category, APName[5], 1.00).getDouble(1.00);
        float sunMult = (float)config.get(category, APName[6], 1.00).getDouble(1.00);
        float sanity = (float)config.get(category, APName[7], 0.00).getDouble(0.00);
        float air = (float)config.get(category, APName[8], 0.00).getDouble(0.00);
        String filename = config.getConfigFile().getName();

        Object item = Item.REGISTRY.getObject(new ResourceLocation(name));
        boolean allowCamelPack = true;
        if (item instanceof ItemArmor && ((ItemArmor)item).armorType.getIndex() == 1) {
            allowCamelPack = config.get(category, APName[9], true).getBoolean(true);
        }

        ArmorProperties entry = new ArmorProperties((Item)item, name, nightTemp, shadeTemp, sunTemp, nightMult, shadeMult, sunMult, sanity, air, allowCamelPack, filename);

        if(EMP_Settings.armorProperties.containsKey(name) && !EMP_ConfigHandler.loadedConfigs.contains(filename)) EnviroMinePort.logger.log(Level.ERROR, "CONFIG DUPLICATE: Armor - "+ name.toUpperCase() +" was already added from "+ EMP_Settings.armorProperties.get(name).loadedFrom.toUpperCase() +" and will be overriden by "+ filename.toUpperCase());

        EMP_Settings.armorProperties.put(name, entry);
    }

    @Override
    public void SaveProperty(Configuration config, String category)
    {
        config.get(category, APName[0], name).getString();
        config.get(category, APName[1], nightTemp).getDouble(nightTemp);
        config.get(category, APName[2], shadeTemp).getDouble(shadeTemp);
        config.get(category, APName[3], sunTemp).getDouble(sunTemp);
        config.get(category, APName[4], nightMult).getDouble(nightMult);
        config.get(category, APName[5], shadeMult).getDouble(shadeMult);
        config.get(category, APName[6], sunMult).getDouble(sunMult);
        config.get(category, APName[7], sanity).getDouble(sanity);
        config.get(category, APName[8], air).getDouble(air);
    }
}
*/