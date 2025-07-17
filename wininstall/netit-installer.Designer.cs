namespace netit_instaler
{
    partial class Netit
    {
        /// <summary>
        /// Wymagana zmienna projektanta.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Wyczyść wszystkie używane zasoby.
        /// </summary>
        /// <param name="disposing">prawda, jeżeli zarządzane zasoby powinny zostać zlikwidowane; Fałsz w przeciwnym wypadku.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Kod generowany przez Projektanta formularzy systemu Windows

        /// <summary>
        /// Metoda wymagana do obsługi projektanta — nie należy modyfikować
        /// jej zawartości w edytorze kodu.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Netit));
            this.iscLicense = new System.Windows.Forms.RichTextBox();
            this.chkAccept = new System.Windows.Forms.CheckBox();
            this.lblname = new System.Windows.Forms.Label();
            this.install = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // iscLicense
            // 
            this.iscLicense.Location = new System.Drawing.Point(105, 23);
            this.iscLicense.Name = "iscLicense";
            this.iscLicense.ReadOnly = true;
            this.iscLicense.Size = new System.Drawing.Size(378, 345);
            this.iscLicense.TabIndex = 0;
            this.iscLicense.Text = resources.GetString("iscLicense.Text");
            // 
            // chkAccept
            // 
            this.chkAccept.AutoSize = true;
            this.chkAccept.Location = new System.Drawing.Point(120, 419);
            this.chkAccept.Name = "chkAccept";
            this.chkAccept.Size = new System.Drawing.Size(149, 17);
            this.chkAccept.TabIndex = 1;
            this.chkAccept.Text = "Akceptuję warunki licencji";
            this.chkAccept.UseVisualStyleBackColor = true;
            this.chkAccept.CheckedChanged += new System.EventHandler(this.checkBox1_CheckedChanged);
            // 
            // lblname
            // 
            this.lblname.AutoSize = true;
            this.lblname.Location = new System.Drawing.Point(323, 423);
            this.lblname.Name = "lblname";
            this.lblname.Size = new System.Drawing.Size(45, 13);
            this.lblname.TabIndex = 2;
            this.lblname.Text = "netit 1.3";
            this.lblname.Click += new System.EventHandler(this.label1_Click);
            // 
            // install
            // 
            this.install.Location = new System.Drawing.Point(120, 462);
            this.install.Name = "install";
            this.install.Size = new System.Drawing.Size(75, 23);
            this.install.TabIndex = 3;
            this.install.Text = "Instaluj";
            this.install.UseVisualStyleBackColor = true;
            this.install.Click += new System.EventHandler(this.install_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(608, 531);
            this.Controls.Add(this.install);
            this.Controls.Add(this.lblname);
            this.Controls.Add(this.chkAccept);
            this.Controls.Add(this.iscLicense);
            this.Name = "Netit Installer";
            this.Text = "Netit Installer";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.RichTextBox iscLicense;
        private System.Windows.Forms.CheckBox chkAccept;
        private System.Windows.Forms.Label lblname;
        private System.Windows.Forms.Button install;
    }
}

