package com.faforever.client.map.generator;

import org.apache.maven.artifact.versioning.ComparableVersion;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeneratorCommandBuilder {

  private Path generatorExecutableFile;
  private ComparableVersion version;
  private String mapFilename;
  private Integer spawnCount;
  private Integer mapSize;
  private String seed;
  private Float landDensity;
  private Float plateauDensity;
  private Float mountainDensity;
  private Float rampDensity;
  private Float mexDensity;
  private Float reclaimDensity;
  private GenerationType generationType;

  public static GeneratorCommandBuilder create() {
    return new GeneratorCommandBuilder();
  }

  public GeneratorCommandBuilder generatorExecutableFilePath(Path generatorExecutableFile) {
    this.generatorExecutableFile = generatorExecutableFile;
    return this;
  }

  public GeneratorCommandBuilder version(ComparableVersion version) {
    this.version = version;
    return this;
  }

  public GeneratorCommandBuilder mapFilename(String mapFilename) {
    this.mapFilename = mapFilename;
    return this;
  }

  public GeneratorCommandBuilder spawnCount(Integer spawnCount) {
    this.spawnCount = spawnCount;
    return this;
  }

  public GeneratorCommandBuilder mapSize(Integer mapSize) {
    this.mapSize = mapSize;
    return this;
  }

  public GeneratorCommandBuilder seed(String seed) {
    this.seed = seed;
    return this;
  }

  public GeneratorCommandBuilder landDensity(Float landDensity) {
    this.landDensity = landDensity;
    return this;
  }

  public GeneratorCommandBuilder plateauDensity(Float plateauDensity) {
    this.plateauDensity = plateauDensity;
    return this;
  }

  public GeneratorCommandBuilder mountainDensity(Float mountainDensity) {
    this.mountainDensity = mountainDensity;
    return this;
  }

  public GeneratorCommandBuilder rampDensity(Float rampDensity) {
    this.rampDensity = rampDensity;
    return this;
  }

  public GeneratorCommandBuilder mexDensity(Float mexDensity) {
    this.mexDensity = mexDensity;
    return this;
  }

  public GeneratorCommandBuilder reclaimDensity(Float reclaimDensity) {
    this.reclaimDensity = reclaimDensity;
    return this;
  }

  public GeneratorCommandBuilder generationType(GenerationType generationType) {
    this.generationType = generationType;
    return this;
  }

  public List<String> build() {
    String javaPath = Paths.get(System.getProperty("java.home")).resolve("bin").resolve(org.bridj.Platform.isWindows() ? "java.exe" : "java").toAbsolutePath().toString();
    if (generatorExecutableFile == null) {
      throw new IllegalStateException("Map generator path not set");
    }
    if (version.compareTo(new ComparableVersion("1")) >= 0) {
      if (mapFilename == null && (mapSize == null || spawnCount == null || generationType == null)) {
        throw new IllegalStateException("Map generation parameters not properly set");
      }

      List<String> command;

      if (mapFilename == null) {
        command = new ArrayList<>(List.of(javaPath, "-jar", generatorExecutableFile.toAbsolutePath().toString(),
            "--map-size", mapSize.toString(), "--spawn-count", spawnCount.toString()));

        switch (generationType) {
          case BLIND -> command.add("--blind");
          case TOURNAMENT -> command.add("--tournament-style");
          case UNEXPLORED -> command.add("--unexplored");
          default -> {
          }
        }

        if (landDensity != null) {
          command.addAll(Arrays.asList("--land-density", landDensity.toString()));
        }
        if (mountainDensity != null) {
          command.addAll(Arrays.asList("--mountain-density", mountainDensity.toString()));
        }
        if (plateauDensity != null) {
          command.addAll(Arrays.asList("--plateau-density", plateauDensity.toString()));
        }
        if (rampDensity != null) {
          command.addAll(Arrays.asList("--ramp-density", rampDensity.toString()));
        }
        if (mexDensity != null) {
          command.addAll(Arrays.asList("--mex-density", mexDensity.toString()));
        }
        if (reclaimDensity != null) {
          command.addAll(Arrays.asList("--reclaim-density", reclaimDensity.toString()));
        }
      } else {
        command = new ArrayList<>(List.of(javaPath, "-jar", generatorExecutableFile.toAbsolutePath().toString(),
            "--map-name", mapFilename));
      }

      return command;
    } else {
      return Arrays.asList(javaPath, "-jar", generatorExecutableFile.toAbsolutePath().toString(), ".",
          String.valueOf(seed), version.toString(), mapFilename);
    }
  }
}
