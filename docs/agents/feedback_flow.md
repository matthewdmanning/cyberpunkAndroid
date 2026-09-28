# Feedback Flow Runbook

To prevent dropped balls, race conditions, and compilation errors, all agents MUST follow this strict 5-step process when deploying a new batch of UI tests to the user.

## Step 1: Sync Ratings
Before generating anything, run the pull script to ensure your local master_ratings.json is perfectly up to date with the device.
python pull_and_merge.py

## Step 2: Clear Completed Tests
Read docs/master_ratings.json. For every test that is fully graded, explicitly delete its corresponding 	est_<name>.json from cyberpunkandroid/src/main/assets/. 
*(Note: pull_and_merge.py no longer does this automatically to prevent race conditions during builds).*

## Step 3: Generate Exactly 10 Tests
Count the remaining files in ssets/. Generate new 	est_<name>.json files until there are exactly 10 files in the folder.
- Do NOT test effects that are known to be buggy (e.g., BlurMaskFilter glows on this device).
- Reference docs/test_drive_plan.md for parameter values.

## Step 4: Map the Modifiers
Open sample/src/main/java/com/example/sample/screens/FeedbackFormScreen.kt.
- Locate the al effectModifier = when { ... } block.
- Add a mapping for every new test you just generated.
- **CRITICAL:** Ensure your modifier adheres to the architectural rules in docs/agents/effects-rules.md! If it's a macro-shader, the layout MUST route it correctly (we now have isMacroEffect handling this, but be careful).

## Step 5: Verify and Deploy
Run ./gradlew :sample:installDebug.
- You MUST wait for this command to finish with BUILD SUCCESSFUL. 
- If it fails, fix the compilation error immediately. Do NOT hand off to the user if the build fails.
- Once installed, force restart the app: db shell am force-stop com.example.sample; adb shell am start -n com.example.sample/.MainActivity
- Finally, inform the user the next batch is ready.
