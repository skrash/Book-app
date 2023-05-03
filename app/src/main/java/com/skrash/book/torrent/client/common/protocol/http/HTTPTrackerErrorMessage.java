/**
 * Copyright (C) 2012 Turn, Inc.
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
package com.skrash.book.torrent.client.common.protocol.http;


import com.skrash.book.torrent.client.Constants;
import com.skrash.book.torrent.client.bcodec.BEValue;
import com.skrash.book.torrent.client.bcodec.BEncoder;
import com.skrash.book.torrent.client.bcodec.InvalidBEncodingException;
import com.skrash.book.torrent.client.common.protocol.TrackerMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


/**
 * An error message from an HTTP tracker.
 *
 * @author mpetazzoni
 */
public class HTTPTrackerErrorMessage extends HTTPTrackerMessage
        implements TrackerMessage.ErrorMessage {

  private final String reason;

  private HTTPTrackerErrorMessage(ByteBuffer data, String reason) {
    super(Type.ERROR, data);
    this.reason = reason;
  }

  @Override
  public String getReason() {
    return this.reason;
  }

  public static HTTPTrackerErrorMessage parse(BEValue decoded)
          throws IOException, MessageValidationException {
    if (decoded == null) {
      throw new MessageValidationException(
              "Could not decode tracker message (not B-encoded?)!");
    }

    Map<String, BEValue> params = decoded.getMap();

    try {
      return new HTTPTrackerErrorMessage(
              Constants.EMPTY_BUFFER,
              params.get("failure reason")
                      .getString(Constants.BYTE_ENCODING));
    } catch (InvalidBEncodingException ibee) {
      throw new MessageValidationException("Invalid tracker error " +
              "message!", ibee);
    }
  }

  public static HTTPTrackerErrorMessage craft(
          FailureReason reason) throws IOException {
    return HTTPTrackerErrorMessage.craft(reason.getMessage());
  }

  public static HTTPTrackerErrorMessage craft(String reason)
          throws IOException {
    Map<String, BEValue> params = new HashMap<String, BEValue>();
    params.put("failure reason",
            new BEValue(reason, Constants.BYTE_ENCODING));
    return new HTTPTrackerErrorMessage(
            BEncoder.bencode(params),
            reason);
  }
}
