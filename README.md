# Study Assistant
[![Release](https://badgen.net/github/release/Paxtan-Development/study-assistant/stable)](https://github.com/Paxtan-Development/study-assistant/releases)
[![Build Status](https://drone.pcchin.com/api/badges/Paxtan-Development/study-assistant/status.svg)](https://drone.pcchin.com/Paxtan-Development/study-assistant)
[![Lines of Code](https://badgen.net/codeclimate/loc/Paxtan-Development/study-assistant)](/)
[![Maintainability](https://api.codeclimate.com/v1/badges/97cf7ed9b1087dbd5e75/maintainability)](https://codeclimate.com/github/Paxtan-Development/study-assistant/maintainability)
[![Technical Debt](https://badgen.net/codeclimate/tech-debt/Paxtan-Development/study-assistant)](https://codeclimate.com/github/Paxtan-Development/study-assistant/)

An app that help students to monitor their notes and projects.

## Installation
The apk files can be found at the [releases](https://github.com/Paxtan-Development/study-assistant/releases) page.

If you wish to compile the apk yourself, you can compile it directly from the [source code](https://github.com/Paxtan-Development/study-assistant/releases).
However, do publish the app under a different package name (Not com.pcchin.studyassistant) to avoid any conflict with this app.

If you wish to compile in a version other than debug, you would need to set up the following environment variables for keystores under a `keystore.properties` file in the root directory of the project:
- `keystoreDir`: The file containing your keystore.
- `keystoreAlias`: The alias for your keystore.
- `keystorePass`: The password to access your keystore.
- `sentryDsn`: The DSN to access your Sentry instance (Optional).

You would also need to set up the public key to access the server in a PKCS8 PEM format (2048 bits) under `app/src/main/assets/public.pem` without its headers.
A corresponding private key should be available on the server as seen [here](https://github.com/Paxtan-Development/api) with PKCS8 PEM format as well.

## Data Collection
An unique identifier, or UID, is assigned to each app to assist in error tracking.
 This UID is randomly generated and does not contain any personal or device information of the user.

Your UID and app version would be automatically recorded in the event of a crash or when a bug report is submitted.
 For beta testers, in addition to your UID and app version, your Android version and device model would be recorded as well.
 When submitting a feature suggestion, only your UID would be automatically recorded.

If you wish to submit more information about a bug or to provide feedback,
 you may do so through the **Bug Report** and **Feature Suggestion** page located in the **About** section of the app.
 If you submit a **Bug Report** manually, your UID, app version, Android Version and device model would be sent as well,
 even if you are not a beta tester.

 The name and email fields in both the **Bug Report** and **Feature Suggestion** pages are optional.

## Contribution
Any contribution is welcome, feel free to add any issues or pull requests to the repository.

Before you contribute, do read through the [Code of Conduct](/CODE_OF_CONDUCT.md).

## License
This project is licensed under the [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0) license. All licenses for the media / libraries used can be found in the About page of the compiled app.