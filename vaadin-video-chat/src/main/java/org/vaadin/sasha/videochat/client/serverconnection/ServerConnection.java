package org.vaadin.sasha.videochat.client.serverconnection;

import org.vaadin.sasha.videochat.client.SessionInfo;
import org.vaadin.sasha.videochat.client.event.SessionDescriptionEvent;
import org.vaadin.sasha.videochat.client.event.SocketEvent;
import org.vaadin.sasha.videochat.client.event.UserLogedInEvent;
import org.vaadin.sasha.videochat.client.message.VMessage;
import org.vaadin.sasha.videochat.client.util.StringUtil;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import elemental.client.Browser;
import elemental.events.ErrorEvent;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.MessageEvent;
import elemental.html.WebSocket;
import elemental.js.util.Json;

public class ServerConnection implements SessionDescriptionEvent.Handler, UserLogedInEvent.Handler {
    
    private final EventBus eventBus;
    
    private WebSocket socket;
    
    private SessionInfo sessionInfo;
    
    @Inject
    public ServerConnection(final EventBus eventBus, final SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
        this.eventBus = eventBus;
        this.eventBus.addHandler(SessionDescriptionEvent.TYPE, this);
        this.eventBus.addHandler(UserLogedInEvent.TYPE, this);
    }

    private WebSocket createWebSocket() {
        final String wsUrl = StringUtil.prepareWsUrl(sessionInfo.getUserId());
        WebSocket ws = Browser.getWindow().newWebSocket(wsUrl);
        ws.setOnopen(new EventListener() {
            @Override
            public void handleEvent(Event evt) {

            }
        });

        ws.setOnclose(new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                GWT.log("I am CLOSED!!");
            }
        });

        ws.setOnerror(new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                Browser.getWindow().alert(((ErrorEvent) evt).getMessage());
            }
        });

        ws.setOnmessage(new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                final MessageEvent messageEvent = (MessageEvent) evt;
                final String data = String.valueOf(messageEvent.getData());
                final VMessage message = VMessage.parse(data);
                eventBus.fireEvent(new SocketEvent(message));
            }
        });

        return ws;
    }

    @Override
    public void onSessionDescriptionEvent(SessionDescriptionEvent event) {
        socket.send(Json.stringify(event.getMessage()));
    }

    @Override
    public void onUserLogedIn(UserLogedInEvent event) {
        socket = createWebSocket();
    }
}
