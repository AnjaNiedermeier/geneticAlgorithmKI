# geneticAlgorithmKI
## Vorgehensweise
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
Dafür wird die Roulette Wheel Selection verwendet, das heißt Individuen mit einem höheren Fitness Wert haben eine proportional höhere Chance als Eltern ausgewählt zu werden. Um die Varianz innerhalb der Population jedoch beizubehalten, können auch Individuen mit einer niedrigeren Fitness vorkommen, dies allerdings nur mit einer geringeren Wahrscheinlichkeit. 

Das funktioniert so:
1. Aufaddieren aller Fitnesswerte der Population (totalFitness)
2. Generiere Zufallszahl zwischen 0 und totalFitness
3. Mappe die Zufallszahl mit dem Individuum, in dessen Range die Zufallszahl liegt
4. Gib den Index dieses Individuums zurück

## 5. Reproduktion
1. **Crossover**
Generiere aus den n Eltern n neue Strategien
Multiple Variants are implemented based of this source: https://content.wolfram.com/sites/13/2018/02/05-3-4.pdf
Ranking: 
1. Horizontal Band Crossover
2. Vertical Band Crossover
3. Block Crossover (Aufträge und LKWs werden gesplittet. Deshalb schlechte Ergebnisse keine Überraschung)
4. Uniform Crossover

2. **Mutation**
Mutiere einen gewissen Prozentsatz der neuen Lösungen/aller Lösungen
Only Mutating the offspring seems to work best
Gradually changing the mutation rate from low to high
## 6. Ersetzung
Wähle numParents zufällige Individuen aus, die ersetzt werden mit den neu generierten Kindern. Dadurch bleibt die Populationsgröße gleich.
## 7. Terminierungskriterium
## 8. Ausgabe