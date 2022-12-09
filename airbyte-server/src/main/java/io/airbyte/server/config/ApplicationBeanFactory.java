/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.server.config;

import io.airbyte.analytics.TrackingClient;
import io.airbyte.commons.features.EnvVariableFeatureFlags;
import io.airbyte.commons.features.FeatureFlags;
import io.airbyte.commons.temporal.TemporalClient;
import io.airbyte.commons.temporal.config.WorkerMode;
import io.airbyte.commons.version.AirbyteVersion;
import io.airbyte.config.Configs.DeploymentMode;
import io.airbyte.config.Configs.TrackingStrategy;
import io.airbyte.config.persistence.ConfigRepository;
import io.airbyte.config.persistence.split_secrets.JsonSecretsProcessor;
import io.airbyte.persistence.job.JobPersistence;
import io.airbyte.persistence.job.WebUrlHelper;
import io.airbyte.persistence.job.tracker.JobTracker;
import io.airbyte.server.scheduler.EventRunner;
import io.airbyte.server.scheduler.TemporalEventRunner;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Factory
public class ApplicationBeanFactory {

  @Singleton
  public Supplier<UUID> randomUUIDSupplier() {
    return () -> UUID.randomUUID();
  }

  @Singleton
  @Requires(env = WorkerMode.CONTROL_PLANE)
  public EventRunner eventRunner(final TemporalClient temporalClient) {
    return new TemporalEventRunner(temporalClient);
  }

  @Singleton
  public TrackingStrategy trackingStrategy(@Value("${airbyte.tracking-strategy}") final String trackingStrategy) {
    return convertToEnum(trackingStrategy, TrackingStrategy::valueOf, TrackingStrategy.LOGGING);
  }

  @Singleton
  public AirbyteVersion airbyteVersion(@Value("${airbyte.version}") final String airbyteVersion) {
    return new AirbyteVersion(airbyteVersion);
  }

  @Singleton
  public DeploymentMode deploymentMode(@Value("${airbyte.deployment-mode}") final String deploymentMode) {
    return convertToEnum(deploymentMode, DeploymentMode::valueOf, DeploymentMode.OSS);
  }

  @Singleton
  @Requires(env = WorkerMode.CONTROL_PLANE)
  public JobTracker jobTracker(
                               final ConfigRepository configRepository,
                               final JobPersistence jobPersistence,
                               final TrackingClient trackingClient) {
    return new JobTracker(configRepository, jobPersistence, trackingClient);
  }

  @Singleton
  @Requires(env = WorkerMode.CONTROL_PLANE)
  public WebUrlHelper webUrlHelper(@Value("${airbyte.web-app.url}") final String webAppUrl) {
    return new WebUrlHelper(webAppUrl);
  }

  @Singleton
  public FeatureFlags featureFlags() {
    return new EnvVariableFeatureFlags();
  }

  @Singleton
  @Named("workspaceRoot")
  public Path workspaceRoot(@Value("${airbyte.workspace.root}") final String workspaceRoot) {
    return Path.of(workspaceRoot);
  }

  @Singleton
  public JsonSecretsProcessor jsonSecretsProcessor(final FeatureFlags featureFlags) {
    return JsonSecretsProcessor.builder()
        .copySecrets(false)
        .build();
  }

  private <T> T convertToEnum(final String value, final Function<String, T> creatorFunction, final T defaultValue) {
    return StringUtils.isNotEmpty(value) ? creatorFunction.apply(value.toUpperCase(Locale.ROOT)) : defaultValue;
  }

}
