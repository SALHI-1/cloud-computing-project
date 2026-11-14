# Partie 3 : Infrastructure as Code (Terraform & Ansible)

**Auteur :** Mohammed Salhi  
**CNE / Identifiant :** R134235437  
**Établissement :** Université Abdelmalek Essaâdi (UAE)  

---

## 1. Description du Module

Cette partie met en œuvre les principes de l'**Infrastructure as Code (IaC)** et de la **Gestion de Configuration Automatisée** :

1. **Provisionnement avec Terraform :**
   - Définition déclarative de l'infrastructure OpenStack via le provider officiel `terraform-provider-openstack`.
   - Création automatique du groupe de sécurité `secgroup-terraform` (autorisant ICMP, SSH sur le port 22, et HTTP sur le port 80).
   - Déploiement de l'instance `ubuntu-terraform` (Ubuntu 22.04 LTS, flavor `m1.saas`, rattachée à `iaas-net`).
   - Allocation et association dynamique de l'IP flottante (`172.24.4.195`).
   - Exportation automatique des adresses IP publique et privée via les `outputs`.

2. **Configuration Automatisée avec Ansible :**
   - Configuration d'un inventaire dynamique/statique pointant vers l'instance provisionnée.
   - Exécution idempotente d'un playbook Ansible (`playbook.yml`) :
     - Mise à jour du cache APT et installation du serveur web Nginx.
     - Déploiement d'une page d'accueil HTML responsive personnalisée aux couleurs de l'Université Abdelmalek Essaâdi.
     - Démarrage et activation du service `nginx` au démarrage système.
     - Validation automatisée de la réponse HTTP 200 via le module `uri`.

---

## 2. Structure du Dossier

```
partie3-iac-terraform-ansible/
├── terraform/
│   └── main.tf               # Déclaration HCL des ressources OpenStack (secgroup, instance, floating IP)
├── ansible/
│   ├── ansible.cfg           # Configuration SSH et options d'inventaire
│   ├── inventory.ini         # Inventaire définissant l'hôte cible [web]
│   └── playbook.yml          # Playbook Ansible pour l'installation et configuration de Nginx
└── README.md                 # Documentation et instructions d'exécution
```

---

## 3. Guide d'Exécution

### Étape 1 : Provisionnement Terraform
```bash
cd partie3-iac-terraform-ansible/terraform

# Initialisation du provider OpenStack
terraform init

# Vérification du plan d'exécution (7 ressources à créer)
terraform plan

# Déploiement de l'infrastructure
terraform apply -auto-approve

# Note : Pour détruire proprement l'infrastructure une fois les tests terminés :
# terraform destroy -auto-approve
```

### Étape 2 : Configuration Ansible
```bash
cd ../ansible

# Vérification de la connectivité avec l'hôte
ansible -i inventory.ini web -m ping

# Exécution du playbook de configuration Nginx
ansible-playbook -i inventory.ini playbook.yml

# Test de l'accès web
curl http://<INSTANCE_FLOATING_IP>
```
