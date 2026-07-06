const url = "http://localhost:8081/api/auth/register";

const payload_empty = {
    fullName: "Test Pilgrim",
    email: "test@example.com",
    phone: "",
    gender: "Male",
    age: 30,
    holyDipDate: "2027-01-14",
    bookingDate: "2026-06-30",
    presentAddress: "Address 1",
    presentDistrict: "District",
    presentState: "State",
    presentPinCode: "123456",
    permanentAddress: "Address 1",
    permanentDistrict: "District",
    permanentState: "State",
    permanentPinCode: "123456",
    isPresentCoApplicant: "No",
    comorbidities: "None"
};

async function logResponse(name, res) {
    const text = await res.text();
    let parsed;
    try {
        parsed = JSON.parse(text);
    } catch(e) {
        parsed = text;
    }
    console.log(`${name}: status = ${res.status}, body =`, parsed);
}

async function runTests() {
    // Test 1: Empty Phone Number
    let res1 = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload_empty)
    });
    await logResponse("Test 1 (Empty Phone)", res1);

    // Test 2: Invalid format (9 digits)
    let payload_9 = { ...payload_empty, phone: "123456789" };
    let res2 = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload_9)
    });
    await logResponse("Test 2 (9 Digits)", res2);

    // Test 3: Invalid characters (letters)
    let payload_chars = { ...payload_empty, phone: "123456789a" };
    let res3 = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload_chars)
    });
    await logResponse("Test 3 (With Letters)", res3);

    // Test 4: Valid 10-digit phone
    let payload_valid = { ...payload_empty, phone: "9876543210" };
    let res4 = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload_valid)
    });
    await logResponse("Test 4 (Valid 10 Digits)", res4);
}

runTests().catch(err => console.error("Test execution failed:", err));
