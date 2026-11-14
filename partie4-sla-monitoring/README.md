# Partie 4 : Gestion des SLA et Supervision Avancée

**Auteur :** Mohammed Salhi  
**CNE / Identifiant :** R134235437  
**Établissement :** Université Abdelmalek Essaâdi (UAE)  

---

## 1. Description du Module

Ce module met en œuvre un système autonome de surveillance de la qualité de service (**Service Level Agreement - SLA**) pour les instances du Cloud OpenStack :

1. **Définition du Contrat SLA (`sla.json`) :**
   - Objectif de disponibilité (*Availability Target*) : $\ge 99.0\%$.
   - Temps maximal d'indisponibilité toléré (*Max Downtime Allowed*) : $\le 30$ secondes.
   - Temps moyen entre pannes minimal (*MTBF Target*) : $\ge 300$ secondes.
   - Temps moyen de réparation maximal (*MTTR Target*) : $\le 30$ secondes.
   - Pénalité financière contractuelle : $500$ MAD par minute de dépassement.

2. **Moteur de Surveillance (`sla_monitor.py`) :**
   - Interroge périodiquement l'état des machines virtuelles via l'API OpenStack Compute (Nova).
   - Teste la réactivité réseau et système des instances cibles.
   - Détecte en temps réel les pannes ou arrêts inattendus.
   - Enregistre chaque mesure dans `sla_measures.csv` (horodatage, statut, latence, disponibilité cumulée).
   - Calcule les indicateurs clés de fiabilité :
     - **Disponibilité Réelle ($A$)** : $\frac{T_{\text{up}}}{T_{\text{total}}} \times 100$
     - **MTBF** (*Mean Time Between Failures*) : $\frac{T_{\text{up}}}{\text{Nombre de pannes}}$
     - **MTTR** (*Mean Time To Repair*) : $\frac{T_{\text{down}}}{\text{Nombre de réparations}}$
   - Génère un rapport d'audit contractuel JSON finalisé (`sla_report.json`).

3. **Injection de Panne et Validation :**
   - Simulation d'un incident via l'arrêt volontaire de l'instance (`openstack server stop cirros-instance`).
   - Détection immédiate du passage à l'état `SHUTOFF` par le script de monitoring.
   - Redémarrage de l'instance (`openstack server start cirros-instance`), mesure de la durée de récupération ($18\text{ s} \le 30\text{ s}$), confirmation du respect des engagements SLA contractuels.

---

## 2. Structure du Dossier

```
partie4-sla-monitoring/
├── sla.json              # Définition des clauses et seuils contractuels SLA
├── sla_monitor.py        # Script Python de supervision et calcul des métriques
├── sla_measures.csv      # Historique des mesures périodiques enregistrées
├── sla_report.json       # Rapport final d'audit SLA généré
└── README.md             # Documentation et instructions d'exécution
```

---

## 3. Guide d'Exécution

### Exécution du Moniteur SLA
```bash
cd partie4-sla-monitoring

# Sourcer l'environnement OpenStack
source /opt/stack/devstack/openrc admin admin

# Lancer la surveillance (surveille par exemple cirros-instance pendant 120s avec un intervalle de 2s)
python3 sla_monitor.py --server cirros-instance --duration 120 --interval 2
```

### Injection d'une Défaillance (dans un terminal séparé)
```bash
# Arrêt forcé de l'instance pour tester la détection de panne
openstack server stop cirros-instance

# Attendre quelques secondes, puis rétablir le service
openstack server start cirros-instance
```

À la fin de la période d'échantillonnage, le moniteur affichera le résumé contractuel et générera `sla_report.json` et `sla_measures.csv`.
