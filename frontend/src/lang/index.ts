import Vue from 'vue'
import VueI18n from 'vue-i18n'
import elementZhTW from 'element-ui/lib/locale/lang/zh-TW'
import elementEn from 'element-ui/lib/locale/lang/en'
import { getLanguage, setLanguage } from '@/utils/cookies'
import zhTW from './zh-TW.json'
import en from './en.json'

Vue.use(VueI18n)

const messages = {
  'zh-TW': { ...elementZhTW, ...zhTW },
  en: { ...elementEn, ...en }
}

export const defaultLanguage = 'zh-TW'

export const languages = [
  { value: 'en', label: 'EN' },
  { value: 'zh-TW', label: '繁中' }
]

export const getLocale = () => {
  const language = getLanguage()
  return language && Object.keys(messages).includes(language) ? language : defaultLanguage
}

const i18n = new VueI18n({
  locale: getLocale(),
  fallbackLocale: defaultLanguage,
  messages
})

export const changeLanguage = (language: string) => {
  i18n.locale = language
  setLanguage(language)
  document.documentElement.lang = language
}

export const translateError = (msg: string, data?: any) => {
  const key = `errors.${msg}`
  if (i18n.te(key)) {
    return i18n.t(key, data && typeof data === 'object' ? data : undefined) as string
  }
  if (/^[A-Z][A-Z0-9_]*$/.test(msg)) {
    return i18n.t('errors.UNKNOWN_ERROR') as string
  }
  return msg
}

export default i18n
