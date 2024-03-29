package com.skrash.book.torrent.client.peer;


import com.skrash.book.torrent.client.common.TorrentHash;

import java.nio.ByteBuffer;

/**
 * @author Sergey.Pak
 * Date: 8/9/13
 * Time: 6:40 PM
 */
public interface SharingPeerInfo {

  String getIp();

  int getPort();

  TorrentHash getTorrentHash();

  ByteBuffer getPeerId();

}
