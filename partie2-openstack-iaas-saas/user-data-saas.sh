#!/bin/bash
set -e

# Mise à jour des paquets et installation des dépendances SaaS
apt-get update
DEBIAN_FRONTEND=noninteractive apt-get install -y python3-pip python3-flask python3-gunicorn

# Création du répertoire de l'application SaaS
mkdir -p /opt/saas-app

# Déploiement du code de la plateforme SaaS Cloud Intelligence
cat << 'APPEOF' > /opt/saas-app/app.py
from flask import Flask, render_template_string
import datetime, socket, platform

app = Flask(__name__)

services = [
    {"name": "Nextcloud Storage", "status": "Actif", "category": "Stockage Cloud", "badge": "success"},
    {"name": "Base de Données SaaS", "status": "Actif", "category": "Base de données", "badge": "success"},
    {"name": "Passerelle API Analytics", "status": "Actif", "category": "Intelligence Cloud", "badge": "info"},
    {"name": "Service de Surveillance SLA", "status": "Actif", "category": "Supervision", "badge": "primary"}
]

HTML_TEMPLATE = """
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Projet Cloud Intelligence - Plateforme SaaS</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f4f6f9; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
        .hero { background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); color: white; padding: 40px 0; margin-bottom: 30px; }
        .card { border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); margin-bottom: 24px; border: none; }
        .stat-card { text-align: center; padding: 20px; }
        .badge-cat { font-size: 0.8rem; }
    </style>
</head>
<body>
    <div class="hero shadow">
        <div class="container">
            <h1 class="display-5 fw-bold">🎓 Cloud Intelligence - Plateforme SaaS</h1>
            <p class="lead">Université Abdelmalek Essaâdi — Projet Cloud Computing (Partie 2 : Déploiement SaaS)</p>
            <span class="badge bg-light text-primary px-3 py-2 fw-semibold">Instance : {{ hostname }} | OS : {{ os_info }}</span>
        </div>
    </div>

    <div class="container">
        <div class="row">
            <div class="col-md-3">
                <div class="card stat-card text-primary">
                    <h3>4</h3>
                    <small class="text-muted text-uppercase fw-bold">Services Cloud Actifs</small>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card stat-card text-success">
                    <h3>99.9%</h3>
                    <small class="text-muted text-uppercase fw-bold">Disponibilité SLA</small>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card stat-card text-info">
                    <h3>OpenStack</h3>
                    <small class="text-muted text-uppercase fw-bold">Infrastructure IaaS</small>
                </div>
            </div>
            <div class="col-md-3">
                <div class="card stat-card text-warning">
                    <h3>Ubuntu 22.04</h3>
                    <small class="text-muted text-uppercase fw-bold">Image Cloud</small>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-lg-8">
                <div class="card p-4">
                    <h4 class="mb-3">🚀 Catalogue des Services SaaS Déployés</h4>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle">
                            <thead class="table-light">
                                <tr>
                                    <th>Nom du Service</th>
                                    <th>Catégorie</th>
                                    <th>Statut</th>
                                </tr>
                            </thead>
                            <tbody>
                                {% for s in services %}
                                <tr>
                                    <td class="fw-bold">{{ s.name }}</td>
                                    <td><span class="badge bg-secondary badge-cat">{{ s.category }}</span></td>
                                    <td><span class="badge bg-{{ s.badge }}">{{ s.status }}</span></td>
                                </tr>
                                {% endfor %}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card p-4">
                    <h4>ℹ️ Informations d'Exécution</h4>
                    <ul class="list-group list-group-flush mt-3">
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <strong>Date/Heure :</strong>
                            <span>{{ now }}</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <strong>Hôte VM :</strong>
                            <span>{{ hostname }}</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <strong>Flavor :</strong>
                            <span>m1.saas (1.5 Go RAM)</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <strong>Port d'accès :</strong>
                            <span class="badge bg-success">Port 80 (HTTP)</span>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
"""

@app.route('/')
def home():
    hostname = socket.gethostname()
    os_info = f"{platform.system()} {platform.release()}"
    now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    return render_template_string(HTML_TEMPLATE, services=services, hostname=hostname, os_info=os_info, now=now)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80)
APPEOF

# Création du service Systemd pour assurer l'exécution continue
cat << 'SVCEOF' > /etc/systemd/system/saas-app.service
[Unit]
Description=Cloud Intelligence SaaS Application
After=network.target

[Service]
User=root
WorkingDirectory=/opt/saas-app
ExecStart=/usr/bin/python3 /opt/saas-app/app.py
Restart=always

[Install]
WantedBy=multi-user.target
SVCEOF

# Activation et démarrage automatique
systemctl daemon-reload
systemctl enable saas-app.service
systemctl start saas-app.service
