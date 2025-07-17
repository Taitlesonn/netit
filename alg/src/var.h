#define VAR_H
#ifdef VAR_H

#include <stdint.h>
#include <stdlib.h>



static const uint32_t BUFFOR_F = 4294967295U;

const int8_t ruter_t = 0;
const int8_t switch_t = 1;
const int8_t linux_t = 2;
const int8_t windows_t = 3;
const int8_t linux_s_t = 4;
const int8_t windows_s_t = 5;

int16_t max_id = 1000;
int16_t min_id = 1;

// Bezpośrednia zmienna a nie wskaźnik do zmiennej 
// Skoro jej nie modyfikujemy to lepiej mieć "[node]" a nie zmmienna -> "[node]"
const char nd[] = "[node]";


typedef struct Node {
    int16_t id;
    int8_t type;
    int16_t *con;
    size_t con_len;
} Node;

typedef struct File_an {
    int32_t siz;
    int16_t lins;
    char **lines;
} File_an;

typedef struct Cunt_device{
    int16_t ruters;
    int16_t switches;
    int16_t linuxes;
    int16_t windowses;
    int16_t linux_servers;
    int16_t windows_servers;
} Cunt_device;

typedef struct Topologie{
    int16_t topologia_pierscienia;
    int16_t topologia_gwiazdy;
    int16_t topologia_siatki;
    int16_t topologia_drzewa;
    int16_t topologia_punkt_punkt;
    int16_t topologia_punkt_wielopunkt;
    int16_t topologia_hybrydowa;
    int16_t topologia_liniowa;
} Topologie;

typedef struct Working {
    Node *nlist;
    File_an fil;
    size_t dynamic_n;
    Cunt_device cn;
    Topologie tp;
} Working;



#endif
