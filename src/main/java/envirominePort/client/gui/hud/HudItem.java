package envirominePort.client.gui.hud;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import envirominePort.client.gui.UI_Settings;
import envirominePort.utils.Alignment;

public abstract class HudItem {
    public Alignment alignment;
    public int posX;
    public int posY;
    public int phase;
    private int id;
    public boolean rotated = false;

    public boolean blink = false;
    public static int blinkTick = 0;

    public HudItem() {
        alignment = getDefaultAlignment();
        posX = getDefaultPosX();
        posY = getDefaultPosY();
        id = getDefaultID();
        phase = 0;
    }

    public abstract String getName();

    public abstract String getNameLoc();

    public abstract String getButtonLabel();

    public abstract Alignment getDefaultAlignment();

    public abstract int getDefaultPosX();

    public abstract int getDefaultPosY();

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract ResourceLocation getResource(String type);

    public abstract boolean isBlinking();

    public abstract int getDefaultID();

    public abstract void render();

    public abstract void renderScreenOverlay(int scaledwidth, int scaledheight);

    public int getTextFrameWidth() {
        if (!UI_Settings.ShowText || rotated) {
            return 0;
        } else {
            return 32;
        }
    }

    public int getIconPosX() {
        if (UI_Settings.minimalHud && !rotated) {
            if(!isLeftSide()) {
                return posX - getTextFrameWidth() - 16;
            } else {
                return posX + getTextFrameWidth();
            }
        } else {
            if (rotated) {
                return posX - 16 + (getHeight() /2);
            } else if (!isLeftSide()) {
                return posX - getTextFrameWidth() - 16 + (rotated? (getHeight() / 2) : 0);
            } else {
                return posX + getWidth() + getTextFrameWidth();
            }
        }
    }

    public int getTextPosX() {
        if(UI_Settings.minimalHud) {
            if (!isLeftSide()) {
                return posX - getTextFrameWidth();
            } else {
                return posX;
            }
        } else {
            if (!isLeftSide()) {
                return posX - getTextFrameWidth();
            } else {
                return posX + getWidth();
            }
        }
    }

    public int getTotalBarWidth() {
        return (getWidth() + getTextFrameWidth() + 16);
    }

    public boolean isLeftSide() {
        boolean Side = false;

        int ScreenHalf = HUDRegistry.screenWidth / 2;
        int BarPos = (getTotalBarWidth() / 2) + posX;

        if (BarPos <= ScreenHalf) {
            Side = true;
        }

        return Side;
    }

    public void rotate() {
        rotated = !rotated;
    }

    public boolean blink() {
        if(blinkTick >= 60) {
            blink = !blink;
            blinkTick = 1;
        }

        return blink;
    }

    public void tick() {}

    public boolean needsTick() {
        return false;
    }

    public boolean isMoveable() {
        return true;
    }

    public boolean isEnabledByDefault() {
        return true;
    }

    public boolean isRenderedInCreative() {
        return true;
    }

    public void fixBounds() {
        posX = Math.max(0, Math.min(HUDRegistry.screenWidth - (int)(getWidth() * UI_Settings.guiScale), posX));
        posY = Math.max(0, Math.min(HUDRegistry.screenHeight - (int)(getHeight() * UI_Settings.guiScale), posY));
    }

    public void loadFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("posX")) {
            posX = nbt.getInteger("posX");
        } else {
            posX = getDefaultPosX();
        }

        if (nbt.hasKey("posY")) {
            posY = nbt.getInteger("posY");
        } else {
            posY = getDefaultPosY();
        }

        if (nbt.hasKey("alignment")) {
            alignment = Alignment.fromString(nbt.getString("alignment"));
        } else {
            alignment = getDefaultAlignment();
        }

        if (nbt.hasKey("id")) {
            id = nbt.getInteger("id");
        } else {
            id = getDefaultID();
        }

        if (nbt.hasKey("rotated")) {
            rotated = nbt.getBoolean("rotated");
        } else {
            rotated = false;
        }
    }

    public void saveToNBT(NBTTagCompound nbt) {
        nbt.setInteger("posX", posX);
        nbt.setInteger("posY", posY);
        nbt.setString("alignment", alignment.toString());
        nbt.setInteger("id", id);
        nbt.setBoolean("rotated", rotated);
    }

    public boolean shouldDrawOnMount() {
        return true;
    }

    public boolean shouldDrawAsPlayer() {
        return true;
    }

    public boolean canRotate() {
        return true;
    }
}
