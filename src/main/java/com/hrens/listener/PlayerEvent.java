package com.hrens.listener;

import com.hrens.BungeeSystem;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class PlayerEvent implements Listener {
    MongoCollection<Document> bans;
    public PlayerEvent() {
        bans = BungeeSystem.getInstance().getMongodatabase().getCollection("bans");
    }

    @EventHandler
    public void onJoin(PostLoginEvent event) {
        ProxiedPlayer p = event.getPlayer();
        if (BungeeSystem.getInstance().module_bansystem && BungeeSystem.getInstance().getBanManager().isBanned(p.getUniqueId())){
            Bson bson = Filters.and(Filters.eq("type", "ban"), Filters.or(Filters.not(Filters.lt("end", System.currentTimeMillis())), Filters.eq("end", -1)), Filters.eq("bannedUUID", p.getUniqueId().toString()));
            Document document = bans.find(bson).first();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            Long end = document.getLong("end");
            String s = (end!=-1) ? formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(end), TimeZone.getDefault().toZoneId())) : BungeeSystem.getInstance().getMessageString("permanent");
            p.disconnect(TextComponent.fromLegacyText(BungeeSystem.getInstance().getMessageString("youarebanned")
                    .replace("{id}", String.valueOf(document.getInteger("_id")))
                    .replace("{reason}", BungeeSystem.getInstance().getConfig().getString("mute." + document.getInteger("reason") + ".reason"))
                    .replace("{date}", s)));
        }
    }
    @EventHandler
    public void onChat(ChatEvent e){
        ProxiedPlayer p = (ProxiedPlayer) e.getSender();
        if(BungeeSystem.getInstance().getBanManager().isMuted(p.getUniqueId())){
            e.setCancelled(true);
            Document document = bans.find(Filters.and(Filters.eq("type", "mute"), Filters.or(Filters.not(Filters.lt("end", System.currentTimeMillis())), Filters.eq("end", -1)), Filters.eq("bannedUUID", p.getUniqueId().toString()))).first();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            Long end = document.getLong("end");
            String s = (end!=-1) ? formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(end), TimeZone.getDefault().toZoneId())) : BungeeSystem.getInstance().getMessageString("permanent");
            p.sendMessage(TextComponent.fromLegacyText(BungeeSystem.getInstance().getMessageString("youaremuted")
                    .replace("{id}", String.valueOf(document.getInteger("_id")))
                    .replace("{reason}", BungeeSystem.getInstance().getConfig().getString("mute." + document.getInteger("reason") + ".reason"))
                    .replace("{date}", s)));
        }
    }
}
