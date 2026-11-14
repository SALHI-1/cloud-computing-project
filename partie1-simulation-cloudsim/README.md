# Partie 1 : Simulation d'un Cloud Hybride Universitaire (CloudSim Plus)

**Auteur :** Mohammed Salhi  
**CNE / Identifiant :** R134235437  
**Établissement :** Université Abdelmalek Essaâdi (UAE)  

---

## 1. Description du Projet

Ce module implémente une simulation événementielle discrète d'une infrastructure cloud hybride pour l'Université Abdelmalek Essaâdi en utilisant le framework **CloudSim Plus**.

L'infrastructure modélise :
- Deux centres de données physiques privés (Datacenter Tanger et Datacenter Tétouan), hébergeant un total de **12 serveurs physiques (Hosts)**, **384 cœurs vCPU**, et **1536 Go de mémoire RAM**.
- Un centre de données public (Cloud Public AWS / Azure) utilisé pour le débordement automatique (*Cloud Bursting*).
- Deux politiques de charge :
  1. **Charge Normale (Hors Période d'Examens) :** 100 cloudlets distribués sur 48 VMs de base. Utilisation moyenne de ~25% de la capacité privée.
  2. **Pic de Charge (Période d'Examens / Inscriptions) :** 801 cloudlets simultanés. Dépassement du seuil de débordement fixé à 80% (307 cœurs).
- **Débordement Hybride Dynamique (*Cloud Bursting*) :**
  - Dès saturation des hôtes privés au-delà du seuil de 80%, le broker alloue 23 VMs supplémentaires sur les ressources privées restantes et déploie automatiquement 27 VMs dans le cloud public, absorbant 100% de la charge sans rejet.

---

## 2. Structure du Dossier

```
partie1-simulation-cloudsim/
├── pom.xml                        # Configuration Maven et dépendances CloudSim Plus
├── src/
│   └── main/java/org/cloudsimplus/
│       └── UAECloudSimulation.java# Code source principal de la simulation
├── uae_cloud_architecture.png     # Schéma d'architecture détaillé du Cloud Hybride UAE
├── generate_diagram.py            # Script Graphviz pour la génération du schéma
├── architecture_uae.md            # Spécifications de l'architecture matérielle
├── modelisation_des_charges.md    # Modélisation mathématique et scénarios de charge
├── analyse_des_resultats.md       # Analyse détaillée des métriques et résultats
└── README.md                      # Présentation et guide d'exécution
```

---

## 3. Prérequis et Exécution

### Prérequis
- Java JDK 11 ou supérieur
- Apache Maven 3.6+
- Python 3 et Graphviz (`dot`) pour la regénération du schéma (optionnel)

### Compilation et Exécution de la Simulation
```bash
cd partie1-simulation-cloudsim
mvn clean compile exec:java
```

### Regénération du Diagramme d'Architecture
```bash
python3 generate_diagram.py
```
Le fichier `uae_cloud_architecture.png` sera généré dans le répertoire courant.
