package com.dimitrije.hiddengems.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun RatingDropdown(
    selectedRating: Int?,
    onRatingSelected: (Int) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedRating?.let { "Your rating: $it ★" } ?: "Rate this gem",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (1..5).forEach { rating ->
                DropdownMenuItem(
                    text = { Text("$rating ★") },
                    onClick = {
                        onRatingSelected(rating)
                        expanded = false
                    }
                )
            }
        }
    }
}
