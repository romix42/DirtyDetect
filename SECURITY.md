# Security Policy

## Supported Versions

Only the latest release of DirtyDetect receives security updates.  
Older versions are **not** supported and may contain unpatched vulnerabilities.

| Version | Supported |
|---------|-----------|
| Latest  | ✅ Yes    |
| Older   | ❌ No     |

DirtyDetect requires **Minecraft 1.20.x or newer**. Using the plugin on unsupported
server versions is outside the scope of any security support.

---

## Reporting a Vulnerability

> ⚠️ **Please do NOT open a public GitHub issue for security vulnerabilities.**

If you discover a security vulnerability in DirtyDetect, follow these steps:

1. **Open a [GitHub Security Advisory](../../security/advisories/new)** — this keeps the report private until it is resolved.
2. Alternatively, email the maintainer directly (see the profile page for contact details).
3. Include as much detail as possible:
   - A clear description of the vulnerability
   - Steps to reproduce the issue
   - Potential impact (e.g. detection bypass, server crash, privilege escalation)
   - Your suggested fix, if you have one
   - If possible, notify me through discord (DMs, tickets).

You can expect an **acknowledgement within 72 hours** and a resolution or status update
within **14 days**, depending on complexity.

Please allow reasonable time to patch the issue before any public disclosure
(**coordinated disclosure**).

---

## Scope

The following are **in scope** for security reports:

- Detection bypass techniques that allow hack clients to go undetected
- Privilege escalation via plugin commands or permissions
- Server crashes or denial-of-service caused by the plugin
- Unintended remote code execution triggered by plugin logic
- Bypass of the `dirtydetect.bypass` permission node

The following are **out of scope**:

- Hack clients patching the Sign Translation vulnerability on their end — this is expected and documented behavior
- Issues on server versions below 1.20.x — unsupported versions are not covered
- General Minecraft server misconfigurations unrelated to this plugin

---

## Security Considerations for Server Admins

When deploying DirtyDetect, keep these points in mind:

- **Limit OP carefully.** The `dirtydetect.reload` and `dirtydetect.bypass` permissions default to OP. Grant them only to trusted staff.
- **Review the punishment command.** The `punish.command` value in `config.yml` runs with server-level permissions. Avoid commands that could be exploited if the `%player%` or `%mod%` placeholders contain unexpected input.
- **Keep the plugin updated.** Always use the latest release from the [Releases](../../releases) page to benefit from the most recent fixes.
- **Restrict config access.** The `plugins/DirtyDetect/config.yml` file should only be writable by trusted administrators, as it controls punishment commands.

---

## License Note

This project is licensed under [CC BY-NC 4.0](../../blob/main/LICENSE).  
Security researchers are permitted to test and analyze the plugin for non-commercial
vulnerability research purposes under the terms of this license.
