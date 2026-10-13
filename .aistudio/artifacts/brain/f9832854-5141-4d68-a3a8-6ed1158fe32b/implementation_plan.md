# Implementation Plan: Marks-Based Sorting & Serial Rank Badges for One-Tap Filtering

Implement obtained-marks-wise descending sorting and overall section serial number rank badges when interactive category filtering (মেধা, ১ম বিবেচনা, ২য় বিবেচনা, বিশেষ বিবেচনা) is active, while preserving standard roll-order in regular data entry mode.

---

## 1. User Review & Clarifications Confirmed

Based on your clarification responses:
1. **Serial Number (Rank) Calculation**: Overall section merit rank across all inputted students in the section (1 = highest total marks in the section, extending down to the lowest).
2. **Tie-breaking**: When total marks are tied, students are ordered by roll number ascending.
3. **Badge Styling**: English numbered badge (e.g., `#1`, `#2`) placed prominently in front of the student's name and roll on the student card.
4. **Conditional Activation**: In normal data entry mode (no category filter selected), cards remain in standard roll order without the serial rank badge.

---

## 2. Proposed Changes

### `app/src/main/java/com/abutorab/resultentry/MainActivity.kt`

#### A. Overall Merit Rank Map Calculation
- In `RosterScreen`, derive a memoized `sectionRankMap: Map<Int, Int>` computed from `sectionResults` (all entered students in the current section):
  - Filter students who have an entry (`it.entry != null`).
  - Sort by `totalMarks` descending, then `roster.roll` ascending.
  - Map each `roster.roll` to its 1-based serial rank: `rankMap[roll] = index + 1`.

#### B. Conditional Sorting for Display Results
- Update the `displayResults` calculation when `selectedCategory != null`:
  - When a category filter (মেধা, ১ম, ২য়, বিশেষ) is active:
    - Filter by category as currently done.
    - Sort the filtered list by `it.entry.totalMarks` descending, tie-breaking by `it.roster.roll` ascending.
  - In normal data entry mode (`selectedCategory == null`):
    - Maintain existing list ordering (sorted by roll number ascending).

#### C. Student Card Serial Number Display (`QuickEditStudentRow`)
- Pass `serialRank: Int?` into `QuickEditStudentRow`:
  - When `selectedCategory != null`, lookup the student's roll in `sectionRankMap` and pass the calculated serial rank.
  - When `selectedCategory == null`, pass `null`.
- In `QuickEditStudentRow`:
  - If `serialRank != null`, display a clean, high-contrast rank pill (e.g., `#1`, `#2`, `#42`) before the Roll and Name header:
    - Styled with `Surface` container using primary/tertiary tonal colors (`MaterialTheme.colorScheme.primaryContainer` or distinctive accent).
    - Bold English typography: `"#$serialRank"`.
    - Aligned seamlessly with the student name & roll text row.

---

## 3. Design & Theming Details

- **Rank Badge Appearance**:
  - Compact rounded badge (`shape = RoundedCornerShape(8.dp)`) with subtle tonal elevation or border.
  - English numeral format (e.g., `#1`, `#12`).
  - Color:
    - Light mode: Deep teal/indigo tonal container (`#E0E7FF` / `#4338CA`) or M3 `primaryContainer` / `onPrimaryContainer`.
    - Dark mode: Crisp night badge (`#312E81` / `#C7D2FE`).
- **Responsive & Spacing**:
  - Touch targets for marks, failed count, and GPA inputs remain standard 48dp.
  - The card header gracefully flexes using `Row` with `verticalAlignment = Alignment.CenterVertically`.

---

## 4. Verification Plan

### Automated Verification
- Run Gradle unit tests: `gradle :app:testDebugUnitTest`.
- Run `compile_applet` to ensure Kotlin code and Compose layouts compile cleanly without errors.

### Manual Verification Steps
1. In Roster Screen, observe normal data entry mode:
   - Cards appear sorted by Roll 1, 2, 3...
   - No serial rank badge is displayed.
2. Tap on "মেধা":
   - Cards filter to students with 0 failed subjects.
   - Cards are sorted from highest obtained marks to lowest obtained marks.
   - Students display `#1`, `#2`, etc., corresponding to their overall section merit ranking.
3. Tap on "১ম", "২য়", or "বিশেষ":
   - Cards filter to the respective failed count group.
   - Cards are sorted from highest obtained marks to lowest within the category.
   - Each card displays their section rank (e.g. `#18`, `#25`).
4. Tap the category again to reset the filter:
   - List returns to standard roll-order view.
