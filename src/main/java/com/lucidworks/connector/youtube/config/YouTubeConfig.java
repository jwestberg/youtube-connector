package com.lucidworks.connector.youtube.config;

import com.lucidworks.connector.youtube.config.YouTubeConfig.Properties;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;
import com.lucidworks.fusion.schema.UIHints;

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
            required = true,
            order = 1
    )
    List<String> channels();


    @Property(
            title = "OAuth Client ID",
            description = "Your OAuth Client ID. Minimum required scope is 'youtube.readonly'",
            required = true,
            order = 2
    )
    String clientId();

    @Property(
            title = "OAuth Client secret",
            description = "Your OAuth client secret",
            required = true,
            hints = UIHints.SECRET,
            order = 3
    )
    String clientSecret();


    @Property(
            title = "OAuth Refresh Token",
            description = "Your OAuth Refresh Token",
            required = true,
            hints = UIHints.SECRET,
            order = 4
    )
    String refreshToken();
  }
}
