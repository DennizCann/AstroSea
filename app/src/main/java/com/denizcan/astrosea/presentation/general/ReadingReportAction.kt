package com.denizcan.astrosea.presentation.general

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.denizcan.astrosea.R
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
internal fun ReadingReportAction(reading: String, onReport: suspend (String) -> Unit) {
    var showDialog by rememberSaveable(reading) { mutableStateOf(false) }
    var reported by rememberSaveable(reading) { mutableStateOf(false) }
    var sending by remember(reading) { mutableStateOf(false) }
    var errorMessage by remember(reading) { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    TextButton(
        onClick = {
            errorMessage = null
            showDialog = true
        },
        enabled = !reported,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
    ) {
        Text(
            text = stringResource(if (reported) R.string.reading_report_sent else R.string.reading_report),
            color = if (reported) Color.White else Color(0xFFD4AF37)
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { if (!sending) showDialog = false },
            title = { Text(stringResource(R.string.reading_report_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.reading_report_description))
                    errorMessage?.let {
                        Text(
                            text = stringResource(it),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !sending,
                    onClick = {
                        sending = true
                        errorMessage = null
                        scope.launch {
                            try {
                                onReport(reading)
                                reported = true
                                showDialog = false
                            } catch (error: CancellationException) {
                                throw error
                            } catch (error: Exception) {
                                errorMessage = if ((error as? FirebaseFunctionsException)?.code ==
                                    FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED
                                ) R.string.reading_report_limit else R.string.reading_report_failed
                            } finally {
                                sending = false
                            }
                        }
                    }
                ) {
                    Text(stringResource(if (sending) R.string.reading_report_sending else R.string.reading_report_send))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }, enabled = !sending) {
                    Text(stringResource(R.string.reading_report_cancel))
                }
            }
        )
    }
}
