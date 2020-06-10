package envirominePort.utils;

import java.awt.Color;
import java.nio.ByteOrder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

public class RenderAssist {
    public static float zLevel;

    public static void drawUnfilledCircle(float posX, float posY, float radius, int num_segments, int color) {
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(f1,f2,f3,f);

        GL11.glBegin(GL11.GL_LINE_LOOP);

        for (int i = 0; i < num_segments; i++) {
            double theta = 2.0f * Math.PI * i / num_segments;// get the current
            // angle

            double x = radius * Math.cos(theta);// calculate the x component
            double y = radius * Math.sin(theta);// calculate the y component

            GL11.glVertex2d(x + posX, y + posY);// output vertex

        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void drawCircle(float posX, float posY, float radius, int num_segments, int color) {
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(f1,f2,f3,f);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(posX, posY);
        for (int i = num_segments; i >= 0; i--) {
            double theta = i * (Math.PI * 2) / num_segments;
            GL11.glVertex2d(posX+radius*Math.cos(theta), posY+radius*Math.sin(theta));
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void drawUnfilledRect(float x1, float g, float x2, float y2, int color) {
        float j1;

        if (x1 < x2) {
            j1 = x1;
            x1 = x2;
            x2 = j1;
        }

        if (g < y2) {
            j1 = g;
            g = y2;
            y2 = j1;
        }

        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(f1, f2, f3, f);

        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2d(x1,y2);
        GL11.glVertex2d(x2,y2);
        GL11.glVertex2d(x2,g);
        GL11.glVertex2d(x1,g);

        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void drawHorizontalLine(int par1, int par2, int par3, int par4) {
        if (par2 < par1) {
            int temp = par1;
            par1 = par2;
            par2 = temp;
        }

        drawRect(par1, par3, par2+1, par3+1, par4);
    }

    public static void drawVerticalLine(int par1, int par2, int par3, int par4) {
        if (par3 < par2) {
            int temp = par2;
            par2 = par3;
            par3 = temp;
        }

        drawRect(par1, par2+1, par1+1, par3, par4);
    }

    public static void drawTextureModalRect(float x, float y, float u, float v, float width, float height) {
        float f = 0.00390625F;
        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bb.pos(x,y+height, RenderAssist.zLevel).tex(u*f, (v+height)*f).endVertex();
        bb.pos(x+width,y+height, RenderAssist.zLevel).tex((u+width)*f, (v+height)*f).endVertex();
        bb.pos(x+width,y, RenderAssist.zLevel).tex((u+width)*f, v*f).endVertex();
        bb.pos(x,y, RenderAssist.zLevel).tex(u*f, v*f).endVertex();
        bb.finishDrawing();
    }

    public static void bindTexture(ResourceLocation res) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(res);
    }

    public static void bindTexture(String textureLocation) {
        ResourceLocation res = new ResourceLocation(textureLocation);
        Minecraft.getMinecraft().getTextureManager().bindTexture(res);
    }

    public static void drawRect(float g, float h, float i, float j, int color) {
        float j1;

        if (g < i) {
            j1 = g;
            g = i;
            i = j1;
        }

        if (h < j) {
            j1 = h;
            h = j;
            j = j1;
        }

        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(f1, f2, f3, f);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(g, j);
        GL11.glVertex2d(i, j);
        GL11.glVertex2d(i, h);
        GL11.glVertex2d(g, h);

        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void renderInventorySlot(int slot, int x, int y, float partialTick, Minecraft mc) {
        RenderItem itemRenderer = mc.getRenderItem();
        ItemStack itemstack = mc.player.inventory.mainInventory.get(slot);
        x += 91;
        y += 12;

        if (itemstack != null) {
            float f1 = itemstack.getAnimationsToGo() - partialTick;

            if (f1 > 0.0F) {
                GL11.glPushMatrix();
                float f2 = 1.0F + f1 / 5.0F;
                GL11.glTranslatef(x+8, y+12, 0.0F);
                GL11.glScalef(1.0F / f2, (f2 + 1.0F) / 2.0F, 1.0F);
                GL11.glTranslatef(-(x+8), -(y+12), 0.0F);
            }

            itemRenderer.renderItemAndEffectIntoGUI(itemstack, x, y);

            if (f1 > 0.0F) {
                GL11.glPopMatrix();
            }

            itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, itemstack, x, y, "");
        }
    }

    public static int getColorFromRGBA_F(float par1, float par2, float par3, float par4) {
        int R = (int)(par1 * 255.0F);
        int G = (int)(par2 * 255.0F);
        int B = (int)(par3 * 255.0F);
        int A = (int)(par4 * 255.0F);

        return getColorFromRGBA(R, G, B, A);
    }

    public static int getColorFromRGBA(int R, int G, int B, int A) {
        if(R > 255)
        {
            R = 255;
        }

        if(G > 255)
        {
            G = 255;
        }

        if(B > 255)
        {
            B = 255;
        }

        if(A > 255)
        {
            A = 255;
        }

        if(R < 0)
        {
            R = 0;
        }

        if(G < 0)
        {
            G = 0;
        }

        if(B < 0)
        {
            B = 0;
        }

        if(A < 0)
        {
            A = 0;
        }

        if(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
        {
            return A << 24 | R << 16 | G << 8 | B;
        } else
        {
            return B << 24 | G << 16 | R << 8 | A;
        }
    }

    public static Color blendColors(int a, int b, float ratio) {
        if(ratio > 1f)
        {
            ratio = 1f;
        } else if(ratio < 0f)
        {
            ratio = 0f;
        }
        float iRatio = 1.0f - ratio;

        int aA = (a >> 24 & 0xff);
        int aR = ((a & 0xff0000) >> 16);
        int aG = ((a & 0xff00) >> 8);
        int aB = (a & 0xff);

        int bA = (b >> 24 & 0xff);
        int bR = ((b & 0xff0000) >> 16);
        int bG = ((b & 0xff00) >> 8);
        int bB = (b & 0xff);

        int A = (int)((aA * iRatio) + (bA * ratio));
        int R = (int)((aR * iRatio) + (bR * ratio));
        int G = (int)((aG * iRatio) + (bG * ratio));
        int B = (int)((aB * iRatio) + (bB * ratio));

        return new Color(R, G, B, A);
    }

    @SideOnly(Side.CLIENT)
    public static void drawScreenOverlay(int par1, int par2, int par5) {
        float f = (float)(par5 >> 24 & 255) / 255.0F;
        float f1 = (float)(par5 >> 16 & 255) / 255.0F;
        float f2 = (float)(par5 >> 8 & 255) / 255.0F;
        float f3 = (float)(par5 & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bb.pos(0.0D,(double)par2,-90.0D).tex(0.0D,1.0D).endVertex();
        bb.pos((double)par1, (double)par2, -90.0D).tex(1.0D, 1.0D).endVertex();
        bb.pos((double)par1,0.0, -90.0D).tex(1.0D,0.0D).endVertex();
        bb.pos(0.0D,0.0D,-90.0D).tex(0.0D,0.0D).endVertex();
        bb.finishDrawing();

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void scaledTextureModalRect(int x,int y,int u,int v,int width,int height, int scale) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bb.pos((double)x, (double)(y + (height * scale)), 0.0D).tex((double)((float)u * f), (double)((float)(v + height) * f1)).endVertex();
        bb.pos((double)(x + (width * scale)), (double)(y + (height * scale)), 0.0D).tex((double)((float)(u + width) * f), (double)((float)(v + height) * f1)).endVertex();
        bb.pos((double)(x + (width * scale)), (double)y, 0.0D).tex((double)((float)(u + width) * f), (double)((float)v * f1)).endVertex();
        bb.pos(0.0D,0.0D,0.0D).tex((double)((float)(u) * f), (double)((float)(v) * f1)).endVertex();
        bb.finishDrawing();
    }
}
