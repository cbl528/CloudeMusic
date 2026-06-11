import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'
import Recommend from '@/views/Recommend.vue'
import Toplist from '@/views/Toplist.vue'
import Artists from '@/views/Artists.vue'
import Search from '@/views/Search.vue'
import Favorites from '@/views/Favorites.vue'
import History from '@/views/History.vue'
import Profile from '@/views/Profile.vue'
import Settings from '@/views/Settings.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MainLayout,
      children: [
        { path: '', name: 'recommend', component: Recommend },
        { path: 'toplist', name: 'toplist', component: Toplist },
        { path: 'artists', name: 'artists', component: Artists },
        { path: 'search', name: 'search', component: Search },
        { path: 'favorites', name: 'favorites', component: Favorites },
        { path: 'history', name: 'history', component: History },
        { path: 'profile', name: 'profile', component: Profile },
        { path: 'settings', name: 'settings', component: Settings },
      ],
    },
  ],
})

export default router
