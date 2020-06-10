package envirominePort.client.gui.hud.items;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;
import envirominePort.client.gui.Gui_EventManager;
import envirominePort.client.gui.UI_Settings;
import envirominePort.client.gui.hud.HUDRegistry;
import envirominePort.client.gui.hud.HudItem;
import envirominePort.core.EMP_Settings;
import envirominePort.utils.Alignment;
import envirominePort.utils.RenderAssist;

public class HudItemAirQuality extends HudItem {
    @Override
    public String getName() {
        return "Air Quality";
    }

    public String getNameLoc() {
        return I18n.format("options.envirominePort.hud.air");
    }

    @Override
    public String getButtonLabel() {
        return getNameLoc() + " Bar";
    }

    @Override
    public Alignment getDefaultAlignment() {
        return Alignment.BOTTOMRIGHT;
    }

    @Override
    public int getDefaultPosX() {
        return(((HUDRegistry.screenWidth - 4) - getWidth()));
    }

    @Override
    public int getDefaultPosY() {
        return (HUDRegistry.screenHeight - 15);
    }

    @Override
    public int getWidth() {
        return UI_Settings.minimalHud && !rotated ? 0 : 64;
    }

    @Override
    public int getHeight() {
        return 8;
    }

    @Override
    public boolean isEnabledByDefault() {
        return EMP_Settings.enableAirQ;
    }

    @Override
    public boolean isBlinking() {
        if(blink() && Gui_EventManager.tracker.airQuality < 25) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getDefaultID() {
        return 3;
    }

    @Override
    public void render() {
        GL11.glPushMatrix();

        float transx = (float)(this.posX - (this.posX * UI_Settings.guiScale));
        float transy = (float)(this.posY - (this.posY * UI_Settings.guiScale));

        GL11.glTranslated(transx, transy, 0);

        GL11.glScalef((float)UI_Settings.guiScale, (float)UI_Settings.guiScale, (float)UI_Settings.guiScale);

        int airBar = MathHelper.ceil((Gui_EventManager.tracker.airQuality / 100) * this.getWidth());

        int frameBorder = 4;
        if (this.isBlinking()) {
            frameBorder = 5;
        }

        if (airBar > this.getWidth()) {
            airBar = this.getWidth();
        } else if (airBar < 0) {
            airBar = 0;
        }

        if (!UI_Settings.minimalHud || rotated) {
            GL11.glPushMatrix();

            if(this.rotated) {
                int angle = -90;

                GL11.glTranslatef(posX, posY, 0);
                GL11.glRotatef(angle, 0, 0, 1);
                GL11.glTranslatef(-posX+6, -posY-8+(getWidth()/2), 0);
            }

            RenderAssist.drawTextureModalRect(posX, posY, 0, 8, getWidth(), getHeight());
            RenderAssist.drawTextureModalRect(posX,posY,64,8,airBar, getHeight());

            RenderAssist.drawTextureModalRect(posX+airBar-2, posY+2,8,64,4,4);

            RenderAssist.drawTextureModalRect(posX, posY, 0, getHeight()*frameBorder, getWidth(), getHeight());

            GL11.glPopMatrix();
        }

        if(UI_Settings.ShowGuiIcons) {
            int iconPosX = getIconPosX();
            if (rotated) {
                iconPosX = posX + 20;
            }

            RenderAssist.drawTextureModalRect(iconPosX, posY-4,48,80,16,16);
        }

        if (UI_Settings.ShowText && !rotated) {
            RenderAssist.drawTextureModalRect(getTextPosX(),posY,64,getHeight()*4,32,getHeight());

            Minecraft.getMinecraft().fontRenderer.drawString(Gui_EventManager.tracker.airQuality + "%", getTextPosX(), posY, 16777215);
        }
        GL11.glPopMatrix();
    }

    @Override
    public ResourceLocation getResource(String type) {
        if (type == "TintOverlay") {
            return Gui_EventManager.blurOverlayResource;
        } else {
            return Gui_EventManager.guiResource;
        }
    }

    @Override
    public void renderScreenOverlay(int scaledwidth, int scaledheight) {
        if (Gui_EventManager.tracker.airQuality < 50F) {
            int grad = (int)((50F - Gui_EventManager.tracker.airQuality) / 50 * 255);
            RenderAssist.drawScreenOverlay(scaledwidth,scaledheight,RenderAssist.getColorFromRGBA(32, 96, 0 ,grad));
        }
    }
}
