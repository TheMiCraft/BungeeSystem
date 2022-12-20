package com.hrens.commands;

import com.hrens.BungeeSystem;
import com.mongodb.client.model.Filters;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.bson.Document;

import java.util.Objects;
import java.util.UUID;

public class UnbanCommand extends Command {
    public UnbanCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        UUID senderUUID = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : null;
        if(!sender.hasPermission("serversystem.unban")) {
            sender.sendMessage(BungeeSystem.getInstance().getMessage("notallowed"));
            return;
        }
        if (strings.length >= 1) {
            try {
                int _id = Integer.parseInt(strings[0]);
                String reason = strings.length > 1 ? strings[1] : null;
                Document document = BungeeSystem.getInstance().getMongodatabase().getCollection(BungeeSystem.getInstance().getConfig().getString("mongodb.bans"))
                        .find(Filters.and(Filters.eq("_id", _id), Filters.eq("type", "ban"))).first();
                if (Objects.nonNull(document)) {
                    BungeeSystem.getInstance().getBanManager().unban(_id, senderUUID, UUID.fromString(document.getString("bannedUUID")), reason);
                    sender.sendMessage(BungeeSystem.getInstance().getMessage("unbansucceeded"));
                } else {
                    sender.sendMessage(BungeeSystem.getInstance().getMessage("unbanidnotexist"));
                }

            } catch (NumberFormatException e) {
                sender.sendMessage(BungeeSystem.getInstance().getMessage("idnumber"));
            }

        } else {
            sender.sendMessage(BungeeSystem.getInstance().getMessage("giveonearguments"));
        }
    }
}
