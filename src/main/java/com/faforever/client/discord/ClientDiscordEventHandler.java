package com.faforever.client.discord;


import com.faforever.client.notification.NotificationService;
import com.faforever.client.preferences.PreferencesService;
import lombok.extern.slf4j.Slf4j;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRPC.DiscordReply;
import net.arikia.dev.drpc.DiscordUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ClientDiscordEventHandler extends DiscordEventHandlers {
  private final ApplicationEventPublisher applicationEventPublisher;
  private final NotificationService notificationService;
  private final PreferencesService preferencesService;

  public ClientDiscordEventHandler(ApplicationEventPublisher applicationEventPublisher, NotificationService notificationService, PreferencesService preferencesService) {
    this.applicationEventPublisher = applicationEventPublisher;
    this.notificationService = notificationService;
    this.preferencesService = preferencesService;
    ready = this::onDiscordReady;
    disconnected = this::onDisconnected;
    errored = this::onError;
    spectateGame = this::onSpecate;
    joinGame = this::joinGame;
    joinRequest = this::respondeOnJoinRequest;
  }

  private void respondeOnJoinRequest(DiscordUser discordUser) {
    //Always answer with yes
    if (preferencesService.getPreferences().isDisallowJoinsViaDiscord()) {
      DiscordRPC.discordRespond(discordUser.userId, DiscordReply.NO);
      return;
    }
    DiscordRPC.discordRespond(discordUser.userId, DiscordReply.YES);
  }

  private void joinGame(String s) {
    try {
      Pattern compile = Pattern.compile(DiscordRichPresenceService.JOIN_SECRET);
      Matcher matcher = compile.matcher(s);
      int gameId = Integer.parseInt(matcher.group(DiscordRichPresenceService.GAME_ID_REGEX_NAME));
      applicationEventPublisher.publishEvent(new DiscordJoinEvent(gameId));
    } catch (Exception e) {
      notificationService.addImmediateErrorNotification(e, "game.couldNotJoin", s.replace("g", ""));
      log.error("Could not join game from discord rich presence", e);
    }
  }

  private void onSpecate(String s) {
    try {
      Pattern compile = Pattern.compile(DiscordRichPresenceService.SPECTATE_SECRET);
      Matcher matcher = compile.matcher(s);
      int replayId = Integer.parseInt(matcher.group(DiscordRichPresenceService.GAME_ID_REGEX_NAME));
      int playerId = Integer.parseInt(matcher.group(DiscordRichPresenceService.PLAYER_ID_REGEX_NAME));
      applicationEventPublisher.publishEvent(new DiscordSpectateEvent(replayId, playerId));
    } catch (Exception e) {
      notificationService.addImmediateErrorNotification(e, "replay.couldNotOpen", s.replace("s", ""));
      log.error("Could not join game from discord rich presence", e);
    }
  }

  private void onError(int i, String s) {
    log.error("Discord error , {}, {}", i, s);
  }

  private void onDisconnected(int i, String s) {
    log.info("Discord disconnected , {}, {}", i, s);
  }

  private void onDiscordReady(DiscordUser discordUser) {
    log.info("Discord is ready, with user: {}", discordUser.username);
  }
}
