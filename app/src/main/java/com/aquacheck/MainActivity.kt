package com.aquacheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val Blue = Color(0xFF0277BD)
private val LightBlue = Color(0xFFE3F2FD)

/** One input field. The order MUST match FEATURES in ml/train_model.py. */
private class Field(val label: String, val hint: String, val isValid: (Double) -> Boolean, val rule: String)

private val nonNegative = { v: Double -> v >= 0.0 }
private val FIELDS = listOf(
    Field("pH", "0 to 14, e.g. 7.0", { it in 0.0..14.0 }, "Must be between 0 and 14"),
    Field("Hardness (mg/L)", "e.g. 196", nonNegative, "Must be 0 or more"),
    Field("Solids / TDS (ppm)", "e.g. 22000", nonNegative, "Must be 0 or more"),
    Field("Chloramines (ppm)", "e.g. 7.1", nonNegative, "Must be 0 or more"),
    Field("Sulfate (mg/L)", "e.g. 333", nonNegative, "Must be 0 or more"),
    Field("Conductivity (μS/cm)", "e.g. 426", nonNegative, "Must be 0 or more"),
    Field("Organic Carbon (ppm)", "e.g. 14.3", nonNegative, "Must be 0 or more"),
    Field("Trihalomethanes (μg/L)", "e.g. 66", nonNegative, "Must be 0 or more"),
    Field("Turbidity (NTU)", "e.g. 4.0", nonNegative, "Must be 0 or more"),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Blue, background = Color.White)) {
                AquaCheckScreen()
            }
        }
    }
}

@Composable
private fun AquaCheckScreen() {
    val context = LocalContext.current
    val modelResult = remember { runCatching { RandomForestModel.load(context) } }
    val model = modelResult.getOrNull()

    val values = remember { mutableStateListOf<String>().apply { repeat(FIELDS.size) { add("") } } }
    val errors = remember { mutableStateListOf<String?>().apply { repeat(FIELDS.size) { add(null) } } }
    var message by remember { mutableStateOf<String?>(null) }
    var potable by remember { mutableStateOf<Boolean?>(null) }
    var votePercent by remember { mutableStateOf(0) }

    fun predict() {
        if (model == null) return
        val numbers = DoubleArray(FIELDS.size)
        var ok = true
        FIELDS.forEachIndexed { i, field ->
            val v = values[i].trim().toDoubleOrNull()
            errors[i] = when {
                values[i].isBlank() -> "Required"
                v == null || !v.isFinite() -> "Enter a valid number"
                !field.isValid(v) -> field.rule
                else -> null
            }
            if (errors[i] != null) ok = false else numbers[i] = v!!
        }
        if (!ok) {
            potable = null
            message = "Please fill in every field with a valid value."
            return
        }
        message = null
        val p = model.potableProbability(numbers)
        potable = p > 0.5
        votePercent = (p * 100).roundToInt()
    }

    fun reset() {
        for (i in FIELDS.indices) { values[i] = ""; errors[i] = null }
        message = null
        potable = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Blue)
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Column {
                Text("AquaCheck", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Water potability predictor (Random Forest)", color = Color(0xFFBBDEFB), fontSize = 14.sp)
            }
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (model == null) {
                Notice("Could not load the model file (assets/rf_model.json). Run ml/train_model.py first.", Color(0xFFFFEBEE), Color(0xFFB71C1C))
            } else {
                if (model.isDemo) {
                    Notice("DEMO MODEL: trained on synthetic data, not a real dataset. Results are meaningless. Run ml/train_model.py with the real CSV.", Color(0xFFFFF8E1), Color(0xFF8D6E00))
                } else if (model.testAccuracy != null) {
                    Text("Model test-set accuracy: ${(model.testAccuracy * 1000).roundToInt() / 10.0}% (held-out 20% of data)", fontSize = 13.sp, color = Color.Gray)
                }
            }

            FIELDS.forEachIndexed { i, field ->
                OutlinedTextField(
                    value = values[i],
                    onValueChange = { values[i] = it; errors[i] = null },
                    label = { Text(field.label) },
                    placeholder = { Text(field.hint) },
                    isError = errors[i] != null,
                    supportingText = { errors[i]?.let { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            message?.let { Notice(it, Color(0xFFFFEBEE), Color(0xFFB71C1C)) }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { predict() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue),
                ) { Text("Predict Potability") }
                OutlinedButton(
                    onClick = { reset() },
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) { Text("Reset") }
            }

            potable?.let { isPotable ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isPotable) LightBlue else Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
                        Text(
                            if (isPotable) "Predicted Potable" else "Predicted Non-Potable",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPotable) Blue else Color(0xFFB71C1C),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (isPotable) "The model found these readings similar to samples labelled potable in its training data."
                            else "The model found these readings similar to samples labelled non-potable in its training data.",
                            fontSize = 14.sp,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("Trees voting potable: $votePercent%", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }

            Notice(
                "Educational ML demonstration only. A prediction does not prove that water is safe to drink. " +
                    "Real drinking-water safety requires laboratory testing and compliance with applicable standards.",
                LightBlue, Color(0xFF0D47A1),
            )
        }
    }
}

@Composable
private fun Notice(text: String, background: Color, textColor: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) { Text(text, color = textColor, fontSize = 13.sp) }
}
