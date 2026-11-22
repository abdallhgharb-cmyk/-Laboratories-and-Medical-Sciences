const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";
const DATABASE_ID = "ai-studio-android-54cab10f-fc9a-423b-a3bc-7db40e600537";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function dbFor(context) {
  return context.firestore(DATABASE_ID);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write user account, progress, or notes", async () => {
  const unauth = dbFor(testEnv.unauthenticatedContext());
  const now = new Date(Date.now() - 1000);

  await assertFails(unauth.collection("users").doc(ALICE_UID).get());
  await assertFails(unauth.collection("users").doc(ALICE_UID).collection("progress").get());
  await assertFails(unauth.collection("users").doc(ALICE_UID).collection("notes").get());

  await assertFails(
    unauth.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Dr. Alice",
      email: "alice@example.com",
      fullName: "Alice Lab",
      degreeType: "BACHELOR",
      specialty: "MED_LAB",
      totalScore: 100,
      totalCorrect: 5,
      totalAnswered: 6,
      masteredTopicsCount: 1,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Authenticated owner can create, update, and read their UserAccount document", async () => {
  const alice = dbFor(testEnv.authenticatedContext(ALICE_UID));
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Dr. Alice",
      email: "alice@example.com",
      fullName: "Alice Medical Lab",
      degreeType: "Bachelor",
      specialty: "Medical Labs",
      totalScore: 450,
      totalCorrect: 18,
      totalAnswered: 20,
      masteredTopicsCount: 2,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).update({
      totalScore: 600,
      totalCorrect: 24,
      totalAnswered: 25,
      masteredTopicsCount: 3,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(alice.collection("users").doc(ALICE_UID).get());

  // Reject ghost field & createdAt overwrite
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Authenticated owner can create, update, and list CloudProgress records", async () => {
  const alice = dbFor(testEnv.authenticatedContext(ALICE_UID));
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("progress").doc("MED_LAB__BACHELOR").set({
      userId: ALICE_UID,
      specialtyId: "MED_LAB",
      academicDegree: "BACHELOR",
      highestUnlockedStage: 3,
      completedStagesCount: 2,
      totalScore: 1200,
      totalCorrectAnswers: 45,
      totalQuestionsAnswered: 50,
      professionalRankEn: "Junior Specialist",
      professionalRankAr: "أخصائي متدرب",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("progress").doc("MED_LAB__BACHELOR").update({
      highestUnlockedStage: 4,
      completedStagesCount: 3,
      totalScore: 1800,
      totalCorrectAnswers: 60,
      totalQuestionsAnswered: 65,
      professionalRankEn: "Clinical Specialist",
      professionalRankAr: "أخصائي سريري معتمد",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("progress").get()
  );

  // Reject out-of-range stage > 10 or invalid specialty
  await assertFails(
    alice.collection("users").doc(ALICE_UID).collection("progress").doc("MED_LAB__MASTER").set({
      userId: ALICE_UID,
      specialtyId: "MED_LAB",
      academicDegree: "MASTER",
      highestUnlockedStage: 30,
      completedStagesCount: 2,
      totalScore: 100,
      totalCorrectAnswers: 5,
      totalQuestionsAnswered: 5,
      professionalRankEn: "Rank",
      professionalRankAr: "رتبة",
      createdAt: created,
      updatedAt: created,
    })
  );
});

test("Authenticated owner can create, update, list, and delete CloudNote records", async () => {
  const alice = dbFor(testEnv.authenticatedContext(ALICE_UID));
  const created = new Date(Date.now() - 5000);

  // Question-linked note (omitting optional fields if null, no @DocumentId id field)
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").set({
      userId: ALICE_UID,
      localNoteId: 1,
      questionId: 42,
      specialtyId: "MED_LAB",
      topic: "Hematology",
      title: "Sickle Cell Morphology",
      content: "Drepanocytes appear under Wright-Giemsa stain in HbSS.",
      clientUpdatedAtMs: Date.now() - 5000,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Standalone notebook note (questionId omitted)
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("notes").doc("note_2").set({
      userId: ALICE_UID,
      localNoteId: 2,
      specialtyId: "GENERAL",
      topic: "Quality Control",
      title: "Westgard 1-3s Rule",
      content: "Reject run when one control observation exceeds +/- 3 SD.",
      clientUpdatedAtMs: Date.now() - 4000,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").update({
      title: "Sickle Cell Smear Note",
      content: "Updated clinical pearl: target cells + Howell-Jolly bodies.",
      clientUpdatedAtMs: Date.now() - 1000,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(alice.collection("users").doc(ALICE_UID).collection("notes").get());
  await assertSucceeds(alice.collection("users").doc(ALICE_UID).collection("notes").doc("note_2").delete());

  // Reject `@DocumentId` 'id' key leakage or future timestamp
  await assertFails(
    alice.collection("users").doc(ALICE_UID).collection("notes").doc("note_bad").set({
      id: "note_bad",
      userId: ALICE_UID,
      localNoteId: 3,
      specialtyId: "MED_LAB",
      topic: "Hematology",
      title: "Invalid",
      content: "Should fail",
      clientUpdatedAtMs: Date.now(),
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertFails(
    alice.collection("users").doc(ALICE_UID).collection("notes").doc("note_future").set({
      userId: ALICE_UID,
      localNoteId: 4,
      specialtyId: "MED_LAB",
      topic: "Hematology",
      title: "Future",
      content: "Should fail",
      clientUpdatedAtMs: Date.now(),
      createdAt: new Date(Date.now() + 60000),
      updatedAt: new Date(Date.now() + 60000),
    })
  );
});

test("Cross-user isolation blocks Bob from reading or modifying Alice's data", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore(DATABASE_ID);
    const created = new Date(Date.now() - 10000);
    await db.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      fullName: "Alice",
      degreeType: "Bachelor",
      specialty: "MED_LAB",
      totalScore: 100,
      totalCorrect: 10,
      totalAnswered: 12,
      masteredTopicsCount: 1,
      createdAt: created,
      updatedAt: created,
    });
    await db.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").set({
      userId: ALICE_UID,
      localNoteId: 1,
      specialtyId: "MED_LAB",
      topic: "Hematology",
      title: "Private Note",
      content: "Confidential study note",
      clientUpdatedAtMs: Date.now() - 10000,
      createdAt: created,
      updatedAt: created,
    });
  });

  const bob = dbFor(testEnv.authenticatedContext(BOB_UID));
  await assertFails(bob.collection("users").doc(ALICE_UID).get());
  await assertFails(bob.collection("users").doc(ALICE_UID).collection("notes").get());
  await assertFails(
    bob.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").update({
      content: "Hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(bob.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").delete());
});
