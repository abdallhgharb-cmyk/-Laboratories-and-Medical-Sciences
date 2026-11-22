package com.example.data.seed

import com.example.data.model.StudyMaterialEntity

object StudyMaterialSeedData {

    fun getInitialMaterials(): List<StudyMaterialEntity> = listOf(
        StudyMaterialEntity(
            id = 1,
            specialtyId = "MED_LAB",
            title = "Hematology: Differential Diagnosis of Microcytic Hypochromic Anemia",
            category = "Hematology & Blood Banking",
            summary = "Essential laboratory algorithms for distinguishing Iron Deficiency Anemia, Thalassemia Trait, Sideroblastic Anemia, and Anemia of Chronic Disease.",
            clinicalContent = "Microcytic anemias (MCV < 80 fL) are among the most frequently tested Prometric and Board exam topics. Key differentiating parameters include Serum Ferritin (gold standard for iron deficiency), Total Iron Binding Capacity (TIBC), Transferrin Saturation, and RDW. In Beta-Thalassemia minor, Hb A2 is typically elevated > 3.5%, while RDW remains normal in uncomplicated trait.",
            keyPointsJson = "• Iron Deficiency: Low Ferritin, High TIBC, High RDW, Low Serum Iron.\n• Thalassemia Trait: Normal/High Ferritin, Normal TIBC, Normal RDW, Elevated HbA2.\n• Sideroblastic Anemia: Ringed sideroblasts on Prussian blue bone marrow stain.\n• Mentzer Index: MCV / RBC count (< 13 suggests Thalassemia; > 13 suggests Iron Deficiency).",
            highYieldRating = 5,
            isSavedOffline = true
        ),
        StudyMaterialEntity(
            id = 2,
            specialtyId = "MED_LAB",
            title = "Clinical Microbiology: Gram-Negative Non-Fermenters & Oxidase Algorithm",
            category = "Clinical Microbiology",
            summary = "Flowcharts for differentiating Pseudomonas aeruginosa, Acinetobacter baumannii, and Stenotrophomonas maltophilia in nosocomial infections.",
            clinicalContent = "Non-fermenting Gram-negative bacilli fail to ferment glucose on TSI/KIA agars (alkaline/alkaline slant/butt). Oxidase test is the primary branching point: Pseudomonas aeruginosa is Oxidase positive with characteristic pyocyanin (blue-green) pigment and fruity grape-like odor, whereas Acinetobacter baumannii and Stenotrophomonas maltophilia are Oxidase negative.",
            keyPointsJson = "• Pseudomonas aeruginosa: Oxidase (+), Motile, Grows at 42°C, Pyocyanin & Pyoverdine pigments.\n• Acinetobacter baumannii: Oxidase (-), Non-motile, Coccobacilli, Highly multidrug-resistant.\n• Stenotrophomonas maltophilia: Oxidase (-), Motile, Catalase (+), Inherently resistant to carbapenems, drug of choice is TMP-SMX.",
            highYieldRating = 5,
            isSavedOffline = true
        ),
        StudyMaterialEntity(
            id = 3,
            specialtyId = "MED_LAB",
            title = "Clinical Chemistry: Arterial Blood Gas (ABG) & Acid-Base Compensation",
            category = "Clinical Chemistry",
            summary = "Step-by-step interpretation of metabolic and respiratory acid-base disorders with expected physiological compensations.",
            clinicalContent = "ABG evaluation begins with pH (normal 7.35–7.45), followed by PaCO2 (respiratory component, normal 35–45 mmHg) and HCO3- (metabolic component, normal 22–26 mEq/L). Calculate Anion Gap = [Na+] - ([Cl-] + [HCO3-]) (normal 8–12 mEq/L) in metabolic acidosis to categorize into High Anion Gap (MUDPILES / GOLDMARK) vs Normal Anion Gap (hyperchloremic) acidosis.",
            keyPointsJson = "• High Anion Gap Metabolic Acidosis causes: Methanol, Uremia, DKA, Paraldehyde, Isoniazid, Lactic acidosis, Ethylene glycol, Salicylates.\n• Winter's Formula: Expected PaCO2 = (1.5 * [HCO3-]) + 8 ± 2 in metabolic acidosis.\n• Respiratory Acidosis: Hypoventilation leads to acute/chronic renal HCO3- retention.",
            highYieldRating = 5,
            isSavedOffline = true
        ),
        StudyMaterialEntity(
            id = 4,
            specialtyId = "MED_LAB",
            title = "Immunology & Serology: Hepatitis B Serological Markers Matrix",
            category = "Immunology & Serology",
            summary = "Comprehensive guide to interpreting HBsAg, Anti-HBs, Anti-HBc (IgM/IgG), HBeAg, and HBV DNA in acute, chronic, and vaccinated states.",
            clinicalContent = "HBsAg is the first detectable marker indicating active infection. Anti-HBs indicates immunity (either natural or post-vaccination). The 'Window Period' occurs between the disappearance of HBsAg and appearance of Anti-HBs; during this phase, IgM Anti-HBc is the sole diagnostic marker of acute infection. Anti-HBc is negative in vaccinated individuals.",
            keyPointsJson = "• Vaccinated: Anti-HBs (+), HBsAg (-), Anti-HBc (-).\n• Natural Immunity (Recovered): Anti-HBs (+), Anti-HBc IgG (+), HBsAg (-).\n• Acute HBV: HBsAg (+), IgM Anti-HBc (+), HBeAg (+).\n• Chronic HBV: HBsAg (+) > 6 months, IgG Anti-HBc (+), Anti-HBs (-).",
            highYieldRating = 5,
            isSavedOffline = true
        ),
        StudyMaterialEntity(
            id = 5,
            specialtyId = "NURSING",
            title = "Clinical Nursing: ACLS Pharmacology & Crash Cart Protocol",
            category = "Emergency & Critical Care",
            summary = "Emergency medication dosages, indications, and administration routes for cardiac arrest, shockable vs non-shockable rhythms.",
            clinicalContent = "In adult cardiac arrest (VF/pVT), immediate defibrillation is prioritized followed by CPR. Epinephrine 1 mg IV/IO is administered every 3–5 minutes. Amiodarone 300 mg bolus is given after the 3rd shock for refractory VF/pVT (second dose 150 mg). For non-shockable rhythms (Asystole/PEA), high-quality CPR and early epinephrine are paramount while assessing reversible causes (5 H's and 5 T's).",
            keyPointsJson = "• Shockable rhythms: VF and Pulseless VT (Defibrillate ➔ CPR ➔ Shock ➔ Epi ➔ Shock ➔ Amiodarone).\n• Non-shockable rhythms: Asystole and PEA (CPR ➔ Epinephrine ASAP ➔ Reversible causes).\n• 5 H's: Hypovolemia, Hypoxia, Hydrogen ion (acidosis), Hypo/Hyperkalemia, Hypothermia.\n• 5 T's: Tension pneumothorax, Tamponade, Toxins, Thrombosis pulmonary, Thrombosis coronary.",
            highYieldRating = 5,
            isSavedOffline = true
        )
    )
}
