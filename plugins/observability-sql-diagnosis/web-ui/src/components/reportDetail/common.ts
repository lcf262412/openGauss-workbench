import { useI18n } from 'vue-i18n';

export type reportSceneType = {
    id: string;
    name: string;
    title: string;
    advise: string;
    show: Array<string>;
    analysisIdea?: string;
}

export const allReportSceneList: Array<reportSceneType> = [
    {
        id: 'ObjectInfoCheck',
        name: "Analysis ideas",
        title: "Object statistics check",
        advise: 'There is a large gap between the estimated rows and the actual rows in the execution plan calculation. It is recommended to execute analyze to update the statistics of related objects to generate the optimal execution plan and improve the execution efficiency',
        analysisIdea: `Analyze the objects with large gap between actual rows and estimated rows in the execution plan generated by explain analyze. Please check the report details of the analysis results. It is recommended to update the statistics for the objects with gaps between the actual rows and the estimated rows in the analysis results, so as to generate the optimal execution plan and improve the execution efficiency. <br />
        Object statistics can be updated with the analyze tablename command. `,
        show: [],
    },
    {
        id: "ObjectRecommendedToUpdateStatistics",
        name: "Diagnostic results",
        title: "It is recommended to execute analyze to update object statistics",
        advise: 'There is a large gap between the estimated rows and the actual rows in the execution plan calculation. It is recommended to execute analyze to update the statistics of related objects to generate the optimal execution plan and improve the execution efficiency',
        show: ['RowsDiff', 'ExplainAnalyze'],
    },
    {
        id: "ExecPlan",
        name: "Analysis ideas",
        title: "Execution plan",
        advise: 'There is a large gap between the estimated rows and the actual rows in the execution plan calculation. It is recommended to execute analyze to update the statistics of related objects to generate the optimal execution plan and improve the execution efficiency',
        analysisIdea: `Analyze the original SQL execution plan, locate the calculation step that consumes the most in the execution plan, and generate the corresponding analysis results according to the analysis path, which includes <br />
            1、Index suggestion analysis <br />
            2、Object structure analysis <br />
            3、Object data analysis <br />
            4、Memory consumption analysis <br />
            It is recommended to optimize and process SQL or related objects according to the generated analysis results to improve SQL execution efficiency.`,
        show: [],
    },
    {
        id: "PlanRecommendedToCreateIndex",
        name: "Diagnostic results",
        title: "",
        advise: '',
        show: ['ObjectStructure', 'IndexInformation', 'ImplementationPlan'],
    },
    {
        id: "PlanChangedToPartitionTable",
        name: "Diagnostic results",
        title: "It is recommended to change TABLE to partition table",
        advise: 'The number of live tuples in the TABLE exceeds 20 million. It is recommended to change it to a partitioned table to query a single partition as far as possible to improve the query efficiency',
        show: ['BaseInfo', 'ObjectStructure', 'ImplementationPlan'],
    },
    {
        id: "PlanRecommendedToQueryBasedOnPartition",
        name: "Diagnostic results",
        title: "It is recommended to query based on partitions to avoid scanning all partitions",
        advise: 'TABLE is a partitioned table, but it is not queried based on partitions. It is recommended to add partition column conditions or query based on partition keys to query partitioned tables to avoid scanning all partitions',
        show: ['BaseInfo', 'ObjectStructure', 'PartitionInformation', 'ImplementationPlan'],
    },
    {
        id: "PlanRecommendedToDoVacuumCleaning",
        name: "Diagnostic results",
        title: "It is recommended to clean the TABLE vacuum",
        advise: 'The number of deleted or updated records in the TABLE exceeds the total number of table rows * 0.2+50. It is recommended to clean the table vacuum to avoid consuming a lot of IO and affecting query efficiency',
        show: ['BaseInfo', 'ObjectStructure', 'ImplementationPlan'],
    },
    {
        id: "PlanRecommendedToOptimizeStatementsOrAddWorkMemSize",
        name: "Diagnostic results",
        title: "It is recommended to optimize statements or add work_ Mem size",
        advise: 'Memory required for sorting exceeds work_ The size of the mem parameter. It is recommended to optimize the statement or add work_ The size of mem to avoid using disk space and affect query efficiency',
        show: ['WorkMemDiff', 'ExplainAnalyze'],
    },
]

export type OptionType = {
    label: string;
    value: string
}

export const baseInfoOptionTypeA: Array<Array<OptionType>> = [
    [
        { label: 'schemaname', value: 'schemaname' },
        { label: 'objectSize', value: 'object_size' },
    ],
    [
        { label: 'objectType', value: 'object_type' },
        { label: 'nLiveTup', value: 'n_live_tup' },
    ],
]
export const baseInfoOptionTypeB: Array<Array<OptionType>> = [
    [
        { label: 'schemaname', value: 'schemaname' },
        { label: 'deadTupRatio', value: 'dead_tup_ratio' },
    ],
    [
        { label: 'objectType', value: 'object_type' },
        { label: 'lastVacuum', value: 'last_vacuum' }
    ],
    [
        { label: 'objectSize', value: 'object_size' },
        { label: 'lastVacuumAutovacuum', value: 'last_vacuum_autovacuum' },
    ],
    [
        { label: 'nLiveTup', value: 'n_live_tup' },
        { label: 'lastAnalyze', value: 'last_analyze' },
    ],
    [
        { label: 'nDeadTup', value: 'n_dead_tup' },
        { label: 'lastAutoanalyze', value: 'last_autoanalyze' },
    ]
];

export type rowsDiffType = {
    actualRows: string;
    estimateRows: string;
    stepName: string;
    use?: boolean;
}

export type nativePlanType = {
    unit?: string;
    queryPlan?: string;
    peakMem: string;
    workMem: string;
    rowsDiff?: Array<rowsDiffType>
}

export type partitionInfoType = {
    partStrategy: string;
    partKey: string;
    relPages: string;
    relTuples: string;
    relallVisible: string;
    interval: string;
}

export type tableStructureType = {
    attlen: number;
    attname: string;
    attnotnull: boolean;
    attnum: number;
    description: string;
    typname: string;
}

export type tableIndexType = {
    relname: string;
    indisprimary: string;
    indisunique: string;
    indisclustered: string;
    indisvalid: string;
    indisreplident: string;
    def: string;
}

export type tableMetaDataType = {
    schemaname: string;
    relname: string;
    object_type: string;
    object_size: string;
    n_live_tup: string;
    n_dead_tup: string;
    dead_tup_ratio: string;
    last_vacuum: string;
    last_autovacuum: string;
    last_analyze: string;
    last_autoanalyze: string;
}

export type indexAdvicesType = {
    column: string;
    indexType: string;
    schema: string;
    table: string;
}

export const formatValue = (v: string) => {
    const { t } = useI18n();
    if (!v) {
        return t('report.none')
    }
    return v;
}