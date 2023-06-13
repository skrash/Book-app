package com.skrash.book.torrent.client;

import java.util.concurrent.BlockingQueue;

public interface WriteAttachment {

  /**
   * @return queue for offer/peek write tasks
   */
  BlockingQueue<WriteTask> getWriteTasks();

}
