<template>
  <el-dialog
    :title="$t('password.title')"
    :visible.sync="dialogFormVisible"
    width="568px"
    class="pwdCon"
    @close="handlePwdClose()"
  >
    <el-form :model="form" :label-width="$i18n.locale === 'en' ? '150px' : '85px'" :rules="rules" ref="form">
      <el-form-item :label="$t('password.oldPassword')" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          :placeholder="$t('common.pleaseEnter')"
        ></el-input>
      </el-form-item>
      <el-form-item :label="$t('password.newPassword')" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          :placeholder="$t('password.rule')"
        ></el-input>
      </el-form-item>
      <el-form-item :label="$t('password.confirmPassword')" prop="affirmPassword">
        <el-input
          v-model="form.affirmPassword"
          type="password"
          :placeholder="$t('common.pleaseEnter')"
        ></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="handlePwdClose()">{{ $t('common.cancel') }}</el-button>
      <el-button type="primary" @click="handleSave()">{{ $t('common.save') }}</el-button>
    </div>
  </el-dialog>
</template>
<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator'
import { Form as ElForm, Input } from 'element-ui'
// 接口
import { editPassword } from '@/api/users'
@Component({
  name: 'Password',
})
export default class extends Vue {
  @Prop() private dialogFormVisible!: any
  private validatePwd = (rule: any, value: any, callback: Function) => {
    const reg = /^[0-9A-Za-z]{6,20}$/
    if (!value) {
      callback(new Error(this.$t('common.pleaseEnter') as string))
    } else if (!reg.test(value)) {
      callback(new Error(this.$t('password.rule') as string))
    } else {
      callback()
    }
  }
  private validatePass2 = (rule, value, callback) => {
    if (!value) {
      callback(new Error(this.$t('password.reenter') as string))
    } else if (value !== this.form.newPassword) {
      callback(new Error(this.$t('password.mismatch') as string))
    } else {
      callback()
    }
  }
  rules = {
    oldPassword: [{ validator: this.validatePwd, trigger: 'blur' }],
    newPassword: [{ validator: this.validatePwd, trigger: 'blur' }],
    affirmPassword: [{ validator: this.validatePass2, trigger: 'blur' }],
  }
  private form = {} as any
  private affirmPassword = ''
  handleSave() {
    ;(this.$refs.form as ElForm).validate(async (valid: boolean) => {
      if (valid) {
        const parnt = {
          oldPassword: this.form.oldPassword,
          newPassword: this.form.newPassword,
        }
        await editPassword(parnt)
        this.$emit('handleclose')
        ;(this.$refs.form as ElForm).resetFields()
      } else {
        return false
      }
    })
  }
  handlePwdClose() {
    ;(this.$refs.form as ElForm).resetFields()
    this.$emit('handleclose')
  }
}
</script>
<style lang="scss">
.navbar {
  .pwdCon {
    .el-dialog__body {
      padding-top: 60px;
      padding: 60px 100px 0;
    }
    .el-input__inner {
      padding: 0 12px;
    }
    .el-form-item {
      margin-bottom: 26px;
    }
    .el-form-item__label {
      text-align: left;
    }
    .el-dialog__footer {
      padding-top: 14px;
    }
  }
}
</style>