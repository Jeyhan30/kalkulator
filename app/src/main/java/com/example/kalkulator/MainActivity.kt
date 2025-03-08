package com.example.kalkulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kalkulator.ui.theme.KalkulatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorApp()
//            KalkulatorTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
        }
    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    KalkulatorTheme {
//        Greeting("Android")
//    }
//}

// Composable utama untuk aplikasi kalkulator
@Composable
fun CalculatorApp() {
    // MutableState untuk menyimpan ekspresi matematika yang dimasukkan oleh pengguna
    var expression by remember { mutableStateOf(TextFieldValue("")) }
    // MutableState untuk menyimpan hasil perhitungan
    var result by remember { mutableStateOf("") }

    // Column digunakan untuk mengatur tata letak elemen UI secara vertikal
    Column(
        modifier = Modifier
            .fillMaxSize() // Mengisi seluruh layar
            .padding(16.dp), // Memberikan padding di sekitar kolom
        verticalArrangement = Arrangement.Center, // Menengahkan elemen secara vertikal
        horizontalAlignment = Alignment.CenterHorizontally // Menengahkan elemen secara horizontal
    ) {
        // TextField untuk memasukkan ekspresi matematika
        TextField(
            value = expression, // Nilai yang ditampilkan di TextField
            onValueChange = { expression = it }, // Mengupdate nilai expression saat pengguna mengetik
            label = { Text("berikan angka yang akan anda hitung\ntambah(+), kurang(-), kali(*), bagi(/)") }, // Label untuk TextField
            modifier = Modifier.fillMaxWidth(), // Mengisi lebar maksimum
            textStyle = TextStyle(fontSize = 16.sp) //ukuran text inputan user
        )

        // Spacer untuk memberikan jarak antara TextField dan Button
        Spacer(modifier = Modifier.height(16.dp))

        // Button untuk memicu perhitungan
        Button(onClick = {
            // Memanggil fungsi calculateExpression dengan ekspresi yang dimasukkan
            result = calculateExpression(expression.text)
        }) {
            Text("Calculate") // Teks yang ditampilkan di tombol
        }

        // Spacer untuk memberikan jarak antara Button dan Text hasil
        Spacer(modifier = Modifier.height(16.dp))

        // Text untuk menampilkan hasil perhitungan
        Text(
            text = "Result: $result", // Menampilkan hasil perhitungan
            fontSize = 24.sp, // Ukuran font
            fontWeight = FontWeight.Bold // Ketebalan font
        )
    }
}

// Fungsi untuk menghitung ekspresi matematika
fun calculateExpression(expression: String): String {
    return try {
        // Memanggil fungsi eval untuk mengevaluasi ekspresi
        val result = eval(expression)
        result.toString() // Mengembalikan hasil sebagai string
    } catch (e: Exception) {
        "Error" // Mengembalikan "Error" jika terjadi kesalahan
    }
}

// Fungsi untuk mengevaluasi ekspresi matematika
fun eval(expression: String): Double {
    return object : Any() {
        var pos = -1 // Posisi saat ini dalam string ekspresi
        var ch = 0 // Karakter saat ini

        // Fungsi untuk membaca karakter berikutnya
        fun nextChar() {
            ch = if (++pos < expression.length) expression[pos].toInt() else -1
        }

        // Fungsi untuk melewati spasi dan memeriksa karakter tertentu
        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.toInt()) nextChar() // Melewati spasi
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        // Fungsi utama untuk memulai parsing ekspresi
        fun parse(): Double {
            nextChar()
            val x = parseExpression() // Memulai parsing ekspresi
            if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        // Fungsi untuk parsing ekspresi (penjumlahan dan pengurangan)
        fun parseExpression(): Double {
            var x = parseTerm() // Parsing term pertama
            while (true) {
                when {
                    eat('+'.toInt()) -> x += parseTerm() // Jika menemukan '+', tambahkan term berikutnya
                    eat('-'.toInt()) -> x -= parseTerm() // Jika menemukan '-', kurangi term berikutnya
                    else -> return x // Jika tidak ada operator, kembalikan hasil
                }
            }
        }

        // Fungsi untuk parsing term (perkalian dan pembagian)
        fun parseTerm(): Double {
            var x = parseFactor() // Parsing faktor pertama
            while (true) {
                when {
                    eat('*'.toInt()) -> x *= parseFactor() // Jika menemukan '*', kalikan dengan faktor berikutnya
                    eat('/'.toInt()) -> x /= parseFactor() // Jika menemukan '/', bagi dengan faktor berikutnya
                    else -> return x // Jika tidak ada operator, kembalikan hasil
                }
            }
        }

        // Fungsi untuk parsing faktor (angka atau ekspresi dalam tanda kurung)
        fun parseFactor(): Double {
            if (eat('+'.toInt())) return parseFactor() // Jika menemukan '+', abaikan
            if (eat('-'.toInt())) return -parseFactor() // Jika menemukan '-', negasikan faktor
            var x: Double
            val startPos = pos
            if (eat('('.toInt())) {
                x = parseExpression() // Jika menemukan '(', parsing ekspresi dalam kurung
                eat(')'.toInt()) // Pastikan ada ')'
            } else if (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) {
                // Jika menemukan angka atau titik, parsing angka tersebut
                while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) nextChar()
                x = expression.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar()) // Jika karakter tidak dikenali, lempar exception
            }
            return x
        }
    }.parse() // Memulai parsing dari awal ekspresi
}