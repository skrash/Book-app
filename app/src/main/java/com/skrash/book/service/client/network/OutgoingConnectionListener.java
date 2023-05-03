package com.skrash.book.service.client.network;

import com.skrash.book.service.client.ConnectionListener;
import com.skrash.book.service.client.Context;
import com.skrash.book.service.client.common.TorrentHash;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class OutgoingConnectionListener implements ConnectionListener {

  private volatile DataProcessor myNext;
  private final TorrentHash torrentHash;
  private final String myRemotePeerIp;
  private final int myRemotePeerPort;
  private final Context myContext;

  public OutgoingConnectionListener(Context context,
                                    TorrentHash torrentHash,
                                    String remotePeerIp,
                                    int remotePeerPort) {
    this.torrentHash = torrentHash;
    myRemotePeerIp = remotePeerIp;
    myRemotePeerPort = remotePeerPort;
    myNext = new ShutdownProcessor();
    myContext = context;
  }

  @Override
  public void onNewDataAvailable(SocketChannel socketChannel) throws IOException {
    this.myNext = this.myNext.processAndGetNext(socketChannel);
  }

  @Override
  public void onConnectionEstablished(SocketChannel socketChannel) throws IOException {
    HandshakeSender handshakeSender = new HandshakeSender(
            torrentHash,
            myRemotePeerIp,
            myRemotePeerPort,
            myContext);
    this.myNext = handshakeSender.processAndGetNext(socketChannel);
  }

  @Override
  public void onError(SocketChannel socketChannel, Throwable ex) throws IOException {
    this.myNext.handleError(socketChannel, ex);
  }
}
