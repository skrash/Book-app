package com.skrash.book.service.client.common.protocol;


import com.skrash.book.service.client.common.Peer;

import java.util.List;

/**
 * Base interface for announce response messages.
 *
 * <p>
 * This interface must be implemented by all subtypes of announce response
 * messages for the various tracker protocols.
 * </p>
 *
 * @author mpetazzoni
 */
public interface AnnounceResponseMessage {

  int getInterval();

  int getComplete();

  int getIncomplete();

  List<Peer> getPeers();
}
