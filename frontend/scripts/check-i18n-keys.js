const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '..')
const srcDir = path.join(root, 'src')
const langDir = process.argv[2] ? path.resolve(process.argv[2]) : path.join(srcDir, 'lang')
const languages = ['zh-TW', 'en']
const namespaces = ['common', 'route', 'navbar', 'password', 'login', 'component', 'employee', 'category', 'dish', 'setmeal', 'order', 'dashboard', 'statistics']
const dynamicPrefixes = ['errors.', 'dish.flavor.', 'order.reasonDialog.reject.', 'order.reasonDialog.cancel.']
const ignored = new Set(['route.path'])

const walk = (dir) => fs.readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
  const full = path.join(dir, entry.name)
  if (entry.isDirectory()) {
    return walk(full)
  }
  return /\.(vue|ts)$/.test(entry.name) ? [full] : []
})

const flatten = (obj, prefix = '') => Object.keys(obj).flatMap((key) =>
  obj[key] !== null && typeof obj[key] === 'object'
    ? flatten(obj[key], `${prefix}${key}.`)
    : [`${prefix}${key}`])

const used = new Set()
const literal = new RegExp(`['"]((?:${namespaces.join('|')})\\.[A-Za-z0-9_.]+)['"]`, 'g')
for (const file of walk(srcDir)) {
  const source = fs.readFileSync(file, 'utf8')
  for (const match of source.matchAll(/\$t\(\s*'([A-Za-z0-9_.]+)'/g)) {
    used.add(match[1])
  }
  for (const match of source.matchAll(literal)) {
    used.add(match[1])
  }
}
ignored.forEach((key) => used.delete(key))

const keysByLanguage = {}
for (const language of languages) {
  const file = path.join(langDir, `${language}.json`)
  if (!fs.existsSync(file)) {
    console.error(`Missing translation file: ${file}`)
    process.exit(1)
  }
  keysByLanguage[language] = new Set(flatten(JSON.parse(fs.readFileSync(file, 'utf8'))))
}

let failed = false
for (const language of languages) {
  const missing = [...used].filter((key) => !keysByLanguage[language].has(key)).sort()
  if (missing.length) {
    const isBase = language === languages[0]
    failed = failed || isBase
    const log = isBase ? console.error : console.warn
    log(`[${language}] ${missing.length} key(s) used in code but missing${isBase ? '' : ' (falls back to ' + languages[0] + ')'}:`)
    missing.forEach((key) => log(`  - ${key}`))
  }
}

const base = keysByLanguage[languages[0]]
const unused = [...base].filter((key) => !used.has(key) && !dynamicPrefixes.some((prefix) => key.startsWith(prefix))).sort()
if (unused.length) {
  console.warn(`${unused.length} key(s) not referenced in code:`)
  unused.forEach((key) => console.warn(`  - ${key}`))
}

if (failed) {
  process.exit(1)
}
console.log(`i18n keys OK: ${used.size} used, ${base.size} in ${languages[0]}.json`)
