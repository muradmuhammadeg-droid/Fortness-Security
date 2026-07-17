package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CryptoHelper
import com.example.data.SecureNote
import com.example.ui.theme.*
import com.example.ui.viewmodel.SecurityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadView(viewModel: SecurityViewModel) {
    val hasMasterPin by viewModel.hasMasterPin.collectAsState()
    val isVerified by viewModel.isPinVerified.collectAsState()
    val pinError by viewModel.pinError.collectAsState()
    val notes by viewModel.secureNotes.collectAsState()

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var selectedNoteForDetail by remember { mutableStateOf<SecureNote?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        when {
            // Case 1: Setup PIN
            !hasMasterPin -> {
                PinSetupView(onPinSet = { viewModel.setupMasterPin(it) }, errorMsg = pinError)
            }

            // Case 2: Enter PIN
            !isVerified -> {
                PinLockView(
                    onPinEntered = { viewModel.verifyMasterPin(it) },
                    errorMsg = pinError
                )
            }

            // Case 3: Authenticated Secure Notepad
            else -> {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showAddNoteDialog = true },
                            containerColor = ElectricCyan,
                            contentColor = Color(0xFF381E72),
                            modifier = Modifier
                                .padding(bottom = 80.dp)
                                .testTag("add_note_fab"),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EditNote,
                                contentDescription = "Add Safe Note",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    containerColor = DeepObsidian,
                    contentWindowInsets = WindowInsets(0)
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp)
                    ) {
                        // Header with Lock Vault Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Secure Notepad",
                                    color = TextWhite,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "END-TO-END CRYPTOGRAPHIC NOTES",
                                    color = ElectricCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                            }

                            Button(
                                onClick = { viewModel.lockNotes() },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Lock Notes",
                                    tint = DangerCrimson,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("LOCK", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Grid of Notes
                        if (notes.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FolderOpen,
                                    contentDescription = "Empty Folder",
                                    tint = BorderSlate,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Encrypted Notes",
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Write journals, recovery seeds, or sensitive keys completely encrypted on your local memory.",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 160.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(notes) { note ->
                                    NoteCard(
                                        note = note,
                                        onClick = { selectedNoteForDetail = note },
                                        onDelete = { viewModel.removeNote(note) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Note Dialog Modal
        if (showAddNoteDialog) {
            AddNoteDialog(
                onDismiss = { showAddNoteDialog = false },
                onSave = { title, content ->
                    val encryptedContent = CryptoHelper.encrypt(content)
                    viewModel.addNote(
                        SecureNote(
                            title = title,
                            contentCipher = encryptedContent
                        )
                    )
                    showAddNoteDialog = false
                }
            )
        }

        // Selected Note Decrypted Detail Modal
        selectedNoteForDetail?.let { note ->
            NoteDetailDialog(
                note = note,
                onDismiss = { selectedNoteForDetail = null }
            )
        }
    }
}

@Composable
fun NoteCard(
    note: SecureNote,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val plainContent = remember(note.contentCipher) {
        CryptoHelper.decrypt(note.contentCipher)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable(onClick = onClick)
            .testTag("note_card_${note.title.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = note.title,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plainContent,
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Encrypted",
                    tint = ElectricCyan.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Note",
                        tint = DangerCrimson.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PinSetupView(onPinSet: (String) -> Unit, errorMsg: String?) {
    var pinText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.EnhancedEncryption,
            contentDescription = "Lock Shield",
            tint = ElectricCyan,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Create Sentinel PIN",
            color = TextWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Choose a 4-digit Master Security PIN. This will be required to unlock and read notes.",
            color = TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Dots indicator
        PinDotsIndicator(pinLength = pinText.length)

        errorMsg?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = DangerCrimson, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Virtual Numpad Grid
        VirtualNumpad(
            onDigitClick = { digit ->
                if (pinText.length < 4) {
                    pinText += digit
                    if (pinText.length == 4) {
                        onPinSet(pinText)
                    }
                }
            },
            onDeleteClick = {
                if (pinText.isNotEmpty()) {
                    pinText = pinText.dropLast(1)
                }
            },
            onClearClick = {
                pinText = ""
            }
        )
    }
}

@Composable
fun PinLockView(onPinEntered: (String) -> Unit, errorMsg: String?) {
    var pinText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = "Secure Lock",
            tint = ElectricCyan,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Authenticate Required",
            color = TextWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter your 4-digit Master Security PIN to decrypt vaults",
            color = TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Dots indicator
        PinDotsIndicator(pinLength = pinText.length)

        errorMsg?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = DangerCrimson, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Virtual Numpad Grid
        VirtualNumpad(
            onDigitClick = { digit ->
                if (pinText.length < 4) {
                    pinText += digit
                    if (pinText.length == 4) {
                        onPinEntered(pinText)
                        pinText = "" // reset entered buffer
                    }
                }
            },
            onDeleteClick = {
                if (pinText.isNotEmpty()) {
                    pinText = pinText.dropLast(1)
                }
            },
            onClearClick = {
                pinText = ""
            }
        )
    }
}

@Composable
fun PinDotsIndicator(pinLength: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0..3) {
            val isActive = i < pinLength
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isActive) ElectricCyan else BorderSlate)
            )
        }
    }
}

@Composable
fun VirtualNumpad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit
) {
    val digits = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        digits.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { char ->
                    val isAction = char == "C" || char == "⌫"
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(if (isAction) BorderSlate else DarkNavy)
                            .clickable {
                                when (char) {
                                    "C" -> onClearClick()
                                    "⌫" -> onDeleteClick()
                                    else -> onDigitClick(char)
                                }
                            }
                            .testTag("numpad_$char"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            color = if (char == "⌫") DangerCrimson else TextWhite,
                            fontSize = if (isAction) 20.sp else 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && content.isNotEmpty()) {
                        onSave(title, content)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color(0xFF381E72)
                ),
                enabled = title.isNotEmpty() && content.isNotEmpty()
            ) {
                Text("Lock & Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        title = {
            Text(
                text = "Write Encrypted Note",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = BorderSlate
                    )
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Sensitive Details...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = BorderSlate
                    )
                )
            }
        },
        containerColor = DarkNavy
    )
}

@Composable
fun NoteDetailDialog(
    note: SecureNote,
    onDismiss: () -> Unit
) {
    val plainContent = remember(note.contentCipher) {
        CryptoHelper.decrypt(note.contentCipher)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color(0xFF381E72)
                )
            ) {
                Text("Secure Close")
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = note.title,
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Encrypted",
                    tint = AccentEmerald,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = plainContent,
                        color = TextWhite,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        containerColor = DarkNavy
    )
}
