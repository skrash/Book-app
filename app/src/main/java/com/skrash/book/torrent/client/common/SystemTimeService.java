package com.skrash.book.torrent.client.common;

public class SystemTimeService implements TimeService {

  @Override
  public long now() {
    return System.currentTimeMillis();
  }
}
