<template>
  <div class="detail-container">
    <div class="title-con">
      <div class="title-left">
        <div class="title">数据迁移任务详情（{{ task.taskName }}）</div>
        <div class="task-status-con">
          <span class="task-status-title">任务状态：</span>
          <span class="task-status">{{ execStatusMap(task.execStatus) }}</span>
        </div>
      </div>
      <div class="title-right">
        <a-button v-if="task.execStatus === 1" type="primary" @click="stopTask">停止</a-button>
      </div>
    </div>
    <a-divider />
    <div class="desc-con">
      <a-descriptions :data="descData" layout="inline-horizontal" table-layout="fixed" bordered />
    </div>
    <div class="progress-con">
      <span class="progress-info">总进度</span>
      <a-progress size="large" :percent="task.execStatus === 2 ? 1: (task.execProgress || 0)" />
      <a-button type="text" @click="getTaskDetail">
        <template #icon>
          <icon-refresh />
        </template>
        <template #default>刷新</template>
      </a-button>
    </div>
    <div class="table-con">
      <a-table :data="tableData" :bordered="false" stripe :pagination="pagination" @page-change="pageChange">
        <template #columns>
          <a-table-column title="子任务ID" data-index="id"></a-table-column>
          <a-table-column title="源库名" data-index="sourceDb"></a-table-column>
          <a-table-column title="目的库名" data-index="targetDb"></a-table-column>
          <a-table-column title="迁移过程模式">
            <template #cell="{ record }">
              {{ record.migrationModelId === 1 ? '离线模式' : '在线模式' }}
            </template>
          </a-table-column>
          <a-table-column title="执行机器">
            <template #cell="{ record }">
              {{ record.runHost }}（{{ record.runHostname }}）
            </template>
          </a-table-column>
          <a-table-column title="当前状态">
            <template #cell="{ record }">
              {{ execSubStatusMap(record.execStatus) }}
            </template>
          </a-table-column>
          <a-table-column title="操作" align="center" :width="300" fixed="right">
            <template #cell="{ record }">
              <a-button
                size="mini"
                type="text"
                @click="handleDetail(record)"
              >
                <template #icon>
                  <icon-eye />
                </template>
                <template #default>详情</template>
              </a-button>
              <!-- <a-button
                v-if="(record.migrationModelId === 1 && record.execStatus === 2) || (record.migrationModelId === 2 && record.execStatus === 2)"
                size="mini"
                type="text"
                @click="stopSubTask(record)"
              >
                <template #icon>
                  <icon-pause />
                </template>
                <template #default>停止全量</template>
              </a-button> -->
              <a-button
                v-if="record.migrationModelId === 2 && record.execStatus === 8"
                size="mini"
                type="text"
                @click="stopSubIncrease(record)"
              >
                <template #icon>
                  <icon-pause />
                </template>
                <template #default>停止增量</template>
              </a-button>
              <a-button
                v-if="record.migrationModelId === 2 && record.execStatus === 3"
                size="mini"
                type="text"
                @click="startSubReverse(record)"
              >
                <template #icon>
                  <icon-play-arrow />
                </template>
                <template #default>启动反向</template>
              </a-button>
              <!-- <a-button
                v-if="record.migrationModelId === 2 && record.execStatus === 11"
                size="mini"
                type="text"
              >
                <template #icon>
                  <icon-pause />
                </template>
                <template #default>停止反向</template>
              </a-button> -->
              <a-button
                v-if="record.execStatus !== 100"
                size="mini"
                type="text"
                @click="stopSubTask(record)"
              >
                <template #icon>
                  <icon-stop />
                </template>
                <template #default>结束迁移</template>
              </a-button>
              <a-button
                size="mini"
                type="text"
                @click="handleLog(record)"
              >
                <template #icon>
                  <icon-file />
                </template>
                <template #default>日志</template>
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>

    <!-- sub task detail -->
    <sub-task-detail v-model:open="subTaskDetailVisible" :task-info="task" :sub-task-id="subTaskId" :tab="tabIndex" />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount } from 'vue'
import { Message } from '@arco-design/web-vue'
import SubTaskDetail from './components/SubTaskDetail.vue'
import { stop } from '@/api/list'
import { taskDetail, refreshStatus, subTaskList, subTaskFinish, subTaskStartReverse, subTaskStopIncremental } from '@/api/detail'

const task = ref({})
const descData = ref([])
let timerTop = null
let timerDown = null

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})
const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10
})
const tableData = ref([])

// status map
const execStatusMap = (status) => {
  const maps = {
    0: '未启动',
    1: '迁移中',
    2: '已完成'
  }
  return maps[status]
}

// sub task status map
const execSubStatusMap = (status) => {
  const maps = {
    0: '未启动',
    1: '全量迁移开始',
    2: '全量迁移进行中',
    3: '全量迁移完成',
    4: '全量校验开始',
    5: '全量校验中',
    6: '全量检验完成',
    7: '增量迁移开始',
    8: '增量迁移进行中',
    9: '增量迁移已停止',
    10: '反向迁移开始',
    11: '反向迁移进行中',
    12: '反向迁移已停止',
    100: '迁移完成',
    500: '迁移失败',
    1000: '等待资源中'
  }
  return maps[status]
}

const pageChange = (current) => {
  queryParams.pageNum = current
  pagination.current = current
  getSubTaskList()
}

const subTaskDetailVisible = ref(false)
const subTaskId = ref()
const tabIndex = ref(1)

const handleDetail = row => {
  subTaskDetailVisible.value = true
  subTaskId.value = row.id
  tabIndex.value = 1
}

const handleLog = row => {
  subTaskDetailVisible.value = true
  subTaskId.value = row.id
  tabIndex.value = 2
}

// stop task
const stopTask = async () => {
  await stop(task.value.id)
  Message.success('Stop success')
  getTaskDetail()
  getSubTaskList()
}

// stop sub task full
const stopSubTask = row => {
  subTaskFinish(row.id).then(() => {
    Message.success('Stop success')
    getSubTaskList()
  })
}

// stop sub task increase
const stopSubIncrease = row => {
  subTaskStopIncremental(row.id).then(() => {
    Message.success('Stop success')
    getSubTaskList()
  })
}

// start sub task reverse
const startSubReverse = row => {
  subTaskStartReverse(row.id).then(() => {
    Message.success('Start success')
    getSubTaskList()
  })
}

const loopSubTaskStatus = () => {
  const id = window.$wujie?.props.data.id
  refreshStatus(id).then(res => {
    console.log(res)
  })
}

const getSubTaskList = () => {
  timerDown && clearTimeout(timerDown)
  const id = window.$wujie?.props.data.id
  subTaskList(id, {
    ...queryParams
  }).then(res => {
    tableData.value = res.rows
    pagination.total = res.total
    if (task.value.execStatus !== 2) {
      timerDown = setTimeout(() => {
        getSubTaskList()
      }, 5000)
    }
  }).catch(() => {
    timerDown && clearTimeout(timerDown)
    timerDown = null
  })
}

const getTaskDetail = () => {
  timerTop && clearTimeout(timerTop)
  const id = window.$wujie?.props.data.id
  taskDetail(id).then(res => {
    task.value = res.data.task
    const taskInfo = res.data.task
    const subTaskCount = res.data.counts
    const hosts = res.data.hosts
    descData.value = [
      {
        label: '创建人：',
        value: taskInfo.createUser
      },
      {
        label: '子任务数量：',
        value: subTaskCount['1'] + subTaskCount['2']
      },
      {
        label: '分配执行机器：',
        value: `${hosts.length}台（${hosts.map(item => item.hostName)}）`
      },
      {
        label: '创建时间：',
        value: taskInfo.createTime
      },
      {
        label: '子任务数量（离线模式）：',
        value: subTaskCount['1'],
        span: 2
      },
      {
        label: '执行时间：',
        value: taskInfo.execTime
      },
      {
        label: '子任务数量（在线模式）：',
        value: subTaskCount['2'],
        span: 2
      }
    ]
    if (task.value.execStatus !== 2) {
      timerTop = setTimeout(() => {
        getTaskDetail()
      }, 5000)
    }
  }).catch(() => {
    timerTop && clearTimeout(timerTop)
    timerTop = null
  })
}

onMounted(() => {
  getTaskDetail()
  getSubTaskList()
  setTimeout(() => {
    loopSubTaskStatus()
  }, 3000)
})

onBeforeUnmount(() => {
  timerTop && clearTimeout(timerTop)
  timerDown && clearTimeout(timerDown)
})
</script>

<style lang="less" scoped>
.detail-container {
  position: relative;
  .title-con {
    padding: 20px 20px 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    .title-left {
      display: flex;
      align-items: center;
      .title {
        font-size: 20px;
        color: var(--color-text-1);
      }
      .task-status-con {
        margin-left: 40px;
        display: flex;
        align-items: center;
        .task-status-title {
          color: var(--color-text-1);
          white-space: nowrap;
          margin-right: 10px;
          display: flex;
          align-items: center;
        }
        .task-status {
          color: rgb(var(--primary-6));
        }
      }
    }
  }
  .desc-con {
    padding: 0 20px;
  }
  .progress-con {
    margin-top: 20px;
    padding: 0 20px;
    display: flex;
    align-items: center;
    .progress-info {
      white-space: nowrap;
      margin-right: 10px;
    }
  }
  .table-con {
    margin-top: 20px;
    padding: 0 20px 30px;
  }
}
</style>