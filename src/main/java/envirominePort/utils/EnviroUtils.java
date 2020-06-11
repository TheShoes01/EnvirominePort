package envirominePort.utils;

import envirominePort.core.EMP_Settings;
import envirominePort.core.EnviroMinePort;
import envirominePort.handlers.ObjectHandler;
import envirominePort.trackers.properties.StabilityType;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.EnumFacing;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import org.apache.logging.log4j.Level;

import static java.math.RoundingMode.HALF_UP;

public class EnviroUtils {
    public static final String[] reservedNames = new String[] {"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
    public static final char[] specialCharacters = new char[] {'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};

    public static void extendPotionList() {
        int maxID = 32;
        int totalPotions = 0;

        for (Potion potion : Potion.REGISTRY) {
            totalPotions++;
        }

        if (EMP_Settings.heatstrokePotionID >= maxID) {
            maxID = EMP_Settings.heatstrokePotionID + 1;
        }

        if (EMP_Settings.hypothermiaPotionID >= maxID) {
            maxID = EMP_Settings.hypothermiaPotionID + 1;
        }

        if (EMP_Settings.frostBitePotionID >= maxID) {
            maxID = EMP_Settings.frostBitePotionID + 1;
        }

        if (EMP_Settings.dehydratePotionID >= maxID) {
            maxID = EMP_Settings.dehydratePotionID + 1;
        }

        if (EMP_Settings.insanityPotionID >= maxID) {
            maxID = EMP_Settings.insanityPotionID + 1;
        }

        if (totalPotions >= maxID) {
            return;
        }

        Potion[] potionTypes = null;

        for(Field f : Potion.class.getDeclaredFields()) {
            f.setAccessible(true);

            try {
                if (f.getName().equals("potionTypes") || f.getName().equals("field_76425_a")) {
                    Field modfield = Field.class.getDeclaredField("modifiers");
                    modfield.setAccessible(true);
                    modfield.setInt(f, f.getModifiers() & ~Modifier.FINAL);

                    potionTypes = (Potion[])f.get(null);
                    final Potion[] newPotionTypes = new Potion[maxID];
                    System.arraycopy(potionTypes, 0, newPotionTypes, 0, potionTypes.length);
                    f.set(null, newPotionTypes);
                }
            } catch (Exception e) {
                EnviroMinePort.logger.log(Level.ERROR, "Failed to extend potion list for EMP!", e);
            }
        }
    }

    public static int[] getAdjacentBlockCoordsFromSide(int x, int y, int z, int side) {
        int[] coords = {x, y, z};

        EnumFacing dir = EnumFacing.getFront(side);
        switch(dir) {
            case NORTH:
                coords[2] -= 1;
                break;
            case SOUTH:
                coords[2] += 1;
                break;
            case WEST:
                coords[0] -= 1;
                break;
            case EAST:
                coords[0] += 1;
                break;
            case UP:
                coords[1] += 1;
                break;
            case DOWN:
                coords[1] -= 1;
                break;
            default:
                break;
        }

        return coords;
    }

    public static String replaceULN(String unlocalizedName) {
        unlocalizedName = unlocalizedName.replaceAll("[\\(\\)]","");
        unlocalizedName = unlocalizedName.replaceAll("\\.+","\\_");

        return unlocalizedName;
    }

    public static float convertToFarenheit(float num) {
        return convertToFarenheit(num, 2);
    }

    public static float convertToFarenheit(float num, int decimalPlace) {
        float newNum = (float) ((num * 1.8) + 32F);
        BigDecimal convert = new BigDecimal(Float.toString(newNum));
        convert = convert.setScale(decimalPlace, HALF_UP);

        return convert.floatValue();
    }

    public static float convertToCelcius(float num) {
        return ((num - 32F) * (5F / 9F));
    }

    public static double getBiomeTemp(Biome biome) {
        return getBiomeTemp(biome.getDefaultTemperature());
    }

    public static double getBiomeTemp (int x, int y, int z, Biome biome) {
        return getBiomeTemp(biome.getTemperature(new BlockPos(x,y,z)));
    }

    public static double getBiomeTemp(float biomeTemp) {
        float maxTemp = 45F;
        float minTemp = -15F;

        double temp = Math.sin(Math.toRadians(biomeTemp * 45F));
        if (biomeTemp >= 0) {
            return temp *maxTemp;
        } else {
            return temp *minTemp;
        }
    }

    public static String getBiomeWater(Biome biome) {
        int waterColor = biome.getWaterColorMultiplier();
        boolean looksBad = false;

        if (waterColor != 16777215) {
            Color bColor = new Color(waterColor);

            if (bColor.getRed() < 200 || bColor.getGreen() < 200 || bColor.getBlue() < 200) {
                looksBad = true;
            }
        }

        Type[] typeArray = (Type[]) BiomeDictionary.getTypes(biome).toArray();
        ArrayList<Type> typeList = new ArrayList<Type>(Arrays.asList(typeArray));

        if (typeList.contains(Type.SWAMP) || typeList.contains(Type.JUNGLE) || typeList.contains(Type.DEAD) || typeList.contains(Type.WASTELAND) || looksBad) {
            return "dirty";
        } else if (typeList.contains(Type.OCEAN) || typeList.contains(Type.BEACH)) {
            return "salty";
        } else if (typeList.contains(Type.SNOWY) || typeList.contains(Type.CONIFEROUS) || biome.getDefaultTemperature() < 0F) {
            return "cold";
        } else {
            return "clean";
        }
    }

    public static StabilityType getDefaultStabilityType(Block block) {
        StabilityType type = null;

        Material material = block.getBlockState().getBaseState().getMaterial();

        if (block instanceof BlockMobSpawner || block instanceof BlockLadder || block instanceof BlockWeb || block instanceof BlockSign || block instanceof BlockBed || block instanceof BlockDoor || block instanceof BlockAnvil || block instanceof BlockGravel || block instanceof BlockPortal || block instanceof BlockEndPortal || block instanceof BlockEndPortalFrame || block.equals(ObjectHandler.elevator) || block.equals(Blocks.END_STONE) || !material.blocksMovement()) {
            type = EMP_Settings.stabilityTypes.get("none");
        } else if (block instanceof BlockGlowstone) {
            type = EMP_Settings.stabilityTypes.get("glowstone");
        } else if (block instanceof BlockFalling) {
            type = EMP_Settings.stabilityTypes.get("sand-like");
        } else if (material.equals(Material.IRON) || material.equals(Material.WOOD) || block instanceof BlockObsidian || block.equals(Blocks.STONEBRICK) || block.equals(Blocks.BRICK_BLOCK) || block.equals(Blocks.QUARTZ_BLOCK)) {
            type = EMP_Settings.stabilityTypes.get("strong");
        } else if (material.equals(Material.ROCK) || material.equals(Material.GLASS) || material.equals(Material.ICE) || block instanceof BlockLeaves) {
            type = EMP_Settings.stabilityTypes.get("average");
        } else {
            type = EMP_Settings.stabilityTypes.get(EMP_Settings.defaultStability);
        }

        if (type == null) {
            EnviroMinePort.logger.log(Level.ERROR, "Block " + block.getUnlocalizedName() + " has a null StabilityType. Crash imminent!");
        }

        return type;
    }

    public static String SafeFilename(String filename) {
        String safeName = filename;
        for (String reserved : reservedNames) {
            if (safeName.equalsIgnoreCase(reserved)) {
                safeName = "_" + safeName + "_";
            }
        }

        for (char badChar : specialCharacters) {
            safeName = safeName.replace(badChar, '_');
        }

        return safeName;
    }

    public static int compareVersions(String oldVer, String newVer) {
        if (oldVer == null || newVer == null || oldVer.isEmpty() || newVer.isEmpty()) {
            return -2;
        }

        int result = 0;
        int[] oldNum;
        int[] newNum;
        String[] oldNumStr;
        String[] newNumStr;

        try {
            oldNumStr = oldVer.split("\\.");
            newNumStr = newVer.split("\\.");

            oldNum = new int[]{Integer.parseInt(oldNumStr[0]),Integer.parseInt(oldNumStr[1]),Integer.parseInt(oldNumStr[2])};
            newNum = new int[]{Integer.parseInt(newNumStr[0]),Integer.parseInt(newNumStr[1]),Integer.parseInt(newNumStr[2])};
        } catch (IndexOutOfBoundsException e) {
            EnviroMinePort.logger.log(Level.WARN, "An IndexOutOfBoundsException occurred while checking version!", e);
            return -2;
        } catch (NumberFormatException e) {
            EnviroMinePort.logger.log(Level.WARN, "A NumberFormatException occured while checking version!\n", e);
            return -2;
        }

        for (int i = 0; i < 3; i++) {
            if (oldNum[i] < newNum[i]) {
                return -1;
            } else if (oldNum[i] > newNum[i]) {
                return 1;
            }
        }

        return result;
    }
}
