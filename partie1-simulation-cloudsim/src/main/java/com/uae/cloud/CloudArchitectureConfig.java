package com.uae.cloud;

/**
 * Configuration de l'architecture Cloud Computing de l'Université Abdelmalek Essaâdi (UAE).
 * Conçue par : Mohammed Salhi (R134235437).
 *
 * Spécifications matérielles, seuils d'élasticité et modèles économiques
 * pour l'infrastructure hybride distribuée (Tanger, Tétouan-Martil et Cloud Public).
 */
public final class CloudArchitectureConfig {

    private CloudArchitectureConfig() {
        // Empêcher l'instanciation (classe de configuration statique)
    }

    // =========================================================================
    // 1. CARACTÉRISTIQUES PHYSIQUES DES SERVEURS HÔTES (SERVEURS BIXEON)
    // =========================================================================
    /** Nombre de cœurs processeur (PEs) par serveur physique */
    public static final int HOST_PES = 32;

    /** Puissance de calcul par cœur en MIPS (Million Instructions Per Second) */
    public static final double HOST_MIPS = 2600.0;

    /** Mémoire vive RAM par serveur en Mo (128 Go = 131 072 Mo) */
    public static final long HOST_RAM_MB = 128 * 1024L;

    /** Capacité de stockage disque NVMe par serveur en Mo (12 To = 12 582 912 Mo) */
    public static final long HOST_STORAGE_MB = 12 * 1024 * 1024L;

    /** Débit réseau par hôte en Mbps (Liaison montante 10 Gbps) */
    public static final long HOST_BW_MBPS = 10_000L;

    // =========================================================================
    // 2. TOPOLOGIE GÉOGRAPHIQUE DU CLOUD PRIVÉ (Tanger & Tétouan)
    // =========================================================================
    /** Nombre de serveurs au Datacenter 1 (Campus Tanger - Boukhalef) */
    public static final int DC1_HOSTS_COUNT = 6;

    /** Nombre de serveurs au Datacenter 2 (Campus Tétouan - Martil) */
    public static final int DC2_HOSTS_COUNT = 6;

    /** Total des serveurs physiques dans le Cloud Privé de l'UAE (12 serveurs) */
    public static final int TOTAL_PRIVATE_HOSTS = DC1_HOSTS_COUNT + DC2_HOSTS_COUNT;

    /** Capacité processeur globale du Cloud Privé (12 * 32 = 384 cœurs) */
    public static final int TOTAL_PRIVATE_CORES = TOTAL_PRIVATE_HOSTS * HOST_PES;

    // =========================================================================
    // 3. SEUIL D'ÉLASTICITÉ (BURSTING) ET DIMENSIONNEMENT PUBLIC
    // =========================================================================
    /**
     * Seuil de sécurité CPU dans le Cloud Privé (80%).
     * Une réserve stricte de 20% (77 cœurs) est conservée pour garantir
     * la continuité de service en cas de panne matérielle (Disaster Recovery).
     */
    public static final double PRIVATE_BURSTING_THRESHOLD_PERCENT = 0.80;

    /** Nombre maximal de cœurs allouables dans le privé avant débordement (384 * 0.80 = 307 cœurs) */
    public static final int MAX_PRIVATE_CORES_ALLOWED = (int) (TOTAL_PRIVATE_CORES * PRIVATE_BURSTING_THRESHOLD_PERCENT);

    /** Cœurs consommés en période normale par les 48 VMs de base (212 cœurs) */
    public static final int NORMAL_PERIOD_CORES_USED = 212;

    /** Cœurs disponibles dans le privé pour l'élasticité locale (307 - 212 = 95 cœurs, soit ~23 VMs de 4 cœurs) */
    public static final int AVAILABLE_LOCAL_BURST_CORES = MAX_PRIVATE_CORES_ALLOWED - NORMAL_PERIOD_CORES_USED;

    /** Nombre d'hôtes virtuels provisionnés chez le fournisseur Cloud Public pour le débordement */
    public static final int PUBLIC_DC_HOSTS_COUNT = 7;

    // =========================================================================
    // 4. DIMENSIONNEMENT DES MACHINES VIRTUELLES (VMs) PAR SERVICE
    // =========================================================================
    // A. Bases de Données Relationnelles (PaaS) - 8 VMs (4 Tanger + 4 Tétouan avec réplication)
    public static final int VM_DB_PES = 8;
    public static final long VM_DB_RAM_MB = 32 * 1024L;
    public static final int VM_DB_COUNT = 8;

    // B. Plateforme E-learning Moodle (SaaS) - 16 VMs (8 Tanger + 8 Tétouan)
    public static final int VM_MOODLE_PES = 4;
    public static final long VM_MOODLE_RAM_MB = 16 * 1024L;
    public static final int VM_MOODLE_COUNT = 16;

    // C. Portail Scolarité & Inscriptions Apogée/ENT (SaaS) - 6 VMs de base (+50 VMs max en pointe)
    public static final int VM_ENT_PES = 4;
    public static final long VM_ENT_RAM_MB = 8 * 1024L;
    public static final int VM_ENT_BASE_COUNT = 6;
    public static final int VM_ENT_BURST_ADDITIONAL = 50; // +50 VMs élastiques lors des pics d'inscriptions
    public static final int VM_ENT_MAX_COUNT = VM_ENT_BASE_COUNT + VM_ENT_BURST_ADDITIONAL; // 56 VMs

    // D. Plateforme d'Examens & QCM en ligne (SaaS) - 4 VMs de routine
    public static final int VM_EXAMS_PES = 4;
    public static final long VM_EXAMS_RAM_MB = 8 * 1024L;
    public static final int VM_EXAMS_BASE_COUNT = 4;

    // E. Calcul Scientifique & Laboratoires de Recherche (IaaS) - 8 VMs
    public static final int VM_DSI_PES = 4;
    public static final long VM_DSI_RAM_MB = 16 * 1024L;
    public static final int VM_DSI_COUNT = 8;

    // F. Messagerie & Bibliothèque Numérique (SaaS) - 6 VMs (3 Tanger + 3 Tétouan)
    public static final int VM_MAIL_PES = 2;
    public static final long VM_MAIL_RAM_MB = 4 * 1024L;
    public static final int VM_MAIL_COUNT = 6;

    /** Total des VMs de base en période normale (8 + 16 + 6 + 4 + 8 + 6 = 48 VMs = 212 cœurs) */
    public static final int TOTAL_BASE_VMS = VM_DB_COUNT + VM_MOODLE_COUNT + VM_ENT_BASE_COUNT
            + VM_EXAMS_BASE_COUNT + VM_DSI_COUNT + VM_MAIL_COUNT;

    // =========================================================================
    // 5. MODÈLE DE COÛTS (CLOUD PRIVÉ vs CLOUD PUBLIC)
    // =========================================================================
    /** Coût à la seconde - Cloud Privé (amortissement matériel, électricité, climatisation) */
    public static final double COST_PRIVATE_PER_SEC = 0.012;
    public static final double COST_PRIVATE_PER_MEM = 0.0006;
    public static final double COST_PRIVATE_PER_STORAGE = 0.0001;
    public static final double COST_PRIVATE_PER_BW = 0.0001;

    /** Coût à la seconde - Cloud Public (facturation on-demand externe à l'usage) */
    public static final double COST_PUBLIC_PER_SEC = 0.085;
    public static final double COST_PUBLIC_PER_MEM = 0.0045;
    public static final double COST_PUBLIC_PER_STORAGE = 0.0006;
    public static final double COST_PUBLIC_PER_BW = 0.0006;
}
