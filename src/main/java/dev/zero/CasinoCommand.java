
package dev.zero;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class CasinoCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        CommandManager.literal("empresa")
                                .then(CommandManager.literal("cassino")
                                        .requires(src -> src.hasPermissionLevel(2)) 
                                        .then(CommandManager.argument("owner", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    String nick = StringArgumentType.getString(ctx, "owner");
                                                    CasinoManager.setOwner(nick);
                                                    ServerCommandSource src = ctx.getSource();
                                                    
                                                    src.sendFeedback(() -> Text.literal("Proprietário do cassino definido como “" + nick + "”"), false);
                                                    return 1;
                                                })
                                        )
                                )
                )
        );
    }
}
