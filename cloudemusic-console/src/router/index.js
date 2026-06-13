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
import PlaylistDetail from '@/views/PlaylistDetail.vue'
import ArtistDetail from '@/views/ArtistDetail.vue'
import AlbumDetail from '@/views/AlbumDetail.vue'

/** 需要登录才能访问的路由名称 */
const authRoutes = ['profile', 'settings']

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MainLayout,
      children: [
        { path: '', name: 'recommend', component: Recommend, meta: { requiresAuth: false } },
        { path: 'toplist', name: 'toplist', component: Toplist, meta: { requiresAuth: false } },
        { path: 'artists', name: 'artists', component: Artists, meta: { requiresAuth: false } },
        { path: 'playlist/:id', name: 'playlist', component: PlaylistDetail, meta: { requiresAuth: false } },
        { path: 'artist/:id', name: 'artist', component: ArtistDetail, meta: { requiresAuth: false } },
        { path: 'album/:id', name: 'album', component: AlbumDetail, meta: { requiresAuth: false } },
        { path: 'search', name: 'search', component: Search, meta: { requiresAuth: false } },
        { path: 'favorites', name: 'favorites', component: Favorites, meta: { requiresAuth: false } },
        { path: 'history', name: 'history', component: History, meta: { requiresAuth: false } },
        { path: 'profile', name: 'profile', component: Profile, meta: { requiresAuth: true } },
        { path: 'settings', name: 'settings', component: Settings, meta: { requiresAuth: true } },
      ],
    },
  ],
})

/** 路由守卫：未登录禁止直接访问需要登录的页面 */
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    next({ name: 'recommend' })
  } else {
    next()
  }
})

export default router
