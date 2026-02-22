package com.dungz.openappsdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dungz.openappsdk.R
import com.dungz.openappsdk.model.Language
import java.util.Locale

@Composable
fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showHandPointer: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flag
//        Image(
//            painter = painterResource(id = language.flagRes),
//            contentDescription = language.name,
//            modifier = Modifier
//                .size(40.dp)
//                .clip(CircleShape)
//        )

        Spacer(modifier = Modifier.width(16.dp))

        // Language name
        Text(
            text = language.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        // Hand pointer animation
        if (showHandPointer) {
            HandPointerAnimation(modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Checkmark
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LanguageItemPreview() {
    LanguageItem(
        language = Language("en", "English", Locale.ENGLISH, R.drawable.flag_us),
        isSelected = false,
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun LanguageItemSelectedPreview() {
    LanguageItem(
        language = Language("en", "English", Locale.ENGLISH, R.drawable.flag_us),
        isSelected = true,
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun LanguageItemWithHandPointerPreview() {
    LanguageItem(
        language = Language("pt_BR", "Brazil", Locale("pt", "BR"), R.drawable.flag_brazil),
        isSelected = false,
        onClick = {},
        showHandPointer = true
    )
}
