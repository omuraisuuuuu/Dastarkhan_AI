package com.pm.foodscanner.util

import com.pm.foodscanner.data.model.Product
import com.pm.foodscanner.data.model.UserProfile

data class EvaluationResult(
    val isCompatible: Boolean,
    val reasons: List<String>
)

object PreferenceEvaluator {

    private val PORK_KEYWORDS = listOf("pork", "lard", "bacon", "ham", "gelatin", "swine")
    private val ALCOHOL_KEYWORDS = listOf("alcohol", "wine", "beer", "rum", "vodka", "ethanol")
    private val DAIRY_KEYWORDS = listOf("en:milk", "en:lactose")
    private val NON_VEGAN_TAGS = listOf(
        "en:non-vegan", "en:meat", "en:fish", "en:dairy",
        "en:eggs", "en:honey", "en:gelatin"
    )

    private val INHERENTLY_HALAL_CATEGORIES = listOf(
        "en:waters", "en:spring-waters", "en:mineral-waters", "en:flavored-waters",
        "en:sparkling-waters", "en:natural-mineral-waters",
        "en:fruits", "en:fresh-fruits", "en:dried-fruits", "en:frozen-fruits",
        "en:vegetables", "en:fresh-vegetables", "en:frozen-vegetables", "en:canned-vegetables",
        "en:cereals", "en:grains", "en:rice", "en:pasta", "en:noodles", "en:wheat",
        "en:breads", "en:flatbreads",
        "en:coffees", "en:teas", "en:herbal-teas", "en:green-teas",
        "en:juices", "en:fruit-juices", "en:vegetable-juices",
        "en:sugars", "en:honeys", "en:salts", "en:sea-salts",
        "en:spices", "en:herbs", "en:seasonings",
        "en:plant-based-oils", "en:olive-oils", "en:sunflower-oils", "en:vegetable-oils",
        "en:nuts", "en:seeds", "en:legumes", "en:lentils", "en:beans",
        "en:milks", "en:plant-milks", "en:eggs",
        "en:sodas", "en:soft-drinks", "en:carbonated-drinks",
        "en:chocolates", "en:dark-chocolates",
        "en:chips", "en:crisps", "en:crackers",
        "en:baby-foods", "en:infant-formulas",
        "en:frozen-foods", "en:canned-foods"
    )

    private val INHERENTLY_HALAL_NAME_KEYWORDS = listOf(
        "water", "вода", "су",
        "juice", "сок",
        "tea", "чай", "шай",
        "coffee", "кофе",
        "rice", "рис", "күріш",
        "salt", "соль", "тұз",
        "sugar", "сахар", "қант",
        "honey", "мёд", "мед", "бал",
        "bread", "хлеб", "нан",
        "milk", "молоко", "сүт",
        "butter", "масло", "май",
        "flour", "мука", "ұн",
        "oil", "растительное масло",
        "apple", "яблоко", "алма",
        "banana", "банан",
        "orange", "апельсин",
        "potato", "картофель", "картошка",
        "tomato", "помидор",
        "cucumber", "огурец",
        "carrot", "морковь",
        "onion", "лук",
        "egg", "яйцо", "жұмыртқа",
        "cheese", "сыр", "ірімшік",
        "yogurt", "йогурт"
    )

    fun evaluate(profile: UserProfile, product: Product): EvaluationResult {
        val reasons = mutableListOf<String>()
        val ingredients = product.ingredientsText?.lowercase() ?: ""
        val allergenTags = product.allergensTags.map { it.lowercase() }
        val labelTags = product.labelsTags.map { it.lowercase() }
        val categoryTags = product.categoriesTags.map { it.lowercase() }

        if (profile.isHalal) {
            checkHalal(ingredients, labelTags, categoryTags, product.productName, reasons)
        }

        if (profile.isLactoseFree) {
            checkLactose(allergenTags, ingredients, reasons)
        }

        if (profile.isVegan) {
            checkVegan(allergenTags, labelTags, ingredients, reasons)
        }

        if (profile.allergies.isNotEmpty()) {
            checkAllergies(profile.allergies, allergenTags, ingredients, reasons)
        }

        return EvaluationResult(
            isCompatible = reasons.isEmpty(),
            reasons = reasons
        )
    }

    private fun checkHalal(
        ingredients: String,
        labelTags: List<String>,
        categoryTags: List<String>,
        productName: String?,
        reasons: MutableList<String>
    ) {
        val hasHalalLabel = labelTags.any { it.contains("halal") }
        if (hasHalalLabel) return

        val hasPork = PORK_KEYWORDS.any { ingredients.contains(it) }
        val hasAlcohol = ALCOHOL_KEYWORDS.any { ingredients.contains(it) }

        if (hasPork) reasons.add("Contains pork-derived ingredients (not Halal)")
        if (hasAlcohol) reasons.add("Contains alcohol (not Halal)")

        if (!hasPork && !hasAlcohol) {
            val isNaturallyHalal = isInherentlyHalalProduct(categoryTags, productName)
            if (!isNaturallyHalal) {
                reasons.add("No Halal certification found")
            }
        }
    }

    private fun isInherentlyHalalProduct(
        categoryTags: List<String>,
        productName: String?
    ): Boolean {
        val matchesCategory = categoryTags.any { cat ->
            INHERENTLY_HALAL_CATEGORIES.any { cat.contains(it) }
        }
        if (matchesCategory) return true

        val nameLower = productName?.lowercase() ?: return false
        return INHERENTLY_HALAL_NAME_KEYWORDS.any { keyword ->
            nameLower.contains(keyword)
        }
    }

    private fun checkLactose(
        allergenTags: List<String>,
        ingredients: String,
        reasons: MutableList<String>
    ) {
        val hasLactose = DAIRY_KEYWORDS.any { tag ->
            allergenTags.any { it.contains(tag) }
        } || ingredients.contains("milk") || ingredients.contains("lactose")
            || ingredients.contains("cream") || ingredients.contains("cheese")
            || ingredients.contains("butter") || ingredients.contains("whey")

        if (hasLactose) reasons.add("Contains dairy/lactose")
    }

    private fun checkVegan(
        allergenTags: List<String>,
        labelTags: List<String>,
        ingredients: String,
        reasons: MutableList<String>
    ) {
        val isNonVegan = NON_VEGAN_TAGS.any { tag ->
            allergenTags.any { it.contains(tag) } || labelTags.any { it.contains(tag) }
        }

        val hasAnimalIngredients = listOf(
            "meat", "chicken", "beef", "fish", "egg", "milk",
            "honey", "gelatin", "butter", "cream", "cheese", "whey"
        ).any { ingredients.contains(it) }

        if (isNonVegan || hasAnimalIngredients) {
            reasons.add("Contains animal-derived ingredients (not Vegan)")
        }
    }

    private fun checkAllergies(
        userAllergies: List<String>,
        allergenTags: List<String>,
        ingredients: String,
        reasons: MutableList<String>
    ) {
        for (allergy in userAllergies) {
            val allergyLower = allergy.trim().lowercase()
            if (allergyLower.isEmpty()) continue

            val found = allergenTags.any { it.contains(allergyLower) }
                || ingredients.contains(allergyLower)

            if (found) {
                reasons.add("Contains allergen: $allergy")
            }
        }
    }
}
