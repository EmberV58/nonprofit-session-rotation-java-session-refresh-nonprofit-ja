package org.example.nonprofit;

import java.util.HashMap;
import java.util.Map;

public final class SessionRotationService {
    private final Map<String, String> activeTokens = new HashMap<>();
    private final Map<String, Boolean> revoked = new HashMap<>();

    public void open(String sessionId, String refreshToken) {
        activeTokens.put(sessionId, refreshToken);
        revoked.put(sessionId, false);
    }

    public Decision refresh(String sessionId, String presentedToken) {
        String current = activeTokens.get(sessionId);
        if (current == null || revoked.getOrDefault(sessionId, true) || !current.equals(presentedToken)) {
            revoked.put(sessionId, true);
            return new Decision(false, true, "session-revoked");
        }
        String next = presentedToken + "-rotated";
        activeTokens.put(sessionId, next);
        return new Decision(true, false, next);
    }

    public record Decision(boolean accepted, boolean revoked, String value) {}
}
