#define ANALIZA_H
#ifdef ANALIZA_H

#include <limits.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <stdbool.h>


bool has_duplicates(const int16_t* arr, size_t len) {
    // 65536 możliwych wartości int16_t (od -32768 do 32767)
    bool seen[65536] = {false};

    for (size_t i = 0; i < len; i++) {
        // przesunięcie zakresu: -32768 → 0, 32767 → 65535
        uint16_t index = (uint16_t)(arr[i] + 32768);
        if (seen[index]) {
            return true; // znaleziono duplikat
        }
        seen[index] = true;
    }
    return false;
}


void def_device(Node *n, Cunt_device *c){
    switch (n->type)
    {
    case ruter_t:
        (c->ruters)++;
        break;
    case switch_t:
        (c->switches)++;
        break;
    case linux_t:
        (c->linuxes)++;
        break;
    case windows_t:
        (c->windowses)++;
        break;
    case linux_s_t:
        (c->linux_servers)++;
        break;
    case windows_s_t:
        (c->windows_servers)++;
        break;
    default:
        break;
    }
}

void wstempne_dzielenie(Working *w) {
    for (int16_t i = 0; i < w->fil.lins; i++) {
        if (strcmp(w->fil.lines[i], nd) == 0) {
            w->dynamic_n++;
            Node *new_r = realloc(w->nlist, w->dynamic_n * sizeof(Node));
            if (new_r == NULL) {
                fprintf(stderr, "Alloc ERROR \n");
                exit(1);
            }
            w->nlist = new_r;
        } else {
            if (w->fil.lines[i][0] == 'i') {
                if (w->fil.lines[i][5] == '\0') {
                    fprintf(stderr, "id error\n");
                    exit(1);
                }
                w->nlist[w->dynamic_n - 1].id = (int16_t)atoi(&w->fil.lines[i][5]);
                
            } else if (w->fil.lines[i][0] == 't') {
                w->nlist[w->dynamic_n - 1].type = (int8_t)atoi(&w->fil.lines[i][7]);
            } else if (w->fil.lines[i][0] == 'c') {
                size_t count = 0;
                const char *start = strchr(w->fil.lines[i], '[');
                const char *end = strchr(w->fil.lines[i], ']');
                if (!start || !end || end <= start + 1) {
                    w->nlist[w->dynamic_n - 1].con = NULL;
                    continue;
                }
                size_t len = (size_t)(end - start - 1);
                char *buffer = (char *)malloc(len + 1);
                if (!buffer) {
                    w->nlist[w->dynamic_n - 1].con = NULL;
                    continue;
                }
                strncpy(buffer, start + 1, len);
                buffer[len] = '\0';

                size_t cap = 4;
                int16_t *values = (int16_t *)malloc(cap * sizeof(int16_t));
                if (!values) {
                    free(buffer);
                    exit(1);
                }

                char *token = strtok(buffer, ",");
                while (token) {
                    if (count >= cap) {
                        cap *= 2;
                        int16_t *tmp = realloc(values, cap * sizeof(int16_t));
                        if (!tmp) {
                            free(buffer);
                            free(values);
                            exit(1);
                        }
                        values = tmp;
                    }
                    values[count++] = (int16_t)atoi(token);
                    token = strtok(NULL, ",");
                }
                free(buffer);
                w->nlist[w->dynamic_n -1].con_len = count;
                w->nlist[w->dynamic_n - 1].con = values;
            }
        }
    }
    int16_t arr[w->dynamic_n];
    for (size_t i = 0; i < w->dynamic_n; i++)
    {
        arr[i] = w->nlist[i].id;
    }
    if (has_duplicates(arr, w->dynamic_n))
    {
        fprintf(stderr, "ERROR ID HAS DUPLICATE \n");
        exit(1);
    }
    
      
}

char **split_lines(const char *str, size_t len, int16_t *out_count) {
    char **lines = NULL;
    size_t count = 0;

    size_t start = 0;
    for (size_t i = 0; i <= len; i++) {
        if (i == len || str[i] == '\n') {
            size_t linelen = i - start;
            char *line = malloc(linelen + 1);
            if (!line) {
                for (size_t j = 0; j < count; j++) free(lines[j]);
                free(lines);
                return NULL;
            }
            memcpy(line, &str[start], linelen);
            line[linelen] = '\0';

            char **temp = realloc(lines, (count + 1) * sizeof(char *));
            if (!temp) {
                for (size_t j = 0; j < count; j++) free(lines[j]);
                free(line);
                free(lines);
                return NULL;
            }

            lines = temp;
            lines[count++] = line;
            start = i + 1;
        }
    }
    if (count > INT16_MAX) {
        return NULL;
    }
    *out_count = (int16_t)count;
    return lines;
}

void print_all(Working *w){
    for (size_t i = 0; i < w->dynamic_n; i++){
    printf("\nid: %d type: %d\n", w->nlist[i].id, w->nlist[i].type);
    for (size_t j = 0; j < w->nlist[i].con_len; j++) {
        printf("Con nr: %zu  val: %d\n", j+1, w->nlist[i].con[j]);
    }
}
    printf("\nRuters: %d, Switches: %d, Linuxes: %d, Windowses: %d, Linux_s: %d, Windows_s: %d \n", w->cn.ruters, w->cn.switches, w->cn.linuxes, w->cn.windowses, w->cn.linux_servers, w->cn.windows_servers);
}

char *analiz(char *f, size_t buffor) {
    if (strlen(f) > buffor) {
        exit(1);
    }
    Working *work = (Working *)malloc(sizeof(Working));
    size_t len = strlen(f);
    if (len > INT32_MAX) {
        return NULL;
    }
    memset(work, 0, sizeof(work));
    work->fil.siz = (int32_t)len;
    work->fil.lines = split_lines(f, (size_t)work->fil.siz, &work->fil.lins);
   
    
    wstempne_dzielenie(work);
    
    // Podstawowe przypadki nie zależne od typu dla zerowych lub prawie zerowych topologi
    if(work->dynamic_n <= 2){
        switch(work->dynamic_n){
            case 0:
               return "Topologia pusta";
               break;
            case 1:
               return "Topologia punktowa";
               break;
            case 2:
               if (work->nlist[0].con_len == 0){
                    return "Topologia punktowa";
               }else{
                    return "Point to point";
               }
               break;
        }
    }
    for(size_t i = 0; i < work->dynamic_n; i++){
        Node *n = &work->nlist[i];
        for(size_t j = 0; j < n->con_len; j++){
            if(n->id == n->con[j]){
                printf("SELF CONECT ERROR \n");
                exit(1);
            }
        }
        def_device(n, &work->cn);
    }
    
    // Czy topologia ma ruter ? jeśli ma to karzda by otwrzymała +100 czyli bez celowe
    if (work->cn.ruters == 0){
        // Topologia siatki wymaga rutera
        work->tp.topologia_siatki -= 1000;
        // Inne jeśli 100 to nie wymaga a jeśli 50 to to zalerzy
        work->tp.topologia_gwiazdy += 50;
        work->tp.topologia_pierscienia += 100;
        work->tp.topologia_drzewa += 50;
        work->tp.topologia_punkt_punkt += 100;
        work->tp.topologia_punkt_wielopunkt += 50;
        work->tp.topologia_hybrydowa += 50;
        work->tp.topologia_hybrydowa += 100;
    }


    print_all(work);    
    
    for (int16_t i = 0; i < work->fil.lins; i++) {
        free(work->fil.lines[i]);
    }
    free(work->nlist);
    free(work);
    return NULL;
}

#endif
