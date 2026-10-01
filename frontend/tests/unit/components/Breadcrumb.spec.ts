import { mount, createLocalVue } from '@vue/test-utils';
import VueRouter from 'vue-router';
import VueI18n from 'vue-i18n';
import ElementUI from 'element-ui';
import Breadcrumb from '@/components/Breadcrumb/index.vue';

const localVue = createLocalVue();
localVue.use(VueRouter);
localVue.use(VueI18n);
localVue.use(ElementUI);

const Empty = { 'render': (h: any) => h('div') };

const routes = [
    {
        'path': '/',
        'component': Empty,
        'children': [{
            'path': 'dashboard',
            'component': Empty
        }]
    },
    {
        'path': '/menu',
        'component': Empty,
        'children': [{
            'path': 'menu1',
            'component': Empty,
            'meta': { 'title': 'route.menu1' },
            'children': [{
                'path': 'menu1-1',
                'component': Empty,
                'meta': { 'title': 'route.menu11' }
            },
            {
                'path': 'menu1-2',
                'component': Empty,
                'redirect': 'noredirect',
                'meta': { 'title': 'route.menu12' },
                'children': [{
                    'path': 'menu1-2-1',
                    'component': Empty,
                    'meta': { 'title': 'route.menu121' }
                },
                {
                    'path': 'menu1-2-2',
                    'component': Empty
                }]
            }]
        }]
    }];

const i18n = new VueI18n({
    'locale': 'en',
    'messages': {
        'en': { 'route': { 'menu1': 'Menu 1', 'menu11': 'Menu 1-1', 'menu12': 'Menu 1-2', 'menu121': 'Menu 1-2-1' } }
    }
});

const router = new VueRouter({ routes });

const wrapper = mount(Breadcrumb, { localVue, router, i18n });

const visit = async (path: string) => {
    await router.push(path).catch(() => undefined);
    await wrapper.vm.$nextTick();
    return wrapper.findAll('.el-breadcrumb__inner');
};

describe('Breadcrumb.vue', () => {
    it('route without title has no breadcrumb', async () => {
        expect((await visit('/dashboard')).length).toBe(0);
    });

    it('normal route', async () => {
        expect((await visit('/menu/menu1')).length).toBe(1);
    });

    it('nested route', async () => {
        expect((await visit('/menu/menu1/menu1-2/menu1-2-1')).length).toBe(3);
    });

    it('translates route titles', async () => {
        const items = await visit('/menu/menu1/menu1-2/menu1-2-1');
        expect(items.at(0).text()).toBe('Menu 1');
        expect(items.at(2).text()).toBe('Menu 1-2-1');
    });

    it('earlier breadcrumbs are links', async () => {
        const items = await visit('/menu/menu1/menu1-2/menu1-2-2');
        expect(items.at(0).find('a').text()).toBe('Menu 1');
    });

    it('noredirect and last breadcrumb are not links', async () => {
        const items = await visit('/menu/menu1/menu1-2/menu1-2-1');
        expect(items.at(1).contains('a')).toBe(false);
        expect(items.at(2).contains('a')).toBe(false);
    });
});
