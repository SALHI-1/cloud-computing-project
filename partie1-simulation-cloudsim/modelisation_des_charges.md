# 3. Modélisation des Charges Applicatives (Cloudlets)
**Auteur :** Mohammed Salhi (R134235437)  
**Projet :** Cloud Intelligence — Partie 1  

---

Dans le simulateur **CloudSim Plus**, une requête ou une tâche de calcul soumise par un usager est formalisée par un objet `Cloudlet`, caractérisé par sa longueur d'exécution en **MI** (*Millions d'Instructions*), son empreinte mémoire et le nombre de cœurs processeur alloués (`pesNumber`).

Pour évaluer la robustesse de l'architecture Cloud de l'UAE, deux scénarios contrastés de charge de travail sont modélisés :

---

### A. Période Normale (Semestre d'Enseignement Standard)

Durant les semaines ordinaires de cours, le trafic est régulier, dispersé tout au long de la journée et composé majoritairement de consultations web légères.

* **Volume de requêtes simultanées :** Environ 10 000 à 15 000 usagers actifs simultanément (soit ~10 % de la communauté universitaire connectée).
* **Représentation dans CloudSim Plus :** **100 Cloudlets** distribués sur les 48 VMs socles.
* **Modèle d'utilisation des ressources :** `UtilizationModelStochastic`, simulant les variations aléatoires naturelles du trafic web réel.
* **Répartition détaillée des 100 Cloudlets :**
  1. **E-learning Moodle (SaaS - 30 Cloudlets) :** Consultation de polycopiés (PDF), diapositives et lecture de forums (4 000 à 8 000 MI, 1 vCPU).
  2. **Portail Scolarité & ENT (SaaS - 20 Cloudlets) :** Vérification des emplois du temps et consultation des états de scolarité (2 000 à 4 000 MI, 1 vCPU).
  3. **Messagerie & Bibliothèque (SaaS - 20 Cloudlets) :** Échanges de courriels académiques et recherches documentaires (2 000 à 5 000 MI, 1 vCPU).
  4. **Bases de Données Relationnelles (PaaS - 15 Cloudlets) :** Requêtes SQL SELECT de consultation de dossiers étudiants (5 000 à 10 000 MI, 2 vCPUs).
  5. **Calcul Scientifique & DSI (IaaS - 10 Cloudlets) :** Traitements de recherche et scripts d'analyse de données (20 000 à 40 000 MI, 4 vCPUs).
  6. **Examens & QCM en ligne (SaaS - 5 Cloudlets) :** Tests d'entraînement et devoirs réguliers (1 000 à 2 000 MI, 1 vCPU).

---

### B. Période de Pointe (Rentrée Universitaire — Inscriptions Massives de Septembre)

Ce scénario modélise la contrainte opérationnelle la plus sévère subie annuellement par le Cloud universitaire de l'UAE : **la semaine de rentrée et d'inscription administrative des 130 000 étudiants sur la plateforme Apogée/ENT**.

* **Volume de requêtes simultanées :** Plus de 50 000 connexions concurrentes, provoquant l'engorgement immédiat des serveurs applicatifs de base et déclenchant l'auto-scaling horizontal (`HorizontalVmScaling`) et le débordement public (*Cloud Bursting*).
* **Représentation dans CloudSim Plus :** **801 Cloudlets** injectés de manière échelonnée.
* **Modèle d'utilisation des ressources :** `UtilizationModelFull`, simulant la saturation instantanée des cœurs processeur des machines virtuelles réceptrices.
* **Structure du plan de charge (801 Cloudlets) :**
  1. **Inscriptions & ENT (SaaS - 680 Cloudlets critiques) :**
     * **Vague initiale (t = 0 s) :** 140 Cloudlets courts (3 000 à 5 000 MI, 1 vCPU) injectés brutalement sur les 6 VMs ENT de base pour saturer le CPU (> 50 %) et déclencher l'auto-scaling.
     * **Vagues échelonnées (t > 0 s) :** 6 vagues successives de 90 Cloudlets (540 requêtes) injectées à intervalle régulier de 2 secondes au fur et à mesure du provisionnement des nouvelles VMs locales et publiques.
  2. **Bases de Données Apogée (PaaS - 60 Cloudlets transactionnels) :**
     * Écritures SQL lourdes (INSERT / UPDATE) : validation d'inscriptions, choix des filières et affectation des bourses (20 000 à 30 000 MI, 2 vCPUs).
     * 25 requêtes initiales puis 6 requêtes par vague échelonnée, absorbées à 100 % dans le Cloud Privé pour respecter la souveraineté des données.
  3. **Trafic de fond résiduel du campus (61 Cloudlets) :**
     * *Moodle :* 25 Cloudlets (45 000 à 75 000 MI, 1 vCPU).
     * *Messagerie :* 20 Cloudlets (10 000 à 12 000 MI, 1 vCPU).
     * *Examens & QCM :* 10 Cloudlets (2 000 à 3 000 MI, 1 vCPU).
     * *Recherche DSI :* 5 Cloudlets (280 000 MI, 4 vCPUs).

---

### C. Stratégie d'Ordonnancement et d'Allocation Processeur

1. **Parallélisme par thread (pesNumber) :**
   Dans une architecture web applicative moderne, une requête HTTP d'usager (soumission d'un formulaire ENT, clic QCM) est traitée par un thread indépendant sur **1 seul cœur virtuel (1 vCPU)**. Une machine virtuelle à 4 vCPUs peut ainsi exécuter 4 requêtes web simultanément en parallèle. Seules les transactions de bases de données (2 vCPUs pour gérer les verrous et les journaux de transaction) et les calculs scientifiques de recherche (4 vCPUs) exploitent le parallélisme multi-cœurs.
2. **Politique d'Ordonnancement (CloudletScheduler) :**
   * En `CloudletSchedulerSpaceShared` (FIFO exclusif), chaque requête monopolise un cœur jusqu'à son terme, provoquant des files d'attente bloquantes massives pour les autres étudiants.
   * En `CloudletSchedulerTimeShared` (temps partagé / Round-Robin), l'hyperviseur alloue des quanta de temps CPU à l'ensemble des requêtes, garantissant un démarrage immédiat du traitement pour chaque étudiant.
