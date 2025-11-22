package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.draw.blur

@Composable
fun ModalOverlay(
    onDismiss: () -> Unit,
    clickOutsideDismiss: Boolean = true,
    blurRadiusDp: androidx.compose.ui.unit.Dp = 6.dp,
    content: @Composable () -> Unit
) {
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // фон: сначала размываем "скрин" (задний план), затем добавляем затемнение и кликабельность
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .then(
                        if (blurRadiusDp > androidx.compose.ui.unit.Dp(0f))
                            Modifier.blur(blurRadiusDp)
                        else Modifier
                    )
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x99000000))
                    .let { if (clickOutsideDismiss) it.clickable { onDismiss() } else it }
            )

            // Сам диалог — поверх, без blur
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 12.dp
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        content()
                    }
                }
            }
        }
    }
}
