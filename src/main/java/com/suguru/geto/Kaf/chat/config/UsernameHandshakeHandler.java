package com.suguru.geto.Kaf.chat.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

// Minimal identity layer for the WebSocket handshake only.
// This is NOT Spring Security — it does not authenticate anyone,
// it just labels a socket connection with a name so Spring's
// SimpUserRegistry (and therefore /user destinations) has something
// real to key off, instead of the raw session-id workaround.
public class UsernameHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        String query = request.getURI().getQuery(); // e.g. "username=kabadi"
        String username = "anon-" + UUID.randomUUID();

        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2 && kv[0].equals("username")) {
                    username = kv[1];
                }
            }
        }

        final String resolvedUsername = username;
        return () -> resolvedUsername; // Principal is a functional interface — getName() only
    }
}