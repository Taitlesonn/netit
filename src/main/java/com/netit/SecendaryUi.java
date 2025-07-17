package com.netit;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * Kontroler ładowania dynamicznych plików HTML z katalogu resources/files/sys-cfg/X/
 * oraz zewnętrznych folderów w katalogu out/files/X/
 */
public class SecendaryUi {
    private Button but;
    private ImageView logo;
    private static FileSystem jarFileSystem = null;

    @FXML private WebView idex_info;
    @FXML private HBox button_list;

    @FXML
    private void initialize() throws URISyntaxException, IOException {
        int state = Topology.App_state_get();
        String resourceDir = "/files/sys-cfg/" + state + "/";
        URL dirURL = getClass().getResource(resourceDir);
        if (dirURL != null) {
            // 1) Lista wszystkich .html w resourceDir
            try (Stream<Path> paths = listHtmlFiles(dirURL, resourceDir)) {
                createButtonsForResources(paths, resourceDir);
            }
            // 4) Pokaż index.html na start z zasobów wewnętrznych
            showHtmlResource(resourceDir + "index.html");
        }

        // 5) Sprawdź zewnętrzny katalog out/files/<state>/
        scanExternalFoldersAndCreateButtons(state);

        // 6) Ustawienia proporcji po dodaniu do sceny
        button_list.sceneProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) setupBindings(newS);
        });
    }

    /** Tworzy przyciski dla strumienia plików HTML z danego katalogu */
    private void createButtonsForResources(Stream<Path> paths, String resourceDir) {
        // logo, del, info
        button_list.getChildren().add(logo);
        Button deleteBtn = new Button("del");
        deleteBtn.setOnAction(e -> {
            Topology.workPanel.getChildren().remove(but);
            Topology.del_system(but);
            but = null;
        });
        button_list.getChildren().add(deleteBtn);

        Button infoBtn = new Button("info");
        infoBtn.setOnAction(e -> showHtmlResource(resourceDir + "index.html"));
        button_list.getChildren().add(infoBtn);

        // Dla każdego pliku stwórz przycisk
        paths.forEach(p -> {
            String fileName = p.getFileName().toString();
            if (fileName.equals("index.html")) return;
            String name = fileName.substring(0, fileName.length() - 5);
            Button b = new Button(name);
            b.setOnAction(e -> showHtmlResource(resourceDir + fileName));
            button_list.getChildren().add(b);
        });
    }

    /**
     * Przeszukuje zasoby HTML wewnątrz JAR lub katalogu klasy
     */
    private Stream<Path> listHtmlFiles(URL dirURL, String resourceDir) throws IOException, URISyntaxException {
        URI uri = dirURL.toURI();
        Path dirPath;
        if (uri.getScheme().equals("jar")) {
            if (jarFileSystem == null || !jarFileSystem.isOpen()) {
                jarFileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            }
            dirPath = jarFileSystem.getPath(resourceDir);
        } else {
            dirPath = Paths.get(uri);
        }
        return Files.list(dirPath).filter(p -> p.getFileName().toString().endsWith(".html"));
    }

    /**
     * Przeszukuje katalog out/files/<state>/ pod kątem podfolderów,
     * tworzy przycisk dla każdego folderu i ładuje index.html z folderu po kliknięciu.
     */
    private void scanExternalFoldersAndCreateButtons(int state) {
        String n = "";
        switch (state){
            case Topology.ruter_t -> n = "ruter";
            case Topology.swithe_t -> n = "switch";
            case Topology.linux_t -> n = "linux";
            case Topology.windows_t -> n = "windows";
            case Topology.linux_server_t -> n = "linux_s";
            case Topology.windos_server_t -> n = "windows_s";
        }
        Path base = Paths.get(System.getProperty("user.dir"), "out", "files", n);
        if (!Files.isDirectory(base)) return;
        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(base, Files::isDirectory)) {
            for (Path dir : dirs) {
                String folderName = dir.getFileName().toString();
                Path index = dir.resolve("index.html");
                if (Files.exists(index)) {
                    // stwórz przycisk
                    Button extBtn = new Button(folderName);
                    extBtn.setOnAction(e -> idex_info.getEngine().load(index.toUri().toString()));
                    button_list.getChildren().add(extBtn);
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd przy skanowaniu zewnętrznych folderów: " + e.getMessage());
        }
    }

    private void showHtmlResource(String resourcePath) {
        URL url = getClass().getResource(resourcePath);
        if (url == null) {
            String err = "<html><body><h2>Nie znaleziono:</h2><code>" +
                    resourcePath + "</code></body></html>";
            idex_info.getEngine().loadContent(err);
        } else {
            idex_info.getEngine().load(url.toExternalForm());
        }
    }

    private void setupBindings(Scene scene) {
        double h = 100, wFrac = 0.15;
        logo.setFitHeight(h);
        logo.fitWidthProperty().bind(scene.widthProperty().multiply(wFrac));
        VBox.setVgrow(idex_info, Priority.ALWAYS);
        button_list.getChildren().forEach(node -> {
            if (node instanceof Button btn) {
                btn.setMinHeight(h);
                btn.prefWidthProperty().bind(scene.widthProperty().multiply(wFrac));
                HBox.setHgrow(btn, Priority.ALWAYS);
            }
        });
    }

    // Metody wywoływane z Topology przy tworzeniu kontrolera:
    public void point_to_button(Button b) { this.but = b; }
    public void set_logo(ImageView logo)     { this.logo = logo; }
}
