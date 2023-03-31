package com.example.supdevinciweatherapp.commonModels

data class Coordinate(
    val longitude: Float,
    val latitude: Float
)

data class City(val town: String, val longitude: Float, val latitude: Float)

val cities = listOf(
    City("Aix-en-Provence", 5.4474f, 43.5299f),
    City("Amiens", 2.2975f, 49.8942f),
    City("Angers", -0.5542f, 47.4784f),
    City("Besançon", 6.0333f, 47.2380f),
    City("Bordeaux", -0.5792f, 44.8378f),
    City("Brest", -4.4860f, 48.3904f),
    City("Clermont-Ferrand", 3.0870f, 45.7772f),
    City("Dijon", 5.0415f, 47.3220f),
    City("Grenoble", 5.7245f, 45.1885f),
    City("Lille", 3.0573f, 50.6292f),
    City("Le Havre", 0.1079f, 49.4944f),
    City("Le Mans", 0.1922f, 48.0061f),
    City("Limoges", 1.2611f, 45.8336f),
    City("Lyon", 4.8357f, 45.7640f),
    City("Marseille", 5.3698f, 43.2965f),
    City("Metz", 6.1757f, 49.1193f),
    City("Montpellier", 3.8767f, 43.6108f),
    City("Nantes", -1.5536f, 47.2184f),
    City("Nice", 7.2661f, 43.7031f),
    City("Nîmes", 4.3601f, 43.8367f),
    City("Orléans", 1.9039f, 47.9029f),
    City("Paris", 2.3522f, 48.8566f),
    City("Perpignan", 2.8956f, 42.6880f),
    City("Reims", 4.0317f, 49.2583f),
    City("Rennes", -1.6743f, 48.1173f),
    City("Saint-Etienne", 4.3872f, 45.4397f),
    City("Strasbourg", 7.7521f, 48.5734f),
    City("Talence", -0.61f, 44.81f),
    City("Toulon", 5.9290f, 43.1242f),
    City("Toulouse", 1.4442f, 43.6045f),
    City("Tours", 0.6892f, 47.3941f),
    City("Villeurbanne", 4.8807f, 45.7669f)
)
data class CounrtyCoordonates(
    val name: String,
    val latitude: Float,
    val longitude: Float,
    val country: String,
    val state: String
)