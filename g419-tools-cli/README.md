# Actions

## wordnet-wupalmer-cli

Calculate Wu-Palmer similarity for given two lemmas on plWordNet 3.0 (https://clarin-pl.eu/dspace/handle/11321/273).

```bash
./tools-cli wordnet-wupalmer-cli
```

```bash
 INFO [main] (WordnetWuPalmerCli.java:31) - Loading wordnet ...
 INFO [main] (WordnetWuPalmerCli.java:33) - done.
```

Input two words separated with semicolon:
```bash
Enter two words (w1;w2), leave empty to exit: ręka;noga
```

Output:
```bash
[65383] dłoń.1, ręka.3 (rzeczownik) -- chwytna część kończyny górnej
 0.200: [390358] noga.6 (rzeczownik) -- 
 0.200: [1962] noga.5 (rzeczownik) -- 
 0.200: [75897] dętka.3, noga.1 (rzeczownik) -- 
 0.200: [14523] piłka nożna.1, futbol.1, soccer.1, noga.3 (rzeczownik) -- brak danych
 0.182: [1961] noga.2 (rzeczownik) -- 
 0.167: [3269] noga.4 (rzeczownik) -- 

[51880] ręka.4 (rzeczownik) -- o człowieku jako wykonawcy jakiejś pracy
 0.200: [390358] noga.6 (rzeczownik) -- 
 0.200: [1962] noga.5 (rzeczownik) -- 
 0.200: [75897] dętka.3, noga.1 (rzeczownik) -- 
 0.200: [14523] piłka nożna.1, futbol.1, soccer.1, noga.3 (rzeczownik) -- brak danych
 0.182: [1961] noga.2 (rzeczownik) -- 
 0.167: [3269] noga.4 (rzeczownik) -- 

[51881] ręka.2 (rzeczownik) -- o sposobie wykonywania czegoś
 0.167: [390358] noga.6 (rzeczownik) -- 
 0.167: [1962] noga.5 (rzeczownik) -- 
 0.167: [75897] dętka.3, noga.1 (rzeczownik) -- 
 0.167: [14523] piłka nożna.1, futbol.1, soccer.1, noga.3 (rzeczownik) -- brak danych
 0.154: [1961] noga.2 (rzeczownik) -- 
 0.143: [3269] noga.4 (rzeczownik) -- 

[51882] ręka.5 (rzeczownik) -- wykroczenie w piłce nożnej, niedozwolone dotknięcie piłki ręką
 0.182: [390358] noga.6 (rzeczownik) -- 
 0.182: [1962] noga.5 (rzeczownik) -- 
 0.182: [75897] dętka.3, noga.1 (rzeczownik) -- 
 0.182: [14523] piłka nożna.1, futbol.1, soccer.1, noga.3 (rzeczownik) -- brak danych
 0.167: [1961] noga.2 (rzeczownik) -- 
 0.154: [3269] noga.4 (rzeczownik) -- 

[51884] ręka.6 (rzeczownik) -- pozwolenie na ślub, obietnica zaręczyn
 0.133: [390358] noga.6 (rzeczownik) -- 
 0.133: [1962] noga.5 (rzeczownik) -- 
 0.133: [75897] dętka.3, noga.1 (rzeczownik) -- 
 0.133: [14523] piłka nożna.1, futbol.1, soccer.1, noga.3 (rzeczownik) -- brak danych
 0.125: [1961] noga.2 (rzeczownik) -- 
 0.118: [3269] noga.4 (rzeczownik) -- 

[6701] ręka.1 (rzeczownik) -- swobodna część kończyny górnej człowieka
 0.800: [1961] noga.2 (rzeczownik) -- 
 0.750: [3269] noga.4 (rzeczownik) -- 
 0.714: [390358] noga.6 (rzeczownik) -- 
 0.286: [1962] noga.5 (rzeczownik) -- 
 0.286: [75897] dętka.3, noga.1 (rzeczownik) -- 
 0.125: [14523] piłka nożna.1, futbol.1, soccer.1, noga.3 (rzeczownik) -- brak danych

[21696] piłka ręczna.1, szczypiorniak.1, ręka.7 (rzeczownik) -- 
 0.857: [14523] piłka nożna.1, futbol.1, soccer.1, noga.3 (rzeczownik) -- brak danych
 0.143: [390358] noga.6 (rzeczownik) -- 
 0.143: [1962] noga.5 (rzeczownik) -- 
 0.143: [75897] dętka.3, noga.1 (rzeczownik) -- 
 0.133: [1961] noga.2 (rzeczownik) -- 
 0.125: [3269] noga.4 (rzeczownik) -- 
```