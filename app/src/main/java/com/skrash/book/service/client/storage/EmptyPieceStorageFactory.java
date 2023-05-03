package com.skrash.book.service.client.storage;

import com.skrash.book.service.client.common.TorrentMetadata;

import java.util.BitSet;

public class EmptyPieceStorageFactory implements PieceStorageFactory {

  public static final EmptyPieceStorageFactory INSTANCE = new EmptyPieceStorageFactory();

  private EmptyPieceStorageFactory() {
  }

  @Override
  public PieceStorage createStorage(TorrentMetadata metadata, TorrentByteStorage byteStorage) {
    return new PieceStorageImpl(
            byteStorage,
            new BitSet(metadata.getPiecesCount()),
            metadata.getPiecesCount(),
            metadata.getPieceLength()
    );
  }
}
