<template>
    <div class="dialog">
        <el-dialog :width="dialogWith" height="200px" :title="t('install.installAgent')" v-model="visible" :close-on-click-modal="false" draggable @close="closeDialog">
            <div class="dialog-content" v-show="installData.length != 0" style="padding-bottom: 0px">
                <div>
                    <el-steps direction="vertical" :active="doingIndex">
                        <el-step v-for="item in installData" :key="item.name" :title="item.name">
                            <template #description>
                                <div v-for="msg in item.msg"><b>{{ msg }}</b></div>
                                <el-input v-if="item.error"
                                    v-model="item.error"
                                    :rows="5"
                                    type="textarea"
                                    readonly
                                />
                            </template>
                        </el-step>
                    </el-steps>
                </div>
            </div>
            <div class="dialog-content" v-loading="started" v-show="installData.length === 0">
                <el-form :model="formData" :rules="connectionFormRules" ref="connectionFormRef" label-width="150px">
                    <el-form-item :label="t('install.collectInstance')" prop="nodeId">
                        <ClusterCascader width="200" instanceValueKey="nodeId" @getCluster="handleClusterValue" autoSelectFirst notClearable @loaded="getClusterList" />
                    </el-form-item>
                    <el-form-item :label="t('install.rootPWD')" prop="rootPassword">
                        <el-input v-model="formData.rootPassword" show-password style="width: 200px; margin: 0 4px" />
                    </el-form-item>
                    <el-form-item :label="t('install.ogRunLogPath')" prop="ogRunLogPath">
                        <el-input v-model="formData.ogRunLogPath" style="width: 200px; margin: 0 4px" />
                    </el-form-item>
                    <el-form-item :label="t('install.gsCtlLogPath')" >
                        <el-input v-model="formData.gsCtlLogPath" style="width: 200px; margin: 0 4px" />
                    </el-form-item>
                    <el-form-item :label="t('install.gsGucLogPath')" >
                        <el-input v-model="formData.gsGucLogPath" style="width: 200px; margin: 0 4px" />
                    </el-form-item>
                    <el-form-item :label="t('install.gsOmLogPath')" >
                        <el-input v-model="formData.gsOmLogPath" style="width: 200px; margin: 0 4px" />
                    </el-form-item>
                    <el-form-item :label="t('install.gsInstallLogPath')" >
                        <el-input v-model="formData.gsInstallLogPath" style="width: 200px; margin: 0 4px" />
                    </el-form-item>
                    <el-form-item :label="t('install.gsLocalLogPath')" >
                        <el-input v-model="formData.gsLocalLogPath" style="width: 200px; margin: 0 4px" />
                    </el-form-item>
                    <el-form-item :label="t('install.installMode')" prop="installMode">
                        <el-radio-group v-model="formData.installMode">
                            <el-radio label="online">{{ t('install.online') }}</el-radio>
                            <el-radio label="offline">{{ t('install.offline') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <div v-if="formData.installMode === 'offline'">
                        <el-form-item label="Filebeat" style="margin-bottom: 0">
                            <el-link :underline="false" @click="showUploadFile('node', formData.pkg)">{{ t('install.uploadPkg') }}</el-link>
                            <el-popover placement="top-start" :width="350" trigger="hover" :content="t('install.pleaseUpload') + formData.pkg">
                                <template #reference>
                                    <el-icon class="upload-icon"><Warning /></el-icon>
                                </template>
                            </el-popover>
                        </el-form-item>
                        <el-form-item required :label="t('install.src')">
                            <div>{{ formData.src ? formData.src : '--' }}</div>
                        </el-form-item>
                    </div>
                </el-form>
            </div>

            <template #footer>
                <el-button v-if="installData.length === 0" :loading="started" style="padding: 5px 20px" type="primary" @click="install">{{ $t('install.install') }}</el-button>
                <el-button v-if="installData.length != 0" style="padding: 5px 20px" @click="back">{{ $t('app.back') }}</el-button>
                <el-button style="padding: 5px 20px" @click="handleCancelModel">{{ $t('app.cancel') }}</el-button>
            </template>
        </el-dialog>
        <el-dialog v-if="showUpload" v-model="showUpload" :close-on-click-modal="false" :title="t('install.pleaseUpload') + pgkName">
            <div class="dialog-content" style="padding-bottom: 0px">
                <div class="suggest-info">
                    <span>{{ t('install.downloadSuggest') }}：</span><el-link :href="formData.url">{{ formData.url }}</el-link>
                </div>

                <el-upload v-model:file-list="fileList" drag :http-request="upload" :limit="1" :show-file-list="true" :on-exceed="handleExceed" :before-upload="uploadBefore">
                    <el-icon class="el-icon--upload"><plus /></el-icon>
                    <div class="el-upload__text">{{ t('install.uploadInfo') }}</div>
                </el-upload>
                <div>
                    <el-progress v-if="showProgress" :percentage="progressPercent" :format="progressFormat" />
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { cloneDeep } from 'lodash-es';
import { FormRules, FormInstance, ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
import WebSocketClass from '../../../utils/websocket';
import { encryptPassword } from '../../../utils/jsencrypt';
import moment from 'moment';
import { Plus, Warning } from '@element-plus/icons-vue';
import type { UploadProps } from 'element-plus';
import ogRequest from '../../../request';
const { t } = useI18n();

const visible = ref(false);
const props = withDefaults(
    defineProps<{
        show: boolean;
    }>(),
    {}
);
watch(
    () => props.show,
    (newValue) => {
        visible.value = newValue;
    },
    { immediate: true }
);

// form data
const initFormData = {
    rootPassword: '',
    port: '9090',
    // msgLogPath: "",
    ogRunLogPath: '',
    gsCtlLogPath: "",
    gsGucLogPath: "",
    gsOmLogPath: "",
    gsInstallLogPath: "",
    gsLocalLogPath: "",
    // gs: ""
    installMode: 'online',
    pkg: '',
    src: '',
    url: '',
};
const formData = reactive(cloneDeep(initFormData));
const connectionFormRef = ref<FormInstance>();
const connectionFormRules = reactive<FormRules>({
    nodeId: [{ required: true, message: t("install.collectorRules[0]"), trigger: "blur" }],
    rootPassword: [{ required: true, message: t("install.collectorRules[1]"), trigger: "blur" }],
    ogRunLogPath: [{ required: true, message: t("install.collectorRules[2]"), trigger: "blur" }],
});
// cluster component
const handleClusterValue = (val: any) => {
    formData.nodeId = val.length > 1 ? val[1] : "";
    if (formData.nodeId) {
        nextTick(() => {
            refreshPkgInfo();
        });
    }
};
const clusterList = ref<any[]>();
const getClusterList = (val: any) => {
    clusterList.value = val;
};
const getPkgInfo = (hostId: string) => {
    const key = 'filebeat';
    ogRequest
        .get('/observability/v1/environment/pkg', {
            key,
            hostId,
        })
        .then(function (res) {
            if (res) {
                formData.pkg = res.pkg;
                formData.src = res.src;
                formData.url = res.url;
            }
            return res;
        })
        .catch(function (res) {});
};

const started = ref(false);
const installSucceed = ref(false);
const ws = reactive({
    name: '',
    webUser: '',
    connectionName: '',
    sessionId: '',
    instance: null,
});
const install = async () => {
    let result = await connectionFormRef.value?.validate();
    if (!result) return;
    started.value = true;
    //判断是否安装了服务端
    ogRequest.get("/observability/v1/environment/elasticsearch", "").then(res => {
        if(!res || res.length === 0) {
            ElMessage({
                showClose: true,
                message: t("install.installServerAlert"),
                type: "warning",
            });
            started.value = false
        }else {
            ws.name = moment(new Date()).format("YYYYMMDDHHmmss") as string; // websocket connection name
            ws.sessionId = moment(new Date()).format("YYYYMMDDHHmmss") as string; // websocket connection id
            ws.instance = new WebSocketClass(ws.name, ws.sessionId, onWebSocketMessage);
            sendData();
        }
    }).catch(() => {
        started.value = false
    })
}
const sendData = async () => {
    const encryptPwd = await encryptPassword(formData.rootPassword);
    const sendData = {
        key: 'filebeat',
        nodeId: formData.nodeId,
        rootPassword: encryptPwd,
        // msgLogPath: formData.msgLogPath,
        logPath: formData.ogRunLogPath,
        slowlogPath: formData.ogRunLogPath,
        errorlogPath: formData.ogRunLogPath,
        gsCtlLogPath: formData.gsCtlLogPath,
        gsGucLogPath: formData.gsGucLogPath,
        gsOmLogPath: formData.gsOmLogPath,
        gsInstallLogPath: formData.gsInstallLogPath,
        gsLocalLogPath: formData.gsLocalLogPath,
        language: localStorage.getItem('locale') === 'en-US' ? 'en_US' : 'zh_CN'
        // gs: formData.gs
    };

    ws.instance.send(sendData);
};
const onWebSocketMessage = (data: Array<any>) => {
    // if (Array.isArray(installData.value)) installData.value = JSON.parse(data);
    if (data) {
        try {
            installData.value = JSON.parse(data);
        } catch (error) {
            installData.value.forEach((item) => {
                if (item.state === 'ERROR') {
                    item['error'] = data;
                    dialogWith.value = '800px';
                }
            });
        }
        
    }
};

// action
const back = () => {
    started.value = false;
    dialogWith.value = '800px';
    ws.instance.close();
    installData.value = [];
};

// list Data
const installData = ref<Array<any>>([]);
const dialogWith = ref<string>('500px');
const doingIndex = computed(() => {
    for (let index = 0; index < installData.value.length; index++) {
        const element = installData.value[index];
        if (element.state === 'DOING' || element.state === 'ERROR') return index;
    }
    if (installData.value.length > 0 && !installSucceed.value) installSucceed.value = true;
    return installData.value.length;
});

// dialog
const emit = defineEmits(['changeModal', 'installed']);
const handleCancelModel = () => {
    visible.value = false;
    if (installSucceed.value) emit('installed');
    emit('changeModal', visible.value);
};
const closeDialog = () => {
    visible.value = false;
    if (installSucceed.value) emit('installed');
    emit('changeModal', visible.value);
};

// package info
const refreshPkgInfo = () => {
    let nodeInfoList = clusterList.value
        ?.map((itme) => itme.clusterNodes)
        .flat()
        .filter((item) => item.nodeId === formData.nodeId);
    if (nodeInfoList && nodeInfoList.length > 0) {
        let hostId = nodeInfoList[0].hostId;
        getPkgInfo(hostId);
    }
};

const showUpload = ref<boolean>(false);
const fileList = ref<any[]>();
const pgkName = ref<string>();
const showProgress = ref<boolean>(false);
const progressPercent = ref<number>(0);
const progressFormat = (percentage:number) => percentage + '%';
const showUploadFile = (_type: string, _pgkName: string) => {
    showUpload.value = true;
    pgkName.value = _pgkName;
};
const upload = (action: any) => {
    if (!action) {
        return;
    }
    let formData = new FormData();
    formData.append('name', pgkName.value);
    formData.append('pkg', action.file);
    showProgress.value = true;
    ogRequest
        .post('/observability/v1/environment/upload', formData, { headers: { contentType: 'multipart/form-data' },onUploadProgress: event => {
            progressPercent.value = parseInt(event.loaded / event.total  * 100);
        } })
        .then(function (res) {
            if(!res) {
                ElMessage({
                    message: t('install.uploadSucceed'),
                    type: 'success',
                });
                showUpload.value = false;
                if(!fileList.value || fileList.value.length === 0) {
                    fileList.value = [{name:action.file.name,raw: action.file}]
                }
                refreshPkgInfo();
            }else {
                fileList.value = [];
                ElMessage({
                    message: res && res.msg ? res.msg : t('install.uploadFail'),
                    type: 'error',
                }); 
            }
            showProgress.value = false;
            progressPercent.value = 0;
        })
        .catch(function (res) {
            fileList.value = [];
            showProgress.value = false;
            progressPercent.value = 0;
            ElMessage({
                    message: res && res.msg ? res.msg : t('install.uploadFail'),
                    type: 'error',
                }); 
        });
};
const handleExceed: UploadProps['onExceed'] = (files, uploadFiles) => {
    fileList.value = files;
};
const uploadBefore = () => {
    fileList.value = [];
    return true;
};

onBeforeUnmount(() => {
    if (ws.instance) ws.instance.close();
});
</script>
<style lang="scss" scoped>
@import '../../../assets/style/style1.scss';
.upload-icon {
    margin-left: 5px;
    font-size: 13px;
}

.suggest-info {
    margin-bottom: 10px;
}
</style>
