# Romantic App - Android Application

Made for my gf to automate my response to her asking if I love her.

## Features

- **First Button**: "Ma mai iubesti?" (Do you still love me?)
  - When pressed, displays a big "DA" (Yes) with hearts and confetti animation for 5 seconds
  
- **Second Button**: Appears after the first animation
  - Shows "Cat de mult?" (How much?)
  - When pressed, displays "DA" with hearts and confetti for 5 seconds
  
- **Auto Reset**: After 10 seconds total, the app resets to the original screen

## Technical Details

- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **UI**: Material Design Components
- **Animations**: Custom confetti view and floating heart animations

## Project Structure

```
app/
├── src/main/
│   ├── java/com/romanticapp/love/
│   │   ├── MainActivity.kt          # Main activity handling UI logic
│   │   └── ConfettiView.kt          # Custom confetti animation view
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml    # Main layout
│   │   └── values/
│   │       ├── strings.xml          # String resources
│   │       ├── colors.xml           # Color resources
│   │       └── themes.xml           # App theme
│   └── AndroidManifest.xml
└── build.gradle.kts                 # App build configuration
```

