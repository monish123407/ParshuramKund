package com.parshuramKund.Service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class AiChatService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiChatService.class);

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    public String generateResponse(String userQuery) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return "Hello! I am your Parshuram Kund Mela Assistant. How can I help you today?";
        }

        String query = userQuery.toLowerCase().trim();

        // 1. Try Gemini API if key is available
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = System.getProperty("gemini.api.key");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = geminiApiKey;
        }

        if (apiKey != null && !apiKey.trim().isEmpty()) {
            log.info("Gemini API key found, querying generative model...");
            String geminiResponse = queryGemini(userQuery, apiKey.trim());
            if (geminiResponse != null && !geminiResponse.trim().isEmpty()) {
                return geminiResponse;
            }
            log.warn("Gemini query returned empty or failed. Falling back to local knowledge base.");
        }

        // 2. Local Knowledge Base Fallback / Core Matcher
        return generateLocalResponse(query, userQuery);
    }

    private String queryGemini(String userQuery, String apiKey) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
            
            String systemInstructions = "You are the official Mela AI Assistant for the Parshuram Kund Makar Sankranti Mela 2027. "
                + "Answer the user's question politely and accurately. Keep your response formatting clean (use markdown). "
                + "Here is the official knowledge base context for the Mela:\n"
                + "- Location: Lohit District, Arunachal Pradesh, India. Nestled near the Lohit River.\n"
                + "- Significance: Holy Hindu pilgrimage site. Sage Parshuram washed away his matricide sin by taking a holy dip here on Makar Sankranti.\n"
                + "- Main Dip Date: 14th January 2027. Other dates: 12-16th January 2027.\n"
                + "- Registration: Mandatory entry pass. Register at the Register tab, confirm details, download PDF. Lost passes can be downloaded on 'Print Pass' tab by search.\n"
                + "- Transport: Nearest airport is Tezu and Dibrugarh. Nearest railway is Tinsukia. Well connected by road buses from Dibrugarh/Tinsukia/Namsai.\n"
                + "- Stay: Free administration tents with community kitchen (Langar), circuit houses at Tezu, budget hotels at Tezu.\n"
                + "- Emergency Hotline: +91 9233495795. DC Office Tezu: 03804-224485. Email: dc-lohit-arn@nic.in. Police/medical booths every 500m.\n"
                + "- Rules: Carry original photo ID, register comorbidities, take bath only at designated ghats under life guard supervision.\n"
                + "- General: Weather is very cold in January (temperature 5-15 degrees), bring heavy woolen clothes. Local Arunachali cuisines and craft fairs are available.\n\n"
                + "User Question: " + userQuery;

            String escapedPrompt = systemInstructions.replace("\\", "\\\\")
                                                    .replace("\"", "\\\"")
                                                    .replace("\n", "\\n")
                                                    .replace("\r", "\\r");

            String jsonBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapedPrompt + "\"}]}]}";

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody, java.nio.charset.StandardCharsets.UTF_8))
                .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                int textIdx = body.indexOf("\"text\":");
                if (textIdx != -1) {
                    int startIdx = body.indexOf("\"", textIdx + 7);
                    int endIdx = startIdx + 1;
                    boolean escaped = false;
                    StringBuilder sb = new StringBuilder();
                    while (endIdx < body.length()) {
                        char c = body.charAt(endIdx);
                        if (escaped) {
                            if (c == 'n') sb.append('\n');
                            else if (c == 't') sb.append('\t');
                            else if (c == 'r') sb.append('\r');
                            else sb.append(c);
                            escaped = false;
                        } else if (c == '\\') {
                            escaped = true;
                        } else if (c == '"') {
                            break;
                        } else {
                            sb.append(c);
                        }
                        endIdx++;
                    }
                    return sb.toString();
                }
            }
            log.error("Gemini API call failed with status: {}, body: {}", response.statusCode(), response.body());
        } catch (Exception e) {
            log.error("Error calling Gemini API: ", e);
        }
        return null;
    }

    private String generateLocalResponse(String query, String userQuery) {
        // GREETINGS
        if (query.matches(".*\\b(hi|hello|hey|greetings|good morning|good afternoon|good evening)\\b.*")) {
            return "Greetings! I am the **Parshuram Kund AI Mela Assistant**. \n\n" +
                   "I can help you with details regarding:\n" +
                   "1. **Registration & Passes** (Process, Print Pass)\n" +
                   "2. **Holy Dip Dates** (Makar Sankranti timing)\n" +
                   "3. **Directions & Transport** (Air, Train, Road)\n" +
                   "4. **Accommodation** (Tents, Dharamshalas)\n" +
                   "5. **Emergency Helplines** & Medical Camps\n\n" +
                   "What would you like to know?";
        }

        // REGISTRATION / PASS
        if (query.contains("register") || query.contains("registration") || query.contains("pass") || 
            query.contains("apply") || query.contains("book") || query.contains("booking")) {
            return "### 📝 Pilgrim Registration & Passes\n\n" +
                   "Registration is mandatory to enter the Parshuram Kund Mela premises. Here is how you can register:\n" +
                   "1. Click the **Register** button in the top navigation bar.\n" +
                   "2. **Step 1-3**: Enter your personal details, comorbidities, contact information, and address.\n" +
                   "3. **Step 4**: Add details of any **Co-travellers** accompanying you.\n" +
                   "4. **Step 5**: Review all information, tick the official declaration statement, and click **Confirm & Submit**.\n\n" +
                   "Once submitted, you can download your entry pass PDF immediately. \n\n" +
                   "**Lost your pass?** Go to the **Print Pass** page and search using your registered Mobile Number, Email, Name, or Registration ID to download it again.";
        }

        // DATES
        if (query.contains("date") || query.contains("when") || query.contains("dip") || 
            query.contains("mela") || query.contains("timing") || query.contains("time")) {
            return "### 📅 Holy Dip Dates & Timing\n\n" +
                   "The holy dip festival at Parshuram Kund takes place during the auspicious occasion of **Makar Sankranti 2027**.\n\n" +
                   "**Available Holy Dip Dates:**\n" +
                   "- **12 JAN 2027** (Monday)\n" +
                   "- **13 JAN 2027** (Tuesday)\n" +
                   "- **14 JAN 2027** (Wednesday - **Makar Sankranti Main Dip**)\n" +
                   "- **15 JAN 2027** (Thursday)\n" +
                   "- **16 JAN 2027** (Friday)\n\n" +
                   "*Recommendation:* Please select your preferred date during registration to help the administration manage crowds and safety efficiently.";
        }

        // HOW TO REACH
        if (query.contains("reach") || query.contains("route") || query.contains("map") || 
            query.contains("train") || query.contains("airport") || query.contains("bus") || 
            query.contains("taxi") || query.contains("go") || query.contains("travel") || 
            query.contains("direction") || query.contains("locate") || query.contains("where")) {
            return "### ✈️ How to Reach Parshuram Kund\n\n" +
                   "Parshuram Kund is situated in the Lohit District of Arunachal Pradesh, India. You can reach it via:\n\n" +
                   "- **By Air**: The nearest airport is **Tezu Airport** (for regional flights) and **Dibrugarh Airport (Mohanbari)** for major national connections. From Dibrugarh, you can hire a taxi or catch a state transport bus (approx. 4-5 hours).\n" +
                   "- **By Train**: The nearest railway hub is **Tinsukia Junction**. From Tinsukia, regular private cabs and buses run daily to Tezu and directly to the Mela grounds.\n" +
                   "- **By Road**: It is well-connected by road networks. State transport (APSTS) buses run regularly from Dibrugarh, Tinsukia, and Namsai to Tezu, from where local shuttles take you to the Kund.";
        }

        // ACCOMMODATION
        if (query.contains("hotel") || query.contains("stay") || query.contains("accommodation") || 
            query.contains("dharmashala") || query.contains("tent") || query.contains("guest")) {
            return "### ⛺ Accommodation & Stay\n\n" +
                   "To facilitate pilgrims during the cold winter days, the District Administration provides several facilities:\n\n" +
                   "1. **Temporary Pilgrim Tents**: Free common tent accommodations set up near the Mela grounds with basic amenities, warm blankets, and community kitchens (Langar).\n" +
                   "2. **Dharamshalas**: Local trust dharamshalas are available at Tezu and Wakro (booking on arrival).\n" +
                   "3. **Government Guest Houses**: Circuit Houses in Tezu can be booked via district administrative channels for official/deluxe stays.\n" +
                   "4. **Private Hotels**: A variety of private budget hotels and lodges are located in Tezu town (approx. 45 km from the Kund).";
        }

        // EMERGENCY / HELP
        if (query.contains("emergency") || query.contains("contact") || query.contains("phone") || 
            query.contains("help") || query.contains("number") || query.contains("police") || 
            query.contains("medical") || query.contains("call") || query.contains("support")) {
            return "### 🚨 Helplines & Emergency Contacts\n\n" +
                   "If you need immediate assistance during the Mela, please contact these official helpline numbers:\n\n" +
                   "- **Mela Control Room Hotline**: **+91 9233495795**\n" +
                   "- **DC Office, Tezu**: **03804-224485**\n" +
                   "- **Official Administration Email**: **dc-lohit-arn@nic.in**\n\n" +
                   "*Safety Note:* Police control booths, disaster response units (NDRF/SDRF), and free 24/7 medical camps are positioned every 500 meters along the Kund path for your safety and security.";
        }

        // ABOUT / SIGNIFICANCE
        if (query.contains("about") || query.contains("history") || query.contains("mythology") || 
            query.contains("significance") || query.contains("why") || query.contains("story")) {
            return "### 🕉️ About Parshuram Kund\n\n" +
                   "Parshuram Kund is a holy Hindu pilgrimage site on the Brahmaputra plateau, nestled along the lower reaches of the Lohit River in Arunachal Pradesh.\n\n" +
                   "**Mythological Significance:**\n" +
                   "According to the Hindu scripture *Kalika Purana*, the great sage **Lord Parshuram** (an avatar of Lord Vishnu) washed away the terrible sin of matricide by taking a dip in the holy waters of this Brahma Kund on Makar Sankranti. The axe fell off his hand only after this bath.\n\n" +
                   "Every year in mid-January, lakhs of devotees from across India and neighboring countries visit the Kund, earning it the reputation as the **'Kumbh of the Northeast'**.";
        }

        // HEALTH / COMORBIDITIES
        if (query.contains("disease") || query.contains("diabetes") || query.contains("comorbidities") || 
            query.contains("asthma") || query.contains("illness") || query.contains("health")) {
            return "### 🩺 Health & Comorbidities Selection\n\n" +
                   "For safety and medical preparedness, the registration form asks for any pre-existing comorbidities (e.g. Diabetes, Heart Disease, Asthma):\n" +
                   "- You can select **multiple diseases** from the list.\n" +
                   "- If you do not have any chronic health condition, leave it empty or select **None**.\n" +
                   "- The same selection can be added for **Co-travellers** in Step 4.\n" +
                   "- This helps medical teams at the Mela identify high-risk devotees and provide quick support in emergencies.";
        }

        // WEATHER / CLOTHING
        if (query.contains("weather") || query.contains("climate") || query.contains("temp") || 
            query.contains("cold") || query.contains("winter") || query.contains("cloth") || 
            query.contains("sweater") || query.contains("wool")) {
            return "### ❄️ Weather & Warm Clothing\n\n" +
                   "The Parshuram Kund Mela is held in mid-January, which is the peak of winter in Lohit District, Arunachal Pradesh:\n" +
                   "- **Temperatures**: Expect temperatures to drop between **5°C to 15°C** (41°F to 59°F).\n" +
                   "- **Conditions**: Mornings and nights are highly foggy, cold, and windy due to proximity to the Lohit River.\n" +
                   "- **Clothing Recommendation**: Please carry heavy woolens, thermal innerwear, jackets, gloves, and warm headwear (caps/beanies) to protect yourself against the winter chill, especially if taking the dip in the early morning.";
        }

        // FOOD / LANGAR
        if (query.contains("food") || query.contains("eat") || query.contains("meal") || 
            query.contains("langar") || query.contains("restaurant") || query.contains("canteen") || 
            query.contains("water")) {
            return "### 🍽️ Food & Langar Facilities\n\n" +
                   "Pilgrims visiting the Mela do not need to worry about food arrangements:\n" +
                   "- **Free Langar (Community Kitchen)**: The district administration and local charity organisations set up free 24/7 Langars serving hot, fresh vegetarian food and tea to all pilgrims near the Mela grounds.\n" +
                   "- **Water Stations**: Free drinking water stalls are stationed every 200 meters along the walking pathway.\n" +
                   "- **Local Restaurants**: Small food stalls selling regional snacks, Assamese/Arunachali meals, and standard Indian dishes are available near the transit camp and parking areas.";
        }

        // INNER LINE PERMIT (ILP)
        if (query.contains("ilp") || query.contains("permit") || query.contains("entry") || 
            query.contains("permission") || query.contains("visa") || query.contains("foreigner")) {
            return "### 🎫 Entry Permit & ILP Requirements\n\n" +
                   "- **For Indian Pilgrims**: Typically, entering Arunachal Pradesh requires an **Inner Line Permit (ILP)**. However, for the Parshuram Kund Mela 2027, the **Pilgrim Entry Pass** generated from this website serves as a valid ILP waiver for the Mela period!\n" +
                   "- **For Foreign Nationals**: Foreigners need a **Protected Area Permit (PAP)**. They should register and contact the District Commissioner's office (dc-lohit-arn@nic.in) or nearest tourist office to process their permits beforehand.";
        }

        // PARKING / SHUTTLES
        if (query.contains("park") || query.contains("parking") || query.contains("car") || 
            query.contains("vehicle") || query.contains("bike") || query.contains("bus parking")) {
            return "### 🚗 Parking & Shuttle System\n\n" +
                   "- **Designated Parking Blocks**: To avoid congestion near the holy site, all private cars, bikes, and buses must be parked at the designated administration parking grounds located approx. **1.5 km before** the main entrance.\n" +
                   "- **Shuttles**: Free shuttle buses run frequently from the parking grounds to the entry gate.\n" +
                   "- **Battery Cars**: Golf carts/battery cars are available free of charge specifically for senior citizens and differently-abled pilgrims.";
        }

        // TOURISM / SIGHTSEEING
        if (query.contains("sight") || query.contains("visit") || query.contains("temple") || 
            query.contains("tour") || query.contains("around") || query.contains("near") || 
            query.contains("scenic") || query.contains("place")) {
            return "### 🏞️ Nearby Sightseeing & Attractions\n\n" +
                   "While visiting Parshuram Kund, you can explore several beautiful attractions in the Lohit/Namsai region:\n\n" +
                   "1. **Golden Pagoda (Namsai)**: A stunning Burmese-style Buddhist temple located about 80 km away, famous for its grand architecture and peaceful gardens.\n" +
                   "2. **Tezu Town**: The headquarters of Lohit district, known for its local museum, botanical garden, and vibrant local tribal markets.\n" +
                   "3. **Glow Lake**: A scenic high-altitude natural lake in the Wakro circle, popular for trekking and landscape views (requires special trekking guidance).\n" +
                   "4. **Parasuram Temple**: The historical temple shrine built near the Kund.";
        }

        // LOST AND FOUND
        if (query.contains("lost") || query.contains("found") || query.contains("miss") || 
            query.contains("belonging") || query.contains("luggage") || query.contains("wallet")) {
            return "### 🔍 Lost & Found Cell\n\n" +
                   "If you lose any of your belongings or co-travellers during the Mela:\n" +
                   "- **Lost & Found Helpdesk**: Walk directly to the Mela Control Room located at the main entry gate.\n" +
                   "- **Public Announcements**: The control room operates a centralized public address system to announce missing persons or found items.\n" +
                   "- **Police Assistance**: Inform the nearest police check post or contact the control room hotline at **+91 9233495795** immediately.";
        }

        // FEE / COST
        if (query.contains("price") || query.contains("cost") || query.contains("fee") || 
            query.contains("ticket") || query.contains("charge") || query.contains("free") || 
            query.contains("money")) {
            return "### 💰 Fees & Charges\n\n" +
                   "The Parshuram Kund Mela is organized under the patronage of the Government of Arunachal Pradesh and is highly pilgrim-friendly:\n" +
                   "- **Registration**: 100% Free. There is no charge for generating or printing the Entry Pass.\n" +
                   "- **Accommodation**: Free common tents (Langar stay) are provided by the administration. Government guest houses and private hotels in Tezu are paid.\n" +
                   "- **Langar Meals**: Free hot food is served to all pilgrims.\n" +
                   "- **Parking & Shuttles**: Free of charge.";
        }

        // SMART GENERIC EXTRACTOR FALLBACK
        StringBuilder fallbackResponse = new StringBuilder();
        fallbackResponse.append("### ℹ️ Parshuram Kund Mela Assistant\n\n");
        fallbackResponse.append("Thank you for your question regarding ");
        
        // Dynamic subject extraction
        String subject = "the Mela facilities";
        if (query.contains("kid") || query.contains("child") || query.contains("baby")) {
            subject = "children care and guidelines";
            fallbackResponse.append("**" + subject + "**.\n\n");
            fallbackResponse.append("Children are welcome at the Mela. Please ensure they carry a tag with your contact number and name at all times in case they get separated in the crowd. Warm clothing is highly recommended.");
        } else if (query.contains("senior") || query.contains("old") || query.contains("age")) {
            subject = "facilities for senior citizens";
            fallbackResponse.append("**" + subject + "**.\n\n");
            fallbackResponse.append("Senior citizens are provided with special assistance at the Mela. Free battery-operated golf carts are stationed at the parking area to transport seniors directly to the entry gates. Medical camps with wheel chairs are also available.");
        } else if (query.contains("mobile") || query.contains("network") || query.contains("internet") || query.contains("wifi")) {
            subject = "mobile connectivity at the Kund";
            fallbackResponse.append("**" + subject + "**.\n\n");
            fallbackResponse.append("Due to the remote location and large crowds, mobile network signals (Jio, Airtel, BSNL) can be congested. BSNL typically has the most reliable coverage in the Lohit valley. Free emergency communication booths are set up near control camps.");
        } else if (query.contains("toilet") || query.contains("bathroom") || query.contains("washroom") || query.contains("sanitation")) {
            subject = "sanitation and washrooms";
            fallbackResponse.append("**" + subject + "**.\n\n");
            fallbackResponse.append("The administration has set up temporary eco-toilets and changing rooms separately for men and women near all major bathing ghats and transit camps. Regular sanitation teams ensure they remain clean.");
        } else if (query.contains("camera") || query.contains("photo") || query.contains("video") || query.contains("drone")) {
            subject = "photography and drone guidelines";
            fallbackResponse.append("**" + subject + "**.\n\n");
            fallbackResponse.append("Photography for personal use is permitted throughout the Mela. However, flying drones is strictly prohibited without prior written clearance from the Lohit District Administration for security reasons.");
        } else if (query.contains("vip") || query.contains("special") || query.contains("queue")) {
            subject = "VIP entry or special passes";
            fallbackResponse.append("**" + subject + "**.\n\n");
            fallbackResponse.append("There are no special VIP entry privileges or paid fast-track queues for taking the holy dip. All pilgrims who have registered must stand in the unified queue for safety and crowd-control purposes.");
        } else {
            fallbackResponse.append("**your query**.\n\n");
            fallbackResponse.append("I don't have a specific rule-based article matching that exact query. However, as your Mela assistant, I can confirm that the Parshuram Kund Makar Sankranti Mela 2027 is a fully organized pilgrimage with free food, free tents, free entry passes, strict safety surveillance by police/SDRF, and 24/7 medical camps.\n\n");
            fallbackResponse.append("If you have specific logistical questions, you can contact the **Mela Control Room** at **+91 9233495795** or check our pages for:\n");
            fallbackResponse.append("- **Registration & Passes** (Type 'register')\n");
            fallbackResponse.append("- **Holy Dip Dates** (Type 'dates')\n");
            fallbackResponse.append("- **How to Reach** (Type 'reach')\n");
            fallbackResponse.append("- **Accommodation** (Type 'stay')\n");
            fallbackResponse.append("- **Emergency Contacts** (Type 'emergency')");
        }
        
        return fallbackResponse.toString();
    }
}
