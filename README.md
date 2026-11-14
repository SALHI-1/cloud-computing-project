# 🎓 Projet Cloud Intelligence : Architecture, Déploiement IaaS/SaaS, IaC et Supervision SLA

**Auteur :** Mohammed Salhi  
**CNE / Identifiant :** R134235437  
**Établissement :** Université Abdelmalek Essaâdi (UAE) — Faculté des Sciences et Techniques  
**Module :** Cloud Computing & Ingénierie Cloud  

---

## 📌 Présentation Générale

Ce dépôt regroupe l'ensemble des développements, scripts d'automatisation, configurations déclaratives et analyses de performance réalisés dans le cadre du projet **Cloud Intelligence**.

Le projet couvre les quatre dimensions fondamentales des architectures cloud modernes :
1. **Modélisation et Simulation d'un Cloud Hybride** pour l'Université Abdelmalek Essaâdi avec gestion dynamique du débordement (*Cloud Bursting*) sous **CloudSim Plus**.
2. **Déploiement IaaS et SaaS sous OpenStack** (réseautage virtuel avec Neutron, calcul avec Nova, instances CirrOS et Ubuntu, automatisation Cloud-Init / Flask sous Systemd).
3. **Infrastructure as Code (IaC) et Gestion de Configuration** via **Terraform** et **Ansible** pour le provisionnement déclaratif et le déploiement d'un serveur web Nginx personnalisé.
4. **Supervision et Respect des Accords de Qualité de Service (SLA)** avec un moniteur autonome en **Python**, mesure en temps réel de la disponibilité, détection de pannes injectées, calcul du **MTBF** et du **MTTR**, et génération de rapports d'audit contractuels.

---

## 🗂️ Structure du Répertoire

```
.
├── Salhi_Mohammed_Rapport.pdf       # Rapport de projet officiel (25 pages, A4)
├── Salhi_Mohammed_Code.zip          # Archive propre contenant le code des 4 parties
├── README.md                        # Documentation principale du dépôt
├── .gitignore                       # Fichiers et artefacts exclus du versionnement
│
├── partie1-simulation-cloudsim/     # 1. Simulation CloudSim Plus
│   ├── pom.xml                      # Dépendances Maven (CloudSim Plus 7.3.0)
│   ├── src/                         # Code Java de la simulation hybride UAE
│   ├── uae_cloud_architecture.png   # Schéma d'architecture généré (2100x1434)
│   ├── generate_diagram.py          # Script de génération Graphviz
│   ├── architecture_uae.md          # Spécifications de l'infrastructure
│   ├── modelisation_des_charges.md  # Modélisation des scénarios normale / pic
│   ├── analyse_des_resultats.md     # Analyse des métriques et Cloud Bursting
│   └── README.md                    # Guide d'exécution de la simulation
│
├── partie2-openstack-iaas-saas/     # 2. Déploiement OpenStack IaaS & SaaS
│   ├── user-data-saas.sh            # Script Cloud-Init (Flask + Systemd)
│   └── README.md                    # Guide et commandes CLI OpenStack
│
├── partie3-iac-terraform-ansible/   # 3. Infrastructure as Code
│   ├── terraform/
│   │   └── main.tf                  # Code déclaratif OpenStack (secgroup, VM, FIP)
│   ├── ansible/
│   │   ├── ansible.cfg              # Configuration Ansible
│   │   ├── inventory.ini            # Inventaire de déploiement
│   │   └── playbook.yml             # Playbook d'installation et configuration Nginx
│   └── README.md                    # Guide d'exécution Terraform & Ansible
│
├── partie4-sla-monitoring/          # 4. Supervision SLA & Haute Disponibilité
│   ├── sla.json                     # Contrat d'objectifs de service (SLA)
│   ├── sla_monitor.py               # Moniteur Python d'interrogation OpenStack
│   ├── sla_measures.csv             # Mesures chronologiques enregistrées
│   ├── sla_report.json              # Rapport final d'audit contractuel SLA
│   └── README.md                    # Guide de supervision et d'injection de panne
│
└── screenshots/                     # Captures d'écran renommées et structurées
    ├── partie1/                     # Schéma d'architecture hybride
    ├── partie2/                     # Captures IaaS et SaaS sous Horizon et CLI (21 captures)
    ├── partie3/                     # Captures Terraform Plan/Apply et Ansible (5 captures)
    └── partie4/                     # Captures Supervision SLA et Injection de panne (4 captures)
```

---

## 🚀 Synthèse des Réalisations par Partie

### 🌐 Partie 1 : Cloud Hybride Universitaire (CloudSim Plus)
- **Topologie :** 12 serveurs physiques (6 Tanger, 6 Tétouan), 384 cœurs vCPU, 1536 Go RAM.
- **Règle de Bursting :** Déclenchement à 80% d'utilisation (307 cœurs).
- **Résultats de Charge :**
  - *Charge normale :* 100 cloudlets sur 48 VMs de base, saturation ~25%, 0 débordement.
  - *Pic d'examens :* 801 cloudlets simultanés. Redimensionnement dynamique : +23 VMs privées, débordement de 27 VMs sur AWS public. **Taux de succès : 100%**.

### ☁️ Partie 2 : Déploiement IaaS & SaaS sous OpenStack
- **IaaS :** Réseau `iaas-net` (`192.168.10.0/24`), routeur `iaas-router` vers passerelle `public`, groupe de sécurité `iaas-secgroup` (ICMP/SSH), instance `cirros-instance` avec IP flottante `172.24.4.78`. Connectivité et session SSH validées.
- **SaaS :** Flavor personnalisée `m1.saas` (1 vCPU, 1.5 Go RAM, 10 Go disque), groupe `saas-secgroup` (ICMP/SSH/HTTP), instance Ubuntu 22.04 `saas-instance` avec injection Cloud-Init (`user-data-saas.sh`) hébergeant une application Flask multi-services active sur le port 80.

### ⚙️ Partie 3 : Infrastructure as Code (Terraform & Ansible)
- **Terraform :** Déclaration de `secgroup-terraform`, de l'instance `ubuntu-terraform`, et de l'IP flottante (`172.24.4.195`). Cycle de vie validé : `terraform init` &rarr; `plan` (7 à ajouter) &rarr; `apply` (exécution sans erreur) &rarr; `destroy` (nettoyage complet).
- **Ansible :** Playbook automatisé déployant Nginx avec une page d'accueil personnalisée responsive UAE, gestion du service via systemd, et test HTTP automatique (`status_code == 200`).

### 📊 Partie 4 : Gestion des SLA et Supervision
- **Contrat SLA :** Disponibilité contractuelle $\ge 99.0\%$, MTTR toléré $\le 30\text{ s}$, pénalité financière de 500 MAD/min.
- **Audit en Temps Réel :** Script Python interrogeant l'API Nova toutes les 2 secondes pendant 120 secondes.
- **Validation Incident :** Arrêt provoqué (`openstack server stop cirros-instance`), détection immédiate de l'indisponibilité, redémarrage (`openstack server start`), temps de rétablissement mesuré de 18 secondes. **Conformité contractuelle validée (0 MAD de pénalité)**.

---

## 🛠️ Instructions pour Cloner et Utiliser le Projet

### 1. Cloner le Dépôt
```bash
git clone https://github.com/SALHI-1/cloud-computing-project.git
cd <cloud-computing-project>
```

### 2. Lancer la Simulation CloudSim Plus
```bash
cd partie1-simulation-cloudsim
mvn clean compile exec:java
```

### 3. Exécuter le Déploiement Terraform et Ansible
```bash
# Provisionner avec Terraform
cd partie3-iac-terraform-ansible/terraform
terraform init
terraform apply -auto-approve

# Configurer avec Ansible
cd ../ansible
ansible-playbook -i inventory.ini playbook.yml
```

### 4. Lancer la Supervision SLA
```bash
cd partie4-sla-monitoring
python3 sla_monitor.py --server cirros-instance --duration 120 --interval 2
```

---

## 📄 Rapport de Projet
Le rapport complet de 25 pages rédigé au format académique officiel est disponible directement dans le fichier [Salhi_Mohammed_Rapport.pdf](Salhi_Mohammed_Rapport.pdf). Il détaille l'intégralité des concepts théoriques, l'architecture, les captures d'écran annotées, les calculs de disponibilité et les fichiers de configuration.
