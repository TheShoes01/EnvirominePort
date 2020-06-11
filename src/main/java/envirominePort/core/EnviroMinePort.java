package envirominePort.core;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import envirominePort.EnviroPotion;
import envirominePort.core.commands.CommandPhysics;
import envirominePort.core.commands.EnviroCommand;
import envirominePort.core.commands.QuakeCommand;
import envirominePort.core.proxies.EMP_CommonProxy;
import envirominePort.handlers.EnviroAchievements;
import envirominePort.handlers.EnvrioShaftCreationHandler;
//import envirominePort.handlers.ObjectHandler;
import envirominePort.handlers.Legacy.LegacyHandler;
import envirominePort.network.packet.PacketAutoOverride;
import envirominePort.network.packet.PacketEnviroMinePort;
import envirominePort.network.packet.PacketServerOverride;
import envirominePort.utils.EnviroUtils;
import envirominePort.world.EMP_WorldData;
import envirominePort.world.WorldProviderCaves;
import envirominePort.world.biomes.BiomeGenCaves;
import envirominePort.world.features.WorldFeatureGenerator;
import envirominePort.world.features.mineshaft.EMP_VillageMineshaft;

@Mod(modid = EMP_Settings.ModID, name = EMP_Settings.Name, version = EMP_Settings.Version, guiFactory = "envirominePort.client.gui.menu.config.EnviroMinePortGuiFactory")
public class EnviroMinePort {
    public static Logger logger;
    public static BiomeGenCcaves caves;
    public static EviroTab enviroTab;

    @Instance(EMP_Settings.ModID)
    public static EnviroMinePort instance;

    @SidedProxy(clientSide = EMP_Settings.Proxy + ".EMP_ClientProxy", serverSide = EMP_Settings.Proxy + ".EMP_CommonProxy")
    public static EMP_CommonProxy proxy;

    public SimpleNetworkWrapper network;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        enviroTab = new EnviroTab("envirominePort.enviroTab");

        LegacyHandler.preInit();
        LegacyHandler.init();

        proxy.preInit(event);

        //ObjectHandler.initItems();
        //ObjectHandler.registerItems();
        //ObjectHandler.initBlocks();
        //ObjectHandler.registerBlocks();

        EMP_ConfigHandler.initConfig();

        //ObjectHandler.registerGases();
        //ObjectHandler.registereEntities();

        if (EMP_Settings.shaftGen == true) {
            VillagerRegistry.instance().registerVillageCreationHandler(new EnviroShaftCreationHandler());
            MapGenStructureIO.registerStructure(EMP_VillageMineshaft.class, "ViMS");
        }

        this.network = NetworkRegistry.INSTANCE.newSimpleChannel(EMP_Settings.Channel);
        this.network.registerMessage(PacketEnvrioMinePort.HandlerServer.class, PacketEnvrioMinePort.class, 0, Side.SERVER);
        this.network.registerMessage(PacketEnviroMinePort.HandlerClient.class, PacketEnvrioMinePort.class, 1, Side.CLIENT);
        this.network.registerMessage(PacketAutoOverride.Handler.class, PacketAutoOverride.class, 2, Side.CLIENT);
        this.network.registerMessage(PacketServerOverride.Handler.class, PacketeServerOverride.class, 3, Side.CLIENT);

        GameRegistry.registerWorldGenerator(new WorldFeatureGenerator(), 20);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        //ObjectHandler.registerRecipes();

        EnviroUtils.extendPotionList();

        EnviroPotion.RegisterPotions;

        EnviroAchievements.InitAchievements();

        caves = (BiomeGenCaves)(new BiomeGenCaves(EMP_Settings.caveBiomeID).setColor(0).setBiomeName("Caves").setDisableRain().setTemperatureRainfall(1.0F, 0.0F));

        BiomeDictionary.addTypes(caves, Type.WASTELAND);

        DimensionType caves = DimensionType.register("CaveDim", "idkDude", EMP_Settings.caveDimID, WorldProviderCaves.class, false);
        DimensionManager.registerDimension(EMP_Settings.caveDimID, caves);

        proxy.registerTickHandlers();
        proxy.registerEventHandlers();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);

        EMP_ConfigHandler.initConfig();
    }

    public void serverStart(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        ICommandManager command = server.getCommandManager();

        ServerCommandManager manager = (ServerCommandManager) command;

        manager.registerCommand(new CommandPhysics());
        manager.registerCommand(new EnviroCommand());
        manager.registerCommand(new QuakeCommand());
    }
}
