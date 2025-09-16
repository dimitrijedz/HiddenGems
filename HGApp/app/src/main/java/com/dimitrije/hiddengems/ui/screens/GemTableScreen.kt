package com.dimitrije.hiddengems.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dimitrije.hiddengems.model.Gem
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun GemTableScreen(navController: NavHostController) {
    val context = LocalContext.current
    var allGems by remember { mutableStateOf<List<Gem>>(emptyList()) }

    var emailInput by remember { mutableStateOf("") }
    var userIdFilterList by remember { mutableStateOf<List<String>>(emptyList()) }

    var titleFragment by remember { mutableStateOf("") }

    var dateFromMillis by remember { mutableStateOf<Long?>(null) }
    var dateToMillis by remember { mutableStateOf<Long?>(null) }

    val calendar = remember { Calendar.getInstance() }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("gems")
            .get()
            .addOnSuccessListener { result ->
                allGems = result.documents.mapNotNull { it.toObject(Gem::class.java) }
            }
    }

    fun fetchUserIdsByEmailFragment(fragment: String, onResult: (List<String>) -> Unit) {
        FirebaseFirestore.getInstance().collection("users")
            .get()
            .addOnSuccessListener { result ->
                val uids = result.documents.filter {
                    it.getString("email")?.contains(fragment, ignoreCase = true) == true
                }.map { it.id }
                onResult(uids)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Filter gems", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        TextField(
            value = titleFragment,
            onValueChange = { titleFragment = it },
            label = { Text("Name filter") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        TextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text("User email fragment") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            fetchUserIdsByEmailFragment(emailInput) { uids ->
                userIdFilterList = uids
                if (uids.isEmpty()) {
                    Toast.makeText(context, "No users found for that email fragment", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text("Apply email filter")
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = {
            emailInput = ""
            userIdFilterList = emptyList()
        }) {
            Text("Reset email filter")
        }

        Spacer(Modifier.height(16.dp))

        Text("Filter by date", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth, 0, 0)
                        dateFromMillis = calendar.timeInMillis
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) {
                Text("From")
            }

            Button(onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth, 23, 59)
                        dateToMillis = calendar.timeInMillis
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) {
                Text("To")
            }

            Button(onClick = {
                dateFromMillis = null
                dateToMillis = null
            }) {
                Text("Reset")
            }
        }

        Spacer(Modifier.height(24.dp))

        val filteredGems = allGems.filter { gem ->
            val time = gem.timestamp
            val timeMatch = (dateFromMillis == null || time >= dateFromMillis!!) &&
                    (dateToMillis == null || time <= dateToMillis!!)
            val userMatch = userIdFilterList.isEmpty() || userIdFilterList.contains(gem.createdBy)
            val titleMatch = titleFragment.isBlank() || gem.title.contains(titleFragment, ignoreCase = true)
            timeMatch && userMatch && titleMatch
        }

        Text("Filtered gems: ${filteredGems.size}", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))

        LazyColumn {
            items(filteredGems) { gem ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            navController.navigate("details/${gem.id}")
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(gem.title, style = MaterialTheme.typography.bodyLarge)
                        Text("Created by: ${gem.createdBy}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}