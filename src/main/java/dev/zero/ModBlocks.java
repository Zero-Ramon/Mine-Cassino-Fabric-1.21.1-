package dev.zero;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block ROULETTE_BLOCK = new RouletteBlock(Block.Settings.create()
            .strength(2.5f)
            .requiresTool()
            .solid() // ADICIONE ISSO
    );

    public static void register(String modid) {
        Registry.register(Registries.BLOCK, Identifier.of(modid, "roulette_block"), ROULETTE_BLOCK);
        Registry.register(Registries.ITEM, Identifier.of(modid, "roulette_block"),
                new BlockItem(ROULETTE_BLOCK, new Item.Settings()));
    }
}
