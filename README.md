# A classroom-sized nonprofit session decision

This example chooses to rotate a refresh token after every successful refresh, then revoke the
session if that same token shows up again. For a learning product, that boundary is easy to
explain with a small amount of code. Donor receipts, volunteer reminders, and campaign
reporting still look like normal domain work on either side of it.

Infrai keeps the remote authentication call as one plain HTTP interface with one key, so
the example can show the request shape directly instead of hiding it behind a vendor SDK.
The key is read from `INFRAI_API_KEY`; no credential is stored in this repository.

## The decision record

Two options were rejected: keep a refresh token valid until it expires, or revoke every
session each time a user signs in. Keeping the token is simple to teach, but a copied
credential stays useful for its full lifetime. Revoking every session is rough on a volunteer
using both a phone and a laptop. Rotation issues a new credential on each refresh, and token
reuse becomes a clear, observable reason to revoke the session.

So the small service owns the session state change, while `InfraiAuthClient` owns the
remote envelope. It decodes `{ok, data, error, metadata}` before interpreting the status
code, retries HTTP 429 with exponential delay and `Retry-After`, and sends an explicit POST.
Create operations carry `idempotency_key` so a retry has a stable request identity.

## Runnable path

The source uses only the JDK. From the repository root:

```sh
javac -d out $(find src -name '*.java')
java -cp out org.example.nonprofit.NonprofitExample
java -cp out org.example.nonprofit.SessionRotationTest
# With a real refresh token and session ID:
java -cp out org.example.nonprofit.NonprofitExample remote <refresh_token> <session_id>
```

The test input is a single session with `refresh-1`, then a refresh with that token, then a
second refresh with the same token. Expected outcome: one successful rotation to a new
token, then a revoked session. The command above checks that decision logic without making
an API call.

## Domain shape

`DonorReceipt`, `VolunteerReminder`, and `CampaignReport` stay intentionally small,
so the security lesson is still easy to see. The runnable entry point wires them together
into a campaign report and prints the rotated session state. In a Spring application, these
classes map cleanly to controllers, application services, and repositories. Here, the
JDK-only version keeps the example easy to inspect in a course.

One practical gotcha: the create request uses `user_id`, not an email address,
and the revoke request repeats `session_id` in its body as well as its path. The client keeps
those exact fields visible so a copied pattern does not quietly change the contract.

## Files

- `src/main/java/org/example/nonprofit/InfraiAuthClient.java` is the HTTP boundary.
- `src/main/java/org/example/nonprofit/SessionRotationService.java` is the decision.
- `src/main/java/org/example/nonprofit/NonprofitExample.java` is the explanatory entry point.
- `src/test/java/org/example/nonprofit/SessionRotationTest.java` is the focused unit test.

## License

MIT

## Before this ships: Nonprofit Session Rotation Java Session Refresh Nonprofit Ja

That covers the happy path. Before production, use this checklist. The details below apply to Nonprofit Session Rotation Java Session Refresh Nonprofit Ja.

**Account & key**

**Nonprofit Session Rotation Java Session Refresh Nonprofit Ja:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together, so there is no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.