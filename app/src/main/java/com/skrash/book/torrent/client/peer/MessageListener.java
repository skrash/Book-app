/**
 * Copyright (C) 2011-2012 Turn, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skrash.book.torrent.client.peer;

import com.skrash.book.torrent.client.common.protocol.PeerMessage;

import java.util.EventListener;


/**
 * EventListener interface for objects that want to receive incoming messages
 * from peers.
 *
 * @author mpetazzoni
 */
public interface MessageListener extends EventListener {

  void handleMessage(PeerMessage msg);
}
