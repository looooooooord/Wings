# Wings design system notes

Companion to `UI_AUDIT.md`. Every value here exists in code under `ui/theme`.

## 1. Colour

Brand hue kept from `#1976D2`, split into a light-theme accent and a lighter dark-theme tint so
contrast works on dark surfaces.

| Token | Dark | Light |
|---|---|---|
| background | `#0F1012` | `#F6F7F9` |
| surface | `#16181B` | `#FFFFFF` |
| surfaceContainer | `#1C1F23` | `#F0F2F5` |
| surfaceContainerHigh | `#23272C` | `#FFFFFF` |
| surfaceContainerHighest | `#2A2F35` | `#E8EBEF` |
| outline | `#2E3238` | `#E1E4E8` |
| outlineVariant | `#3A3F46` | `#EDEFF2` |
| onSurface | `#E6E8EB` | `#14171A` |
| onSurfaceVariant | `#A3A9B1` | `#555B63` |
| primary | `#6AA6EB` | `#1D67C6` |
| primaryContainer | `#1D2E42` | `#DCE9FA` |
| error | `#FFB4AB` | `#B3261E` |
| success (extended) | `#7BD88F` | `#1B6E45` |
| warning (extended) | `#F3C969` | `#8A5A00` |
| lastReadTint | primary @ 14% | primary @ 10% |

Reader tokens are theme-independent because the canvas is always black:
`ReaderCanvas #000000`, `ReaderScrim #CC000000`, `ReaderOnScrim #F2F4F6`,
`ReaderOnScrimMuted #B9C0C8`, `ReaderAccent #6AA6EB`.

`WingsExtendedColors` (success / warning / lastReadTint / selectedTint) is provided through
`LocalWingsColors`; everything else fills a real `ColorScheme` role, including `inverseSurface`,
`scrim`, `surfaceDim`/`surfaceBright` and every `surfaceContainer*` level, so no Material default
lavender leaks into nav bars, sheets, dialogs, switches or ripples.

### Contrast results (WCAG relative luminance, computed)

| Pair | Ratio | Target |
|---|---|---|
| dark body `#E6E8EB` on `#0F1012` | 15.51 | ≥ 4.5 ✔ |
| dark secondary `#A3A9B1` on `#16181B` | 7.51 | ≥ 4.5 ✔ |
| dark primary `#6AA6EB` on `#16181B` | 7.00 | ≥ 4.5 ✔ |
| dark onPrimary `#08192B` on `#6AA6EB` | 6.98 | ≥ 4.5 ✔ |
| dark error on background | 11.21 | ≥ 4.5 ✔ |
| dark success on background | 10.93 | ≥ 4.5 ✔ |
| dark warning on background | 12.12 | ≥ 4.5 ✔ |
| dark last-read label `#6AA6EB` on tinted row | 5.84 | ≥ 4.5 ✔ |
| light body `#14171A` on `#F6F7F9` | 16.78 | ≥ 4.5 ✔ |
| light secondary `#555B63` on `#F6F7F9` | 6.40 | ≥ 4.5 ✔ |
| light primary `#1D67C6` on `#F6F7F9` | 5.14 | ≥ 4.5 ✔ |
| light primary on `#F0F2F5` (nav bar label) | 4.92 | ≥ 4.5 ✔ |
| light onPrimary `#FFFFFF` on `#1D67C6` | 5.52 | ≥ 4.5 ✔ |
| light error on background | 6.10 | ≥ 4.5 ✔ |
| light success on background | 5.83 | ≥ 4.5 ✔ |
| light warning on background | 5.53 | ≥ 4.5 ✔ |
| reader text on black | 19.05 | ≥ 4.5 ✔ |
| reader muted on black | 11.44 | ≥ 4.5 ✔ |
| reader accent on black | 8.27 | ≥ 4.5 ✔ |

The first light-primary candidate `#1F6FD1` measured 4.40 on `surfaceContainer`, which is below the
body-text threshold for the 12sp navigation labels, so it was deepened to `#1D67C6`. Disabled text
measures ≈2.1–2.2:1, which WCAG explicitly exempts for disabled controls; it is only used for
disabled states.

## 2. Typography

`Type.kt` defines `WingsFontFamily` (currently `FontFamily.Default`), a full Material `Typography`
and the named `WingsType` styles used by screens:

`screenTitle 24/33`, `sectionHeading 17/25`, `seriesTitleCard 13/19`, `seriesTitleList 15/22`,
`chapterTitle 14/21`, `metadataPrimary 13/19`, `metadataSecondary 12/18`, `description 15/24`,
`button 14/20`, `label 12/17`, `settingsTitle 15/22`, `settingsSupporting 12/18`, `error 14/21`,
`emptyStateTitle 17/25`, `emptyStateBody 13/20`, `readerTitle 15/21`, `readerMeta 12/18`.

Body and description line height ≥ 1.5×; titles ≥ 1.35×. `includeFontPadding = true` and
`LineHeightStyle(Trim.None)` keep Arabic diacritics and tall glyphs unclipped. Nothing is below 12sp.
No bundled Arabic font exists in the repository, so none was fabricated: swap the font by changing
the single `WingsFontFamily` line once a font file is added to `res/font`.

## 3. Spacing, sizes, shapes

Spacing scale: 2 / 4 / 8 / 12 / 16 / 20 / 24 / 32 / 48 (`Spacing`).
`Dimens`: 16dp screen padding, 24dp section spacing, 12dp item spacing, 12dp card padding,
2:3 covers everywhere, carousel card 112dp wide, list cover 72×108, detail cover 120×180,
icons 24/20, touch target 48, buttons 48 (40 compact), search field 52, chips 32, progress 3,
settings and chapter rows ≥ 56, max content width 600dp.

Shapes: 8dp small/extraSmall (covers, chips), 12dp medium (cards, fields, buttons), 20dp large,
28dp extraLarge (sheets, dialogs), pill for badges. Only tonal surfaces plus 0.5dp hairlines are
used; the floating navigation bar is the only shadowed element (`tonalElevation = 2dp`, no custom
drop shadow).

## 4. Reader: direction mapping (corrected)

The app's UI layout direction is always RTL (`MainActivity`). The reader is deliberately independent
of it: the pager and the spread row are wrapped in a **forced LTR** `LocalLayoutDirection`, so page
order depends only on the chosen reading direction.

| Setting (`ReadingDirection`) | Pager layout | `reverseLayout` | Next page appears | Spread, physical left → right |
|---|---|---|---|---|
| `RTL` (manga, right-to-left) | LTR | `true` | left | page 1 on the right, page 2 on the left |
| `LTR` (western, left-to-right) | LTR | `false` | right | page 1 on the left, page 2 on the right |

Previously `reverseLayout = (direction == RTL)` was applied inside the *app's RTL layout*, which
double-flipped the pager, and the segment labels were swapped. Both are fixed; stored enum values
and keys were not touched. The mapping is implemented as pure functions in `ViewerLogic.kt`
(`reverseLayoutFor`, `orderedSpread`) and covered by `ViewerLogicTest`.

`buildSpreads(pageCount, twoPageMode, firstPageSingle)` keeps its exact original behaviour and is
also covered by unit tests.

## 5. Screen-level conventions

- Every screen consumes insets once: the outer `Scaffold` owns the bottom bar inset, `TabHeader`
  and `WingsTopBar` own the status-bar inset, and the reader hides the system bars while its
  controls are hidden (restored on dispose).
- Loading / empty / error / partial states come from `LoadingState`, `EmptyState`, `ErrorState`,
  `InlineMessageBanner`. No screen defines its own.
- Errors are classified (`ErrorKind`) and mapped to Arabic strings; raw exception messages only go
  to `Log.e`.
- Counts use `plurals` (`selected_count`, `list_delete_body`, `detail_chapter_count`).

## 6. Reader behaviour chosen where the brief allowed options

- Manga end of chapter: no synthetic end page. The bottom bar's next/prev buttons remain the
  navigation affordance and render disabled at the ends; webtoon gets a quiet end-of-chapter footer.
- Tap zones: a single tap anywhere toggles the controls (double tap zooms). Edge-thirds page
  turning was not added because it would compete with pager swipes and pinch/pan gestures.
- Reader settings sheet max width is capped at 600dp on tablets.
- No auto-hide timer: controls stay until the user taps, so the state is predictable.
