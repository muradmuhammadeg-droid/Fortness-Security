> [!CAUTION]
> This application was written by AI. Bugs and a high risk of unexpected vulnerabilities might happen. DON'T install this app if you don't want a high risk of unexpected vulnerabilities and bugs.
## Fortness Security
Fortness Security is a way to protect your system.
## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
