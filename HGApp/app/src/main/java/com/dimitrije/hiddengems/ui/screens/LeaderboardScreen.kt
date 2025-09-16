package com.dimitrije.hiddengems.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun LeaderboardScreen() {
    var users by remember { mutableStateOf<List<Pair<String, Long>>>(emptyList()) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("score", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                users = result.documents.mapNotNull {
                    val name = it.getString("email") ?: it.id
                    val score = it.getLong("score") ?: 0L
                    name to score
                }
            }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Leaderboard", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(users) { index, (name, score) ->
                val medal = when (index) {
                    0 -> "🥇"
                    1 -> "🥈"
                    2 -> "🥉"
                    else -> ""
                }

                val backgroundColor = when (index) {
                    0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    1 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    2 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = backgroundColor)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$medal ${index + 1}. $name", style = MaterialTheme.typography.bodyLarge)
                        Text("Score: $score", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}