package com.faforever.client.discord;

import com.faforever.client.config.ClientProperties;
import com.faforever.client.game.Game;
import com.faforever.client.player.PlayerService;
import com.faforever.client.remote.domain.GameStatus;
import lombok.extern.slf4j.Slf4j;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.text.MessageFormat;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@Service
public class DiscordRichPresenceService {
  private static final String HOSTING = "Hosting";
  private static final String WAITING = "Waiting";
  private static final String PLAYING = "Playing";
  public static final String GAME_ID_REGEX_NAME = "gameId";
  public static final String PLAYER_ID_REGEX_NAME = "playerId";


  /**
   * Discord complains if we simply use the game id as a secrete that why we do the following
   */
  public static final String SPECTATE_SECRET = GAME_ID_REGEX_NAME + "=(?<" + GAME_ID_REGEX_NAME + ">\\d*);" + PLAYER_ID_REGEX_NAME + "=(?<" + PLAYER_ID_REGEX_NAME + ">\\d*)";
  public static final String JOIN_SECRET = "gameId=(?<gameId>\\d*)";

  /**
   * It is suggested(libaries github page) to look for callbacks every 5 seconds
   */
  private final int initialDelayForCallback = 5000;
  private final int periodForCallBack = 5000;
  private final ClientProperties clientProperties;

  private final PlayerService playerService;
  private final Timer timer;


  public DiscordRichPresenceService(PlayerService playerService, ClientDiscordEventHandler discordEventHandler, ClientProperties clientProperties) {
    this.playerService = playerService;
    this.clientProperties = clientProperties;
    this.timer = new Timer(true);
    try {
      DiscordRPC.discordInitialize(this.clientProperties.getDiscordConfig().getApplicationId(), discordEventHandler, true);
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          DiscordRPC.discordRunCallbacks();
        }
      }, initialDelayForCallback, periodForCallBack);
    } catch (Exception e) {
      //TODO: report to bugsnap
      log.error("Error in discord init", e);
    }
  }

  public void updatePlayedGameTo(Game game, int currentPlayerId) {
    try {
      if (game.getStatus() == GameStatus.CLOSED) {
        DiscordRPC.discordClearPresence();
      }
      DiscordRichPresence.Builder discordRichPresence = new DiscordRichPresence.Builder(getDiscordState(game));
      discordRichPresence.setDetails(MessageFormat.format("{0} | {1}", game.getFeaturedMod(), game.getTitle()));
      discordRichPresence.setParty(String.valueOf(game.getId()), game.getNumPlayers(), game.getMaxPlayers());
      discordRichPresence.setSmallImage(clientProperties.getDiscordConfig().getSmallImageKey(), "");
      discordRichPresence.setBigImage(clientProperties.getDiscordConfig().getBigImageKey(), "");
      String joinSecret = null;
      String spectateSecrete = null;
      if (game.getStatus() == GameStatus.OPEN) {
        joinSecret = JOIN_SECRET.replaceAll("\\(?<gameId>(.*)\\)", String.valueOf(game.getId()));
      }

      if (game.getStatus() == GameStatus.PLAYING) {
        spectateSecrete = SPECTATE_SECRET.replaceAll("\\(?<gameId>(.*)\\)", String.valueOf(game.getId()))
            .replaceAll("\\(\\?<playerId>(.*)\\)", String.valueOf(currentPlayerId));
      }

      discordRichPresence.setSecrets(joinSecret, spectateSecrete);

      DiscordRPC.discordUpdatePresence(discordRichPresence.build());
    } catch (Exception e) {
      //TODO: report to bugsnap
      log.error("Error reporting game status to discord", e);
    }
  }

  private String getDiscordState(Game game) {
    //I want no internationalisation in here as it should always be English
    switch (game.getStatus()) {
      case OPEN:
        boolean isHost = game.getHost().equals(playerService.getCurrentPlayer().orElseThrow(() -> new IllegalStateException("Player must have been set")).getUsername());
        return isHost ? HOSTING : WAITING;
      case PLAYING:
        return PLAYING;
    }
    return "";
  }

  @PreDestroy
  public void onDestroy() {
    DiscordRPC.discordShutdown();
    timer.cancel();
  }
}
