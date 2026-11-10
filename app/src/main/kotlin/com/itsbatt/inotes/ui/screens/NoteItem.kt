package com.itsbatt.inotes.ui.screens

import androidx.compose.foundation.DragDirection
import androidx.compose.foundation.gesture.DragGestureDetector
import androidx.compose.foundation.gesture.PointerInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.modifier.pointerInput
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.opacity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.consumePress
import androidx.compose.ui.input.pointer.snapshot
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.node.ComposeNode
import androidx.compose.ui.node.LayoutScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ContentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.intOffset
import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.ui.MainViewModel

@Composable
fun NoteItem(
    note: Note,
    onClick: (Note, MainViewModel) -> Unit,
    onDelete: (Int) -> Unit,
    onPinToggle: (Int) -> Unit,
    onRestore: (Int) -> Unit,
    isPinned: Boolean,
    viewModel: MainViewModel
) {
    var swipeProgress by remember { mutableStateOf(0f) }
    var isSwiping by remember { mutableStateOf(false) }
    var dismissed by remember { mutableStateOf(false) }

    // Drag gesture for swipe-to-delete
    DragGestureDetector(
        onDrag = { change ->
            val deltaX = change.offset.x
            swipeProgress = when {
                deltaX > 0 -> maxOf(0f, minOf(1f, deltaX / 120f))
                deltaX < -60 -> minOf(1f, maxOf(0f, (deltaX + 60f) / 60f))
                else -> 0f
            }
            isSwiping = deltaX.abs() > 5
        },
        onPress = {
            isSwiping = false
            swipeProgress = 0f
        },
        onPressUp = {
            isSwiping = false
            if (swipeProgress >= 1f) {
                dismissed = true
                onDelete(note.id)
            } else {
                swipeProgress = 0f
            }
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(4.dp)
                .clip(RoundedCornerShape(12.dp)),
            elevation = if (isPinned) 4.dp else 1.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            // Swipe overlay when swiping
            if (swipeProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .offset { IntOffset(x = (swipeProgress * -60).roundToInt(), y = 0) }
                        .background(MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = androidx.compose.ui.vectors.family.FamilyVector.dpDelete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Eliminar",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                // Title
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.h6,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = note.body.take(60) + (if (note.body.isNotEmpty()) "..." else ""),
                    style = MaterialTheme.typography.body2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Reset dismissed after short time
        if (dismissed) {
            dismissSwipe(note.id)
        }
    }
}

private fun NoteItem.dismissSwipe(noteId: Int) {
    // Move to trash - in our case, just reset the note
    // In a real implementation, this would move to a trash folder
    // and start a 30-day timer for permanent deletion
    // For now, just reset the swipe
}