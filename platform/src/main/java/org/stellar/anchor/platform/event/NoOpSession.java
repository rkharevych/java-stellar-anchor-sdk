package org.stellar.anchor.platform.event;

import org.stellar.anchor.api.event.AnchorEvent;
import org.stellar.anchor.api.exception.AnchorException;
import org.stellar.anchor.event.EventService;

import static org.stellar.anchor.util.Log.*;

public class NoOpSession implements EventService.Session {
  @Override
  public void publish(AnchorEvent event) throws AnchorException {
    info("Sending NoOp");
    debugF("Event ID={} is published to NoOpSession class.", event.getId());
  }

  @Override
  public EventService.ReadResponse read() throws AnchorException {
    debug("Reading event from NoOpSession class returns null");
    return null;
  }

  @Override
  public void ack(EventService.ReadResponse readResponse) throws AnchorException {
    read()
        .getEvents()
        .forEach(event -> debugF("Acking event ID={} to NoOpSession class.", event.getId()));
  }

  @Override
  public void close() throws AnchorException {
    debug("Closing NoOpSession class");
  }

  @Override
  public String getSessionName() {
    return "NoOpSession";
  }
}
