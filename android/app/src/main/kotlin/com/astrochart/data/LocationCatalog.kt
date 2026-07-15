package com.astrochart.data

/**
 * A bundled catalog of major world cities with their coordinates and IANA
 * time-zone id. Lets the birth-input screen offer a location dropdown instead
 * of raw latitude/longitude entry, and works fully offline.
 *
 * Coverage is a curated set of major cities across every inhabited continent;
 * it can be extended as needed.
 */
data class LocationOption(
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: String
) {
    val displayName: String get() = "$city, $country"
}

object LocationCatalog {

    val locations: List<LocationOption> = listOf(
        // North America
        LocationOption("New York", "USA", 40.7128, -74.0060, "America/New_York"),
        LocationOption("Los Angeles", "USA", 34.0522, -118.2437, "America/Los_Angeles"),
        LocationOption("Chicago", "USA", 41.8781, -87.6298, "America/Chicago"),
        LocationOption("Houston", "USA", 29.7604, -95.3698, "America/Chicago"),
        LocationOption("San Francisco", "USA", 37.7749, -122.4194, "America/Los_Angeles"),
        LocationOption("Seattle", "USA", 47.6062, -122.3321, "America/Los_Angeles"),
        LocationOption("Denver", "USA", 39.7392, -104.9903, "America/Denver"),
        LocationOption("Miami", "USA", 25.7617, -80.1918, "America/New_York"),
        LocationOption("Washington", "USA", 38.9072, -77.0369, "America/New_York"),
        LocationOption("Toronto", "Canada", 43.6532, -79.3832, "America/Toronto"),
        LocationOption("Vancouver", "Canada", 49.2827, -123.1207, "America/Vancouver"),
        LocationOption("Mexico City", "Mexico", 19.4326, -99.1332, "America/Mexico_City"),
        // South America
        LocationOption("São Paulo", "Brazil", -23.5505, -46.6333, "America/Sao_Paulo"),
        LocationOption("Rio de Janeiro", "Brazil", -22.9068, -43.1729, "America/Sao_Paulo"),
        LocationOption("Buenos Aires", "Argentina", -34.6037, -58.3816, "America/Argentina/Buenos_Aires"),
        LocationOption("Lima", "Peru", -12.0464, -77.0428, "America/Lima"),
        LocationOption("Bogotá", "Colombia", 4.7110, -74.0721, "America/Bogota"),
        LocationOption("Santiago", "Chile", -33.4489, -70.6693, "America/Santiago"),
        // Europe
        LocationOption("London", "UK", 51.5074, -0.1278, "Europe/London"),
        LocationOption("Manchester", "UK", 53.4808, -2.2426, "Europe/London"),
        LocationOption("Paris", "France", 48.8566, 2.3522, "Europe/Paris"),
        LocationOption("Berlin", "Germany", 52.5200, 13.4050, "Europe/Berlin"),
        LocationOption("Madrid", "Spain", 40.4168, -3.7038, "Europe/Madrid"),
        LocationOption("Barcelona", "Spain", 41.3874, 2.1686, "Europe/Madrid"),
        LocationOption("Rome", "Italy", 41.9028, 12.4964, "Europe/Rome"),
        LocationOption("Amsterdam", "Netherlands", 52.3676, 4.9041, "Europe/Amsterdam"),
        LocationOption("Zurich", "Switzerland", 47.3769, 8.5417, "Europe/Zurich"),
        LocationOption("Vienna", "Austria", 48.2082, 16.3738, "Europe/Vienna"),
        LocationOption("Stockholm", "Sweden", 59.3293, 18.0686, "Europe/Stockholm"),
        LocationOption("Dublin", "Ireland", 53.3498, -6.2603, "Europe/Dublin"),
        LocationOption("Lisbon", "Portugal", 38.7223, -9.1393, "Europe/Lisbon"),
        LocationOption("Moscow", "Russia", 55.7558, 37.6173, "Europe/Moscow"),
        LocationOption("Istanbul", "Turkey", 41.0082, 28.9784, "Europe/Istanbul"),
        // Middle East
        LocationOption("Dubai", "UAE", 25.2048, 55.2708, "Asia/Dubai"),
        LocationOption("Abu Dhabi", "UAE", 24.4539, 54.3773, "Asia/Dubai"),
        LocationOption("Doha", "Qatar", 25.2854, 51.5310, "Asia/Qatar"),
        LocationOption("Riyadh", "Saudi Arabia", 24.7136, 46.6753, "Asia/Riyadh"),
        LocationOption("Tel Aviv", "Israel", 32.0853, 34.7818, "Asia/Jerusalem"),
        // Africa
        LocationOption("Cairo", "Egypt", 30.0444, 31.2357, "Africa/Cairo"),
        LocationOption("Lagos", "Nigeria", 6.5244, 3.3792, "Africa/Lagos"),
        LocationOption("Nairobi", "Kenya", -1.2921, 36.8219, "Africa/Nairobi"),
        LocationOption("Johannesburg", "South Africa", -26.2041, 28.0473, "Africa/Johannesburg"),
        LocationOption("Cape Town", "South Africa", -33.9249, 18.4241, "Africa/Johannesburg"),
        LocationOption("Casablanca", "Morocco", 33.5731, -7.5898, "Africa/Casablanca"),
        // South Asia
        LocationOption("Mumbai", "India", 19.0760, 72.8777, "Asia/Kolkata"),
        LocationOption("Delhi", "India", 28.7041, 77.1025, "Asia/Kolkata"),
        LocationOption("Bengaluru", "India", 12.9716, 77.5946, "Asia/Kolkata"),
        LocationOption("Chennai", "India", 13.0827, 80.2707, "Asia/Kolkata"),
        LocationOption("Hyderabad", "India", 17.3850, 78.4867, "Asia/Kolkata"),
        LocationOption("Kolkata", "India", 22.5726, 88.3639, "Asia/Kolkata"),
        LocationOption("Pune", "India", 18.5204, 73.8567, "Asia/Kolkata"),
        LocationOption("Ahmedabad", "India", 23.0225, 72.5714, "Asia/Kolkata"),
        LocationOption("Coimbatore", "India", 11.0168, 76.9558, "Asia/Kolkata"),
        LocationOption("Erode", "India", 11.3410, 77.7172, "Asia/Kolkata"),
        LocationOption("Madurai", "India", 9.9252, 78.1198, "Asia/Kolkata"),
        LocationOption("Tiruchirappalli", "India", 10.7905, 78.7047, "Asia/Kolkata"),
        LocationOption("Salem", "India", 11.6643, 78.1460, "Asia/Kolkata"),
        LocationOption("Tirunelveli", "India", 8.7139, 77.7567, "Asia/Kolkata"),
        LocationOption("Tiruppur", "India", 11.1085, 77.3411, "Asia/Kolkata"),
        LocationOption("Thanjavur", "India", 10.7870, 79.1378, "Asia/Kolkata"),
        LocationOption("Vellore", "India", 12.9165, 79.1325, "Asia/Kolkata"),
        LocationOption("Nagercoil", "India", 8.1833, 77.4119, "Asia/Kolkata"),
        LocationOption("Thiruvananthapuram", "India", 8.5241, 76.9366, "Asia/Kolkata"),
        LocationOption("Kochi", "India", 9.9312, 76.2673, "Asia/Kolkata"),
        LocationOption("Kozhikode", "India", 11.2588, 75.7804, "Asia/Kolkata"),
        LocationOption("Jaipur", "India", 26.9124, 75.7873, "Asia/Kolkata"),
        LocationOption("Lucknow", "India", 26.8467, 80.9462, "Asia/Kolkata"),
        LocationOption("Kanpur", "India", 26.4499, 80.3319, "Asia/Kolkata"),
        LocationOption("Nagpur", "India", 21.1458, 79.0882, "Asia/Kolkata"),
        LocationOption("Surat", "India", 21.1702, 72.8311, "Asia/Kolkata"),
        LocationOption("Indore", "India", 22.7196, 75.8577, "Asia/Kolkata"),
        LocationOption("Bhopal", "India", 23.2599, 77.4126, "Asia/Kolkata"),
        LocationOption("Patna", "India", 25.5941, 85.1376, "Asia/Kolkata"),
        LocationOption("Visakhapatnam", "India", 17.6868, 83.2185, "Asia/Kolkata"),
        LocationOption("Vijayawada", "India", 16.5062, 80.6480, "Asia/Kolkata"),
        LocationOption("Bhubaneswar", "India", 20.2961, 85.8245, "Asia/Kolkata"),
        LocationOption("Chandigarh", "India", 30.7333, 76.7794, "Asia/Kolkata"),
        LocationOption("Guwahati", "India", 26.1445, 91.7362, "Asia/Kolkata"),
        LocationOption("Colombo", "Sri Lanka", 6.9271, 79.8612, "Asia/Colombo"),
        LocationOption("Karachi", "Pakistan", 24.8607, 67.0011, "Asia/Karachi"),
        LocationOption("Lahore", "Pakistan", 31.5204, 74.3587, "Asia/Karachi"),
        LocationOption("Islamabad", "Pakistan", 33.6844, 73.0479, "Asia/Karachi"),
        LocationOption("Dhaka", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka"),
        LocationOption("Chittagong", "Bangladesh", 22.3569, 91.7832, "Asia/Dhaka"),
        LocationOption("Kathmandu", "Nepal", 27.7172, 85.3240, "Asia/Kathmandu"),
        LocationOption("Malé", "Maldives", 4.1755, 73.5093, "Indian/Maldives"),
        // East & Southeast Asia
        LocationOption("Singapore", "Singapore", 1.3521, 103.8198, "Asia/Singapore"),
        LocationOption("Hong Kong", "China", 22.3193, 114.1694, "Asia/Hong_Kong"),
        LocationOption("Shanghai", "China", 31.2304, 121.4737, "Asia/Shanghai"),
        LocationOption("Beijing", "China", 39.9042, 116.4074, "Asia/Shanghai"),
        LocationOption("Guangzhou", "China", 23.1291, 113.2644, "Asia/Shanghai"),
        LocationOption("Shenzhen", "China", 22.5431, 114.0579, "Asia/Shanghai"),
        LocationOption("Chengdu", "China", 30.5728, 104.0668, "Asia/Shanghai"),
        LocationOption("Chongqing", "China", 29.4316, 106.9123, "Asia/Shanghai"),
        LocationOption("Wuhan", "China", 30.5928, 114.3055, "Asia/Shanghai"),
        LocationOption("Xi'an", "China", 34.3416, 108.9398, "Asia/Shanghai"),
        LocationOption("Hangzhou", "China", 30.2741, 120.1551, "Asia/Shanghai"),
        LocationOption("Nanjing", "China", 32.0603, 118.7969, "Asia/Shanghai"),
        LocationOption("Tianjin", "China", 39.3434, 117.3616, "Asia/Shanghai"),
        LocationOption("Suzhou", "China", 31.2989, 120.5853, "Asia/Shanghai"),
        LocationOption("Qingdao", "China", 36.0671, 120.3826, "Asia/Shanghai"),
        LocationOption("Zhengzhou", "China", 34.7466, 113.6254, "Asia/Shanghai"),
        LocationOption("Changsha", "China", 28.2282, 112.9388, "Asia/Shanghai"),
        LocationOption("Shenyang", "China", 41.8057, 123.4315, "Asia/Shanghai"),
        LocationOption("Harbin", "China", 45.8038, 126.5350, "Asia/Shanghai"),
        LocationOption("Kunming", "China", 25.0389, 102.7183, "Asia/Shanghai"),
        LocationOption("Ürümqi", "China", 43.8256, 87.6168, "Asia/Urumqi"),
        LocationOption("Taipei", "Taiwan", 25.0330, 121.5654, "Asia/Taipei"),
        LocationOption("Kaohsiung", "Taiwan", 22.6273, 120.3014, "Asia/Taipei"),
        LocationOption("Tokyo", "Japan", 35.6762, 139.6503, "Asia/Tokyo"),
        LocationOption("Osaka", "Japan", 34.6937, 135.5023, "Asia/Tokyo"),
        LocationOption("Seoul", "South Korea", 37.5665, 126.9780, "Asia/Seoul"),
        LocationOption("Bangkok", "Thailand", 13.7563, 100.5018, "Asia/Bangkok"),
        LocationOption("Jakarta", "Indonesia", -6.2088, 106.8456, "Asia/Jakarta"),
        LocationOption("Surabaya", "Indonesia", -7.2575, 112.7521, "Asia/Jakarta"),
        LocationOption("Denpasar", "Indonesia", -8.6705, 115.2126, "Asia/Makassar"),
        LocationOption("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        LocationOption("Manila", "Philippines", 14.5995, 120.9842, "Asia/Manila"),
        LocationOption("Ho Chi Minh City", "Vietnam", 10.8231, 106.6297, "Asia/Ho_Chi_Minh"),
        LocationOption("Hanoi", "Vietnam", 21.0278, 105.8342, "Asia/Ho_Chi_Minh"),
        LocationOption("Yangon", "Myanmar", 16.8409, 96.1735, "Asia/Yangon"),
        LocationOption("Phnom Penh", "Cambodia", 11.5564, 104.9282, "Asia/Phnom_Penh"),
        LocationOption("Tashkent", "Uzbekistan", 41.2995, 69.2401, "Asia/Tashkent"),
        LocationOption("Almaty", "Kazakhstan", 43.2220, 76.8512, "Asia/Almaty"),
        LocationOption("Tehran", "Iran", 35.6892, 51.3890, "Asia/Tehran"),
        LocationOption("Baghdad", "Iraq", 33.3152, 44.3661, "Asia/Baghdad"),
        LocationOption("Kuwait City", "Kuwait", 29.3759, 47.9774, "Asia/Kuwait"),
        LocationOption("Muscat", "Oman", 23.5880, 58.3829, "Asia/Muscat"),
        // Oceania
        LocationOption("Sydney", "Australia", -33.8688, 151.2093, "Australia/Sydney"),
        LocationOption("Melbourne", "Australia", -37.8136, 144.9631, "Australia/Melbourne"),
        LocationOption("Perth", "Australia", -31.9505, 115.8605, "Australia/Perth"),
        LocationOption("Auckland", "New Zealand", -36.8485, 174.7633, "Pacific/Auckland")
    ).sortedBy { it.displayName }

    fun byDisplayName(displayName: String): LocationOption? =
        locations.firstOrNull { it.displayName == displayName }
}
