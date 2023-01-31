<template>
  <div class="custom-control-container">
    <div class="flex-row mb">
      <div class="top-label mr-s">{{ $t('customControl.index.else1') }}:</div>
      <a-select class="mr" style="width: 300px;" :loading="data.hostListLoading" v-model="data.hostId"
        :placeholder="$t('customControl.index.5mplgrscm4s0')" @change="hostChange">
        <a-option v-for="(item, index) in data.hostList" :key="index" :label="item.label" :value="item.value" />
      </a-select>
      <a-spin :loading="data.loading">
        <a-tag v-if="(!data.isSuccess && !data.loading)" color="red">Connection establishment failure</a-tag>
      </a-spin>
    </div>
    <div id="xterm" class="xterm"></div>
    <host-pwd-dlg ref="hostPwdRef" @finish="handleConnect($event)"></host-pwd-dlg>
  </div>
</template>

<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import Socket from '@/utils/websocket'
import { reactive, ref, onMounted, onBeforeUnmount } from 'vue'
import { hostListAll, openSSH } from '@/api/ops'
import { WsConnectType } from '@/types/ops/install'
import { Terminal } from 'xterm'
import { FitAddon } from 'xterm-addon-fit'
import { AttachAddon } from 'xterm-addon-attach'
import 'xterm/css/xterm.css'
import HostPwdDlg from './HostPwdDlg.vue'

const data = reactive<KeyValue>({
  loading: false,
  hostId: '',
  rootPassword: '',
  hostListLoading: false,
  hostList: [],
  hostObj: {},
  isSuccess: true
})

const terminalWs = ref<Socket<any, any> | undefined>()

const termTerminal = ref<Terminal>()

onMounted(() => {
  getHostList()
})

onBeforeUnmount(() => {
  terminalWs.value?.destroy()
  if (termTerminal.value) {
    termTerminal.value.dispose()
  }
})

const getHostList = () => {
  data.hostListLoading = true
  hostListAll().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.hostList = []
      res.data.forEach((item: KeyValue) => {
        data.hostObj[item.hostId] = item
        data.hostList.push({
          label: `${item.privateIp}(${item.publicIp})`,
          value: item.hostId
        })
      })
      data.hostId = data.hostList[0].value
      openDlgToValid(data.hostId)
    }
  }).finally(() => {
    data.hostListLoading = false
  })
}

const hostChange = () => {
  if (data.hostId) {
    terminalWs.value?.destroy()
    termTerminal.value?.dispose()
    openDlgToValid(data.hostId)
  }
}

const hostPwdRef = ref<null | InstanceType<typeof HostPwdDlg>>(null)
const openDlgToValid = (hostId: string) => {
  if (data.hostObj[hostId]) {
    hostPwdRef.value?.open(data.hostObj[hostId])
  }
}

const handleConnect = (hostData: any) => {
  data.hostId = hostData.hostId
  data.rootPassword = hostData.password
  openSocket()
}

const openSocket = () => {
  const term = getTermObj()
  const socketKey = new Date().getTime()
  const terminalSocket = new Socket({ url: `custom_terminal_${socketKey}` })
  terminalWs.value = terminalSocket
  terminalSocket.onopen(() => {
    const param = {
      hostId: data.hostId,
      rootPassword: data.rootPassword,
      wsConnectType: WsConnectType.SSH,
      businessId: `custom_terminal_${socketKey}`
    }
    data.loading = true
    openSSH(param).then((res: KeyValue) => {
      if (Number(res.code) !== 200) {
        data.isSuccess = false
        terminalSocket.destroy()
      } else {
        data.isSuccess = true
      }
    }).catch(() => {
      data.isSuccess = false
      terminalSocket.destroy()
    }).finally(() => {
      data.loading = false
    })
    initTerm(term, terminalSocket.ws)
  })
}

const initTerm = (term: Terminal, ws: WebSocket | undefined) => {
  if (ws) {
    const attachAddon = new AttachAddon(ws)
    const fitAddon = new FitAddon()
    term.loadAddon(attachAddon)
    term.loadAddon(fitAddon)
    term.open(document.getElementById('xterm') as HTMLElement)
    fitAddon.fit()
    term.clear()
    term.focus()
    term.write('\r\n\x1b[33m$\x1b[0m ')
    termTerminal.value = term
  }
}

const getTermObj = (): Terminal => {
  return new Terminal({
    fontSize: 14,
    rows: 40,
    cols: 100,
    cursorBlink: true,
    convertEol: true,
    disableStdin: false,
    cursorStyle: 'underline',
    theme: {
      background: 'black'
    }
  })
}

</script>

<style lang="less" scoped>
.custom-control-container {
  padding: 20px;
  background-color: #FFF;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 138px - 40px);

  .top-label {
    white-space: nowrap;
  }

  .xterm {
    width: 100%;
    height: 100%;
  }
}
</style>