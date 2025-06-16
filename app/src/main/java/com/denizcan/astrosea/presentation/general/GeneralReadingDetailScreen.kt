package com.denizcan.astrosea.presentation.general

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralReadingDetailScreen(
    readingType: String,
    onNavigateBack: () -> Unit
) {
    Log.d("AcilimDebug", "readingType: '${readingType}' (length: ${readingType.length})")
    // Açılım türüne göre kart sayısı ve açıklama
    val (cardCount, description) = when (readingType.trim()) {
        // Genel Açılımlar
        "GÜNLÜK AÇILIM" -> 3 to "Bu açılım, günlük düşünce ve hissiyatlarınızı, ayrıca mevcut sürecinizin gidişatını gösterir. Üç kart, sırasıyla düşüncelerinizi, duygularınızı ve sürecin genel yönünü temsil eder."
        "TEK KART AÇILIMI" -> 1 to "Gününüzün genel enerjilerini ve size verilen tavsiyeleri gösteren tek kartlık bir açılımdır. Bu kart, gününüzün ana temasını ve size sunulan rehberliği temsil eder."
        "EVET – HAYIR AÇILIMI" -> 1 to "Aklınızdaki soruya net bir cevap veren tek kartlık bir açılımdır. Kartın pozisyonu ve anlamı, sorunuza 'evet' veya 'hayır' cevabını verir."
        "GEÇMİŞ, ŞİMDİ, GELECEK" -> 3 to "Üç kartlık bu açılım, geçmişteki durumunuzu, şu anki konumunuzu ve gelecekteki potansiyel sonucunuzu gösterir. Her kart, zaman çizgisindeki farklı bir noktayı temsil eder."
        "DURUM, AKSİYON, SONUÇ" -> 3 to "Mevcut durumunuzu, atmanız gereken adımları ve olası sonuçları gösteren üç kartlık bir açılımdır. Her kart, sürecin farklı bir aşamasını temsil eder."
        // İlişki Açılımları
        "İLİŞKİ AÇILIMI" -> 3 to "İlişkinizde yaşanan güncel durumları gösteren temel açılım. Geçmiş, şimdi ve gelecekte ilişkinin durumunu anlamak için kullanılır."
        "UYUMLULUK AÇILIMI" -> 7 to "Karşınızdaki insanla gerçekte ne kadar uyumlusunuz? Duygu, düşünce ve fiziksel uyumunuzu gösteren 7 kartlık açılım."
        "DETAYLI İLİŞKİ AÇILIMI" -> 9 to "Kalp, düşünce ve aksiyon hanelerini içeren ve geçmiş, şimdi ve gelecek ekseninde yorumlanan detaylı açılım. 9 kart ile ilişkinin tüm boyutlarını analiz eder."
        "MÜCADELELER AÇILIMI" -> 7 to "İlişki içerisindeki tartışma ve zorlukları inceleyen ve çözümler öneren 7 kartlık açılım."
        "TAMAM MI, DEVAM MI" -> 6 to "Bazı durumlar ve kişiler değişmez. Peki artık bu ilişki için çabalamalı mı, yoksa bitmesine izin mi vermeli? 6 kart ile ilişkinin devam edip etmeyeceğine dair rehberlik sunar."
        "GELECEĞİNE GİDEN YOL" -> 5 to "İstediğin geleceği biliyorsun, peki oraya nasıl ulaşacaksın? Size yol haritası çizen açılım."
        "İŞ YERİNDEKİ PROBLEMLER" -> 6 to "İş yerinde karşılaştığınız problemlerin sebebini inceleyen açılım."
        "FİNANSAL DURUM" -> 6 to "Finansal durumunuzu gösteren ve neye ihtiyacınız olduğunu söyleyen açılım."
        else -> 1 to "Açılım açıklaması bulunamadı."
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // Arka plan görseli
        Image(
            painter = painterResource(id = R.drawable.acilimlararkaplan),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Scaffold(
            topBar = {
                AstroTopBar(
                    title = "",
                    onBackClick = onNavigateBack,
                    titleStyle = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily(Font(R.font.cinzel_regular))
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Başlık
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = readingType,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                            fontSize = 24.sp
                        ),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
                // Kartların yerleşimi
                if (readingType.trim() in listOf(
                        "İLİŞKİ AÇILIMI", "UYUMLULUK AÇILIMI", "DETAYLI İLİŞKİ AÇILIMI", "MÜCADELELER AÇILIMI", "TAMAM MI, DEVAM MI",
                        "GELECEĞİNE GİDEN YOL", "İŞ YERİNDEKİ PROBLEMLER", "FİNANSAL DURUM"
                    )) {
                    CareerCardLayout(readingType.trim())
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(cardCount) { index ->
                            Card(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(90.dp)
                                    .padding(horizontal = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                        contentDescription = "Kart ${index + 1}",
                                        modifier = Modifier
                                            .width(48.dp)
                                            .height(72.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }
                // Açılım açıklaması
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Açılım Hakkında",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                                color = Color.White
                            )
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                color = Color.White
                            )
                        )
                    }
                }
                // Kartları Çek Butonu
                Button(
                    onClick = { /* TODO: Kart çekme işlemi */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A5568)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Kartları Çek",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily(Font(R.font.cinzel_regular)),
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun RelationshipCardLayout(readingType: String) {
    when (readingType) {
        "İLİŞKİ AÇILIMI" -> {
            // 3 kart yatay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) {
                    Card(
                        modifier = Modifier
                            .width(60.dp)
                            .height(90.dp)
                            .padding(horizontal = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.tarotkartiarkasikesimli),
                                contentDescription = "Kart ${it + 1}",
                                modifier = Modifier.width(48.dp).height(72.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
        "UYUMLULUK AÇILIMI" -> {
            // 1-2-1-2-1 dizilişi (toplam 7 kart)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. satır: 1 kart
                Card(
                    modifier = Modifier.width(60.dp).height(90.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart 1", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                    }
                }
                // 2. satır: 2 kart
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 2..3) {
                        Card(
                            modifier = Modifier.width(60.dp).height(90.dp).padding(horizontal = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart $i", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                            }
                        }
                    }
                }
                // 3. satır: 1 kart
                Card(
                    modifier = Modifier.width(60.dp).height(90.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart 4", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                    }
                }
                // 4. satır: 2 kart
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 5..6) {
                        Card(
                            modifier = Modifier.width(60.dp).height(90.dp).padding(horizontal = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart $i", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                            }
                        }
                    }
                }
                // 5. satır: 1 kart
                Card(
                    modifier = Modifier.width(60.dp).height(90.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart 7", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                    }
                }
            }
        }
        "DETAYLI İLİŞKİ AÇILIMI" -> {
            // 3x3 grid (9 kart)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { row ->
                    Row(horizontalArrangement = Arrangement.Center) {
                        repeat(3) { col ->
                            val index = row * 3 + col + 1
                            Card(
                                modifier = Modifier.width(60.dp).height(90.dp).padding(horizontal = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Box(Modifier.fillMaxSize(), Alignment.Center) {
                                    Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart $index", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                                }
                            }
                        }
                    }
                }
            }
        }
        "MÜCADELELER AÇILIMI" -> {
            // 1-2-1-2-1 dizilişi (toplam 7 kart)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. satır: 1 kart
                Card(
                    modifier = Modifier.width(60.dp).height(90.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart 1", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                    }
                }
                // 2. satır: 2 kart
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 2..3) {
                        Card(
                            modifier = Modifier.width(60.dp).height(90.dp).padding(horizontal = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart $i", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                            }
                        }
                    }
                }
                // 3. satır: 1 kart
                Card(
                    modifier = Modifier.width(60.dp).height(90.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart 4", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                    }
                }
                // 4. satır: 2 kart
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 5..6) {
                        Card(
                            modifier = Modifier.width(60.dp).height(90.dp).padding(horizontal = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart $i", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                            }
                        }
                    }
                }
                // 5. satır: 1 kart
                Card(
                    modifier = Modifier.width(60.dp).height(90.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart 7", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                    }
                }
            }
        }
        "TAMAM MI, DEVAM MI" -> {
            // 1-2-3 (6 kart, piramit)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. satır: 1 kart
                Card(
                    modifier = Modifier.width(60.dp).height(90.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart 1", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                    }
                }
                // 2. satır: 2 kart
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 2..3) {
                        Card(
                            modifier = Modifier.width(60.dp).height(90.dp).padding(horizontal = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart $i", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                            }
                        }
                    }
                }
                // 3. satır: 3 kart
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 4..6) {
                        Card(
                            modifier = Modifier.width(60.dp).height(90.dp).padding(horizontal = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart $i", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CareerCardLayout(readingType: String) {
    when (readingType) {
        "GELECEĞİNE GİDEN YOL" -> {
            // 1-1-3 (5 kart)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. satır: 1 kart
                Card(
                    modifier = Modifier.width(60.dp).height(90.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart 1", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                    }
                }
                // 2. satır: 1 kart
                Card(
                    modifier = Modifier.width(60.dp).height(90.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart 2", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                    }
                }
                // 3. satır: 3 kart
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 3..5) {
                        Card(
                            modifier = Modifier.width(60.dp).height(90.dp).padding(horizontal = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart $i", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                            }
                        }
                    }
                }
            }
        }
        "İŞ YERİNDEKİ PROBLEMLER" -> {
            // 1-4-1 (6 kart)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. satır: 1 kart
                Card(
                    modifier = Modifier.width(60.dp).height(90.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart 1", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                    }
                }
                // 2. satır: 4 kart
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 2..5) {
                        Card(
                            modifier = Modifier.width(60.dp).height(90.dp).padding(horizontal = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart $i", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                            }
                        }
                    }
                }
                // 3. satır: 1 kart
                Card(
                    modifier = Modifier.width(60.dp).height(90.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart 6", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                    }
                }
            }
        }
        "FİNANSAL DURUM" -> {
            // 1-3-2 (6 kart)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. satır: 1 kart
                Card(
                    modifier = Modifier.width(60.dp).height(90.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart 1", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                    }
                }
                // 2. satır: 3 kart
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 2..4) {
                        Card(
                            modifier = Modifier.width(60.dp).height(90.dp).padding(horizontal = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart $i", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                            }
                        }
                    }
                }
                // 3. satır: 2 kart
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 5..6) {
                        Card(
                            modifier = Modifier.width(60.dp).height(90.dp).padding(horizontal = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2236).copy(alpha = 0.7f)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Image(painter = painterResource(id = R.drawable.tarotkartiarkasikesimli), contentDescription = "Kart $i", modifier = Modifier.width(48.dp).height(72.dp), contentScale = ContentScale.Fit)
                            }
                        }
                    }
                }
            }
        }
        else -> RelationshipCardLayout(readingType)
    }
} 