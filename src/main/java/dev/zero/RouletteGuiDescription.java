package dev.zero;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureFlags;

import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class RouletteGuiDescription extends ScreenHandler {
    public static ScreenHandlerType<RouletteGuiDescription> TYPE;
    private final SimpleInventory customInv;
    private int betColor = -1;
    private int lastWinningColor = -1;
    private boolean spinning = false;

    public static final int SLOT_CHIP      = 11;
    public static final int SLOT_BET_RED   = 16;
    public static final int SLOT_BET_BLACK = 25;
    public static final int SLOT_BET_GREEN = 34;


    private static final int[] ROULETTE_SLOTS = {
            0, 1, 2, 3, 4,
            13, 22, 31, 40,
            39, 38, 37, 36,
            27, 18, 9
    };
    private static final int RESULT_SLOT_INDEX = 2;

    private static final Set<Integer> DECORATIVE_SLOTS = new HashSet<>();
    static {
        for (int idx : ROULETTE_SLOTS) DECORATIVE_SLOTS.add(idx);
    }


    private static final int[] INITIAL_COLORS = {
            1, 0, 2, 0, 1,   
            0, 1, 0, 1,      
            0, 1, 0, 1,      
            0, 1, 0         
    };
    private int[] currentColors;

    private static class SpinTask {
        final ServerPlayerEntity player;
        final RouletteGuiDescription handler;
        final int bet, betColor, targetResult;
        final int[] colors;
        final int spinsToAlign;
        int spinStep = 0;
        int slowStart;
        int delay = 0;
        boolean shown = false;

        SpinTask(ServerPlayerEntity p, RouletteGuiDescription h, int bet, int betColor, int targetResult, int[] start, int spinsToAlign) {
            this.player = p;
            this.handler = h;
            this.bet = bet;
            this.betColor = betColor;
            this.targetResult = targetResult;
            this.colors = start;
            this.spinsToAlign = spinsToAlign;
            this.slowStart = Math.max(0, spinsToAlign - 20);
        }
    }
    private static final Queue<SpinTask> queue = new LinkedList<>();
    private static final Random RANDOM = new Random();

    static {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (queue.isEmpty()) return;
            SpinTask t = queue.peek();

            if (t.handler == null) { queue.poll(); return; }
            if (!t.handler.spinning) { queue.poll(); return; }

            int delayTicks;
            if (t.spinStep < t.slowStart) delayTicks = 1;
            else if (t.spinStep < t.spinsToAlign - 8) delayTicks = 2;
            else if (t.spinStep < t.spinsToAlign - 4) delayTicks = 4;
            else delayTicks = 7;

            if (t.delay++ < delayTicks) return;
            t.delay = 0;

            if (t.spinStep < t.spinsToAlign) {

                int last = t.colors[t.colors.length - 1];
                System.arraycopy(t.colors, 0, t.colors, 1, t.colors.length - 1);
                t.colors[0] = last;

  
                for (int i = 0; i < ROULETTE_SLOTS.length; i++) {
                    t.handler.customInv.setStack(
                            ROULETTE_SLOTS[i],
                            t.handler.getColorStack(t.colors[i])
                    );
                }
                t.handler.currentColors = t.colors.clone();
                t.handler.customInv.markDirty();
                t.handler.sendContentUpdates();
                t.spinStep++;

  
                if (t.player != null) {
                    t.player.getWorld().playSound(
                            null,
                            t.player.getBlockPos(),
                            SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                            SoundCategory.PLAYERS,
                            0.7f,
                            1.2f + RANDOM.nextFloat() * 0.6f
                    );
                }
            }
   
            else if (!t.shown) {
                for (int i = 0; i < ROULETTE_SLOTS.length; i++) {
                    t.handler.customInv.setStack(
                            ROULETTE_SLOTS[i],
                            t.handler.getColorStack(t.colors[i])
                    );
                }
                t.handler.currentColors = t.colors.clone();
                t.handler.customInv.markDirty();
                t.handler.sendContentUpdates();

                int real = t.colors[RESULT_SLOT_INDEX];
                t.handler.lastWinningColor = real;
                boolean win = (t.betColor == real);

                int payout = win
                        ? (real == 2 ? t.bet * 10 : (int)Math.ceil(t.bet * 1.2))
                        : 0;

                if (win) {
                    t.player.getInventory().insertStack(new ItemStack(ModItems.CHIP, payout));
                    t.player.sendMessage(Text.literal("Você ganhou " + payout + " fichas!"), false);
                    t.player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("GANHOU!!")));
                    t.player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("+" + payout + " fichas")));
                    t.player.getWorld().playSound(
                            null,
                            t.player.getBlockPos(),
                            SoundEvents.ENTITY_PLAYER_LEVELUP,
                            SoundCategory.PLAYERS,
                            1.2f,
                            1.0f
                    );
                } else {
                    t.player.sendMessage(Text.literal("Perdeu! :("), false);
                    t.player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("PERDEU")));
                    t.player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("TENTE NOVAMENTE!")));
                    t.player.getWorld().playSound(
                            null,
                            t.player.getBlockPos(),
                            SoundEvents.ENTITY_VILLAGER_NO,
                            SoundCategory.PLAYERS,
                            1.0f,
                            1.0f
                    );
                }
                t.handler.spinning = false;
                t.handler.sendContentUpdates();
                t.shown = true;
            }
    
            else if (t.delay < 40) {
                t.delay++;
            }
         
            else {
                t.handler.spinning = false;
                t.handler.betColor = -1;
                t.handler.sendContentUpdates();
                if (t.player != null)
                    t.player.sendMessage(Text.literal("Aposte novamente!"), false);
                queue.poll();
            }
        });
    }

    public RouletteGuiDescription(int syncId, PlayerInventory inv) {
        super(TYPE, syncId);
        this.customInv = new SimpleInventory(45);

        int x0 = 8, y0 = 18;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                int idx = col + row * 9;
                int x = x0 + col * 18;
                int y = y0 + row * 18;

                if (DECORATIVE_SLOTS.contains(idx) ||
                        idx == SLOT_BET_BLACK || idx == SLOT_BET_RED || idx == SLOT_BET_GREEN) {
                    addSlot(new Slot(customInv, idx, x, y) {
                        @Override public boolean canInsert(ItemStack s) { return false; }
                        @Override public boolean canTakeItems(PlayerEntity p) { return false; }
                    });
                } else {
                    addSlot(new Slot(customInv, idx, x, y));
                }
            }
        }
        int ix = 8, iy = 124;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, ix + col * 18, iy + row * 18));
            }
        }
        int hx = 8, hy = 182;
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, hx + col * 18, hy));
        }

        resetRoulette();
    }

    private void resetRoulette() {
        currentColors = INITIAL_COLORS.clone();
        for (int i = 0; i < ROULETTE_SLOTS.length; i++) {
            customInv.setStack(ROULETTE_SLOTS[i], getColorStack(currentColors[i]));
        }
        customInv.setStack(SLOT_BET_BLACK, getButtonStack(0, false));
        customInv.setStack(SLOT_BET_RED,   getButtonStack(1, false));
        customInv.setStack(SLOT_BET_GREEN, getButtonStack(2, false));
        customInv.markDirty();
        sendContentUpdates();
        betColor = -1;
        spinning = false;
    }

    @Override public boolean canUse(PlayerEntity p) { return true; }
    @Override public ItemStack quickMove(PlayerEntity p, int idx) { return ItemStack.EMPTY; }

    @Override
    public void onClosed(PlayerEntity p) {
        super.onClosed(p);
        resetRoulette();
    }

    @Override
    public void onSlotClick(int slot, int btn,
                            net.minecraft.screen.slot.SlotActionType action,
                            PlayerEntity p) {
        if (spinning) {
            if (slot >= 0 && slot < 45) return;
        }
        if (action == net.minecraft.screen.slot.SlotActionType.PICKUP &&
                (slot == SLOT_BET_BLACK || slot == SLOT_BET_RED || slot == SLOT_BET_GREEN)) {
            onButtonClick(p, slot);
            return;
        }
        super.onSlotClick(slot, btn, action, p);
    }

    public boolean onButtonClick(PlayerEntity p, int slot) {
        if (spinning) return false;
        int c = switch (slot) {
            case SLOT_BET_RED   -> 1;
            case SLOT_BET_BLACK -> 0;
            case SLOT_BET_GREEN -> 2;
            default             -> -1;
        };
        if (c < 0) return false;

        ItemStack chip = customInv.getStack(SLOT_CHIP);
        if (chip.isEmpty() || chip.getItem() != ModItems.CHIP) {
            p.sendMessage(Text.literal("Coloque uma ficha!"), false);
            return false;
        }
        final int amt = Math.min(chip.getCount(), 64);
        if (amt <= 0) {
            p.sendMessage(Text.literal("Coloque pelo menos uma ficha!"), false);
            return false;
        }


        final int res = sorteioProbabilidadeVencedor(c);

       
        int offset = 0;
        for (int i = 0; i < currentColors.length; i++) {
            int idx = (RESULT_SLOT_INDEX + i) % currentColors.length;
            if (currentColors[idx] == res) {
                offset = i;
                break;
            }
        }
        int minSpins = 3 * currentColors.length;
        int spinsToAlign = minSpins + offset;

        final int[] start = currentColors.clone();

        if (p instanceof ServerPlayerEntity sp) {
            sp.server.execute(() -> {
                ItemStack stack = customInv.getStack(SLOT_CHIP);
                stack.decrement(amt);
                customInv.setStack(SLOT_BET_BLACK, getButtonStack(0, c == 0));
                customInv.setStack(SLOT_BET_RED,   getButtonStack(1, c == 1));
                customInv.setStack(SLOT_BET_GREEN, getButtonStack(2, c == 2));
                customInv.markDirty();
                sendContentUpdates();

                betColor = c;
                spinning = true;
                sendContentUpdates();

                queue.offer(new SpinTask(sp, this, amt, c, res, start, spinsToAlign));
            });
        }
        return true;
    }

    private int sorteioProbabilidadeVencedor(int aposta) {
        double chanceWin;
        double roll = RANDOM.nextDouble();

      
        if (aposta == 2) {
            chanceWin = 0.08;
        } else {
            chanceWin = 0.41;
        }

        if (roll < chanceWin) {
            return aposta;
        } else {
            double r = RANDOM.nextDouble();
            if (aposta == 2) {
                return (r < 0.5) ? 0 : 1;
            } else {
                if (r < 0.51) {
                    return aposta == 0 ? 1 : 0;
                } else {
                    return 2;
                }
            }
        }
    }

    private ItemStack getColorStack(int color) {
        Item it = switch (color) {
            case 0 -> ModItems.BLACK_SQUARE;
            case 1 -> ModItems.RED_SQUARE;
            case 2 -> ModItems.GREEN_SQUARE;
            default -> null;
        };
        return it == null ? ItemStack.EMPTY : new ItemStack(it);
    }

    private ItemStack getButtonStack(int color, boolean sel) {
        Item it = switch (color) {
            case 0 -> ModItems.BLACK_BOTTON;
            case 1 -> ModItems.RED_BOTTON;
            case 2 -> ModItems.GREEN_BOTTON;
            default -> null;
        };
        if (it == null) return ItemStack.EMPTY;
        return new ItemStack(it, 1);
    }

    public boolean isSpinning() { return spinning; }
    public int     getLastWinningColor() { return lastWinningColor; }

    public static void register() {
        TYPE = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of("roulettezero", "roulette_gui"),
                new ScreenHandlerType<>(RouletteGuiDescription::new, FeatureFlags.VANILLA_FEATURES)
        );
    }
    public static void open(ServerPlayerEntity p) {
        p.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                (sync, inv, pl) -> new RouletteGuiDescription(sync, inv),
                Text.literal("Roleta Cassino")
        ));
    }
}
