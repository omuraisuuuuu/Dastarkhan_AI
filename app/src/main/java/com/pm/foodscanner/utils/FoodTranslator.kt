package com.pm.foodscanner.utils

object FoodTranslator {
    private val foodDictionary = mapOf(
        // English -> Russian -> Kazakh
        "apple" to mapOf("ru" to "яблоко", "kk" to "алма"),
        "banana" to mapOf("ru" to "банан", "kk" to "банан"),
        "orange" to mapOf("ru" to "апельсин", "kk" to "апельсин"),
        "bread" to mapOf("ru" to "хлеб", "kk" to "нан"),
        "rice" to mapOf("ru" to "рис", "kk" to "күрішe"),
        "chicken" to mapOf("ru" to "курица", "kk" to "тауық"),
        "beef" to mapOf("ru" to "говядина", "kk" to "сиыр еті"),
        "fish" to mapOf("ru" to "рыба", "kk" to "балық"),
        "milk" to mapOf("ru" to "молоко", "kk" to "сүт"),
        "cheese" to mapOf("ru" to "сыр", "kk" to "сыр"),
        "egg" to mapOf("ru" to "яйцо", "kk" to "жұмыртқа"),
        "pasta" to mapOf("ru" to "макаронные изделия", "kk" to "макарон"),
        "pizza" to mapOf("ru" to "пицца", "kk" to "пицца"),
        "salad" to mapOf("ru" to "салат", "kk" to "салат"),
        "soup" to mapOf("ru" to "суп", "kk" to "сорпа"),
        "coffee" to mapOf("ru" to "кофе", "kk" to "кофе"),
        "tea" to mapOf("ru" to "чай", "kk" to "шай"),
        "water" to mapOf("ru" to "вода", "kk" to "су"),
        "juice" to mapOf("ru" to "сок", "kk" to "сок"),
        "burger" to mapOf("ru" to "бургер", "kk" to "бургер"),
        "fries" to mapOf("ru" to "картофель фри", "kk" to "қызарған картоп"),
        "chocolate" to mapOf("ru" to "шоколад", "kk" to "шоколад"),
        "vegetable" to mapOf("ru" to "овощ", "kk" to "көкөніс"),
        "fruit" to mapOf("ru" to "фрукт", "kk" to "жеміс"),
        "meat" to mapOf("ru" to "мясо", "kk" to "ет"),
        "cake" to mapOf("ru" to "торт", "kk" to "торт"),
        "sandwich" to mapOf("ru" to "бутерброд", "kk" to "бутерброд"),
        "steak" to mapOf("ru" to "стейк", "kk" to "стейк"),
        "pasta salad" to mapOf("ru" to "макаронный салат", "kk" to "макарон салаты"),
        "grilled chicken" to mapOf("ru" to "курица гриль", "kk" to "ағылған тауық"),
        "baked potato" to mapOf("ru" to "печёная картофелина", "kk" to "піскен картоп"),
    )

    fun translateFoodName(englishName: String, languageCode: String): String {
        if (languageCode == LanguageManager.LANGUAGE_ENGLISH) {
            return englishName
        }

        val lowerName = englishName.lowercase()
        val translations = foodDictionary[lowerName]

        return if (translations != null && translations.containsKey(languageCode)) {
            translations[languageCode] ?: englishName
        } else {
            englishName
        }
    }

    fun getAllTranslations(englishName: String): Map<String, String> {
        val lowerName = englishName.lowercase()
        return mapOf(
            LanguageManager.LANGUAGE_ENGLISH to englishName,
            LanguageManager.LANGUAGE_RUSSIAN to (foodDictionary[lowerName]?.get("ru") ?: englishName),
            LanguageManager.LANGUAGE_KAZAKH to (foodDictionary[lowerName]?.get("kk") ?: englishName)
        )
    }
}
