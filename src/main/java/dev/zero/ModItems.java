package dev.zero;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ModItems {
    public static Item CHIP;
    public static Item BLACK_SQUARE;
    public static Item RED_SQUARE;
    public static Item GREEN_SQUARE;

    public static Item BLACK_BOTTON;
    public static Item RED_BOTTON;
    public static Item GREEN_BOTTON;

    public static void register(String modid) {
   
        CHIP = Registry.register(
                Registries.ITEM,
                Identifier.of(modid, "chip"),
                new Item(new Item.Settings()) {
                    @Override
                    public Text getName(ItemStack stack) {
                     
                        return Text.literal("Ficha ")
                                .formatted(Formatting.GRAY)
                                .append(Text.literal("Cassino").formatted(Formatting.RED));
                    }
                }
        );
        BLACK_SQUARE = Registry.register(
                Registries.ITEM, Identifier.of(modid, "black_square"), new Item(new Item.Settings())
        );
        RED_SQUARE = Registry.register(
                Registries.ITEM, Identifier.of(modid, "red_square"), new Item(new Item.Settings())
        );
        GREEN_SQUARE = Registry.register(
                Registries.ITEM, Identifier.of(modid, "green_square"), new Item(new Item.Settings())
        );

        BLACK_BOTTON = Registry.register(
                Registries.ITEM, Identifier.of(modid, "black_botton"), new Item(new Item.Settings())
        );
        RED_BOTTON = Registry.register(
                Registries.ITEM, Identifier.of(modid, "red_botton"), new Item(new Item.Settings())
        );
        GREEN_BOTTON = Registry.register(
                Registries.ITEM, Identifier.of(modid, "green_botton"), new Item(new Item.Settings())
        );
    }
}
