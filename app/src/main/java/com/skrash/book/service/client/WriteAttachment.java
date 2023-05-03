package com.skrash.book.service.client;

import java.util.concurrent.BlockingQueue;

public interface WriteAttachment {

  /**
   * @return queue for offer/peek write tasks
   */
  BlockingQueue<WriteTask> getWriteTasks();

}
