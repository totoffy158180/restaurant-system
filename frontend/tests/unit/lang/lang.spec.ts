import Cookies from 'js-cookie';
import i18n, { translateError, getLocale } from '@/lang';
import { flavorLabel } from '@/utils/flavor';
import zhTW from '@/lang/zh-TW.json';
import en from '@/lang/en.json';

const keysOf = (obj: any, prefix = ''): string[] =>
    Object.keys(obj).reduce((keys: string[], key) =>
        typeof obj[key] === 'object'
            ? keys.concat(keysOf(obj[key], `${prefix}${key}.`))
            : keys.concat(`${prefix}${key}`), []);

describe('lang', () => {
    afterEach(() => {
        i18n.locale = 'zh-TW';
        Cookies.remove('language');
    });

    it('zh-TW and en have the same keys', () => {
        expect(keysOf(en).sort()).toEqual(keysOf(zhTW).sort());
    });

    it('translates backend error keys', () => {
        i18n.locale = 'zh-TW';
        expect(translateError('PASSWORD_ERROR')).toBe('密碼錯誤');
        i18n.locale = 'en';
        expect(translateError('PASSWORD_ERROR')).toBe('Incorrect password');
    });

    it('passes data to error messages', () => {
        i18n.locale = 'en';
        expect(translateError('ALREADY_EXIST', { 'name': 'zhangsan' })).toBe('zhangsan already exists');
    });

    it('falls back to unknown error for unknown keys', () => {
        i18n.locale = 'en';
        expect(translateError('SOME_NEW_KEY')).toBe('Unknown error');
    });

    it('keeps plain text messages', () => {
        expect(translateError('plain message')).toBe('plain message');
    });

    it('reads supported language from cookie', () => {
        Cookies.set('language', 'en');
        expect(getLocale()).toBe('en');
        Cookies.set('language', 'fr');
        expect(getLocale()).toBe('zh-TW');
    });

    it('translates known flavors and keeps unknown ones', () => {
        i18n.locale = 'en';
        expect(flavorLabel('不要葱')).toBe('No Scallion');
        expect(flavorLabel('自制口味')).toBe('自制口味');
        i18n.locale = 'zh-TW';
        expect(flavorLabel('不要葱')).toBe('不要蔥');
    });
});
