package com.lortey.cardflare

import android.util.Log
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

// File that manages translator
fun createTranslator(sourceLang: String, targetLang: String): Translator {
    val options = TranslatorOptions.Builder()
        .setSourceLanguage(sourceLang)
        .setTargetLanguage(targetLang)
        .build()

    val translator = Translation.getClient(options)

    // Download model if needed
    translator.downloadModelIfNeeded()
        .addOnSuccessListener { Log.d("MLKit", "Model downloaded successfully!") }
        .addOnFailureListener { e -> Log.e("MLKit", "Model download failed: ${e.message}") }

    return translator
}

// Function to translate text
fun translateText(text: String, translator: Translator, onResult: (String) -> Unit) {
    translator.translate(text)
        .addOnSuccessListener { translatedText -> onResult(translatedText) }
        .addOnFailureListener { e -> onResult("Translation failed: ${e.message}") }
}