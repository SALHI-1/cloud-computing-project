#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import openstack
import subprocess
import time
import datetime
import csv
import json
import os
import sys
import argparse

CSV_FILE = 'sla_measures.csv'
SLA_FILE = 'sla.json'
REPORT_FILE = 'sla_report.json'

def parse_args():
    parser = argparse.ArgumentParser(description='Surveillance SLA de la disponibilite OpenStack')
    parser.add_argument('--interval', type=int, default=10, help='Intervalle entre les mesures en secondes (defaut: 10s pour le TP)')
    parser.add_argument('--server', type=str, default='cirros-instance', help='Nom de l instance a surveiller')
    return parser.parse_args()

def check_ping(ip):
    if not ip or ip == 'N/A':
        return False
    try:
        res = subprocess.run(['ping', '-c', '1', '-W', '1', ip], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        return res.returncode == 0
    except Exception:
        return False

def get_server_ip(server):
    if not server or not server.addresses:
        return 'N/A'
    for net_name, addrs in server.addresses.items():
        for addr in addrs:
            if addr.get('OS-EXT-IPS:type') == 'floating':
                return addr.get('addr')
    for net_name, addrs in server.addresses.items():
        for addr in addrs:
            return addr.get('addr')
    return 'N/A'

def main():
    args = parse_args()
    print('================================================================')
    print('SYSTEME DE SURVEILLANCE SLA - UNIVERSITE ABDELMALEK ESSAADI')
    print('Auteur : mohammed salhi (R134235437)')
    print(f'Instance cible      : {args.server}')
    print(f'Intervalle mesure   : {args.interval}s')
    print('Objectif SLA cible  : 99.5 % de disponibilite')
    print('Conseil             : Provoquez la panne dans un autre terminal avec :')
    print(f'                      openstack server stop {args.server}')
    print(f'                      puis openstack server start {args.server}')
    print('                      Appuyez sur Ctrl+C pour arreter et generer le rapport.')
    print('================================================================\n')

    print('Connexion a OpenStack (Keystone)...')
    try:
        conn = openstack.connect(cloud='devstack-admin')
        print('Connecte avec succes a OpenStack ! Debut des mesures :\n')
    except Exception as e:
        print(f'Erreur de connexion OpenStack : {e}')
        sys.exit(1)

    init_csv = not os.path.exists(CSV_FILE)
    with open(CSV_FILE, 'a', newline='') as f:
        writer = csv.writer(f)
        if init_csv:
            writer.writerow(['Horodatage', 'Instance', 'Statut_Nova', 'IP', 'Ping_OK', 'Etat_SLA', 'Disponibilite_Cumulee_%'])

    total_measures = 0
    available_measures = 0
    start_time = datetime.datetime.now()

    try:
        while True:
            now_str = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            try:
                server = conn.compute.find_server(args.server)
            except Exception:
                server = None

            if not server:
                nova_status = 'INTROUVABLE'
                ip = 'N/A'
                ping_ok = False
                is_available = False
            else:
                nova_status = server.status
                ip = get_server_ip(server)
                ping_ok = check_ping(ip)
                is_available = (nova_status == 'ACTIVE') and ping_ok

            total_measures += 1
            if is_available:
                available_measures += 1

            avail_pct = round((available_measures / total_measures) * 100, 2)
            etat_str = 'DISPONIBLE' if is_available else 'INDISPONIBLE (PANNE)'
            color = '\033[92m' if is_available else '\033[91m'
            reset = '\033[0m'

            print(f'[{now_str}] Instance: {args.server} | Nova: {nova_status:7} | IP: {ip:13} | Ping: {str(ping_ok):5} | Etat: {color}{etat_str:20}{reset} | SLA: {avail_pct}%')

            with open(CSV_FILE, 'a', newline='') as f:
                writer = csv.writer(f)
                writer.writerow([now_str, args.server, nova_status, ip, ping_ok, 'DISPONIBLE' if is_available else 'INDISPONIBLE', avail_pct])

            time.sleep(args.interval)

    except KeyboardInterrupt:
        print('\n\nArret de la surveillance. Calcul et generation du rapport final...')

    end_time = datetime.datetime.now()
    duration_sec = int((end_time - start_time).total_seconds())
    final_avail = round((available_measures / total_measures) * 100, 2) if total_measures > 0 else 0
    sla_met = final_avail >= 99.5

    report = {
        'rapport_surveillance': {
            'etudiant': 'mohammed salhi (R134235437)',
            'date_debut': start_time.strftime('%Y-%m-%d %H:%M:%S'),
            'date_fin': end_time.strftime('%Y-%m-%d %H:%M:%S'),
            'duree_secondes': duration_sec,
            'instance_surveillee': args.server,
            'nombre_total_mesures': total_measures,
            'mesures_disponibles': available_measures,
            'mesures_en_echec': total_measures - available_measures,
            'disponibilite_obtenue_pct': final_avail,
            'objectif_sla_pct': 99.5,
            'objectif_respecte': sla_met,
            'conclusion': 'Objectif SLA respecte (>= 99.5 %)' if sla_met else 'Objectif SLA NON respecte (panne detectee, disponibilite en baisse)'
        }
    }

    with open(REPORT_FILE, 'w') as f:
        json.dump(report, f, indent=2)

    try:
        with open(SLA_FILE, 'r') as f:
            data = json.load(f)
        data['rapport_surveillance_final'] = report['rapport_surveillance']
        with open(SLA_FILE, 'w') as f:
            json.dump(data, f, indent=2)
    except Exception as e:
        pass

    print('\n===================== RAPPORT DE SURVEILLANCE SLA =====================')
    print('Etudiant                 : mohammed salhi (R134235437)')
    print(f'Periode de mesure        : {start_time.strftime("%H:%M:%S")} a {end_time.strftime("%H:%M:%S")} ({duration_sec}s)')
    print(f'Nombre total de mesures  : {total_measures}')
    print(f'Mesures disponibles      : {available_measures}')
    print(f'Mesures en panne         : {total_measures - available_measures}')
    print(f'Taux de disponibilite    : {final_avail} %')
    print(f'Objectif SLA (99.5 %)    : {"[RESPECTE]" if sla_met else "[NON RESPECTE - PANNE DETECTEE]"}')
    print(f'Fichiers generes         : {CSV_FILE}, {REPORT_FILE}, {SLA_FILE}')
    print('========================================================================')

if __name__ == '__main__':
    main()
