package com.rakha.kursusku

import android.content.res.Configuration
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakha.kursusku.ui.theme.DarkSuccessContainer
import com.rakha.kursusku.ui.theme.DarkSuccessIcon
import com.rakha.kursusku.ui.theme.DarkSuccessText
import com.rakha.kursusku.ui.theme.KursuskuTheme
import com.rakha.kursusku.ui.theme.LightSuccessContainer
import com.rakha.kursusku.ui.theme.LightSuccessIcon
import com.rakha.kursusku.ui.theme.LightSuccessText
import kotlinx.coroutines.delay

/**
 * Sealed class yang merepresentasikan State Arsitektur UI pendaftaran kelas/kursus:
 * 1. [Idle]: State awal saat pengguna belum mengirim formulir atau sedang mengetik.
 * 2. [Loading]: State saat tombol diproses dan mensimulasikan komunikasi asinkron ke backend.
 * 3. [Result]: State akhir yang menampung status keberhasilan atau kegagalan beserta pesan hasil.
 */
sealed class RegistrationUiState {
    object Idle : RegistrationUiState()
    object Loading : RegistrationUiState()
    data class Result(val isSuccess: Boolean, val message: String) : RegistrationUiState()
}

/**
 * Activity utama aplikasi yang menjadi titik masuk (entry point).
 * Mengatur tema dasar [KursuskuTheme] dan menampilkan layar pendaftaran [RegistrationScreen].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KursuskuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    RegistrationScreen()
                }
            }
        }
    }
}

/**
 * Screen-level state owner untuk pendaftaran akun [RegistrationScreen].
 * Memperagakan penerapan State Management kelas industri menggunakan [RegistrationUiState],
 * event handling, validasi formulir, serta efek animasi Compose [AnimatedVisibility].
 */
@Composable
fun RegistrationScreen() {
    // =========================================================================
    // 1. STATE MANAGEMENT
    // State input formulir (menggunakan rememberSaveable agar tahan terhadap rotasi layar / landscape)
    // =========================================================================
    var nama by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var wa by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var konfirmasiPassword by rememberSaveable { mutableStateOf("") }
    var setuju by rememberSaveable { mutableStateOf(value = false) }
    var passwordTerlihat by rememberSaveable { mutableStateOf(value = false) }

    // State penanda bahwa tombol submit pernah diklik
    var sudahSubmit by rememberSaveable { mutableStateOf(value = false) }

    // Sealed Class UI State Management
    var uiState by remember { mutableStateOf<RegistrationUiState>(RegistrationUiState.Idle) }

    // Evaluasi mode tema untuk warna dinamis banner sukses
    val isDark = isSystemInDarkTheme()
    val successContainer = if (isDark) DarkSuccessContainer else LightSuccessContainer
    val successText = if (isDark) DarkSuccessText else LightSuccessText
    val successIcon = if (isDark) DarkSuccessIcon else LightSuccessIcon

    // =========================================================================
    // 2. SIMULASI ASINKRON (LaunchedEffect & delay)
    // Saat uiState berubah menjadi Loading, LaunchedEffect mensimulasikan request API selama 2000 ms,
    // lalu memperbarui state ke Result(isSuccess = true, message = "Pendaftaran Berhasil!")
    // =========================================================================
    LaunchedEffect(uiState) {
        if (uiState is RegistrationUiState.Loading) {
            delay(2000)
            uiState = RegistrationUiState.Result(
                isSuccess = true,
                message = "Pendaftaran Berhasil!",
            )
        }
    }

    // =========================================================================
    // 3. FORM VALIDATION (VALIDASI FORMULIR)
    // Evaluasi reaktif setiap kali terjadi perubahan state input
    // =========================================================================
    val errorNama = sudahSubmit && nama.trim().isEmpty()
    val errorEmail = sudahSubmit && !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val errorWa = sudahSubmit && (wa.trim().filter { it.isDigit() }.length < 10)
    val errorPassword = sudahSubmit && (password.length < 8)
    val errorKonfirmasi = sudahSubmit && (konfirmasiPassword != password)
    val errorPersetujuan = sudahSubmit && !setuju

    val formValid = nama.trim().isNotEmpty() &&
            Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() &&
            wa.trim().filter { it.isDigit() }.length >= 10 &&
            password.length >= 8 &&
            konfirmasiPassword == password &&
            setuju

    // Layout utama layar dengan scroll vertikal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header Aplikasi
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "KursusKu",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "Buat Akun Baru",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Daftarkan dirimu untuk memulai perjalanan belajar",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 14.sp,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Kartu kontainer formulir
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // =========================================================================
                // 4. ANATOMI FIELD (OutlinedTextField) & EVENT HANDLING
                // =========================================================================

                // Anatomi Field 1: Input Nama Lengkap
                OutlinedTextField(
                    value = nama,
                    onValueChange = {
                        nama = it
                        if (uiState !is RegistrationUiState.Idle) uiState = RegistrationUiState.Idle
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nama Lengkap") },
                    placeholder = { Text("Nama sesuai identitas") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = if (isDark) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    isError = errorNama,
                    supportingText = { if (errorNama) Text("Nama wajib diisi") },
                )

                // Anatomi Field 2: Input Email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (uiState !is RegistrationUiState.Idle) uiState = RegistrationUiState.Idle
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    placeholder = { Text("contoh@email.com") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = null,
                            tint = if (isDark) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = errorEmail,
                    supportingText = { if (errorEmail) Text("Masukkan alamat email yang valid") },
                )

                // Anatomi Field 3: Input Nomor WhatsApp
                OutlinedTextField(
                    value = wa,
                    onValueChange = {
                        wa = it
                        if (uiState !is RegistrationUiState.Idle) uiState = RegistrationUiState.Idle
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nomor WhatsApp") },
                    placeholder = { Text("08xxxxxxxxxx") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Phone,
                            contentDescription = null,
                            tint = if (isDark) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = errorWa,
                    supportingText = { if (errorWa) Text("Nomor WA minimal 10 angka") },
                )

                // Anatomi Field 4: Input Kata Sandi
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (uiState !is RegistrationUiState.Idle) uiState = RegistrationUiState.Idle
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Kata Sandi") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (isDark) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordTerlihat = !passwordTerlihat }) {
                            Icon(
                                imageVector = if (passwordTerlihat) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordTerlihat) "Sembunyikan kata sandi" else "Tampilkan kata sandi",
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = if (passwordTerlihat) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    isError = errorPassword,
                    supportingText = { if (errorPassword) Text("Kata sandi minimal 8 karakter") },
                )

                // Anatomi Field 5: Input Konfirmasi Kata Sandi
                OutlinedTextField(
                    value = konfirmasiPassword,
                    onValueChange = {
                        konfirmasiPassword = it
                        if (uiState !is RegistrationUiState.Idle) uiState = RegistrationUiState.Idle
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Konfirmasi Kata Sandi") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (isDark) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = errorKonfirmasi,
                    supportingText = { if (errorKonfirmasi) Text("Konfirmasi harus sama dengan kata sandi") },
                )

                // Checkbox Persetujuan Ketentuan
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Checkbox(
                        checked = setuju,
                        onCheckedChange = {
                            setuju = it
                            if (uiState !is RegistrationUiState.Idle) uiState = RegistrationUiState.Idle
                        },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary),
                    )
                    Text(
                        text = "Saya menyetujui ketentuan penggunaan",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (errorPersetujuan) {
                    Text(
                        text = "Persetujuan wajib dicentang",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // =========================================================================
                // 5. EVENT SUBMIT FORMULIR & CONTROL FLOW when (uiState)
                // =========================================================================
                val isLoading = uiState is RegistrationUiState.Loading

                Button(
                    onClick = {
                        sudahSubmit = true
                        if (formValid) {
                            uiState = RegistrationUiState.Loading
                        } else {
                            uiState = RegistrationUiState.Result(
                                isSuccess = false,
                                message = "Mohon lengkapi seluruh kolom dengan benar.",
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading, // Disabling button during loading to prevent duplicate clicks
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    // Control flow when (uiState) untuk merender konten di dalam Button
                    when (uiState) {
                        is RegistrationUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp,
                            )
                        }
                        else -> {
                            Text(
                                text = "Daftar Sekarang",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 6.dp),
                            )
                        }
                    }
                }

                // =========================================================================
                // 6. STATE RESULT & ANIMATED VISIBILITY
                // Menampilkan banner hasil sukses/gagal secara smooth menggunakan AnimatedVisibility
                // =========================================================================
                AnimatedVisibility(
                    visible = uiState is RegistrationUiState.Result,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    if (uiState is RegistrationUiState.Result) {
                        val result = uiState as RegistrationUiState.Result
                        val isSuccess = result.isSuccess

                        val containerBg = if (isSuccess) successContainer else MaterialTheme.colorScheme.errorContainer
                        val contentClr = if (isSuccess) successText else MaterialTheme.colorScheme.onErrorContainer
                        val iconTint = if (isSuccess) successIcon else MaterialTheme.colorScheme.error
                        val iconVector = if (isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = containerBg),
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = iconTint,
                                )
                                Column {
                                    Text(
                                        text = if (isSuccess) "Pendaftaran Berhasil!" else "Pendaftaran Gagal",
                                        color = contentClr,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                    )
                                    Text(
                                        text = if (isSuccess) "Selamat bergabung, ${nama.trim()}! Formulir siap diproses." else result.message,
                                        color = contentClr.copy(alpha = 0.9f),
                                        fontSize = 13.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "Petunjuk: Isi semua kolom (Nama, Email, WA minimal 10 angka, Kata Sandi minimal 8 karakter) dan centang persetujuan.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
}

/**
 * Fungsi Preview Mode Terang (Light Mode) untuk Interactive Preview.
 */
@Preview(showBackground = true, name = "Light Mode")
@Composable
fun RegistrationScreenLightPreview() {
    KursuskuTheme(darkTheme = false) {
        RegistrationScreen()
    }
}

/**
 * Fungsi Preview Mode Gelap (Dark Mode) untuk Interactive Preview.
 */
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun RegistrationScreenDarkPreview() {
    KursuskuTheme(darkTheme = true) {
        RegistrationScreen()
    }
}
