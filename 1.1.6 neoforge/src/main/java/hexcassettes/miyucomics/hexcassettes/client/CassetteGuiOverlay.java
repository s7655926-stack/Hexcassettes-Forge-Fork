package hexcassettes.miyucomics.hexcassettes.client;

import com.mojang.blaze3d.systems.RenderSystem;
import hexcassettes.miyucomics.hexcassettes.HexcassettesMain;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesNetworking;
import hexcassettes.miyucomics.hexcassettes.inits.HexcassettesSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

public class CassetteGuiOverlay {
    public static final ResourceLocation CASSETTE_NORMAL =
            ResourceLocation.fromNamespaceAndPath("hexcassettes", "textures/gui/cassette.png");
    public static final ResourceLocation CASSETTE_EDIT =
            ResourceLocation.fromNamespaceAndPath("hexcassettes", "textures/gui/cassette_edit.png");
    public static final ResourceLocation CASSETTE_PLAY =
            ResourceLocation.fromNamespaceAndPath("hexcassettes", "textures/gui/cassette_play.png");
    public static final ResourceLocation CASSETTE_MUTE =
            ResourceLocation.fromNamespaceAndPath("hexcassettes", "textures/gui/cassette_mute.png");

    private static final int W = 20;
    private static final int H = 20;
    private static final int TEX_W = 20;
    private static final int TEX_H = 20;
    private static final int SPACING = 22;
    private static final int BOTTOM_PAD = 30;
    private static final int RIGHT_PAD = 0;
    private static final int MUTE_BADGE = 9;
    private static final int MUTE_SRC_X = 10;
    private static final int MUTE_SRC_Y = 11;
    private static final int MUTE_SRC_W = 11;
    private static final int MUTE_SRC_H = 11;
    private static final int TOP_PAD = 20;
    private static final int SCROLLBAR_W = 4;
    private static final int SCROLLBAR_GAP = 2;

    private static boolean wasLeftDown = false;
    private static boolean wasRightDown = false;
    private static int scrollOffset = 0;
    private static int lastSeenOwned = -1;

    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        String name = event.getScreen().getClass().getName().toLowerCase();
        if (!name.contains("spellcasting") && !name.contains("hexcasting")) return;
        int owned = Math.min(ClientStorage.ownedCassettes, HexcassettesMain.MAX_CASSETTES);
        if (owned <= 0) {
            scrollOffset = 0;
            lastSeenOwned = owned;
            return;
        }
        if (owned != lastSeenOwned) {
            lastSeenOwned = owned;
            scrollOffset = 0;
        }
        GuiGraphics g = event.getGuiGraphics();
        int sw = event.getScreen().width;
        int sh = event.getScreen().height;
        int maxVisible = maxVisibleSlots(sh);
        int maxOffset = Math.max(0, owned - maxVisible);
        scrollOffset = Math.min(Math.max(scrollOffset, 0), maxOffset);
        boolean needsScroll = owned > maxVisible;
        int visible = Math.min(owned - scrollOffset, maxVisible);
        int iconX = sw - W - RIGHT_PAD - (needsScroll ? SCROLLBAR_W + SCROLLBAR_GAP : 0);
        for (int v = 0; v < visible; v++) {
            int slot = owned - 1 - scrollOffset - v;
            int x = iconX;
            int y = sh - BOTTOM_PAD - ((visible - 1 - v) * SPACING);
            ResourceLocation tex;
            if (ClientStorage.isEditSelected(slot)) {
                tex = CASSETTE_EDIT;
            } else if (ClientStorage.isPlaySelected(slot)) {
                tex = CASSETTE_PLAY;
            } else {
                tex = CASSETTE_NORMAL;
            }
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            g.blit(tex, x, y, 0.0F, 0.0F, W, H, TEX_W, TEX_H);
            if (ClientStorage.isMuted(slot)) {
                g.blit(CASSETTE_MUTE,
                        x + W - MUTE_BADGE, y + H - MUTE_BADGE,
                        MUTE_BADGE, MUTE_BADGE,
                        (float) MUTE_SRC_X, (float) MUTE_SRC_Y, MUTE_SRC_W, MUTE_SRC_H,
                        32, 32);
            }
            if (isMouseOver(x, y, W, H, event.getMouseX(), event.getMouseY())) {
                g.fill(x, y, x + W, y + H, 0x33FFFFFF);
            }
        }
        if (needsScroll) {
            int trackX = sw - SCROLLBAR_W;
            int trackTop = sh - BOTTOM_PAD - (visible - 1) * SPACING;
            int trackBottom = sh - BOTTOM_PAD + H;
            int trackH = trackBottom - trackTop;
            g.fill(trackX, trackTop, trackX + SCROLLBAR_W, trackBottom, 0x66000000);
            int thumbH = Math.max(8, trackH * visible / owned);
            int thumbY = trackTop;
            if (maxOffset > 0) {
                thumbY = trackTop + (trackH - thumbH) * scrollOffset / maxOffset;
            }
            g.fill(trackX, thumbY, trackX + SCROLLBAR_W, thumbY + thumbH, 0xFFAAAAAA);
        }
        long window = mc.getWindow().getWindow();
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_PRESS;
        int hovered = getHoveredSlot(sw, sh, owned, needsScroll, event.getMouseX(), event.getMouseY());
        if (leftDown && !wasLeftDown && hovered != -1) {
            boolean shiftHeld =
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                            GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            playClickSound();
            if (shiftHeld) {
                HexcassettesNetworking.sendSelectPlay(hovered);
            } else {
                HexcassettesNetworking.sendSelectEdit(hovered);
            }
        }
        if (rightDown && !wasRightDown && hovered != -1) {
            playClickSound();
            HexcassettesNetworking.sendToggleMute(hovered);
        }
        wasLeftDown = leftDown;
        wasRightDown = rightDown;
    }

    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        String name = event.getScreen().getClass().getName().toLowerCase();
        if (!name.contains("spellcasting") && !name.contains("hexcasting")) return;
        int owned = Math.min(ClientStorage.ownedCassettes, HexcassettesMain.MAX_CASSETTES);
        if (owned <= 0) return;
        int sw = event.getScreen().width;
        int sh = event.getScreen().height;
        int maxVisible = maxVisibleSlots(sh);
        if (owned <= maxVisible) return;
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();
        if (mouseX < sw - W - SCROLLBAR_W - SCROLLBAR_GAP) return;
        if (mouseY < TOP_PAD || mouseY > sh - BOTTOM_PAD + H) return;
        int maxOffset = owned - maxVisible;
        double delta = event.getScrollDeltaY();
        if (delta > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (delta < 0) {
            scrollOffset = Math.min(maxOffset, scrollOffset + 1);
        }
        event.setCanceled(true);
    }

    private static int maxVisibleSlots(int sh) {
        return Math.max(1, (sh - BOTTOM_PAD - TOP_PAD) / SPACING + 1);
    }

    private static void playClickSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(HexcassettesSounds.CASSETTE_EJECT.get(), 1.0F, 1.0F);
        }
    }

    private static int getHoveredSlot(int sw, int sh, int owned, boolean needsScroll, double mouseX, double mouseY) {
        int maxVisible = maxVisibleSlots(sh);
        int maxOffset = Math.max(0, owned - maxVisible);
        scrollOffset = Math.min(Math.max(scrollOffset, 0), maxOffset);
        int visible = Math.min(owned - scrollOffset, maxVisible);
        int iconX = sw - W - RIGHT_PAD - (needsScroll ? SCROLLBAR_W + SCROLLBAR_GAP : 0);
        for (int v = 0; v < visible; v++) {
            int slot = owned - 1 - scrollOffset - v;
            int x = iconX;
            int y = sh - BOTTOM_PAD - ((visible - 1 - v) * SPACING);
            if (isMouseOver(x, y, W, H, mouseX, mouseY)) return slot;
        }
        return -1;
    }

    private static boolean isMouseOver(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
