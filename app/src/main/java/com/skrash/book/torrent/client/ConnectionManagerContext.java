package com.skrash.book.torrent.client;

import java.util.concurrent.ExecutorService;

public interface ConnectionManagerContext extends ChannelListenerFactory {

  ExecutorService getExecutor();

}
