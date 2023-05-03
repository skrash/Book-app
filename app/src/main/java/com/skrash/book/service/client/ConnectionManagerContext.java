package com.skrash.book.service.client;

import java.util.concurrent.ExecutorService;

public interface ConnectionManagerContext extends ChannelListenerFactory {

  ExecutorService getExecutor();

}
