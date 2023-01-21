package com.hrens.commands;

import com.hrens.BungeeSystem;
import com.hrens.utils.UUIDFetcher;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

public class WarnCommand extends Command {
    public WarnCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        UUID senderUUID = commandSender instanceof ProxiedPlayer ? ((ProxiedPlayer) commandSender).getUniqueId() : null;
        if(!commandSender.hasPermission("bungeesystem.check")){
            commandSender.sendMessage(BungeeSystem.getInstance().getMessage("notallowed"));
            return;
        }
        if (args.length >= 1) {
            if (UUIDFetcher.getUUID(args[0]) != null) {
                UUID targetUUID = UUIDFetcher.getUUID(args[0]);
                StringBuilder reason = new StringBuilder();
                for(int i = 1; i < args.length; i++){
                    reason.append(args[i]).append(" ");
                }
                BungeeSystem.getInstance().getBanManager().warn(targetUUID, args.length != 1 ? reason.toString() : null, senderUUID);
                commandSender.sendMessage(BungeeSystem.getInstance().getMessage("playerwarned"));
            } else {
                commandSender.sendMessage(BungeeSystem.getInstance().getMessage("playernotfound"));
            }
        } else {
            commandSender.sendMessage(BungeeSystem.getInstance().getMessage("warnformat"));
        }
    }
}
