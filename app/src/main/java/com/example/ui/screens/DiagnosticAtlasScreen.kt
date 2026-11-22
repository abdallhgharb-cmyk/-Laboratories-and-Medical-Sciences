package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import com.example.ui.theme.ClinicalTeal
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MedicalBlueDark
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.ScreenDestination

data class AtlasItem(
    val assetPath: String,
    val titleAr: String,
    val titleEn: String,
    val categoryAr: String,
    val magnification: String,
    val clinicalDescription: String
)

val ATLAS_ITEMS = listOf(
    AtlasItem(
        assetPath = "atlas/hematology/smear_sickle_cells_highres.webp",
        titleAr = "مسحة دم محيطية - أنيميا الخلايا المنجلية (HbSS)",
        titleEn = "Peripheral Blood Smear - Sickle Cell Anemia (Drepanocytes)",
        categoryAr = "أمراض الدم | Hematology",
        magnification = "100x Oil Immersion",
        clinicalDescription = "مسحة دم مصبوغة بصبغة رايت-جيمزا تُظهر خلايا منجلية متطاولة مع قلة تصبغ وكريات دم حمراء متباينة الأشكال."
    ),
    AtlasItem(
        assetPath = "atlas/hematology/smear_megaloblastic_anemia.webp",
        titleAr = "مسحة دم - الأنيميا الضخمة الأرومات ونقص B12",
        titleEn = "Peripheral Blood Smear - Megaloblastic Anemia",
        categoryAr = "أمراض الدم | Hematology",
        magnification = "100x Oil Immersion",
        clinicalDescription = "تُظهر خلايا عدلات مفرطة التفصص (Hypersegmented Neutrophils >= 6 lobes) مع خلايا بيضاوية ضخمة (Macro-ovalocytes)."
    ),
    AtlasItem(
        assetPath = "atlas/urinalysis/crystals_calcium_oxalate_envelope.webp",
        titleAr = "راسب البول - كريستالات أكسالات الكالسيوم ثنائية الهيدرات",
        titleEn = "Urinary Sediment - Calcium Oxalate Dihydrate Crystals",
        categoryAr = "فحص البول | Urinalysis",
        magnification = "40x High Power Field",
        clinicalDescription = "بلورات ثنائية الهرم على شكل مظروف (Envelope-shaped) تنكسر تحت الضوء المستقطب في البول الحمضي إلى المتعادل."
    ),
    AtlasItem(
        assetPath = "atlas/urinalysis/casts_rbc_glomerulonephritis.webp",
        titleAr = "راسب البول - أسطوانات كريات الدم الحمراء التشخيصية",
        titleEn = "Urinary Sediment - Pathognomonic Red Blood Cell Casts",
        categoryAr = "فحص البول | Urinalysis",
        magnification = "40x High Power Field",
        clinicalDescription = "أسطوانات بولية مدمجة بكريات دم حمراء مشوهة تشير حتماً إلى التهاب كبيبات الكلى الحاد (Acute Glomerulonephritis)."
    ),
    AtlasItem(
        assetPath = "atlas/parasitology/entamoeba_histolytica_cyst.webp",
        titleAr = "طفيليات البراز - كيسة الأميبا الحالة للنسج رباعية النوى",
        titleEn = "Stool Parasitology - Entamoeba histolytica Quadrinucleate Cyst",
        categoryAr = "طفيليات | Parasitology",
        magnification = "100x Oil Immersion",
        clinicalDescription = "كيسة كروية ناضجة تحتوي على أربع نوى مركزية الكاريوسوم مع أجسام كروماتويدية دائرية الأطراف."
    ),
    AtlasItem(
        assetPath = "atlas/parasitology/giardia_lamblia_trophozoite.webp",
        titleAr = "طفيليات البراز - الطور النشط لحيوان الجيارديا اللامبلية",
        titleEn = "Stool Parasitology - Giardia lamblia Trophozoite",
        categoryAr = "طفيليات | Parasitology",
        magnification = "100x Oil Immersion",
        clinicalDescription = "طور نشط كمثري الشكل متناظر الجانبين يحتوي على نواتين وسياط حركية يظهر كوجه قرد مميز."
    ),
    AtlasItem(
        assetPath = "atlas/microbiology/gram_stain_staphylococcus.webp",
        titleAr = "صبغة جرام - المكورات العنقودية الموجبة لجرام",
        titleEn = "Microbiology - Gram-Positive Cocci in Clusters",
        categoryAr = "أحياء دقيقة | Microbiology",
        magnification = "100x Oil Immersion",
        clinicalDescription = "مكورات بنفسجية غامقة متجمعة في عناقيد تشبه عناقيد العنب موجبة الكاتاليز والكوآجيوليز (S. aureus)."
    ),
    AtlasItem(
        assetPath = "atlas/microbiology/macconkey_agar_lactose_fermenters.webp",
        titleAr = "الأطباق الزرعية - مستعمرات بكتيرية مخمرة للاكتوز",
        titleEn = "Culture Plate - Lactose-Fermenting Bacilli (MacConkey Agar)",
        categoryAr = "أحياء دقيقة | Microbiology",
        magnification = "Macroscopic Culture",
        clinicalDescription = "مستعمرات وردية أرجوانية على طبق ماكونكي مع ترسب أملاح الصفراء تؤكد تخمير اللاكتوز (E. coli)."
    ),
    AtlasItem(
        assetPath = "atlas/cardiology_ecg/ecg_inferior_stemi_12lead.webp",
        titleAr = "تخطيط القلب السريري - احتشاء عضلة القلب السفلي (Inferior STEMI)",
        titleEn = "12-Lead ECG - Acute Inferior Wall STEMI",
        categoryAr = "طب القلب | Cardiology",
        magnification = "Standard 25mm/s Grid",
        clinicalDescription = "ارتفاع قطعي في القطعة ST في الاتجاهات السفلى II, III, aVF مع انخفاض متبادل في I و aVL."
    ),
    AtlasItem(
        assetPath = "atlas/cardiology_ecg/ecg_ventricular_fibrillation.webp",
        titleAr = "تخطيط القلب الحرج - الرجفان البطيني (Ventricular Fibrillation)",
        titleEn = "Critical ECG Strip - Coarse Ventricular Fibrillation",
        categoryAr = "طب القلب | Cardiology",
        magnification = "Rhythm Strip",
        clinicalDescription = "نشاط كهربائي قلبي فوضوي وغير منتظم بدون أي مركبات QRS أو موجات P يمكن تمييزها - إيقاع صدمة حرج."
    ),
    AtlasItem(
        assetPath = "atlas/radiology_xray/chest_xray_lobar_pneumonia.webp",
        titleAr = "الأشعة السينية للصدر - الالتهاب الرئوي الفصي الحاد",
        titleEn = "Chest Radiography - Right Middle Lobe Lobar Pneumonia",
        categoryAr = "أشعة | Radiology",
        magnification = "PA Chest X-Ray",
        clinicalDescription = "عتمة شعاعية كثيفة مع علامة القصبات الهوائية (Air Bronchogram) محددة في الفص الرئوي الأيمن."
    ),
    AtlasItem(
        assetPath = "atlas/radiology_xray/chest_xray_tension_pneumothorax.webp",
        titleAr = "الأشعة السينية للصدر - استرواح الصدر الضاغط الحرج",
        titleEn = "Emergency Chest X-Ray - Tension Pneumothorax",
        categoryAr = "أشعة | Radiology",
        magnification = "AP Portable X-Ray",
        clinicalDescription = "غياب تام للعلامات الرئوية الوعائية في نصف الصدر المصاب مع انحراف المنصف والرغامي للجهة المعاكسة."
    ),
    AtlasItem(
        assetPath = "atlas/histopathology/bone_marrow_biopsy.webp",
        titleAr = "علم الأمراض الخلوي - خزعة نخاع العظم الطبيعية",
        titleEn = "Clinical Histopathology - Normocellular Bone Marrow Biopsy",
        categoryAr = "علم الأمراض | Pathology",
        magnification = "40x Light Microscopy",
        clinicalDescription = "نخاع عظم طبيعي الخلوية بنسبة 50% خلايا مكونة للدم و 50% نسيج شحمي مع نواءات خلايا سليمة (Megakaryocytes)."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticAtlasScreen(viewModel: AssessmentViewModel) {
    val context = LocalContext.current

    BackHandler {
        viewModel.navigateTo(ScreenDestination.Home)
    }

    var selectedCategory by remember { mutableStateOf("الكل") }
    var viewingItem by remember { mutableStateOf<AtlasItem?>(null) }

    val categories = listOf("الكل", "أمراض الدم", "فحص البول", "طفيليات", "أحياء دقيقة", "طب القلب", "أشعة", "علم الأمراض")

    val filteredItems = remember(selectedCategory) {
        if (selectedCategory == "الكل") {
            ATLAS_ITEMS
        } else {
            ATLAS_ITEMS.filter { it.categoryAr.contains(selectedCategory) }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "الأطلس السريري والمجهري عالي الدقة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Full Offline WebP Diagnostic Atlas (2048x1600)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD6E3FF)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.Home) },
                        modifier = Modifier.testTag("atlas_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MedicalBluePrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("diagnostic_atlas_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MedicalBlueDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مكتبة الأطلس التشخيصية المدمجة أوفلاين بالكامل",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تتضمن مكتبة الأطلس شرائح مجهرية وسريرية فائقة الدقة (2048x1600) مخزنة محلياً بالكامل لتعمل بدون إنترنت وتدعم التكبير والتصغير التفاعلي.",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Categories Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MedicalBluePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Atlas Cards
            items(filteredItems) { item ->
                AtlasCard(
                    context = context,
                    item = item,
                    onClick = { viewingItem = item }
                )
            }
        }
    }

    viewingItem?.let { item ->
        AtlasZoomViewerDialog(
            context = context,
            item = item,
            onDismiss = { viewingItem = null }
        )
    }
}

@Composable
fun AtlasCard(
    context: Context,
    item: AtlasItem,
    onClick: () -> Unit
) {
    val thumbRequest = remember(context, item.assetPath) {
        ImageRequest.Builder(context)
            .data("file:///android_asset/${item.assetPath}")
            .size(512, 400)
            .scale(Scale.FILL)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .memoryCacheKey("atlas_thumb_${item.assetPath}")
            .diskCacheKey("atlas_thumb_${item.assetPath}")
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("atlas_card_${item.assetPath.hashCode()}"),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(Color(0xFF0F172A))
            ) {
                SubcomposeAsyncImage(
                    model = thumbRequest,
                    contentDescription = item.titleAr,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF93C5FD),
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.5.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = item.titleAr,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                )

                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "عرض بدقة 2048x1600", color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = ClinicalTeal.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.categoryAr,
                            color = ClinicalTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = item.magnification,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.titleAr,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = item.titleEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MedicalBluePrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.clinicalDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun AtlasZoomViewerDialog(
    context: Context,
    item: AtlasItem,
    onDismiss: () -> Unit
) {
    val fullResRequest = remember(context, item.assetPath) {
        ImageRequest.Builder(context)
            .data("file:///android_asset/${item.assetPath}")
            .size(2048, 1600)
            .scale(Scale.FIT)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .memoryCacheKey("atlas_full_${item.assetPath}")
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .crossfade(true)
            .build()
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.8f, 5.0f)
        offset += panChange
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .testTag("atlas_zoom_dialog")
        ) {
            // Main Zoomable View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(state = transformState),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = fullResRequest,
                    contentDescription = item.titleAr,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF93C5FD))
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = item.titleAr,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                )
            }

            // Top Header
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.titleAr,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${item.titleEn} • ${item.magnification}",
                            color = Color(0xFFB0BEC5),
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("close_atlas_zoom")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            // Bottom Zoom Controller
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scale = (scale - 0.4f).coerceAtLeast(1f) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "التكبير: ${(scale * 100).toInt()}% • اسحب للمعاينة",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { scale = (scale + 0.4f).coerceAtMost(4.5f) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                    }
                }
            }
        }
    }
}
