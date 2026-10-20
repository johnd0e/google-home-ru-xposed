# TIME PLAY video findings

Source: TIME PLAY, [“Инструкция по руссификации колонки Google home/nest”](https://youtu.be/AMngzo6bUw8), published 2026-01-05. The notes below summarize the automatically generated transcript and description. The video frames and linked archive were not examined, so screen details are transcript-reported, not visually verified.

| Time | Transcript/description claim | Still needs direct verification |
|---|---|---|
| 2:01–2:18; 4:15–4:27 | Device language settings are changed to expose the desired language choice. | Which exact Android and Assistant settings affect the Home setup screen; change one factor at a time. |
| 5:19–5:47 | Remove the device from the home after setup; stop Home on the phone before adding again. | Effect on the speaker's saved state and whether this is required for the target model. |
| 7:49–8:06 | The proxy address is described as the “router IP.” | The transcript does not identify which host/interface address is intended. Do not infer or reuse an address without checking the emulator network. |
| 9:21–9:41 | Old Home asks to update; author disconnects networking and clears app data. | Exact UI commands/order and whether authentication survives. This is destructive to Home app data and may not be safe outside a clone. |
| 10:03–10:29 | Select English (United States) during setup. | Whether this reaches `set_assistant_language` on the target device. |
| 11:30–12:58 | Result varies by account; Russian Assistant language is recommended. | Account effects versus actual UI, protocol response, and speaker behavior. |
| 13:58–14:14; 14:42–16:29 | Language settings and Voice Match, especially with multiple accounts, may affect Russian behavior. | Voice Match, recognition language, spoken response, and display language are separate measurements. |

The transcript does not establish that SIM country, account age, or VPN use causes the reported behavior. The linked archive was not downloaded or run.
