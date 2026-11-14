package com.uae.cloud;

import org.cloudsimplus.allocationpolicies.VmAllocationPolicy;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicySimple;
import org.cloudsimplus.autoscaling.HorizontalVmScalingSimple;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletScheduler;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudsimplus.util.Log;
import org.cloudsimplus.utilizationmodels.UtilizationModel;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;
import org.cloudsimplus.utilizationmodels.UtilizationModelStochastic;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmCost;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Moteur de simulation Cloud Computing pour l'Université Abdelmalek Essaâdi (UAE).
 * Conçu par : Mohammed Salhi (R134235437).
 *
 * Implémente :
 * - Datacenters privés interconnectés (Tanger & Tétouan-Martil) et Datacenter Public
 * - Déploiement des 48 machines virtuelles de base réparties par service
 * - Modélisation des requêtes applicatives (Cloudlets) en période normale (100) et de pointe (800)
 * - Auto-scaling horizontal (HorizontalVmScaling) et débordement dynamique (Cloud Bursting)
 * - Analyse comparative des politiques d'ordonnancement et d'allocation
 */
public class CloudSimulation {

    private final SimulationScenario scenario;
    private final SimulationPolicy policy;

    private CloudSimPlus simulation;
    private Datacenter dcTanger;
    private Datacenter dcTetouan;
    private Datacenter dcPublic;
    private DatacenterBroker broker;

    private final List<Vm> vmList = new ArrayList<>();
    private final List<Cloudlet> cloudletList = new ArrayList<>();

    // Regroupement des VMs par service applicatif pour le routage
    private final List<Vm> dbVms = new ArrayList<>();
    private final List<Vm> moodleVms = new ArrayList<>();
    private final List<Vm> entVms = new ArrayList<>();
    private final List<Vm> examVms = new ArrayList<>();
    private final List<Vm> dsiVms = new ArrayList<>();
    private final List<Vm> mailVms = new ArrayList<>();

    // Compteurs d'élasticité et de débordement vers le Cloud Public
    private final AtomicInteger scaledVmsCount = new AtomicInteger(0);
    private final AtomicInteger publicBurstVmsCount = new AtomicInteger(0);

    public CloudSimulation(SimulationScenario scenario, SimulationPolicy policy) {
        this.scenario = scenario;
        this.policy = policy;
    }

    /**
     * Point d'entrée de l'exécution de la simulation CloudSim Plus.
     * @return L'ensemble des métriques de performance et de coût collectées.
     */
    public SimulationMetrics run() {
        Log.setLevel(ch.qos.logback.classic.Level.ERROR);

        this.simulation = new CloudSimPlus();

        // 1. Déploiement de l'infrastructure physique (12 serveurs privés + 7 serveurs publics)
        buildDatacenters();

        // 2. Initialisation du courtier (Broker) avec règles de routage hybride et souveraineté
        buildBroker();

        // 3. Instanciation des machines virtuelles socles (48 VMs de base)
        buildBaselineVms();

        // 4. Activation de l'élasticité horizontale (HorizontalVmScaling) si scénario de pointe
        if (scenario == SimulationScenario.PEAK_PERIOD_AUTOSCALE) {
            configureHorizontalAutoscaling();
        }

        // 5. Injection du plan de charge (Cloudlets)
        buildCloudlets();

        // 6. Démarrage du moteur de simulation à événements discrets
        simulation.start();

        // 7. Consolidation des résultats et calcul des métriques SLA
        return collectMetrics();
    }

    // =========================================================================
    // ÉTAPE 1 : INFRASTRUCTURE PHYSIQUE (DATACENTERS & HÔTES)
    // =========================================================================
    private void buildDatacenters() {
        // Datacenter 1 : Pôle Tanger - Boukhalef (6 serveurs de 32 cœurs)
        List<Host> hostsTanger = createHosts(CloudArchitectureConfig.DC1_HOSTS_COUNT);
        dcTanger = new DatacenterSimple(simulation, hostsTanger, createVmAllocationPolicy());
        configureDcCharacteristics(dcTanger, false);
        dcTanger.setSchedulingInterval(1.0);

        // Datacenter 2 : Pôle Tétouan - Martil (6 serveurs de 32 cœurs)
        List<Host> hostsTetouan = createHosts(CloudArchitectureConfig.DC2_HOSTS_COUNT);
        dcTetouan = new DatacenterSimple(simulation, hostsTetouan, createVmAllocationPolicy());
        configureDcCharacteristics(dcTetouan, false);
        dcTetouan.setSchedulingInterval(1.0);

        // Datacenter Public : Débordement Cloud Bursting (7 serveurs alloués à la demande)
        List<Host> hostsPublic = createHosts(CloudArchitectureConfig.PUBLIC_DC_HOSTS_COUNT);
        dcPublic = new DatacenterSimple(simulation, hostsPublic, createVmAllocationPolicy());
        configureDcCharacteristics(dcPublic, true);
        dcPublic.setSchedulingInterval(1.0);
    }

    private List<Host> createHosts(int count) {
        List<Host> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            List<Pe> peList = new ArrayList<>(CloudArchitectureConfig.HOST_PES);
            for (int p = 0; p < CloudArchitectureConfig.HOST_PES; p++) {
                peList.add(new PeSimple(CloudArchitectureConfig.HOST_MIPS));
            }
            Host host = new HostSimple(
                CloudArchitectureConfig.HOST_RAM_MB,
                CloudArchitectureConfig.HOST_BW_MBPS,
                CloudArchitectureConfig.HOST_STORAGE_MB,
                peList
            );
            host.enableUtilizationStats();
            list.add(host);
        }
        return list;
    }

    private VmAllocationPolicy createVmAllocationPolicy() {
        if (policy.getVmAllocationPolicyType() == SimulationPolicy.VmAllocationPolicyType.BEST_FIT) {
            return new VmAllocationPolicyBestFit();
        }
        return new VmAllocationPolicySimple();
    }

    private void configureDcCharacteristics(Datacenter dc, boolean isPublic) {
        if (isPublic) {
            dc.getCharacteristics()
                .setCostPerSecond(CloudArchitectureConfig.COST_PUBLIC_PER_SEC)
                .setCostPerMem(CloudArchitectureConfig.COST_PUBLIC_PER_MEM)
                .setCostPerStorage(CloudArchitectureConfig.COST_PUBLIC_PER_STORAGE)
                .setCostPerBw(CloudArchitectureConfig.COST_PUBLIC_PER_BW);
        } else {
            dc.getCharacteristics()
                .setCostPerSecond(CloudArchitectureConfig.COST_PRIVATE_PER_SEC)
                .setCostPerMem(CloudArchitectureConfig.COST_PRIVATE_PER_MEM)
                .setCostPerStorage(CloudArchitectureConfig.COST_PRIVATE_PER_STORAGE)
                .setCostPerBw(CloudArchitectureConfig.COST_PRIVATE_PER_BW);
        }
    }

    // =========================================================================
    // ÉTAPE 2 : ROUTAGE HYBRIDE ET GESTION DE LA SOUVERAINETÉ (BROKER)
    // =========================================================================
    private void buildBroker() {
        broker = new DatacenterBrokerSimple(simulation);

        // DatacenterMapper : Gestion du placement hybride
        // 1. Données sensibles (DB) : obligatoirement confinées dans les datacenters privés de l'UAE
        // 2. VMs de base : réparties équitablement entre Tanger et Tétouan
        // 3. VMs de débordement : dirigées vers le Cloud Public dès atteinte du seuil de 80%
        broker.setDatacenterMapper((defaultDc, vm) -> {
            String desc = vm.getDescription();
            if (desc != null && desc.contains("PUBLIC")) {
                return dcPublic;
            } else if (desc != null && desc.contains("TETOUAN")) {
                return dcTetouan;
            } else {
                return dcTanger;
            }
        });

        // VmMapper : Aiguillage des requêtes vers le pool de serveurs approprié
        // Priorités : 1=DB, 2=MOODLE, 3=ENT, 4=EXAMS, 5=DSI, 6=MAIL
        broker.setVmMapper(cloudlet -> {
            int prio = cloudlet.getPriority();
            long id = Math.abs(cloudlet.getId());
            switch (prio) {
                case 3: // Portail Scolarité / ENT Apogée
                    return entVms.isEmpty() ? Vm.NULL : entVms.get((int) (id % entVms.size()));
                case 4: // Plateforme Examens & QCM
                    return examVms.isEmpty() ? Vm.NULL : examVms.get((int) (id % examVms.size()));
                case 2: // E-learning Moodle
                    return moodleVms.isEmpty() ? Vm.NULL : moodleVms.get((int) (id % moodleVms.size()));
                case 5: // Recherche & DSI
                    return dsiVms.isEmpty() ? Vm.NULL : dsiVms.get((int) (id % dsiVms.size()));
                case 6: // Messagerie & Bibliothèque
                    return mailVms.isEmpty() ? Vm.NULL : mailVms.get((int) (id % mailVms.size()));
                case 1: // Bases de Données (PaaS)
                    return dbVms.isEmpty() ? Vm.NULL : dbVms.get((int) (id % dbVms.size()));
                default:
                    return entVms.isEmpty() ? Vm.NULL : entVms.get(0);
            }
        });
    }

    // =========================================================================
    // ÉTAPE 3 : DÉPLOIEMENT DES VMS SOCLES (48 VMs = 212 cœurs)
    // =========================================================================
    private void buildBaselineVms() {
        // A. Bases de données relationnelles (PaaS) : 8 VMs (4 Tanger + 4 Tétouan) - 8 cœurs, 32 Go
        for (int i = 0; i < CloudArchitectureConfig.VM_DB_COUNT; i++) {
            boolean isTanger = (i < 4);
            Vm vm = createVm(
                CloudArchitectureConfig.VM_DB_PES,
                CloudArchitectureConfig.VM_DB_RAM_MB,
                isTanger ? "PRIV_TANGER:DB" : "PRIV_TETOUAN:DB"
            );
            dbVms.add(vm);
            vmList.add(vm);
        }

        // B. E-learning Moodle (SaaS) : 16 VMs (8 Tanger + 8 Tétouan) - 4 cœurs, 16 Go
        for (int i = 0; i < CloudArchitectureConfig.VM_MOODLE_COUNT; i++) {
            boolean isTanger = (i < 8);
            Vm vm = createVm(
                CloudArchitectureConfig.VM_MOODLE_PES,
                CloudArchitectureConfig.VM_MOODLE_RAM_MB,
                isTanger ? "PRIV_TANGER:MOODLE" : "PRIV_TETOUAN:MOODLE"
            );
            moodleVms.add(vm);
            vmList.add(vm);
        }

        // C. Portail Scolarité & ENT (SaaS) : 6 VMs de base (3 Tanger + 3 Tétouan) - 4 cœurs, 8 Go
        for (int i = 0; i < CloudArchitectureConfig.VM_ENT_BASE_COUNT; i++) {
            boolean isTanger = (i < 3);
            Vm vm = createVm(
                CloudArchitectureConfig.VM_ENT_PES,
                CloudArchitectureConfig.VM_ENT_RAM_MB,
                isTanger ? "PRIV_TANGER:ENT" : "PRIV_TETOUAN:ENT"
            );
            entVms.add(vm);
            vmList.add(vm);
        }

        // D. Plateforme Examens & Évaluations (SaaS) : 4 VMs (2 Tanger + 2 Tétouan) - 4 cœurs, 8 Go
        for (int i = 0; i < CloudArchitectureConfig.VM_EXAMS_BASE_COUNT; i++) {
            boolean isTanger = (i < 2);
            Vm vm = createVm(
                CloudArchitectureConfig.VM_EXAMS_PES,
                CloudArchitectureConfig.VM_EXAMS_RAM_MB,
                isTanger ? "PRIV_TANGER:EXAMS" : "PRIV_TETOUAN:EXAMS"
            );
            examVms.add(vm);
            vmList.add(vm);
        }

        // E. Recherche & DSI (IaaS) : 8 VMs (4 Tanger + 4 Tétouan) - 4 cœurs, 16 Go
        for (int i = 0; i < CloudArchitectureConfig.VM_DSI_COUNT; i++) {
            boolean isTanger = (i < 4);
            Vm vm = createVm(
                CloudArchitectureConfig.VM_DSI_PES,
                CloudArchitectureConfig.VM_DSI_RAM_MB,
                isTanger ? "PRIV_TANGER:DSI" : "PRIV_TETOUAN:DSI"
            );
            dsiVms.add(vm);
            vmList.add(vm);
        }

        // F. Messagerie & Bibliothèque (SaaS) : 6 VMs (3 Tanger + 3 Tétouan) - 2 cœurs, 4 Go
        for (int i = 0; i < CloudArchitectureConfig.VM_MAIL_COUNT; i++) {
            boolean isTanger = (i < 3);
            Vm vm = createVm(
                CloudArchitectureConfig.VM_MAIL_PES,
                CloudArchitectureConfig.VM_MAIL_RAM_MB,
                isTanger ? "PRIV_TANGER:MAIL" : "PRIV_TETOUAN:MAIL"
            );
            mailVms.add(vm);
            vmList.add(vm);
        }

        broker.submitVmList(vmList);
    }

    private Vm createVm(int pes, long ramMb, String description) {
        CloudletScheduler scheduler = (policy.getTaskSchedulerType() == SimulationPolicy.TaskSchedulerType.TIME_SHARED)
            ? new CloudletSchedulerTimeShared()
            : new CloudletSchedulerSpaceShared();

        Vm vm = new VmSimple(CloudArchitectureConfig.HOST_MIPS, pes, scheduler);
        vm.setRam(ramMb)
          .setBw(1000)
          .setSize(25000)
          .setDescription(description);
        return vm;
    }

    // =========================================================================
    // ÉTAPE 4 : AUTO-SCALING HORIZONTAL & CLOUD BURSTING
    // =========================================================================
    private static class SalhiHorizontalVmScaling extends HorizontalVmScalingSimple {
        private final AtomicInteger globalScaledCounter;
        private final int maxGlobalScaled;

        public SalhiHorizontalVmScaling(AtomicInteger globalScaledCounter, int maxGlobalScaled) {
            this.globalScaledCounter = globalScaledCounter;
            this.maxGlobalScaled = maxGlobalScaled;
        }

        @Override
        protected boolean requestUpScaling(double time) {
            if (globalScaledCounter.get() >= maxGlobalScaled) {
                return false;
            }
            Vm newVm = getVmSupplier().get();
            if (newVm != null && newVm != Vm.NULL) {
                getVm().getBroker().submitVm(newVm);
                return true;
            }
            return false;
        }
    }

    private void configureHorizontalAutoscaling() {
        // Capacité d'absorption locale : 23 VMs (92 cœurs) avant d'atteindre le seuil de 80%
        // Débordement sur Cloud Public : 27 VMs (108 cœurs)
        // Total additionnel en période de pointe : +50 VMs (200 cœurs)
        final int MAX_LOCAL_BURST_VMS = CloudArchitectureConfig.AVAILABLE_LOCAL_BURST_CORES / CloudArchitectureConfig.VM_ENT_PES; // 23
        final int TOTAL_BURST_VMS = CloudArchitectureConfig.VM_ENT_BURST_ADDITIONAL; // 50

        for (Vm vm : entVms) {
            SalhiHorizontalVmScaling scaling = new SalhiHorizontalVmScaling(scaledVmsCount, TOTAL_BURST_VMS);
            scaling.setVmSupplier(() -> {
                int currentScaled = scaledVmsCount.incrementAndGet();
                if (currentScaled > TOTAL_BURST_VMS) {
                    return null;
                }
                boolean routeToPublic = (currentScaled > MAX_LOCAL_BURST_VMS);

                String tag;
                if (routeToPublic) {
                    publicBurstVmsCount.incrementAndGet();
                    tag = "BURST_PUBLIC:ENT";
                } else {
                    tag = (currentScaled % 2 == 0) ? "PRIV_TETOUAN:ENT_SCALE" : "PRIV_TANGER:ENT_SCALE";
                }

                Vm newVm = createVm(CloudArchitectureConfig.VM_ENT_PES, CloudArchitectureConfig.VM_ENT_RAM_MB, tag);
                entVms.add(newVm);
                vmList.add(newVm);
                return newVm;
            });
            scaling.setOverloadPredicate(v -> v.getCpuPercentUtilization() > 0.50);
            vm.setHorizontalScaling(scaling);
        }
    }

    // =========================================================================
    // ÉTAPE 5 : CHARGES DE TRAVAIL (CLOUDLETS)
    // =========================================================================
    private void buildCloudlets() {
        Random rand = new Random(2026); // graine fixe pour reproductibilité

        if (scenario == SimulationScenario.NORMAL_PERIOD) {
            // Période normale : 100 Cloudlets de trafic régulier universitaire (stochastique)
            submitCloudletBatch(2, 30, 4000,   8000,   1, true, rand); // MOODLE (consultation cours/slides)
            submitCloudletBatch(5, 10, 20000,  40000,  4, true, rand); // DSI (scripts scientifiques 4 cœurs)
            submitCloudletBatch(6, 20, 2000,   5000,   1, true, rand); // MAIL (consultation webmail)
            submitCloudletBatch(3, 20, 2000,   4000,   1, true, rand); // ENT (consultation notes/scolarité)
            submitCloudletBatch(4,  5, 1000,   2000,   1, true, rand); // EXAMS (petits quiz réguliers)
            submitCloudletBatch(1, 15, 5000,   10000,  2, true, rand); // DB (requêtes SQL de consultation)
        } else {
            // Période de pointe : 800 Cloudlets (Inscriptions massives de rentrée + Examens en ligne)
            // 1. Trafic de fond universitaire classique (60 Cloudlets)
            submitCloudletBatch(2, 25, 45000, 75000, 1, false, rand); // MOODLE (1 cœur)
            submitCloudletBatch(5,  5, 280000, 280000, 4, false, rand); // DSI (4 cœurs)
            submitCloudletBatch(6, 20, 10000, 12000, 1, false, rand); // MAIL (1 cœur)
            submitCloudletBatch(4, 10, 2000,  3000,  1, false, rand); // EXAMS (1 cœur)

            // 2. Écritures transactionnelles critiques sur bases de données Apogée (60 Cloudlets)
            submitCloudletBatch(1, 25, 20000, 30000, 2, false, rand); // DB (vague initiale de 25)

            // 3. Soumissions massives d'inscriptions et formulaires ENT (680 Cloudlets)
            // Vague 1 à t=0 pour provoquer la saturation immédiate (140 Cloudlets sur les 6 VMs de base)
            submitCloudletBatch(3, 140, 3000, 5000, 1, false, rand);

            // Vagues d'inscriptions successives injectées dynamiquement au fil de l'horloge
            // 6 vagues de 90 ENT + 6 DB = 540 ENT + 35 DB
            final int[] wavesSent = {0};
            final int TOTAL_WAVES = 6;
            simulation.addOnClockTickListener(evt -> {
                double time = evt.getTime();
                if (wavesSent[0] < TOTAL_WAVES && time >= (wavesSent[0] + 1) * 2.0) {
                    wavesSent[0]++;
                    submitCloudletBatch(3, 90, 3000, 5000, 1, false, rand); // +90 ENT par vague
                    submitCloudletBatch(1,  6, 20000, 30000, 2, false, rand); // +6 DB par vague
                }
            });
        }
    }

    private void submitCloudletBatch(
            int servicePriority,
            int count,
            long minMi,
            long maxMi,
            int pesNumber,
            boolean isStochastic,
            Random rand) {

        List<Cloudlet> batch = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            long length = (minMi == maxMi) ? minMi : (minMi + (long) (rand.nextDouble() * (maxMi - minMi)));

            UtilizationModel cpuModel = isStochastic
                ? new UtilizationModelStochastic()
                : new UtilizationModelFull();

            Cloudlet cloudlet = new CloudletSimple(length, pesNumber, cpuModel);
            cloudlet.setUtilizationModelRam(new UtilizationModelDynamic(0.005));
            cloudlet.setUtilizationModelBw(new UtilizationModelDynamic(0.005));
            cloudlet.setPriority(servicePriority);
            cloudlet.setSubmissionDelay(simulation.clock());

            batch.add(cloudlet);
            cloudletList.add(cloudlet);
        }

        broker.submitCloudletList(batch);
    }

    // =========================================================================
    // ÉTAPE 6 : CONSOLIDATION DES MÉTRIQUES & COÛTS
    // =========================================================================
    private SimulationMetrics collectMetrics() {
        SimulationMetrics m = new SimulationMetrics(
            scenario.getDescription(),
            policy.getTaskSchedulerType().getLabel(),
            policy.getVmAllocationPolicyType().getLabel()
        );

        m.setMakespanSeconds(simulation.clock());

        List<Cloudlet> finished = broker.getCloudletFinishedList();
        m.setTotalCloudletsSubmitted(cloudletList.size());
        m.setTotalCloudletsFinished(finished.size());

        double totalWaitTime = 0.0;
        double totalExecTime = 0.0;
        double totalResponseTime = 0.0;

        for (Cloudlet c : finished) {
            double wait = c.getWaitingTime();
            double exec = c.getActualCpuTime();
            double response = c.getFinishTime() - c.getSubmissionDelay();

            totalWaitTime += wait;
            totalExecTime += exec;
            totalResponseTime += Math.max(0.0, response);
        }

        int count = Math.max(1, finished.size());
        m.setAvgWaitingTimeSeconds(totalWaitTime / count);
        m.setAvgExecutionTimeSeconds(totalExecTime / count);
        m.setAvgResponseTimeSeconds(totalResponseTime / count);

        // Zoom sur le service critique en pointe (Inscriptions & ENT)
        double entWait = 0.0;
        double entResponse = 0.0;
        int entCount = 0;
        for (Cloudlet c : finished) {
            if (c.getPriority() == 3) {
                entWait += c.getWaitingTime();
                entResponse += (c.getFinishTime() - c.getSubmissionDelay());
                entCount++;
            }
        }
        if (entCount > 0) {
            System.out.printf("      [Détail ENT/Inscriptions] Rép. Moy: %.2fs | Attente Moy: %.2fs (%d tâches)%n",
                entResponse / entCount, entWait / entCount, entCount);
        }

        // Utilisation moyenne des serveurs physiques privés
        List<Host> privateHosts = new ArrayList<>();
        privateHosts.addAll(dcTanger.getHostList());
        privateHosts.addAll(dcTetouan.getHostList());

        double sumCpuUtil = 0.0;
        double sumRamUtil = 0.0;
        for (Host h : privateHosts) {
            sumCpuUtil += h.getCpuUtilizationStats().getMean();
            sumRamUtil += ((double) h.getRam().getAllocatedResource() / h.getRam().getCapacity());
        }
        int privateHostCount = Math.max(1, privateHosts.size());
        m.setHostCpuUtilizationPercent((sumCpuUtil / privateHostCount) * 100.0);
        m.setHostRamUtilizationPercent((sumRamUtil / privateHostCount) * 100.0);

        // Statistiques des VMs
        m.setTotalVmsCreated(vmList.size());
        m.setPublicBurstVmsCount(publicBurstVmsCount.get());
        m.setPrivateVmsCount(vmList.size() - publicBurstVmsCount.get());

        // Calcul des coûts
        double privateCost = 0.0;
        double publicCost = 0.0;

        for (Vm vm : vmList) {
            VmCost vmCost = new VmCost(vm);
            double cost = vmCost.getTotalCost();
            String desc = vm.getDescription();
            if (desc != null && desc.contains("PUBLIC")) {
                publicCost += cost;
            } else {
                privateCost += cost;
            }
        }

        m.setPrivateCloudCost(privateCost);
        m.setPublicCloudCost(publicCost);
        m.setTotalCost(privateCost + publicCost);

        return m;
    }
}
