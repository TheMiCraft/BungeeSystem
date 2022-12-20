package com.hrens.commands;

import com.hrens.BungeeSystem;
import com.hrens.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MuteCommand extends Command {
    public MuteCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        UUID senderUUID = commandSender instanceof ProxiedPlayer ? ((ProxiedPlayer) commandSender).getUniqueId() : null;
        if (args.length == 2) {
            if (UUIDFetcher.getUUID(args[0]) != null) {
                UUID targetUUID = UUIDFetcher.getUUID(args[0]);
                try {
                    int id = Integer.parseInt(args[1]);
                    List<Integer> muteIds = BungeeSystem.getInstance().getBanManager().getMuteIds();
                    if (!muteIds.contains(id)) {
                        commandSender.sendMessage(TextComponent.fromLegacyText(BungeeSystem.getInstance()
                                .getMessageString("idnotexist")
                                .replace("{ids}", String.join(", ", muteIds.stream().map(Object::toString)
                                        .collect(Collectors.toList())))));
                        return;
                    }
                    if (BungeeSystem.getInstance().getBanManager().canMute(commandSender, id)) {
                        BungeeSystem.getInstance().getBanManager().mute(targetUUID, id, senderUUID);
                        commandSender.sendMessage(BungeeSystem.getInstance().getMessage("playermuted"));
                    } else {
                        commandSender.sendMessage(BungeeSystem.getInstance().getMessage("notallowed"));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                commandSender.sendMessage(BungeeSystem.getInstance().getMessage("playernotfound"));
            }
        } else {
            commandSender.sendMessage(BungeeSystem.getInstance().getMessage("muteformat"));
        }
    }
}
