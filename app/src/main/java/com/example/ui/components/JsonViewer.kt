package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PurpleAccent

@Composable
fun JsonViewer(
    jsonString: String,
    modifier: Modifier = Modifier,
    title: String = "JSON Response Body"
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1424), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("JSON Data", jsonString)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Respon JSON berhasil disalin!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy JSON",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp)
                .background(Color(0xFF070B14), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            val scrollStateVertical = rememberScrollState()
            val scrollStateHorizontal = rememberScrollState()

            SelectionContainer {
                Text(
                    text = highlightJsonSyntax(jsonString),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier
                        .verticalScroll(scrollStateVertical)
                        .horizontalScroll(scrollStateHorizontal)
                )
            }
        }
    }
}

private fun highlightJsonSyntax(json: String): AnnotatedString {
    return buildAnnotatedString {
        val keyRegex = """"([^"\\]|\\.)*"\s*:""".toRegex()
        val stringValueRegex = """:\s*"([^"\\]|\\.)*"""".toRegex()
        val numberValueRegex = """:\s*(-?\d+(\.\d+)?)""".toRegex()
        val booleanValueRegex = """:\s*(true|false|null)""".toRegex()

        var lastIndex = 0

        // Parse line by line for simplicity
        val lines = json.lines()
        lines.forEachIndexed { i, line ->
            var currentPos = 0
            val lineLen = line.length

            while (currentPos < lineLen) {
                val char = line[currentPos]
                when {
                    char == '"' -> {
                        val endQuote = line.indexOf('"', currentPos + 1)
                        if (endQuote != -1) {
                            val str = line.substring(currentPos, endQuote + 1)
                            if (line.substring(endQuote + 1).trimStart().startsWith(":")) {
                                append(AnnotatedString(str, SpanStyle(color = BlueAccent, fontWeight = FontWeight.SemiBold)))
                            } else {
                                append(AnnotatedString(str, SpanStyle(color = EmeraldPrimary)))
                            }
                            currentPos = endQuote + 1
                        } else {
                            append(char)
                            currentPos++
                        }
                    }
                    char.isDigit() || char == '-' -> {
                        var endNum = currentPos
                        while (endNum < lineLen && (line[endNum].isDigit() || line[endNum] == '.' || line[endNum] == '-')) {
                            endNum++
                        }
                        val numStr = line.substring(currentPos, endNum)
                        append(AnnotatedString(numStr, SpanStyle(color = AmberGold)))
                        currentPos = endNum
                    }
                    line.startsWith("true", currentPos) || line.startsWith("false", currentPos) || line.startsWith("null", currentPos) -> {
                        val word = if (line.startsWith("true", currentPos)) "true" else if (line.startsWith("false", currentPos)) "false" else "null"
                        append(AnnotatedString(word, SpanStyle(color = PurpleAccent, fontWeight = FontWeight.Bold)))
                        currentPos += word.length
                    }
                    else -> {
                        append(char)
                        currentPos++
                    }
                }
            }
            if (i < lines.size - 1) append("\n")
        }
    }
}
