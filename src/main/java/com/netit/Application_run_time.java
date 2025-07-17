package com.netit;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * Application_run_time: główny kontroler aplikacji JavaFX.
 * Odpowiada za inicjalizację UI, ładowanie grafik, obsługę przycisków
 * dodających węzły do topologii oraz trybów łączenia/rozłączania.
 */
public class Application_run_time {
    //----------------------------------------------------------------------------------
    // Pole przechowujące wykryty system operacyjny:
    // 0 = Windows, 1 = Linux, 2 = MacOS
    private int OS_run;

    //---------------------------------------------------------------------------------
    // Informacje o stage
    private Stage primary_stage;

    //----------------------------------------------------------------------------------
    // Stałe określające wymiary głównego okna aplikacji (szer. x, wys. y)
    private final int x_run = 900;
    private final int y_run = 1300;

    //----------------------------------------------------------------------------------
    // Teksty nagłówka i stopki wyświetlane w UI:
    private final String hed_text  = "NETIT; It is launch on: ";
    private final String autor     = "Authors: https://github.com/Taitlesonn";
    private final String full_name = "  full name: Network Emulation and Topology Implementation Tool";

    //----------------------------------------------------------------------------------
    // Opisy dla tooltipów opisujących każdy typ węzła
    private final String linux_t          = "Linux - a Unix-type operating system, often used on servers and in the cloud.";
    private final String windows_t        = "Windows - a popular Microsoft operating system, widely used on personal computers.";
    private final String ruter_t          = "Router - a network device used to forward packets between networks.";
    private final String switch_t         = "Switch - a network device that connects multiple devices within a local area network (LAN).";
    private final String windows_server_t = "Windows Server - a Windows version designed for server use.";
    private final String linux_server_t   = "Linux Server - a server operating system based on the Linux kernel, popular in IT environments.";

    //----------------------------------------------------------------------------------
    // Obiekty ImageView przechowujące wczytane ikony dla wszystkich typów systemów
    private ImageView ruter_img;
    private ImageView switch_img;
    private ImageView linux_img;
    private ImageView windows_img;
    private ImageView windows_server_img;
    private ImageView linux_server_img;
    // Dodatkowe ikony przekazywane do kontrolera Topology
    private ImageView linux_gi;
    private ImageView windows_gi;
    private ImageView net_gi;
    private ImageView server_gi;

    //----------------------------------------------------------------------------------
    // Wymiary miniaturowych ikon w przyciskach dodawania węzłów
    private final Integer x_box = 75;
    private final Integer y_box = 75;

    //----------------------------------------------------------------------------------
    // Adnotacje FXML: komponenty zdefiniowane w pliku aplikacji (secendary_ui.fxml)
    @FXML private ToggleButton connecter;        // Przełącznik trybu łączenia w Topology
    @FXML private ToggleButton disconecter;      // Przełącznik trybu rozłączania
    @FXML private HBox HBox_heder;               // Kontener nagłówka
    @FXML private HBox HBox_footer;              // Kontener stopki
    @FXML private Label heder_inf;               // Etykieta wyświetlająca tekst nagłówka
    @FXML private Label footer_inf;              // Etykieta wyświetlająca tekst stopki
    @FXML private Button ruter_b;                // Przycisk dodania routera
    @FXML private Button switch_b;               // Przycisk dodania switcha
    @FXML private Button windows_server_b;       // Przycisk dodania Windows Server
    @FXML private Button linux_server_b;         // Przycisk dodania Linux Server
    @FXML private Button windows_b;              // Przycisk dodania Windows
    @FXML private Button linux_b;                // Przycisk dodania Linux
    @FXML private Pane workspace;                // Główny panel roboczy, przekazywany do Topology
    @FXML private Accordion accordion_left;      // Panel boczny UI (niewykorzystany do nodów)
    @FXML private Button save;                   // Panel boczny w sekcji funkcje. Zapisuje aktualny stan aplikacji do pliku .tml
    @FXML private Button load;                   // Przycisk od ładnowani z fxml
    @FXML private Button clean;                  // Usuwa wszystkie elementy z topologi
    @FXML private Button edytor;                 // Otwiera edytor
    @FXML private Button export_html;            // Exportuje notatki

    //----------------------------------------------------------------------------------
    /**
     * Konstruktor: wywoływany przed initialize(); wykrywa i zapisuje aktualny OS.
     */
    public Application_run_time()                     { this.os_gues(); }
    public void setPrimary_stage(Stage primaryStage)  { this.primary_stage = primaryStage; }


    //----------------------------------------------------------------------------------
    // Gettery dla dodatkowych ikon przekazywanych do Topology oraz wymiarów i tekstów
    public ImageView getServer_gi()     { return server_gi; }
    public ImageView getLinux_gi()      { return linux_gi; }
    public ImageView getWindows_gi()    { return windows_gi; }
    public ImageView getNet_gi()        { return net_gi; }

    public int      getX_run()          { return x_run; }
    public int      getY_run()          { return y_run; }
    public String   getAutor()          { return autor; }
    public String   getFull_name()      { return full_name; }
    public ImageView getRuter_img()     { return ruter_img; }
    public ImageView getLinux_img()     { return linux_img; }
    public ImageView getSwitch_img()    { return switch_img; }
    public ImageView getWindows_img()   { return windows_img; }
    public ImageView getWindows_server_img() { return windows_server_img; }
    public ImageView getLinux_server_img()   { return linux_server_img; }

    //----------------------------------------------------------------------------------
    /**
     * Wykrywa system operacyjny na podstawie właściwości systemowej "os.name".
     * Ustawia pole OS_run na odpowiadającą wartość.
     */
    private void os_gues() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            this.OS_run = 0;
        } else if (os.contains("nix") || os.contains("nux") || os.contains("linux") || os.contains("aix")) {
            this.OS_run = 1;
        } else if (os.contains("mac")) {
            this.OS_run = 2;
        }
    }

    //----------------------------------------------------------------------------------
    /**
     * Zwraca tekstową nazwę systemu na podstawie wartości OS_run.
     * @return "Windows", "Linux", "MacOS" lub komunikat o braku wsparcia
     */
    public String get_os() {
        return switch (this.OS_run) {
            case 0 -> "Windows";
            case 1 -> "Linux";
            case 2 -> "MacOS";
            default -> "Error Not Supported Platform";
        };
    }

    //----------------------------------------------------------------------------------
    /**
     * Ustawia tooltip wyświetlany po prawej stronie przycisku.
     * Tooltip pojawia się przy zdarzeniu onShowing i jest przesunięty o kilka pikseli.
     * @param button przycisk, do którego przypisujemy tooltip
     * @param text   tekst wyświetlany w tooltipie
     */
    private void setTooltipRight(Button button, String text) {
        Tooltip tooltip = new Tooltip(text);
        button.setTooltip(tooltip);
        tooltip.setOnShowing(event -> {
            double x = button.localToScreen(button.getBoundsInLocal()).getMaxX();
            double y = button.localToScreen(button.getBoundsInLocal()).getMinY();
            tooltip.setX(x + 5);
            tooltip.setY(y);
        });
    }

    //----------------------------------------------------------------------------------
    /**
     * Metoda wywoływana przez FXML po załadowaniu kontrolera.
     * Inicjalizuje obrazy, tooltipy, podłącza workspace do Topology
     * oraz uruchamia watcher połączeń.
     */
    @FXML
    private void initialize() {
        //--- 1) Ustawienie tekstu nagłówka i stopki
        heder_inf.setText(hed_text + get_os());
        footer_inf.setText(getAutor() + getFull_name());

        //--- 2) Wczytywanie ikon z zasobów
        ruter_img          = createImageView("files/Ruter.png");
        switch_img         = createImageView("files/switch.png");
        linux_img          = createImageView("files/Linux.png");
        windows_img        = createImageView("files/Windows.png");
        windows_server_img = createImageView("files/Windows-Server.png");
        linux_server_img   = createImageView("files/Linux-Server.png");

        // Ikony pomocnicze do Topology
        server_gi   = createImageView("files/ser_g.png");
        net_gi      = createImageView("files/net_g.png");
        windows_gi  = createImageView("files/win_g.png");
        linux_gi    = createImageView("files/lin_g.png");

        //--- 3) Ustawienie rozmiarów i proporcji dla głównych ikon
        List<ImageView> images = List.of(
                ruter_img, switch_img, linux_img,
                windows_img, windows_server_img, linux_server_img
        );
        for (ImageView iv : images) {
            iv.setFitWidth(75);
            iv.setFitHeight(75);
            iv.setPreserveRatio(true);
        }

        //--- 4) Przypisanie grafik do przycisków dodawania węzłów
        ruter_b.setGraphic(ruter_img);
        windows_b.setGraphic(windows_img);
        linux_b.setGraphic(linux_img);
        switch_b.setGraphic(switch_img);
        linux_server_b.setGraphic(linux_server_img);
        windows_server_b.setGraphic(windows_server_img);

        //--- 5) Ustawienie tooltipów
        setTooltipRight(linux_b, linux_t);
        setTooltipRight(windows_b, windows_t);
        setTooltipRight(ruter_b, ruter_t);
        setTooltipRight(switch_b, switch_t);
        setTooltipRight(windows_server_b, windows_server_t);
        setTooltipRight(linux_server_b, linux_server_t);

        //--- 6) Rozciągnięcie przycisków funkcji
        disconecter.setMaxWidth(Double.MAX_VALUE);
        connecter.setMaxWidth(Double.MAX_VALUE);
        save.setMaxWidth(Double.MAX_VALUE);
        load.setMaxWidth(Double.MAX_VALUE);
        clean.setMaxWidth(Double.MAX_VALUE);
        edytor.setMaxWidth(Double.MAX_VALUE);

        //--- 7) Przekazanie panelu do Topology
        Topology.setPanelToTopology(workspace);

        //--- 8) Rejestracja ToggleButtonów
        Topology.registerToggles(connecter, disconecter);


        //--- 9) Uruchomienie watcher’a połączeń
        Topology.startConnectionWatcher();
    }



    /**
     * Utility: tworzy ImageView z zasobu w /src/main/resources
     */
    private ImageView createImageView(String resourcePath) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalStateException("Nie znaleziono zasobu: " + resourcePath);
        }
        ImageView iv = new ImageView(new Image(is));
        return iv;
    }


    //----------------------------------------------------------------------------------
    /**
     * Dodaje router do panelu Topology po naciśnięciu przycisku.
     * Sprawdza najpierw czy w danej chwili nie otwarte jest żadne okno dialogowe.
     */
    @FXML
    private void add_router() {
        if (Topology.App_new_window_q()) {
            List<Integer> coords = Topology.findFreeCoordinates();
            assert coords != null; // założenie: jest miejsce
            ImageView copy = new ImageView(ruter_img.getImage());
            copy.setFitWidth(x_box);
            copy.setFitHeight(y_box);
            copy.setPreserveRatio(true);
            Topology.addsystem(coords.get(0), coords.get(1), Topology.ruter_t, copy, net_gi);
        }
    }

    // Analogicznie: metody dodające switch, Linux, Windows, serwery
    @FXML private void add_switch()
    { if (Topology.App_new_window_q()) {
        List<Integer> coords = Topology.findFreeCoordinates();
        assert coords!=null;
        ImageView copy = new ImageView(switch_img.getImage());
        copy.setFitWidth(x_box);
        copy.setFitHeight(y_box);
        copy.setPreserveRatio(true);
        Topology.addsystem(coords.get(0),coords.get(1),Topology.swithe_t,copy, net_gi);}
    }
    @FXML private void add_linux()
    {
        if (Topology.App_new_window_q())
        {
            List<Integer> coords = Topology.findFreeCoordinates();
            assert coords!=null;
            ImageView copy = new ImageView(linux_img.getImage());
            copy.setFitWidth(x_box);
            copy.setFitWidth(x_box);
            copy.setFitHeight(y_box);
            copy.setPreserveRatio(true);
            Topology.addsystem(coords.get(0),coords.get(1),Topology.linux_t, copy,linux_gi);}
    }
    @FXML private void add_windows()        {
        if (Topology.App_new_window_q())
        {
            List<Integer> coords = Topology.findFreeCoordinates();
            assert coords!=null;
            ImageView copy = new ImageView(windows_img.getImage());
            copy.setFitWidth(x_box);
            copy.setFitWidth(x_box);
            copy.setFitHeight(y_box);
            copy.setPreserveRatio(true);
            Topology.addsystem(coords.get(0),coords.get(1),Topology.windows_t, copy,windows_gi);
        }
    }
    @FXML private void add_linux_server()
    {
        if (Topology.App_new_window_q()) {
            List<Integer> coords = Topology.findFreeCoordinates();
            assert coords!=null;
            ImageView copy = new ImageView(linux_server_img.getImage());
            copy.setFitWidth(x_box);
            copy.setFitWidth(x_box);
            copy.setFitHeight(y_box);
            copy.setPreserveRatio(true);
            Topology.addsystem(coords.get(0),coords.get(1),Topology.linux_server_t, copy,server_gi);
        }
    }
    @FXML private void add_windows_server() {
        if (Topology.App_new_window_q())
        {
            List<Integer> coords = Topology.findFreeCoordinates();
            assert coords!=null;
            ImageView copy = new ImageView(windows_server_img.getImage());
            copy.setFitWidth(x_box);
            copy.setFitWidth(x_box);
            copy.setFitHeight(y_box);
            copy.setPreserveRatio(true);
            Topology.addsystem(coords.get(0),coords.get(1),Topology.windos_server_t,copy,server_gi);
        }
    }

    //----------------------------------------------------------------------------------
    /**
     * Wywoływane przy zmianie stanu przycisku connecter.
     * Steruje włączaniem/wyłączaniem trybu łączenia Topology.
     */
    @FXML
    private void connecter_f() {
        if (connecter.isSelected()) {
            // upewnij się, że nie jest aktywny tryb rozłączania
            if (disconecter.isSelected()) {
                disconecter.setSelected(false);
                Topology.cancelAllModes();
            }
            // włącz tryb łączenia, jeśli jest co najmniej 2 węzły
            if (Topology.getSystemCount() >= 2) {
                Topology.connectTwoButtons();
            } else {
                connecter.setSelected(false);
            }
        } else {
            // wyłącz tryb łączenia
            Topology.cancelAllModes();
        }
    }

    //----------------------------------------------------------------------------------
    /**
     * Wywoływane przy zmianie stanu przycisku disconecter.
     * Steruje włączaniem/wyłączaniem trybu rozłączania Topology.
     */
    @FXML
    private void connecter_d() {
        // ... analogiczna logika do connecter_f(), ale dla usuwania połączeń ...
        if (connecter.isSelected()) { connecter.setSelected(false); Topology.cancelAllModes(); }
        if (disconecter.isSelected()) {
            if (Topology.getSystemCount() >= 2) {
                Topology.removeConnectionBetweenButtons();
            } else {
                disconecter.setSelected(false);
            }
        } else {
            Topology.cancelAllModes();
        }
    }

    @FXML
    private void save_to_file(){
        if (Topology.App_new_window_q()){
            Topology.setApp_stet(999);
            FileChooser fileChooser = new FileChooser();

            //Domyślna nazwa pliku
            fileChooser.setInitialFileName("topologia.toml");

            //Filtry
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Toml files (*.toml)", "*.toml");
            fileChooser.getExtensionFilters().add(extensionFilter);

            File file = fileChooser.showSaveDialog(this.primary_stage);
            if (file != null){
                Topology.save(file.getAbsolutePath());
            }
            Topology.setApp_stet(-1);
        }
    }

    @FXML
    private void open_from_file() {
        // tylko jedno okno
        if (!Topology.App_new_window_q()) {
            return;
        }
        Topology.setApp_stet(999);


        // wybór pliku
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("TOML files (*.toml)", "*.toml")
        );
        File file = fileChooser.showOpenDialog(this.primary_stage);
        if (file == null) {
            Topology.setApp_stet(-1);
            return;
        }

        // wczytaj dane z pliku
        Map<String, Object> topologyInf;
        try {
            topologyInf = Topology.loadTopologyData(file.getAbsolutePath());
            Topology.clean_all_b();
        } catch (Exception e) {
            Topology.setApp_stet(-1);
            return;
        }
        if (topologyInf.isEmpty()) {
            Topology.setApp_stet(-1);
            return;
        }

        @SuppressWarnings("unchecked")
        List<Integer> ids             = (List<Integer>) topologyInf.get("ids");
        @SuppressWarnings("unchecked")
        List<Map<String,Integer>> coords = (List<Map<String,Integer>>) topologyInf.get("x_and_y");
        @SuppressWarnings("unchecked")
        List<Integer> types           = (List<Integer>) topologyInf.get("typs_l");
        @SuppressWarnings("unchecked")
        List<List<Integer>> conns     = (List<List<Integer>>) topologyInf.get("conects");

        // walidacja
        if (ids == null || coords == null || types == null || conns == null
                || ids.size() != coords.size() || ids.size() != types.size()) {
            Topology.setApp_stet(-1);
            return;
        }

        // helper do tworzenia nowych ImageView (kopii oryginałów)
        Function<Image, ImageView> copyView = src -> {
            ImageView iv = new ImageView(src);
            iv.setFitWidth(x_box);
            iv.setFitHeight(y_box);
            iv.setPreserveRatio(true);
            return iv;
        };

        // funkcja pomocnicza do umieszczania węzła
        BiConsumer<Integer,Integer> placeNode = (i, typeLocal) -> {
            Map<String,Integer> c = coords.get(i);
            if (c == null) return;
            int x = c.getOrDefault("x", 0);
            int y = c.getOrDefault("y", 0);

            ImageView view   = null;
            ImageView target = null;
            switch (typeLocal) {
                case Topology.ruter_t:
                    view   = copyView.apply(ruter_img.getImage());
                    target = getNet_gi();
                    break;
                case Topology.swithe_t:
                    view   = copyView.apply(switch_img.getImage());
                    target = getNet_gi();
                    break;
                case Topology.linux_t:
                    view   = copyView.apply(linux_img.getImage());
                    target = getLinux_gi();
                    break;
                case Topology.windows_t:
                    view   = copyView.apply(windows_img.getImage());
                    target = getWindows_gi();
                    break;
                case Topology.linux_server_t:
                    view   = copyView.apply(linux_server_img.getImage());
                    target = getServer_gi();
                    break;
                case Topology.windos_server_t:
                    view   = copyView.apply(windows_server_img.getImage());
                    target = getServer_gi();
                    break;
                default:
                    return;
            }

            // dodaj system: oryginał ImageView + kopia
            Topology.addsystem(x, y, typeLocal, view, target);
        };

        // dodaj wszystkie węzły
        for (int i = 0; i < ids.size(); i++) {
            placeNode.accept(i, types.get(i));
        }

        // połączenia
        for (int i = 0; i < ids.size(); i++) {
            Button b1 = Topology.get_b(i);
            if (b1 == null) continue;
            for (int j = i + 1; j < conns.size(); j++) {
                if (conns.get(j) != null && conns.get(j).contains(ids.get(i))) {
                    Button b2 = Topology.get_b(j);
                    if (b2 == null) continue;

                    boolean exists = Topology.connections.stream().anyMatch(c ->
                            (c.b1 == b1 && c.b2 == b2) ||
                                    (c.b1 == b2 && c.b2 == b1)
                    );
                    if (!exists) {
                        Line link = new Line();
                        link.startXProperty().bind(b1.layoutXProperty().add(b1.widthProperty().divide(2)));
                        link.startYProperty().bind(b1.layoutYProperty().add(b1.heightProperty().divide(2)));
                        link.endXProperty().bind(b2.layoutXProperty().add(b2.widthProperty().divide(2)));
                        link.endYProperty().bind(b2.layoutYProperty().add(b2.heightProperty().divide(2)));
                        Topology.workPanel.getChildren().add(0, link);
                        Topology.connections.add(new Topology.Connection(b1, b2, link));
                    }
                }
            }
        }

        Topology.setApp_stet(-1);
    }

    @FXML
    private void open_edytor() throws IOException {

        //Zatrzymanie reszty
        Topology.setApp_stet(999);
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edytor.fxml"));
            Parent root = loader.load();
            Edytor ed = loader.getController();
            ed.set_stage(this.primary_stage);
            loader.setController(ed);


            //Konfiguracja okna
            Stage stage = new Stage();
            stage.setTitle("Edytor");
            // Ładujemy styl CSS i pokazujemy okno
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(
                            getClass().getResource("/style.css")).toExternalForm()
            );


            stage.setScene(scene);
            stage.setResizable(true);

            //Zamykanie okna
            stage.setOnCloseRequest(e -> {
                if (Topology.getIs_title() && ed.is_type()){
                    String s = ed.gen_text();
                    Path baseDir = Paths.get(System.getProperty("user.dir"), "out", "files", ed.GetTT(), Topology.getTitle_e(), "text.raw");
                    Path baseDir2 = Paths.get(System.getProperty("user.dir"), "out", "files", ed.GetTT(), Topology.getTitle_e());
                    try {
                        // Tworzy lub nadpisuje plik
                        Files.createDirectories(baseDir2);
                        Files.writeString(baseDir, s,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    Topology.del_title();
                    Topology.setApp_stet(-1);
            }else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("ERROR");
                    alert.setHeaderText("NO TITLE OR DEVICE");
                    alert.setContentText("Nie ustawiłeś tytułu ani urządzenia. Nie będzie zapisu do pliku.");

                    alert.showAndWait();
                }
            });
            stage.show();
            stage.setWidth(900);
            stage.setHeight(700);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    private void export_nots() {
        Topology.setApp_stet(999);

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Export");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        File selectedDir = directoryChooser.showDialog(this.primary_stage);
        if (selectedDir != null) {
            Path sourcePath = Paths.get(System.getProperty("user.dir"), "out");
            Path targetPath = selectedDir.toPath();

            try {
                FileUtils.copyDirectory(sourcePath.toString(), targetPath.toString());
            } catch (IOException e) {
                e.printStackTrace(); // lub pokaż dialog z błędem użytkownikowi
            }
        }
    }


    @FXML
    private void clean_all_f(){
        Topology.clean_all_b();
    }

}
