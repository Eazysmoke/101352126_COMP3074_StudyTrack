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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StudyTrackTheme {
                AssignmentListScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentListScreen() {
    var selectedFilter by remember { mutableStateOf("All") }
    var showForm by remember { mutableStateOf(false) }

    val assignments = remember {
        mutableStateListOf(
            Assignment(
                id = 1,
                title = "UI Prototype",
                course = "COMP3074",
                dueDate = LocalDate.now().plusDays(2),
                priority = "High",
                notes = "Submit the GitHub link and explanation video."
            ),
            Assignment(
                id = 2,
                title = "Data Science Lab",
                course = "COMP3122",
                dueDate = LocalDate.now().plusDays(4),
                priority = "Medium"
            ),
            Assignment(
                id = 3,
                title = "Express Lab",
                course = "COMP3123",
                dueDate = LocalDate.now().minusDays(1),
                priority = "Low",
                isCompleted = true
            )
        )
    }

    if (showForm) {
        AssignmentFormScreen(
            onCancel = { showForm = false },
            onSave = { assignment ->
                assignments.add(assignment)
                selectedFilter = "All"
                showForm = false
            }
        )
        return
    }

    val visibleAssignments = assignments
        .filter {
            when (selectedFilter) {
                "Pending" -> !it.isCompleted
                "Completed" -> it.isCompleted
                else -> true
            }
        }
        .sortedBy { it.dueDate }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("StudyTrack") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showForm = true }) {
                Text(
                    "+",
                    style = MaterialTheme.typography.headlineMedium
                )
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
                text = "My Assignments",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Keep your courses and deadlines in one place.",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Pending", "Completed").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            if (visibleAssignments.isEmpty()) {
                Text("No assignments to show.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(
                        items = visibleAssignments,
                        key = { it.id }
                    ) { assignment ->
                        AssignmentCard(assignment)
                    }
                }
            }
        }
    }
}

@Composable
fun AssignmentCard(assignment: Assignment) {
    val dateFormat = DateTimeFormatter.ofPattern("MMM d, yyyy")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = assignment.course,
                style = MaterialTheme.typography.bodyMedium
            )

            Text("Due: ${assignment.dueDate.format(dateFormat)}")
            Text("Priority: ${assignment.priority}")

            Text(
                text = if (assignment.isCompleted) "Completed" else "Pending",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentFormScreen(
    onCancel: () -> Unit,
    onSave: (Assignment) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<LocalDate?>(null) }
    var priority by remember { mutableStateOf("Medium") }
    var notes by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }

    BackHandler { onCancel() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Assignment") },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Back")
                    }
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
                    if (showErrors && title.isBlank()) {
                        Text("Enter an assignment title.")
                    }
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
                    if (showErrors && course.isBlank()) {
                        Text("Enter a course name or code.")
                    }
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
                Text(
                    dueDate?.format(
                        DateTimeFormatter.ofPattern("MMM d, yyyy")
                    ) ?: "Choose due date"
                )
            }

            if (showErrors && dueDate == null) {
                Text(
                    text = "Choose a due date.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text(
                text = "Priority",
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                                id = System.currentTimeMillis(),
                                title = title.trim(),
                                course = course.trim(),
                                dueDate = chosenDate,
                                priority = priority,
                                notes = notes.trim()
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

@Preview(showBackground = true)
@Composable
fun StudyTrackPreview() {
    StudyTrackTheme {
        AssignmentListScreen()
    }
}