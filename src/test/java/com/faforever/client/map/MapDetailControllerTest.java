package com.faforever.client.map;

import com.faforever.client.i18n.I18n;
import com.faforever.client.notification.NotificationService;
import com.faforever.client.player.PlayerService;
import com.faforever.client.remote.domain.Player;
import com.faforever.client.reporting.ReportingService;
import com.faforever.client.test.AbstractPlainJavaFxTest;
import com.faforever.client.util.TimeService;
import com.faforever.client.vault.review.ReviewController;
import com.faforever.client.vault.review.ReviewService;
import com.faforever.client.vault.review.ReviewsController;
import com.faforever.client.vault.review.StarController;
import com.faforever.client.vault.review.StarsController;
import com.google.common.eventbus.EventBus;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MapDetailControllerTest extends AbstractPlainJavaFxTest {

  @Mock
  private MapService mapService;
  @Mock
  private NotificationService notificationService;
  @Mock
  private ReportingService reportingService;
  @Mock
  private TimeService timeService;
  @Mock
  private PlayerService playerService;
  @Mock
  private ReviewService reviewService;
  @Mock
  private I18n i18n;
  @Mock
  private ReviewsController reviewsController;
  @Mock
  private ReviewController reviewController;
  @Mock
  private StarsController starsController;
  @Mock
  private StarController starController;
  @Mock
  private EventBus eventBus;

  private MapDetailController instance;

  @Before
  public void setUp() throws Exception {
    when(mapService.downloadAndInstallMap(any(), any(DoubleProperty.class), any(StringProperty.class))).thenReturn(CompletableFuture.runAsync(() -> {
    }));
    instance = new MapDetailController(mapService, notificationService, i18n, reportingService, timeService, playerService, reviewService, eventBus);

    loadFxml("theme/vault/map/map_detail.fxml", param -> {
      if (param == ReviewsController.class) {
        return reviewsController;
      }
      if (param == ReviewController.class) {
        return reviewController;
      }
      if (param == StarsController.class) {
        return starsController;
      }
      if (param == StarController.class) {
        return starController;
      }
      return instance;
    });
  }

  @Test
  public void onInstallButtonClicked() {
    instance.onInstallButtonClicked();
    WaitForAsyncUtils.waitForFxEvents();
    assertThat(instance.uninstallButton.isVisible(), is(true));
    assertThat(instance.installButton.isVisible(), is(false));
  }

  @Test
  public void testAuthorControls() {
    when(playerService.getCurrentPlayer()).then(invocation -> {
      Player player = new Player();
      player.setLogin("axel12");
      return Optional.of(player);
    });
    MapBean mapBean = new MapBean();
    mapBean.setAuthor("axel12");
    mapBean.setIsRanked(true);
    mapBean.setIsHidden(false);
    instance.setMap(mapBean);

    assertThat(instance.hiddenRow.getPrefHeight(), not(is(0)));
    assertThat(instance.unrankButton.isVisible(), is(true));
  }

  @Test
  public void testAuthorControlsHiddenWhenPlayerNotAuthor() {
    when(playerService.getCurrentPlayer()).then(invocation -> {
      Player player = new Player();
      player.setLogin("Downlord");
      return Optional.of(player);
    });
    MapBean mapBean = new MapBean();
    mapBean.setAuthor("axel12");
    mapBean.setIsRanked(true);
    mapBean.setIsHidden(false);
    instance.setMap(mapBean);

    assertThat(instance.hiddenRow.getPrefHeight(), is(0));
    assertThat(instance.unrankButton.isVisible(), is(false));
  }
}
