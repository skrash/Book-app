package com.skrash.book.torrent.client;

import com.skrash.book.torrent.client.common.Peer;
import com.skrash.book.torrent.client.common.PeerUID;
import com.skrash.book.torrent.client.peer.SharingPeer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class PeersStorage {

  private volatile Peer self = null;
  private final ConcurrentHashMap<PeerUID, SharingPeer> connectedSharingPeers;

  public PeersStorage() {
    this.connectedSharingPeers = new ConcurrentHashMap<PeerUID, SharingPeer>();
  }

  public Peer getSelf() {
    return self;
  }

  public void setSelf(Peer self) {
    this.self = self;
  }

  public SharingPeer putIfAbsent(PeerUID peerId, SharingPeer sharingPeer) {
    return connectedSharingPeers.putIfAbsent(peerId, sharingPeer);
  }

  public SharingPeer removeSharingPeer(PeerUID peerId) {
    return connectedSharingPeers.remove(peerId);
  }

  public SharingPeer getSharingPeer(PeerUID peerId) {
    return connectedSharingPeers.get(peerId);
  }

  public void removeSharingPeer(SharingPeer peer) {
    connectedSharingPeers.values().remove(peer);
  }

  public Collection<SharingPeer> getSharingPeers() {
    return new ArrayList<SharingPeer>(connectedSharingPeers.values());
  }
}
