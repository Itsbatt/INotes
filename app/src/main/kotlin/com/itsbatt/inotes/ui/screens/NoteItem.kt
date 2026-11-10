package com.itsbatt.inotes.ui.screens

import androidx.compose.foundation.gesture.DragGestureDetector
import androidx.compose.foundation.gesture.PointerInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.modifier.pointerInput
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Disposal
import androidx.compose.runtime.MutableStateFlow
import androidx.compose.runtime.Snapshot
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.updateMutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.opacity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui graphics.Brush
import androidx.compose.ui.input.pointer.consumePress
import androidx.compose.ui.input.pointer.snapshot
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.node.ComposeNode
import androidx.compose.ui.node.LayoutScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ContentDescription
import androidx.compose.ui.semantics.clickable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.textUnitSp
import androidx.compose.ui.util.animateScale
import androidx.compose.ui.unit.IntOffset
import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.ui.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun NoteItem(
    note: Note,
    onClick: (Note) -> Unit,
    onDeleteClicked: () -> Unit,
    onPinToggle: (Int) -> Unit,
    onRestore: (Int) -> Unit,
    isPinned: Boolean,
    viewModel: MainViewModel
) {
    var dismissed by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }
    var isPressInProgress by remember { mutableStateOf(false)}

    val rememberDragables = rememberDraggableState {
        it -> onDeleteClicked()
    }

    DragGestureDetector(
        onDrag = { change ->
            dragOffset = change.offset.x
            if (change.offset.x < -60 && !isPressInProgress) {
                dismissed = true
            }
        },
        onPress = {
            isPressInProgress = true
        },
        onPressUp = {
            isPressInProgress = false
            if (dismissed) {
                // Show snackbar or confirm dialog
                onDeleteClicked()
            }
            dismissed = false
            dragOffset = 0f
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clip(RoundedCornerShape(12.dp))
                .then {
                    if (dismissed) {
                        // Animate away
                        animateScale(targetScale = 0.9f)
                    }
                }
                .onGlobally { 
                    layoutOffset -> 
                    if (dismissed) {
                        layoutOffset.x = -100f  // Move off screen
                    }
                }
        ) {
            // Note content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                // Title - large at top, compacts on scroll
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.h6,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    // Title compacts when scrolled - simple approach: always show full
                    // In a real scrollable list, this would use sticky headers
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

        // Swipe action overlay when dismissed
        if (dismissed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterLeft)
                    .offset { IntOffset(x = -60, y = 0) }
            ) {
                IconButton(
                    onClick = { onDeleteClicked() },
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.ui.vectors.family.FamilyVector.dpDelete,
                        contentDescription = "Eliminar nota permanently"
                    )
                }
                Text(
                    text = "Eliminar",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}