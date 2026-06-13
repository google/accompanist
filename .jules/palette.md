## 2024-05-22 - [Permission Samples UX]
**Learning:** Enhancing permission rationale screens with illustrative icons and specific action-oriented button labels (e.g., "Allow camera access" vs. "Request permission") improves user trust and clarity. Using `Arrangement.spacedBy` in `Column` ensures consistent visual rhythm without manual spacers.
**Action:** Use `Icons.Default.Face` for rationale prompts and `Icons.Default.Done` with success colors for granted states in Compose-based permission flows.

## 2024-06-12 - [Hierarchical Sample Navigation]
**Learning:** In sample apps with nested categories, static TopAppBar titles and missing back buttons disorient users. Contextual titles (last segment of path) and a standard back icon improve orientation and navigation flow.
**Action:** Use `Icons.AutoMirrored.Filled.ArrowBack` for back buttons and always add `Role.Button` to clickable list items for accessibility.

## 2024-12-19 - [Navigation Feedback in Samples]
**Learning:** Interactive navigation components (NavRail, NavDrawer) in sample apps feel "broken" if they don't provide immediate feedback in the main content area. Lifting selection state and displaying the selected item's identity (icon/label) validates user interaction.
**Action:** Always provide a visual response in the main content area when a navigation item is selected, and set `contentDescription = null` for icons accompanied by labels to avoid redundant screen reader announcements.
