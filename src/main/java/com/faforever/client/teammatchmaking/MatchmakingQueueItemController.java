package com.faforever.client.teammatchmaking;

import com.faforever.client.chat.ChatService;
import com.faforever.client.chat.CountryFlagService;
import com.faforever.client.chat.avatar.AvatarService;
import com.faforever.client.fx.Controller;
import com.faforever.client.i18n.I18n;
import com.faforever.client.player.PlayerService;
import com.faforever.client.theme.UiService;
import com.jfoenix.controls.JFXButton;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import static javafx.beans.binding.Bindings.createStringBinding;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MatchmakingQueueItemController implements Controller<Node> {

  private final CountryFlagService countryFlagService;
  private final AvatarService avatarService;
  private final PlayerService playerService;
  private final TeamMatchmakingService teamMatchmakingService;
  private final UiService uiService;
  private final ChatService chatService;
  private final I18n i18n;

  @FXML
  public Node queueItemRoot;
  @FXML
  public Label queuenameLabel;
  @FXML
  public Label playersInQueueLabel;
  @FXML
  public Label teamSizeLabel;
  @FXML
  public ImageView leagueImageView;
  @FXML
  public Label queuePopTimeLabel;
  @FXML
  public JFXButton joinLeaveQueueButton;
  @FXML
  public Label refreshingLabel;

  private Timeline queuePopTimeUpdater;

  private MatchmakingQueue queue;

  public MatchmakingQueueItemController(CountryFlagService countryFlagService, AvatarService avatarService, PlayerService playerService, TeamMatchmakingService teamMatchmakingService, UiService uiService, ChatService chatService, I18n i18n) {
    this.countryFlagService = countryFlagService;
    this.avatarService = avatarService;
    this.playerService = playerService;
    this.teamMatchmakingService = teamMatchmakingService;
    this.uiService = uiService;
    this.chatService = chatService;
    this.i18n = i18n;
  }

  @Override
  public void initialize() {

  }

  @Override
  public Node getRoot() {
    return queueItemRoot;
  }

  void setQueue(MatchmakingQueue queue) {
    this.queue = queue;

    // TODO: localize
    queuenameLabel.textProperty().bind(queue.queueNameProperty());

    teamSizeLabel.textProperty().bind(createStringBinding(
        () -> i18n.get("teammatchmaking.teamSize", queue.getTeamSize()),
        queue.teamSizeProperty()));
    playersInQueueLabel.textProperty().bind(createStringBinding(
        () -> i18n.get("teammatchmaking.playersInQueue", queue.getPlayersInQueue()),
        queue.playersInQueueProperty()));

//    leagueImageView.imageProperty().bind(createObjectBinding(() -> avatarService.loadAvatar(player.getAvatarUrl()), player.avatarUrlProperty()));
    leagueImageView.setImage(avatarService.loadAvatar("https://content.faforever.com/faf/avatars/ICE_Test.png"));

    joinLeaveQueueButton.textProperty().bind(createStringBinding(
        () -> queue.isJoined() ? i18n.get("teammatchmaking.leaveQueue") : i18n.get("teammatchmaking.joinQueue"),
        queue.joinedProperty()
    ));
    joinLeaveQueueButton.defaultButtonProperty().bind(queue.joinedProperty().not());

    queue.joinedProperty().addListener(observable -> refreshingLabel.setVisible(false));

    queuePopTimeLabel.visibleProperty().bind(queue.queuePopTimeProperty().isNotNull());
    queuePopTimeUpdater = new Timeline(1, new KeyFrame(javafx.util.Duration.seconds(0), (ActionEvent event) -> {
      if (queue.getQueuePopTime() != null) {
        Instant now = Instant.now();
        Duration timeUntilPopQueue = Duration.between(now, queue.getQueuePopTime());
        if (!timeUntilPopQueue.isNegative()) {
          String formatted = i18n.get("teammatchmaking.queuePopTimer",
              timeUntilPopQueue.toMinutes(),
              timeUntilPopQueue.toSecondsPart());
          queuePopTimeLabel.setText(formatted);
          return;
        }
      }
    }), new KeyFrame(javafx.util.Duration.seconds(1)));
    queuePopTimeUpdater.setCycleCount(Timeline.INDEFINITE);
    queuePopTimeUpdater.play();

    queue.joinedProperty().addListener((Observable o) -> {
      ObservableList<String> classes = queueItemRoot.getStyleClass();
      if (queue.isJoined() && !classes.contains("card-queueJoined")) {
        classes.add("card-queueJoined");
      }
      if (!queue.isJoined()) {
        classes.remove("card-queueJoined");
      }
    });
  }

  public void onJoinLeaveQueueClicked(ActionEvent actionEvent) {
    if (queue.isJoined()) {
      teamMatchmakingService.leaveQueue(queue);
    } else {
      teamMatchmakingService.joinQueue(queue);
    }
    refreshingLabel.setVisible(true);
  }
}
