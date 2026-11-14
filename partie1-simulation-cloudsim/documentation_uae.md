# Partie 1 : Simulation d'un Cloud Universitaire avec CloudSim Plus
**Étudiant :** Mohammed Salhi (CNE : R134235437)  
**Institution :** Université Abdelmalek Essaâdi (UAE)  
**Encadrant :** Prof. C. EL AMRANI  

---

## 1. Documentation sur l'Université Abdelmalek Essaâdi (UAE)

Pour concevoir et dimensionner une infrastructure de Cloud Computing réaliste, performante et adaptée aux besoins de l'Université Abdelmalek Essaâdi (UAE), une étude approfondie de l'envergure de l'institution, de son implantation géographique régionale et de son catalogue de services numériques a été menée.

---

### A. Indicateurs Clés & Démographie Universitaire

L'Université Abdelmalek Essaâdi représente le pôle universitaire majeur de la région Nord du Royaume du Maroc. Les données démographiques recueillies pour calibrer les capacités de notre infrastructure Cloud sont les suivantes :

* **Population étudiante :** **130 750 étudiants** inscrits dans l'ensemble des cycles (Licence, Master, Ingénierie, Doctorat, Médecine, Commerce).
* **Corps professoral :** **1 357 enseignants-chercheurs** permanents.
* **Personnel administratif et technique :** Environ **600 cadres et techniciens** répartis sur les différents campus.
* **Volume d'utilisateurs potentiels :** Plus de **132 700 usagers** ayant un compte actif sur le système d'information de l'université.

> **Sources officielles :**
> 1. Portail institutionnel officiel de l'Université Abdelmalek Essaâdi : rubrique *« Chiffres Clés & Établissements »* ([www.uae.ac.ma](https://www.uae.ac.ma)).
> 2. Données statistiques du Ministère de l'Enseignement Supérieur, de la Recherche Scientifique et de l'Innovation (MESRSI).

---

### B. Répartition Géographique & Établissements Rattachés

Le réseau informatique de l'UAE doit couvrir une zone géographique très étendue (Région Tanger-Tétouan-Al Hoceïma). L'université regroupe **21 établissements d'enseignement supérieur** implantés dans **5 villes** :

1. **Pôle Universitaire de Tanger (7 établissements) :**
   * Faculté des Sciences et Techniques (FST Tanger - Boukhalef)
   * École Nationale des Sciences Appliquées (ENSA Tanger)
   * École Nationale de Commerce et de Gestion (ENCG Tanger)
   * Faculté de Médecine et de Pharmacie (FMPT)
   * Faculté des Sciences Juridiques, Économiques et Sociales (FSJES Tanger)
   * Faculté d'Économie et de Gestion (FEG)
   * École Supérieure Roi Fahd de Traduction (ESRFT)

2. **Pôle Universitaire de Tétouan / Martil (7 établissements) :**
   * Faculté des Sciences de Tétouan (FS Tétouan)
   * Faculté des Lettres et des Sciences Humaines (FLSH Martil)
   * Faculté des Sciences Juridiques, Économiques et Sociales (FSJES Tétouan)
   * École Normale Supérieure (ENS Martil)
   * École Nationale des Sciences Appliquées (ENSA Tétouan)
   * École Supérieure de Technologie (EST Tétouan)
   * Formation Ouverte et à Distance (FOD)

3. **Pôle Régional d'Al Hoceïma (5 établissements) :**
   * École Nationale des Sciences Appliquées (ENSA Al Hoceïma)
   * Faculté des Sciences et Techniques (FST Al Hoceïma)
   * Faculté des Sciences Juridiques, Économiques et Sociales (FSJES Al Hoceïma)
   * École Supérieure de Technologie (EST Al Hoceïma)
   * École Nationale de Commerce et de Gestion (ENCG Al Hoceïma)

4. **Pôle de Larache (1 établissement) :**
   * Faculté Polydisciplinaire de Larache (FPL)

5. **Pôle de Ksar El-Kébir (1 établissement) :**
   * Faculté Polydisciplinaire de Ksar El-Kébir (FPK)

---

### C. Catalogue des Services Numériques (Actuels et Potentiels)

Les applications hébergées sur le Cloud universitaire sont réparties selon les modèles de service du NIST (**SaaS**, **PaaS**, **IaaS**) :

* **Portail Scolarité & Inscriptions Apogée / ENT (SaaS) :**
  Système central gérant les dossiers administratifs, la saisie des notes, les réinscriptions et la délivrance des attestations. Ce service subit un afflux massif et soudain en début d'année universitaire (septembre/octobre) et lors de la publication semestrielle des résultats d'examens.
* **Plateforme Pédagogique E-learning Moodle (SaaS) :**
  Accessible via `e-learning.uae.ac.ma`, elle permet la diffusion des supports de cours (PDF, vidéos), les devoirs en ligne et les forums d'échange. Elle génère une charge continue avec des pics avant les sessions d'examens.
* **Plateforme d'Examens & QCM Sécurisés en ligne (SaaS - Potentiel) :**
  Mise en place d'un environnement d'évaluation numérique pour le passage des contrôles continus et des tests d'admission. Ce service critique exige des temps de réponse ultra-courts (< 3 s) et une très haute disponibilité.
* **Messagerie Institutionnelle & Collaboration (SaaS) :**
  Comptes académiques `@etu.uae.ac.ma` pour les étudiants et `@uae.ac.ma` pour les enseignants-chercheurs, couplés à des outils collaboratifs.
* **Bibliothèque Numérique Universitaire (SaaS/PaaS) :**
  Accès distant aux bases documentaires scientifiques (IEEE, ScienceDirect, Scopus) et au catalogue des thèses de l'UAE.
* **Plateforme de Gestion de Bases de Données relationnelles (PaaS) :**
  Instances de bases de données (PostgreSQL / Oracle) abritant les données hautement sensibles de la scolarité et de la gestion administrative.
* **Infrastructures Virtuelles de Recherche & TP (IaaS) :**
  Fourniture de machines virtuelles Linux/Windows à la demande pour les départements d'informatique, les laboratoires de recherche et les écoles d'ingénieurs (ENSA, FST) pour exécuter des calculs scientifiques et des travaux pratiques.

> **Sources :** Services de l'Espace Numérique de Travail (`ent.uae.ac.ma`), plateforme e-learning de l'UAE et référentiels nationaux de transformation numérique universitaire.
