# geneticAlgorithmKI
## Vorgehensweise
[X] Einlesen der Aufträge und LKWs  
[O] Initialisierung der Anfangs Population  
[O] Bewertung (Fitnessfunktion)  
[O] Auswahl  
[O] Reproduktion     
[O] Ersetzung  
[O] Terminierungskriterium  
[O] Ausgabe  

## 1. Einlesen der Aufträge und LKWs

## 2. Initialisierung der Anfangspopulation
  
Idee: Darstellung eines Individuums als 2D-Array (Integer):  

Zeilen: LKWs  
Spalten: Auftrag  
Individuum[n][m]=5 bedeutet: Der LKW n befördert 5 Kisten für den Auftrag m

Problem: Lösungen müssen compliant sein mit den Rahmenbedingungen:
- Ein LKW darf nicht mehr Kisten fahren als erlaubt
- Ein LKW darf nicht mehr Gewicht fahren als erlaubt
- Es sollen genauso viele Kisten transportiert werden wie für jeden Auftrag nötig
- Mehrere Aufträge auf einem LKW müssen das gleiche Ziel haben

Vorgehen:  
Generierung von zufälligen Lösungen, anschließende Überprüfung ob Rahmenbedingungen erfüllt werden. Wiederhole dies so lange bis die gewünschte Populationsgröße erlaubt ist.

## 3. Bewertung (Fitnessfunktion)  
## 4. Auswahl
## 5. Reproduktion
## 6. Ersetzung
## 7. Terminierungskriterium
## 8. Ausgabe