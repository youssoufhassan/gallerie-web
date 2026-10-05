import { createRouter, createWebHistory } from 'vue-router'
import AuthPage from './components/AuthPage.vue'
import ImageGallery from './components/ImageGallery.vue'
import Journal from './components/Journal.vue'

const routes = [
  {
    path: '/',
    name: 'login',
    component: AuthPage
  },
  {
    path: '/gallery',
    name: 'gallery',
    component: ImageGallery
  },
  {
    path: '/Journal',
    name: 'Journal',
    component: Journal
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router