package hexcassettes.miyucomics.hexcassettes.client;

import com.mojang.blaze3d.systems.RenderSystem;
import hexcassettes.miyucomics.hexcassettes.HexcassettesMain;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesNetworking;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

public class CassetteGuiOverlay {

    public static final ResourceLocation CASSETTE_NORMAL =
            new ResourceLocation("hexcassettes", "textures/gui/cassette.png");

    public static final ResourceLocation CASSETTE_EDIT =
            new ResourceLocation("hexcassettes", "textures/gui/cassette_edit.png");

    public static final ResourceLocation CASSETTE_PLAY =
            new ResourceLocation("hexcassettes", "textures/gui/cassette_play.png");

    private static final int W = 20;
    private static final int H = 20;
    private static final int TEX_W = 20;
    private static final int TEX_H = 20;
    private static final int SPACING = 22;
    private static final int BOTTOM_PAD = 30;
    private static final int RIGHT_PAD = 0;

    private static boolean wasLeftDown = false;

    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String name = event.getScreen().getClass().getName().toLowerCase();
        if (!name.contains("spellcasting") && !name.contains("hexcasting")) return;

        int owned = Math.min(ClientStorage.ownedCassettes, HexcassettesMain.MAX_CASSETTES);
        if (owned <= 0) return;

        GuiGraphics g = event.getGuiGraphics();
        int sw = event.getScreen().width;
        int sh = event.getScreen().height;

        // ── РЕНДЕР ─────────────────────────────────────────────────────
        for (int i = 0; i < owned; i++) {
            int x = sw - W - RIGHT_PAD;
            int y = sh - BOTTOM_PAD - (i * SPACING);

            // ===================== ВЫБОР ТЕКСТУРЫ =====================
            ResourceLocation tex;
            if (ClientStorage.isEditSelected(i)) {
                tex = CASSETTE_EDIT;
            } else if (ClientStorage.isPlaySelected(i)) {
                tex = CASSETTE_PLAY;
            } else {
                tex = CASSETTE_NORMAL;
            }
            // ==========================================================

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            g.blit(tex, x, y, 0, 0, W, H, TEX_W, TEX_H);

            // Hover подсветка
            if (isMouseOver(x, y, W, H, event.getMouseX(), event.getMouseY())) {
                g.fill(x, y, x + W, y + H, 0x33FFFFFF);
            }
        }

        // ── КЛИКИ ──────────────────────────────────────────────────────
        boolean leftDown = GLFW.glfwGetMouseButton(
                mc.getWindow().getWindow(),
                GLFW.GLFW_MOUSE_BUTTON_1
        ) == GLFW.GLFW_PRESS;

        if (leftDown && !wasLeftDown) {
            int slot = getHoveredSlot(sw, sh, owned, event.getMouseX(), event.getMouseY());

            if (slot != -1) {
                boolean shiftHeld =
                        GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                                GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

                playClickSound();

                if (shiftHeld) {
                    HexcassettesNetworking.sendSelectPlay(slot);
                } else {
                    HexcassettesNetworking.sendSelectEdit(slot);
                }
            }
        }

        wasLeftDown = leftDown;
    }

    private static void playClickSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(HexcassettesSounds.CASSETTE_EJECT.get(), 1.0F, 1.0F);
        }
    }

    private static int getHoveredSlot(int sw, int sh, int owned, double mouseX, double mouseY) {
        for (int i = 0; i < owned; i++) {
            int x = sw - W - RIGHT_PAD;
            int y = sh - BOTTOM_PAD - (i * SPACING);
            if (isMouseOver(x, y, W, H, mouseX, mouseY)) return i;
        }
        return -1;
    }

    private static boolean isMouseOver(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
