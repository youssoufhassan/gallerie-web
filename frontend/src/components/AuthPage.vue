<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import type { User } from '../Types/User';

const router = useRouter();

// === STATE ===
const isLogin = ref(true);
const isLoading = ref(false);

// Cursor trail
const cursorTrails = ref<Array<{ id: number; x: number; y: number }>>([]);
let trailId = 0;

// Formulaires
const loginForm = ref({ username: '', password: '' });
const registerForm = ref({ nom: '', prenom: '', username: '', password: '', confirmPassword: '' });

// Erreurs
const errors = ref<Record<string, string>>({});

const safePasswordStrength = computed(() => ({
  score: passwordStrength.value?.score ?? 0,
  label: passwordStrength.value?.label ?? '',
  color: passwordStrength.value?.color ?? ''
}));

// === CURSOR TRAIL EFFECT ===
const handleMouseMove = (e: MouseEvent) => {
  const trail = {
    id: trailId++,
    x: e.clientX,
    y: e.clientY
  };
  cursorTrails.value.push(trail);
  
  // Remove trail after animation
  setTimeout(() => {
    cursorTrails.value = cursorTrails.value.filter(t => t.id !== trail.id);
  }, 1000);
};

onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove);
});

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove);
});

// === PASSWORD STRENGTH ===
const passwordStrength = computed(() => {
  const password = registerForm.value.password;
  if (!password) return { score: 0, label: '', color: '' };

  let score = 0;
  if (password.length >= 8) score++;
  if (/[a-z]/.test(password)) score++;
  if (/[A-Z]/.test(password)) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[^a-zA-Z0-9]/.test(password)) score++;

  const levels = [
    { score: 0, label: '', color: '' },
    { score: 1, label: 'Très faible', color: '#C62828' },
    { score: 2, label: 'Faible', color: '#EF6C00' },
    { score: 3, label: 'Moyen', color: '#D4A574' },
    { score: 4, label: 'Fort', color: '#558B2F' },
    { score: 5, label: 'Très fort', color: '#2E7D32' }
  ];

  return levels[score];
});

// === VALIDATION ===
const validateLogin = () => {
  errors.value = {};
  if (!loginForm.value.username) errors.value.username = "Le nom d'utilisateur est requis";
  if (!loginForm.value.password) errors.value.password = 'Le mot de passe est requis';
  return Object.keys(errors.value).length === 0;
};

const validateRegister = () => {
  errors.value = {};
  if (!registerForm.value.nom) errors.value.nom = 'Le nom est requis';
  if (!registerForm.value.prenom) errors.value.prenom = 'Le prénom est requis';
  if (!registerForm.value.username) errors.value.username = "Le nom d'utilisateur est requis";
  if (!registerForm.value.password) errors.value.password = 'Le mot de passe est requis';
  else if (registerForm.value.password.length < 8) errors.value.password = 'Au moins 8 caractères';
  if (registerForm.value.password !== registerForm.value.confirmPassword)
    errors.value.confirmPassword = 'Les mots de passe ne correspondent pas';
  return Object.keys(errors.value).length === 0;
};

// === HANDLERS ===
const handleLogin = async () => {
  if (!validateLogin()) return;

  isLoading.value = true;
  try {
    const response = await axios.post<User & { token: string }>(
      'http://localhost:8080/users/login',
      {
        username: loginForm.value.username,
        password: loginForm.value.password
      }
    );

    const loggedUser = response.data;

    localStorage.setItem('userId', loggedUser.id.toString());
    localStorage.setItem('token', loggedUser.token);

    // 🔥 AJOUTER CECI
    localStorage.setItem('username', loggedUser.username);

    alert('Connexion réussie !');
    router.push('/gallery');
  } catch (err: any) {
    alert(err.response?.data ?? 'Erreur serveur ou réseau');
  } finally {
    isLoading.value = false;
  }
};
const handleRegister = async () => {
  if (!validateRegister()) return;

  isLoading.value = true;
  try {
    const response = await axios.post('http://localhost:8080/users/register', {
      username: registerForm.value.username,
      password: registerForm.value.password,
      name: registerForm.value.nom,
      prenom: registerForm.value.prenom
    });

    alert(`Inscription réussie ! ${response.data}`);
    isLogin.value = true;
    registerForm.value = { nom: '', prenom: '', username: '', password: '', confirmPassword: '' };
  } catch (err: any) {
    alert(err.response?.data ?? 'Erreur serveur ou réseau');
  } finally {
    isLoading.value = false;
  }
};

const toggleForm = () => {
  isLogin.value = !isLogin.value;
  errors.value = {};
};
</script>
<template>
  <div class="auth-container">
    
    <!-- Cursor Trail Effect -->
    <div 
      v-for="trail in cursorTrails" 
      :key="trail.id"
      class="cursor-trail"
      :style="{ left: trail.x + 'px', top: trail.y + 'px' }"
    ></div>
    
    <div class="background-animation">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
      <div class="orb orb-4"></div>
    </div>

    <!-- Floating particles -->
    <div class="particles">
      <div class="particle" v-for="n in 20" :key="n"></div>
    </div>

    <div class="auth-card" :class="{ 'flip': !isLogin }">
      <div class="auth-header">
        <div class="logo-container">
          <svg class="logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>

        <h1 class="auth-title">{{ isLogin ? 'Connexion' : 'Inscription' }}</h1>
        <p class="auth-subtitle">
          {{ isLogin ? 'Connectez-vous à votre compte' : 'Créez votre compte gratuitement' }}
        </p>
      </div>

      <!-- LOGIN -->
      <form v-if="isLogin" @submit.prevent="handleLogin" class="auth-form">

        <div class="form-group">
          <label class="form-label">Email</label>
          <div class="input-wrapper">
           <input
            v-model="loginForm.username"
            type="text"
            class="form-input"
            placeholder="Nom d'utilisateur"
            />
          </div>
          <span v-if="errors.loginEmail" class="error-message">{{ errors.loginEmail }}</span>
        </div>

        <div class="form-group">
          <label class="form-label">Mot de passe</label>
          <div class="input-wrapper">
            <input
              v-model="loginForm.password"
              type="password"
              class="form-input"
              :class="{ error: errors.loginPassword }"
              placeholder="Votre mot de passe"
            />
          </div>
          <span v-if="errors.loginPassword" class="error-message">{{ errors.loginPassword }}</span>
        </div>

        <button type="submit" class="submit-btn" :disabled="isLoading">
          <span v-if="isLoading" class="spinner"></span>
          <span v-else>Se connecter</span>
        </button>

      </form>

      <!-- REGISTER -->
      <form v-else @submit.prevent="handleRegister" class="auth-form">

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Nom</label>
            <input
              v-model="registerForm.nom"
              type="text"
              class="form-input"
              :class="{ error: errors.nom }"
              placeholder="Votre nom"
            />
            <span v-if="errors.nom" class="error-message">{{ errors.nom }}</span>
          </div>

          <div class="form-group">
            <label class="form-label">Prénom</label>
            <input
              v-model="registerForm.prenom"
              type="text"
              class="form-input"
              :class="{ error: errors.prenom }"
              placeholder="Votre prénom"
            />
            <span v-if="errors.prenom" class="error-message">{{ errors.prenom }}</span>
          </div>
        </div>

          <div class="form-group">
        <label class="form-label">Nom d'utilisateur</label>
        <input
          v-model="registerForm.username"
          type="text"         
          class="form-input"
          :class="{ error: errors.username }"
          placeholder="Nom utilisateur"
        />
        <span v-if="errors.username" class="error-message">{{ errors.username }}</span>
      </div>
        <div class="form-group">
          <label class="form-label">Mot de passe</label>
          <input
            v-model="registerForm.password"
            type="password"
            class="form-input"
            :class="{ error: errors.password }"
            placeholder="Minimum 8 caractères"
          />
          <span v-if="errors.password" class="error-message">{{ errors.password }}</span>

          <!-- Indicateur de force -->
          <div v-if="registerForm.password" class="password-strength">
            <div class="strength-bar">
              <div 
                class="strength-fill"
                :style="{ 
                  width: safePasswordStrength.score * 20 + '%', 
                  backgroundColor: safePasswordStrength.color 
                }"
              ></div>
            </div>
            <span class="strength-label" :style="{ color: safePasswordStrength.color }">
              {{ safePasswordStrength.label }}
            </span>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">Confirmer</label>
          <input
            v-model="registerForm.confirmPassword"
            type="password"
            class="form-input"
            :class="{ error: errors.confirmPassword }"
            placeholder="Confirmez le mot de passe"
          />
          <span v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</span>
        </div>

        <button type="submit" class="submit-btn" :disabled="isLoading">
          <span v-if="isLoading" class="spinner"></span>
          <span v-else>Créer mon compte</span>
        </button>
      </form>

      <!-- Basculer -->
      <div class="toggle-section">
        <p class="toggle-text">
          {{ isLogin ? "Vous n'avez pas de compte ?" : "Vous avez déjà un compte ?" }}
          <button type="button" @click="toggleForm" class="toggle-btn">
            {{ isLogin ? "Inscrivez-vous" : "Connectez-vous" }}
          </button>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ========== VARIABLES ========== */
:root {
  --primary: #D4A574;
  --primary-light: #FFEDAC;
  --primary-dark: #3E2723;
  --secondary: #5D4037;
  --accent: #3E2723;
  --background: #FFEDAC;
  --surface: #FFF8E7;
  --surface-light: #FFFDF7;
  --text-primary: #3E2723;
  --text-secondary: #5D4037;
  --error: #C62828;
  --success: #558B2F;
  --cream: #FFEDAC;
  --cream-light: #FFF8E7;
  --cream-dark: #F5E6A3;
}

/* ========== CURSOR TRAIL ========== */
.cursor-trail {
  position: fixed;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(212, 165, 116, 0.8) 0%, rgba(255, 237, 172, 0.5) 50%, transparent 70%);
  pointer-events: none;
  z-index: 9999;
  transform: translate(-50%, -50%);
  animation: trailFade 1s ease-out forwards;
  box-shadow: 
    0 0 20px rgba(212, 165, 116, 0.6),
    0 0 40px rgba(255, 237, 172, 0.5),
    0 0 60px rgba(62, 39, 35, 0.2);
}

@keyframes trailFade {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(2.5);
  }
}

/* ========== CONTAINER PRINCIPAL ========== */
.auth-container {
  height: 100vh;
  max-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFEDAC 0%, #FFF8E7 25%, #F5E6A3 50%, #FFEDAC 75%, #FFF8E7 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
  padding: 10px;
  position: relative;
  overflow: hidden;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* ========== PARTICLES ========== */
.particles {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 8px;
  height: 8px;
  background: radial-gradient(circle, rgba(212, 165, 116, 0.6), transparent);
  border-radius: 50%;
  animation: particleFloat 8s ease-in-out infinite;
}

.particle:nth-child(1) { left: 10%; top: 20%; animation-delay: 0s; }
.particle:nth-child(2) { left: 20%; top: 80%; animation-delay: 0.5s; }
.particle:nth-child(3) { left: 30%; top: 40%; animation-delay: 1s; }
.particle:nth-child(4) { left: 40%; top: 60%; animation-delay: 1.5s; }
.particle:nth-child(5) { left: 50%; top: 30%; animation-delay: 2s; }
.particle:nth-child(6) { left: 60%; top: 70%; animation-delay: 2.5s; }
.particle:nth-child(7) { left: 70%; top: 50%; animation-delay: 3s; }
.particle:nth-child(8) { left: 80%; top: 20%; animation-delay: 3.5s; }
.particle:nth-child(9) { left: 90%; top: 90%; animation-delay: 4s; }
.particle:nth-child(10) { left: 15%; top: 55%; animation-delay: 4.5s; }
.particle:nth-child(11) { left: 25%; top: 15%; animation-delay: 5s; }
.particle:nth-child(12) { left: 35%; top: 85%; animation-delay: 5.5s; }
.particle:nth-child(13) { left: 45%; top: 45%; animation-delay: 6s; }
.particle:nth-child(14) { left: 55%; top: 75%; animation-delay: 6.5s; }
.particle:nth-child(15) { left: 65%; top: 25%; animation-delay: 7s; }
.particle:nth-child(16) { left: 75%; top: 65%; animation-delay: 7.5s; }
.particle:nth-child(17) { left: 85%; top: 35%; animation-delay: 0.25s; }
.particle:nth-child(18) { left: 5%; top: 95%; animation-delay: 0.75s; }
.particle:nth-child(19) { left: 95%; top: 5%; animation-delay: 1.25s; }
.particle:nth-child(20) { left: 50%; top: 95%; animation-delay: 1.75s; }

@keyframes particleFloat {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-30px) rotate(180deg);
    opacity: 0.8;
  }
}

/* ========== ANIMATION BACKGROUND ========== */
.background-animation {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.5;
  animation: float 10s ease-in-out infinite;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #D4A574 0%, #FFEDAC 100%);
  top: -150px;
  left: -150px;
  animation-delay: 0s;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #FFEDAC 0%, #FFF8E7 100%);
  bottom: -100px;
  right: -100px;
  animation-delay: -3s;
}

.orb-3 {
  width: 350px;
  height: 350px;
  background: linear-gradient(135deg, #5D4037 0%, #3E2723 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -5s;
  opacity: 0.2;
}

.orb-4 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #8D6E63 0%, #D4A574 100%);
  top: 20%;
  right: 10%;
  animation-delay: -7s;
  opacity: 0.3;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(40px, -40px) scale(1.1);
  }
  50% {
    transform: translate(-30px, 30px) scale(0.95);
  }
  75% {
    transform: translate(20px, 20px) scale(1.05);
  }
}

/* ========== CARTE PRINCIPALE ========== */
.auth-card {
  width: 100%;
  max-width: 400px;
  max-height: calc(100vh - 20px);
  overflow-y: auto;
  background: rgba(255, 248, 231, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 28px 32px;
  border: 2px solid rgba(212, 165, 116, 0.3);
  box-shadow: 
    0 30px 60px -15px rgba(62, 39, 35, 0.15),
    0 0 0 1px rgba(212, 165, 116, 0.1),
    0 0 80px rgba(212, 165, 116, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  position: relative;
  z-index: 10;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1), 
              box-shadow 0.3s ease;
}

.auth-card::before {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 30px;
  background: linear-gradient(135deg, rgba(212, 165, 116, 0.5), rgba(255, 237, 172, 0.3), rgba(212, 165, 116, 0.5));
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.auth-card:hover {
  transform: translateY(-5px);
  box-shadow: 
    0 40px 80px -20px rgba(62, 39, 35, 0.2),
    0 0 60px rgba(212, 165, 116, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.auth-card:hover::before {
  opacity: 1;
}

.auth-card.flip {
  animation: cardFlip 0.6s ease;
}

@keyframes cardFlip {
  0% {
    transform: rotateY(0deg) scale(1);
  }
  50% {
    transform: rotateY(10deg) scale(0.95);
  }
  100% {
    transform: rotateY(0deg) scale(1);
  }
}

/* ========== HEADER ========== */
.auth-header {
  text-align: center;
  margin-bottom: 20px;
}

.logo-container {
  width: 60px;
  height: 60px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #D4A574 0%, #3E2723 100%);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 15px 35px rgba(212, 165, 116, 0.4),
    0 5px 15px rgba(62, 39, 35, 0.2);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
  overflow: hidden;
}

.logo-container::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, transparent 40%, rgba(255, 255, 255, 0.3) 50%, transparent 60%);
  transform: translateX(-100%);
  transition: transform 0.6s ease;
}

.logo-container:hover {
  transform: translateY(-5px) rotate(5deg);
  box-shadow: 
    0 20px 45px rgba(212, 165, 116, 0.5),
    0 8px 20px rgba(62, 39, 35, 0.3);
}

.logo-container:hover::after {
  transform: translateX(100%);
}

.logo-icon {
  width: 30px;
  height: 30px;
  color: #FFF8E7;
}

.auth-title {
  font-size: 26px;
  font-weight: 700;
  color: #3E2723;
  margin: 0 0 6px 0;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 4px rgba(62, 39, 35, 0.1);
}

.auth-subtitle {
  font-size: 14px;
  color: #5D4037;
  margin: 0;
  font-weight: 500;
}

/* ========== FORMULAIRE ========== */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

@media (max-width: 480px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 12px;
  font-weight: 700;
  color: #3E2723;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  font-size: 14px;
  color: #3E2723;
  background: rgba(255, 237, 172, 0.6);
  border: 2px solid rgba(212, 165, 116, 0.3);
  border-radius: 12px;
  outline: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
  font-weight: 500;
}

.form-input::placeholder {
  color: #8D6E63;
  font-weight: 400;
}

.form-input:hover {
  border-color: rgba(212, 165, 116, 0.5);
  background: rgba(255, 237, 172, 0.8);
}

.form-input:focus {
  border-color: #D4A574;
  background: rgba(255, 248, 231, 0.9);
  box-shadow: 
    0 0 0 4px rgba(212, 165, 116, 0.2),
    0 0 30px rgba(212, 165, 116, 0.15);
}

.form-input.error {
  border-color: #C62828;
  background: rgba(198, 40, 40, 0.05);
}

.form-input.error:focus {
  box-shadow: 0 0 0 4px rgba(198, 40, 40, 0.15);
}

.error-message {
  font-size: 12px;
  color: #C62828;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
  animation: shake 0.4s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

/* ========== INDICATEUR DE FORCE ========== */
.password-strength {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 4px;
}

.strength-bar {
  flex: 1;
  height: 6px;
  background: rgba(212, 165, 116, 0.2);
  border-radius: 3px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.4s ease, background-color 0.4s ease;
}

.strength-label {
  font-size: 12px;
  font-weight: 700;
  min-width: 80px;
  text-align: right;
}

/* ========== BOUTON SUBMIT ========== */
.submit-btn {
  width: 100%;
  padding: 14px 24px;
  font-size: 14px;
  font-weight: 700;
  color: #FFF8E7;
  background: linear-gradient(135deg, #D4A574 0%, #3E2723 50%, #D4A574 100%);
  background-size: 200% 200%;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
  margin-top: 6px;
  box-shadow: 
    0 8px 25px rgba(212, 165, 116, 0.4),
    0 4px 10px rgba(62, 39, 35, 0.2);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  transition: left 0.5s ease;
}

.submit-btn:hover:not(:disabled) {
  background-position: 100% 0;
  transform: translateY(-3px);
  box-shadow: 
    0 12px 35px rgba(212, 165, 116, 0.5),
    0 6px 15px rgba(62, 39, 35, 0.3);
}

.submit-btn:hover:not(:disabled)::before {
  left: 100%;
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 
    0 4px 15px rgba(212, 165, 116, 0.4),
    0 2px 5px rgba(62, 39, 35, 0.2);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* ========== SPINNER ========== */
.spinner {
  display: inline-block;
  width: 24px;
  height: 24px;
  border: 3px solid rgba(255, 248, 231, 0.3);
  border-top-color: #FFF8E7;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ========== TOGGLE SECTION ========== */
.toggle-section {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 2px solid rgba(212, 165, 116, 0.2);
  text-align: center;
}

.toggle-text {
  font-size: 13px;
  color: #5D4037;
  margin: 0;
  font-weight: 500;
}

.toggle-btn {
  background: none;
  border: none;
  color: #D4A574;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  padding: 0;
  margin-left: 6px;
  transition: all 0.3s ease;
  position: relative;
}

.toggle-btn::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #D4A574, #3E2723);
  transition: width 0.3s ease;
}

.toggle-btn:hover {
  color: #3E2723;
}

.toggle-btn:hover::after {
  width: 100%;
}

/* ========== RESPONSIVE ========== */
@media (max-width: 480px) {
  .auth-card {
    padding: 32px 28px;
    border-radius: 24px;
  }

  .logo-container {
    width: 70px;
    height: 70px;
  }

  .logo-icon {
    width: 35px;
    height: 35px;
  }

  .auth-title {
    font-size: 26px;
  }

  .auth-subtitle {
    font-size: 14px;
  }

  .form-input {
    padding: 14px 18px;
    font-size: 14px;
  }

  .submit-btn {
    padding: 16px 24px;
    font-size: 15px;
  }
}
</style>
