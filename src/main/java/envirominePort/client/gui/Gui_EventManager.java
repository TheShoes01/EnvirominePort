package envirominePort.client.gui;

import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

import envirominePort.client.gui.hud.HUDRegistry;
import envirominePort.client.gui.hud.HudItem;
import envirominePort.client.gui.hud.items.Debug_Info;
//import envirominePort.client.gui.hud.items.GasMaskHud;
//import envirominePort.client.gui.menu.EMP_Button;
//import envirominePort.client.gui.menu.EMP_Gui_Menu;
import envirominePort.core.EMP_Settings;
import envirominePort.core.EnviroMinePort;
import envirominePort.handlers.EMP_StatusManager;
import envirominePort.trackers.EnviroDataTracker;
import envirominePort.utils.RenderAssist;
import envirominePort.world.ClientQuake;

@SideOnly(Side.CLIENT)
public class Gui_EventManager {
    public int width;
    public int height;
    public GuiButton envirominePort;
    private Minecraft mc = Minecraft.getMinecraft();

    public static int scaleTranslateX;
    public static int scaleTranslateY;
    public static final ResourceLocation guiResource = new ResourceLocation("envirominePort", "textures/gui/status_Gui.png");
    public static final ResourceLocation blurOverlayResource = new ResourceLocation("envirominePort", "textures/misc/blur.png");
    public static EnviroDataTracker tracker = null;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onGuiRender(RenderGameOverlayEvent.Post event) {
        if (event.getType() != ElementType.HELMET || event.isCancelable()) {
            return;
        }

        mc.player.height = 1.62F;
        /* if (ClientQuake.GetQuakeShake(mc.world, mc.player) > 0) {

        } */

        HUDRegistry.checkForResize();

        if (tracker == null) {
            if (!(EMP_Settings.enableAirQ == false && EMP_Settings.enableBodyTemp == false && EMP_Settings.enableHydrate == false && EMP_Settings.enableSanity == false)) {
                tracker = EMP_StatusManager.lookupTrackerFromUsername(this.mc.player.getName());
            }
        } else if (tracker.isDisabled || !EMP_StatusManager.trackerList.containsValue(tracker)) {
            tracker = null;
        } else {
            HudItem.blinkTick++;

            /*if (UI_Settings.overlay) {
                GasMaskHud.renderGasMask(mc);
            }*/

            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glColor4f(1F, 1F, 1F, 1F);

            for (HudItem huditem : HUDRegistry.getActiveHudItemList()) {
                if (mc.playerController.isInCreativeMode() && !huditem.isRenderedInCreative()) {
                    continue;
                }

                if (mc.player.getRidingEntity() instanceof EntityLivingBase) {
                    if (huditem.shouldDrawOnMount()) {
                        if (UI_Settings.overlay) {
                            RenderAssist.bindTexture(huditem.getResource("TintOverlay"));
                            huditem.renderScreenOverlay(HUDRegistry.screenWidth, HUDRegistry.screenHeight);
                        }

                        RenderAssist.bindTexture(huditem.getResource(""));

                        huditem.fixBounds();
                        huditem.render();
                    }
                }else {
                    if (huditem.shouldDrawAsPlayer()) {
                        if(UI_Settings.overlay) {
                            RenderAssist.bindTexture(huditem.getResource("TintOverlay"));
                            huditem.renderScreenOverlay(HUDRegistry.screenWidth, HUDRegistry.screenHeight);
                        }

                        RenderAssist.bindTexture(huditem.getResource(""));

                        huditem.fixBounds();
                        huditem.render();
                    }
                }
            }
            Debug_Info.ShowDebutText(event, mc);
            GL11.glPopMatrix();
        }
    }
}
