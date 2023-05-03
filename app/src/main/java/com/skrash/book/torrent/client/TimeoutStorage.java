package com.skrash.book.torrent.client;

import java.util.concurrent.TimeUnit;

public interface TimeoutStorage {

  void setTimeout(long millis);

  void setTimeout(int timeout, TimeUnit timeUnit);

  long getTimeoutMillis();

}
