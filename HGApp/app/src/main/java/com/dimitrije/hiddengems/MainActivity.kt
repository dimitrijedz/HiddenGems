package com.dimitrije.hiddengems

import com.dimitrije.hiddengems.ui.screens.LoginScreen
import com.dimitrije.hiddengems.ui.screens.ProfileScreen
import com.dimitrije.hiddengems.ui.theme.HiddenGemsTheme

import android.os.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                var isLoggedIn by remember { mutableStateOf(false) }

                if (isLoggedIn) {
                    ProfileScreen(onLogout = { isLoggedIn = false })
                } else {
                    LoginScreen(onLoginSuccess = { isLoggedIn = true })
                }
            }
        }

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "$name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HiddenGemsTheme {
        Greeting("Hidden Gems")
    }
}