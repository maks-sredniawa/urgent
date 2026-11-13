package app.urgent.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.urgent.R
import app.urgent.call.CALL_PERMISSIONS
import app.urgent.call.ChosenCaller
import app.urgent.call.hasCallPermissions
import app.urgent.call.hasContactsPermission
import app.urgent.data.CallerRules
import app.urgent.data.ContactScope
import app.urgent.data.PAUSE_HOUR_CHOICES
import app.urgent.data.REPEAT_WINDOW_CHOICES
import app.urgent.theme.Spacing
import app.urgent.ui.appViewModelExtras
import app.urgent.ui.common.ActionChip
import app.urgent.ui.common.CheckSwitch
import app.urgent.ui.common.ChipGroup
import app.urgent.ui.common.ChoiceChip
import app.urgent.ui.common.LinkRow
import app.urgent.ui.common.LinkTarget
import app.urgent.ui.common.Mark
import app.urgent.ui.common.SectionHeader
import app.urgent.ui.common.SwitchRow
import app.urgent.ui.common.WarningCard
import app.urgent.ui.common.startActivitySafely
import app.urgent.work.hasAutostartSettings
import app.urgent.work.isIgnoringBatteryOptimisations
import app.urgent.work.openAutostartSettings
import app.urgent.work.openBatteryOptimisationSettings
import java.text.Collator
import java.util.Date

/**
 * The whole app, really: the switch, the window, and everything Android or the ROM must allow before
 * the switch can do anything.
 *
 * Every grant below is a **live read re-taken on each resume**, because the fix for each one is a
 * system screen this one hands the user to, and they come back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory, extras = appViewModelExtras()),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var callPermissions by remember { mutableStateOf(context.hasCallPermissions()) }
    var batteryExempt by remember { mutableStateOf(context.isIgnoringBatteryOptimisations()) }
    var contactsGranted by remember { mutableStateOf(context.hasContactsPermission()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    callPermissions = context.hasCallPermissions()
                    batteryExempt = context.isIgnoringBatteryOptimisations()
                    contactsGranted = context.hasContactsPermission()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val askPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            callPermissions = context.hasCallPermissions()
        }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.base),
                    ) {
                        Mark(
                            width = 32.dp,
                            dots = MaterialTheme.colorScheme.onSurface,
                            ring = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(stringResource(R.string.app_name))
                    }
                },
            )
        },
    ) { insets ->
        Column(modifier = Modifier.padding(insets).verticalScroll(rememberScrollState())) {
            Text(
                text = stringResource(R.string.home_intro),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = Spacing.base, end = Spacing.base, top = Spacing.tight),
            )

            RingThroughCard(
                checked = state.ringThrough,
                paused = state.pausedUntilMillis != null,
                onChange = { enabled ->
                    viewModel.setRingThrough(enabled)
                    // The ask rides on the moment the feature is switched on, never at launch:
                    // Android allows two denials before the dialog stops appearing for good.
                    if (enabled && !callPermissions) askPermissions.launch(permissionsToAsk())
                },
                modifier = Modifier.padding(start = Spacing.base, end = Spacing.base, top = Spacing.section),
            )

            if (state.ringThrough) {
                PauseControl(
                    pausedUntilMillis = state.pausedUntilMillis,
                    onPause = viewModel::pauseFor,
                    onResume = viewModel::resume,
                    modifier = Modifier.padding(top = Spacing.snug),
                )
            }

            // Only while the switch is on: a warning about a grant a disabled feature would need is
            // the app asking for something it is not using.
            val callsBlocked = state.ringThrough && !callPermissions
            val batteryBlocked = state.ringThrough && !batteryExempt
            if (callsBlocked || batteryBlocked) {
                Column(
                    modifier = Modifier.padding(start = Spacing.base, end = Spacing.base, top = Spacing.section),
                    verticalArrangement = Arrangement.spacedBy(Spacing.snug),
                ) {
                    if (callsBlocked) {
                        WarningCard(
                            icon = painterResource(R.drawable.ic_warning_lock),
                            title = stringResource(R.string.home_permissions_missing_title),
                            body = stringResource(R.string.home_permissions_missing_body),
                            actionLabel = stringResource(R.string.home_permissions_missing_action),
                            onAct = { context.openAppDetails() },
                        )
                    }
                    if (batteryBlocked) {
                        WarningCard(
                            icon = painterResource(R.drawable.ic_warning_battery),
                            title = stringResource(R.string.home_battery_title),
                            body = stringResource(R.string.home_battery_body),
                            actionLabel = stringResource(R.string.home_battery_action),
                            onAct = { context.openBatteryOptimisationSettings() },
                        )
                    }
                }
            }

            SectionHeader(stringResource(R.string.home_window), top = Spacing.block)
            ChipGroup {
                for (minutes in REPEAT_WINDOW_CHOICES) {
                    ChoiceChip(
                        selected = state.windowMinutes == minutes,
                        label = pluralStringResource(R.plurals.home_window_minutes, minutes, minutes),
                        onClick = { viewModel.setWindowMinutes(minutes) },
                    )
                }
            }

            Spacer(Modifier.height(Spacing.section))
            SwitchRow(
                title = stringResource(R.string.home_count_declined),
                subtitle = stringResource(R.string.home_count_declined_hint),
                checked = state.countDeclined,
                onChange = viewModel::setCountDeclined,
            )
            SwitchRow(
                title = stringResource(R.string.home_keep_alive),
                subtitle = stringResource(R.string.home_keep_alive_hint),
                checked = state.keepAlive,
                onChange = viewModel::setKeepAlive,
            )

            // Xiaomi's grant has no readable state, so it is a permanent row that claims nothing
            // rather than a card that could never clear (work/Autostart.kt).
            if (state.ringThrough && context.hasAutostartSettings()) {
                SectionHeader(stringResource(R.string.home_xiaomi))
                LinkRow(
                    title = stringResource(R.string.home_autostart),
                    subtitle = stringResource(R.string.home_autostart_hint),
                    target = LinkTarget.Outside,
                    onClick = { context.openAutostartSettings() },
                )
            }
            CallersSection(
                callers = state.callers,
                contactsGranted = contactsGranted,
                ringThrough = state.ringThrough,
                onUseChosen = viewModel::setUseChosenCallers,
                onContactScope = viewModel::setContactScope,
                onAddTyped = viewModel::addChosenCaller,
                loadContacts = viewModel::contactCallers,
                onAddFromContacts = viewModel::addChosenCallerFromContacts,
                onRemove = viewModel::removeChosenCaller,
            )
            RingAtOnceSection(
                entries = state.callers.ringAtOnce,
                onAddTyped = viewModel::addRingAtOnceCaller,
                loadContacts = viewModel::contactCallers,
                onAddFromContacts = viewModel::addRingAtOnceCallerFromContacts,
                onRemove = viewModel::removeRingAtOnceCaller,
            )
        }
    }
}

/**
 * The point of the app, so the one tonal block on the surface. Amber appears in a normal state only
 * here, as the ring of the mark while the app would ring. A pause leaves the switch on, so the card
 * keeps its colour and only the amber goes.
 */
@Composable
private fun RingThroughCard(
    checked: Boolean,
    paused: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val waiting = checked && !paused
    Surface(
        color = if (checked) colors.primaryContainer else colors.surfaceContainerHighest,
        contentColor = if (checked) colors.onPrimaryContainer else colors.onSurface,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
                    .padding(Spacing.base),
            verticalArrangement = Arrangement.spacedBy(Spacing.snug),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.base),
            ) {
                Mark(
                    width = 64.dp,
                    dots = if (checked) colors.onPrimaryContainer else colors.onSurfaceVariant,
                    ring =
                        when {
                            waiting -> colors.tertiary
                            checked -> colors.onPrimaryContainer
                            else -> colors.outlineVariant
                        },
                    firstDotAlpha = if (waiting) 0.6f else 1f,
                )
                Text(
                    text = stringResource(R.string.home_ring_through),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                CheckSwitch(checked = checked)
            }
            Text(
                text =
                    stringResource(
                        if (checked &&
                            paused
                        ) {
                            R.string.home_ring_through_paused_hint
                        } else {
                            R.string.home_ring_through_hint
                        },
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = if (checked) colors.onPrimaryContainer else colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PauseControl(
    pausedUntilMillis: Long?,
    onPause: (Int) -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    if (pausedUntilMillis != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.base),
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.base, end = Spacing.hair),
        ) {
            Text(
                text =
                    stringResource(
                        R.string.home_paused_until,
                        DateFormat.getTimeFormat(context).format(Date(pausedUntilMillis)),
                    ),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onResume) {
                Text(stringResource(R.string.home_pause_resume))
            }
        }
    } else {
        Column(modifier = modifier) {
            Text(
                text = stringResource(R.string.home_pause_for),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.base),
            )
            ChipGroup(modifier = Modifier.padding(top = Spacing.tight)) {
                for (hours in PAUSE_HOUR_CHOICES) {
                    ActionChip(
                        label = pluralStringResource(R.plurals.home_pause_hours, hours, hours),
                        onClick = { onPause(hours) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CallersSection(
    callers: CallerRules,
    contactsGranted: Boolean,
    ringThrough: Boolean,
    onUseChosen: (Boolean) -> Unit,
    onContactScope: (ContactScope) -> Unit,
    onAddTyped: (String) -> Unit,
    loadContacts: suspend () -> List<PickedCaller>,
    onAddFromContacts: (PickedCaller) -> Unit,
    onRemove: (String) -> Unit,
) {
    var scopeAwaitingContacts by rememberSaveable { mutableStateOf(ContactScope.OFF) }
    val context = LocalContext.current
    val askForContactsToRing =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                onContactScope(scopeAwaitingContacts)
            } else {
                Toast.makeText(context, R.string.home_contacts_ring_denied, Toast.LENGTH_LONG).show()
            }
        }
    val scope = if (contactsGranted) callers.contactScope else ContactScope.OFF
    val listOn = callers.useChosen && callers.chosen.isNotEmpty()

    SectionHeader(stringResource(R.string.home_callers))
    Text(
        text = stringResource(callersStatus(listOn, scope)),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = Spacing.base, end = Spacing.base, bottom = Spacing.snug),
    )
    SwitchRow(
        title = stringResource(R.string.home_use_chosen),
        subtitle = stringResource(R.string.home_use_chosen_hint),
        checked = callers.useChosen,
        onChange = onUseChosen,
    )
    Column(modifier = Modifier.padding(vertical = Spacing.snug)) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.base),
            verticalArrangement = Arrangement.spacedBy(Spacing.hair),
        ) {
            Text(stringResource(R.string.home_contacts_scope), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(R.string.home_contacts_scope_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ChipGroup(modifier = Modifier.padding(top = Spacing.snug)) {
            for (option in ContactScope.entries) {
                ChoiceChip(
                    selected = scope == option,
                    label = stringResource(option.labelRes()),
                    onClick = {
                        if (option != ContactScope.OFF && !context.hasContactsPermission()) {
                            scopeAwaitingContacts = option
                            askForContactsToRing.launch(Manifest.permission.READ_CONTACTS)
                        } else {
                            onContactScope(option)
                        }
                    },
                )
            }
        }
    }
    if (ringThrough && callers.contactScope != ContactScope.OFF && !contactsGranted) {
        WarningCard(
            icon = painterResource(R.drawable.ic_warning_lock),
            title = stringResource(R.string.home_contacts_missing_title),
            body = stringResource(R.string.home_contacts_missing_body),
            actionLabel = stringResource(R.string.home_permissions_missing_action),
            onAct = { context.openAppDetails() },
            modifier = Modifier.padding(start = Spacing.base, end = Spacing.base, bottom = Spacing.snug),
        )
    }
    CallerList(
        entries = callers.chosen,
        onAddTyped = onAddTyped,
        loadContacts = loadContacts,
        onAddFromContacts = onAddFromContacts,
        onRemove = onRemove,
    )
}

@Composable
private fun RingAtOnceSection(
    entries: Set<String>,
    onAddTyped: (String) -> Unit,
    loadContacts: suspend () -> List<PickedCaller>,
    onAddFromContacts: (PickedCaller) -> Unit,
    onRemove: (String) -> Unit,
) {
    SectionHeader(stringResource(R.string.home_ring_at_once))
    Text(
        text = stringResource(R.string.home_ring_at_once_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = Spacing.base, end = Spacing.base, bottom = Spacing.snug),
    )
    CallerList(
        entries = entries,
        onAddTyped = onAddTyped,
        loadContacts = loadContacts,
        onAddFromContacts = onAddFromContacts,
        onRemove = onRemove,
    )
}

/** A list of callers with its two ways to add one: from contacts, or a typed number. */
@Composable
private fun CallerList(
    entries: Set<String>,
    onAddTyped: (String) -> Unit,
    loadContacts: suspend () -> List<PickedCaller>,
    onAddFromContacts: (PickedCaller) -> Unit,
    onRemove: (String) -> Unit,
) {
    var typed by rememberSaveable { mutableStateOf("") }
    var choosingContact by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    // The ask waits for the button: the contacts list is only needed once the user asks for it.
    val askForContacts =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                choosingContact = true
            } else {
                Toast.makeText(context, R.string.home_callers_contacts_denied, Toast.LENGTH_LONG).show()
            }
        }
    // Plain string order puts Ł after Z; a collator follows the default locale's alphabet.
    val collator = Collator.getInstance()
    for (entry in entries.sortedWith(compareBy(collator) { ChosenCaller.fromEntry(it).label })) {
        val caller = ChosenCaller.fromEntry(entry)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.base),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.base, end = Spacing.hair, top = Spacing.snug, bottom = Spacing.snug),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                CallerText(caller.name, caller.numbers.firstOrNull().orEmpty())
            }
            TextButton(onClick = { onRemove(entry) }) {
                Text(stringResource(R.string.home_callers_remove))
            }
        }
    }
    OutlinedButton(
        onClick = {
            if (context.hasContactsPermission()) {
                choosingContact = true
            } else {
                askForContacts.launch(Manifest.permission.READ_CONTACTS)
            }
        },
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        // The stock border is outlineVariant, too faint to be a button's only edge on this surface.
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.padding(start = Spacing.base, end = Spacing.base, top = Spacing.base),
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(stringResource(R.string.home_callers_from_contacts))
    }
    if (choosingContact) {
        ContactsDialog(
            loadContacts = loadContacts,
            onPick = { caller ->
                choosingContact = false
                onAddFromContacts(caller)
            },
            onDismiss = { choosingContact = false },
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.tight),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = Spacing.base, end = Spacing.base, top = Spacing.base, bottom = Spacing.section),
    ) {
        OutlinedTextField(
            value = typed,
            onValueChange = { typed = it },
            label = { Text(stringResource(R.string.home_callers_type)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.weight(1f),
        )
        TextButton(
            enabled = storedCallerNumber(typed) != null,
            onClick = {
                onAddTyped(typed)
                typed = ""
            },
        ) {
            Text(stringResource(R.string.home_callers_add))
        }
    }
}

@Composable
private fun ContactsDialog(
    loadContacts: suspend () -> List<PickedCaller>,
    onPick: (PickedCaller) -> Unit,
    onDismiss: () -> Unit,
) {
    val contacts by produceState<List<PickedCaller>>(initialValue = emptyList()) { value = loadContacts() }
    var query by rememberSaveable { mutableStateOf("") }
    val shown = remember(contacts, query) { contacts.filter { caller -> caller.matches(query) } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_callers_from_contacts)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.home_callers_search)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    items(shown) { caller ->
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onPick(caller) }
                                    .padding(vertical = Spacing.tight),
                        ) {
                            CallerText(caller.name, caller.forms.first())
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@Composable
private fun CallerText(
    name: String?,
    number: String,
) {
    if (name == null) {
        Text(number, style = MaterialTheme.typography.bodyLarge)
    } else {
        Text(name, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = number,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun callersStatus(
    listOn: Boolean,
    scope: ContactScope,
): Int =
    when (scope) {
        ContactScope.OFF -> if (listOn) R.string.home_callers_chosen else R.string.home_callers_anyone
        ContactScope.EVERYONE ->
            if (listOn) R.string.home_callers_contacts_and_chosen else R.string.home_callers_contacts_only
        ContactScope.STARRED ->
            if (listOn) R.string.home_callers_starred_and_chosen else R.string.home_callers_starred_only
    }

private fun ContactScope.labelRes(): Int =
    when (this) {
        ContactScope.OFF -> R.string.home_scope_off
        ContactScope.EVERYONE -> R.string.home_scope_everyone
        ContactScope.STARRED -> R.string.home_scope_starred
    }

/**
 * The notification permission rides along on Android 13+: without it the ringing still happens, but
 * the notification with its Silence button is hidden.
 */
private fun permissionsToAsk(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        CALL_PERMISSIONS + Manifest.permission.POST_NOTIFICATIONS
    } else {
        CALL_PERMISSIONS
    }

private fun Context.openAppDetails(): Boolean =
    startActivitySafely(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
