import i18n from '@/lang'

const flavorKeys: { [text: string]: string } = {
  甜味: 'sweetness',
  无糖: 'noSugar',
  少糖: 'lessSugar',
  半糖: 'halfSugar',
  多糖: 'moreSugar',
  全糖: 'fullSugar',
  温度: 'temperature',
  热饮: 'hot',
  常温: 'roomTemperature',
  去冰: 'noIce',
  少冰: 'lessIce',
  多冰: 'moreIce',
  忌口: 'avoid',
  不要葱: 'noScallion',
  不要蒜: 'noGarlic',
  不要香菜: 'noCilantro',
  不要辣: 'noChili',
  辣度: 'spiciness',
  不辣: 'notSpicy',
  微辣: 'mild',
  中辣: 'medium',
  重辣: 'extraSpicy'
}

export const flavorLabel = (text: string) => {
  const key = `dish.flavor.${flavorKeys[text]}`
  return flavorKeys[text] && i18n.te(key) ? (i18n.t(key) as string) : text
}
