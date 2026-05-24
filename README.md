[![donation](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/donate/?hosted_button_id=KPXD92CM944RL)
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/L3L71Q7HGY)

![Dirty Detect](https://cdn.modrinth.com/data/cached_images/deb1dcd29fc64a21103211bd4486afa7eb4183f6.png)

> A lightweight Minecraft plugin that detects hack clients by exploiting the **Sign Translation 'Vulnerability'**. 

## ⚠️ Important Notice

> **Compatibility:** This plugin only works on versions **1.20.x** and newer.
> Most modern hack clients have patched this vulnerability. DirtyDetect works best against **older or less maintained clients**.


## 📖 How It Works
 
When a player joins the server, Dirty Detect immediately sends them a **sign change packet**. Hack clients bind their internal actions (such as opening a mod menu, toggling features, etc.) to real keyboard keys (for example: `Right Shift` - opens the mod menu).
 
Because these keybinds are registered inside the client, the hack client **automatically responds** to the sign packet with its bound key (e.g. `Right Shift`). A vanilla Minecraft client would never send such a response unprompted.
 
Dirty Detect intercepts this response: if a **real key** is returned instead of nothing, the player is immediately flagged and the configured action is triggered.
 
> **Note:** Most modern, actively maintained hack clients have patched this behavior. Dirty Detect is most effective against **older or less frequently updated clients**.


## ✨ Features

- 🔍 **Automatic hack client detection** via the Sign Translation 'Vulnerability'
- 📢 **Configurable notifications** - alert players with a specific permission node, or broadcast to all online players
- ⚙️ **Configurable detection action** - runs any command on the detected player
- 🏷️ **Custom prefix** for all plugin messages
- 🔧 **Command customization** - rename or alias plugin commands to your liking
- ♻️ **Live reload** - apply config changes without restarting the server


## 🛠️ Commands

| Command | Description | Permission |
|---|---|---|
| `/dirtydetect reload` | Reloads the plugin configuration | `dirtydetect.reload` |
| `/dirtydetect version` | Displays the current plugin version | `dirtydetect.version` |


## 🔐 Permissions

| Permission | Description | Default |
|---|---|---|
| `dirtydetect.reload` | Allows use of the reload command | OP |
| `dirtydetect.bypass` | Exempts a player from being detected | OP |


## ⚙️ Configuration

```yaml
# ─────────────────────────────────────────────────────────────────
#
#  MiniMessage format: https://docs.advntr.dev/minimessage/format.html
#
# ─────────────────────────────────────────────────────────────────

prefix: "<gray>[<#ff6060>Dirty Detect<gray>]<reset>"

staff-permission: "dirtydetect.staff"

# true  -> punishment announcements are sent to ALL online players
# false -> only players with the staff-permission see them
broadcast-to-all: true # Default true

# Placeholders: %player%, %mod%
messages:
  punished: " <white>Player <yellow>%player%</yellow> was caught using \
<red>%mod%</red> and has been punished."
  alert: " <gold>[ALERT] <white>Player <yellow>%player%</yellow> may be \
using <gold>%mod%</gold>."

punish:
  command: "ban %player% [Dirty Detect] Hacked client detected (%mod%)."

  mods:
    Wurst:
      key: "key.wurst.gui"

    Meteor:
      key: "key.meteor.openGui"

    LiquidBounce:
      key: "key.liquidbounce.panel"

    Aristois:
      key: "key.aristois.panel"

    Future:
      key: "key.future.clickgui"

alert:
  mods:
    OptiFine:
      key: "key.zoom"
```


## 📦 Installation

1. Download the latest `DirtyDetect.jar` from the [Releases](../../releases) page.
2. Place the `.jar` file into your server's `plugins/` folder.
3. Start or restart your server -> the plugin will generate a default `config.yml` inside `plugins/DirtyDetect/`.
4. Edit `config.yml` to your liking (notification mode, detection command, prefix, mods, etc.).
5. Run `/dirtydetect reload` to apply any changes without a restart.

---

## 📄 License

This project uses the [CC BY-NC 4.0](../../blob/main/LICENSE) license.
