package com.uae.cloud;

import java.util.ArrayList;
import java.util.List;

/**
 * Point d'entrée principal de la simulation CloudSim Plus pour l'UAE.
 * Conçu par : Mohammed Salhi (R134235437).
 *
 * Évalue et compare les politiques d'ordonnancement, d'allocation de ressources
 * et l'élasticité horizontale demandées dans le projet de Cloud Computing.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================================================================");
        System.out.println("   SIMULATION CLOUD COMPUTING — UNIVERSITÉ ABDELMALEK ESSAÂDI (UAE)");
        System.out.println("   Étudiant : Mohammed Salhi | CNE : R134235437");
        System.out.println("   Moteur : CloudSim Plus 8.0.0 | Topologie : Tanger (6H) + Tétouan (6H) + Public (7H)");
        System.out.println("==========================================================================================\n");

        List<SimulationMetrics> results = new ArrayList<>();

        // ---------------------------------------------------------------------
        // CAS 1 : PÉRIODE NORMALE (Semestre classique - Référence First-Fit)
        // ---------------------------------------------------------------------
        System.out.println(">>> [1/5] Exécution : Période Normale (TimeShared, Allocation Simple)...");
        CloudSimulation simNormal = new CloudSimulation(
            SimulationScenario.NORMAL_PERIOD,
            new SimulationPolicy(SimulationPolicy.TaskSchedulerType.TIME_SHARED, SimulationPolicy.VmAllocationPolicyType.SIMPLE)
        );
        results.add(simNormal.run());

        // ---------------------------------------------------------------------
        // CAS 2 : PÉRIODE DE POINTE - CloudletSchedulerTimeShared (Statique)
        // ---------------------------------------------------------------------
        System.out.println(">>> [2/5] Exécution : Période de Pointe Statique (TimeShared, Simple)...");
        CloudSimulation simPeakTimeShared = new CloudSimulation(
            SimulationScenario.PEAK_PERIOD_STATIC,
            new SimulationPolicy(SimulationPolicy.TaskSchedulerType.TIME_SHARED, SimulationPolicy.VmAllocationPolicyType.SIMPLE)
        );
        results.add(simPeakTimeShared.run());

        // ---------------------------------------------------------------------
        // CAS 3 : PÉRIODE DE POINTE - CloudletSchedulerSpaceShared (Statique)
        // ---------------------------------------------------------------------
        System.out.println(">>> [3/5] Exécution : Période de Pointe Statique (SpaceShared, Simple)...");
        CloudSimulation simPeakSpaceShared = new CloudSimulation(
            SimulationScenario.PEAK_PERIOD_STATIC,
            new SimulationPolicy(SimulationPolicy.TaskSchedulerType.SPACE_SHARED, SimulationPolicy.VmAllocationPolicyType.SIMPLE)
        );
        results.add(simPeakSpaceShared.run());

        // ---------------------------------------------------------------------
        // CAS 4 : COMPARAISON ALLOCATION VM - VmAllocationPolicyBestFit
        // ---------------------------------------------------------------------
        System.out.println(">>> [4/5] Exécution : Période Normale avec VmAllocationPolicyBestFit...");
        CloudSimulation simBestFit = new CloudSimulation(
            SimulationScenario.NORMAL_PERIOD,
            new SimulationPolicy(SimulationPolicy.TaskSchedulerType.TIME_SHARED, SimulationPolicy.VmAllocationPolicyType.BEST_FIT)
        );
        results.add(simBestFit.run());

        // ---------------------------------------------------------------------
        // CAS 5 : PÉRIODE DE POINTE AVEC AUTO-SCALING (HorizontalVmScaling + Cloud Bursting)
        // ---------------------------------------------------------------------
        System.out.println(">>> [5/5] Exécution : Période de Pointe ÉLASTIQUE (HorizontalVmScaling + Bursting Public)...");
        CloudSimulation simAutoscale = new CloudSimulation(
            SimulationScenario.PEAK_PERIOD_AUTOSCALE,
            new SimulationPolicy(SimulationPolicy.TaskSchedulerType.TIME_SHARED, SimulationPolicy.VmAllocationPolicyType.BEST_FIT)
        );
        results.add(simAutoscale.run());

        System.out.println("\n[OK] Toutes les simulations se sont achevées avec succès.\n");

        // Affichage du tableau récapitulatif
        printResultsTable(results);

        System.out.println("Consultez le fichier d'analyse complet :");
        System.out.println("--> analyse_des_resultats.md\n");
    }

    private static void printResultsTable(List<SimulationMetrics> list) {
        System.out.println("=========================================================================================================================================================");
        System.out.println("                                                   TABLEAU COMPARATIF DES RÉSULTATS DE SIMULATION");
        System.out.println("                                                            Étudiant : Mohammed Salhi");
        System.out.println("=========================================================================================================================================================");
        System.out.printf("| %-38s | %-12s | %-12s | %-10s | %-10s | %-10s | %-8s | %-14s | %-9s | %-10s |%n",
            "Scénario / Configuration", "Ordonnanceur", "Alloc. VM", "Makespan", "Rép. Moy.", "Attente", "CPU Host", "VMs (Priv/Pub)", "Cloudlets", "Coût Total"
        );
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------");

        for (SimulationMetrics m : list) {
            String shortOrdo = m.getTaskSchedulerName().contains("TimeShared") ? "TimeShared" : "SpaceShared";
            String shortAlloc = m.getVmAllocationName().contains("BestFit") ? "BestFit" : "Simple";
            String vmSummary = String.format("%d (%d/%d)", m.getTotalVmsCreated(), m.getPrivateVmsCount(), m.getPublicBurstVmsCount());
            String cloudletSummary = String.format("%d/%d", m.getTotalCloudletsFinished(), m.getTotalCloudletsSubmitted());

            System.out.printf("| %-38s | %-12s | %-12s | %8.2f s | %8.2f s | %8.2f s | %6.1f %% | %-14s | %-9s | $%9.2f |%n",
                truncate(m.getScenarioName(), 38),
                shortOrdo,
                shortAlloc,
                m.getMakespanSeconds(),
                m.getAvgResponseTimeSeconds(),
                m.getAvgWaitingTimeSeconds(),
                m.getHostCpuUtilizationPercent(),
                vmSummary,
                cloudletSummary,
                m.getTotalCost()
            );
        }
        System.out.println("=========================================================================================================================================================\n");
    }

    private static String truncate(String str, int maxLen) {
        if (str == null) return "";
        return (str.length() > maxLen) ? str.substring(0, maxLen - 3) + "..." : str;
    }
}
