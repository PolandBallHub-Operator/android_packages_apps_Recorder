# Build and UI audit notes

## Sources

- LineageOS Recorder repository: https://github.com/LineageOS/android_packages_apps_Recorder
- Material 3 in Compose / Expressive overview: https://developer.android.com/develop/ui/compose/designsystems/material3
- Material 3 Expressive introduction: https://m3.material.io/blog/building-with-m3-expressive
- Android SDK command-line tools: https://developer.android.com/tools/releases/cmdline-tools

## Findings

The LineageOS repository is on the `lineage-23.2` branch and uses Kotlin with Android Gradle Plugin. It already uses Material 3 XML themes and Material Components, including an adaptive icon with a monochrome layer. The project declares compileSdk/targetSdk 36 and minSdk 31.

Material 3 Expressive is an evolution of Material 3 rather than a separate M4 system. Its guidance emphasizes dynamic color, expressive typography, varied shapes, strong hierarchy, expressive buttons/FABs/app bars, and natural motion. The current Recorder project is view/XML based, so the first implementation will use the available Material Components XML APIs and expressive theming/layout choices rather than a full Compose rewrite.

The baseline Gradle build currently cannot start because no Android SDK location is configured in the sandbox. SDK setup is required before a real APK build can be audited.

The requested fork was created through GitHub CLI as `PolandBallHub-Operator/android_packages_apps_Recorder`.

Pixel Recorder icon extraction is being performed from the supplied original APKM base.apk. The previous modified APKM is not used as the source because it was found to have resource differences; the original APKM is the authoritative source for the icon assets and comparison.

## Expressive implementation requirement

A current Material Components Android repository/release search indicates that Material 3 Expressive moved to the 1.14.0 line and exposes `Theme.Material3Expressive` theme/style families. The implementation must therefore use the Material Components 1.14.x dependency and an explicit `Theme.Material3Expressive`-family parent, not only a normal `Theme.Material3` parent. The final audit will grep both Gradle dependency and resource theme/component names.

Official references:

- https://github.com/material-components/material-components-android/releases
- https://github.com/material-components/material-components-android/blob/master/lib/java/com/google/android/material/theme/res/values/themes.xml
- https://m3.material.io/blog/building-with-m3-expressive

## Playback reference audit

The supplied PhoneArena reference shows a light recording screen with a compact top date/title row, a large pale waveform card occupying the center, a segmented waveform/transcript toggle, a prominent red recording dot with elapsed time, and two large pill actions at the bottom: Pause in a neutral tonal container and Stop in a red filled container. The new playback/recording UI should preserve this hierarchy while using explicit Material 3 Expressive styles and dynamic colors.

Reference image: https://m-cdn.phonearena.com/images/articles/435152-image/re-a.webp

## Additional reference screenshots

The playback menu reference uses a rounded bottom sheet with a drag handle, recording date/title, edit action, and grouped rows for Delete, Share, Crop & remove, Search transcript, Playback speed, and product feedback. The recording-list reference uses a compact search affordance near the top, a profile/settings affordance at the upper right, a Favorites container, month-grouped recording cards, download/action icons, durations, and a visible progress rail. The implementation will add an internal search bar with a settings button at its right edge and a settings activity for location tagging and high-quality recording. Both resources must render correctly in light and dark mode.

References:
- https://m-cdn.phonearena.com/images/articles/435149-image/rec-1.webp
- https://9to5google.com/wp-content/uploads/sites/4/2025/08/Pixel-Recorder-Material-You-1.jpg?quality=82&strip=all
