package com.lucidworks.connector.youtube.config;

import com.lucidworks.connector.youtube.config.YouTubeConfig.Properties;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;

import java.util.List;

@RootSchema(
    title = "YouTube Connector",
    description = "A connector that fetches video information from YouTube channels",
    category = "Fetcher"
)
public interface YouTubeConfig extends ConnectorConfig<Properties> {

  @Property(
      title = "Properties",
      required = true
  )
  Properties properties();

  /**
   * Connector specific settings
   */
  interface Properties extends ConnectorPluginProperties {
    @Property(
            title = "Channels to index",
            description = "The Channel id is the part that comes after channel/ in 'https://www.youtube.com/channel/UCPItOdfUk_tjlvqggkY-JsA'",
            required = true
    )
    List<String> channels();


    @Property(
            title = "OAuth Token",
            description = "Minimum required scope is 'youtube.readonly'",
            required = true
    )
    String oauthToken();

  }
}
