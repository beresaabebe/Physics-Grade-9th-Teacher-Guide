# Implementation Plan - Fix Ad Reloading and "Auto-Closing" Behavior

Address the issue where ads between list items appear to "auto-close" or disappear by implementing proper ad caching and improving ad lifecycle management.

## User Review Required

> [!IMPORTANT]
> **Ad Caching**: I will modify both `Adapter.java` and `BookDetailActivity.java` to cache loaded ads. This prevents them from reloading every time you scroll, which was likely the cause of ads "disappearing."
>
> **AppOpen Ad Timing**: I will refine the "First Launch" ad logic to ensure it doesn't interrupt the user if they've already started interacting with the app content significantly.

## Proposed Changes

### [Adapter Component]

#### [MODIFY] [Adapter.java](file:///D:/AndroidStudioProjects/Physics-Grade-9th-Teacher-Guide/app/src/main/java/com/beckytech/physicsgrade9thteacherguide/adapter/Adapter.java)
- Change the internal items list to store loaded `NativeAd` objects.
- Update `onBindViewHolder` to only load an ad if one isn't already present in the list for that position.

### [BookDetail Component]

#### [MODIFY] [BookDetailActivity.java](file:///D:/AndroidStudioProjects/Physics-Grade-9th-Teacher-Guide/app/src/main/java/com/beckytech/physicsgrade9thteacherguide/activity/BookDetailActivity.java)
- Refactor `PageAdapter` to support ad caching in its `items` list.
- Improve the `AdViewHolder` to only clear and reload if necessary.
- Add a small delay or debounce to `showRewardedAd()` to prevent rapid-fire ad triggers during fast page flipping.

### [MyApplication Component]

#### [MODIFY] [MyApplication.java](file:///D:/AndroidStudioProjects/Physics-Grade-9th-Teacher-Guide/app/src/main/java/com/beckytech/physicsgrade9thteacherguide/MyApplication.java)
- Add a timeout for the "First Launch" ad logic. If the ad takes longer than 5 seconds to load, it will not be shown automatically to avoid interrupting the user's flow.

## Verification Plan

### Manual Verification
1. **Scrolling Performance**: Verify that scrolling is smooth and ads don't "blink" or disappear when coming back into view.
2. **Ad Persistence**: Ensure ads in the list stay the same after being loaded once.
3. **Launch Ad**: Verify the AppOpen ad still works but is more predictable on slow networks.
