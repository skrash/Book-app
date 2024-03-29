package com.skrash.book.torrent.client;


import java.util.concurrent.ExecutorService;

public interface Context extends SharingPeerFactory, ChannelListenerFactory {

  /**
   * @return single instance of peers storage
   */
  PeersStorage getPeersStorage();

  /**
   * @return single instance of torrents storage
   */
  TorrentsStorage getTorrentsStorage();

  /**
   * @return executor for handling incoming messages
   */
  ExecutorService getExecutor();

  /**
   * @return single instance for load torrents
   */
  TorrentLoader getTorrentLoader();

}
