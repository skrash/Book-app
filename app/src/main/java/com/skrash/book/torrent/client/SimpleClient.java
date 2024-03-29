package com.skrash.book.torrent.client;

import com.skrash.book.torrent.PieceDownloadHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SimpleClient {

  private final static int DEFAULT_EXECUTOR_SIZE = 10;
  private final CommunicationManager communicationManager;

  private PieceDownloadHandler pieceDownloadHandler;

  public SimpleClient(PieceDownloadHandler pieceDownloadHandler) {
    this(DEFAULT_EXECUTOR_SIZE, DEFAULT_EXECUTOR_SIZE);
    this.pieceDownloadHandler = pieceDownloadHandler;
  }

  public SimpleClient(int workingExecutorSize, int validatorExecutorSize) {
    communicationManager = new CommunicationManager(Executors.newFixedThreadPool(workingExecutorSize), Executors.newFixedThreadPool(validatorExecutorSize));
  }

  public void stop() {
    stop(60, TimeUnit.SECONDS);
  }

  public void stop(int timeout, TimeUnit timeUnit) {
    communicationManager.stop(timeout, timeUnit);
    Exception interruptedException = null;
    boolean anyFailedByTimeout = false;
    for (ExecutorService executorService : Arrays.asList(
            communicationManager.getExecutor(),
            communicationManager.getPieceValidatorExecutor())) {
      executorService.shutdown();

      //if the thread is already interrupted don't try to await termination
      if (Thread.currentThread().isInterrupted()) continue;

      try {
        if (!executorService.awaitTermination(timeout, timeUnit)) {
          anyFailedByTimeout = true;
        }
      } catch (InterruptedException e) {
        interruptedException = e;
      }
    }
    if (interruptedException != null) {
      throw new RuntimeException("Thread was interrupted, " +
              "shutdown methods are invoked but maybe tasks are not finished yet", interruptedException);
    }
    if (anyFailedByTimeout)
      throw new RuntimeException("At least one executor was not fully shutdown because timeout was elapsed");

  }

  public void downloadTorrent(String torrentFile, String downloadDir, InetAddress iPv4Address) throws IOException, InterruptedException {
    communicationManager.start(iPv4Address);
    TorrentManager torrentManager = communicationManager.addTorrent(torrentFile, downloadDir);
    final Semaphore semaphore = new Semaphore(0);
    torrentManager.addListener(new TorrentListenerWrapper() {
      @Override
      public void pieceDownloaded(PieceInformation pieceInformation, PeerInformation peerInformation) {
        pieceDownloadHandler.pieceDownload(pieceInformation, peerInformation);
        super.pieceDownloaded(pieceInformation, peerInformation);
      }

      @Override
      public void downloadComplete() {
        semaphore.release();
      }
    });
    semaphore.acquire();
  }

}
