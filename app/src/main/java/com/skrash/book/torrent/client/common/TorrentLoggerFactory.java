package com.skrash.book.torrent.client.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TorrentLoggerFactory {

  public static Logger getLogger() {
    return LoggerFactory.getLogger("jetbrains.torrent.Library");
  }
}
