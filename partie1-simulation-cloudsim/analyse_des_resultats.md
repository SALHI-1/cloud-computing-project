# 5. Analyse des Résultats de Simulation et Conclusion Générale
**Étudiant :** Mohammed Salhi (CNE : R134235437)  
**Institution :** Université Abdelmalek Essaâdi (UAE)  
**Projet :** Cloud Intelligence — Partie 1  

---

Ce document présente l'évaluation quantitative et qualitative des performances de l'architecture Cloud de l'Université Abdelmalek Essaâdi, répondant précisément à la question posée dans l'énoncé du projet :
> *« Analyser les résultats : temps de réponse, temps d'exécution, taux d'utilisation des hôtes, coût. Conclure : l'architecture proposée suffit-elle en période de pointe ? »*

---

## 1. Tableau Récapitulatif des Résultats de Simulation

Les expérimentations ont été menées sur le moteur de référence **CloudSim Plus (v8.0.0)** en injectant les plans de charge modélisés dans `modelisation_des_charges.md` sur l'infrastructure hybride définie dans `architecture_uae.md`.

| Scénario / Configuration | Ordonnanceur | Politique Alloc. VM | Makespan (s) | Temps Rép. Moy. (s) | Temps Attente (s) | CPU Hôtes (%) | VMs Créées (Privé / Public) | Tâches Réussies | Coût Total ($) |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **1. Période Normale (Référence)** | `TimeShared` | `Simple` (First-Fit) | 44.34 s | 6.70 s | 0.00 s | 2.8 % | 48 (48 / 0) | 100 / 100 | $694.73 |
| **2. Période de Pointe (Statique)** | `TimeShared` | `Simple` (First-Fit) | 108.12 s | 25.77 s | 0.00 s | 8.5 % | 48 (48 / 0) | 801 / 801 | $856.98 |
| **3. Période de Pointe (Statique)** | `SpaceShared` | `Simple` (First-Fit) | 108.12 s | 19.11 s | 10.35 s | 8.5 % | 48 (48 / 0) | 801 / 801 | $856.98 |
| **4. Période Normale (BestFit)** | `TimeShared` | `BestFit` | 40.75 s | 6.59 s | 0.00 s | 3.5 % | 48 (48 / 0) | 100 / 100 | $685.57 |
| **5. Période de Pointe (Élastique)** | `TimeShared` | `BestFit` | **108.01 s** | **10.30 s** | **0.00 s** | **8.2 %** | **98 (71 / 27)** | **801 / 801** | **$3 520.04** |

---

### Focus sur le Service Critique : Inscriptions & ENT Apogée (680 Cloudlets)

* **En Période Normale (48 VMs) :** Temps de réponse moyen = **2.39 s** (navigation web fluide, chargement immédiat des pages de scolarité).
* **En Pointe Statique (48 VMs - 6 VMs ENT) :** Temps de réponse moyen = **27.21 s** (latence multipliée par plus de **11×**, temps d'attente inacceptable provoquant des abandons de formulaires par les étudiants).
* **En Pointe Élastique (98 VMs - 56 VMs ENT) :** Temps de réponse moyen = **8.98 s** (chute spectaculaire de **67 %** du temps de réponse grâce à l'apport des 50 VMs additionnelles et du débordement public).

---

## 2. Analyse Approfondie des Métriques

### A. Temps de Réponse (Response Time) & Temps d'Attente (Waiting Time)

Le temps de réponse mesure la durée totale écoulée entre l'envoi de la requête par l'usager et la réception de la confirmation finale :

1. **Impact de l'élasticité horizontale :**
   * Sous une infrastructure statique (Scénario 2), le goulot d'étranglement est sévère : 680 soumissions d'inscription affluent sur seulement 6 serveurs ENT de base, étirant le temps de réponse moyen à **25.77 s** au global et **27.21 s** sur l'ENT.
   * Avec l'auto-scaling horizontal (`HorizontalVmScaling`) et le *Cloud Bursting* (Scénario 5), le cluster ENT passe dynamiquement de 6 à 56 machines virtuelles. Le temps de réponse moyen retombe à **8.98 s** pour les inscriptions, un niveau optimal sous une charge extrême de 50 000 connexions concurrentes.

2. **Comparaison des politiques d'ordonnancement (`TimeShared` vs `SpaceShared`) :**
   * En `CloudletSchedulerSpaceShared` (Scénario 3), les cœurs processeur sont réservés de manière exclusive selon une file FIFO. Dès que les 6 VMs ENT sont saturées, les requêtes suivantes sont bloquées en file d'attente : le temps d'attente explose à **10.35 s en moyenne** et **12.13 s sur l'ENT**, se traduisant par un écran figé pour les étudiants.
   * En `CloudletSchedulerTimeShared` (Scénarios 1, 2, 4, 5), le temps CPU est partagé équitablement par tranches d'horloge. Le temps d'attente est de **0.00 s**, offrant une réactivité immédiate dès le premier clic. Pour des services web universitaires interactifs, la politique `TimeShared` s'impose donc comme le choix technique incontournable.

---

### B. Durée Globale d'Exécution (Makespan)

Le Makespan représente l'instant où la toute dernière requête du plan de charge est achevée :

* **Période Normale ($\approx$ 40 à 44 s) :** Les 100 requêtes de fonctionnement courant sont évacuées avec fluidité, confirmant que le dimensionnement de base est optimal en dehors des périodes d'inscription.
* **Période de Pointe ($\approx$ 108 s) :** Ce délai reflète la durée complète de la simulation avec l'injection échelonnée de 6 vagues successives d'inscriptions et de transactions SQL. L'infrastructure absorbe la totalité du pic sans laisser aucune tâche en souffrance.

---

### C. Taux d'Utilisation des Serveurs Hôtes (CPU & RAM)

* **Régime Normal (2.8 % à 3.5 %) :**
  La puissance physique installée (12 serveurs $\times$ 32 cœurs = 384 cœurs physiques, soit 998 400 MIPS) offre une marge thermique et énergétique remarquable.
* **Régime de Pointe (8.2 % à 8.5 %) :**
  L'activité moyenne sur les 12 serveurs privés monte à 8.5 %, avec des pics de saturation localisés sur les hôtes hébergeant les VMs de scolarité et les bases de données relationnelles.
* **Comparaison `VmAllocationPolicySimple` vs `VmAllocationPolicyBestFit` :**
  * `Simple` (First-Fit) alloue chaque machine virtuelle sur le premier serveur physique disposant de capacités suffisantes, dispersant les VMs sur les 12 hôtes.
  * `BestFit` concentre le placement sur l'hôte physique laissant le moins de ressources résiduelles. Cette stratégie maximise la consolidation, réduit la consommation énergétique globale et libère des serveurs physiques complets pouvant être basculés en veille prolongée hors des périodes de pointe.

---

### D. Analyse Financière et Rentabilité Économique

Le modèle de facturation sépare les coûts internes fixes/d'amortissement du Cloud Privé des coûts variables à la demande du Cloud Public :

$$\text{Coût Total} = \text{Coût Cloud Privé} + \text{Coût Cloud Public (Bursting)}$$

| Scénario | Coût Privé ($) | Coût Public ($) | Coût Total ($) | Remarque Stratégique |
| :--- | :---: | :---: | :---: | :--- |
| **Période Normale (48 VMs)** | $685.57 | $0.00 | **$685.57** | 100 % hébergé en interne, aucun coût externe. |
| **Pointe Statique (48 VMs)** | $856.98 | $0.00 | **$856.98** | Saturation majeure, dégradation du service aux étudiants. |
| **Pointe Élastique (98 VMs)** | $1 124.32 | $2 395.72 | **$3 520.04** | Débordement temporaire de 27 VMs sur Cloud Public. |

> **Arbitrage économique (Capex vs Opex) :**  
> L'investissement matériel (Capex) pour acheter, héberger et climatiser 27 serveurs physiques supplémentaires à l'année coûterait plus de **80 000 $**, alors que ces serveurs resteraient inactifs 11 mois sur 12.  
> À l'inverse, recourir au Cloud Bursting public durant la seule semaine des inscriptions représente une dépense opérationnelle (Opex) maîtrisée de quelques milliers de dollars, ce qui constitue la décision de gestion la plus rationnelle pour les finances de l'université.

---

### E. Souveraineté Numérique et Sécurité des Données

Conformément à la directive d'architecture implémentée dans le `DatacenterMapper` :
* **Bases de Données (PaaS - 8 VMs) :** Hébergent les dossiers personnels, notes et données confidentielles des 130 000 étudiants. Elles sont **strictement confinées dans les Datacenters privés de Tanger et Tétouan**. Aucun débordement externe n'est autorisé pour les données sensibles.
* **Serveurs Web Frontaux (SaaS) :** Seules les machines virtuelles applicatives sans état (formulaires web ENT) sont déportées sur le Cloud Public. En cas d'incident de sécurité sur une instance publique, aucune donnée de base n'est compromise.

---

## 3. Conclusion Générale : L'Architecture Proposée Suffit-elle en Période de Pointe ?

À la question posée dans l'énoncé du projet, l'analyse des résultats permet de formuler une réponse technique claire et argumentée :

### 1. En Cloud Privé isolé (sans débordement) : **NON, ELLE NE SUFFIT PAS.**
* Le Cloud Privé de l'UAE dispose d'une capacité physique totale de **384 cœurs processeur**.
* Pour garantir la tolérance aux pannes (Disaster Recovery / PRA), un seuil de sécurité strict de **80 % (307 cœurs max)** a été configuré.
* Les services de base consomment déjà **212 cœurs** en période normale (48 VMs).
* La marge d'élasticité locale disponible avant franchissement du seuil critique n'est que de **95 cœurs** (soit environ **23 VMs** de 4 cœurs).
* Or, le pic massif des inscriptions de rentrée nécessite la création de **50 nouvelles VMs ENT (200 cœurs additionnels)**. L'infrastructure locale seule est donc mathématiquement incapable d'absorber la charge sans saturer ses hôtes à plus de 100 %, entraînant des temps de réponse supérieurs à **27 secondes** ou l'écroulement des services.

### 2. Avec l'architecture Cloud Hybride adoptée (Cloud Bursting) : **OUI, PARFAITEMENT.**
* Le système d'auto-scaling sature intelligemment les capacités locales disponibles jusqu'au seuil de 80 %, déployant **23 VMs ENT dans le Cloud Privé** (portant le parc local à 71 VMs et 304 cœurs).
* Les **27 VMs restantes (108 cœurs)** sont automatiquement instanciées sur le **Cloud Public**.
* **Bilan final :**
  1. **Disponibilité :** **100 % des 801 requêtes** sont traitées avec succès, aucun échec.
  2. **Performance :** Le temps de réponse moyen pour les inscriptions passe de 27.21 s à **8.98 s** (gain de performance de **67 %**).
  3. **Haute Disponibilité :** La réserve de sécurité de 20 % (77 cœurs) reste préservée pour parer à une panne éventuelle d'un hôte ou d'un datacenter.
  4. **Souveraineté :** L'intégrité et la confidentialité des dossiers étudiants sont totalement préservées au sein des campus de Tanger et Tétouan.
