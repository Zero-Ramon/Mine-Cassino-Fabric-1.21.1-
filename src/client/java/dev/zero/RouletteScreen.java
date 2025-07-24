package dev.zero;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class RouletteScreen extends HandledScreen<RouletteGuiDescription> {
    private static final Identifier GUI = Identifier.of("roulettezero", "textures/gui/gui_all.png");

    public static final int SLOT_BET_RED   = 16;
    public static final int SLOT_BET_BLACK = 25;
    public static final int SLOT_BET_GREEN = 34;

    public RouletteScreen(RouletteGuiDescription handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        this.backgroundWidth       = 176;
        this.backgroundHeight      = 206;
        this.titleY                = 6;
        this.playerInventoryTitleY = 119;
    }

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
        int x = (width  - backgroundWidth)  / 2;
        int y = (height - backgroundHeight) / 2;
        ctx.drawTexture(GUI, x, y, 0, 0, backgroundWidth, backgroundHeight, 176, 206);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        if (handler.isSpinning()) {
            // Enquanto gira, exibe a mensagem
            String msg = "Girando...";
            int w  = textRenderer.getWidth(msg);
            int xx = this.x + (backgroundWidth - w) / 2;
            int yy = this.y + 10;
            ctx.drawText(textRenderer, msg, xx, yy, 0xFFFFFF, false);
        } else {
            // Quando para, mostra os tooltips dos botões
            drawBetButtonTooltips(ctx, mouseX, mouseY, handler);
        }
    }

    private void drawBetButtonTooltips(DrawContext ctx, int mouseX, int mouseY, RouletteGuiDescription gui) {
        for (int i = 0; i < gui.slots.size(); i++) {
            var slot = gui.slots.get(i);
            int absX = this.x + slot.x;
            int absY = this.y + slot.y;

            if (i == SLOT_BET_RED && isMouseOverSlot(mouseX, mouseY, absX, absY, 16)) {
                ctx.drawTooltip(textRenderer, List.of(
                        Text.literal("§cApostar em Vermelho"),
                        Text.literal("§7Multiplicador: §e1.2x"),
                        Text.literal("§7Chance de vitória: §a37%"),
                        Text.literal("§8↳ §oPagamentos frequentes!"),
                        Text.literal("§7Proprietário: §a" + CasinoManager.getOwner())
                ), mouseX, mouseY);
            }
            if (i == SLOT_BET_BLACK && isMouseOverSlot(mouseX, mouseY, absX, absY, 16)) {
                ctx.drawTooltip(textRenderer, List.of(
                        Text.literal("§8Apostar em Preto"),
                        Text.literal("§7Multiplicador: §e1.2x"),
                        Text.literal("§7Chance de vitória: §a37%"),
                        Text.literal("§8↳ §oSeguro e constante!"),
                        Text.literal("§7Proprietário: §a" + CasinoManager.getOwner())
                ), mouseX, mouseY);
            }
            if (i == SLOT_BET_GREEN && isMouseOverSlot(mouseX, mouseY, absX, absY, 16)) {
                ctx.drawTooltip(textRenderer, List.of(
                        Text.literal("§aApostar em Verde"),
                        Text.literal("§7Multiplicador: §c10x"),
                        Text.literal("§7Chance de vitória: §c2.5%"),
                        Text.literal("§e↳ §oOusado! Só para os sortudos."),
                        Text.literal("§7Proprietário: §a" + CasinoManager.getOwner())
                ), mouseX, mouseY);
            }
        }
    }

    private boolean isMouseOverSlot(int mouseX, int mouseY, int slotX, int slotY, int size) {
        return mouseX >= slotX
                && mouseY >= slotY
                && mouseX < slotX + size
                && mouseY < slotY + size;
    }

    @Override
    protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
        // Vazio, não desenha texto extra
    }
}
