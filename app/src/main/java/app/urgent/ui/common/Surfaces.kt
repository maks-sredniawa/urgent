package app.urgent.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import app.urgent.R
import app.urgent.theme.Spacing

/**
 * The shape every detail screen takes: a top bar with a title and a back arrow, and content below.
 *
 * Extracted because three screens had it and the fourth got the padding subtly wrong. A shared
 * scaffold is not about saving lines — it is about the app looking like one app.
 *
 * `onBack` is nullable rather than defaulted to `{}`: a screen with no back arrow and a screen whose
 * back arrow does nothing look identical in a screenshot and are completely different bugs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScaffold(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                // AutoMirrored so the arrow points the other way in a right-to-left
                                // locale. The manifest declares `supportsRtl`, which makes this the
                                // app's problem rather than the platform's.
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    }
                },
                actions = { actions() },
            )
        },
    ) { insets ->
        content(Modifier.padding(insets))
    }
}

/**
 * What a list shows when it is empty.
 *
 * **Say what to do, not that there is nothing.** "No items yet" tells the user what they can already
 * see; "Tap + to add your first item" tells them what to do about it.
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.block),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * **Something Android needs before Urgent can work, said as what the user loses, with one button to
 * the fix.** Amber rather than error: the phone is missing a grant, Urgent has not failed.
 *
 * The idiom for a permission or a system setting the app **can re-read**: it appears only while the
 * state is actually wrong and clears itself on the resume after the user comes back, because it is
 * re-read rather than remembered. A grant the app **cannot** read back, such as a vendor's autostart
 * toggle (`work/Autostart.kt`), gets a permanent [LinkRow] instead: a card that could never clear
 * would assert a state the app has no way to observe.
 */
@Composable
fun WarningCard(
    icon: Painter,
    title: String,
    body: String,
    actionLabel: String,
    onAct: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Spacing.base)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.snug)) {
                Icon(painter = icon, contentDescription = null)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.tight),
                ) {
                    Text(text = title, style = MaterialTheme.typography.titleSmall)
                    Text(text = body, style = MaterialTheme.typography.bodyMedium)
                }
            }
            TextButton(
                onClick = onAct,
                colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current),
                modifier = Modifier.align(Alignment.End).padding(top = Spacing.tight),
            ) {
                Text(actionLabel)
            }
        }
    }
}

/**
 * The M3 switch with a check in its thumb while on, so on and off differ in shape as well as colour.
 *
 * `onCheckedChange = null` makes it display-only: the row or card around it carries the
 * `toggleable`, so a screen reader announces one switch with its label instead of a bare control.
 */
@Composable
fun CheckSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = null,
        enabled = enabled,
        modifier = modifier,
        thumbContent =
            if (checked) {
                { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(SwitchDefaults.IconSize)) }
            } else {
                null
            },
    )
}

/**
 * A title, a line of supporting text, and a switch — **and the whole row is the target**.
 *
 * A 48dp strip the width of the screen is far easier to hit than a 32dp control at the edge of it.
 *
 * **`enabled = false` draws the row inert rather than hiding it**: a control the app cannot honour on
 * this particular phone leaves the user unable to tell the app from an app that behaves differently
 * on their phone than on someone else's.
 */
@Composable
fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onChange)
                .padding(horizontal = Spacing.base, vertical = Spacing.snug),
        horizontalArrangement = Arrangement.spacedBy(Spacing.base),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowText(title, subtitle, Modifier.weight(1f))
        CheckSwitch(checked = checked, enabled = enabled)
    }
}

/** Where a [LinkRow] goes, which decides its trailing icon. */
enum class LinkTarget {
    Inside,
    Outside,

    /** Nowhere: an action done on this screen, at most behind a dialog. No icon. */
    Here,
}

/**
 * A row that goes somewhere. A row that **leaves** the app takes a subtitle saying where, because a
 * tap that hands the user to another app with no warning is the kind they back out of and never
 * repeat.
 */
@Composable
fun LinkRow(
    title: String,
    target: LinkTarget,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = Spacing.base, vertical = Spacing.snug),
        horizontalArrangement = Arrangement.spacedBy(Spacing.base),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowText(title, subtitle, Modifier.weight(1f))
        val icon =
            when (target) {
                LinkTarget.Inside -> rememberVectorPainter(Icons.AutoMirrored.Filled.KeyboardArrowRight)
                LinkTarget.Outside -> painterResource(R.drawable.ic_open_in_new)
                LinkTarget.Here -> null
            }
        if (icon != null) {
            Icon(painter = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RowText(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.hair)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * One option of a single-select group, with the leading check M3 gives a selected filter chip so the
 * choice reads by shape and not by fill alone.
 */
@Composable
fun ChoiceChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        leadingIcon =
            if (selected) {
                { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
            } else {
                null
            },
        border =
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = MaterialTheme.colorScheme.outline,
            ),
    )
}

/**
 * A chip that does something once and holds no selection. Looks like an unselected [ChoiceChip], but
 * a screen reader announces it as a button rather than a checkbox.
 */
@Composable
fun ActionChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = MaterialTheme.colorScheme.outline),
    )
}

@Composable
fun ChipGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier = modifier.padding(horizontal = Spacing.base),
        horizontalArrangement = Arrangement.spacedBy(Spacing.tight),
    ) {
        content()
    }
}

/**
 * A section heading.
 *
 * The padding is the rhythm rule from [Spacing]: [Spacing.section] above, [Spacing.tight] below, so
 * the header reads as attached to what follows. [top] exists for the one header that follows the
 * hero block, which takes [Spacing.block].
 */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    top: Dp = Spacing.section,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            modifier.padding(
                start = Spacing.base,
                end = Spacing.base,
                top = top,
                bottom = Spacing.tight,
            ),
    )
}
