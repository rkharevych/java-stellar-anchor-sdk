package org.stellar.anchor.platform.event;

import lombok.SneakyThrows;
import org.stellar.anchor.api.callback.SendEventRequest;
import org.stellar.anchor.api.event.AnchorEvent;
import org.stellar.anchor.api.exception.InvalidConfigException;
import org.stellar.anchor.apiclient.CallbackApiClient;
import org.stellar.anchor.platform.config.CallbackApiConfig;

import static org.stellar.anchor.util.Log.*;

public class CallbackApiEventHandler extends EventHandler {
  final CallbackApiClient callbackApiClient;

  CallbackApiEventHandler(CallbackApiConfig callbackApiConfig) throws InvalidConfigException {
    callbackApiClient =
        new CallbackApiClient(callbackApiConfig.buildAuthHelper(), callbackApiConfig.getBaseUrl());
  }

  @SneakyThrows
  @Override
  void handleEvent(AnchorEvent event) {
    infoF("Sending event ({}) to callback API.", event.getId());
    traceF("Sending event to callback API: {}", event);

    callbackApiClient.sendEvent(SendEventRequest.from(event));
  }
}
