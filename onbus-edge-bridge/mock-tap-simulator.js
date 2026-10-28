// onbus-edge-bridge/mock-tap-simulator.js
// Simulates student smart card taps against the Spring Boot Boarding Ingestion API

const BACKEND_URL = process.env.API_URL || "http://localhost/api/boarding/tap";

async function simulateCardTap(testLabel, cardUid, vehicleId = 1, lat = 2.9278, lng = 101.6418) {
  console.log(`\n======================================================`);
  console.log(`[TEST: ${testLabel}]`);
  console.log(`Tapping Card UID: "${cardUid}" on Vehicle ID: ${vehicleId}`);

  const payload = {
    cardUid: cardUid,
    vehicleId: vehicleId,
    latitude: lat,
    longitude: lng,
    timestamp: Date.now()
  };

  try {
    const res = await fetch(BACKEND_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const statusText = res.ok ? "SUCCESS (HTTP " + res.status + ")" : "RESPONSE (HTTP " + res.status + ")";
    const body = await res.json();
    console.log(`Status: ${statusText}`);
    console.log(`Response Body:`, JSON.stringify(body, null, 2));
    return body;
  } catch (error) {
    console.error(`Connection Error: ${error.message}`);
    console.error(`Ensure Docker containers are running (http://localhost/api/boarding/tap)`);
  }
}

async function runSimulationSuite() {
  console.log("Starting MMU Shuttle Bus NFC Tap Simulation Suite...");
  console.log("Target Ingestion Endpoint:", BACKEND_URL);

  // 1. Valid registered student tap (Tan Ah Teck - FCI)
  await simulateCardTap("1. Valid Registered Student Card", "3E4C6403", 1);

  // 2. Immediate re-tap with same card (< 5s cooldown test)
  console.log("\n... Attempting rapid re-tap within 1 second ...");
  await new Promise(r => setTimeout(r, 1000));
  await simulateCardTap("2. Debounce Test (Rapid Double-Tap)", "3E4C6403", 1);

  // 3. Second registered student tap (Priya Mohan - FOE)
  console.log("\n... Waiting 2 seconds then tapping different student card ...");
  await new Promise(r => setTimeout(r, 2000));
  await simulateCardTap("3. Second Registered Student Card", "4CB36203", 1);

  // 4. Unregistered card tap (Visitor or unknown NFC tag)
  await simulateCardTap("4. Unregistered Card Test", "DEADBEEF01", 1);

  // 5. Malformed / Malicious UID tap (Sanitization test)
  await simulateCardTap("5. Malformed UID Sanitization Test", "INVALID_CARD_XYZ!", 1);

  console.log("\n======================================================");
  console.log("Simulation complete!");
}

runSimulationSuite();

