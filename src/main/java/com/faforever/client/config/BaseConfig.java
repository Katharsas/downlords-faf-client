package com.faforever.client.config;

import com.bugsnag.Bugsnag;
import com.faforever.client.reporting.ReportingService;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * This configuration has to be imported by other configurations and should only contain beans that are necessary to run
 * the application.
 */
@Slf4j
@Configuration
public class BaseConfig {

  @Bean
  ReloadableResourceBundleMessageSource messageSource() {
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setDefaultEncoding("utf-8");
    messageSource.setBasename("classpath:i18n/messages");
    messageSource.setFallbackToSystemLocale(false);
    return messageSource;
  }

  @Bean
  EventBus eventBus(ReportingService reportingService) {
    EventBus bus = new EventBus((exception, context) -> {
      log.warn("Exception in '{}#{}' while handling event: {}",
          context.getSubscriber().getClass(), context.getSubscriberMethod().getName(), context.getEvent(), exception);
      reportingService.reportError(exception);
    });
    bus.register(new DeadEventHandler());
    return bus;
  }

  @Bean
  public Bugsnag bugsnag(ClientProperties clientProperties) {
    String token = clientProperties.getBugsnagConfig().getToken();
    if (token == null) {
      log.warn("No token for bugsnag specified, bugsnag wont report errors correctly");
    }
    return new Bugsnag(token == null ? "" : token);
  }

  private static class DeadEventHandler {
    @Subscribe
    public void onDeadEvent(DeadEvent deadEvent) {
      Object unhandledEvent = deadEvent.getEvent();
      log.warn("No event handler registered for event of type '{}'", unhandledEvent.getClass().getSimpleName());
    }
  }
}
