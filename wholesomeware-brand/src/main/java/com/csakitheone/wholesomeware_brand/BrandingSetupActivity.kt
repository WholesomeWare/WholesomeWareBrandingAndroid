package com.csakitheone.wholesomeware_brand

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.csakitheone.wholesomeware_brand.ui.components.WholesomeWareStoreButton
import com.csakitheone.wholesomeware_brand.ui.theme.WholesomewareBrandTheme

class BrandingSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrandingSetupScreen()
        }
    }

    @Composable
    fun Step(
        text: String,
        onChanged: (Boolean) -> Unit = {},
    ) {
        var isDone by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(isDone) { onChanged(isDone) }

        Box(
            modifier = Modifier
                .clickable { isDone = !isDone }
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = isDone,
                    onCheckedChange = { isDone = it },
                )
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    fun BrandingSetupScreen() {
        WholesomewareBrandTheme {
            val scrollState = rememberScrollState()

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .systemBarsPadding(),
                ) {
                    Card(modifier = Modifier.padding(8.dp)) {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = "Splash screen",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Step(text = "Import dependency")
                        Step(text = "Create splash screen")
                        Card(
                            modifier = Modifier.padding(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceTint,
                            ),
                        ) {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = """
                            <style name="SplashScreen" parent="Theme.SplashScreen">
                                <item name="postSplashScreenTheme">@style/Theme.WholesomeWare</item>
                                <item name="android:windowSplashScreenBrandingImage">@drawable/logo_with_text</item>
                            </style>
                        """.trimIndent(),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Step(text = "installSplashScreen()")
                        Button(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            onClick = {
                                Log.w(
                                    "BrandingSetup",
                                    "https://developer.android.com/develop/ui/views/launch/splash-screen"
                                )
                                Toast.makeText(
                                    this@BrandingSetupActivity,
                                    "Check logcat",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Text(text = "Send documentation to debugger")
                        }
                    }
                    Card(modifier = Modifier.padding(8.dp)) {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = "Brand placement",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Step(text = "Place a link in the app which opens the developer page")
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = "WholesomeWareStoreButton()",
                        )
                        WholesomeWareStoreButton(
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                    Card(modifier = Modifier.padding(8.dp)) {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = "Finishing up",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Step(text = "Remove the setup function call") {}
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = "After completing these steps, please rebuild the app.",
                        )
                    }
                }
            }
        }
    }
}
