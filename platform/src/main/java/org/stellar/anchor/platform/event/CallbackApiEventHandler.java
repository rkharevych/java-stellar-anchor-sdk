package org.stellar.anchor.platform.event;

import static org.stellar.anchor.util.Log.debugF;
import static org.stellar.anchor.util.Log.traceF;

import java.io.IOException;
import org.stellar.anchor.api.callback.SendEventRequest;
import org.stellar.anchor.api.event.AnchorEvent;
import org.stellar.anchor.api.exception.AnchorException;
import org.stellar.anchor.api.exception.InvalidConfigException;
import org.stellar.anchor.apiclient.CallbackApiClient;
import org.stellar.anchor.platform.config.CallbackApiConfig;

public class CallbackApiEventHandler extends EventHandler {
  final CallbackApiClient callbackApiClient;

  CallbackApiEventHandler(CallbackApiConfig callbackApiConfig) throws InvalidConfigException {
    callbackApiClient =
        new CallbackApiClient(callbackApiConfig.buildAuthHelper(), callbackApiConfig.getBaseUrl());
  }

  @Override
  boolean handleEvent(AnchorEvent event) throws IOException {
    debugF("Sending event {} to callback API.", event.getId());
    traceF("Sending event to callback API: {}", event);

    try {
      callbackApiClient.sendEvent(SendEventRequest.from(event));
      return true;
    } catch (AnchorException e) {
      return false;
    }
  }
}
