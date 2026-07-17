package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CryptoHelper
import com.example.data.PasswordCredential
import com.example.ui.theme.*
import com.example.ui.viewmodel.SecurityViewModel
import java.security.SecureRandom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultView(viewModel: SecurityViewModel) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredPasswords by viewModel.filteredPasswords.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Finance", "Social", "Work", "Personal", "Shopping", "Other")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ElectricCyan,
                contentColor = Color(0xFF381E72),
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("add_credential_fab"),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Credential",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = DeepObsidian,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepObsidian)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            // Header
            Text(
                text = "Credentials Vault",
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "ENCRYPTED ON-DEVICE KEYSTORE",
                color = ElectricCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_search_input"),
                placeholder = { Text("Search records...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = DarkNavy,
                    unfocusedContainerColor = DarkNavy,
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = BorderSlate
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Horizontal Selection
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CyberBlue else DarkNavy)
                            .clickable { viewModel.updateCategory(category) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("category_chip_$category")
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color(0xFF1D192B) else TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Credentials List
            if (filteredPasswords.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Empty",
                        tint = BorderSlate,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Credentials Saved",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap the [+] button to add your first secure login credential.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPasswords, key = { it.id }) { credential ->
                        CredentialCard(
                            credential = credential,
                            onDelete = { viewModel.removePassword(credential) },
                            context = context
                        )
                    }
                }
            }
        }
    }

    // Add Credential Dialogue Modal with Generator inside
    if (showAddDialog) {
        AddCredentialDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, username, password, url, category, notes ->
                val cipher = CryptoHelper.encrypt(password)
                viewModel.addPassword(
                    PasswordCredential(
                        title = title,
                        username = username,
                        passwordCipher = cipher,
                        url = url,
                        category = category,
                        notes = notes
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CredentialCard(
    credential: PasswordCredential,
    onDelete: () -> Unit,
    context: Context
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val plainPassword = remember(credential.passwordCipher) {
        CryptoHelper.decrypt(credential.passwordCipher)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("credential_card_${credential.title.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = credential.title,
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = credential.url,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Category Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BorderSlate)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = credential.category.uppercase(),
                        color = ElectricCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Username",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = credential.username,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Password row with secure reveal and copy actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepObsidian)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Password",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (passwordVisible) plainPassword else "••••••••••••",
                        color = if (passwordVisible) ElectricCyan else TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                Row {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Toggle Visibility",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Password", plainPassword)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Password copied securely!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy Password",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (credential.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notes: ${credential.notes}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            // Bottom Actions (Delete)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (showDeleteConfirm) {
                    TextButton(
                        onClick = { showDeleteConfirm = false }
                    ) {
                        Text("Cancel", color = TextMuted, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onDelete()
                            showDeleteConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson)
                    ) {
                        Text("Delete", color = TextWhite, fontSize = 13.sp)
                    }
                } else {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete record",
                            tint = DangerCrimson.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddCredentialDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, username: String, password: String, url: String, category: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Personal") }

    // Password generator states
    var genLength by remember { mutableStateOf(16f) }
    var useUpper by remember { mutableStateOf(true) }
    var useLower by remember { mutableStateOf(true) }
    var useDigits by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }
    var generatedPassPreview by remember { mutableStateOf("") }

    // Initial generated password
    LaunchedEffect(genLength, useUpper, useLower, useDigits, useSymbols) {
        generatedPassPreview = generateSecurePassword(
            length = genLength.toInt(),
            useUpper = useUpper,
            useLower = useLower,
            useDigits = useDigits,
            useSymbols = useSymbols
        )
    }

    val categories = listOf("Personal", "Finance", "Social", "Work", "Shopping", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                        onSave(title, username, password, url, selectedCategory, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = Color(0xFF381E72)
                ),
                enabled = title.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()
            ) {
                Text("Save Securely", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        title = {
            Text(
                text = "Add Safe Credential",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Account / Service Title (e.g. Google)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = BorderSlate
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username / Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = BorderSlate
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Vault Password Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = BorderSlate
                            )
                        )
                    }

                    // Integrated Password Generator sub-card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DeepObsidian),
                            border = BorderStroke(1.dp, BorderSlate)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "SECURE PASS-KEY GENERATOR",
                                    color = ElectricCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Generator preview box
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkNavy)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = generatedPassPreview,
                                        color = AccentEmerald,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            password = generatedPassPreview
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Apply password",
                                            tint = ElectricCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Length Slider
                                Text(
                                    text = "Length: ${genLength.toInt()} characters",
                                    color = TextWhite,
                                    fontSize = 12.sp
                                )
                                Slider(
                                    value = genLength,
                                    onValueChange = { genLength = it },
                                    valueRange = 8f..24f,
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = ElectricCyan,
                                        inactiveTrackColor = BorderSlate,
                                        thumbColor = CyberBlue
                                    )
                                )

                                // Parameters Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = useUpper,
                                            onCheckedChange = { useUpper = it },
                                            colors = CheckboxDefaults.colors(checkmarkColor = DeepObsidian)
                                        )
                                        Text("A-Z", color = TextWhite, fontSize = 11.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = useLower,
                                            onCheckedChange = { useLower = it },
                                            colors = CheckboxDefaults.colors(checkmarkColor = DeepObsidian)
                                        )
                                        Text("a-z", color = TextWhite, fontSize = 11.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = useDigits,
                                            onCheckedChange = { useDigits = it },
                                            colors = CheckboxDefaults.colors(checkmarkColor = DeepObsidian)
                                        )
                                        Text("0-9", color = TextWhite, fontSize = 11.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = useSymbols,
                                            onCheckedChange = { useSymbols = it },
                                            colors = CheckboxDefaults.colors(checkmarkColor = DeepObsidian)
                                        )
                                        Text("!@#", color = TextWhite, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("Website / Domain URL (e.g. google.com)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = BorderSlate
                            )
                        )
                    }

                    item {
                        Text("Category Tag", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { cat ->
                                val selected = cat == selectedCategory
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) CyberBlue else BorderSlate)
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (selected) TextWhite else TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Secret Notes (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = BorderSlate
                            )
                        )
                    }
                }
            }
        },
        containerColor = DarkNavy
    )
}

// Cryptographically secure password generation logic
fun generateSecurePassword(
    length: Int,
    useUpper: Boolean,
    useLower: Boolean,
    useDigits: Boolean,
    useSymbols: Boolean
): String {
    val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val lower = "abcdefghijklmnopqrstuvwxyz"
    val digits = "0123456789"
    val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    val pool = StringBuilder()
    if (useUpper) pool.append(upper)
    if (useLower) pool.append(lower)
    if (useDigits) pool.append(digits)
    if (useSymbols) pool.append(symbols)

    if (pool.isEmpty()) return "Pass123!"

    val random = SecureRandom()
    return (1..length)
        .map { pool[random.nextInt(pool.length)] }
        .joinToString("")
}
