package com.redox.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redox.assistant.domain.model.MemoryCategory
import com.redox.assistant.ui.components.GlassButton
import com.redox.assistant.ui.components.GlassSettingsPanel
import com.redox.assistant.ui.components.GlassTextField
import com.redox.assistant.ui.components.LiquidGlassCard
import com.redox.assistant.ui.theme.CrimsonRed
import com.redox.assistant.ui.theme.PureBlack
import com.redox.assistant.ui.theme.TextGrayMuted
import com.redox.assistant.ui.theme.TextGraySecondary
import com.redox.assistant.ui.theme.TextWhitePrimary
import com.redox.assistant.viewmodel.RedoxViewModel

@Composable
fun MemoryScreen(
    viewModel: RedoxViewModel,
    modifier: Modifier = Modifier
) {
    val memories by viewModel.memories.collectAsState()

    var keyInput by remember { mutableStateOf("") }
    var valueInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(MemoryCategory.FACT) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .padding(bottom = 75.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NEURAL MEMORY MATRIX",
                    color = CrimsonRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Long-Term Contextual Knowledge Base",
                    color = TextGrayMuted,
                    fontSize = 12.sp
                )
            }

            if (memories.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearAllMemories() }) {
                    Icon(Icons.Default.DeleteSweep, "Clear Memories", tint = TextGraySecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Add Memory Card
        GlassSettingsPanel(title = "Inject Knowledge / Fact") {
            GlassTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                placeholder = "Key / Topic (e.g. 'User Name', 'Favorite Coffee')"
            )
            Spacer(modifier = Modifier.height(10.dp))
            GlassTextField(
                value = valueInput,
                onValueChange = { valueInput = it },
                placeholder = "Memory Value / Context details"
            )
            Spacer(modifier = Modifier.height(12.dp))
            GlassButton(
                text = "Save to Neural Matrix",
                icon = Icons.Default.Add,
                onClick = {
                    if (keyInput.isNotBlank() && valueInput.isNotBlank()) {
                        viewModel.saveMemory(selectedCategory, keyInput, valueInput)
                        keyInput = ""
                        valueInput = ""
                    }
                },
                isPrimary = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Existing Memories List
        GlassSettingsPanel(title = "Retained Memory Entries (${memories.size})") {
            if (memories.isEmpty()) {
                Text(
                    text = "No stored facts yet. REDOX automatically stores user preferences and instructions as you speak.",
                    color = TextGraySecondary,
                    fontSize = 13.sp
                )
            } else {
                memories.forEach { item ->
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentPadding = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "[${item.category.label}] ${item.key}",
                                    color = CrimsonRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = item.value,
                                    color = TextWhitePrimary,
                                    fontSize = 13.sp
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteMemory(item.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, "Delete", tint = TextGrayMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
