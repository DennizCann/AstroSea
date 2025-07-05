package com.denizcan.astrosea.presentation.general

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denizcan.astrosea.R
import com.denizcan.astrosea.presentation.components.AstroTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralReadingInfoScreen(
    readingType: String,
    onNavigateBack: () -> Unit,
    onNavigateToReadingDetail: (String) -> Unit
) {
    val readingInfo = remember(readingType) {
        getReadingInfo(readingType)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.acilimlararkaplan),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Scaffold(
            topBar = {
                AstroTopBar(
                    title = readingType,
                    onBackClick = onNavigateBack
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Çerçeve ve Kartlar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.98f)
                        .weight(6.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.acilimlarsayfasitak),
                        contentDescription = "Çerçeve",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    // Kartların yerleşeceği alan
                    CardLayoutContainer(
                        readingInfo = readingInfo,
                        onNavigateToCardDetail = { /* Bu sayfada kart detayına gitmeyeceğiz */ }
                    )
                }
                
                // Açılım Bilgileri ve Butonlar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .weight(3.5f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Açılım Bilgileri - Tek bir card içinde
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Text(
                                    text = getReadingDescription(readingType),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White,
                                    textAlign = TextAlign.Justify
                                )
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Kart Anlamları:",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            
                            itemsIndexed(readingInfo.cardMeanings) { index, meaning ->
                                Text(
                                    text = "${index + 1}. $meaning: ${getCardMeaningDescription(readingType, meaning)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily(Font(R.font.cormorantgaramond_regular)),
                                        fontSize = 14.sp
                                    ),
                                    color = Color.White,
                                    textAlign = TextAlign.Justify
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Açılıma Başla Butonu
                    Button(
                        onClick = { onNavigateToReadingDetail(readingType) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text("Açılıma Başla", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CardLayoutContainer(
    readingInfo: ReadingInfo,
    onNavigateToCardDetail: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        when (readingInfo.layout) {
            CardLayout.SINGLE -> SingleCardLayout()
            CardLayout.HORIZONTAL_3 -> HorizontalLayout(3)
            CardLayout.PYRAMID_3 -> Pyramid3Layout()
            CardLayout.CROSS_5 -> Cross5Layout()
            CardLayout.PYRAMID_6 -> Pyramid6Layout()
            CardLayout.CROSS_7 -> Cross7Layout()
            CardLayout.COMPATIBILITY_CROSS -> CompatibilityCrossLayout()
            CardLayout.GRID_3x3 -> Grid3x3Layout()
            CardLayout.PATH_5 -> Path5Layout()
            CardLayout.WORK_PROBLEM_6 -> WorkProblemLayout()
            CardLayout.FINANCIAL_4 -> FinancialLayout()
            // Diğer layout'lar için varsayılan
            else -> HorizontalLayout(readingInfo.cardCount)
        }
    }
}

@Composable
private fun CardView(
    modifier: Modifier,
    cardNumber: Int
) {
    Card(
        modifier = modifier
            .clickable { /* Bu sayfada tıklama işlevi yok */ },
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(2.dp, Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cardNumber.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily(Font(R.font.cinzel_bold)),
                    fontSize = 24.sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
fun SingleCardLayout() {
    val cardModifier = Modifier
        .width(130.dp)
        .aspectRatio(0.7f)
    CardView(
        modifier = cardModifier,
        cardNumber = 1
    )
}

@Composable
fun HorizontalLayout(cardCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(0.65f),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(cardCount) { index ->
            CardView(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.7f),
                cardNumber = index + 1
            )
        }
    }
}

@Composable
fun Pyramid3Layout() {
    val cardModifier = Modifier
        .width(100.dp)
        .aspectRatio(0.7f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CardView(
            modifier = cardModifier,
            cardNumber = 1
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CardView(
                modifier = cardModifier,
                cardNumber = 2
            )
            CardView(
                modifier = cardModifier,
                cardNumber = 3
            )
        }
    }
}

@Composable
fun Cross5Layout() {
    val cardModifier = Modifier
        .width(85.dp)
        .aspectRatio(0.7f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CardView(cardModifier, 1)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CardView(cardModifier, 2)
            CardView(cardModifier, 3)
            CardView(cardModifier, 4)
        }
        CardView(cardModifier, 5)
    }
}

@Composable
fun Pyramid6Layout() {
    Box(modifier = Modifier.padding(top = 16.dp), contentAlignment = Alignment.Center) {
        val cardModifier = Modifier
            .width(54.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. sıra (1 kart)
            CardView(cardModifier, 1)
            // 2. sıra (2 kart)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, 2)
                CardView(cardModifier, 3)
            }
            // 3. sıra (3 kart)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, 4)
                CardView(cardModifier, 5)
                CardView(cardModifier, 6)
            }
        }
    }
}

@Composable
fun Cross7Layout() {
    val cardModifier = Modifier
        .width(80.dp)
        .aspectRatio(0.7f)
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardView(cardModifier, 1)
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally)) {
            CardView(cardModifier, 2)
            CardView(cardModifier, 3)
            CardView(cardModifier, 4)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally)) {
            CardView(cardModifier, 5)
            CardView(cardModifier, 6)
            CardView(cardModifier, 7)
        }
    }
}

@Composable
fun CompatibilityCrossLayout() {
    Box(modifier = Modifier.fillMaxSize().padding(top = 24.dp), contentAlignment = Alignment.Center) {
        val cardModifier = Modifier
            .width(43.dp)
            .aspectRatio(0.7f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Row 1: Card 1
            CardView(cardModifier, 1)

            // Row 2: Cards 2 & 3
            Row(horizontalArrangement = Arrangement.spacedBy(90.dp)) {
                CardView(cardModifier, 2)
                CardView(cardModifier, 3)
            }

            // Row 3: Card 4
            CardView(cardModifier, 4)

            // Row 4: Cards 5 & 6
            Row(horizontalArrangement = Arrangement.spacedBy(90.dp)) {
                CardView(cardModifier, 5)
                CardView(cardModifier, 6)
            }

            // Row 5: Card 7
            CardView(cardModifier, 7)
        }
    }
}

@Composable
fun Grid3x3Layout() {
    Box(modifier = Modifier.padding(top = 28.dp)) {
        val cardModifier = Modifier
            .width(58.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(3) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(3) { col ->
                        val index = row * 3 + col + 1
                        CardView(
                            modifier = cardModifier,
                            cardNumber = index
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Path5Layout() {
    Box(modifier = Modifier.padding(top = 16.dp)) {
        val cardModifier = Modifier
            .width(62.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CardView(cardModifier, 1)
            CardView(cardModifier, 2)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, 3)
                CardView(cardModifier, 4)
                CardView(cardModifier, 5)
            }
        }
    }
}

@Composable
fun WorkProblemLayout() {
    Box(modifier = Modifier.padding(top = 16.dp)) {
        val cardModifier = Modifier
            .width(48.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. sıra: 1 kart
            CardView(cardModifier, 1)
            
            // 2. sıra: 4 kart
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, 2)
                CardView(cardModifier, 3)
                CardView(cardModifier, 4)
                CardView(cardModifier, 5)
            }
            
            // 3. sıra: 1 kart
            CardView(cardModifier, 6)
        }
    }
}

@Composable
fun FinancialLayout() {
    Box(modifier = Modifier.padding(top = 16.dp)) {
        val cardModifier = Modifier
            .width(58.dp)
            .aspectRatio(0.7f)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. sıra: 1 kart
            CardView(cardModifier, 1)
            
            // 2. sıra: 2 kart
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, 2)
                CardView(cardModifier, 3)
            }
            
            // 3. sıra: 1 kart
            CardView(cardModifier, 4)
        }
    }
}

fun getReadingDescription(readingType: String): String {
    return when (readingType.trim()) {
        "GÜNLÜK AÇILIM" -> "Günlük açılım, gününüzün genel enerjilerini ve size rehberlik edecek mesajları görmenizi sağlar. Bu açılım, günlük hayatınızın akışını anlamanıza ve gününüzü daha bilinçli yaşamanıza yardımcı olur."
        "TEK KART AÇILIMI" -> "Tek kart açılımı, gününüzün ana temasını veya belirli bir konuda size rehberlik edecek mesajı görmenizi sağlar. Bu basit ama etkili açılım, gününüzün genel durumunu anlamanıza yardımcı olur."
        "EVET – HAYIR AÇILIMI" -> "Evet-Hayır açılımı, net bir soruya cevap almak için kullanılır. Bu açılım, evet/hayır cevabı alabileceğiniz sorular için idealdir ve size net bir yön gösterir."
        "GEÇMİŞ, ŞİMDİ, GELECEK" -> "Geçmiş-Şimdi-Gelecek açılımı, bir durumun zaman çizelgesini görmenizi sağlar. Bu açılım, geçmişin etkilerini, mevcut durumu ve gelecekteki potansiyeli anlamanıza yardımcı olur."
        "DURUM, AKSİYON, SONUÇ" -> "Durum-Aksiyon-Sonuç açılımı, mevcut durumunuzu, yapmanız gerekenleri ve olası sonucu görmenizi sağlar. Bu açılım, karar verme sürecinizde size rehberlik eder."
        "İLİŞKİ AÇILIMI" -> "İlişki açılımı, ilişkinizin farklı yönlerini incelemenizi sağlar. Bu açılım, ilişkinizin dinamiklerini anlamanıza ve daha sağlıklı bir ilişki kurmanıza yardımcı olur."
        "UYUMLULUK AÇILIMI" -> "Uyumluluk açılımı, iki kişi arasındaki uyumu detaylı bir şekilde analiz eder. Bu açılım, ilişkinizin güçlü yanlarını ve geliştirilmesi gereken alanları görmenizi sağlar."
        "DETAYLI İLİŞKİ AÇILIMI" -> "Detaylı ilişki açılımı, ilişkinizin tüm boyutlarını kapsamlı bir şekilde inceler. Bu açılım, ilişkinizin geçmişini, şimdiki durumunu ve geleceğini detaylı olarak analiz eder."
        "MÜCADELELER AÇILIMI" -> "Mücadeleler açılımı, ilişkinizdeki zorlukları ve çözüm yollarını görmenizi sağlar. Bu açılım, problemleri anlamanıza ve çözüm bulmanıza yardımcı olur."
        "TAMAM MI, DEVAM MI" -> "Tamam mı Devam mı açılımı, bir ilişkinin devam edip etmemesi konusunda size rehberlik eder. Bu açılım, karar verme sürecinizde size net bir yön gösterir."
        "GELECEĞİNE GİDEN YOL" -> "Geleceğine Giden Yol açılımı, kariyerinizdeki yolculuğunuzu görmenizi sağlar. Bu açılım, hedeflerinizi, engellerinizi ve başarıya ulaşma yollarınızı analiz eder."
        "İŞ YERİNDEKİ PROBLEMLER" -> "İş Yerindeki Problemler açılımı, iş hayatınızdaki zorlukları ve çözüm yollarını görmenizi sağlar. Bu açılım, iş ortamınızdaki dinamikleri anlamanıza yardımcı olur."
        "FİNANSAL DURUM" -> "Finansal Durum açılımı, para ve maddi konularınızı analiz eder. Bu açılım, finansal durumunuzu, fırsatlarınızı ve dikkat etmeniz gereken alanları görmenizi sağlar."
        else -> "Bu açılım hakkında detaylı bilgi bulunmaktadır."
    }
}

fun getCardMeaningDescription(readingType: String, cardMeaning: String): String {
    return when (readingType.trim()) {
        "GÜNLÜK AÇILIM" -> when (cardMeaning) {
            "Düşünce" -> "Gününüzdeki düşünce kalıplarınızı ve zihinsel durumunuzu temsil eder."
            "His" -> "Gününüzdeki duygusal durumunuzu ve hislerinizi temsil eder."
            "Aksiyon" -> "Gününüzde yapmanız gereken eylemleri ve davranışlarınızı temsil eder."
            else -> "Bu kartın anlamını temsil eder."
        }
        "TEK KART AÇILIMI" -> when (cardMeaning) {
            "Günün Kartı" -> "Gününüzün ana temasını ve size verilen mesajı temsil eder."
            else -> "Bu kartın anlamını temsil eder."
        }
        "EVET – HAYIR AÇILIMI" -> when (cardMeaning) {
            "Cevap" -> "Sorunuzun cevabını ve size verilen yönlendirmeyi temsil eder."
            else -> "Bu kartın anlamını temsil eder."
        }
        "GEÇMİŞ, ŞİMDİ, GELECEK" -> when (cardMeaning) {
            "Geçmiş" -> "Sorunuzla ilgili geçmişteki etkenleri ve temelleri temsil eder."
            "Şimdi" -> "Mevcut durumunuzu ve şu anki koşulları temsil eder."
            "Gelecek" -> "Gelecekteki potansiyeli ve olası sonuçları temsil eder."
            else -> "Bu kartın anlamını temsil eder."
        }
        "DURUM, AKSİYON, SONUÇ" -> when (cardMeaning) {
            "Durum" -> "Mevcut durumunuzu ve koşulları temsil eder."
            "Aksiyon" -> "Yapmanız gereken eylemleri ve davranışları temsil eder."
            "Sonuç" -> "Olası sonuçları ve sonuçları temsil eder."
            else -> "Bu kartın anlamını temsil eder."
        }
        "İLİŞKİ AÇILIMI" -> when (cardMeaning) {
            "Sen" -> "İlişkideki rolünüzü ve bakış açınızı temsil eder."
            "O" -> "Partnerinizin rolünü ve bakış açısını temsil eder."
            "İlişkiniz" -> "İlişkinizin genel durumunu ve dinamiklerini temsil eder."
            else -> "Bu kartın anlamını temsil eder."
        }
        "UYUMLULUK AÇILIMI" -> when (cardMeaning) {
            "Senin Geçmişin" -> "Sizin geçmiş deneyimlerinizi ve etkilerini temsil eder."
            "Onun Geçmişi" -> "Partnerinizin geçmiş deneyimlerini ve etkilerini temsil eder."
            "Sizin Uyumunuz" -> "İkinizin uyum seviyesini ve uyumluluğunuzu temsil eder."
            "Senin Beklentin" -> "Sizin beklentilerinizi ve umutlarınızı temsil eder."
            "Onun Beklentisi" -> "Partnerinizin beklentilerini ve umutlarını temsil eder."
            "İlişkinin Geleceği" -> "İlişkinizin gelecekteki potansiyelini temsil eder."
            "Sonuç" -> "İlişkinizin genel sonucunu ve yönünü temsil eder."
            else -> "Bu kartın anlamını temsil eder."
        }
        "DETAYLI İLİŞKİ AÇILIMI" -> when (cardMeaning) {
            "Geçmiş" -> "İlişkinizin geçmişini ve temellerini temsil eder."
            "Şimdi" -> "İlişkinizin mevcut durumunu temsil eder."
            "Gelecek" -> "İlişkinizin gelecekteki potansiyelini temsil eder."
            "Senin Bilinçaltın" -> "Sizin bilinçaltı düşüncelerinizi ve duygularınızı temsil eder."
            "Onun Bilinçaltı" -> "Partnerinizin bilinçaltı düşüncelerini ve duygularını temsil eder."
            "Dış Etkenler" -> "İlişkinizi etkileyen dış faktörleri temsil eder."
            "Umutlar ve Korkular" -> "İlişkiyle ilgili umutlarınızı ve korkularınızı temsil eder."
            "Potansiyel" -> "İlişkinizin potansiyelini ve gelişim alanlarını temsil eder."
            "Nihai Sonuç" -> "İlişkinizin nihai sonucunu ve yönünü temsil eder."
            else -> "Bu kartın anlamını temsil eder."
        }
        "MÜCADELELER AÇILIMI" -> when (cardMeaning) {
            "Ana Sorun" -> "İlişkinizdeki ana problemi temsil eder."
            "Senin Bakış Açın" -> "Soruna karşı sizin bakış açınızı temsil eder."
            "Onun Bakış Açısı" -> "Soruna karşı partnerinizin bakış açısını temsil eder."
            "Geçmişin Etkisi" -> "Geçmişin mevcut soruna etkisini temsil eder."
            "Çözüm Önerisi" -> "Sorunun çözümü için önerileri temsil eder."
            "Olası Gelecek" -> "Sorun çözüldükten sonraki olası geleceği temsil eder."
            "Nihai Tavsiye" -> "Size verilen nihai tavsiyeyi temsil eder."
            else -> "Bu kartın anlamını temsil eder."
        }
        "TAMAM MI, DEVAM MI" -> when (cardMeaning) {
            "İlişkinin Temeli" -> "İlişkinizin temelini ve gücünü temsil eder."
            "Mevcut Durum" -> "İlişkinizin mevcut durumunu temsil eder."
            "Devam Etme Potansiyeli" -> "İlişkinin devam etme potansiyelini temsil eder."
            "Bitirme Potansiyeli" -> "İlişkinin bitme potansiyelini temsil eder."
            "Senin İçin En İyisi" -> "Sizin için en iyi olanı temsil eder."
            "Nihai Karar" -> "Size verilen nihai kararı temsil eder."
            else -> "Bu kartın anlamını temsil eder."
        }
        "GELECEĞİNE GİDEN YOL" -> when (cardMeaning) {
            "Mevcut Durumun" -> "Kariyerinizdeki mevcut durumunuzu temsil eder."
            "Hedefin" -> "Kariyer hedeflerinizi temsil eder."
            "Engellerin" -> "Hedefinize ulaşmanızdaki engelleri temsil eder."
            "Yardımcı Etkenler" -> "Size yardımcı olacak faktörleri temsil eder."
            "Atman Gereken Adım" -> "Hedefinize ulaşmak için atmanız gereken adımı temsil eder."
            else -> "Bu kartın anlamını temsil eder."
        }
        "İŞ YERİNDEKİ PROBLEMLER" -> when (cardMeaning) {
            "Problemin Kökü" -> "İş yerindeki problemin temel nedenini temsil eder."
            "Seni Etkileyen Faktör" -> "Problemin sizi nasıl etkilediğini temsil eder."
            "Diğerlerini Etkileyen Faktör" -> "Problemin diğerlerini nasıl etkilediğini temsil eder."
            "Gözden Kaçırdığın" -> "Gözden kaçırdığınız faktörleri temsil eder."
            "Çözüm Yolu" -> "Problemin çözümü için yolu temsil eder."
            "Sonuç" -> "Problemin çözümünden sonraki sonucu temsil eder."
            else -> "Bu kartın anlamını temsil eder."
        }
        "FİNANSAL DURUM" -> when (cardMeaning) {
            "Mevcut Finansal Durum" -> "Şu anki finansal durumunuzu temsil eder."
            "Para Akışın" -> "Para akışınızı ve gelir-gider durumunuzu temsil eder."
            "Engeller" -> "Finansal hedeflerinize ulaşmanızdaki engelleri temsil eder."
            "Fırsatlar" -> "Finansal fırsatlarınızı ve potansiyelinizi temsil eder."
            else -> "Bu kartın anlamını temsil eder."
        }
        else -> "Bu kartın anlamını temsil eder."
    }
} 