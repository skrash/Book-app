package com.skrash.book.torrent;

import com.skrash.book.torrent.client.PeerInformation;
import com.skrash.book.torrent.client.PieceInformation;

public interface PieceDownloadHandler {

    void pieceDownload(PieceInformation pieceInformation, PeerInformation peerInformation);
}
