package envirominePort.trackers;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.google.common.collect.Iterators;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import envirominePort.EnviroDamageSource;
import envirominePort.EnviroPotion;
import envirominePort.client.gui.UI_Settings;
import envirominePort.core.EMP_Settings;
import envirominePort.core.EnviroMine;
import envirominePort.handlers.EMP_StatusManager;
import envirominePort.trackers.properties.DimensionProperties;
import envirominePort.trackers.properties.EntityProperties;

public class EnviroDataTracker {
    public EntityLivingBase trackedEntity;

    public float prevBodyTemp = 37F;
    public float prevHydration = 100F;
    public float prevAirQuality = 100;
    public float prevSanity = 100F;

    public float gasAirDiff = 0F;

    public float airQuality;

    public float bodyTemp;
    public float airTemp;

    public float hydration;

    public float sanity;

    public int attackDelay = 1;
    public int curAttackTime = 0;
    public boolean isDisabled = false;

    public int frostbiteLevel = 0;
    public boolean frostIrreversible = false;

    public boolean brokenLeg = false;
    public boolean brokenArm = false;
    public boolean bleedingOut = false;

    public String sleepState = "Awake";
    public int lastSleepTime = 0;

    public int timeBelow10 = 0;

    public int updateTimer = 0;

    private Side side = FMLCommonHandler.instance().getSide();

    //Sound Time
    public long chillPrevTime = 0;
    public long sizzlePrevTime = 0;

    public EnviroDataTracker(EntityLivingBase entity) {
        trackedEntity = entity;
        airQuality = 100F;
        bodyTemp = 37F;
        hydration = 100F;
        sanity = 100F;
    }

    public void updateData() {
        prevBodyTemp = bodyTemp;
        prevAirQuality = airQuality;
        prevHydration = hydration;
        prevSanity = sanity;

        updateTimer = 0;

        if (trackedEntity == null || isDisabled) {
            EMP_StatusManager.removeTracker(this);
            return;
        }

        if (trackedEntity.isDead) {
            return;
        }

        if (!(trackedEntity instanceof EntityPlayer) && !EMP_Settings.trackNonPlayer || (!EMP_Settings.enableAirQ && !EMP_Settings.enableBodyTemp && !EMP_Settings.enableHydrate && !EMP_Settings.enableSanity)) {
            EMP_StatusManager.saveAndRemoveTracker(this);
            return;
        }

        int i = MathHelper.floor(trackedEntity.posX);
        int j = MathHelper.floor(trackedEntity.posY);
        int k = MathHelper.floor(trackedEntity.posZ);

        BlockPos temp = new BlockPos(i, j, k);

        if (!trackedEntity.world.getChunkFromBlockCoords(temp).isLoaded()) {
            return;
        }

        float[] enviroData = EMP_StatusManager.getSurroundingData(trackedEntity, 5);
        boolean isCreative = false;

        if (trackedEntity instanceof EntityPlayer) {
            if (((EntityPlayer)trackedEntity).capabilities.isCreativeMode) {
                isCreative = true;
            }
        }

        if ((trackedEntity.getHealth() <= 2F || bodyTemp >= 41F) && enviroData[7] > (float)(-1F * EMP_Settings.sanityMult)) {
            enviroData[7] = (float)(-1F * EMP_Settings.sanityMult);
        } else if (trackedEntity.getHealth() >= trackedEntity.getMaxHealth() && enviroData[7] < (0.1F * EMP_Settings.sanityMult) && trackedEntity.world.isDaytime() && !trackedEntity.world.provider.hasSkyLight() && trackedEntity.world.canBlockSeeSky(temp)) {
            enviroData[7] = (float)(0.1F * EMP_Settings.sanityMult);
        }

        enviroData[0] += gasAirDiff;
        gasAirDiff = 0F;
        airQuality += enviroData[0];

        ItemStack helmet = trackedEntity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (helmet != null && !isCreative) {
            if (helmet.hasTagCompound() && helmet.getTagCompound().hasKey("gasMaskFill")) {
                NBTTagCompound tag = helmet.getTagCompound();
                int gasMaskFill = tag.getInteger("gasMaskFill");

                if(gasMaskFill > 0 && airQuality <= 99F) {
                    int airDrop = 100 - MathHelper.ceil(airQuality);
                    airDrop = Math.min(gasMaskFill, airDrop);

                    if (airDrop > 0) {
                        airQuality += airDrop;
                        tag.setInteger("gasMaskFill", (gasMaskFill - airDrop));
                    }
                }
            }
        }

        if (airQuality <= 0F) {
            airQuality = 0;
        }

        if (airQuality >= 100F) {
            airQuality = 100F;
        }

        airTemp = enviroData[1];
        float tnm = enviroData[4];
        float tpm = enviroData[5];

        float relTemp = airTemp + 12;

        if (bodyTemp - relTemp > 0) {
            float spAmp = Math.abs(bodyTemp - relTemp) > 10F ? Math.abs(bodyTemp - relTemp)/10F : 1F;
            if (bodyTemp - relTemp >= tnm * spAmp) {
                bodyTemp -= tnm * spAmp;
            } else {
                bodyTemp = relTemp;
            }
        } else if (bodyTemp - relTemp < 0) {
            float spAmp = Math.abs(bodyTemp - relTemp) > 10F ? Math.abs(bodyTemp - relTemp)/10F : 1F;
            if (bodyTemp - relTemp <= -tpm * spAmp) {
                bodyTemp += tpm * spAmp;
            } else {
                bodyTemp = relTemp;
            }
        }

        if (hydration > 0F && (enviroData[6] == 1 || !(trackedEntity instanceof EntityAnimal))) {
            if (bodyTemp >= 38.02F) {
                dehydrate(0.1F);

                if (hydration >= 75F) {
                    bodyTemp -= 0.01F;
                }
            }

            if (enviroData[3] > 0F) {
                dehydrate(0.05F + enviroData[3]);
            } else {
                if (enviroData[3] < 0F) {
                    hydrate(-enviroData[3]);
                }
                dehydrate(0.05F);
            }
        } else if (enviroData[6] == -1 && trackedEntity instanceof EntityAnimal) {
            hydrate(0.05F);
        } else if (hydration <= 0F) {
            hydration = 0;
        }

        if (sanity <= 0F) {
            sanity = 0F;
        }

        if (enviroData[7] < 0F) {
            if (sanity + enviroData[7] >= 0F) {
                sanity += enviroData[7];
            } else {
                sanity = 0F;
            }
        } else if (enviroData[7] > 0F) {
            if (sanity + enviroData[7] <= 100F) {
                sanity += enviroData[7];
            } else {
                sanity = 100F;
            }
        }

        boolean enableAirQ = true;
        boolean enableBodyTemp = true;
        boolean enableHydrate = true;
        boolean enableFrostbite = true;
        boolean enableHeat = true;
        int id = 0;

        if (EntityProperties.base.hasProperty(trackedEntity)) {
            EntityProperties livingProps = EntityProperties.base.getProperty(trackedEntity);
            enableHydrate = livingProps.dehydration;
            enableBodyTemp = livingProps.bodyTemp;
            enableAirQ = livingProps.airQ;
            enableFrostbite = !livingProps.immuneToFrost;
            enableHeat = !livingProps.immuneToHeat;
        } else if ((trackedEntity instanceof EntitySheep) || (trackedEntity instanceof EntityWolf)) {
            enableFrostbite = false;
        } else if (trackedEntity instanceof EntityChicken) {
            enableHeat = false;
        }

        if (!EMP_Settings.enableAirQ || !enableAirQ) {
            airQuality = 100F;
        }
        if(!EMP_Settings.enableBodyTemp || !enableBodyTemp)
        {
            bodyTemp = 37F;
        }
        if(!EMP_Settings.enableHydrate || !enableHydrate)
        {
            hydration = 100F;
        }
        if(!EMP_Settings.enableSanity || !(trackedEntity instanceof EntityPlayer))
        {
            sanity = 100F;
        }

        ItemStack plate = trackedEntity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (plate != null && !isCreative) {
            if (plate.hasTagCompound() && plate.getTagCompound().hasKey("camelPackFill")) {
                int fill = plate.getTagCompound().getInteger("camelPackFill");
                if (fill > 0 && hydration <= 100F - EMP_Settings.hydrationMult) {
                    plate.getTagCompound().setInteger("camelPackFill", fill-1);
                    hydrate((float)EMP_Settings.hydrationMult);

                    if (bodyTemp >= 37F + EMP_Settings.tempMult/10F) {
                        bodyTemp -= EMP_Settings.tempMult/10F;
                    }
                }
            }
        }

        this.fixFloatinfPointErrors();

        if(trackedEntity instanceof EntityPlayer) {
            if (((EntityPlayer)trackedEntity).capabilities.isCreativeMode) {
                bodyTemp = prevBodyTemp;
                airQuality = prevAirQuality;
                hydration = prevHydration;
                sanity = prevSanity;
            }
        }

        if (airTemp <= 10F && bodyTemp <= 35F || bodyTemp <= 30F) {
            timeBelow10 += 1;
        } else {
            timeBelow10 = 0;
        }

        if (curAttackTime >= attackDelay) {
            if (airQuality <= 0) {
                trackedEntity.attackEntityFrom(EnviroDamageSource.suffocate, 4.0F);

                //TODO: put in gag sound
            }

            if (airQuality <= 10F) {
                trackedEntity.addPotionEffect(new PotionEffect(Potion.digSlowdown.id,200,1));
                trackedEntity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 200, 1));
            } else if (airQuality <= 25F) {
                trackedEntity.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 200, 0));
                trackedEntity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 200, 0));
            }

            if (!trackedEntity.isPotionActive(Potion.getPotionFromResourceLocation("fire_resistance"))) {
                if (bodyTemp >= 39F && enableHeat && EMP_Settings.enableHeatstrokeGlobal && (enviroData[6] == 1 || !(trackedEntity instanceof EntityAnimal))) {
                    if (bodyTemp >= 43F) {
                        trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.heatstroke.id, 200,2));
                    } else if (bodyTemp >= 41F) {
                        trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.heatstroke.id, 200, 1));
                    } else {
                        trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.heatstroke.id, 200, 0));
                    }
                }
            } else if (trackedEntity.isPotionActive(EnviroPotion.heatstroke)) {
                trackedEntity.removePotionEffect(EnviroPotion.heatstroke.id);
            }

            //TODO: add frostbite

            if ((bodyTemp >= 45F && enviroData[2] ==1) || bodyTemp >= 50F) {
                trackedEntity.setFire(10);
            }

            if(hydration <= 0F) {
                trackedEntity.attackEntityFrom(EnviroDamageSource.dehydrate, 4.0F);
            }

            //TODO: add sanity stuff

            curAttackTime = 0;
        } else {
            curAttackTime += 1;
        }

        EnviroPotion.checkAndApplyEffects(trackedEntity);

        if (isCreative) {
            bodyTemp = prevBodyTemp;
            airQuality = prevAirQuality;
            hydration = prevHydration;
            sanity = prevSanity;
        }

        //TODO: dimensin property stuff
    }

    public void fixFloatinfPointErrors() {
        airQuality = new BigDecimal(String.valueOf(airQuality)).setScale(2, RoundingMode.HALF_UP).floatValue();
        bodyTemp = new BigDecimal(String.valueOf(bodyTemp)).setScale(3, RoundingMode.HALF_UP).floatValue();
        airTemp = new BigDecimal(String.valueOf(airTemp)).setScale(3, RoundingMode.HALF_UP).floatValue();
        hydration = new BigDecimal(String.valueOf(hydration)).setScale(2, RoundingMode.HALF_UP).floatValue();
        sanity = new BigDecimal(String.valueOf(sanity)).setScale(3, RoundingMode.HALF_UP).floatValue();
    }

    public void hydrate(float amount) {
        float MAmount = (float)(amount * EMP_Settings.hydrationMult);

        if(hydration >= 100F - MAmount) {
            hydration = 100.0F;
        } else {
            hydration += MAmount;
        }

        this.fixFloatinfPointErrors();

        if (!EnviroMinePort.proxy.isClient() || EnviroMinePort.proxy.isOpenToLAN()) {
            EMP_StatusManager.syncMultiplayerTracker(this);
        }
    }

    public void dehydrate(float amount) {
        float MAmount = (float)(amount * EMP_Settings.hydrationMult);

        if (hydration >= MAmount) {
            hydration -= MAmount;
        } else {
            hydration = 0F;
        }

        this.fixFloatinfPointErrors();

        if (!EnviroMinePort.proxy.isClient() || EnviroMinePost.proxy.isOpenToLAN()) {
            EMP_StatusManager.syncMultiplayerTracker(this);
        }
    }
}
