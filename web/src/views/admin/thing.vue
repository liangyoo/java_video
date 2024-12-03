<template>
  <div>
    <!--页面区域-->
    <div class="page-view">
      <div class="table-operations">
        <a-space>
          <a-button type="primary" @click="handleAdd">模拟新增</a-button>
          <a-button @click="handleBatchDelete">批量删除</a-button>
        </a-space>
      </div>
      <a-table size="middle" rowKey="id" :loading="data.loading" :columns="columns" :data-source="data.list"
        :scroll="{ x: 'max-content' }" :row-selection="rowSelection" :pagination="{
          size: 'default',
          current: data.page,
          pageSize: data.pageSize,
          onChange: (current) => (data.page = current),
          showSizeChanger: false,
          showTotal: (total) => `共${total}条数据`,
        }">
        <template #bodyCell="{ text, record, index, column }">
          <template v-if="column.key === 'operation'">
            <span>
              <a-divider type="vertical" />
              <a-popconfirm title="确定删除?" ok-text="是" cancel-text="否" @confirm="confirmDelete(record)">
                <a href="#">删除</a>
              </a-popconfirm>
            </span>
          </template>
        </template>
      </a-table>
      <!-- 弹窗区域 -->
      <div>
        <a-modal :visible="modal.visile" :forceRender="true" :title="modal.title" ok-text="确认" cancel-text="取消"
          @cancel="handleCancel" @ok="handleOk">
          <div>
            <a-form ref="myform" :label-col="{ style: { width: '80px' } }" :model="modal.form" :rules="modal.rules">
              <a-row :gutter="24">
                <a-col span="24">
                  <a-form-item label="视频名称" name="title">
                    <a-input placeholder="请输入" v-model:value="modal.form.title"></a-input>
                  </a-form-item>
                  <a-form-item label="分类" name="classifcation">
                    <a-select aria-placeholder="请选择" allowClear v-model:value="modal.form.classifcation">
                      <a-select-option key="0" value="0">科技</a-select-option>
                      <a-select-option key="1" value="1">娱乐</a-select-option>
                      <a-select-option key="2" value="2">体育</a-select-option>
                    </a-select>
                  </a-form-item>
                  <a-form-item label="标签" name="status">
                    <a-select placeholder="请选择" allowClear v-model:value="modal.form.status" >
                      <a-select-option key="0" value="0">正常</a-select-option>
                      <a-select-option key="1" value="1">封号</a-select-option>
                    </a-select>
                  </a-form-item>
                  <a-form-item label="视频封面" name="title">
                    <a-upload-dragger v-model:fileList="fileList" name="file" :multiple="true"
                      action="https://www.mocky.io/v2/5cc8019d300000980a055e76" @change="handleChange">
                      <p class="ant-upload-drag-icon">
                        <inbox-outlined></inbox-outlined>
                      </p>
                      <p class="ant-upload-text">Click or drag file to this area to upload</p>
                      <p class="ant-upload-hint">
                        单击上传封面
                      </p>
                    </a-upload-dragger>
                  </a-form-item>
                </a-col>
              </a-row>
            </a-form>
          </div>
        </a-modal>
      </div>
    </div>

  </div>

</template>
<script setup lang="ts">
import { FormInstance, message } from 'ant-design-vue';
import { createApi, listApi, deleteApi } from '/@/api/thing';
import { BASE_URL } from "/@/store/constants";
import { getFormatTime } from "/@/utils";
import { UploadOutlined } from '@ant-design/icons-vue';
import { defineComponent, ref } from 'vue';

interface FileItem {
  uid: string;
  name?: string;
  status?: string;
  response?: string;
  url?: string;
}

interface FileInfo {
  file: FileItem;
  fileList: FileItem[];
}

const handleChange = (info: FileInfo) => {
  if (info.file.status !== 'uploading') {
    console.log(info.file, info.fileList);
  }
  if (info.file.status === 'done') {
    message.success(`${info.file.name} file uploaded successfully`);
  } else if (info.file.status === 'error') {
    message.error(`${info.file.name} file upload failed.`);
  }
};

const fileList = ref<FileItem[]>([]);
const headers = {
  authorization: 'authorization-text',
};
const columns = reactive([
  {
    title: '序号',
    dataIndex: 'index',
    key: 'index',
    align: 'center'
  },
  {
    title: '名称',
    dataIndex: 'title',
    key: 'title',
    align: 'center'
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    align: 'center'
  },
  {
    title: '简介',
    dataIndex: 'content',
    key: 'content',
    align: 'center'
  },
  {
    title: '操作',
    dataIndex: 'action',
    key: 'operation',
    align: 'center',
    fixed: 'right',
    width: 140,
  },
]);

// 页面数据
const data = reactive({
  list: [],
  loading: false,
  currentAdminUserName: '',
  keyword: '',
  selectedRowKeys: [] as any[],
  pageSize: 10,
  page: 1,
  fileName: '请选择文件',
  fileUrl: '',
  options: {
    options: [
      {
        value: '1',
        label: '1',
      },
      {
        value: '2',
        label: '2',
      },
      {
        value: '3',
        label: '3',
      },
    ],
  },
}
);

// 弹窗数据源
const modal = reactive({
  visile: false,
  editFlag: false,
  title: '',
  form: {
    id: undefined,
    classifcation: undefined,
    title: undefined,
    content: undefined,
    cover: '',
    file: undefined,
    status: undefined,
    tag: ''
  },
  rules: {
    link: [{ required: true, message: '请输入', trigger: 'change' }],
  },

});

const myform = ref<FormInstance>();

const rowSelection = ref({
  onChange: (selectedRowKeys: (string | number)[], selectedRows: any[]) => {
    console.log(`selectedRowKeys: ${selectedRowKeys}`, 'selectedRows: ', selectedRows);
    data.selectedRowKeys = selectedRowKeys;
  },
});

const handleAdd = () => {
  resetModal();
  modal.editFlag = false;
  modal.visile = true;
  modal.title = '新增';
  createApi({}).then(res => {
    getList()
  }).catch(err => {

  })
};

const confirmDelete = (record: any) => {
  console.log('delete', record);
  deleteApi({ ids: record.id })
    .then((res) => {
      getList();
    })
    .catch((err) => {
      message.error(err.msg || '操作失败');
    });
};

onMounted(() => {
  getList();
});

const getList = () => {
  data.loading = true;
  listApi({
    keyword: data.keyword,
  })
    .then((res) => {
      data.loading = false;
      console.log(res);
      res.data.forEach((item: any, index: any) => {
        item.index = index + 1;
        if (item.image) {
          item.image = BASE_URL + item.image
        }
      });
      data.list = res.data;
    })
    .catch((err) => {
      data.loading = false;
      console.log(err);
    });
};

const handleBatchDelete = () => {
  console.log(data.selectedRowKeys);
  if (data.selectedRowKeys.length <= 0) {
    console.log('hello');
    message.warn('请勾选删除项');
    return;
  }
  deleteApi({ ids: data.selectedRowKeys.join(',') })
    .then((res) => {
      message.success('删除成功');
      data.selectedRowKeys = [];
      getList();
    })
    .catch((err) => {
      message.error(err.msg || '操作失败');
    });
};

const handleOk = () => {

  myform.value
    ?.validate()
    .then(() => {
      createApi(modal.form)
        .then((res) => {
          message.success('操作成功');
          hideModal();
        })
        .catch((err) => {
          message.error(err.msg || '操作失败');
        });

    })
    .catch((err) => {
      console.log('不能为空');
    });
};


const handleCancel = () => {
  hideModal();
};


// 恢复表单初始状态
const resetModal = () => {
  myform.value?.resetFields();
};

// 关闭弹窗
const hideModal = () => {
  modal.visile = false;
};


</script>
<style scoped lang="less">
.page-view {
  min-height: 100%;
  background: #fff;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.table-operations {
  margin-bottom: 16px;
  text-align: right;
}

.table-operations>button {
  margin-right: 8px;
}
</style>