package com.faforever.client.reporting;

import com.bugsnag.Bugsnag;
import com.faforever.client.config.ClientProperties;
import com.faforever.client.update.Version;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ReportingService {
  private final Bugsnag bugsnag;
  private final int sentLogLines;

  public ReportingService(Bugsnag bugsnag, ClientProperties clientProperties) {
    this.bugsnag = bugsnag;
    bugsnag.setAppVersion(Version.VERSION);
    sentLogLines = clientProperties.getBugsnagConfig().getLogLinesSent();
  }

  public void reportError(Throwable e) {
    try {
      bugsnag.notify(e, report -> report.addToTab("Log", "log", readLogLines(sentLogLines)));
    } catch (Exception exception) {
      log.error("Failed to notify Bugsnag of error", exception);
    }
  }

  /**
   * reads last count log lines
   *
   * @param count the number of lines to be read
   * @return the log as String
   */
  private String readLogLines(int count) {
    List<String> lines = new ArrayList<>();
    try {
      File logFile = Paths.get(System.getProperty("logging.file")).toFile();
      if (!logFile.exists()) {
        return "";
      }
      ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(logFile, Charset.defaultCharset());
      for (int i = 0; i < count; i++) {
        String line = reversedLinesFileReader.readLine();
        if (line == null) {
          break;
        }
        lines.add(line);
      }
    } catch (Exception e) {
      log.error("Error while reading log", e);
    }
    Collections.reverse(lines);
    return lines.stream().collect(Collectors.joining("\n"));
  }

}
