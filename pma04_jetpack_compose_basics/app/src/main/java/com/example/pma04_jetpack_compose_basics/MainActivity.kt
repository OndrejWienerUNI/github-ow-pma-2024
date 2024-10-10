package com.example.pma04_jetpack_compose_basics
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeExample()
        }
    }
}

/*
This function defines a Composable, which renders UI in Jetpack Compose.
All the logic and UI for this simple app are inside this function.
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeExample() {

    // States for each text input
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var residence by remember { mutableStateOf("") }
    var damns by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }

    // Scaffold adds a TopAppBar (toolbar)
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My App", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.DarkGray
                )
            )
        }
    ) { innerPadding ->
        // The rest of the content goes inside the Scaffold with padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input fields
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = surname,
                onValueChange = { surname = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = age,
                onValueChange = { ageInput ->
                    // Restrict input to digits and ensure age is <= 125
                    if (ageInput.all { char -> char.isDigit() }
                        && ageInput.toIntOrNull()?.let { it in 0..124 } == true) {
                        age = ageInput
                    }
                },
                label = { Text("Age (less than 125)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = residence,
                onValueChange = { residence = it },
                label = { Text("Residence") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = damns,
                onValueChange = { damnsInput ->
                    // Restrict input to digits and ensure the value is between 0 and 10
                    if (damnsInput.all { char -> char.isDigit() }
                        && damnsInput.toIntOrNull()?.let { it in 0..10 } == true) {
                        damns = damnsInput
                    }
                },
                label = { Text("Damns to give (0-10)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Submit and Clear buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        // Provide placeholders for empty fields
                        val firstName = name.ifBlank { "John" }
                        val lastName = surname.ifBlank { "Doe" }
                        val nick = nickname.ifBlank { "none of your business" }
                        val ageValue = age.ifBlank { "an unknown number of" }
                        val place = residence.ifBlank { "a distant parking garage" }
                        val damnsGiven = damns.ifBlank { "absolutely no" }

                        resultText = "My name is $firstName $lastName, aka $nick. " +
                                "I am $ageValue years old, live in $place, " +
                                "and I have $damnsGiven damns to give."
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Submit")
                }

                Button(
                    onClick = {
                        name = ""
                        surname = ""
                        nickname = ""
                        age = ""
                        residence = ""
                        damns = ""
                        resultText = ""
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF0000),
                        contentColor = Color.White
                    )
                ) {
                    Text("Clear")
                }
            }

            // Result text
            if (resultText.isNotEmpty()) {
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeExample()
}
