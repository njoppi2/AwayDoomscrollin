# Security Policy

## Supported Versions

| Version | Supported |
|---|---|
| Latest stable release | Yes |
| Current `main` branch | Yes |
| Older releases | No |

## Reporting a Vulnerability

**Please do not open public issues for security vulnerabilities.**

Report security issues privately via:
- **Email (Security & Support):** support@awaydoomscrollin.com
- **General Inquiries:** info@resolvecommunity.com
- **GitLab:** Use the [confidential issue](https://gitlab.com/resolve-community/AwayDoomscrollin/-/issues/new?issue%5Bconfidential%5D=true) feature

Please include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

We will respond within **72 hours** and aim to release a fix within **14 days** for critical issues.

## Scope

This app:
- Performs screen-content analysis and blocking locally on the device
- Declares Android `INTERNET` permission only for explicit opt-in telemetry
- Keeps telemetry disabled by default and sends no request to `awaydoomscrollin.com` until the user explicitly opts in
- Generates a random per-installation UUID for telemetry that is not derived from Android ID, IMEI, MAC address, hardware, account, or advertising data
- Sends the constant `PSEUDONYMOUS_TELEMETRY` type marker, that UUID, the disclosed device/app fields, and aggregate blocking and protection measurements to `awaydoomscrollin.com` while telemetry is enabled (local streak and XP statistics are not sent)
- Allows one immediate attempt after explicit opt-in; afterward automatic app-startup and blocking-event triggers share one persisted 24-hour attempt interval, including failed attempts
- Stops future submissions when the user opts out; the latest server snapshot expires within 90 days
- Executes all detection and protection logic locally on-device without remote dependencies
- Stores blocking statistics locally via `SharedPreferences` in addition to the disclosed aggregate telemetry submissions
- Limits development-only local protection traces to structural view IDs, classifications, geometry and timings; screen text and titles, including chat and account names, must never enter those traces or network payloads

## Beta Compatibility Scope

Instagram Reels, TikTok, and YouTube Shorts detection are beta features. Third-party UI/accessibility-tree changes and manufacturer-specific Android behavior can cause missed detections or false positives. Reports about those compatibility failures are in scope as functional bugs, but the project does not claim perfect detection or universal device compatibility.

Out of scope: vulnerabilities in Android OS, third-party libraries, or user device configurations.



## Home gesture scope and asynchronous observations

Balanced Instagram can optionally block vertical drags in both directions in the uncovered Home area below the fixed toolbar. Pull-to-refresh is also blocked. Story taps and horizontal swipes are replayed after finger-up; replay can feel slower, and its brief forwarding interval is not a guarantee against every rapid gesture. This option is off by default in both release and development test builds, keeping native Home touch controls unless explicitly enabled. It does not intercept messages or recording controls, add permissions or transmit gesture data. Supported Balanced Home and Explore/search layouts use a serial background reader with bounded, conflated work; unknown or unsupported layouts retain protection and return to the existing detector.

A gesture is forwarded at most once, after a same-window Home check. Upward, ambiguous diagonal, cancelled and multi-touch gestures are consumed. Foreground, navigation, preference and lifetime changes invalidate pending work. Re-arming requires a fresh Home observation, including after Story taps. The existing accessibility permission supplies gesture dispatch; no new permission or network destination is added.

The Home input band covers the uncovered area below the toolbar. Both vertical directions and ambiguous diagonals are consumed. Geometry changes cancel pending touches, so a moving row cannot retarget a tap or sideways swipe. Permitted replay has a bounded 48 ms input-window removal handoff and a generation/preference check. No vertical gesture or refresh action is injected.

## Early Messages handoff

A fresh native click must resolve to the exact Messages tab or its icon within the previously verified tab rectangle and window. The current foreground window must still belong to Instagram, its Messages tab must be selected, and Back/active Story/Reel markers must be absent. A tap alone never qualifies. Only the outgoing content cover is parked; protected Reels and return-navigation guards remain until normal inbox verification. One bounded event hint survives the same navigation’s window updates, while each cancelled reader owns and releases its own copy. No extra navigation, input interception or idle polling is added. The current candidate has local regression coverage; device timing remains pending.
