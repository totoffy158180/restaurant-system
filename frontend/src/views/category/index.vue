<template>
  <div class="dashboard-container">
    <div class="container">
      <div class="tableBar"
           style="display: inline-block; width: 100%">
        <label style="margin-right: 10px">{{ $t('category.nameLabel') }}</label>
        <el-input v-model="name"
                  :placeholder="$t('category.searchPlaceholder')"
                  style="width: 15%"
                  clearable
                  @clear="init"
                  @keyup.enter.native="init" />

        <label style="margin-right: 5px; margin-left: 20px">{{ $t('category.typeLabel') }}</label>
        <el-select v-model="categoryType"
                   :placeholder="$t('common.pleaseSelect')"
                   clearable
                   style="width: 15%"
                   @clear="init">
          <el-option v-for="item in options"
                     :key="item.value"
                     :label="$t(item.label)"
                     :value="item.value" />
        </el-select>

        <div style="float: right">
          <el-button type="primary"
                     class="continue"
                     @click="addClass('class')">
            + {{ $t('category.addDishCategory') }}
          </el-button>
          <el-button type="primary"
                     style="margin-left:20px"
                     @click="addClass('meal')">
            + {{ $t('category.addSetmealCategory') }}
          </el-button>
        </div>

        <el-button class="normal-btn continue"
                   @click="init(true)">
          {{ $t('common.search') }}
        </el-button>
      </div>
      <el-table v-if="tableData.length"
                :data="tableData"
                stripe
                class="tableBox">
        <el-table-column prop="name"
                         :label="$t('category.column.name')" />
        <el-table-column prop="type"
                         :label="$t('category.column.type')">
          <template slot-scope="scope">
            <span>{{ scope.row.type == '1' ? $t('category.type.dish') : $t('category.type.setmeal') }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="sort"
                         :label="$t('category.column.sort')" />
        <el-table-column :label="$t('category.column.status')">
          <template slot-scope="scope">
            <div class="tableColumn-status"
                 :class="{ 'stop-use': String(scope.row.status) === '0' }">
              {{ String(scope.row.status) === '0' ? $t('common.statusDisabled') : $t('common.statusEnabled') }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime"
                         :label="$t('category.column.updateTime')" />
        <el-table-column :label="$t('common.operation')"
                         :width="$i18n.locale === 'en' ? 240 : 200"
                         align="center">
          <template slot-scope="scope">
            <el-button type="text"
                       size="small"
                       class="blueBug"
                       @click="editHandle(scope.row)">
              {{ $t('common.edit') }}
            </el-button>
            <el-button type="text"
                       size="small"
                       class="delBut"
                       @click="deleteHandle(scope.row.id)">
              {{ $t('common.delete') }}
            </el-button>
            <el-button type="text"
                       size="small"
                       class="non"
                       :class="{
                         blueBug: scope.row.status == '0',
                         delBut: scope.row.status != '0'
                       }"
                       @click="statusHandle(scope.row)">
              {{ scope.row.status == '1' ? $t('common.disable') : $t('common.enable') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <Empty v-else
             :is-search="isSearch" />
      <el-pagination v-if="counts > 10"
                     class="pageList"
                     :page-sizes="[10, 20, 30, 40]"
                     :page-size="pageSize"
                     layout="total, sizes, prev, pager, next, jumper"
                     :total="counts"
                     @size-change="handleSizeChange"
                     @current-change="handleCurrentChange" />
    </div>
    <el-dialog :title="$t(classData.title)"
               :visible.sync="classData.dialogVisible"
               width="30%"
               :before-close="handleClose">
      <el-form ref="classData"
               :model="classData"
               class="demo-form-inline"
               :rules="rules"
               label-width="100px">
        <el-form-item :label="$t('category.form.name')"
                      prop="name">
          <el-input v-model="classData.name"
                    :placeholder="$t('category.form.namePlaceholder')"
                    maxlength="20" />
        </el-form-item>
        <el-form-item :label="$t('category.form.sort')"
                      prop="sort">
          <el-input v-model="classData.sort"
                    :placeholder="$t('category.form.sortPlaceholder')" />
        </el-form-item>
      </el-form>
      <span slot="footer"
            class="dialog-footer">
        <el-button size="medium"
                   @click="
            ;(classData.dialogVisible = false), $refs.classData.resetFields()
                   ">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary"
                   :class="{ continue: actionType === 'add' }"
                   size="medium"
                   @click="submitForm()">{{ $t('common.confirm') }}</el-button>
        <el-button v-if="action != 'edit'"
                   type="primary"
                   size="medium"
                   @click="submitForm('go')">
          {{ $t('category.saveAndContinue') }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import HeadLable from '@/components/HeadLable/index.vue'
import {
  getCategoryPage,
  deleCategory,
  editCategory,
  addCategory,
  enableOrDisableEmployee
} from '@/api/category'
import Empty from '@/components/Empty/index.vue'

@Component({
  name: 'Category',
  components: {
    HeadLable,
    Empty
  }
})
export default class extends Vue {
  private options: any = [
    {
      value: 1,
      label: 'category.type.dish'
    },
    {
      value: 2,
      label: 'category.type.setmeal'
    }
  ]
  private actionType: string = ''
  private id = ''
  private status = ''
  private categoryType: number = null
  private name: string = ''
  private action: string = ''
  private counts: number = 0
  private page: number = 1
  private pageSize: number = 10
  private tableData = []
  private type = ''
  private isSearch: boolean = false
  private classData: any = {
    title: 'category.addDishCategory',
    dialogVisible: false,
    categoryId: '',
    name: '',
    sort: ''
  }

  get rules() {
    return {
      name: [
        {
          required: true,
          trigger: 'blur',
          validator: (rule: any, value: string, callback: Function) => {
            // const reg = /[\u4e00-\u9fa5]/
            var reg = new RegExp('^[A-Za-z\u4e00-\u9fa5]+$')
            if (!value) {
              callback(new Error(this.$t('category.rules.nameRequired') as string))
            } else if (value.length < 2) {
              callback(new Error(this.$t('category.rules.nameLength') as string))
            } else if (!reg.test(value)) {
              callback(new Error(this.$t('category.rules.nameSpecialChars') as string))
            } else {
              callback()
            }
          }
        }
      ],
      sort: [
        {
          required: true,
          trigger: 'blur',
          validator: (rule: any, value: string, callback: Function) => {
            if (value || String(value) === '0') {
              const reg = /^\d+$/
              if (!reg.test(value)) {
                callback(new Error(this.$t('category.rules.sortNumber') as string))
              } else if (Number(value) > 99) {
                callback(new Error(this.$t('category.rules.sortRange') as string))
              } else {
                callback()
              }
            } else {
              callback(new Error(this.$t('category.rules.sortRequired') as string))
            }
          }
        }
      ]
    }
  }

  created() {
    this.init()
  }

  // 初始化信息
  private async init(isSearch?) {
    this.isSearch = isSearch
    await getCategoryPage({
      page: this.page,
      pageSize: this.pageSize,
      name: this.name ? this.name : undefined,
      type: this.categoryType ? this.categoryType : undefined
    })
      .then(res => {
        if (String(res.data.code) === '1') {
          this.tableData =
            res && res.data && res.data.data && res.data.data.records
          this.counts = Number(res.data.data.total)
        } else {
          this.$message.error(res.data.desc)
        }
      })
      .catch(err => {
        console.log(err, 'err')
        this.$message.error(this.$t('common.requestError', { message: err.message }) as string)
      })
  }

  // 添加
  private addClass(st: any) {
    if (st == 'class') {
      this.classData.title = 'category.addDishCategory'
      this.type = '1'
    } else {
      this.classData.title = 'category.addSetmealCategory'
      this.type = '2'
    }
    this.action = 'add'
    this.classData.name = ''
    this.classData.sort = ''
    this.classData.dialogVisible = true
    this.actionType = 'add'
  }

  // 修改
  private editHandle(dat: any) {
    this.classData.title = 'category.editCategory'
    this.action = 'edit'
    this.classData.name = dat.name
    this.classData.sort = dat.sort
    this.classData.id = dat.id
    this.classData.dialogVisible = true
    this.actionType = 'edit'
  }

  // 关闭弹窗
  private handleClose(st: string) {
    console.log(this.$refs.classData, 'this.$refs.classData')
    this.classData.dialogVisible = false
    //对该表单项进行重置，将其值重置为初始值并移除校验结果
    this.$refs.classData.resetFields()
  }

  //状态修改
  private statusHandle(row: any) {
    this.id = row.id
    this.status = row.status
    this.$confirm(this.$t('category.statusConfirm') as string, this.$t('common.tip') as string, {
      confirmButtonText: this.$t('common.confirm') as string,
      cancelButtonText: this.$t('common.cancel') as string,
      type: 'warning',
      customClass: 'customClass'
    }).then(() => {
      enableOrDisableEmployee({ id: this.id, status: !this.status ? 1 : 0 })
        .then(res => {
          if (String(res.status) === '200') {
            this.$message.success(this.$t('category.statusSuccess') as string)
            this.init()
          }
        })
        .catch(err => {
          this.$message.error(this.$t('common.requestError', { message: err.message }) as string)
        })
    })
  }

  //删除
  private deleteHandle(id: any) {
    this.$confirm(this.$t('category.deleteConfirm') as string, this.$t('common.deleteTitle') as string, {
      confirmButtonText: this.$t('common.delete') as string,
      cancelButtonText: this.$t('common.cancel') as string,
      type: 'warning'
    }).then(() => {
      deleCategory(id)
        .then(res => {
          if (res.data.code === 1) {
            this.$message.success(this.$t('common.deleteSuccess') as string)
            this.init()
          } else {
            this.$message.error(res.data.msg)
          }
        })
        .catch(err => {
          this.$message.error(this.$t('common.requestError', { message: err.message }) as string)
        })
    })
  }

  $refs!: {
    classData: any
  }

  //数据提交
  submitForm(st: any) {
    if (this.action === 'add') {
      this.$refs.classData.validate((value: boolean) => {
        if (value) {
          addCategory({
            name: this.classData.name,
            type: this.type,
            sort: this.classData.sort
          })
            .then(res => {
              if (res.data.code === 1) {
                this.$message.success(this.$t('category.addSuccess') as string)
                this.$refs.classData.resetFields()
                if (!st) {
                  this.classData.dialogVisible = false
                }
                this.init()
              } else {
                this.$message.error(res.data.desc || res.data.msg)
              }
            })
            .catch(err => {
              this.$message.error(this.$t('common.requestError', { message: err.message }) as string)
            })
        }
      })
    } else {
      this.$refs.classData.validate((value: boolean) => {
        if (value) {
          editCategory({
            id: this.classData.id,
            name: this.classData.name,
            sort: this.classData.sort
          })
            .then(res => {
              if (res.data.code === 1) {
                this.$message.success(this.$t('category.editSuccess') as string)
                this.classData.dialogVisible = false
                this.$refs.classData.resetFields()
                this.init()
              } else {
                this.$message.error(res.data.desc || res.data.msg)
              }
            })
            .catch(err => {
              this.$message.error(this.$t('common.requestError', { message: err.message }) as string)
            })
        }
      })
    }
  }

  //分页
  private handleSizeChange(val: any) {
    this.pageSize = val
    this.init()
  }

  private handleCurrentChange(val: any) {
    this.page = val
    this.init()
  }
}
</script>
<style lang="scss" scoped>
.dashboard {
  &-container {
    margin: 30px;

    .container {
      background: #fff;
      position: relative;
      z-index: 1;
      padding: 30px 28px;
      border-radius: 4px;

      .tableBar {
        display: flex;
        margin-bottom: 20px;
        justify-content: space-between;
      }

      .tableBox {
        width: 100%;
        border: 1px solid $gray-5;
        border-bottom: 0;
      }

      .pageList {
        text-align: center;
        margin-top: 30px;
      }
      //查询黑色按钮样式
      .normal-btn {
        background: #333333;
        color: white;
        margin-left: 20px;
      }
    }
  }
}
</style>
<style lang='scss'>
// .customClass {
//   .el-button--primary {
//     background-color: #ffc200 !important ;
//   }
// }
</style>
