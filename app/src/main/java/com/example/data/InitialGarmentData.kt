package com.example.data

object InitialGarmentData {
    fun getInitialGarments(): List<GarmentItem> {
        return listOf(
            GarmentItem(
                id = 1,
                categoryName = "Girls Tshirts",
                subStyle = "Round Neck / Printed",
                sizeVariations = listOf("12", "14", "16", "18", "20", "22", "24", "26", "28", "30", "32", "XS", "S", "M", "L", "XL", "XXL"),
                sizeTypeCategory = "Girls Numeric & Alpha Extended",
                handwrittenSizes = listOf("12", "XS", "XXL"),
                isPantModel = false,
                notes = "Comprehensive size range. Handwritten sizes (12, XS, XXL) scanned on right margin. Rates excluded.",
                itemCode = "GTS-001",
                stockPerSize = mapOf("12" to 15, "14" to 20, "16" to 25, "18" to 30, "20" to 35, "22" to 35, "24" to 30, "26" to 25, "28" to 20, "30" to 20, "32" to 15, "XS" to 18, "S" to 40, "M" to 45, "L" to 50, "XL" to 40, "XXL" to 20),
                totalStock = 463
            ),
            GarmentItem(
                id = 2,
                categoryName = "Ladies Night Set",
                subStyle = "Plain Set",
                sizeVariations = listOf("XS", "S", "M", "L", "XL", "XXL", "4XL", "5XL"),
                sizeTypeCategory = "Standard Alpha (XS-5XL)",
                handwrittenSizes = listOf("XS", "XXL", "4XL", "5XL"),
                isPantModel = false,
                notes = "Plain 2-piece loungewear set. Handwritten additions: XS, XXL, 4XL, 5XL.",
                itemCode = "LNS-PLN",
                stockPerSize = mapOf("XS" to 12, "S" to 25, "M" to 35, "L" to 35, "XL" to 30, "XXL" to 15, "4XL" to 10, "5XL" to 10),
                totalStock = 172
            ),
            GarmentItem(
                id = 3,
                categoryName = "Ladies Night Set",
                subStyle = "AOP Set",
                sizeVariations = listOf("XS", "S", "M", "L", "XL", "XXL", "4XL", "5XL"),
                sizeTypeCategory = "Standard Alpha (XS-5XL)",
                handwrittenSizes = listOf("XS", "XXL", "4XL", "5XL"),
                isPantModel = false,
                notes = "All-Over Print (AOP) 2-piece set. Handwritten additions: XS, XXL, 4XL, 5XL.",
                itemCode = "LNS-AOP",
                stockPerSize = mapOf("XS" to 10, "S" to 30, "M" to 40, "L" to 40, "XL" to 35, "XXL" to 20, "4XL" to 10, "5XL" to 10),
                totalStock = 195
            ),
            GarmentItem(
                id = 4,
                categoryName = "Ladies Night Set",
                subStyle = "Collar Set",
                sizeVariations = listOf("S", "M", "L", "XL", "XXL", "4XL", "5XL"),
                sizeTypeCategory = "Standard Alpha (S-5XL)",
                handwrittenSizes = listOf("XXL", "4XL", "5XL"),
                isPantModel = false,
                notes = "Button-down notched collar pyjama set. Extended size XXL, 4XL, 5XL handwritten on right.",
                itemCode = "LNS-COL",
                stockPerSize = mapOf("S" to 20, "M" to 30, "L" to 35, "XL" to 30, "XXL" to 18, "4XL" to 10, "5XL" to 10),
                totalStock = 153
            ),
            GarmentItem(
                id = 5,
                categoryName = "Feeding Frock",
                subStyle = "Concealed Zippers",
                sizeVariations = listOf("L", "XL", "XXL", "3XL"),
                sizeTypeCategory = "Maternity Alpha & Plus",
                handwrittenSizes = listOf("3XL"),
                isPantModel = false,
                notes = "Maternity feeding frock with vertical discreet zip openings. 3XL added on right margin.",
                itemCode = "FDF-ZIP",
                stockPerSize = mapOf("L" to 25, "XL" to 30, "XXL" to 25, "3XL" to 15),
                totalStock = 95
            ),
            GarmentItem(
                id = 6,
                categoryName = "Feeding Nighties",
                subStyle = "Front Open / Zip",
                sizeVariations = listOf("L", "XL", "XXL"),
                sizeTypeCategory = "Maternity Alpha",
                handwrittenSizes = listOf("XXL"),
                isPantModel = false,
                notes = "Full length maternity nighty. Handwritten XXL included.",
                itemCode = "FDN-001",
                stockPerSize = mapOf("L" to 20, "XL" to 25, "XXL" to 20),
                totalStock = 65
            ),
            GarmentItem(
                id = 7,
                categoryName = "Ladies Nighties",
                subStyle = "Regular / Sleeveless",
                sizeVariations = listOf("Free Size", "L", "XL", "XXL"),
                sizeTypeCategory = "Free Size & Extended",
                handwrittenSizes = listOf("XXL"),
                isPantModel = false,
                notes = "Dailywear cotton nighties. Free Size standard plus L, XL, XXL.",
                itemCode = "LNG-REG",
                stockPerSize = mapOf("Free Size" to 50, "L" to 30, "XL" to 35, "XXL" to 25),
                totalStock = 140
            ),
            GarmentItem(
                id = 8,
                categoryName = "Girls Frocks",
                subStyle = "A-Line & Flare",
                sizeVariations = listOf("12", "14", "16", "18", "20", "22", "24", "26", "28", "30", "32"),
                sizeTypeCategory = "Girls Numeric (12-32)",
                handwrittenSizes = listOf("12", "32"),
                isPantModel = false,
                notes = "Girls dresses in sequential numeric size range 12 through 32.",
                itemCode = "GFR-SET",
                stockPerSize = mapOf("12" to 10, "14" to 15, "16" to 20, "18" to 25, "20" to 25, "22" to 25, "24" to 20, "26" to 20, "28" to 15, "30" to 15, "32" to 10),
                totalStock = 200
            ),
            GarmentItem(
                id = 9,
                categoryName = "Ladies Tops",
                subStyle = "Short & Long Kurtis",
                sizeVariations = listOf("XS", "S", "M", "L", "XL", "XXL", "4XL", "5XL"),
                sizeTypeCategory = "Standard Alpha (XS-5XL)",
                handwrittenSizes = listOf("XS", "XXL", "4XL", "5XL"),
                isPantModel = false,
                notes = "Everyday printed tops and tunic styles. XS, XXL, 4XL, 5XL handwritten.",
                itemCode = "LTP-001",
                stockPerSize = mapOf("XS" to 15, "S" to 30, "M" to 40, "L" to 40, "XL" to 30, "XXL" to 20, "4XL" to 10, "5XL" to 10),
                totalStock = 195
            ),
            GarmentItem(
                id = 10,
                categoryName = "Ladies Pants",
                subStyle = "Ankle Length & Churidar Leggings",
                sizeVariations = listOf("34", "36", "38", "40"),
                sizeTypeCategory = "Pant Waist (34-40)",
                handwrittenSizes = listOf("40"),
                isPantModel = true,
                notes = "STRICT PANT SIZES ONLY: 34, 36, 38, 40. Strictly isolated from alpha tops and numeric kids sizes.",
                itemCode = "LPT-3440",
                stockPerSize = mapOf("34" to 40, "36" to 50, "38" to 50, "40" to 35),
                totalStock = 175
            ),
            GarmentItem(
                id = 11,
                categoryName = "Ladies Palazzo",
                subStyle = "Wide Flared Bottom",
                sizeVariations = listOf("34", "36", "38", "40", "Free Size"),
                sizeTypeCategory = "Pant Waist (34-40)",
                handwrittenSizes = listOf("40"),
                isPantModel = true,
                notes = "STRICT PANT SIZES: 34, 36, 38, 40 plus elastic Free Size. Strictly separated from top wear.",
                itemCode = "LPZ-FLR",
                stockPerSize = mapOf("34" to 20, "36" to 30, "38" to 30, "40" to 25, "Free Size" to 40),
                totalStock = 145
            ),
            GarmentItem(
                id = 12,
                categoryName = "Track Pants",
                subStyle = "Cotton Rib Joggers",
                sizeVariations = listOf("34", "36", "38", "40"),
                sizeTypeCategory = "Pant Waist (34-40)",
                handwrittenSizes = listOf("40"),
                isPantModel = true,
                notes = "STRICT PANT SIZES: 34, 36, 38, 40 waist measurements. Strictly isolated.",
                itemCode = "TRK-3440",
                stockPerSize = mapOf("34" to 25, "36" to 35, "38" to 35, "40" to 30),
                totalStock = 125
            )
        )
    }
}
