package com.skrash.book.torrent.client;

import com.skrash.book.torrent.client.common.AnnounceableInformation;
import com.skrash.book.torrent.client.common.TorrentHash;
import com.skrash.book.torrent.client.common.TorrentMetadata;
import com.skrash.book.torrent.client.common.TorrentStatistic;
import com.skrash.book.torrent.client.storage.PieceStorage;

import org.jetbrains.annotations.NotNull;

public interface LoadedTorrent {

  /**
   * @return {@link PieceStorage} where stored available pieces
   */
  PieceStorage getPieceStorage();

  /**
   * @return {@link TorrentMetadata} instance
   * @throws IllegalStateException if unable to fetch metadata from source
   *                               (e.g. source is .torrent file and it was deleted manually)
   */
  TorrentMetadata getMetadata() throws IllegalStateException;

  /**
   * @return new instance of {@link AnnounceableInformation} for announce this torrent to the tracker
   */
  @NotNull
  AnnounceableInformation createAnnounceableInformation();

  /**
   * @return {@link TorrentStatistic} instance related with this torrent
   */
  TorrentStatistic getTorrentStatistic();

  /**
   * @return hash of this torrent
   */
  TorrentHash getTorrentHash();

  /**
   * @return related {@link EventDispatcher}
   */
  EventDispatcher getEventDispatcher();

}
