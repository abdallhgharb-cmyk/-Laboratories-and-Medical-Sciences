package com.example.ui.viewmodel

import com.example.data.model.AcademicDegree
import com.example.data.model.QuestionCategory

object DashboardStrings {
    fun screenTitle(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Performance Dashboard" else "لوحة أداء الاختبارات والكفاءة"

    fun toggleLabel(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "عربي" else "EN"

    fun readinessTitle(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Board Exam Readiness" else "معدل الجاهزية لامتحانات البورد"

    fun overallAccuracy(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Overall Accuracy" else "الدقة التراكمية"

    fun totalScoreLabel(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Total Score" else "النقاط الكلية"

    fun answeredLabel(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Answered" else "أسئلة محلولة"

    fun correctLabel(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Correct" else "إجابات صحيحة"

    fun topicsMasteredLabel(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Mastered Topics" else "مواضيع متقنة"

    fun accuracyChartTitle(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Accuracy by Clinical Topic" else "معدل الدقة حسب التخصص الفرعي"

    fun accuracyChartSubtitle(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Dashed line marks 80% Board Mastery threshold" else "الخط المتقطع يمثل حد الإتقان المعتمد (80%)"

    fun noChartDataTip(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) {
            "Answer clinical questions offline or online to visualize topic accuracy bars here."
        } else {
            "أجب على الأسئلة السريرية دون اتصال أو مع المزامنة لعرض مخططات الدقة هنا."
        }

    fun quickToolsTitle(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Quick Clinical Tools" else "الأدوات السريرية السريعة"

    fun licenseLabel(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "License" else "الترخيص"

    fun dailyLabel(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Daily Test" else "التحدي اليومي"

    fun mistakesLabel(lang: DashboardLanguage, count: Int) =
        if (lang == DashboardLanguage.EN) "Mistakes ($count)" else "الأخطاء ($count)"

    fun favoritesLabel(lang: DashboardLanguage, count: Int) =
        if (lang == DashboardLanguage.EN) "Saved ($count)" else "المفضلة ($count)"

    fun topicsMasteryTitle(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Topics Mastered & Review" else "المواضيع المتقنة والمراجعة"

    fun filterAll(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "All" else "الكل"

    fun filterMastered(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Mastered (>=80%)" else "متقن (>=80%)"

    fun filterNeedsReview(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Needs Review" else "تحتاج مراجعة"

    fun practiceTopicBtn(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Practice" else "تدرب الآن"

    fun degreeBreakdownTitle(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Progress by Academic Degree" else "التقدم حسب الدرجة الأكاديمية"

    fun notesSectionTitle(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Student Study Notes (Offline + Sync)" else "دفتر ملاحظات الطالب (أوفلاين + سحابي)"

    fun addNoteLabel(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "New Note" else "ملاحظة جديدة"

    fun filterQuestionNotes(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Question Notes" else "مرتبطة بسؤال"

    fun filterStandaloneNotes(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Notebook" else "دفتر حر"

    fun searchNotesHint(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Search notes by title, topic, or text..." else "ابحث في الملاحظات بالعنوان أو الموضوع..."

    fun emptyNotesTip(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) {
            "No study notes yet. Tap \"New Note\" for your personal notebook or tap the Note icon on any exam question."
        } else {
            "لا توجد ملاحظات بعد. اضغط على \"ملاحظة جديدة\" أو أيقونة الملاحظة داخل أي سؤال."
        }

    fun googleSyncCardTitle(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Cloud Progress & Notes Backup" else "حساب الطالب والمزامنة السحابية"

    fun googleGuestSubtitle(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) {
            "Offline Room practice is always active. Sign in with Google optionally to back up scores and notes."
        } else {
            "التدريب الأوفلاين عبر Room يعمل دائماً. سجّل بحساب جوجل اختيارياً لمزامنة نتائجك وملاحظاتك."
        }

    fun signInWithGoogleBtn(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Sign in with Google" else "تسجيل الدخول عبر Google"

    fun syncNowBtn(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Sync Now" else "مزامنة الآن"

    fun signOutBtn(lang: DashboardLanguage) =
        if (lang == DashboardLanguage.EN) "Sign Out" else "تسجيل الخروج"

    fun statusLabel(status: TopicMasteryStatus, lang: DashboardLanguage): String = when (status) {
        TopicMasteryStatus.MASTERED -> if (lang == DashboardLanguage.EN) "Mastered" else "متقن"
        TopicMasteryStatus.IN_PROGRESS -> if (lang == DashboardLanguage.EN) "In Progress" else "قيد التقدم"
        TopicMasteryStatus.NEEDS_REVIEW -> if (lang == DashboardLanguage.EN) "Needs Review" else "يحتاج مراجعة"
        TopicMasteryStatus.UNSTARTED -> if (lang == DashboardLanguage.EN) "Not Started" else "لم يبدأ"
    }

    fun specialtyName(specialtyId: String, lang: DashboardLanguage): String {
        return when (specialtyId) {
            QuestionCategory.MEDICAL_LABS ->
                if (lang == DashboardLanguage.EN) "Medical Laboratory Sciences" else "المختبرات والعلوم الطبية"
            QuestionCategory.MED_LAB_LICENSING ->
                if (lang == DashboardLanguage.EN) "Medical Lab Licensing (530 Qs)" else "مزاولة المهنة - المختبرات (530 سؤال)"
            QuestionCategory.NURSING ->
                if (lang == DashboardLanguage.EN) "Nursing & Critical Care" else "علوم التمريض والرعاية الحرجة"
            QuestionCategory.PHYSICIAN_ASSISTANT ->
                if (lang == DashboardLanguage.EN) "Physician Assistant" else "مساعد الطبيب والطب العام"
            else ->
                if (lang == DashboardLanguage.EN) "General Study" else "ملاحظات عامة"
        }
    }

    fun degreeLabel(degree: String, lang: DashboardLanguage): String {
        return when (degree) {
            AcademicDegree.DIPLOMA -> if (lang == DashboardLanguage.EN) "Diploma" else "دبلوم / فني"
            AcademicDegree.BACHELOR -> if (lang == DashboardLanguage.EN) "Bachelor" else "بكالوريوس"
            AcademicDegree.MASTER -> if (lang == DashboardLanguage.EN) "Master" else "ماجستير"
            AcademicDegree.CONSULTANT -> if (lang == DashboardLanguage.EN) "Consultant" else "استشاري"
            else -> degree
        }
    }
}
