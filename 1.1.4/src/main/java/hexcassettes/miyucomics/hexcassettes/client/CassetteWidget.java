package hexcassettes.miyucomics.hexcassettes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class CassetteWidget extends AbstractWidget {

    private final int slot;

    public CassetteWidget(int slot, int x, int y) {
        super(x, y, 20, 18, Component.literal("Cassette " + slot));
        this.slot = slot;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = isHovered();

        // ── Цвета ──────────────────────────────────────────────────────────────
        // Оригинальные значения — это int-представление ARGB цветов.
        // -1426074774 = 0xAA334455 (тёмно-синий, полупрозрачный, при наведении)
        // -1435809234 = 0xAA223344 (ещё темнее, обычное состояние)
        int backgroundColor = hovered ? 0xAA334455 : 0xAA223344;

        // -3672      = 0xFFFFEF28  (жёлтая рамка при наведении)
        // -14019062  = 0xFF2A4A6A  (синяя рамка в обычном состоянии)
        int borderColor = hovered ? 0xFFFFEF28 : 0xFF2A4A6A;

        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        // ── Фон виджета ────────────────────────────────────────────────────────
        guiGraphics.fill(x, y, x + w, y + h, backgroundColor);

        // ── Рамка (4 стороны по 1 пикселю) ────────────────────────────────────
        guiGraphics.fill(x,         y,         x + w,     y + 1,     borderColor); // верх
        guiGraphics.fill(x,         y + h - 1, x + w,     y + h,     borderColor); // низ
        guiGraphics.fill(x,         y,         x + 1,     y + h,     borderColor); // лево
        guiGraphics.fill(x + w - 1, y,         x + w,     y + h,     borderColor); // право

        // ── Катушки кассеты (два прямоугольника) ───────────────────────────────
        // -12899818 = 0xFF3C5C56  (светлее при наведении)
        // -15069176 = 0xFF1A3C38  (темнее в обычном состоянии)
        int reelColor = hovered ? 0xFF3C5C56 : 0xFF1A3C38;
        guiGraphics.fill(x + 4,  y + 6, x + 8,  y + 10, reelColor); // левая катушка
        guiGraphics.fill(x + 12, y + 6, x + 16, y + 10, reelColor); // правая катушка

        // ── Индикатор активного слота (edit или play) ──────────────────────────
        // -11141291 = 0xFF55AADD (голубая полоска внизу виджета)
        if (ClientStorage.isEditSelected(slot) || ClientStorage.isPlaySelected(slot)) {
            guiGraphics.fill(x + 2, y + h - 4, x + w - 2, y + h - 2, 0xFF55AADD);
        }

        // ── Номер слота при наведении ──────────────────────────────────────────
        // -1 = 0xFFFFFFFF (белый)
        if (hovered) {
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    String.valueOf(slot + 1), // слоты начинаются с 0, показываем с 1
                    x + 7, y + 5,
                    0xFFFFFFFF,
                    false
            );
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        System.out.println("[Hexcassettes] Clicked cassette slot: " + slot);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // Нет описания для скринридера
    }
}