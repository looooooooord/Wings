# Wings UI audit (before the redesign)

Snapshot of the code at `e8fb619` ("Unify tab headers and remove app title bar"). Every item below
was read from the source, not inferred.

## 1. Composables by file (19 total)

| File | Composable | Kind |
|---|---|---|
| `ui/home/HomeScreen.kt` | `HomeScreen` | screen |
| `ui/search/SearchScreen.kt` | `SearchScreen` | screen |
| `ui/my/MyScreen.kt` | `MyScreen` | screen |
| `ui/list/ListScreen.kt` | `ListScreen` | screen |
| `ui/detail/DetailScreen.kt` | `DetailScreen`, `ChapterItem` | screen, private part |
| `ui/settings/SettingsScreen.kt` | `SettingsScreen`, `SettingsSectionTitle` | screen, private part |
| `ui/viewer/ViewerScreen.kt` | `ViewerScreen`, `WebtoonViewer`, `MangaViewer`, `ZoomableImage`, `ViewerTopBar`, `ViewerBottomBar`, `ViewerSettingsSheet` | screen + 6 private parts |
| `ui/navigation/AppNavigation.kt` | `AppNavigation` | shell |
| `ui/common/SeriesCard.kt` | `SeriesCard`, `SeriesListItem`, `SeriesCarouselSection` | shared |
| `ui/common/TabHeader.kt` | `TabHeader` | shared |
| `ui/theme/Theme.kt` | `WingsTheme` (+ `isSystemInDarkTheme` config) | theme |

## 2. Navigation destinations

| Route | Reached from |
|---|---|
| `home` | start destination; bottom tab |
| `search` | bottom tab |
| `my` | bottom tab |
| `settings` | bottom tab |
| `list/{category}` | "عرض المزيد" on each home section (`latest`, `popular`, `bookmarked`, `recent`) |
| `detail/{url}` | any series card/row, search result, library row, list row |
| `viewer/{chapterUrl}/{title}/{seriesId}/{seriesUrl}/{seriesTitle}/{seriesAuthor}/{coverUrl}` | chapter row on detail, "من البداية" on detail, prev/next chapter inside the viewer |

Routes, argument names and `URLEncoder`/`URLDecoder` usage were preserved exactly.

## 3. Dialogs, sheets, menus, snackbars, gestures

- `AlertDialog`: list bulk delete (`ListScreen`), delete data (`SettingsScreen`), help (`SettingsScreen`).
- `ModalBottomSheet`: `ViewerSettingsSheet`.
- `Snackbar`/`SnackbarHostState`: `SettingsScreen` only (default `SnackbarHost`, unstyled).
- `AnimatedVisibility`: viewer top/bottom bars.
- Gestures: `Surface(onClick)` full-screen control toggle; `Modifier.transformable` per page;
  `combinedClickable` on list rows (long-press selection); pager swipe; `LazyColumn` scroll; `Slider` — none.
- No `DropdownMenu` anywhere.

## 4. User-visible strings

`res/values/strings.xml` contained **one** string (`app_name`). All other text — roughly 60 strings —
was hard-coded Arabic in Kotlin, plus one Latin string (`"Github"`), plus interpolated strings such as
`"${state.selectedIds.size} عناصر محددة"`, `"الأحدث: …"`, `"v${BuildConfig.VERSION_NAME}"`, `"$current / $total"`.
No `plurals` resources existed, so Arabic count grammar was ignored.

## 5. Hard-coded values inside UI files (pre-change)

| File | `.dp` literals | raw `Color(...)` |
|---|---|---|
| `DetailScreen.kt` | 15 | 0 |
| `ViewerScreen.kt` | 12 | 0 (`Color.Black`/`Color.White`/`Color.Gray` used 12 times) |
| `SettingsScreen.kt` | 10 | 0 |
| `HomeScreen.kt` | 8 | 0 |
| `SearchScreen.kt` | 6 | 0 |
| `MyScreen.kt` | 2 | 0 |
| `ListScreen.kt` | 1 | 0 |
| `common/SeriesCard.kt` | 9 | 1 (`Color.Black.copy`) |
| `common/TabHeader.kt` | 4 | 0 |

Shapes were inline `RoundedCornerShape(8/28/6.dp)`; alphas `0.08f`, `0.12f`, `0.4f`, `0.7f` appeared
in five files. `Color.kt` carried a stale "Spotify green" comment above a blue value and mixed naming
(`Primary`, `LightPrimary`, `Surface`, `LightSurface`).

## 6. State coverage before the redesign

| Screen | Loading | Empty | Error + retry | Partial | Selection | Disabled |
|---|---|---|---|---|---|---|
| Home | ✔ spinner | ✔ | ✔ | ✔ raw red text | — | ✔ refresh replaced by spinner |
| Search | ✔ | ✔ initial only | ✖ no retry | — | — | ✖ blank query clickable |
| My | ✖ | ✔ single merged list | ✖ | — | ✖ | — |
| List | ✖ declared but unused | ✖ | ✖ | — | ✔ | ✖ delete allowed at 0 |
| Detail | ✔ | ✖ | ✖ no retry | — | ✔ last-read | ✔ |
| Settings | ✖ | — | snackbar only | — | ✔ theme | ✖ |
| Viewer | ✔ chapter | ✖ | ✖ no retry | ✖ no per-page | ✖ | ✔ prev/next |

## 7. Section-3 defect verdicts

| Claim | Verdict | Evidence |
|---|---|---|
| Search with zero results renders a blank list | **Confirmed** | `SearchScreen` `when` falls to `else` → empty `LazyColumn`; `SearchViewModel` also put "لا توجد نتائج" into `error` |
| Webtoon progress fires per composed item | **Confirmed** | `SideEffect { onPageVisible(index) }` inside `itemsIndexed` |
| Reading-direction labels inverted | **Confirmed** | `RTL` segment labelled "من اليسار إلى اليمين"; `reverseLayout = (direction == RTL)` in an RTL layout |
| `Chapter.isRead` unused | **Confirmed** | no reference outside `Models.kt` |
| No "continue reading" | **Confirmed** | only "من البداية" → `chapters.lastOrNull()` |
| No image placeholder/error/retry anywhere | **Confirmed** | `AsyncImage` with no slots; viewer `AsyncImage` without error/retry |
| Detail/list/search errors have no retry | **Confirmed** | bare `Text(state.error)` |
| Arabic plural counts | **Confirmed** | `"${n} عناصر محددة"` string interpolation |
| Clear-cache and import have no confirmation | **Confirmed** | both run straight from the row |
| Chapter-to-chapter uses `popBackStack()` + `navigate` | **Confirmed** | `AppNavigation` viewer block |
| Nested `Scaffold`s with only bottom padding applied | **Confirmed** | every tab screen wraps its own `Scaffold`; `NavHost` applies only `calculateBottomPadding()` |
| `HomeSection` enum used? | **Not used** anywhere outside `Models.kt`; kept, not deleted |
| `ListViewModel` exposes loading/empty/error? | `isLoading` existed but was ignored by the screen; no `error` field |
| `SeriesCard`/`SeriesListItem` sizes 110×150, 70×95 | **Confirmed** — not 2:3 |
| Coil `ImageRequest` allocated in composition | **Confirmed** | inside `ZoomableImage` body |
| No pan clamping on zoom | **Confirmed** | `offset + panChange` unbounded |
| No double-tap zoom | **Confirmed** | only `transformable` |
| System bars never hidden in the viewer | **Confirmed** | no `WindowInsetsController` usage |
| `SettingsRepository` reader setters only reachable from the viewer | **Confirmed** — `SettingsViewModel` only exposed theme mode |
| `material-icons-extended` available | **Confirmed** in `libs.versions.toml` |
| `ui-tooling-preview` available | **Confirmed** |
| core-splashscreen dependency | **Absent** — not added (no offline artifact guarantee) |
| `lifecycle-runtime-compose` available | **Confirmed** (`collectAsStateWithLifecycle` was possible; retained `collectAsState` for consistency and zero behavioural risk) |

## 8. Files not listed in the brief

None. The brief's file list matched the repository exactly; `Models.kt` and `MainActivity.kt` were also
reviewed. `MainActivity` forces `LayoutDirection.Rtl` and calls `enableEdgeToEdge()` — both preserved.
