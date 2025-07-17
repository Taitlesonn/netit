package com.netit;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Edytor {
    @FXML private HBox top_p;
    @FXML private BorderPane e;
    @FXML private VBox left_scr;

    @FXML private MenuItem img_path_b;
    @FXML private MenuItem tile_opt;
    @FXML private MenuItem podg;
    @FXML private MenuItem typer;
    @FXML private MenuItem css_seter;
    @FXML private MenuItem load_c;

    private final TextArea edytor_lf = new TextArea();
    private final WebView podglad_w = new WebView();
    private Stage primary_s;

    private final Map<Integer, String> castom_t = new HashMap<>();
    private int cunt_map = 0;
    private int type = 0;
    private int ttype = -1;
    private String title;
    private boolean is_title = false;
    private boolean stan_podgladu;

    public void set_stage(Stage primary_s) {
        this.primary_s = primary_s;
    }

    @FXML
    private void initialize() {
        stan_podgladu = true;
        e.setCenter(edytor_lf);
        edytor_lf.setStyle("-fx-control-inner-background: #d3d3d3;");
        edytor_lf.setFont(Font.font("Arial", 15));
    }

    @FXML
    private void add_image_to_text() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Png file (*.png)", "*.png"),
                new FileChooser.ExtensionFilter("Jpg file (*.jpg)", "*.jpg")
        );
        File file = fileChooser.showOpenDialog(primary_s);
        if (file != null) {
            add_tag(file.getAbsolutePath());
            ImageView obraz = new ImageView(new Image(file.toURI().toString()));
            obraz.setFitWidth(75);
            obraz.setFitHeight(75);
            left_scr.getChildren().add(obraz);
        }
    }

    @FXML
    private void setLoad_c(){

    }

    @FXML
    private void podgld_f() throws IOException {
        if (stan_podgladu) {
            stan_podgladu = false;
            if (ttype == -1) {
                showAlert(Alert.AlertType.INFORMATION, "TYPE ERROR", "Select device first");
            } else if (title == null || title.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Brak tytułu", "Proszę ustawić tytuł dokumentu przed podglądem.");
                stan_podgladu = true;
                return;
            } else {
                // przygotowanie i załadowanie HTML
                String html = converter();
                String ff = GetTT();
                Path baseDir = Paths.get(System.getProperty("user.dir"), "out", "files", ff, title);
                Files.createDirectories(baseDir);
                Path index = baseDir.resolve("index.html");
                Files.writeString(index, html);

                e.setCenter(podglad_w);
                podglad_w.getEngine().load(index.toUri().toString());
            }
        } else {
            stan_podgladu = true;
            e.setCenter(edytor_lf);
        }
    }


    @FXML
    private void pokazOpcje() {
        List<String> opcje = List.of("Ruter", "Switch", "Linux", "Windows", "Linux Server", "Windows Server");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(opcje.getFirst(), opcje);
        dialog.setTitle("Wybierz opcję");
        dialog.setHeaderText("Wybierz jedną z opcji:");
        dialog.setContentText("Opcja:");

        dialog.showAndWait().ifPresent(wybrana -> {
            switch (wybrana) {
                case "Ruter"         -> ttype = Topology.ruter_t;
                case "Switch"        -> ttype = Topology.swithe_t;
                case "Linux"         -> ttype = Topology.linux_t;
                case "Windows"       -> ttype = Topology.windows_t;
                case "Linux Server"  -> ttype = Topology.linux_server_t;
                case "Windows Server"-> ttype = Topology.windos_server_t;
            }
        });
    }

    @FXML
    private void set_title() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Wprowadź tytuł");
        dialog.setHeaderText("Podaj tytuł dokumentu:");
        dialog.setContentText("Tytuł:");

        dialog.showAndWait().ifPresent(tytul -> {
            this.is_title = true;
            this.title = tytul;
            Topology.set_title(this.title);
        });
    }

    @FXML
    private void setter_of_css() {
        List<String> opcje = List.of("mono", "blue", "gray");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(opcje.get(0), opcje);
        dialog.setTitle("Wybierz opcję");
        dialog.setHeaderText("Wybierz jedną z opcji:");
        dialog.setContentText("Opcja:");

        dialog.showAndWait().ifPresent(wybrany -> {
            switch (wybrany) {
                case "mono" -> this.set_type_mono();
                case "blue" -> this.set_type_blue();
                case "gray" -> this.set_type_gray();
            }
        });
    }




    // --- Pomocnicze metody do generowania HTML i zarządzania obrazami ---
    private void set_type_mono()  { type = 0; }
    private void set_type_gray()  { type = 1; }
    private void set_type_blue()  { type = 2; }

    private void add_tag(String path) {
        cunt_map++;
        castom_t.put(cunt_map, path);
    }

    public String GetTT() {
        return switch (ttype) {
            case Topology.ruter_t        -> "ruter";
            case Topology.swithe_t       -> "switch";
            case Topology.linux_t        -> "linux";
            case Topology.windows_t      -> "windows";
            case Topology.linux_server_t -> "linux_s";
            case Topology.windos_server_t-> "windows_s";
            default -> throw new IllegalStateException("Unexpected topology type: " + ttype);
        };
    }

    private String getHtmlHeader() {
        String css;
        switch (type) {
            case 1 -> css = "style-gray.css";
            case 2 -> css = "style-blue.css";
            default-> css = "style-mono.css";
        }
        return """
            <!DOCTYPE html>
            <html>
              <head>
                <title>NETIT - GENERETED HTML</title>
                <meta charset="utf-8" />
                <script src="../../src/index.js"></script>
                <link rel="stylesheet" href="../../src/""" + css + "\" />\n" +
                "  </head>\n" +
                "  <body class=\"body-std\">\n" +
                "    <canvas id=\"particles-canvas\"></canvas>\n" +
                "    <article class=\"article\">\n";
    }

    private String getHtmlFooter() {
        return """
            </article>
          </body>
        </html>
        """;
    }

    private String converter() {
        List<String> lines = List.of(edytor_lf.getText().split("\\R"));
        StringBuilder sb = new StringBuilder();
        sb.append(getHtmlHeader());

        for (String line : lines) {
            if (line.isBlank()) continue;
            if (line.charAt(0) == '#') {
                sb.append("<h1 class=\"h1\">").append(line.substring(1).trim()).append("</h1>\n");
            } else if (line.charAt(0) == '*') {
                sb.append("<h2 class=\"h2\">").append(line.substring(1).trim()).append("</h2>\n");
            } else if (line.startsWith("<img")) {
                String rest = line.substring(line.indexOf('>') + 1).trim();
                String numStr = rest.replaceFirst("^([0-9]+).*", "$1");
                try {
                    int idx = Integer.parseInt(numStr);
                    if (idx >= 0 && idx <= cunt_map) {
                        String path = castom_t.get(idx);
                        String ext = path.substring(path.lastIndexOf('.'));
                        String ff  = GetTT();
                        Path baseDir = Paths.get(System.getProperty("user.dir"), "out", "files", ff, title);
                        Files.createDirectories(baseDir);
                        Path src = Path.of(path);
                        Path dst = baseDir.resolve(idx + ext);
                        Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                        sb.append("<img src=\"").append(idx).append(ext)
                                .append("\" class=\"responsive-img\" />\n");
                    }
                } catch (NumberFormatException | IOException e) {
                    // pomijamy błędny wpis
                }
            } else {
                sb.append("<p>").append(line).append("</p>\n");
            }
        }

        sb.append(getHtmlFooter());
        return sb.toString();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public String gen_text(){
       return this.edytor_lf.getText();
    }

    public Boolean is_type(){
        return title != null && !title.isEmpty();
    }

}
