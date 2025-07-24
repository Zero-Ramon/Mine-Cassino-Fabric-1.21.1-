package dev.zero;

import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class RegistryHelper {
    public static SoundEvent getSound(String id) {
        Identifier ident = Identifier.tryParse(id);
        if (ident == null) return null;
        return Registries.SOUND_EVENT.get(ident);
    }
}
