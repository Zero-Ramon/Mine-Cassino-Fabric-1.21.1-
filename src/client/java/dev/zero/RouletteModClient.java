package dev.zero;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class RouletteModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HandledScreens.register(RouletteGuiDescription.TYPE, RouletteScreen::new);
	}
}
