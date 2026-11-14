# 2. Conception de l'Architecture Cloud Universitaire (UAE)
**Auteur :** Mohammed Salhi (R134235437)  
**Projet :** Cloud Intelligence — Partie 1  

---

### A. Modèles de Déploiement et de Service

Pour concilier la protection des données institutionnelles et l'absorption des pics de charge saisonniers, l'architecture retenue est un **Cloud Hybride** structuré comme suit :

1. **Modèle de Déploiement : Cloud Hybride**
   * **Cloud Privé Souverain :** Déployé sur l'infrastructure matérielle interne de l'université. Il héberge en permanence l'ensemble des données sensibles (dossiers scolaires Apogée, notes confidentielles, sujets d'examens, identités des 130 000 étudiants).
   * **Cloud Public Débordé (*Cloud Bursting*) :** Sollicité de manière dynamique et temporaire uniquement lors des périodes de pointe (inscriptions massives de septembre, sessions d'examens). Ce débordement est géré par la règle de mise à l'échelle horizontale (`HorizontalVmScaling`), sans compromettre la sécurité des données internes.

2. **Modèles de Service :**
   * **SaaS (Software as a Service) :** Utilisé pour les services consommés directement par les usagers (étudiants, enseignants) sans gestion d'infrastructure : portail ENT, plateforme Moodle, messagerie universitaire et examens en ligne.
   * **PaaS (Platform as a Service) :** Utilisé pour l'hébergement des moteurs de bases de données relationnelles (SGBD Apogée) avec gestion automatisée des sauvegardes et du partitionnement.
   * **IaaS (Infrastructure as a Service) :** Réservé aux chercheurs et aux administrateurs de la DSI pour déployer des instances virtuelles complètes (accès root, OS personnalisé) pour le calcul intensif et les laboratoires de TP.

---

### B. Emplacement et Rôle des Datacenters

L'université met en place **deux Datacenters physiques interconnectés** complétés par un tiers public :

* **Datacenter 1 (Campus Tanger - Boukhalef) :**
  Datacenter principal hébergeant les services pour le pôle occidental (Tanger, Larache, Ksar El-Kébir).
* **Datacenter 2 (Campus Tétouan - Martil) :**
  Datacenter régional hébergeant les services pour le pôle oriental (Tétouan, Al Hoceïma) et assurant le rôle de site de secours pour le Plan de Reprise d'Activité (PRA).
* **Datacenter Public (Cloud Provider tiers) :**
  Datacenter externe activé à la demande pour absorber les requêtes web excédentaires lors des pics critiques.

> **Justification :** Cette implantation bivalente minimise la latence pour les usagers des deux métropoles universitaires et garantit une résilience totale : si le Datacenter de Tanger venait à subir un incident électrique ou réseau majeur, le trafic est instantanément redirigé vers Tétouan via une liaison fibre optique dédiée de 10 Gbps.

---

### C. Dimensionnement des Serveurs Physiques (Hôtes)

* **Nombre d'hôtes dans le Cloud Privé :** **12 serveurs physiques** au total (6 serveurs à Tanger et 6 serveurs à Tétouan).
* **Spécifications par serveur physique (Bi-Xeon Enterprise) :**
  * **Processeurs :** 2 CPU Intel Xeon Gold (16 cœurs chacun) = **32 cœurs physiques (PEs)** cadencés à **2 600 MIPS** par cœur (soit 83 200 MIPS par hôte).
  * **Mémoire vive (RAM) :** **128 Go** DDR4 ECC (131 072 Mo).
  * **Stockage :** **12 To** de stockage NVMe haute performance (12 582 912 Mo).
  * **Réseau :** Carte réseau double port **10 Gbps** redondée.
* **Capacité brute totale du Cloud Privé :**
  $$\text{Cœurs Physiques Totaux} = 12 \times 32 = 384\text{ cœurs (PEs)}$$
  $$\text{Mémoire RAM Totale} = 12 \times 128\text{ Go} = 1\,536\text{ Go RAM}$$
  $$\text{Puissance Totale} = 384 \times 2\,600 = 998\,400\text{ MIPS}$$

---

### D. Répartition des Machines Virtuelles (VMs de base en période normale)

En régime régulier de fonctionnement, le Cloud Privé héberge **48 Machines Virtuelles** dimensionnées selon la criticité des services :

| Service Applicatif | Modèle | Nb VMs | Cœurs / VM | RAM / VM | Total Cœurs | Rôle et Confinement |
| :--- | :---: | :---: | :---: | :---: | :---: | :--- |
| **Bases de Données (Apogée)** | PaaS | **8** | 8 | 32 Go | 64 | Données critiques scolarité (4 Tanger + 4 Tétouan avec réplication). **100% privé**. |
| **E-learning (Moodle)** | SaaS | **16** | 4 | 16 Go | 64 | Consultation de cours, streaming vidéo, dépôt de devoirs. |
| **Scolarité & ENT** | SaaS | **6** | 4 | 8 Go | 24 | Inscriptions de routine, consultation des notes et attestations. |
| **Examens en ligne** | SaaS | **4** | 4 | 8 Go | 16 | QCM réguliers et devoirs de contrôle continu. |
| **Calcul Recherche / DSI** | IaaS | **8** | 4 | 16 Go | 32 | Simulations scientifiques et serveurs métiers de la DSI. |
| **Messagerie & Bibliothèque** | SaaS | **6** | 2 | 4 Go | 12 | Webmail institutionnel et accès aux articles scientifiques. |
| **TOTAL SOCLE** | — | **48** | — | — | **212 cœurs** | Consommation socle de routine dans le Cloud Privé. |

---

### E. Politique d'Élasticité, Seuil de Débordement et Disaster Recovery

La stratégie d'ingénierie adoptée garantit une étanchéité stricte contre l'effondrement des hyperviseurs :

1. **Seuil de sécurité CPU (80 %) :**
   Pour préserver une marge de sécurité opérationnelle et autoriser le basculement d'urgence d'un serveur défaillant (PRA), le Cloud Privé ne doit jamais dépasser **80 % d'utilisation processeur**, soit un plafond autorisé de :
   $$\text{Plafond Privé} = 384 \times 0.80 = 307\text{ cœurs}$$
   Les **77 cœurs restants (20 %)** constituent la réserve intouchable de haute disponibilité.

2. **Capacité locale d'auto-scaling :**
   En période normale, les 48 VMs socles consomment **212 cœurs**. L'espace libre disponible pour absorber les hausses de trafic dans le privé est de :
   $$\text{Marge Locale} = 307 - 212 = 95\text{ cœurs} \quad (\approx 23\text{ VMs de 4 cœurs})$$

3. **Mécanisme de Débordement Élastique (*Cloud Bursting*) :**
   Lors du pic massif des inscriptions de rentrée, la demande additionnelle est de **+50 VMs ENT (200 cœurs)** :
   * L'orchestrateur déploie d'abord **23 VMs ENT dans le Cloud Privé** (atteignant 71 VMs privées et 304 cœurs, restant strictement sous le seuil critique de 307 cœurs).
   * Les **27 VMs excédentaires (108 cœurs)** sont automatiquement provisionnées sur le **Cloud Public**.
   * Le parc total atteint alors **98 VMs (71 privées et 27 publiques)**.
   * Les bases de données contenant les informations des étudiants restent confinées dans le Cloud Privé à 100 %.

---

### F. Schéma Architectural

Le schéma architectural complet a été généré sous Graphviz et se trouve dans le fichier :
**`uae_cloud_architecture.png`** (résolution : $2100 \times 1434$ pixels).

Ce schéma illustre les couches utilisateurs régionales, le répartiteur de charge haute disponibilité, les 12 serveurs physiques répartis sur Tanger et Tétouan, la liaison inter-datacenter de PRA, ainsi que le bus de débordement vers le Cloud Public.
