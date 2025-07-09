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
            CardLayout.FINANCIAL_6 -> FinancialLayout()
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
            // 2. sıra: 3 kart
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, 2)
                CardView(cardModifier, 3)
                CardView(cardModifier, 4)
            }
            // 3. sıra: 2 kart
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CardView(cardModifier, 5)
                CardView(cardModifier, 6)
            }
        }
    }
}

fun getReadingDescription(readingType: String): String {
    return when (readingType.trim()) {
        "GÜNLÜK AÇILIM" -> "Güne başlarken niyetinizi belirlemek ve günün enerjilerine uyumlanmak için bu açılımdan faydalanabilirsiniz. Bu açılım, güne dair bir farkındalık meditasyonu gibidir; seçilen üç kart sırasıyla gün içindeki zihinsel odağınızı, duygusal atmosferinizi ve eylemsel olarak atmanız gereken adımları sembolize eder. Size özel sunulan bu rehberlik, karşınıza çıkacak durumlara daha hazırlıklı ve bilinçli yaklaşmanızı sağlar. Günlük yol haritanızı çizerek potansiyelinizi en üst seviyeye çıkarabilir, günü daha verimli ve anlamlı kılabilirsiniz. Her yeni gün, yeni bir başlangıçtır ve bu açılım o başlangıcı en doğru şekilde yapmanıza yardımcı olur.\n\nİpucu: Günlük açılım günde bir defa o günün enerjilerini yorumlamak için yapılır."
        "TEK KART AÇILIMI" -> "Zamanınız kısıtlı olduğunda veya aklınızdaki spesifik bir konu için anlık bir rehberliğe ihtiyaç duyduğunuzda, tek kart seçimi en güçlü ve pratik yardımcınızdır. Bu yöntem, evrenin size o anki mesajını en öz şekilde iletir. Seçtiğiniz kart, o an bilmeniz gereken en önemli bilgiyi, odaklanmanız gereken ana temayı veya ihtiyacınız olan tavsiyeyi size sunar. Karmaşık durumları basitleştirerek anın özünü yakalamanızı ve ihtiyacınız olan ilhamı bulmanızı sağlar. Bu kartı gün boyunca size yol gösterecek bir \"manevi çapa\" veya gününüzün mottosu olarak benimseyebilirsiniz."
        "EVET – HAYIR AÇILIMI" -> "Aklınızda net bir \"evet\" ya da \"hayır\" cevabı gerektiren bir soru olduğunda, bu açılım size bir yön gösterebilir. Ancak unutmamanız gerekir ki tarot, basit bir madeni para atışı değildir; çok daha derin bir bilgelik sunar. Seçtiğiniz kart, sadece olumlu veya olumsuz bir yönelim belirtmekle kalmaz, aynı zamanda bu cevabın arkasındaki enerjileri ve koşulları da açıklar. Cevabı \"evet\" yapan destekleyici güçleri veya \"hayır\" cevabına yönlendiren koruyucu engelleri anlamanızı sağlar. Böylece, sadece yüzeysel bir cevap almakla kalmaz, durumun ruhunu ve size ne öğretmek istediğini de kavramış olursunuz."
        "GEÇMİŞ, ŞİMDİ, GELECEK" -> "Hayatınızdaki bir durumun nereden kaynaklandığını, şu anda nerede durduğunu ve nereye doğru evrildiğini anlamak için bu klasik ve güçlü açılımı kullanabilirsiniz. İlk kart, konunun kökenini, geçmişin bugüne olan etkilerini ve öğrenilen dersleri gösterir. Ortadaki kart, mevcut durumun gerçekliğini ve şu anki dinamikleri objektif bir şekilde aydınlatır. Son kart ise, mevcut yolda devam etmeniz halinde karşılaşacağınız olası geleceği ve potansiyel sonucu ortaya koyarak size bir öngörü sunar. Bu açılım, olaylar arasındaki sebep-sonuç ilişkisini görmeniz ve geleceği şekillendirme gücünüzü fark etmeniz için size berrak bir zaman çizgisi çizer."
        "DURUM, AKSİYON, SONUÇ" -> "Bir yol ayrımında hissettiğinizde veya atacağınız önemli bir adımın olası sonuçlarını öngörmek istediğinizde bu açılım en iyi stratejik rehberinizdir. İlk kart, içinde bulunduğunuz durumu tüm çıplaklığıyla tanımlayarak size objektif bir zemin sunar. İkinci kart, değerlendirdiğiniz potansiyel eylemi veya izleyebileceğiniz bir yolu temsil eder. Üçüncü ve son kart ise, o eylemi gerçekleştirmeniz durumunda ortaya çıkması en muhtemel sonucu gözler önüne serer. Karar verme süreçlerinizde size pratik bir içgörü ve stratejik bir bakış açısı kazandırarak en doğru ve bilinçli adımı atmanıza yardımcı olur."
        "İLİŞKİ AÇILIMI" -> "Mevcut ilişkinizin veya duygusal bağınızın anlık enerjisini ve atmosferini anlamak için bu temel açılımdan yararlanabilirsiniz. Bu üç kart, ilişkinin mevcut dinamiğini, temelindeki ana temayı ve kısa vadeli potansiyelini bir anlık fotoğraf gibi net bir şekilde gözler önüne serer. Adeta ilişkinizin \"nabzını ölçmek\" gibidir; her şey yolunda giderken de, belirsizlik anlarında da kullanılabilir. Bağlantınızdaki ana enerjiyi hızla kavramanıza yardımcı olarak neyin yolunda gittiğini veya neye odaklanmanız gerektiğini kolayca anlamanızı sağlar."
        "UYUMLULUK AÇILIMI" -> "Partnerinizle aranızdaki uyumun derinliklerini ve çok katmanlı yapısını keşfetmek için bu özel açılımı seçebilirsiniz. Bu yerleşim, ilişkinizi üç temel seviyede analiz eder: Kalplerin buluştuğu duygusal uyum, zihinlerin anlaştığı düşünsel uyum ve tenlerin konuştuğu fiziksel uyum. Kartlar, sizin ve partnerinizin bu alanlardaki enerjilerini, birbirinizi nasıl beslediğinizi ve hangi noktalarda dengeye ihtiyaç duyduğunuzu gösterir. Aranızdaki bağın güçlü yönlerini kutlamanız ve geliştirilmesi gereken tarafları ise şefkatle ele almanız için size bir yol haritası sunar."
        "DETAYLI İLİŞKİ AÇILIMI" -> "İlişkinizin kapsamlı bir analizini yaparak geçmişin temellerini, şimdinin gerçeklerini ve geleceğin potansiyellerini bütüncül bir bakışla görmek istiyorsanız, bu açılım tam size göredir. Dokuz kartlık bu güçlü yorum, ilişkinizi kalp (duygular), zihin (düşünceler) ve eylem (davranışlar) ekseninde, geçmiş, şimdi ve gelecek zaman dilimlerinde inceler. Bu, sadece ne olduğunu değil, aynı zamanda söze dökülmemiş duyguları, zihinsel süreçleri ve davranış kalıplarını da anlamanızı sağlar. İlişkinizin adeta bir röntgenini çekerek size en derin katmanlara inen, bütüncül bir bakış açısı sunan bir bilgelik yolculuğudur."
        "MÜCADELELER AÇILIMI" -> "Her ilişkide zaman zaman zorluklar ve anlaşmazlıklar yaşanması doğal bir süreçtir. Bu açılım, ilişkinizdeki mevcut sorunların yüzeydeki belirtilerinin ötesine geçerek, kökenine inmek ve kalıcı çözümler bulmak için tasarlanmıştır. Tartışmaların ardındaki gerçek sebepleri, karşılanmamış beklentileri ve sizi zorlayan gizli dinamikleri şefkatle aydınlatır. Bu açılımın amacı suçlu aramak değil, ortak bir anlayış ve çözüm yolu bulmaktır. Engelleri birlikte aşmanız için size yapıcı ve eyleme dönük bir rehberlik sunarak bağınızı daha da güçlendirir."
        "TAMAM MI, DEVAM MI" -> "İlişkinizin kritik bir dönüm noktasında olduğunu hissediyor ve bir karar vermenin ağırlığını taşıyorsanız, bu açılım size ihtiyaç duyduğunuz berraklığı sunacaktır. Bu altı kartlık açılım, mevcut durumda kalmanın ve yola devam etmenin potansiyellerini objektif bir şekilde tartar. Bir yanda ilişkinin size kattıklarını, diğer yanda ise bitişin ardındaki nedenleri ve potansiyel özgürleşmeyi görmenizi sağlar. Hangi yolun sizin ruhsal gelişiminiz ve uzun vadedeki mutluluğunuz için daha iyi olduğunu anlamanıza yardımcı olur. Bu, kalbinizle mantığınız arasına sıkıştığınızda başvurabileceğiniz en dürüst ve şefkatli rehberdir."
        "GELECEĞİNE GİDEN YOL" -> "Kariyer hedeflerinizi belirlediniz ama o zirveye giden patikayı çizmekte zorlanıyor musunuz? Bu açılım, sizin için kişisel bir kariyer yol haritası tasarlar. Mevcut profesyonel konumunuzdan yola çıkarak hedeflerinize giden yoldaki somut adımları, karşılaşabileceğiniz potansiyel engelleri ve bu yolda size destek olacak içsel ve dışsal kaynakları belirler. Bu kartlar, stratejik bir plan oluşturmanıza, motivasyonunuzu tazelemenize ve daha önce düşünmediğiniz alternatif yolları keşfetmenize yardımcı olur. Hayallerinizdeki kariyere ulaşmak için atmanız gereken adımları netleştirerek hedefinizi ulaşılabilir kılar."
        "İŞ YERİNDEKİ PROBLEMLER" -> "İş yerinde tekrar eden sorunlar, gerginlikler veya iletişim kopuklukları enerjinizi mi düşürüyor? Bu açılım, ofis ortamındaki problemlerin sadece görünen yüzünü değil, görünmeyen kök nedenlerini de ortaya çıkarmak için tasarlanmıştır. Mevcut sorunu, sizin ve diğer kişilerin bu duruma olan bilinçli veya bilinçsiz etkisini ve ortamdaki gizli dinamikleri analiz eder. Bu sayede, olaylara daha geniş ve objektif bir perspektiften bakarak çözüm için en doğru yaklaşımı belirleyebilirsiniz. İş hayatınızda huzuru, uyumu ve verimliliği yeniden tesis etmenize yardımcı olacak değerli içgörüler sunar."
        "FİNANSAL DURUM" -> "Maddi durumunuzu daha derin bir seviyede anlamak ve finansal hedeflerinize ulaşmak için neye ihtiyacınız olduğunu keşfetmek istiyorsanız, bu açılımı seçebilirsiniz. Kartlar, mevcut finansal durumunuzun bir anlık görüntüsünü sunmanın ötesine geçer; parayla olan duygusal ilişkinizi ve bilinçaltınızdaki 'para inançlarınızı' yansıtır. Bolluk ve bereketin önündeki engelleri ve bu akışı serbest bırakmak için atmanız gereken adımları gösterir. Sadece bütçenizi değil, paraya dair zihniyetinizi de dönüştürmeniz için size pratik ve ruhsal bir rehberlik yaparak finansal refahınızın önünü açar."
        else -> "Bu açılım hakkında detaylı bilgi bulunmaktadır."
    }
}

fun getCardMeaningDescription(readingType: String, meaning: String): String {
    if (readingType.trim() == "UYUMLULUK AÇILIMI") {
        return when (meaning) {
            "Duygusal uyumunuz" -> "Bu ilişkideki duygusal uyumluluğunuz nasıl? Birbirinize nasıl hissettiriyorsunuz?"
            "Sizin istekleriniz" -> "Siz bu ilişkiden ne istiyorsunuz?"
            "Partnerinizin istekleri" -> "Partneriniz bu ilişkiden ne istiyor?"
            "Fiziksel uyumunuz" -> "İlişkinizdeki fiziksel uyumunuz nasıl? Fiziksel olarak birbirinizi çekici buluyor musunuz?"
            "Farklılıklar" -> "Partneriniz ile temel farklılıklarınız neler? Temelde anlaşamadığınız konular ve durumlar neler?"
            "Benzerlikler" -> "Hangi konularda benzersiniz? Partneriniz ile anlaştığınız ve onu anladığınız noktalar neler? Hayata bakış açınızda aynı olduğunuz noktalar neler?"
            "Mental uyumunuz" -> "İlişkinizdeki mental uyumunuz nasıl? Düşünceleriniz, değerleriniz ve idealleriniz arasındaki uyumun farkına varın."
            else -> ""
        }
    }
    // Diğer açılımlar için eski açıklama metinlerini geri getir
    return when (readingType.trim()) {
        "GÜNLÜK AÇILIM" -> when (meaning) {
            "Düşünce" -> "Bugün aklınızda genel olarak hangi düşünceler olacak? Bugün en çok ne ile ilgili düşüneceksiniz."
            "His" -> "Bugün neler hissedeceksiniz? Bugünkü duygu durumunuzdaki en hakim duygular neler olacak?"
            "Aksiyon" -> "Bugünkü genel eylemleriniz neler olacak? Neler yapacaksınız?"
            else -> "Bu kartın anlamını temsil eder."
        }
        "TEK KART AÇILIMI" -> when (meaning) {
            "Günün Kartı" -> "Sorunuz ile ilgili genel etkiler neler? Evrenden size ulaşan tavsiyeler neler? Ne yapmalısınız ya da sorunuz ile ilgili neleri dikkate almalısınız?"
            else -> "Bu kartın anlamını temsil eder."
        }
        "EVET – HAYIR AÇILIMI" -> when (meaning) {
            "Cevap" -> "Sorduğunuz sorunun güncel enerjilerine göre Evet ya da Hayır cevabınız."
            else -> "Bu kartın anlamını temsil eder."
        }
        "GEÇMİŞ, ŞİMDİ, GELECEK" -> when (meaning) {
            "Geçmiş" -> "Şimdiki durumunuza gelmenize sebep olan geçmiş; inanç, etki, duygu ya da düşünceler."
            "Şimdi" -> "Şuan, şimdi, burada… Hali hazırda aktif olan enerjiler nedir? Şuanki durumunuzda hangi enerjiler sizi etkiliyor. Hangi güçler aktif?"
            "Gelecek" -> "Gelecekte olabilecek durumlar nelerdir?"
            else -> "Bu kartın anlamını temsil eder."
        }
        "DURUM, AKSİYON, SONUÇ" -> when (meaning) {
            "Durum" -> "Şu anki durumunuz nedir?"
            "Aksiyon" -> "Almayı planladığınız ya da alacağını aksiyonlar."
            "Sonuç" -> "Gerçekleştirdiğiniz aksiyon sonucunda ortaya çıkması muhtemel sonuçlar."
            else -> "Bu kartın anlamını temsil eder."
        }
        "İLİŞKİ AÇILIMI" -> when (meaning) {
            "Sen" -> "İlişkideki güncel durumunuzu temsil eder."
            "O" -> "İlişkide olduğunuz partnerinizin güncel durumunu temsil eder."
            "İlişkiniz" -> "İlişkinizdeki güncel durum nedir? Evren enerjilerine göre ilişkiniz nasıl görünüyor? Güncel karakteristik özelliği nedir?"
            else -> "Bu kartın anlamını temsil eder."
        }
        "DETAYLI İLİŞKİ AÇILIMI" -> when (meaning) {
            "Geçmişteki Duygular" -> "İlişkinin geçmişinde hakim olan duygusal atmosfer neydi?"
            "Mevcut Duygular" -> "Şu anda ilişkinin kalbinde yatan duygular nelerdir?"
            "Gelecekteki Duygular" -> "İlişkinin gelecekte evrileceği muhtemel duygusal durum nedir?"
            "Geçmişteki Düşünceler" -> "Geçmişte ilişkiye dair zihinsel tutumunuz ve düşünceleriniz nasıldı?"
            "Mevcut Düşünceler" -> "Şu anda ilişki hakkındaki baskın düşünceler ve zihinsel süreçler nelerdir?"
            "Gelecekteki Düşünceler" -> "Gelecekte ilişkiyle ilgili zihinsel odağınız ne yönde olacak?"
            "Geçmişteki Eylemler" -> "Geçmişte ilişkinizi şekillendiren eylemler nelerdi?"
            "Mevcut Eylemler" -> "Şu anki davranışlarınız ve eylemleriniz ilişkiyi nasıl etkiliyor?"
            "Gelecekteki Eylemler/Sonuç" -> "Tüm bu dinamiklerin birleşimiyle ortaya çıkacak en olası eylem veya nihai sonuç nedir?"
            else -> "Bu kartın anlamını temsil eder."
        }
        "MÜCADELELER AÇILIMI" -> when (meaning) {
            "İlişkinizdeki problemde sizin rolünüz" -> "Bu problemde sizin rolünüz nedir? Eylem ve düşünceleriniz problem üzerinde nasıl bir etki yaratıyor."
            "Problemde partnerinizin rolü" -> "Bu sorunu ne büyütüyor veya ne tetikliyor? Bu problemde partnerinizin rolü nedir? Eylem ve düşünceleri problem üzerinde nasıl bir etki yaratıyor."
            "Sizin için tavsiye" -> "İlişkinin düzelmesi için evren enerjileri ne diyor? Bu problemin çözülmesinde sizin katkınız ne olabilir?"
            "Partneriniz için tavsiye" -> "Partneriniz ilişkinin düzelmesi için hangi adımları atabilir? İlişkinin eski sağlığına kavuşması için ne yapması lazım?"
            "Dış etkiler" -> "Dışarıdan gelen hangi etkiler ilişkinizdeki problemi besliyor?"
            "İlişkiniz kurtarılabilir mi?" -> "Bu kırılma noktası yaşanıp bittikten sonra ilişkiniz devam edecek mi?"
            "İlişkiniz kurtarılmaya değer mi?" -> "Bütün bu zorluk ve mücadelelere karşın ilişkiniz gerçekten kurtarılmaya değer mi? İlişkinin devamı hem sizin hem de partneriniz için sağlıklı mı? Gerçekten mutlu olacak mısınız?"
            else -> "Bu kartın anlamını temsil eder."
        }
        "TAMAM MI, DEVAM MI" -> when (meaning) {
            "İlişkinin Mevcut Durumu" -> "Şu anki durumun en dürüst ve çıplak özeti nedir?"
            "Devam Etmenin Potansiyeli" -> "Bu ilişkide kalırsanız, sizi ne bekliyor? Bu yolun size sunacağı en olumlu potansiyel nedir?"
            "Bitirmenin Potansiyeli" -> "Bu ilişkiyi bitirirseniz, sizi ne bekliyor? Bu yolun size getireceği ders veya özgürleşme nedir?"
            "İlişki devam ederse hissedecekleriniz" -> "İlişkinizi devam ettirirseniz duygusal dünyanızda sizi bekleyen durumlar nelerdir?"
            "İlişkiniz biterse hissedecekleriniz" -> "İlişkinizi bitirirseniz duygusal dünyanızda sizi bekleyen durumlar nelerdir?"
            "Genel tavsiye" -> "Bitirmek ya da devam etmek… Bu iki sonuç arasında kaldığınızda dikkate almanız gereken durumlar nelerdir? Hangi konuları düşünmeli, tartmalı ve ona göre karar vermelisiniz?"
            else -> "Bu kartın anlamını temsil eder."
        }
        "GELECEĞİNE GİDEN YOL" -> when (meaning) {
            "Hayalinizdeki kariyer" -> "Hayalinizdeki iş neye benziyor? Ömrünüzün geri kalanında yapmak istediğiniz ve asla sıkılmayacağınız o iş nedir? Sizin rolünüz nedir? Bu yolda neler bulacaksınız?"
            "Potansiyel yollar" -> "Hayalinizdeki kariyere giden potansiyel yollar neler? İstediğiniz noktaya nasıl ulaşabilirsiniz?"
            "Yetenekleriniz" -> "Hayalinizdeki kariyere ulaşmak için sahip olduğunuz yetenekleriniz neler?"
            "Yardımcı olabilecek kaynaklar" -> "Size kim yardımcı olabilir? Hayalinizdeki kariyere giden yolda yararlanabileceğiniz bağlantılarınız neler veya kimler?"
            "Dikkat etmeniz gerekenler" -> "Hedefinize giderken neleri gözden kaçırmamanız gerekiyor?"
            else -> "Bu kartın anlamını temsil eder."
        }
        "İŞ YERİNDEKİ PROBLEMLER" -> when (meaning) {
            "Hedefiniz" -> "İş yerindeki problemin temel nedenini temsil eder."
            "İşinizdeki Engel" -> "Problemin sizi nasıl etkilediğini temsil eder."
            "Sizi İşinizden Geri Çeken Etmenler" -> "Problemin diğerlerini nasıl etkilediğini temsil eder."
            "Sizi İşinizde İleri İten Etmenler" -> "Gözden kaçırdığınız faktörleri temsil eder."
            "İşinizin Size Kazandırdıkları" -> "Problemin çözümü için yolu temsil eder."
            "Gizli Kalmış Sebepler" -> "Problemin çözümünden sonraki sonucu temsil eder."
            else -> "Bu kartın anlamını temsil eder."
        }
        "FİNANSAL DURUM" -> when (meaning) {
            "Kök Neden" -> "Finansal durumunuzun temelindeki asıl sebep nedir? Bu kart, mevcut durumun kökenindeki ana dinamiği veya başlangıç noktasını aydınlatıyor."
            "Karşıt Güçler" -> "Size karşı çalışan veya finansal hedeflerinize ulaşmanızı zorlaştıran içsel yada dışsal etkenler nelerdir? Bu kart, önünüzdeki engelleri gösteriyor."
            "Eylem Planı" -> "Mevcut finansal durumu iyileştirmek için atabileceğiniz en etkili ve yapıcı adım nedir? Bu kart, size durumu lehinize çevirecek somut bir eylem planı sunar."
            "Destekleyici Güçler" -> "Başarınıza yardımcı olan, lehinize işleyen güçler, yetenekler veya durumlar nelerdir? Bu kart, sahip olduğunuz avantajları ve size destek olan müttefikleri ortaya çıkarır."
            "Yardım Kaynakları" -> "Finansal yolculuğunuzda nereden veya kimden yardım alabilirsiniz? Bu kart, başvurabileceğiniz dış kaynakları, kişileri veya beklenmedik fırsatları işaret eder."
            "Olası Sonuç" -> "Mevcut enerjiler ve dinamikler göz önüne alındığında, bu yolun sizi götüreceği en olası finansal sonuç nedir? Bu kart, yakın gelecekteki potansiyel durumu öngörür."
            else -> "Bu kartın anlamını temsil eder."
        }
        else -> "Bu kartın anlamını temsil eder."
    }
} 