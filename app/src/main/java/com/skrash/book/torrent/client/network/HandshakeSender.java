package com.skrash.book.torrent.client.network;

import com.skrash.book.torrent.client.Context;
import com.skrash.book.torrent.client.Handshake;
import com.skrash.book.torrent.client.common.Peer;
import com.skrash.book.torrent.client.common.TorrentHash;
import com.skrash.book.torrent.client.common.TorrentLoggerFactory;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;

public class HandshakeSender implements DataProcessor {

  private static final Logger logger = TorrentLoggerFactory.getLogger();

  private final TorrentHash myTorrentHash;
  private final String myRemotePeerIp;
  private final int myRemotePeerPort;
  private final Context myContext;

  public HandshakeSender(TorrentHash torrentHash,
                         String remotePeerIp,
                         int remotePeerPort,
                         Context context) {
    myTorrentHash = torrentHash;
    myRemotePeerIp = remotePeerIp;
    myRemotePeerPort = remotePeerPort;
    myContext = context;
  }

  @Override
  public DataProcessor processAndGetNext(ByteChannel socketChannel) throws IOException {

    Peer self = myContext.getPeersStorage().getSelf();
    Handshake handshake = Handshake.craft(myTorrentHash.getInfoHash(), self.getPeerIdArray());
    if (handshake == null) {
      logger.warn("can not craft handshake message. Self peer id is {}, torrent hash is {}",
              Arrays.toString(self.getPeerIdArray()),
              Arrays.toString(myTorrentHash.getInfoHash()));
      return new ShutdownProcessor();
    }
    ByteBuffer messageToSend = ByteBuffer.wrap(handshake.getData().array());
    logger.trace("try send handshake {} to {}", handshake, socketChannel);
    while (messageToSend.hasRemaining()) {
      socketChannel.write(messageToSend);
    }
    return new HandshakeReceiver(
            myContext,
            myRemotePeerIp,
            myRemotePeerPort,
            true);
  }

  @Override
  public DataProcessor handleError(ByteChannel socketChannel, Throwable e) throws IOException {
    return new ShutdownProcessor().processAndGetNext(socketChannel);
  }
}
