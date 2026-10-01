<template>
  <div class="language-switch">
    <template v-for="(item, index) in languages">
      <i v-if="index > 0"
         :key="item.value + '-divider'"
         class="divider">|</i>
      <a :key="item.value"
         :class="{ active: item.value === $i18n.locale }"
         @click="handleLanguage(item.value)">{{ item.label }}</a>
    </template>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { languages, changeLanguage } from '@/lang'

@Component({
  name: 'LanguageSwitch'
})
export default class extends Vue {
  private languages = languages

  private handleLanguage(language: string) {
    if (language === this.$i18n.locale) {
      return
    }
    changeLanguage(language)
    if (this.$route.meta && this.$route.meta.title) {
      document.title = this.$t(this.$route.meta.title) as string
    }
  }
}
</script>

<style lang="scss" scoped>
.language-switch {
  font-size: 14px;
  user-select: none;
  white-space: nowrap;
  a {
    color: rgba(51, 51, 51, 0.5);
    cursor: pointer;
    transition: color 0.2s;
    &:hover {
      color: #333333;
    }
    &.active {
      color: #333333;
      font-weight: 700;
      cursor: default;
    }
  }
  .divider {
    margin: 0 8px;
    font-style: normal;
    color: rgba(51, 51, 51, 0.35);
  }
}
</style>
