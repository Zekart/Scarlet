package com.example.testscarler.services;

import com.example.testscarler.model.Subscriber;
import com.example.testscarler.model.Ticker;
import com.tinder.scarlet.Stream;
import com.tinder.scarlet.WebSocket;
import com.tinder.scarlet.ws.Receive;
import com.tinder.scarlet.ws.Send;

public interface GdaxServices {
    @Receive
    Stream<WebSocket.Event> observeWebSocketEvent();
    @Send
    void sendSubscribe(Subscriber subscriber);
    @Receive
    Stream<Ticker> observeTicker();
}
