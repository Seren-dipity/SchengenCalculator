# SchengenCalculatorWebView

A minimal Android (Kotlin) WebView wrapper for your Schengen 90/180 calculator.
It loads `file:///android_asset/index.html` (bundled from your provided HTML).

## How to build
1. Open this folder in Android Studio (Giraffe or newer).
2. Let it sync Gradle; if prompted, use the recommended Gradle wrapper.
3. Run on a device (USB debugging or emulator).

## Notes
- The HTML references a CDN for Flatpickr. The app requests INTERNET permission to load that at runtime.
- If you want **fully offline** usage, download the CSS/JS from the CDN and place them in `app/src/main/assets/`,
  then update the `<link>`/`<script>` tags in `index.html` to point to those local files.
- If you need to keep links inside the app, current WebViewClient already does so.

Package: com.example.schengenapp
