package com.mechel.studytrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    val assignments = remember {
        listOf(
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
            FloatingActionButton(onClick = {
                // The next stage will open the assignment form.
            }) {
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

@Preview(showBackground = true)
@Composable
fun StudyTrackPreview() {
    StudyTrackTheme {
        AssignmentListScreen()
    }
}