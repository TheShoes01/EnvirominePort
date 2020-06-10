package envirominePort.trackers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Hallucination {
    public EntityLiving falseEntity;
    public EntityPlayer overriding;
    public String falseSound;
    public int x;
    public int y;
    public int z;
    public int time;
    public static int maxTime = 60;
    private Type type = Type.NORMAL;
    public static ArrayList<Hallucination> list = new ArrayList<Hallucination>();
    public static HashMap<String, Hallucination> players = new HashMap<String, Hallucination>();

    @SuppressWarnings("unchecked")
    public Hallucination(EntityLivingBase entityLiving) {
        if (!(entityLiving instanceof EntityPlayer)) {
            return;
        }

        Random rand = entityLiving.getRNG();

        if (rand.nextInt(10) == 0 || true) {
            this.overriding = this.findPlayer(entityLiving);
            if (this.overriding != null) {
                this.type = Type.OVERRIDE;
            }
        }

        x = (int)(this.type == Type.NORMAL ? (entityLiving.posX + rand.nextInt(20) - 10) : this.overriding.posX);
        y = (int)(this.type == Type.NORMAL ? (entityLiving.posY + rand.nextInt(2) - 1) : this.overriding.posY);
        z = (int)(this.type == Type.NORMAL ? (entityLiving.posZ + rand.nextInt(20) - 10) : this.overriding.posZ);

        BlockPos testBlock = new BlockPos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));

        Biome biome = entityLiving.world.getBiomeForCoordsBody(testBlock);

        ArrayList<SpawnListEntry> spawnList = (ArrayList<SpawnListEntry>)biome.getSpawnableList(EnumCreatureType.MONSTER);

        if (spawnList.size() <= 0) {
            return;
        }

        int spawnIndex = entityLiving.getRNG().nextInt(spawnList.size());

        try {
            falseEntity = (EntityLiving)spawnList.get(spawnIndex).entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] {entityLiving.world});
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        }

        if(falseEntity == null) {
            return;
        }

        falseEntity.setPositionAndRotation(x, y, z, rand.nextFloat() * 360F, 0.0F);

        if (!isAtValidSpawn(falseEntity)) {
            return;
        } else if (!entityLiving.world.spawnEntity(falseEntity)) {
            return;
        }
        list.add(this);
        if (this.type == Type.OVERRIDE) {
            players.put(entityLiving.getName(), this);
        }

        if (falseEntity.world.isRemote && falseEntity instanceof EntityLiving) {
            falseEntity.getEntityData().setBoolean("EMP_Hallucination", true);
            ((EntityLiving)falseEntity).playLivingSound();
        }
    }

    private EntityPlayer findPlayer(EntityLivingBase entity) {
        List<EntityPlayer> players = getPlayerList(entity);
        if (players.size() > 0) {
            return players.get(entity.getRNG().nextInt(players.size()));
        } else {
            return null;
        }
    }

    private List<EntityPlayer> getPlayerList(EntityLivingBase entity) {
        List<EntityPlayer> players = new ArrayList<EntityPlayer>();
        @SuppressWarnings("unchecked")
                Iterator<Entity> ite = entity.world.loadedEntityList.iterator();

        while (ite.hasNext()) {
            Entity e = (Entity)ite.next();
            if (e instanceof EntityPlayer && !e.getName().equals(entity.getName()) && !isPlayerSeenWrong(e.getName())) {
                players.add((EntityPlayer)e);
            }
        }

        return players;
    }

    public void doOverride() {
        if (this.type == Type.OVERRIDE) {
            this.falseEntity.setPositionAndRotation(this.overriding.posX, this.overriding.posY, this.overriding.posZ, this.overriding.rotationYaw, this.overriding.rotationPitch);
        }
    }

    public void finish() {
        switch (this.type) {
            case OVERRIDE:
                players.remove(this.overriding.getName());
            case NORMAL:
                this.falseEntity.setDead();
                list.remove(this);
        }
    }

    public static void update() {
        if (list.size() >= 1) {
            for (int i = list.size()-1; i >= 0; i--) {
                Hallucination subject = list.get(i);
                if (subject.time++ >= maxTime) {
                    subject.finish();
                }
            }
        }
    }

    public static boolean isAtValidSpawn(EntityLivingBase creature) {
        return creature.world.checkNoEntityCollision(creature.getEntityBoundingBox()) && creature.world.getCollisionBoxes(creature, creature.getEntityBoundingBox()).isEmpty() && !creature.world.containsAnyLiquid(creature.getEntityBoundingBox()) && isValidLightLevel(creature);
    }

    protected static boolean isValidLightLevel(EntityLivingBase creature) {
        if (creature instanceof EntityBlaze || creature instanceof EntityGhast) {
            return true;
        }

        int i = MathHelper.floor(creature.posX);
        int j = MathHelper.floor(creature.getEntityBoundingBox().minY);
        int k = MathHelper.floor(creature.posZ);

        BlockPos test = new BlockPos(i,j,k);

        if (creature.world.getLightFor(EnumSkyBlock.SKY, test) > creature.getRNG().nextInt(32) && creature.world.isDaytime() && !creature.world.isThundering()) {
            return false;
        } else {
            int l = creature.world.getLightFor(EnumSkyBlock.SKY, test);
            return l <= creature.getRNG().nextInt(8);
        }
    }

    public static boolean isPlayerSeenWrong(String username) {
        return players.containsKey(username);
    }

    public static void renderOverride(EntityPlayer player) {
        Hallucination hal = players.get(player.getName());
        hal.doOverride();
    }

    public static enum Type {
        NORMAL,
        OVERRIDE
    }
}
