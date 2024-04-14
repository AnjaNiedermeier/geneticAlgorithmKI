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

Ansatz 1:  
Generierung von zufälligen Lösungen, anschließende Überprüfung ob Rahmenbedingungen erfüllt werden. Wiederhole dies so lange bis die gewünschte Populationsgröße erlaubt ist.
--> Problem: Zufällige Lösungen erfüllen so gut wie nie die Kapazitäts Rahmenbedingungen der LKWs

Ansatz 2: 
Generierung der Lösungen unter berücksichtigung der Kapazitätsgrenzen der LKWs und der Ziel Restriktionen. Dafür werden Zufällige Aufträge mit zufälligen LKWs ausgefahren, solange deren Kapazitäten und Ziele dies Erlauben. Die Aufträge müssen dabei nicht komplett erfüllt werden, damit ein Individuum für die Population angenommen wird. #
--> Diese Lösung produzierte eine ausreichend gute Startpopulation.

## 3. Bewertung (Fitnessfunktion)  
Für die Bewertung der Fitness der Lösungen gibt es mehrere Möglichkeiten nach denen optimiert werden kann. Für ein Unternehmen ist letztendlich die Kosten/Gewinnfunktion ausschlaggebend, weshalb diese verwendet wird. Diese wird berechnet aus den Werten für Strafe, Entlohnung und Bonus unter berücksichtigung der Zeit.
Der höchste Gewinn bedeutet dabei die beste Lösung.
## 4. Auswahl
## 5. Reproduktion
## 6. Ersetzung
## 7. Terminierungskriterium
## 8. Ausgabe