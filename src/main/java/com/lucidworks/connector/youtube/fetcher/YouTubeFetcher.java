package com.lucidworks.connector.youtube.fetcher;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.lucidworks.connector.youtube.config.YouTubeConfig;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YouTubeFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(YouTubeFetcher.class);

  private final List<String> channels;
  private YouTube youtube;

  private YouTubeConfig config;

  @Inject
  public YouTubeFetcher(
      YouTubeConfig config
  ) {
      channels = config.properties().channels();
      this.config = config;
  }

  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    connectClient();
    FetchInput input = fetchContext.getFetchInput();
    logger.info("Received FetchInput -> {}", input);

    if (input.getMetadata().containsKey("type"))  {
      switch(input.getMetadata().get("type").toString()) {
        case "playlist":
          String playlistId = input.getMetadata().get("playlistId").toString();
          List<String> videoIds = new ArrayList<>();
          String pageToken = null;
          if (!input.getMetadata().containsKey("pageToken")) {
            pageToken = addVideoIds(videoIds, playlistId);
          } else {
            pageToken = addVideoIds(videoIds, playlistId, input.getMetadata().get("pageToken").toString());
          }
          if (pageToken != null) {
            String p = pageToken;
            fetchContext.newCandidate(playlistId+":"+pageToken).metadata(m -> m.setString("type", "playlist").setString("pageToken", p).setString("playlistId", playlistId)).emit();
          }
          List<Map<String, Object>> videos = getVideos(videoIds);
          for (Map<String, Object> v : videos) {
            fetchContext.newDocument(v.get("id").toString()).fields(m -> m.merge(v)).emit();
          }
          break;
        default:
          logger.error("Unknown type found for input {}", input);
      }
    } else {
      logger.info("Listing uploads for channels");
      for (String channelId : channels) {

        try {
          logger.info("Requesting channelId={}", channelId);
          ChannelListResponse response = youtube.channels()
                  .list("contentDetails")
                  .setId(channelId).execute();

          if(response.isEmpty() || response.getItems() == null || response.getItems().isEmpty()) {
            logger.error("Didn't find any uploads for channel {}", channelId);
            fetchContext.newError(channelId).withError("No uploads found").emit();;
          } else {
            String uploadId = response.getItems().get(0).getContentDetails().getRelatedPlaylists().getUploads();
            fetchContext.newCandidate(uploadId).metadata(m -> m.setString("type", "playlist").setString("playlistId", uploadId)).emit();
          }
        } catch (IOException e) {
          logger.error("Unable to get uploads for channel {}", channelId, e);
          fetchContext.newError(channelId).withError(e.getMessage()).emit();
        }
      }

    }
    return fetchContext.newResult();
  }

  private List<Map<String, Object>> getVideos(List<String> videoIds) {
    try {
      VideoListResponse response = youtube.videos()
                      .list("snippet")
                      .setId(String.join(",", videoIds)).execute();
      return response.getItems().stream().map(v -> toVideo(v)).collect(Collectors.toList());
    } catch (IOException e) {
      logger.error("Unable to list videos", e);
      return Collections.emptyList();
    }
  }

  private Map<String, Object> toVideo(Video v) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", v.getId());
    map.put("kind", v.getKind());
    VideoSnippet snippet = v.getSnippet();
    addSnippet(map, snippet);

    map.put("url", "https://www.youtube.com/watch?v="+v.getId());
    return map;
  }

  private List<String> special = Arrays.asList("publishedAt", "localized", "thumbnails");
  private void addSnippet(Map<String, Object> map, VideoSnippet snippet) {
    for (String key : snippet.keySet()) {
      if (!special.contains(key)) {
        map.put(key, snippet.get(key));
      }
    }
    map.put("publishedAt", snippet.getPublishedAt().toStringRfc3339());
    map.put("thumbnailUrl", snippet.getThumbnails().getDefault().getUrl());
  }

  private String addVideoIds(List<String> videoIds, String id) {
    return addVideoIds(videoIds, id, null);
  }

  private String addVideoIds(List<String> videoIds, String id, String pageToken) {
    try {
      YouTube.PlaylistItems.List req = youtube.playlistItems()
              .list("snippet,contentDetails")
              .setPlaylistId(id);
      if (pageToken != null) {
        req.setPageToken(pageToken);
      }
      PlaylistItemListResponse response = req.execute();

      videoIds.addAll(response.getItems().stream().map(item -> item.getContentDetails().getVideoId()).collect(Collectors.toList()));

      if (response.getNextPageToken() != null) {
        return response.getNextPageToken();
      }
      return null;
    } catch (IOException e) {
      logger.error("Unable to get playlist items for id={}, pageToken={}", id, pageToken, e);
      return null;
    }
  }

  private YouTube connectClient() {
    if (youtube != null) {
      return youtube;
    }
    try {
      GoogleCredential cred = new GoogleCredential.Builder()
              .setJsonFactory(JacksonFactory.getDefaultInstance())
              .setTransport(GoogleNetHttpTransport.newTrustedTransport())
              .setClientSecrets(config.properties().clientId(), config.properties().clientSecret()).build();
      cred.setRefreshToken(config.properties().refreshToken());
      youtube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), request -> cred.initialize(request)).setApplicationName("youtube-fusion-plugin").build();
    } catch (Exception e) {
      logger.error("Couldn't connect to youtube", e);
      return null;
    }
    return youtube;
  }
}