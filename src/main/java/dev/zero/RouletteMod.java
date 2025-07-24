package dev.zero;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.network.ServerPlayerEntity;

public class RouletteMod implements ModInitializer {
	public static final String MODID = "roulettezero";

	@Override
	public void onInitialize() {
		System.out.println("[RouletteZero] Inicializando mod...");

		ModItems.register(MODID);
		ModBlocks.register(MODID);
		ModSounds.registerSounds();
		RouletteGuiDescription.register();

		// adiciona bloco no criativo
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS)
				.register(entries -> entries.add(ModBlocks.ROULETTE_BLOCK.asItem()));

		// comando /roleta
		CommandRegistrationCallback.EVENT.register((disp, reg, env) -> {
			disp.register(
					CommandManager.literal("roleta")
							.executes(ctx -> {
								ctx.getSource().getPlayer().openHandledScreen(
										new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
												(sync, inv, player) -> new RouletteGuiDescription(sync, inv),
												net.minecraft.text.Text.literal("Roleta Cassino")
										)
								);
								return 1;
							})
			);
		});

		// comando /empresa cassino
		CasinoCommand.register();

		// **Intercepta todo clique direito em blocos**
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (world.isClient) return ActionResult.PASS;

			BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
			// verifica se é o nosso bloco
			if (world.getBlockState(pos).isOf(ModBlocks.ROULETTE_BLOCK)
					&& player instanceof ServerPlayerEntity serverPlayer) {

				// dispara o /roleta como se o jogador tivesse digitado
				serverPlayer.server
						.getCommandManager()
						.executeWithPrefix(
								serverPlayer.getCommandSource(),
								"roleta"
						);

				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		});

		System.out.println("[RouletteZero] Mod carregado com sucesso!");
	}
}
