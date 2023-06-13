package com.skrash.book.torrent.client;

import com.skrash.book.torrent.client.peer.SharingPeer;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public interface SharingPeerFactory {

  SharingPeer createSharingPeer(String host,
                                int port,
                                ByteBuffer peerId,
                                SharedTorrent torrent,
                                ByteChannel channel,
                                String clientIdentifier,
                                int clientVersion);

}
