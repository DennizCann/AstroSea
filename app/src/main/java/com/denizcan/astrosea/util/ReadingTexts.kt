package com.denizcan.astrosea.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.denizcan.astrosea.R

/**
 * Açılım adları Firestore/JSON/navigasyon anahtarı olarak Türkçe sabit kalır;
 * ekranda gösterilen ad ve açıklama ise seçili dile göre çevrilir.
 */
object ReadingTexts {

    @Composable
    fun displayName(readingKey: String): String {
        val resId = nameRes(readingKey) ?: return readingKey
        return stringResource(resId)
    }

    fun displayName(context: Context, readingKey: String): String {
        val resId = nameRes(readingKey) ?: return readingKey
        return context.getString(resId)
    }

    private fun nameRes(readingKey: String): Int? = when (readingKey.trim()) {
        "GÜNLÜK AÇILIM" -> R.string.reading_name_gunluk_acilim
        "TEK KART AÇILIMI" -> R.string.reading_name_tek_kart
        "EVET – HAYIR AÇILIMI" -> R.string.reading_name_evet_hayir
        "GEÇMİŞ, ŞİMDİ, GELECEK" -> R.string.reading_name_gecmis_simdi_gelecek
        "DURUM, AKSİYON, SONUÇ" -> R.string.reading_name_durum_aksiyon_sonuc
        "İLİŞKİ AÇILIMI" -> R.string.reading_name_iliski
        "UYUMLULUK AÇILIMI" -> R.string.reading_name_uyumluluk
        "DETAYLI İLİŞKİ AÇILIMI" -> R.string.reading_name_detayli_iliski
        "MÜCADELELER AÇILIMI" -> R.string.reading_name_mucadeleler
        "TAMAM MI, DEVAM MI" -> R.string.reading_name_tamam_mi_devam_mi
        "GELECEĞİNE GİDEN YOL" -> R.string.reading_name_gelecege_giden_yol
        "İŞ YERİNDEKİ PROBLEMLER" -> R.string.reading_name_is_problemleri
        "FİNANSAL DURUM" -> R.string.reading_name_finansal_durum
        else -> null
    }

    @Composable
    fun description(readingKey: String): String {
        val resId = descRes(readingKey) ?: return ""
        return stringResource(resId)
    }

    private fun descRes(readingKey: String): Int? = when (readingKey.trim()) {
        "GÜNLÜK AÇILIM" -> R.string.reading_desc_gunluk_acilim
        "TEK KART AÇILIMI" -> R.string.reading_desc_tek_kart
        "EVET – HAYIR AÇILIMI" -> R.string.reading_desc_evet_hayir
        "GEÇMİŞ, ŞİMDİ, GELECEK" -> R.string.reading_desc_gecmis_simdi_gelecek
        "DURUM, AKSİYON, SONUÇ" -> R.string.reading_desc_durum_aksiyon_sonuc
        "İLİŞKİ AÇILIMI" -> R.string.reading_desc_iliski
        "UYUMLULUK AÇILIMI" -> R.string.reading_desc_uyumluluk
        "DETAYLI İLİŞKİ AÇILIMI" -> R.string.reading_desc_detayli_iliski
        "MÜCADELELER AÇILIMI" -> R.string.reading_desc_mucadeleler
        "TAMAM MI, DEVAM MI" -> R.string.reading_desc_tamam_mi_devam_mi
        "GELECEĞİNE GİDEN YOL" -> R.string.reading_desc_gelecege_giden_yol
        "İŞ YERİNDEKİ PROBLEMLER" -> R.string.reading_desc_is_problemleri
        "FİNANSAL DURUM" -> R.string.reading_desc_finansal_durum
        else -> null
    }

    @Composable
    fun slotNames(readingKey: String): List<String> {
        return stringArrayResource(slotArrayRes(readingKey)).toList()
    }

    fun slotNames(context: Context, readingKey: String): List<String> {
        return context.resources.getStringArray(slotArrayRes(readingKey)).toList()
    }

    private fun slotArrayRes(readingKey: String): Int = when (readingKey.trim()) {
        "GÜNLÜK AÇILIM" -> R.array.slots_gunluk
        "TEK KART AÇILIMI" -> R.array.slots_tek_kart
        "EVET – HAYIR AÇILIMI" -> R.array.slots_evet_hayir
        "GEÇMİŞ, ŞİMDİ, GELECEK" -> R.array.slots_gecmis_simdi_gelecek
        "DURUM, AKSİYON, SONUÇ" -> R.array.slots_durum_aksiyon_sonuc
        "İLİŞKİ AÇILIMI" -> R.array.slots_iliski
        "UYUMLULUK AÇILIMI" -> R.array.slots_uyumluluk
        "DETAYLI İLİŞKİ AÇILIMI" -> R.array.slots_detayli_iliski
        "MÜCADELELER AÇILIMI" -> R.array.slots_mucadeleler
        "TAMAM MI, DEVAM MI" -> R.array.slots_tamam_mi
        "GELECEĞİNE GİDEN YOL" -> R.array.slots_gelecege_giden_yol
        "İŞ YERİNDEKİ PROBLEMLER" -> R.array.slots_is_problemleri
        "FİNANSAL DURUM" -> R.array.slots_finansal
        else -> R.array.slots_default
    }
}
