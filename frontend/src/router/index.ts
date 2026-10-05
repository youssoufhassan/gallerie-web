import { createRouter, createWebHistory } from 'vue-router'
import AuthPage from '../components/AuthPage.vue'
import ImageGallery from '../components/ImageGallery.vue'
import Accueil from '../components/Accueil.vue' // si tu gardes Accueil séparé

const routes = [
  {
    path: '/login',
    name: 'login',
    component: AuthPage
  },
  {
    path: '/gallery',
    name: 'gallery',
    // galerie utilisateur connecté
    component: ImageGallery,
    props: { userId: Number(localStorage.getItem('userId')) } 
  },
  {
    path: '/accueil',
    name: 'accueil',
    // galerie affichant toutes les images publiques
    component: ImageGallery,
    props: { userId: null } 
  },
  {
    path: '/',
    redirect: '/login' // Page d'accueil par défaut
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router