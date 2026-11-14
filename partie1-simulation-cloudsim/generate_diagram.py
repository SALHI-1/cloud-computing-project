"""
generate_diagram.py
-------------------
Génère le diagramme d'architecture Cloud Hybride de l'Université Abdelmalek Essaâdi (UAE).
Auteur : Mohammed Salhi (R134235437).

Sortie : uae_cloud_architecture.png dans le dossier courant.
Dépendance : binaire `dot` de Graphviz (sudo apt install graphviz).
"""

import subprocess
import sys
import os
import textwrap

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_PNG  = os.path.join(SCRIPT_DIR, "uae_cloud_architecture.png")

DOT_SOURCE = textwrap.dedent(r"""
digraph UAE_Cloud_Salhi {

    // ── Paramètres globaux ────────────────────────────────────────────────────
    graph [
        label     = "Architecture Cloud Hybride — Université Abdelmalek Essaâdi (UAE)\nConception et Modélisation CloudSim Plus — Mohammed Salhi (R134235437)"
        labelloc  = "t"
        fontsize  = 24
        fontname  = "Helvetica-Bold"
        fontcolor = "#0f172a"
        bgcolor   = "#f8fafc"
        rankdir   = TB
        splines   = curved
        nodesep   = 0.6
        ranksep   = 1.0
        pad       = 0.8
        size      = "14,24!"
    ]
    node [fontname="Helvetica" fontsize=11 style="filled,rounded" shape=box]
    edge [fontname="Helvetica" fontsize=10 color="#475569"]

    // ══════════════════════════════════════════════════════════════════════════
    // LAYER 0 – CAMPUS & COMMUNAUTÉ UNIVERSITAIRE
    // ══════════════════════════════════════════════════════════════════════════
    subgraph cluster_users {
        label      = "Communauté Universitaire UAE  —  130 750 Étudiants · 1 357 Enseignants-Chercheurs · 600 Personnels"
        fontsize   = 13
        fontname   = "Helvetica-Bold"
        fontcolor  = "#ffffff"
        style      = "filled,rounded"
        fillcolor  = "#1e293b"
        color      = "#0f172a"
        penwidth   = 2.5

        TANGER  [label="Pôle Tanger (7 Établissements)\nFST · ENCG · ENSA · FMPT\nFSJP · FEG · ESRFT"
                 fillcolor="#2563eb" fontcolor="#ffffff" shape=cylinder]
        TETOUAN [label="Pôle Tétouan / Martil (7 Établissements)\nFac. Sciences · FLSH · FSJES\nENS · ENSA · EST · FOD"
                 fillcolor="#2563eb" fontcolor="#ffffff" shape=cylinder]
        AUTRES  [label="Pôles Régionaux Associés\nAl Hoceïma (5 Établissements : ENSA · FST · FSJES · EST · ENCG)\nLarache (FPL) · Ksar El-Kébir (FPK)"
                 fillcolor="#0284c7" fontcolor="#ffffff" shape=cylinder]

        TANGER -> TETOUAN -> AUTRES [style=invis]
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LAYER 1 – GATEWAY & LOAD BALANCING
    // ══════════════════════════════════════════════════════════════════════════
    LB [
        label     = "Passerelle Cloud & Répartiteur de Charge (Load Balancer / API Gateway)\nDistribution de trafic · Terminaison SSL · Basculement Haute Disponibilité"
        fillcolor = "#ea580c"
        fontcolor = "#ffffff"
        shape     = diamond
        penwidth  = 2.5
        fontsize  = 13
        style     = "filled"
    ]

    TANGER  -> LB [label="HTTPS / TLS" penwidth=2   color="#0284c7" fontcolor="#0284c7"]
    TETOUAN -> LB [label="HTTPS / TLS" penwidth=2   color="#0284c7" fontcolor="#0284c7"]
    AUTRES  -> LB [label="HTTPS / TLS" penwidth=1.5 color="#0284c7" fontcolor="#0284c7"]

    // ══════════════════════════════════════════════════════════════════════════
    // LAYER 2 – CLOUD PRIVÉ DE L'UAE
    // Capacité : 12 hôtes × 32 cœurs = 384 cœurs / 1 536 Go RAM
    // Base normale : 212 cœurs (48 VMs) | Seuil de sécurité : 80 % (307 cœurs max)
    // Réserve locale PRA / Disaster Recovery : 77 cœurs (20 %)
    // ══════════════════════════════════════════════════════════════════════════
    subgraph cluster_private {
        label      = "Cloud Privé Souverain de l'UAE  (IaaS · PaaS · SaaS)  —  384 cœurs physiques · 1 536 Go RAM\nSeuil de Débordement fixé à 80 % (307 cœurs max autorisés)  |  Marge de Sécurité PRA : 20 % (77 cœurs)"
        fontsize   = 14
        fontname   = "Helvetica-Bold"
        fontcolor  = "#0f4c81"
        style      = "filled,rounded"
        fillcolor  = "#eff6ff"
        color      = "#1e40af"
        penwidth   = 3

        DC1_ANCHOR [style=invis width=0 height=0 label=""]
        DC2_ANCHOR [style=invis width=0 height=0 label=""]
        { rank=same; DC1_ANCHOR; DC2_ANCHOR }
        DC1_ANCHOR -> DC2_ANCHOR [style=invis]

        // ── Datacenter 1 – Tanger (Campus Boukhalef) ──────────────────────
        subgraph cluster_dc1 {
            label      = "Datacenter 1 — Tanger (Boukhalef)"
            fontsize   = 13
            fontname   = "Helvetica-Bold"
            fontcolor  = "#1e3a8a"
            style      = "filled,rounded"
            fillcolor  = "#dbeafe"
            color      = "#1d4ed8"
            penwidth   = 2.5

            subgraph cluster_dc1_hosts {
                label    = "6 Serveurs Bi-Xeon physiques (32 cœurs · 128 Go RAM · 12 To NVMe · 10 Gbps)"
                fontsize = 9
                style    = "dashed"
                color    = "#64748b"

                H1 [label="Hôte 1\n32c / 128Go" fillcolor="#bfdbfe" shape=box3d style=filled]
                H2 [label="Hôte 2\n32c / 128Go" fillcolor="#bfdbfe" shape=box3d style=filled]
                H3 [label="Hôte 3\n32c / 128Go" fillcolor="#bfdbfe" shape=box3d style=filled]
                H4 [label="Hôte 4\n32c / 128Go" fillcolor="#bfdbfe" shape=box3d style=filled]
                H5 [label="Hôte 5\n32c / 128Go" fillcolor="#bfdbfe" shape=box3d style=filled]
                H6 [label="Hôte 6\n32c / 128Go" fillcolor="#bfdbfe" shape=box3d style=filled]
                H1 -> H2 -> H3 -> H4 -> H5 -> H6 [style=invis]
            }

            subgraph cluster_dc1_vms {
                label    = "Machines Virtuelles Hébergées (DC Tanger)"
                fontsize = 9
                style    = "dashed"
                color    = "#1e40af"

                VM1_DB     [label="Bases de Données (VM 1-4)\nPaaS — 4 VMs · 8 cœurs / 32 Go\nDonnées sensibles Apogée (100% privé)"
                            fillcolor="#10b981" fontcolor="#ffffff" color="#047857" penwidth=2 style="filled,rounded"]
                VM1_ELEARN [label="Moodle E-learning (VM 5-12)\nSaaS — 8 VMs · 4 cœurs / 16 Go\nConsultation cours & TD"
                            fillcolor="#93c5fd" style="filled,rounded"]
                VM1_ENT    [label="Scolarité & ENT (VM 13-15)\nSaaS — 3 VMs de base · 4 cœurs / 8 Go\nÉlasticité locale +12 VMs en pointe"
                            fillcolor="#fef08a" color="#ca8a04" penwidth=2 style="filled,rounded"]
                VM1_EXAMS  [label="Examens & QCM (VM 16-17)\nSaaS — 2 VMs · 4 cœurs / 8 Go"
                            fillcolor="#fed7aa" style="filled,rounded"]
                VM1_DSI    [label="Calcul Scientifique & DSI (VM 18-21)\nIaaS — 4 VMs · 4 cœurs / 16 Go\nEnvironnements Linux de recherche"
                            fillcolor="#c084fc" style="filled,rounded"]
                VM1_MAIL   [label="Messagerie & Doc. (VM 22-24)\nSaaS — 3 VMs · 2 cœurs / 4 Go"
                            fillcolor="#93c5fd" style="filled,rounded"]

                VM1_DB -> VM1_ELEARN -> VM1_ENT -> VM1_EXAMS -> VM1_DSI -> VM1_MAIL [style=invis]
            }
        }

        // ── Datacenter 2 – Tétouan (Campus Martil) ────────────────────────
        subgraph cluster_dc2 {
            label      = "Datacenter 2 — Tétouan (Martil)"
            fontsize   = 13
            fontname   = "Helvetica-Bold"
            fontcolor  = "#1e3a8a"
            style      = "filled,rounded"
            fillcolor  = "#dbeafe"
            color      = "#1d4ed8"
            penwidth   = 2.5

            subgraph cluster_dc2_hosts {
                label    = "6 Serveurs Bi-Xeon physiques (32 cœurs · 128 Go RAM · 12 To NVMe · 10 Gbps)"
                fontsize = 9
                style    = "dashed"
                color    = "#64748b"

                H7  [label="Hôte 7\n32c / 128Go"  fillcolor="#bfdbfe" shape=box3d style=filled]
                H8  [label="Hôte 8\n32c / 128Go"  fillcolor="#bfdbfe" shape=box3d style=filled]
                H9  [label="Hôte 9\n32c / 128Go"  fillcolor="#bfdbfe" shape=box3d style=filled]
                H10 [label="Hôte 10\n32c / 128Go" fillcolor="#bfdbfe" shape=box3d style=filled]
                H11 [label="Hôte 11\n32c / 128Go" fillcolor="#bfdbfe" shape=box3d style=filled]
                H12 [label="Hôte 12\n32c / 128Go" fillcolor="#bfdbfe" shape=box3d style=filled]
                H7 -> H8 -> H9 -> H10 -> H11 -> H12 [style=invis]
            }

            subgraph cluster_dc2_vms {
                label    = "Machines Virtuelles Hébergées (DC Tétouan)"
                fontsize = 9
                style    = "dashed"
                color    = "#1e40af"

                VM2_DB     [label="Bases de Données (VM 25-28)\nPaaS — 4 VMs · 8 cœurs / 32 Go\nRéplication synchrone PRA"
                            fillcolor="#10b981" fontcolor="#ffffff" color="#047857" penwidth=2 style="filled,rounded"]
                VM2_ELEARN [label="Moodle E-learning (VM 29-36)\nSaaS — 8 VMs · 4 cœurs / 16 Go\nConsultation cours & TD"
                            fillcolor="#93c5fd" style="filled,rounded"]
                VM2_ENT    [label="Scolarité & ENT (VM 37-39)\nSaaS — 3 VMs de base · 4 cœurs / 8 Go\nÉlasticité locale +11 VMs en pointe"
                            fillcolor="#fef08a" color="#ca8a04" penwidth=2 style="filled,rounded"]
                VM2_EXAMS  [label="Examens & QCM (VM 40-41)\nSaaS — 2 VMs · 4 cœurs / 8 Go"
                            fillcolor="#fed7aa" style="filled,rounded"]
                VM2_DSI    [label="Calcul Scientifique & DSI (VM 42-45)\nIaaS — 4 VMs · 4 cœurs / 16 Go\nEnvironnements Linux de recherche"
                            fillcolor="#c084fc" style="filled,rounded"]
                VM2_MAIL   [label="Messagerie & Doc. (VM 46-48)\nSaaS — 3 VMs · 2 cœurs / 4 Go"
                            fillcolor="#93c5fd" style="filled,rounded"]

                VM2_DB -> VM2_ELEARN -> VM2_ENT -> VM2_EXAMS -> VM2_DSI -> VM2_MAIL [style=invis]
            }
        }

        // Liaison de Haute Disponibilité / PRA
        H1 -> H7 [
            label      = "  Liaison Inter-Datacenter Dédiée (10 Gbps) — Réplication & PRA  "
            style      = dashed
            color      = "#dc2626"
            fontcolor  = "#b91c1c"
            fontsize   = 10
            dir        = both
            penwidth   = 2.5
            constraint = false
        ]
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LAYER 3 – CLOUD PUBLIC (BURSTING À LA DEMANDE)
    // Débordement dynamique déclenché dès atteinte du seuil de 80 %
    // +23 VMs absorbées dans le privé, +27 VMs déployées sur le Cloud Public
    // ══════════════════════════════════════════════════════════════════════════
    subgraph cluster_public {
        label      = "Cloud Public  —  Débordement Élastique (Cloud Bursting)\nServices web sans état (ENT Inscriptions)  |  Local privé : +23 VMs (92 cœurs)  →  Cloud Public : +27 VMs (108 cœurs)"
        fontsize   = 13
        fontname   = "Helvetica-Bold"
        fontcolor  = "#854d0e"
        style      = "filled,rounded"
        fillcolor  = "#fefce8"
        color      = "#ca8a04"
        penwidth   = 3

        PUB_ENT [
            label     = "Instances ENT & Inscriptions Débordées\nHorizontalVmScaling dynamique\n+27 VMs sur Cloud Public · 4 cœurs / 8 Go RAM\nFacturation stricte à l'usage (Opex)"
            fillcolor = "#fef08a"
            color     = "#ca8a04"
            penwidth  = 2
            style     = "filled,rounded"
        ]
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ARÊTES FONCTIONNELLES & ROUTAGE
    // ══════════════════════════════════════════════════════════════════════════
    LB -> DC1_ANCHOR [penwidth=2.5 color="#1e40af" fontcolor="#1e40af" label="vers DC Tanger"]
    LB -> DC2_ANCHOR [penwidth=2.5 color="#1e40af" fontcolor="#1e40af" label="vers DC Tétouan"]
    DC1_ANCHOR -> H1 [style=invis]
    DC2_ANCHOR -> H7 [style=invis]

    // Virtualisation (Hôtes → VMs)
    H1 -> VM1_DB     [style=dashed color="#2563eb" arrowhead=open label="Hyperviseur"]
    H2 -> VM1_ELEARN [style=dashed color="#2563eb" arrowhead=open]
    H3 -> VM1_ENT    [style=dashed color="#2563eb" arrowhead=open]
    H4 -> VM1_EXAMS  [style=dashed color="#2563eb" arrowhead=open]
    H5 -> VM1_DSI    [style=dashed color="#2563eb" arrowhead=open]

    H7  -> VM2_DB     [style=dashed color="#2563eb" arrowhead=open label="Hyperviseur"]
    H8  -> VM2_ELEARN [style=dashed color="#2563eb" arrowhead=open]
    H9  -> VM2_ENT    [style=dashed color="#2563eb" arrowhead=open]
    H10 -> VM2_EXAMS  [style=dashed color="#2563eb" arrowhead=open]
    H11 -> VM2_DSI    [style=dashed color="#2563eb" arrowhead=open]

    // Cloud Bursting (Débordement vers le Cloud Public)
    VM1_ENT -> PUB_ENT [label="Seuil 80% atteint : Bursting (+14 VMs)" color="#ea580c" fontcolor="#c2410c" penwidth=2.5 style=bold]
    VM2_ENT -> PUB_ENT [label="Seuil 80% atteint : Bursting (+13 VMs)" color="#ea580c" fontcolor="#c2410c" penwidth=2.5 style=bold]

    // ══════════════════════════════════════════════════════════════════════════
    // LÉGENDE DU SCHÉMA
    // ══════════════════════════════════════════════════════════════════════════
    subgraph cluster_legend {
        label      = "Légende & Conventions Architecturales"
        fontsize   = 11
        fontname   = "Helvetica-Bold"
        style      = "filled,rounded"
        fillcolor  = "#f1f5f9"
        color      = "#94a3b8"
        penwidth   = 1.5

        L1 [label="──→   Trafic utilisateur HTTPS / Requêtes web" fillcolor="#f8fafc" shape=plaintext style=filled fontsize=10]
        L2 [label="-->   Lien de virtualisation KVM (Hôte → VM)" fillcolor="#f8fafc" shape=plaintext style=filled fontsize=10]
        L3 [label="━━→   Débordement élastique (HorizontalVmScaling vers Public)" fillcolor="#f8fafc" shape=plaintext style=filled fontsize=10]
        L4 [label="⟺    Liaison haute disponibilité PRA inter-campus" fillcolor="#f8fafc" shape=plaintext style=filled fontsize=10]
        L5 [label="🟢  Bases de données hautement souveraines (confinement 100% privé)" fillcolor="#d1fae5" shape=plaintext style=filled fontsize=10]
        L6 [label="🟡  Services élastiques à mise à l'échelle automatique" fillcolor="#fef08a" shape=plaintext style=filled fontsize=10]
        L1 -> L2 -> L3 -> L4 -> L5 -> L6 [style=invis]
    }
}
""")


def build_diagram(dot_source: str, output_path: str, dpi: int = 150) -> None:
    result = subprocess.run(
        ["dot", f"-Gdpi={dpi}", "-Tpng", f"-o{output_path}"],
        input=dot_source.encode("utf-8"),
        capture_output=True,
    )
    if result.stderr:
        for line in result.stderr.decode().splitlines():
            print(f"[graphviz] {line}", file=sys.stderr)
    result.check_returncode()


def main() -> None:
    print("=" * 60)
    print("  Génération du diagramme Cloud UAE — Mohammed Salhi")
    print("=" * 60)

    check = subprocess.run(["which", "dot"], capture_output=True)
    if check.returncode != 0:
        print("ERREUR : Graphviz (`dot`) n'est pas installé.")
        sys.exit(1)

    print(f"Sortie : {OUTPUT_PNG}")
    build_diagram(DOT_SOURCE, OUTPUT_PNG, dpi=150)
    size_kb = os.path.getsize(OUTPUT_PNG) / 1024
    print(f"[OK] Diagramme généré avec succès ({size_kb:.1f} KB)")


if __name__ == "__main__":
    main()
