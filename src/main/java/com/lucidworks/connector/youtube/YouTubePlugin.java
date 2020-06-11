package com.lucidworks.connector.youtube;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.connector.youtube.config.YouTubeConfig;
import com.lucidworks.connector.youtube.fetcher.YouTubeFetcher;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;

public class YouTubePlugin implements ConnectorPluginProvider {

  @Override
  public ConnectorPlugin get() {
    Module fetchModule = new AbstractModule() {
      @Override
      protected void configure() {

      }
    };

    return ConnectorPlugin.builder(YouTubeConfig.class)
        .withFetcher("content", YouTubeFetcher.class, fetchModule)
        .build();
  }
}