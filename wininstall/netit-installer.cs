using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace netit_instaler
{
    public partial class Netit : Form
    {
        // Metoda rekurencyjnego kopiowania folderu
        private void CopyDirectory(string sourceDir, string targetDir)
        {
            // 1. Tworzymy folder docelowy, jeśli jeszcze nie istnieje
            Directory.CreateDirectory(targetDir);

            // 2. Kopiujemy wszystkie pliki z bieżącego katalogu
            foreach (var file in Directory.GetFiles(sourceDir))
            {
                string destFile = Path.Combine(targetDir, Path.GetFileName(file));
                File.Copy(file, destFile, overwrite: true);
            }

            // 3. Dla każdego podfolderu wywołujemy tę samą funkcję (rekurencja)
            foreach (var dir in Directory.GetDirectories(sourceDir))
            {
                string destSubDir = Path.Combine(targetDir, Path.GetFileName(dir));
                CopyDirectory(dir, destSubDir);
            }
        }


        public Netit()
        {
            InitializeComponent();
        }

        private void checkBox1_CheckedChanged(object sender, EventArgs e)
        {

        }

        private void label1_Click(object sender, EventArgs e)
        {

        }

        private void install_Click(object sender, EventArgs e)
        {

            try
            {
                var process = new Process();
                process.StartInfo.FileName = "java";
                process.StartInfo.Arguments = "-version";
                process.StartInfo.RedirectStandardError = true;
                process.StartInfo.UseShellExecute = false;
                process.StartInfo.CreateNoWindow = true;

                process.Start();
                string output = process.StandardError.ReadToEnd();
                process.WaitForExit();
                var match = Regex.Match(output, @"version\s+""(?<major>\d+)(\.\d+)*""");


                if (match.Success)
                {
                    int majorVersion = int.Parse(match.Groups["major"].Value);
                    if (majorVersion < 21)
                    {
                        MessageBox.Show("Musisz pobrać jave w wersji 21+", "Uwaga",
                   MessageBoxButtons.OK, MessageBoxIcon.Warning);
                        return;
                    }
                }
                else
                {
                    MessageBox.Show("Error nie udalo się odczytać czy masz javę", "Uwaga",
                   MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Nie można wywołać polecenia 'java'. Java prawdopodobnie nie jest dodana do zmiennej PATH. Szczegóły: {ex.Message}", "Uwaga",
                   MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }


            // 1. Sprawdzenie licencji
            if (!chkAccept.Checked)
            {
                MessageBox.Show("Musisz zaakceptować licencję.", "Uwaga",
                    MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            try
            {
                // 2. Sprawdzenie folderu "netit" obok instalatora
                string baseDir = AppDomain.CurrentDomain.BaseDirectory;
                string source = Path.Combine(baseDir, "netit");
                if (!Directory.Exists(source))
                {
                    MessageBox.Show("Brakuje folderu 'netit' w katalogu instalatora.", "Błąd",
                        MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;
                }

                // 3. Ścieżka docelowa w Program Files (x86)
                string target = Path.Combine(
                    Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86),
                    "netit"
                );

                // 4. Kopiowanie folderu
                CopyDirectory(source, target);

                // 5. Utworzenie wrappera netit.cmd w katalogu instalacji
                string wrapperPath = Path.Combine(target, "netit.cmd");
                string wrapperContent =
"@echo off\r\n" +
"pushd \"" + target + "\"\r\n" +
"start \"\" /B javaw --module-path \"C:\\Program Files (x86)\\netit\\javafx\\lib\" --add-modules javafx.controls,javafx.fxml,javafx.web -jar netit.jar %*\r\n" +
"popd\r\n";

                var utf8NoBom = new System.Text.UTF8Encoding(false);
                File.WriteAllText(wrapperPath, wrapperContent, utf8NoBom);


                // 6. Dodanie katalogu instalacji do zmiennych środowiskowych (User PATH)
                string pathValue = Environment.GetEnvironmentVariable("Path", EnvironmentVariableTarget.User) ?? "";
                // Sprawdź, czy ścieżka nie jest już dodana
                if (!pathValue.Split(';')
                              .Any(p => string.Equals(p.Trim(), target, StringComparison.OrdinalIgnoreCase)))
                {
                    pathValue = pathValue.TrimEnd(';') + ";" + target;
                    Environment.SetEnvironmentVariable("Path", pathValue, EnvironmentVariableTarget.User);
                }

                MessageBox.Show("Instalacja zakończona pomyślnie!\n" +
                                "W konsoli wpisz po prostu: netit", "Sukces",
                                MessageBoxButtons.OK, MessageBoxIcon.Information);
                this.Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show("Błąd podczas instalacji:\n" + ex.Message, "Błąd",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }

    }
