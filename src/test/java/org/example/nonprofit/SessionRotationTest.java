package org.example.nonprofit;

public final class SessionRotationTest {
    public static void main(String[] args) {
        SessionRotationService service = new SessionRotationService();
        service.open("session-lesson", "refresh-1");
        SessionRotationService.Decision first = service.refresh("session-lesson", "refresh-1");
        SessionRotationService.Decision reuse = service.refresh("session-lesson", "refresh-1");
        check(first.accepted(), "first refresh should rotate");
        check(first.value().equals("refresh-1-rotated"), "rotation should issue a new token");
        check(!reuse.accepted() && reuse.revoked(), "reuse should revoke the session");
        System.out.println("SessionRotationTest passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
