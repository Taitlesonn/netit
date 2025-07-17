package com.netit;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Topology: klasa zarządzająca panelem sieciowym, na którym można
 * dodawać, przesuwać oraz łączyć/rozłączać węzły reprezentowane przez przyciski.
 *
 * Funkcjonalności:
 *  - Dodawanie systemów (router, switch, serwery) na panel
 *  - Przeciąganie i upuszczanie przycisków w celu zmiany położenia
 *  - Otwieranie okienek kontekstowych z detalami systemów
 *  - Łączenie i rozłączanie węzłów za pomocą linii
 *  - Automatyczne czyszczenie połączeń usuniętych węzłów
 */
public class Topology {
    //----------------------------------------------------------------------------------
    // Pola statyczne odpowiadające za tryby łączenia/rozłączania
    private static ToggleButton connectToggle;      // ToggleButton uruchamiający tryb łączenia
    private static ToggleButton disconnectToggle;   // ToggleButton uruchamiający tryb rozłączania

    // Pojedyncze instancje handlerów zdarzeń dla trybów
    private static final EventHandler<MouseEvent> connectHandler    = new ConnectHandler();
    private static final EventHandler<MouseEvent> disconnectHandler = new DisconnectHandler();

    // Główny panel, na którym umieszczane są przyciski (węzły) i linie połączeń
    public static Pane workPanel;

    // Lista obiektów Point przechowująca informacje o współrzędnych i typie każdego węzła
    private static final List<Point> points = new ArrayList<>();

    // Lista przycisków reprezentujących fizyczne węzły (systemy)
    private static final List<Button> sys = new ArrayList<>();

    // Lista obiektów Connection przechowujących referencje do połączonych przycisków i odpowiadającej im linii
    public static final List<Connection> connections = new ArrayList<>();

    // Generator losowy do wyboru wolnej pozycji przy dodawaniu nowego węzła
    private static final Random RNG = new Random();

    // Wątek okresowo sprawdzający, czy połączone przyciski nadal istnieją na panelu
    private static ScheduledExecutorService connectionWatcher;

    //----------------------------------------------------------------------------------
    // Definicja typów systemów jako stałe całkowite, ułatwiające identyfikację
    public static final int ruter_t         = 0;
    public static final int swithe_t        = 1;
    public static final int linux_t         = 2;
    public static final int windows_t       = 3;
    public static final int linux_server_t  = 4;
    public static final int windos_server_t = 5;

    // Stan aplikacji określający, czy można otworzyć nowe okno szczegółów
    // -1: brak aktywnego okna, każda inna wartość: typ systemu otwartego okna
    private static Integer app_stet = -1;

    // Indeks wybranego przycisku w liście sys, wykorzystywany przez kontroler szczegółów
    private static Integer index_b = -1;


    private static Boolean is_title = false;
    private static String title_e;
    public static  void set_title(String t){
        if (!Topology.is_title){
            Topology.is_title = true;
            Topology.title_e = t;
        }else {
            Topology.title_e = t;
        }
    }
    public static void del_title(){
        Topology.is_title = false;
    }

    public static Boolean getIs_title(){
        return Topology.is_title;
    }

    public static String getTitle_e(){
        return Topology.title_e;
    }
    //----------------------------------------------------------------------------------
    // Metody pomocnicze do zarządzania stanami aplikacji

    /**
     * Ustawia stan aplikacji, blokując otwieranie nowych okien gdy wartość != -1
     */
    public static void setApp_stet(Integer x) { app_stet = x; }

    /**
     * Pobiera aktualny stan aplikacji
     */
    public static Integer App_state_get() { return app_stet; }

    /**
     * Sprawdza, czy można otworzyć nowe okno (czy nie jest aktywny inny dialog)
     */
    public static boolean App_new_window_q() { return app_stet == -1; }

    /**
     * Przekazuje referencję do głównego panelu Topology
     */
    public static void setPanelToTopology(Pane panel) { workPanel = panel; }

    /**
     * Zwraca liczbę aktualnie dodanych systemów (przycisków)
     */
    public static int getSystemCount() { return sys.size(); }

    /**
     * Zwraca indeks ostatnio wybranego przycisku (do otwarcia panelu szczegółów)
     */
    public static int getindex_b() { return index_b; }

    //geter dla przycisku po indeksie
    public static Button get_b(int index){
        return Topology.sys.get(index);
    }

    //----------------------------------------------------------------------------------
    /**
     * Dodaje nowy system do panelu:
     * 1. Rejestruje jego pozycję i typ w liście points
     * 2. Tworzy przycisk z grafiką, pozycjonuje go
     * 3. Obsługuje pojedyncze kliknięcie - otwieranie okna szczegółów
     * 4. Wspiera przeciąganie przycisku (drag & drop) i aktualizację współrzędnych
     * 5. Dodaje przycisk do hierarchii panelem i listy sys
     *
     * @param x     współrzędna X w panelu
     * @param y     współrzędna Y w panelu
     * @param type  kod typu systemu, odpowiadający stałym ruter_t, swithe_t, itp.
     * @param img   grafika wyświetlana na przycisku (np. ikona systemu)
     * @param logo  logo lub dodatkowa grafika przekazywana do kontrolera okna szczegółów
     */
    public static void addsystem(int x, int y, int type, ImageView img, ImageView logo) {
        // Dodajemy informacje do listy punktów (model danych)
        points.add(new Point(x, y, type));

        // Tworzymy przycisk reprezentujący system
        Button button = new Button();
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setGraphic(img);

        // Obsługa kliknięcia myszką - otwarcie okna szczegółów systemu
        button.setOnMouseClicked(event -> {
            // Upewniamy się, że to było zwykłe kliknięcie (nie drag) i że nie ma otwartego okna
            if (event.isStillSincePress() && App_new_window_q()) {
                // Blokujemy dalsze otwieranie okien
                setApp_stet(type);
                // Zapamiętujemy indeks aktualnego przycisku
                index_b = getSystemCount() - 1;
                try {
                    // Ładowanie widoku FXML i przypisanie kontrolera z przekazaniem przycisku i logo
                    FXMLLoader loader = new FXMLLoader(Topology.class.getResource("/secendary_ui.fxml"));
                    SecendaryUi ctrl = new SecendaryUi();
                    ctrl.point_to_button(button);
                    ctrl.set_logo(logo);
                    loader.setController(ctrl);
                    Parent root = loader.load();

                    // Konfiguracja nowego Stage (okna)
                    Stage stage = new Stage();
                    // Ustawiamy czytelny tytuł okna na podstawie typu systemu
                    switch (type) {
                        case ruter_t         -> stage.setTitle("Router");
                        case swithe_t        -> stage.setTitle("Switch");
                        case linux_t         -> stage.setTitle("Linux Client");
                        case windows_t       -> stage.setTitle("Windows Client");
                        case linux_server_t  -> stage.setTitle("Linux Server");
                        case windos_server_t -> stage.setTitle("Windows Server");
                        default              -> stage.setTitle("Unknown Device");
                    }
                    // Ładujemy styl CSS i pokazujemy okno
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(Objects.requireNonNull(
                            Topology.class.getResource("/style.css")).toExternalForm());
                    stage.setScene(scene);
                    stage.setResizable(true);
                    // Gdy użytkownik zamknie okno, odblokujemy możliwość otwierania kolejnych
                    stage.setOnCloseRequest(e -> setApp_stet(-1));
                    stage.show();
                    stage.setWidth(500);
                    stage.setHeight(500);
                } catch (Exception ex) {
                    // Jeśli wystąpi błąd przy ładowaniu pliku FXML lub kontrolera, przerywamy i logujemy wyjątek
                    throw new RuntimeException("Błąd podczas otwierania okna szczegółów: ", ex);
                }
            }
        });

        //------------------------------------------------------------------------------
        // Drag & Drop: zachowanie przeciągania przycisku po panelu
        final double[] offsetX = new double[1];
        final double[] offsetY = new double[1];
        final boolean[] dragged = {false};

        // MOUSE_PRESSED: obliczamy offset kursora względem lewego górnego rogu przycisku
        button.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (!App_new_window_q()) return;
            offsetX[0] = e.getSceneX() - button.getLayoutX();
            offsetY[0] = e.getSceneY() - button.getLayoutY();
            dragged[0] = false;
        });

        // MOUSE_DRAGGED: aktualizujemy pozycję przycisku w czasie rzeczywistym
        button.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (!App_new_window_q()) return;
            button.setLayoutX(e.getSceneX() - offsetX[0]);
            button.setLayoutY(e.getSceneY() - offsetY[0]);
            dragged[0] = true; // flaga, że było faktyczne przesunięcie
        });

        // MOUSE_RELEASED: zapiszemy nowe współrzędne w modelu Point, jeśli nastąpił drag
        button.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (!App_new_window_q()) return;
            if (dragged[0]) {
                int idx = sys.indexOf(button);
                if (idx >= 0) {
                    points.get(idx).setX((int) button.getLayoutX());
                    points.get(idx).setY((int) button.getLayoutY());
                }
            }
        });

        // Dodaj przycisk do hierarchii widoku i listy zarządzanych systemów
        workPanel.getChildren().add(button);
        sys.add(button);
    }

    //----------------------------------------------------------------------------------
    /**
     * Usuwa system (przycisk) z panelu i z listy sys.
     * Wszystkie połączenia do tego przycisku zostaną później usunięte przez watcher.
     */
    public static void del_system(Button b) {
        final int index = Topology.sys.indexOf(b);
        Topology.sys.remove(b);
        Topology.points.remove(index);
        workPanel.getChildren().remove(b);
    }


    /**
     * Zwraca obiekt Point opisujący współrzędne i typ danego systemu
     * @param i indeks w liście points
     * @return Point lub null, jeżeli indeks jest poza zakresem
     */
    public static Point getPoint(int i) {
        return (i >= 0 && i < points.size()) ? points.get(i) : null;
    }

    /**
     * Aktualizuje pozycję systemu w modelu danych.
     * @param x nowe X
     * @param y nowe Y
     * @param i indeks systemu
     * @return true, jeżeli aktualizacja powiodła się
     */
    public static boolean setSystem(int x, int y, int i) {
        if (i >= 0 && i < points.size()) {
            points.get(i).setX(x);
            points.get(i).setY(y);
            return true;
        }
        return false;
    }

    //----------------------------------------------------------------------------------
    /**
     * Znajduje wolne pozycje w panelu, by uniknąć nakładania się przycisków.
     * Przeszukuje panel w krokach co 25px i sprawdza kolizje z istniejącymi Point.
     * @return losowa para [x,y] lub null, jeżeli brak miejsca
     */
    public static List<Integer> findFreeCoordinates() {
        final int SIZE = 150;       // założony rozmiar widoku przycisku
        final int STEP = 25;
        double w = workPanel.getWidth();
        double h = workPanel.getHeight();
        List<List<Integer>> free = new ArrayList<>();

        // Przeszukiwanie całego obszaru panelu
        for (int yy = 0; yy <= h - SIZE; yy += STEP) {
            for (int xx = 0; xx <= w - SIZE; xx += STEP) {
                boolean overlaps = false;
                for (Point p : points) {
                    // Sprawdzenie przecięcia prostokątów
                    if (xx < p.getX() + SIZE && xx + SIZE > p.getX() &&
                            yy < p.getY() + SIZE && yy + SIZE > p.getY()) {
                        overlaps = true;
                        break;
                    }
                }
                if (!overlaps) {
                    free.add(List.of(xx, yy));
                }
            }
        }
        return free.isEmpty() ? null : free.get(RNG.nextInt(free.size()));
    }

    //----------------------------------------------------------------------------------
    /**
     * Wewnętrzna klasa Connection przechowująca połączenia między przyciskami.
     * Zawiera referencje do dwóch przycisków oraz linii je łączącej.
     */
    public static class Connection {
        Button b1, b2;
        Line link;
        Connection(Button b1, Button b2, Line link) {
            this.b1 = b1;
            this.b2 = b2;
            this.link = link;
        }
    }

    /**
     * Rejestruje obiekty ToggleButton, aby można było je resetować po operacjach
     */
    public static void registerToggles(ToggleButton connect,
                                       ToggleButton disconnect) {
        connectToggle    = connect;
        disconnectToggle = disconnect;
    }

    //----------------------------------------------------------------------------------
    /**
     * Uruchamia tryb łączenia węzłów. Użytkownik klika dwa przyciski,
     * po czym zostaje narysowana wiązana linia między ich środkami.
     */
    public static void connectTwoButtons() {
        // 1) Ustawiamy specjalny stan, aby zablokować otwieranie okien
        setApp_stet(999);
        // 2) Usunięcie handlera rozłączania, dodanie handlera łączenia
        sys.forEach(b -> b.removeEventHandler(MouseEvent.MOUSE_PRESSED, disconnectHandler));
        sys.forEach(b -> b.addEventHandler(MouseEvent.MOUSE_PRESSED, connectHandler));
    }

    /**
     * Uruchamia tryb rozłączania węzłów. Po kliknięciu dwóch przycisków
     * zostanie usunięte istniejące połączenie między nimi.
     */
    public static void removeConnectionBetweenButtons() {
        setApp_stet(998);
        sys.forEach(b -> b.removeEventHandler(MouseEvent.MOUSE_PRESSED, connectHandler));
        sys.forEach(b -> b.addEventHandler(MouseEvent.MOUSE_PRESSED, disconnectHandler));
    }

    //----------------------------------------------------------------------------------
    /**
     * Handler odpowiedzialny za rysowanie linii między dwoma wybranymi przyciskami.
     * Zaznacza kliknięte elementy niebieską obramówką, a po wybraniu pary generuje Line
     */
    private static class ConnectHandler implements EventHandler<MouseEvent> {
        private final List<Button> sel = new ArrayList<>();  // lista wybranych przycisków

        @Override
        public void handle(MouseEvent e) {
            Button b = (Button) e.getSource();
            // Dodaj do zaznaczonych, nie powtarzaj
            if (!sel.contains(b)) {
                sel.add(b);
                b.setStyle("-fx-border-color: blue; -fx-border-width: 2px;");
            }
            // Gdy wybrano dwie pozycje - generujemy połączenie
            if (sel.size() == 2) {
                Button b1 = sel.get(0);
                Button b2 = sel.get(1);
                Line line = new Line();
                // Wiązania startu i końca linii do środka przycisków
                line.startXProperty().bind(b1.layoutXProperty().add(b1.widthProperty().divide(2)));
                line.startYProperty().bind(b1.layoutYProperty().add(b1.heightProperty().divide(2)));
                line.endXProperty().bind(b2.layoutXProperty().add(b2.widthProperty().divide(2)));
                line.endYProperty().bind(b2.layoutYProperty().add(b2.heightProperty().divide(2)));
                // Dodaj linię jako tło (index 0)
                workPanel.getChildren().add(0, line);
                connections.add(new Connection(b1, b2, line));

                // Krótkie opóźnienie przed przywróceniem normalnego trybu
                PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                pause.setOnFinished(ev -> setApp_stet(-1));
                pause.play();

                cleanup();
            }
        }
        // Porządkujemy po operacji: usuwamy handler, stylizację, czyścimy listę i toggle
        private void cleanup() {
            sys.forEach(btn -> btn.removeEventHandler(MouseEvent.MOUSE_PRESSED, this));
            sys.forEach(btn -> btn.setStyle(""));
            sel.clear();
            if (connectToggle != null) connectToggle.setSelected(false);
        }
    }

    //----------------------------------------------------------------------------------
    /**
     * Handler odpowiedzialny za usuwanie połączenia między dwoma przyciskami.
     * Zaznacza elementy czerwoną obwódką, a następnie usuwa linię i wpis w connections.
     */
    private static class DisconnectHandler implements EventHandler<MouseEvent> {
        private final List<Button> sel = new ArrayList<>();

        @Override
        public void handle(MouseEvent e) {
            Button b = (Button) e.getSource();
            if (!sel.contains(b)) {
                sel.add(b);
                b.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            }
            if (sel.size() == 2) {
                Button b1 = sel.get(0);
                Button b2 = sel.get(1);
                // Znajdź odpowiednie połączenie (kolejność nie ma znaczenia)
                Connection toRemove = connections.stream()
                        .filter(c -> (c.b1 == b1 && c.b2 == b2) || (c.b1 == b2 && c.b2 == b1))
                        .findFirst().orElse(null);
                if (toRemove != null) {
                    workPanel.getChildren().remove(toRemove.link);
                    connections.remove(toRemove);
                }

                // Analogiczny mechanizm opóźnienia i przywrócenia stanu
                PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                pause.setOnFinished(ev -> setApp_stet(-1));
                pause.play();

                cleanup();
            }
        }
        private void cleanup() {
            sys.forEach(btn -> btn.removeEventHandler(MouseEvent.MOUSE_PRESSED, this));
            sys.forEach(btn -> btn.setStyle(""));
            sel.clear();
            if (disconnectToggle != null) disconnectToggle.setSelected(false);
        }
    }

    //----------------------------------------------------------------------------------
    /**
     * Anuluje wszelkie tryby specjalne (łączenia/rozłączania). Usuwa handlery
     * oraz style, resetuje stan aplikacji i wyłącza zaznaczone toggles.
     */
    public static void cancelAllModes() {
        sys.forEach(b -> {
            b.removeEventHandler(MouseEvent.MOUSE_PRESSED, connectHandler);
            b.removeEventHandler(MouseEvent.MOUSE_PRESSED, disconnectHandler);
            b.setStyle("");
        });
        if (connectToggle    != null) connectToggle.setSelected(false);
        if (disconnectToggle != null) disconnectToggle.setSelected(false);
        setApp_stet(-1);
    }

    /**
     * Uruchamia cykliczny watcher w osobnym wątku, który co sekundę sprawdza,
     * czy przyciski z connections nadal istnieją na panelu.
     * Usuwa łącza do usuniętych węzłów.
     */
    public static void startConnectionWatcher() {
        connectionWatcher = Executors.newSingleThreadScheduledExecutor();
        connectionWatcher.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                Iterator<Connection> it = connections.iterator();
                while (it.hasNext()) {
                    Connection conn = it.next();
                    boolean b1Present = workPanel.getChildren().contains(conn.b1);
                    boolean b2Present = workPanel.getChildren().contains(conn.b2);
                    if (!b1Present || !b2Present) {
                        workPanel.getChildren().remove(conn.link);
                        it.remove();
                    }
                }
            });
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Zatrzymuje wątek watchera – wywoływane przy zamykaniu aplikacji,
     * aby nie zostawić wiszących wątków.
     */
    public static void stop() {
        if (connectionWatcher != null && !connectionWatcher.isShutdown()) {
            connectionWatcher.shutdownNow();
        }
    }

    /**
     * Funkcja save jest odpowiedzialna za zapisanie aktualnego stanu aplikacji do pliku (z path) przyjmując format:
     *  - ID: Losowy int z przedziału 1 - 1000
     *  - Lista połączeń z innymi ID
     *  - X i Y
     *  - Typ
     */
    public static void save(String path) {
        int cunt_b = Topology.points.size();
        int cunt_c = Topology.connections.size();

        // --- Generowanie unikalnych ID (1–1000) ---
        List<Integer> allIds = IntStream.rangeClosed(1, 1000)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(allIds, RNG);
        List<Integer> ids = allIds.subList(0, cunt_b);

        // --- Przygotowanie list do zapisu ---
        List<Map<String, Integer>> x_and_y = new ArrayList<>();
        List<Integer> typs_l              = new ArrayList<>();

        for (int i = 0; i < cunt_b; i++) {
            x_and_y.add(Map.of(
                    "x", Topology.points.get(i).getX(),
                    "y", Topology.points.get(i).getY()
            ));
            typs_l.add(Topology.points.get(i).getType());
        }

        // --- Budowanie połączeń ---
        List<List<Integer>> conects = new ArrayList<>();
        for (int i = 0; i < cunt_b; i++) {
            List<Integer> cc = new ArrayList<>();
            for (int j = 0; j < cunt_c; j++) {
                Button b  = Topology.sys.get(i);
                Button b1 = Topology.connections.get(j).b1;
                Button b2 = Topology.connections.get(j).b2;

                if (b == b1 && !cc.contains(ids.get(Topology.sys.indexOf(b2)))) {
                    cc.add(ids.get(Topology.sys.indexOf(b2)));
                }
                else if (b == b2 && !cc.contains(ids.get(Topology.sys.indexOf(b1)))) {
                    cc.add(ids.get(Topology.sys.indexOf(b1)));
                }
            }
            conects.add(cc);
        }

        // --- Tworzenie danych TOML ---
        StringBuilder tomlBuilder = new StringBuilder();
        for (int i = 0; i < cunt_b; i++) {
            tomlBuilder.append("[node]\n");
            tomlBuilder.append("id = ").append(ids.get(i)).append("\n");
            tomlBuilder.append("x = ").append(x_and_y.get(i).get("x")).append("\n");
            tomlBuilder.append("y = ").append(x_and_y.get(i).get("y")).append("\n");
            tomlBuilder.append("type = ").append(typs_l.get(i)).append("\n");
            tomlBuilder.append("connections = [");
            List<Integer> conns = conects.get(i);
            for (int j = 0; j < conns.size(); j++) {
                tomlBuilder.append(conns.get(j));
                if (j < conns.size() - 1) {
                    tomlBuilder.append(", ");
                }
            }
            tomlBuilder.append("]\n\n");
        }

        // --- Zapis do pliku ---
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Paths.get(path)))) {
            out.print(tomlBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Czyści cały panel*/
    public static void clean_all_b(){
        for (Button b : sys){
            Topology.workPanel.getChildren().remove(b);
        }
        sys.clear();
        points.clear();
    }
    /**
     * Wczytuje CFG z .toml*/
    public static Map<String, Object> loadTopologyData(String path) {
        Map<String, Object> data = new HashMap<>();
        List<Integer> ids = new ArrayList<>();
        List<Map<String, Integer>> x_and_y = new ArrayList<>();
        List<Integer> typs_l = new ArrayList<>();
        List<List<Integer>> conects = new ArrayList<>();

        try {
            String tomlContent = Files.readString(Path.of(path));
            String[] sections = tomlContent.split("(?m)(?=^\\[node\\])");

            for (String sec : sections) {
                sec = sec.trim();
                if (sec.isEmpty()) continue;

                // Parsujemy cały fragment
                TomlParseResult result = Toml.parse(sec);
                // Pobieramy pod‑tabelę [node]
                TomlTable node = result.getTable("node");
                if (node == null || !node.contains("id")) continue;

                // 1) Wyciągamy id, x, y, type
                int id   = Objects.requireNonNull(node.getLong("id")).intValue();
                int x    = Objects.requireNonNull(node.getLong("x")).intValue();
                int y    = Objects.requireNonNull(node.getLong("y")).intValue();
                int type = Objects.requireNonNull(node.getLong("type")).intValue();

                ids.add(id);
                x_and_y.add(Map.of("x", x, "y", y));
                typs_l.add(type);

                // 2) Parsujemy connections
                List<Integer> conn = new ArrayList<>();
                TomlArray arr = node.getArray("connections");
                if (arr != null) {
                    for (Object o : arr.toList()) {
                        conn.add(((Long) o).intValue());
                    }
                }
                conects.add(conn);
            }

        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas wczytywania TOML: " + e.getMessage(), e);
        }

        data.put("ids", ids);
        data.put("x_and_y", x_and_y);
        data.put("typs_l", typs_l);
        data.put("conects", conects);
        return data;
    }






}

