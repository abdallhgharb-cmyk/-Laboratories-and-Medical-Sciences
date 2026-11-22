package com.example.data.model

/**
 * Uniform clinical examination grading and stage-unlock policy across all departments and levels:
 * - Pass & unlock threshold: >= 70%
 * - Stars awarded upon passing/unlocking (>= 70%): 2 stars (scaling to 3 stars at >= 90%)
 */
object StageGradingPolicy {
    const val PASS_THRESHOLD_PERCENT = 70
    const val DISTINCTION_THRESHOLD_PERCENT = 90
    const val PASS_STARS_REWARD = 2
    const val DISTINCTION_STARS_REWARD = 3

    fun calculateAccuracyPercent(correctCount: Int, totalCount: Int): Int {
        if (totalCount <= 0 || correctCount <= 0) return 0
        return ((correctCount * 100.0) / totalCount).toInt().coerceIn(0, 100)
    }

    fun isStagePassed(correctCount: Int, totalCount: Int): Boolean {
        if (totalCount <= 0) return false
        return (correctCount * 100) >= (totalCount * PASS_THRESHOLD_PERCENT)
    }

    fun starsForAccuracy(accuracyPercent: Int): Int = when {
        accuracyPercent >= DISTINCTION_THRESHOLD_PERCENT -> DISTINCTION_STARS_REWARD
        accuracyPercent >= PASS_THRESHOLD_PERCENT -> PASS_STARS_REWARD
        else -> 0
    }

    fun gradeLabelEn(accuracyPercent: Int): String = when {
        accuracyPercent >= DISTINCTION_THRESHOLD_PERCENT -> "Distinction (★★★)"
        accuracyPercent >= PASS_THRESHOLD_PERCENT -> "Passed & Unlocked (★★☆)"
        accuracyPercent >= 50 -> "Needs Review (<70%)"
        else -> "Not Passed"
    }

    fun gradeLabelAr(accuracyPercent: Int): String = when {
        accuracyPercent >= DISTINCTION_THRESHOLD_PERCENT -> "امتياز سريري (3 نجوم)"
        accuracyPercent >= PASS_THRESHOLD_PERCENT -> "ناجح ومؤهل لفتح المرحلة التالية (نجمتان ★ ★)"
        accuracyPercent >= 50 -> "أقل من الحد المعتمد (70%) — يحتاج مراجعة"
        else -> "غير مجتاز"
    }
}
