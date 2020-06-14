package envirominePort.handlers;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.apache.logging.log4j.Level;

import envirominePort.EntityPhysicsBlock;
import envirominePort.blocks.BlockBurningCoal;
import envirominePort.blocks.BlockDavyLamp;
import envirominePort.blocks.BlockElevator;
import envirominePort.blocks.BlockEsky;
import envirominePort.blocks.BlockFireTorch;
import envirominePort.blocks.BlockFlammableCoal;
import envirominePort.blocks.BlockFreezer;
import envirominePort.blocks.BlockGas;
import envirominePort.blocks.BlockNoPhysics;
import envirominePort.blocks.materials.MaterialElevator;
import envirominePort.blocks.materials.MatieralGas;
import envirominePort.blocks.tiles.TileEntityBurningCoal;
import envirominePort.blocks.tiles.TileEntityDavyLamp;
import envirominePort.blocks.tiles.TileEntityElevator;
import envirominePort.blocks.tiles.TileEntityEsky;
import envirominePort.blocks.tiles.TileEntityFreezer;
import envirominePort.blocks.tiles.tileEntityGas;
import envirominePort.core.EnviroMinePort;
import envirominePort.items.EnviroArmor;
import envirominePort.items.EnviroItemBadWaterBottle;
import envirominePort.items.EnviroItemColdWaterBottle;
import envirominePort.items.EnviroItemSaltWaterBottle;
import envirominePort.items.ItemDavyLamp;
import envirominePort.items.ItemElevator;
import envirominePort.items.ItemSpoiledMilk;
import envirominePort.items.RottenFood;

public class ObjectHandler {
    public static HashMap<Block, ArrayList<Integer>> igniteList = new HashMap<Block, ArrayList<Integer>>();
    public static ArmorMaterial camelPackMaterial;

    public static Item badWaterBottle;
    public static Item saltWaterBottle;
    public static Item coldWaterBottle;

    public static Item airFilter;
    public static Item davyLamp;
    public static Item gasMeter;
    public static Item rottenFood;
    public static Item spoiledMilk;

    public static ItemArmor camelPack;
    public static ItemArmor gasMask;
    public static ItemArmor hardHat;

    public static Block davyLampBlock;
    public static Block elevator;
    public static Block gasBlock;
    public static Block fireGasBlock;

    public static Block flammableCoal;
    public static Block burningCoal;
    public static Block fireTorch;
    public static Block offTorch;

    public static Block esky;
    public static Block freezer;

    public static Block noPhysBlock;

    public static int renderGasID;
    public static int renderSpecialID;

    public static Material gasMat;
    public static Material elevatorMat;

    public static void initItems() {
        badWaterBottle = new EnviroItemBadWaterBottle().setMaxStackSize(1).setUnlocalizedName("envirominePort.badwater").setCreativeTab(EnviroMinePort.enviroTab);
        saltWaterBottle = new EnviroItemSaltWaterBottle().setMaxStackSize(1).setUnlocalizedName("envirominePort.saltwater").setCreativeTab(EnviroMinePort.enviroTab);
        coldWaterBottle = new EnviroItemColdWaterBottle().setMaxStackSize(1).setUnlocalizedName("envirominePort.coldwater").setCreativeTab(EnviroMinePort.enviroTab);
        airFilter = new Item().setMaxStackSize(16).setUnlocalizedName("envirominePort.airfilter").setCreativeTab(EnviroMinePort.enviroTab).setTextureName("envirominePort:air_filter");
        rottenFood = new RottenFood(1).setMaxStackSize(64).setUnlocalizedName("envirominePort.rottenfood").setCreativeTab(EnviroMinePort.enviroTab).setTextureName("envirominePort:rot");
        spoiledMilk = new ItemSpoiledMilk().setUnlocalizedName("envirominePort.spoiledmilk").setCreativeTab(EnviroMinePort.enviroTab).setTextureName("bucket_milk");

        camelPackMaterial = EnumHelper.addArmorMaterial("camelPack", "envirominePort:camel_pack",100,new int[]{2,2,0,0},0, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,1);

        camelPack = (ItemArmor)new EnviroArmor(camelPackMaterial, 4, 1).setTextureName("camel_pack").setUnlocalizedName("envirominePort.camelPack").setCreativeTab(null);

        gasMask = (ItemArmor)new EnviroArmor(camelPackMaterial, 4, 0).setTextureName("gas_mask").setUnlocalizedName("envirominePort.gasmask").setCreativeTab(null);
        hardHat = (ItemArmor)new EnviroArmor(camelPackMaterial, 4, 0).setTextureName("hard_hat").setUnlocalizedName("envirominePort.hardhat").setCreativeTab(null);
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(badWaterBottle, saltWaterBottle, coldWaterBottle, airFilter, rottenFood, spoiledMilk, camelPack, gasMask, hardHat);

        ItemStack camelStack1 = new ItemStack(camelPack);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("camelPackFill", 0);
        tag.setInteger("camelPackMax", 100);
        tag.setBoolean("isCamelPack", true);
        tag.setString("camelPath", Item.REGISTRY.getNameForObject(camelPack).toString());
        camelStack1.setTagCompound(tag);
        EnviroMinePort.enviroTab.addRawStack(camelStack1);

        ItemStack camelStack2 = new ItemStack(camelPack);
        tag = new NBTTagCompound();
        tag.setInteger("camelPackFill", 100);
        tag.setInteger("camelPackMax", 100);
        tag.setBoolean("isCamelPack", true);
        tag.setString("camelPath", Item.REGISTRY.getNameForObject(camelPack).toString());
        camelStack2.setTagCompound(tag);
        EnviroMinePort.enviroTab.addRawStack(camelStack2);

        ItemStack mask = new ItemStack(gasMask);
        tag = new NBTTagCompound();
        tag.setInteger("gasMaskFill", 1000);
        tag.setInteger("gasMaskMax", 1000);
        mask.setTagCompound(tag);
        EnviroMinePort.enviroTab.addRawStack(mask);
    }

    public static void initBlocks() {
        gasMat = new MaterialGas(MapColor.airColor);
        gasBlock = new BlockGas(gasMat).setBlockName("envirominePort.gas").setCreativeTab(EnviroMinePort.enviroTab).setBlockTextureName("envirominePort:gas_block");
        fireGasBlock = new BlockGas(gasMat).setBlockName("envirominePort.firegas").setCreativeTab(EnviroMinePort.enviroTab).setBlockTextureName("envirominePort:gas_block").setLightLevel(1.0F);

        elevatorMat = new MaterialElevator(MapColor.IRON);
        elevator = new BlockElevator(elevatorMat).setBlockName("envirominePort.elevator").setCreativeTab(EnviroMinePort.enviroTab).setBlockTextureName("iron_block");

        davyLampBlock = new BlockDavyLamp(Material.REDSTONE_LIGHT).setLightLevel(1.0F).setBlockName("envirominePort.davy_lamp").setCreativeTab(EnviroMinePort.enviroTab);
        davyLamp = new ItemDavyLamp(davyLampBlock).setUnlocalizedName("envirominePort.davylamp").setCreativeTab(EnviroMinePort.enviroTab);

        flammableCoal = new BlockFlammableCoal();
        burningCoal = new BlockBurningCoal(Material.ROCK).setBlockName("envirominePort.burningcoal").setCreativeTab(EnviroMinePort.enviroTab);
        fireTorch = new BlockFireTorch(true).setTickRandomly(true).setBlockName("torch").setBlockTextureName("torch_on").setLightLevel(0.9375F).setCreativeTab(EnviroMinePort.enviroTab);
        offTorch = new BlockFireTorch(false).setTickRandomly(false).setBlockName("torch").setBlockTextureName("torch_on").setLightLevel(0F).setCreativeTab(EnviroMinePort.enviroTab);
        esky = new BlockEsky(Material.IRON).setBlockName("envirominePort.esky").setCreativeTab(EnviroMinePort.enviroTab);
        freezer = new BlockFreezer(Material.IRON).setBlockName("envirominePort.freezer").setCreativeTab(EnviroMinePort.enviroTab);

        noPhysBlock = new BlockNoPhysics();

        Blocks.REDSTONE_TORCH.setLightLevel(0.9375F);
    }

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(gasBlock, fireGasBlock, elevator, davyLampBlock, fireTorch, offTorch, burningCoal, flammableCoal, esky, freezer, noPhysBlock);

        Blocks.FIRE.setFireInfo(flammableCoal, 60, 100);

        OreDictionary.registerOre("oreCoal", flammableCoal);
    }

    public static void registerGases() {}

    public static void registerEntities() {
        ResourceLocation rl = new ResourceLocation("EnviroPhysicsBlock");
        EntityRegistry.registerModEntity(rl, EneityPhysicsBlock.class, "EnviroPhysicsEntity");
        GameRegistry.registerTileEntity()
    }
}
