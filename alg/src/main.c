#include <stdio.h>
#include <stdlib.h>

//Zmienne i struktury
#include "var.h"
// Funkcje analizy
#include "analiza.h"


/*
### Wspierane topologie

Z uwagi na ograniczone możliwości środowiska symulacyjnego **NETIT**, obsługiwane są następujące topologie:

- **Topologia pierścienia**
  * Każde urządzenie ma połączenie z dokładnie dwoma innymi urządzeniami.
  * Tworzy zamkniętą pętlę, po której dane krążą w określonym kierunku.
  * Stosowana np. w technologiach takich jak FDDI czy Token Ring.

 [A]───[B] 
  │     │
 [D]───[C]


- **Topologia gwiazdy (star)**
* Wszystkie urządzenia są połączone z jednym centralnym urządzeniem, takim jak switch lub hub.
* Centralny punkt stanowi jedyny kanał komunikacji między urządzeniami końcowymi.
* Awaria urządzenia centralnego prowadzi do przerwy w komunikacji całej sieci.


      [C]
       │
[A]──[ S ]──[B]
 │
[D]


- **Topologia siatki (mesh)**
* Każde urządzenie może mieć połączenie z wieloma innymi.
* Zapewnia dużą niezawodność i odporność na awarie.
* Występuje w dwóch odmianach:
  - **Pełna siatka (full mesh)**  

    [A]──[B]
     │ ╲  │
    [C]──[D]
   
  - **Częściowa siatka (partial mesh)**  

    [A]──[B]──[C]
     │     ╱
    [D]───┘
  

- **Topologia drzewa (tree)**
* Struktura hierarchiczna, przypominająca drzewo genealogiczne.
* Łączy cechy topologii magistrali i gwiazdy.
* Często spotykana w większych sieciach korporacyjnych.

     [Root]
        │
    ┌───┴───┐
 [A]       [B]
            │
           [C]


- **Topologia punkt-punkt (point-to-point)**
* Bezpośrednie połączenie między dwoma urządzeniami.
* Prosta, szybka i niezawodna forma komunikacji – często stosowana w połączeniach WAN.

[A]--[B]


- **Topologia punkt-wielopunkt (point-to-multipoint)**
* Jedno urządzenie (np. serwer, nadajnik) komunikuje się z wieloma urządzeniami końcowymi.
* Stosowana m.in. w sieciach bezprzewodowych i transmisjach satelitarnych.

      [M]
     ╱ │ ╲
   [A] [B] [C]


- **Topologia hybrydowa**
* Kombinacja różnych topologii w jednej sieci – np. połączenie gwiazdy z magistralą.
* Pozwala na optymalizację wydajności i zwiększenie elastyczności sieci.

    [Core]
    /  |  \
  [A] [B] [C]
       │
 [D]───[E]───[F]


- **Topologia liniowa**
* Odmiana topologii magistrali, gdzie każde urządzenie jest połączone liniowo z następnym.
* Nie tworzy zamkniętej pętli, co ogranicza redundancję.

[A]──[B]──[C]──[D]

*/

int main(int argc, char **argv){
    if(argc != 2){
    	return 1;
    }
    FILE *fp = fopen(argv[1], "r");
    if (fp == NULL) {
        fprintf(stderr, "File error");
	return 1;
    }
    char *buffor = (char *)malloc(BUFFOR_F); // bufor dla pliku 
    size_t len = fread(buffor, 1, BUFFOR_F - 1, fp);
    buffor[len] = '\0';    
    
    char *ret = analiz(buffor, BUFFOR_F); 
    if (!ret)
    {

      free(ret);
    }else{
      printf("%s\n", ret);
    }
    

    free(buffor);

    fclose(fp);
    return 0;
}
