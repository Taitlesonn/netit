# NETIT - 1.3

**NETIT – Network Emulation and Topology Implementation Tool**

NETIT to lekkie oprogramowanie napisane głównie w Javie z użyciem bibliotek JavaFX oraz tomlj (org.tomlj). Interfejs graficzny został zaprojektowany na wzór GNS3. Program działa na zasadzie modularnych notatek.

## Zakładki główne:

- **Systemy** – Linux, Windows, Linux Server, Windows Server  
- **Urządzenia** – Ruter, Switch (L2)  
- **Funkcje** – Łącz, Rozłącz, Zapisz, Załaduj, Wyczyść, Edytor, Eksportuj  

## Podstawowe funkcje:

Po kliknięciu dowolnego elementu z zakładek "Systemy" lub "Urządzenia", obiekt zostanie dodany do topologii. Można go swobodnie przesuwać, łączyć z innymi elementami, rozłączać, usuwać lub edytować.

Kliknięcie na element otwiera okno dialogowe z zakładkami:
- **del** – usuwa element  
- **info** – wyświetla podstawowe informacje o obiekcie  

Program automatycznie wczytuje stworzone przez użytkownika notatki kture będą widoczne jako kolejne zakładki.

## Jak tworzyć notatki?

1. Otwórz **Edytor**
2. Ustaw:
   - Tytuł notatki
   - Typ urządzenia
   - Styl (mono, blue, gray)
3. Twórz zawartość notatki, korzystając ze składni:

```
# Duży tytuł z linią oddzielającą
* Mniejszy tytuł
Zwykły tekst
```

4. Aby dodać obraz:
   - Użyj opcji „Dodaj obraz”
   - Wstaw tag: `<img>1</img>` (gdzie `1` to indeks obrazu)
5. Zmień widok, aby zobaczyć podgląd.

### Przykład notatki:

```
# NETIT
* Co to?
Jest to narzędzie do tworzenia topologii. Na przykład jak ta na obrazku:

<img>1</img>
```

Po zmianie widoku zobaczysz:

![Obraz topologii](src/other/ob1.png)


### Modularność

Wystarczy że naciśniesz save by zapisać całą topologie do pliku .toml potem możesz ją wczytać gdziekolwiek a jeśli chcesz udostępnić notatki wystarczy nacisnąć export i dostaniesz folder z wszystkimi plikami w formie HTML, CSS, JS, oraz tekst.raw (forma #, * itd. )

Co najlepsze program ma pełne wsparcie dla HTMLa, css, JavaScriptu. Możesz samodzielnie wklejać pliki html z doswolnym formatowaniem skryptami linkami do stron itp i wszystko zadziała jak w przeglądarce bo program ma wbudowany silnik javafx.web. Oraz w edytorze możesz pisać po prostu HTML i wszystko zadziała jak normalnie.

### Skrypt
W repozytorium znajduje się skrypt use.sh działa on na tej zasadzie:

<pre>
Wywołanie: use.sh [ --clean ] [ --no-run ] [ --set-version <version>] [ --rpm ] [ --wininstaler ] [ --help ]
   --clean : czyści katalog wyjściowy
   --no-run : kompiluje projekt bez uruchamiania
   --set-version <version> : ustawia określoną wersje (dowolny string)
   --rpm : buduje cały projekt i tworzy repozytorium rpm
   --wininstaler : tworzy projekt na windows (domyślnie tworzy na linux)
   --help : wyświetlenie tego co teraz czytasz
 
Zmiennymi środowiskowymi można kontrolować:
   JAVA_FX_SDK : ścieżka do katalogu lib JavaFX
   MAVEN_ARGS dodatkowe flagi do mvn
   JAVA_ARGS dodatkowe falgi do javy

</pre>

 ## Pamiętaj by ustawić poprawną sicierzkę do JavyFX ! 
 Lub urzyj gotowca zbudowanego do rpm lub linstalatora na windows (po instalcji wpisz w konsoli netit):
 [Google Drive]( https://drive.google.com/drive/folders/17C-igY_6j2UcYgBPe5AKYI2uKE18AtZN?usp=drive_link)




---
 	🛈 Some of the icons used in this project were generated with the help of AI (ChatGPT by OpenAI). These icons were custom-made for this project and do not violate any license terms.
	
	*Happy network emulating!*
