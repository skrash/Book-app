package com.skrash.book.torrent.client.network;

import com.skrash.book.torrent.client.common.TorrentLoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.channels.ByteChannel;

public class ShutdownProcessor implements DataProcessor {

  private static final Logger logger = TorrentLoggerFactory.getLogger();

  @Override
  public DataProcessor processAndGetNext(ByteChannel socketChannel) throws IOException {
    DataProcessorUtil.closeChannelIfOpen(logger, socketChannel);
    return null;
  }

  @Override
  public DataProcessor handleError(ByteChannel socketChannel, Throwable e) throws IOException {
    return processAndGetNext(socketChannel);
  }
}
