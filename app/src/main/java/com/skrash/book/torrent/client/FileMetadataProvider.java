package com.skrash.book.torrent.client;

import com.skrash.book.torrent.client.common.TorrentMetadata;
import com.skrash.book.torrent.client.common.TorrentParser;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class FileMetadataProvider implements TorrentMetadataProvider {

  private final String filePath;

  public FileMetadataProvider(String filePath) {
    this.filePath = filePath;
  }

  @NotNull
  @Override
  public TorrentMetadata getTorrentMetadata() throws IOException {
    File file = new File(filePath);
    return new TorrentParser().parseFromFile(file);
  }
}
