package com.example.meditox.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun OtpScreen(modifier: Modifier,navController: NavController){
    var otp by remember { mutableStateOf("") }
    val backgroundColor = Color(0xFFE8F5E9)
    val GreenDark = Color(0xFF005005)


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enter the OTP",
                style = MaterialTheme.typography.displayMedium,
                color = GreenDark
            )
            Spacer(modifier = Modifier.height(12.dp))


            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it.take(6) }, // OTP limit
                label = { Text("OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    color = Color.Black,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,

            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "We've sent an OTP to your whatsapp.Only valid for 2 minutes",
                style = MaterialTheme.typography.labelSmall,
                color = GreenDark.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(22.dp))
            Button(
                onClick = {
                    // TODO: Verify OTP logic

                },
                colors = ButtonDefaults.buttonColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Verify", color = Color.White)
            }
        }
    }
}