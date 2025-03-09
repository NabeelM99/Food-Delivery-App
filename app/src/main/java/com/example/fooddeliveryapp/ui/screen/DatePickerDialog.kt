package com.example.fooddeliveryapp.ui.screen

import android.app.DatePickerDialog
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import androidx.compose.runtime.DisposableEffect

@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePicker = android.app.DatePickerDialog(
        LocalContext.current,
        { _, selectedYear, selectedMonth, selectedDay ->
            onDateSelected("$selectedDay/${selectedMonth + 1}/$selectedYear")
        },
        year,
        month,
        day
    )

    datePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, "OK") { _, _ ->
        datePicker.dismiss()
    }

    datePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel") { _, _ ->
        onDismiss()
    }

    DisposableEffect(Unit) {
        datePicker.show()
        onDispose { datePicker.dismiss() }
    }
}
