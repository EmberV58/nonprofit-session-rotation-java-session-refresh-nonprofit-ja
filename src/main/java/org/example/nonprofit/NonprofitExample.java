package org.example.nonprofit;

import java.net.http.HttpClient;
import java.io.IOException;
import java.util.List;

public final class NonprofitExample {
    public record DonorReceipt(String donor, int cents) {}
    public record VolunteerReminder(String volunteer, String lesson) {}
    public record CampaignReport(String campaign, int receiptCount, int reminderCount) {}

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length > 0 && args[0].equals("remote")) {
            if (args.length != 3) {
                throw new IllegalArgumentException("usage: remote <refresh_token> <session_id>");
            }
            InfraiAuthClient client = InfraiAuthClient.fromEnvironment(HttpClient.newHttpClient());
            String accessToken = client.refresh(args[1]);
            client.revoke(args[2]);
            System.out.println("remote refresh succeeded; access_token=" + accessToken + ", session revoked");
            return;
        }
        SessionRotationService sessions = new SessionRotationService();
        sessions.open("session-7", "refresh-1");
        SessionRotationService.Decision rotated = sessions.refresh("session-7", "refresh-1");
        SessionRotationService.Decision reuse = sessions.refresh("session-7", "refresh-1");
        List<DonorReceipt> receipts = List.of(new DonorReceipt("Amina", 2500));
        List<VolunteerReminder> reminders = List.of(new VolunteerReminder("Kai", "send the reading link"));
        CampaignReport report = new CampaignReport("winter-books", receipts.size(), reminders.size());
        System.out.println("rotated=" + rotated.accepted() + ", reused=" + reuse.accepted() + ", revoked=" + reuse.revoked());
        System.out.println(report);
    }
}
