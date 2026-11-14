# Partie 2 : Déploiement IaaS et SaaS sous OpenStack

**Auteur :** Mohammed Salhi  
**CNE / Identifiant :** R134235437  
**Établissement :** Université Abdelmalek Essaâdi (UAE)  

---

## 1. Description du Module

Cette partie couvre le déploiement manuel et scripté sur la plateforme OpenStack (DevStack Yoga) en deux étapes :

1. **Étape 1 — Déploiement IaaS (Infrastructure as a Service) :**
   - Création d'un réseau privé isolé `iaas-net` (`192.168.10.0/24`) et de son sous-réseau `iaas-subnet`.
   - Création et association d'un routeur virtuel `iaas-router` reliant `iaas-net` au réseau externe `public`.
   - Configuration d'un groupe de sécurité `iaas-secgroup` autorisant les flux ICMP (Ping) et TCP 22 (SSH).
   - Génération d'une paire de clés SSH `cirros-key`.
   - Instanciation d'une machine virtuelle CirrOS `cirros-instance` (`m1.tiny`).
   - Attribution et association d'une adresse IP flottante (`172.24.4.78`).
   - Validation de la connectivité réseau (Ping et session SSH interactive).

2. **Étape 2 — Déploiement SaaS (Software as a Service) :**
   - Création d'un gabarit de ressources personnalisé `m1.saas` (1 vCPU, 1536 Mo RAM, 10 Go disque).
   - Configuration d'un groupe de sécurité `saas-secgroup` ouvrant ICMP, SSH (port 22) et HTTP (port 80).
   - Lancement d'une instance Ubuntu 22.04 LTS `saas-instance` avec injection d'un script Cloud-Init (`user-data-saas.sh`).
   - Automatisation du déploiement d'une application web interactive « Cloud Intelligence » hébergée sur Flask et gérée via `systemd`.
   - Association d'une adresse IP flottante (`172.24.4.154`) et accès applicatif complet via navigateur web.

---

## 2. Commandes OpenStack CLI Exécutées

### Étape 1 : Déploiement IaaS (CirrOS)

```bash
# Sourcer l'environnement OpenStack
source /opt/stack/devstack/openrc admin admin

# 1. Création du réseau et du sous-réseau
openstack network create iaas-net
openstack subnet create --network iaas-net --subnet-range 192.168.10.0/24 --dns-nameserver 8.8.8.8 iaas-subnet

# 2. Création du routeur et rattachement de la passerelle publique
openstack router create iaas-router
openstack router set --external-gateway public iaas-router
openstack router add subnet iaas-router iaas-subnet

# 3. Groupe de sécurité (ICMP et SSH)
openstack security group create --description "Groupe de securite IaaS" iaas-secgroup
openstack security group rule create --protocol icmp iaas-secgroup
openstack security group rule create --proto tcp --dst-port 22 iaas-secgroup

# 4. Paire de clés SSH
openstack keypair create cirros-key > cirros-key.pem
chmod 600 cirros-key.pem

# 5. Lancement de l'instance CirrOS
openstack server create --flavor m1.tiny --image cirros-0.6.3-x86_64-disk \
  --network iaas-net --security-group iaas-secgroup --key-name cirros-key cirros-instance

# 6. Allocation et association d'une IP flottante
FLOATING_IP=$(openstack floating ip create public -f value -c floating_ip_address)
openstack server add floating ip cirros-instance $FLOATING_IP

# 7. Test de connectivité
ping -c 3 $FLOATING_IP
ssh -i cirros-key.pem cirros@$FLOATING_IP
```

---

### Étape 2 : Déploiement SaaS (Ubuntu + Flask)

```bash
# 1. Création de la flavor personnalisée
openstack flavor create --id auto --ram 1536 --disk 10 --vcpus 1 m1.saas

# 2. Groupe de sécurité SaaS (ICMP, SSH, HTTP)
openstack security group create --description "Secgroup SaaS HTTP et SSH" saas-secgroup
openstack security group rule create --protocol icmp saas-secgroup
openstack security group rule create --proto tcp --dst-port 22 saas-secgroup
openstack security group rule create --proto tcp --dst-port 80 saas-secgroup

# 3. Lancement de l'instance avec Cloud-Init
openstack server create --flavor m1.saas --image ubuntu-22.04 \
  --network iaas-net --security-group saas-secgroup --key-name cirros-key \
  --user-data user-data-saas.sh saas-instance

# 4. Allocation et association de l'IP flottante SaaS
SAAS_FIP=$(openstack floating ip create public -f value -c floating_ip_address)
openstack server add floating ip saas-instance $SAAS_FIP

# 5. Vérification du service HTTP
curl http://$SAAS_FIP
```

---

## 3. Fichiers du Dossier

```
partie2-openstack-iaas-saas/
├── user-data-saas.sh     # Script Cloud-Init installant Flask, l'application SaaS et le service Systemd
└── README.md             # Documentation et commandes CLI
```
