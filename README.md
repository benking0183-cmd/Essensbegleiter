# Essensbegleiter – Android-Projekt

Eine einfache, offline nutzbare Android-App zum Tracken von Wasser, Mahlzeiten und Kalorien sowie täglichen Ess-/Trink-Aufgaben.

## Funktionen
- Wasser in ml erfassen, Tagesziel einstellen und Fortschritt sehen
- Mahlzeiten mit Kategorie und kcal eintragen und löschen
- Tageskalorien und Anzahl gespeicherter Mahlzeiten
- Tägliche Aufgaben abhaken und zurücksetzen
- Fünf Designs: Nothing, Wald, Ozean, Sunset und Hell
- Daten lokal auf dem Gerät (SharedPreferences), kein Konto/Internet erforderlich

## Öffnen und starten
1. Android Studio installieren.
2. `ErnaehrungsBegleiter`-Ordner entpacken.
3. In Android Studio **Open** wählen und den Projektordner auswählen.
4. Gradle-Synchronisierung abwarten (beim ersten Build werden Android-Gradle-Abhängigkeiten heruntergeladen).
5. Android-Handy per USB mit aktivierten Entwickleroptionen/USB-Debugging verbinden oder einen Emulator starten.
6. Auf **Run ▶** drücken.

## Hinweise
- Paketname: `at.ernaehrung.begleiter`
- Mindestversion: Android 8.0 (API 26)
- Ziel-SDK: 35
- Die App berechnet keine Nährwerte automatisch. Kalorien werden beim Eintragen manuell angegeben.
- Trinkmengen und Mahlzeiten werden lokal je Kalendertag gespeichert. Es gibt derzeit noch keine Benachrichtigungen, Barcode-Erkennung, Cloud-Synchronisierung oder Exportfunktion.
- Gesundheits-/Ernährungsziele sind individuell; voreingestellte Aufgaben sind allgemeine Erinnerungen und keine medizinische Empfehlung.
