package com.mechel.studytrack

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mechel.studytrack.ui.theme.StudyTrackTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Assignment(
    val id: Long,
    val title: String,
    val course: String,
    val dueDate: LocalDate,
    val priority: String,
    val notes: String = "",
    val isCompleted: Boolean = false
)

private fun formattedDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyTrackTheme {
                StudyTrackApp()
            }
        }
    }
}

@Composable
fun StudyTrackApp() {
    var screen by remember { mutableStateOf("List") }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }

    val assignments = remember {
        mutableStateListOf(
            Assignment(
                1, "UI Prototype", "COMP3074",
                LocalDate.now().plusDays(2), "High",
                "Submit the GitHub link and explanation video."
            ),
            Assignment(
                2, "Data Science Lab", "COMP3122",
                LocalDate.now().plusDays(4), "Medium"
            ),
            Assignment(
                3, "Express Lab", "COMP3123",
                LocalDate.now().minusDays(1), "Low",
                isCompleted = true
            )
        )
    }

    val selected = assignments.find { it.id == selectedId }

    fun updateAssignment(updated: Assignment) {
        val index = assignments.indexOfFirst { it.id == updated.id }
        if (index >= 0) assignments[index] = updated
    }

    when (screen) {
        "Add", "Edit" -> {
            val editing = screen == "Edit"
            AssignmentFormScreen(
                existing = if (editing) selected else null,
                onCancel = {
                    screen = if (editing) "Details" else "List"
                },
                onSave = { assignment ->
                    if (editing) {
                        updateAssignment(assignment)
                        screen = "Details"
                    } else {
                        assignments.add(assignment)
                        selectedFilter = "All"
                        screen = "List"
                    }
                }
            )
        }

        "Details" -> {
            if (selected != null) {
                AssignmentDetailsScreen(
                    assignment = selected,
                    onBack = { screen = "List" },
                    onEdit = { screen = "Edit" },
                    onToggleCompleted = {
                        updateAssignment(
                            selected.copy(
                                isCompleted = !selected.isCompleted
                            )
                        )
                    },
                    onDelete = {
                        assignments.removeAll { it.id == selected.id }
                        selectedId = null
                        screen = "List"
                    }
                )
            } else {
                LaunchedEffect(Unit) { screen = "List" }
            }
        }

        else -> AssignmentListScreen(
            assignments = assignments,
            selectedFilter = selectedFilter,
            onFilterChange = { selectedFilter = it },
            onAdd = { screen = "Add" },
            onOpen = {
                selectedId = it.id
                screen = "Details"
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentListScreen(
    assignments: List<Assignment>,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onAdd: () -> Unit,
    onOpen: (Assignment) -> Unit
) {
    val visibleAssignments = assignments.filter {
        when (selectedFilter) {
            "Pending" -> !it.isCompleted
            "Completed" -> it.isCompleted
            else -> true
        }
    }.sortedBy { it.dueDate }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("StudyTrack") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "My Assignments",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text("Keep your courses and deadlines in one place.")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Pending", "Completed").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter) }
                    )
                }
            }

            if (visibleAssignments.isEmpty()) {
                Text("No assignments to show. Tap + to add one.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(visibleAssignments, key = { it.id }) { assignment ->
                        Card(
                            onClick = { onOpen(assignment) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    assignment.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(assignment.course)
                                Text("Due: ${formattedDate(assignment.dueDate)}")
                                Text("Priority: ${assignment.priority}")
                                Text(
                                    if (assignment.isCompleted)
                                        "Completed" else "Pending",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentFormScreen(
    existing: Assignment?,
    onCancel: () -> Unit,
    onSave: (Assignment) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var course by remember { mutableStateOf(existing?.course ?: "") }
    var dueDate by remember {
        mutableStateOf<LocalDate?>(existing?.dueDate)
    }
    var priority by remember {
        mutableStateOf(existing?.priority ?: "Medium")
    }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var showErrors by remember { mutableStateOf(false) }

    BackHandler { onCancel() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existing == null)
                            "Add Assignment" else "Edit Assignment"
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onCancel) { Text("Back") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Assignment title") },
                singleLine = true,
                isError = showErrors && title.isBlank(),
                supportingText = {
                    if (showErrors && title.isBlank())
                        Text("Enter an assignment title.")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = course,
                onValueChange = { course = it },
                label = { Text("Course") },
                singleLine = true,
                isError = showErrors && course.isBlank(),
                supportingText = {
                    if (showErrors && course.isBlank())
                        Text("Enter a course name or code.")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = {
                    val initialDate = dueDate ?: LocalDate.now()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            dueDate = LocalDate.of(year, month + 1, day)
                        },
                        initialDate.year,
                        initialDate.monthValue - 1,
                        initialDate.dayOfMonth
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(dueDate?.let { formattedDate(it) } ?: "Choose due date")
            }

            if (showErrors && dueDate == null) {
                Text(
                    "Choose a due date.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text("Priority", fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Low", "Medium", "High").forEach { option ->
                    FilterChip(
                        selected = priority == option,
                        onClick = { priority = option },
                        label = { Text(option) }
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    showErrors = true
                    val chosenDate = dueDate
                    if (
                        title.isNotBlank() &&
                        course.isNotBlank() &&
                        chosenDate != null
                    ) {
                        onSave(
                            Assignment(
                                id = existing?.id ?: System.currentTimeMillis(),
                                title = title.trim(),
                                course = course.trim(),
                                dueDate = chosenDate,
                                priority = priority,
                                notes = notes.trim(),
                                isCompleted = existing?.isCompleted ?: false
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Assignment")
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDetailsScreen(
    assignment: Assignment,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assignment Details") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                assignment.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text("Course: ${assignment.course}")
            Text("Due: ${formattedDate(assignment.dueDate)}")
            Text("Priority: ${assignment.priority}")
            Text(
                "Status: " +
                        if (assignment.isCompleted) "Completed" else "Pending"
            )
            Text("Notes", fontWeight = FontWeight.Bold)
            Text(assignment.notes.ifBlank { "No notes added." })

            Button(
                onClick = onToggleCompleted,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (assignment.isCompleted)
                        "Mark Pending" else "Mark Completed"
                )
            }

            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Assignment")
            }

            OutlinedButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Delete Assignment",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete assignment?") },
            text = { Text("Delete \"${assignment.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StudyTrackPreview() {
    StudyTrackTheme {
        StudyTrackApp()
    }
}