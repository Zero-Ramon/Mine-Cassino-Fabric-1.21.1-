package dev.zero;

import net.minecraft.sound.SoundEvent;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final Identifier ROULETTE_SPIN_ID = Identifier.of("roulettezero", "roulette_spin");
    public static final Identifier ROULETTE_WIN_ID = Identifier.of("roulettezero", "roulette_win");
    public static final Identifier ROULETTE_LOSE_ID = Identifier.of("roulettezero", "roulette_lose");

    public static SoundEvent ROULETTE_SPIN = SoundEvent.of(ROULETTE_SPIN_ID);
    public static SoundEvent ROULETTE_WIN = SoundEvent.of(ROULETTE_WIN_ID);
    public static SoundEvent ROULETTE_LOSE = SoundEvent.of(ROULETTE_LOSE_ID);

    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, ROULETTE_SPIN_ID, ROULETTE_SPIN);
        Registry.register(Registries.SOUND_EVENT, ROULETTE_WIN_ID, ROULETTE_WIN);
        Registry.register(Registries.SOUND_EVENT, ROULETTE_LOSE_ID, ROULETTE_LOSE);
    }
}
