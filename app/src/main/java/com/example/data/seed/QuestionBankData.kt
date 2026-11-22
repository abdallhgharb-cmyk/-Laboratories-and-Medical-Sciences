package com.example.data.seed

import com.example.data.model.QuestionEntity

object QuestionBankData {

    fun getAllInitialQuestions(): List<QuestionEntity> {
        val list = mutableListOf<QuestionEntity>()

        // ==========================================
        // 1. MEDICAL LABORATORIES (MED_LAB) - 30 Stages
        // ==========================================
        
        // Stage 1: Hematology Basics & RBC Morphology
        list.add(
            QuestionEntity(
                specialtyId = "MED_LAB",
                stageLevel = 1,
                subCategory = "Hematology",
                questionEn = "Which hemoglobin variant is responsible for the sickle-shaped red blood cells seen on the peripheral blood smear?",
                optionA = "Hemoglobin A1c",
                optionB = "Hemoglobin S",
                optionC = "Hemoglobin F",
                optionD = "Hemoglobin C",
                correctOptionIndex = 1,
                clinicalExplanationEn = "Hemoglobin S (HbS) results from a point mutation substituting valine for glutamic acid at position 6 of the beta-globin chain. Deoxygenation causes polymerization into insoluble fibers, distorting RBCs into sickled shapes (drepanocytes).",
                clinicalHintEn = "Look for the hemoglobin variant caused by a single amino acid substitution of valine for glutamic acid on the beta chain.",
                imageResName = "img_hematology_smear"
            )
        )
        list.add(
            QuestionEntity(
                specialtyId = "MED_LAB",
                stageLevel = 1,
                subCategory = "Hematology",
                questionEn = "What is the normal reference range for adult male hemoglobin in standard clinical hematology?",
                optionA = "8.0 - 11.5 g/dL",
                optionB = "13.5 - 17.5 g/dL",
                optionC = "18.5 - 22.0 g/dL",
                optionD = "10.0 - 13.0 g/dL",
                correctOptionIndex = 1,
                clinicalExplanationEn = "In healthy adult males, normal hemoglobin ranges between 13.5 and 17.5 g/dL (135 - 175 g/L). Values below 13.5 g/dL indicate anemia according to WHO criteria.",
                clinicalHintEn = "Consider typical male reference values, which are slightly higher than female values due to androgenic stimulation of erythropoiesis.",
                imageResName = null
            )
        )
        list.add(
            QuestionEntity(
                specialtyId = "MED_LAB",
                stageLevel = 1,
                subCategory = "Hematology",
                questionEn = "Which red blood cell index represents the average volume of a single erythrocyte?",
                optionA = "MCH (Mean Corpuscular Hemoglobin)",
                optionB = "MCHC (Mean Corpuscular Hemoglobin Concentration)",
                optionC = "MCV (Mean Corpuscular Volume)",
                optionD = "RDW (Red Cell Distribution Width)",
                correctOptionIndex = 2,
                clinicalExplanationEn = "Mean Corpuscular Volume (MCV) quantifies average RBC size in femtoliters (fL). Normal adult range is 80 - 100 fL, classifying anemias as microcytic, normocytic, or macrocytic.",
                clinicalHintEn = "Focus on the index measured in femtoliters (fL) calculating (Hematocrit x 10) / RBC count.",
                imageResName = null
            )
        )
        list.add(
            QuestionEntity(
                specialtyId = "MED_LAB",
                stageLevel = 1,
                subCategory = "Hematology",
                questionEn = "What stain is considered the gold standard for routine evaluation of peripheral blood smears?",
                optionA = "Gram stain",
                optionB = "Wright-Giemsa stain",
                optionC = "Periodic acid–Schiff (PAS)",
                optionD = "Ziehl-Neelsen stain",
                correctOptionIndex = 1,
                clinicalExplanationEn = "Wright-Giemsa is a polychromatic Romanowsky-type stain containing methylene blue and eosin, providing optimal differentiation of nucleus, cytoplasm, and granules in blood cells.",
                clinicalHintEn = "This Romanowsky stain uses eosin Y and methylene blue to impart characteristic coloration to leukocyte granules and erythrocytes.",
                imageResName = null
            )
        )

        // Stage 2: Clinical Urinalysis & Sediment
        list.add(
            QuestionEntity(
                specialtyId = "MED_LAB",
                stageLevel = 2,
                subCategory = "Urinalysis",
                questionEn = "In microscopic urinalysis, envelope-shaped crystals viewed under polarizing light in acidic urine are characteristic of:",
                optionA = "Triple phosphate",
                optionB = "Calcium oxalate dihydrate",
                optionC = "Ammonium biurate",
                optionD = "Cystine crystals",
                correctOptionIndex = 1,
                clinicalExplanationEn = "Calcium oxalate dihydrate crystals typically display an envelope or octahedral shape. They precipitate in acidic to neutral urine and may increase in ethylene glycol toxicity or hyperoxaluria.",
                clinicalHintEn = "Identify the classic bipyramidal 'envelope' crystal frequently associated with dietary oxalate or renal lithiasis.",
                imageResName = "img_urinalysis_crystals"
            )
        )
        list.add(
            QuestionEntity(
                specialtyId = "MED_LAB",
                stageLevel = 2,
                subCategory = "Urinalysis",
                questionEn = "Which type of urinary cast is pathognomonic for acute glomerulonephritis?",
                optionA = "Hyaline casts",
                optionB = "Red Blood Cell (RBC) casts",
                optionC = "Waxy casts",
                optionD = "Broad casts",
                correctOptionIndex = 1,
                clinicalExplanationEn = "Red blood cell casts indicate glomerular hematuria and basement membrane damage, characteristic of acute glomerulonephritis or vasculitis.",
                clinicalHintEn = "Look for the cast type that confirms bleeding originating specifically from within the renal glomeruli.",
                imageResName = null
            )
        )
        list.add(
            QuestionEntity(
                specialtyId = "MED_LAB",
                stageLevel = 2,
                subCategory = "Urinalysis",
                questionEn = "What Tamm-Horsfall mucoprotein constitutes the organic matrix of all true urinary casts?",
                optionA = "Albumin",
                optionB = "Uromodulin",
                optionC = "Bence-Jones protein",
                optionD = "Beta-2 microglobulin",
                correctOptionIndex = 1,
                clinicalExplanationEn = "Uromodulin (formerly known as Tamm-Horsfall protein) is a glycoprotein secreted exclusively by the thick ascending limb of the loop of Henle and forms the structural matrix of urinary casts.",
                clinicalHintEn = "This protein was formerly discovered by Tamm and Horsfall and is secreted in the loop of Henle.",
                imageResName = null
            )
        )

        // Stage 3: Clinical Microbiology & Gram Staining
        list.add(
            QuestionEntity(
                specialtyId = "MED_LAB",
                stageLevel = 3,
                subCategory = "Microbiology",
                questionEn = "Gram-positive cocci arranged in clusters resembling bunches of grapes that test positive for catalase and coagulase are identified as:",
                optionA = "Streptococcus pyogenes",
                optionB = "Staphylococcus aureus",
                optionC = "Enterococcus faecalis",
                optionD = "Staphylococcus epidermidis",
                correctOptionIndex = 1,
                clinicalExplanationEn = "Staphylococcus aureus is a Gram-positive coccus that clusters like grapes and produces both catalase and coagulase, distinguishing it from coagulase-negative staphylococci.",
                clinicalHintEn = "Consider the coagulase-positive cluster-forming staphylococcus responsible for golden colonies on sheep blood agar.",
                imageResName = "img_microbiology_gram"
            )
        )
        list.add(
            QuestionEntity(
                specialtyId = "MED_LAB",
                stageLevel = 3,
                subCategory = "Microbiology",
                questionEn = "What is the primary differentiating component in MacConkey agar that inhibits Gram-positive bacterial growth?",
                optionA = "Crystal violet and bile salts",
                optionB = "Sodium chloride 7.5%",
                optionC = "Eosin and methylene blue",
                optionD = "Colistin and nalidixic acid",
                correctOptionIndex = 0,
                clinicalExplanationEn = "MacConkey agar uses bile salts and crystal violet dye to selectively inhibit the growth of Gram-positive organisms while allowing Gram-negative bacilli to proliferate.",
                clinicalHintEn = "These two selective agents suppress Gram-positive cell walls while allowing enteric Gram-negative rods to grow.",
                imageResName = null
            )
        )

        // Generate comprehensive questions across all 30 stages for MED_LAB
        val medLabCategories = listOf(
            "Clinical Chemistry", "Hematology", "Microbiology", "Immunohematology",
            "Parasitology", "Urinalysis", "Molecular Diagnostics", "Hemostasis & Coagulation",
            "Endocrinology", "Immunology & Serology"
        )

        for (stage in 4..30) {
            val cat = medLabCategories[(stage - 4) % medLabCategories.size]
            when (stage % 5) {
                0 -> {
                    list.add(
                        QuestionEntity(
                            specialtyId = "MED_LAB",
                            stageLevel = stage,
                            subCategory = cat,
                            questionEn = "In a patient presenting with acute chest pain, which cardiac biomarker demonstrates the earliest elevation and highest diagnostic specificity for myocardial infarction?",
                            optionA = "Total Creatine Kinase (CK-total)",
                            optionB = "High-sensitivity Cardiac Troponin (hs-cTnI / hs-cTnT)",
                            optionC = "Lactate Dehydrogenase (LDH-1)",
                            optionD = "Myoglobin alone",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "High-sensitivity cardiac troponins (cTnI and cTnT) are the preferred standard biomarkers for acute coronary syndrome due to their superior myocardial tissue specificity and sensitivity within 1-3 hours.",
                            clinicalHintEn = "This regulatory protein complex of the cardiac myofibril has superseded older enzyme markers in universal MI guidelines.",
                            imageResName = null
                        )
                    )
                    list.add(
                        QuestionEntity(
                            specialtyId = "MED_LAB",
                            stageLevel = stage,
                            subCategory = cat,
                            questionEn = "A laboratory specimen shows elevated serum potassium (6.8 mEq/L) with normal ECG and visible pink discoloration of plasma. What is the most appropriate next step?",
                            optionA = "Report result immediately as hyperkalemic crisis",
                            optionB = "Check the specimen hemolysis index and request an unhemolyzed redraw",
                            optionC = "Administer IV calcium gluconate without retesting",
                            optionD = "Recalibrate the electrolyte ISE analyzer and re-run identical hemolyzed tube",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "In vitro hemolysis causes rupture of erythrocytes, which contain potassium concentrations 20-fold higher than extracellular fluid, creating pseudohyperkalemia.",
                            clinicalHintEn = "Recognize spurious elevation caused by mechanical red cell breakdown releasing intracellular cations.",
                            imageResName = null
                        )
                    )
                }
                1 -> {
                    list.add(
                        QuestionEntity(
                            specialtyId = "MED_LAB",
                            stageLevel = stage,
                            subCategory = cat,
                            questionEn = "Which coagulation test specifically evaluates the extrinsic and common pathways of the secondary hemostasis cascade?",
                            optionA = "Activated Partial Thromboplastin Time (aPTT)",
                            optionB = "Prothrombin Time (PT / INR)",
                            optionC = "Thrombin Time (TT)",
                            optionD = "Bleeding Time",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "Prothrombin Time (PT/INR) measures the extrinsic factor VII and common factors X, V, II (prothrombin), and fibrinogen using tissue thromboplastin and calcium.",
                            clinicalHintEn = "This test is standardly calibrated against international sensitivity index (ISI) to monitor oral warfarin therapy.",
                            imageResName = null
                        )
                    )
                    list.add(
                        QuestionEntity(
                            specialtyId = "MED_LAB",
                            stageLevel = stage,
                            subCategory = cat,
                            questionEn = "What is the recommended anticoagulant and proper blood-to-anticoagulant ratio for routine plasma coagulation testing?",
                            optionA = "EDTA, 1:4 ratio",
                            optionB = "3.2% Buffered Sodium Citrate, 9:1 blood-to-anticoagulant ratio",
                            optionC = "Lithium Heparin, 9:1 ratio",
                            optionD = "Sodium Fluoride, 1:9 ratio",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "Light blue top tubes contain 3.2% (0.109 M) buffered sodium citrate with a strict 9:1 ratio of blood to anticoagulant to ensure accurate calcium chelation for coagulation assays.",
                            clinicalHintEn = "Light blue vacutainer tube filled strictly to the indicator mark to maintain 9 parts blood to 1 part liquid citrate.",
                            imageResName = null
                        )
                    )
                }
                2 -> {
                    list.add(
                        QuestionEntity(
                            specialtyId = "MED_LAB",
                            stageLevel = stage,
                            subCategory = cat,
                            questionEn = "In immunohematology, what is the consequence of administering Group A packed red blood cells to a Group O recipient?",
                            optionA = "Mild delayed allergic urticaria",
                            optionB = "Acute intravascular hemolytic transfusion reaction mediated by anti-A antibodies",
                            optionC = "Transfusion-related acute lung injury (TRALI)",
                            optionD = "No significant reaction if Rh factor is compatible",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "Group O individuals naturally harbor naturally occurring IgM and IgG anti-A and anti-B isohemagglutinins. Infusing Group A RBCs triggers massive complement activation and acute intravascular hemolysis.",
                            clinicalHintEn = "Recipient serum contains preformed potent anti-A isoantibodies that bind donor red cells and activate complement.",
                            imageResName = null
                        )
                    )
                    list.add(
                        QuestionEntity(
                            specialtyId = "MED_LAB",
                            stageLevel = stage,
                            subCategory = cat,
                            questionEn = "Which test distinguishes in vivo sensitization of erythrocytes (autoimmune hemolytic anemia) from in vitro serum antibody sensitization?",
                            optionA = "Indirect Antiglobulin Test (IAT)",
                            optionB = "Direct Antiglobulin Test (DAT / Coombs test)",
                            optionC = "Major Crossmatch",
                            optionD = "Serum protein electrophoresis",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "The Direct Antiglobulin Test (DAT) detects antibodies or complement fragments (C3d) that have already bound in vivo to circulating red blood cells.",
                            clinicalHintEn = "This antiglobulin assay does not require prior incubation with donor red cells because sensitization occurred in the patient's own circulation.",
                            imageResName = null
                        )
                    )
                }
                3 -> {
                    list.add(
                        QuestionEntity(
                            specialtyId = "MED_LAB",
                            stageLevel = stage,
                            subCategory = cat,
                            questionEn = "Stool examination reveals quadrinucleate spherical cysts measuring 12-15 µm with central endosome and chromatoid bodies with rounded ends. The pathogen is:",
                            optionA = "Giardia lamblia",
                            optionB = "Entamoeba histolytica",
                            optionC = "Cryptosporidium parvum",
                            optionD = "Balantidium coli",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "Mature cysts of Entamoeba histolytica contain up to four nuclei with distinct central karyosomes and smooth, blunt-ended ('cigar-shaped') chromatoidal bars.",
                            clinicalHintEn = "Identify the invasive ameba causing amebic dysentery whose cysts display up to 4 nuclei and cigar-shaped chromatoid bars.",
                            imageResName = null
                        )
                    )
                    list.add(
                        QuestionEntity(
                            specialtyId = "MED_LAB",
                            stageLevel = stage,
                            subCategory = cat,
                            questionEn = "In quality control analysis, which Westgard rule violation represents a rejection rule indicating systematic error when four consecutive QC results exceed 1 SD in the same direction?",
                            optionA = "1_2s rule",
                            optionB = "4_1s rule",
                            optionC = "R_4s rule",
                            optionD = "1_3s rule",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "The 4_1s Westgard multi-rule detects systematic bias or shift when four consecutive control measurements exceed +1 SD or -1 SD from the established mean.",
                            clinicalHintEn = "Look for the multi-rule designating 4 serial values falling on the same side of the 1 standard deviation boundary.",
                            imageResName = null
                        )
                    )
                }
                else -> {
                    list.add(
                        QuestionEntity(
                            specialtyId = "MED_LAB",
                            stageLevel = stage,
                            subCategory = cat,
                            questionEn = "In arterial blood gas (ABG) analysis, a pH of 7.28 with PaCO2 of 56 mmHg and HCO3- of 26 mEq/L indicates which primary acid-base disturbance?",
                            optionA = "Metabolic acidosis",
                            optionB = "Uncompensated respiratory acidosis",
                            optionC = "Compensated respiratory alkalosis",
                            optionD = "Metabolic alkalosis",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "A low pH (< 7.35) confirms acidemia. Elevated PaCO2 (> 45 mmHg) with normal bicarbonate (22-26 mEq/L) defines uncompensated respiratory acidosis due to acute hypoventilation.",
                            clinicalHintEn = "The acidemia is driven by hypercapnia (carbon dioxide retention) while renal bicarbonate compensation has not yet occurred.",
                            imageResName = null
                        )
                    )
                    list.add(
                        QuestionEntity(
                            specialtyId = "MED_LAB",
                            stageLevel = stage,
                            subCategory = cat,
                            questionEn = "Which diagnostic PCR parameter determines the thermal denaturation temperature of double-stranded DNA template into single strands?",
                            optionA = "50°C - 58°C",
                            optionB = "94°C - 96°C",
                            optionC = "72°C",
                            optionD = "37°C",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "Denaturation in standard PCR thermal cycling occurs between 94°C and 96°C, breaking the hydrogen bonds between complementary base pairs to yield single strands.",
                            clinicalHintEn = "The highest temperature phase of the three-step polymerase chain reaction cycle.",
                            imageResName = null
                        )
                    )
                }
            }
        }

        // ==========================================
        // 2. NURSING SCIENCES (NURSING) - 30 Stages
        // ==========================================
        for (stage in 1..30) {
            val stageCategory = when (stage % 6) {
                1 -> "Critical Care & ICU"
                2 -> "Pharmacology & Medication Safety"
                3 -> "Emergency Triage"
                4 -> "Medical-Surgical Nursing"
                5 -> "Pediatrics & Neonatal Care"
                else -> "Infection Control & Patient Safety"
            }

            when (stage % 4) {
                0 -> {
                    list.add(
                        QuestionEntity(
                            specialtyId = "NURSING",
                            stageLevel = stage,
                            subCategory = stageCategory,
                            questionEn = "A post-operative patient receiving IV morphine has a respiratory rate of 7 breaths per minute and is difficult to arouse. What is the immediate priority nursing action?",
                            optionA = "Elevate the head of bed and recheck vitals in 30 minutes",
                            optionB = "Administer IV Naloxone (Narcan) as ordered and maintain airway support",
                            optionC = "Infuse 500 mL normal saline bolus",
                            optionD = "Apply warm blanket and continue monitoring",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "Severe respiratory depression (RR < 8-10/min) is a life-threatening opioid overdose complication requiring immediate opioid receptor reversal with Naloxone and respiratory support.",
                            clinicalHintEn = "Recall the specific competitive opioid antagonist used emergently for opioid-induced central hypoventilation.",
                            imageResName = null
                        )
                    )
                    list.add(
                        QuestionEntity(
                            specialtyId = "NURSING",
                            stageLevel = stage,
                            subCategory = stageCategory,
                            questionEn = "According to the Five Rights of Medication Administration, which nursing check is vital prior to administering high-alert medications?",
                            optionA = "Patient, Medication, Dose, Route, and Time verification with two identifiers",
                            optionB = "Administering within 2 hours of physician signoff without identity verification",
                            optionC = "Only verifying the patient's room number",
                            optionD = "Checking drug name on syringe after injection",
                            correctOptionIndex = 0,
                            clinicalExplanationEn = "The core rights ensure patient safety: right patient, right medication, right dose, right route, and right time, confirmed with at least two unique patient identifiers.",
                            clinicalHintEn = "The fundamental five verification checkpoints taught in standard nursing fundamentals.",
                            imageResName = null
                        )
                    )
                }
                1 -> {
                    list.add(
                        QuestionEntity(
                            specialtyId = "NURSING",
                            stageLevel = stage,
                            subCategory = stageCategory,
                            questionEn = "In cardiac emergency nursing, what is the initial management for an adult patient confirmed to be in pulseless Ventricular Fibrillation (VF)?",
                            optionA = "Immediate unsynchronized defibrillation followed by CPR",
                            optionB = "Synchronized cardioversion at 50 Joules",
                            optionC = "Administration of IV atropine 1 mg",
                            optionD = "Immediate endotracheal intubation before any chest compressions",
                            correctOptionIndex = 0,
                            clinicalExplanationEn = "Pulseless ventricular fibrillation is a shockable cardiac arrest rhythm. Immediate unsynchronized defibrillation with high-quality uninterrupted CPR delivers the highest survival rate.",
                            clinicalHintEn = "Identify the shockable arrest protocol where electrical defibrillation takes precedence.",
                            imageResName = null
                        )
                    )
                    list.add(
                        QuestionEntity(
                            specialtyId = "NURSING",
                            stageLevel = stage,
                            subCategory = stageCategory,
                            questionEn = "Which electrolyte imbalance is the primary risk factor for digoxin toxicity in heart failure patients receiving loop diuretics?",
                            optionA = "Hypercalcemia",
                            optionB = "Hypokalemia",
                            optionC = "Hypernatremia",
                            optionD = "Hypophosphatemia",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "Low serum potassium levels enhance digoxin binding to the myocardial Na+/K+-ATPase pump, dramatically increasing myocardial sensitivity and risk of lethal digitalis toxicity.",
                            clinicalHintEn = "Furosemide-induced wasting of this intracellular cation potentiates digitalis arrhythmias.",
                            imageResName = null
                        )
                    )
                }
                2 -> {
                    list.add(
                        QuestionEntity(
                            specialtyId = "NURSING",
                            stageLevel = stage,
                            subCategory = stageCategory,
                            questionEn = "Under the Emergency Severity Index (ESI) triage algorithm, how should a patient presenting with acute severe stridor and impending airway compromise be classified?",
                            optionA = "ESI Level 1 (Immediate life-saving resuscitation required)",
                            optionB = "ESI Level 3 (Urgent, requires two or more resources)",
                            optionC = "ESI Level 4 (Less urgent)",
                            optionD = "ESI Level 5 (Non-urgent)",
                            correctOptionIndex = 0,
                            clinicalExplanationEn = "ESI Level 1 is reserved for patients requiring immediate life-saving intervention (compromised airway, severe respiratory arrest, or hemodynamic collapse).",
                            clinicalHintEn = "Highest acuity classification where immediate resuscitation is mandatory to prevent imminent death.",
                            imageResName = null
                        )
                    )
                    list.add(
                        QuestionEntity(
                            specialtyId = "NURSING",
                            stageLevel = stage,
                            subCategory = stageCategory,
                            questionEn = "When assessing a newborn at 1 minute of life: Heart rate 110 bpm, vigorous cry, active motion, grimace with suctioning, body pink with blue extremities. What is the APGAR score?",
                            optionA = "6",
                            optionB = "9",
                            optionC = "7",
                            optionD = "10",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "HR > 100 (2 pts), Respiratory effort vigorous cry (2 pts), Muscle tone active (2 pts), Reflex irritability grimace (1-2 pts), Color acrocyanosis body pink extremities blue (1 pt). Total APGAR = 9.",
                            clinicalHintEn = "Calculate 2 points each for pulse, respiration, and muscle tone, with 1 point deduction for acrocyanotic extremities.",
                            imageResName = null
                        )
                    )
                }
                else -> {
                    list.add(
                        QuestionEntity(
                            specialtyId = "NURSING",
                            stageLevel = stage,
                            subCategory = stageCategory,
                            questionEn = "For an immunocompromised patient colonized with Clostridioides difficile, which hand hygiene method is mandatory for healthcare personnel?",
                            optionA = "Standard alcohol-based hand rub for 20 seconds",
                            optionB = "Hand washing with soap and running water to mechanically remove spores",
                            optionC = "Wearing double gloves without hand hygiene between patient contacts",
                            optionD = "Chlorhexidine hand foam without water",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "Alcohol-based sanitizers do not kill bacterial endospores of C. difficile. Friction washing with soap and running water is mandatory to physically dislodge and rinse away spores.",
                            clinicalHintEn = "Bacterial spores are resistant to alcohol denaturing and require mechanical removal with soap and water.",
                            imageResName = null
                        )
                    )
                    list.add(
                        QuestionEntity(
                            specialtyId = "NURSING",
                            stageLevel = stage,
                            subCategory = stageCategory,
                            questionEn = "Which clinical sign indicates development of hypocalcemia following thyroidectomy surgery?",
                            optionA = "Chvostek's sign (facial muscle twitching on facial nerve percussion)",
                            optionB = "Cullen's sign (periumbilical ecchymosis)",
                            optionC = "Kernig's sign",
                            optionD = "Murphy's sign",
                            correctOptionIndex = 0,
                            clinicalExplanationEn = "Accidental damage or excision of parathyroid glands causes acute hypocalcemia, eliciting neuromuscular irritability manifested by Chvostek's sign and Trousseau's sign.",
                            clinicalHintEn = "Tapping over the facial nerve produces unilateral spasm of facial musculature due to latent tetany.",
                            imageResName = null
                        )
                    )
                }
            }
        }

        // ==========================================
        // 3. PHYSICIAN ASSISTANT (PHYSICIAN_ASSISTANT) - 30 Stages
        // ==========================================
        for (stage in 1..30) {
            val paCategory = when (stage % 6) {
                1 -> "Cardiovascular Emergencies"
                2 -> "Endocrinology & Metabolic Crises"
                3 -> "Neurology & Stroke Protocols"
                4 -> "Infectious Disease & Pharmacology"
                5 -> "Pulmonary & Critical Care"
                else -> "Gastrointestinal & Acute Abdomen"
            }

            when (stage % 4) {
                0 -> {
                    list.add(
                        QuestionEntity(
                            specialtyId = "PHYSICIAN_ASSISTANT",
                            stageLevel = stage,
                            subCategory = paCategory,
                            questionEn = "A 58-year-old male presents with sudden crushing retrosternal chest pain radiating to left jaw. ECG reveals ST elevation in leads II, III, and aVF. Which coronary vessel is most likely occluded?",
                            optionA = "Left Anterior Descending artery (LAD)",
                            optionB = "Right Coronary Artery (RCA)",
                            optionC = "Left Circumflex artery (LCx)",
                            optionD = "Left Main coronary artery",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "Leads II, III, and aVF reflect the inferior wall of the left ventricle, supplied by the Right Coronary Artery (RCA) in approximately 85-90% of individuals (right-dominant circulation).",
                            clinicalHintEn = "Inferior STEMI involves leads looking at the diaphragmatic surface supplied by the right coronary system.",
                            imageResName = null
                        )
                    )
                    list.add(
                        QuestionEntity(
                            specialtyId = "PHYSICIAN_ASSISTANT",
                            stageLevel = stage,
                            subCategory = paCategory,
                            questionEn = "In a patient presenting with suspected acute ischemic stroke within 3 hours of symptom onset, what diagnostic imaging study must be obtained first before initiating thrombolytic therapy?",
                            optionA = "Carotid duplex ultrasound",
                            optionB = "Non-contrast Brain Computed Tomography (CT)",
                            optionC = "Echocardiogram with bubble study",
                            optionD = "Lumbar puncture for xanthochromia",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "Non-contrast head CT is essential first-line imaging to rule out intracranial hemorrhage before intravenous tissue plasminogen activator (tPA/Alteplase) can be administered.",
                            clinicalHintEn = "Immediate non-contrast scan used to differentiate ischemic penumbra from hemorrhagic stroke.",
                            imageResName = null
                        )
                    )
                }
                1 -> {
                    list.add(
                        QuestionEntity(
                            specialtyId = "PHYSICIAN_ASSISTANT",
                            stageLevel = stage,
                            subCategory = paCategory,
                            questionEn = "A 22-year-old with Type 1 Diabetes presents with vomiting, Kussmaul respirations, blood glucose 480 mg/dL, arterial pH 7.15, and serum beta-hydroxybutyrate 6.2 mmol/L. What is the initial resuscitation priority?",
                            optionA = "Immediate intravenous regular insulin bolus before fluid administration",
                            optionB = "Aggressive IV volume resuscitation with isotonic normal saline (0.9% NaCl)",
                            optionC = "Intravenous sodium bicarbonate infusion",
                            optionD = "Immediate administration of subcutaneous basal insulin glargine",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "Fluid resuscitation with 0.9% NaCl (1-1.5 L/hr initially) restores intravascular volume, promotes renal perfusion, and lowers counter-regulatory hormones before initiating insulin drip.",
                            clinicalHintEn = "Restoration of intravascular perfusion with isotonic crystalloid takes precedence in initial DKA management.",
                            imageResName = null
                        )
                    )
                    list.add(
                        QuestionEntity(
                            specialtyId = "PHYSICIAN_ASSISTANT",
                            stageLevel = stage,
                            subCategory = paCategory,
                            questionEn = "In the management of Diabetic Ketoacidosis (DKA), at what serum potassium threshold must intravenous potassium replacement be initiated BEFORE starting insulin therapy?",
                            optionA = "K+ < 3.3 mEq/L",
                            optionB = "K+ < 5.0 mEq/L",
                            optionC = "K+ > 6.0 mEq/L",
                            optionD = "K+ < 4.5 mEq/L",
                            correctOptionIndex = 0,
                            clinicalExplanationEn = "Insulin shifts potassium into cells. Starting insulin when serum K+ < 3.3 mEq/L can cause fatal hypokalemia, cardiac arrhythmias, and respiratory muscle paralysis.",
                            clinicalHintEn = "Insulin driving extracellular potassium into cells can trigger lethal arrhythmias if baseline potassium is below 3.3.",
                            imageResName = null
                        )
                    )
                }
                2 -> {
                    list.add(
                        QuestionEntity(
                            specialtyId = "PHYSICIAN_ASSISTANT",
                            stageLevel = stage,
                            subCategory = paCategory,
                            questionEn = "A 28-year-old female presents with acute right lower quadrant abdominal pain, anorexia, and localized tenderness at McBurney's point. Which clinical sign describes pain elicited by passive extension of the right hip?",
                            optionA = "Rovsing's sign",
                            optionB = "Psoas sign",
                            optionC = "Obturator sign",
                            optionD = "Blumberg's sign",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "The Psoas sign indicates retrocecal appendicitis when retroperitoneal inflammation irritates the iliopsoas muscle during passive hip extension.",
                            clinicalHintEn = "Extension of the right thigh tenses the retroperitoneal psoas muscle against the inflamed appendix.",
                            imageResName = null
                        )
                    )
                    list.add(
                        QuestionEntity(
                            specialtyId = "PHYSICIAN_ASSISTANT",
                            stageLevel = stage,
                            subCategory = paCategory,
                            questionEn = "What is the first-line vasopressor of choice for restoring mean arterial pressure in septic shock refractory to adequate fluid resuscitation?",
                            optionA = "Dopamine",
                            optionB = "Norepinephrine (Levophed)",
                            optionC = "Epinephrine alone",
                            optionD = "Phenylephrine",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "Surviving Sepsis Guidelines recommend Norepinephrine as the first-choice vasopressor due to potent alpha-1 vasoconstriction with modest beta-1 inotropic support, maintaining MAP >= 65 mmHg.",
                            clinicalHintEn = "The premier potent alpha-agonist vasopressor recommended in international Surviving Sepsis protocols.",
                            imageResName = null
                        )
                    )
                }
                else -> {
                    list.add(
                        QuestionEntity(
                            specialtyId = "PHYSICIAN_ASSISTANT",
                            stageLevel = stage,
                            subCategory = paCategory,
                            questionEn = "A 65-year-old smoker presents with productive cough, dyspnea, barrel chest, and forced expiratory volume in 1 second (FEV1/FVC ratio) of 0.58 post-bronchodilator. This confirms:",
                            optionA = "Idiopathic Pulmonary Fibrosis",
                            optionB = "Chronic Obstructive Pulmonary Disease (COPD)",
                            optionC = "Acute respiratory distress syndrome",
                            optionD = "Bronchiectasis without airflow obstruction",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "GOLD criteria define COPD by a persistent post-bronchodilator FEV1/FVC ratio < 0.70 in the presence of appropriate exposure history and chronic respiratory symptoms.",
                            clinicalHintEn = "Look for the obstructive pulmonary disease standardly diagnosed when post-bronchodilator FEV1/FVC falls below 0.70.",
                            imageResName = null
                        )
                    )
                    list.add(
                        QuestionEntity(
                            specialtyId = "PHYSICIAN_ASSISTANT",
                            stageLevel = stage,
                            subCategory = paCategory,
                            questionEn = "Which antiarrhythmic medication is class III potassium channel blocker approved for acute conversion and maintenance of normal sinus rhythm in hemodynamically stable atrial fibrillation?",
                            optionA = "Lidocaine",
                            optionB = "Amiodarone",
                            optionC = "Metoprolol",
                            optionD = "Adenosine",
                            correctOptionIndex = 1,
                            clinicalExplanationEn = "Amiodarone is a broad-spectrum Class III antiarrhythmic that prolongs action potential duration and effective refractory period across atrial and ventricular myocardium.",
                            clinicalHintEn = "Iodine-containing class III agent commonly utilized in ACLS and atrial arrhythmia protocols.",
                            imageResName = null
                        )
                    )
                }
            }
        }

        list.addAll(SupremeBoardQuestionsData.getQuestions())
        list.addAll(AdenAndBoardQuestionsData.getQuestions())

        return list
    }
}
