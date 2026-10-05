<template>
  <div class="profile-dropdown">
    <!-- BOUTON PROFIL -->
    <button class="profile-btn" @click="toggleProfileMenu">
      <div class="avatar">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
          <circle cx="12" cy="7" r="4"/>
        </svg>
      </div>

      <!-- USERNAME -->
      <span class="profile-name">
        {{ user?.username || 'Profil' }}
      </span>

      <svg class="chevron" :class="{ rotate: showProfileMenu }" viewBox="0 0 24 24">
        <polyline points="6 9 12 15 18 9"/>
      </svg>
    </button>

    <!-- MENU -->
    <div v-if="showProfileMenu" class="dropdown-menu">

      <!-- INFOS USER -->
      <div class="dropdown-header">
        <div class="user-avatar">👤</div>

        <div class="user-info">
          <span class="user-name">
            {{ user?.prenom }} {{ user?.name }}

            <!-- EDIT BUTTON -->
            <button class="edit-btn" @click="toggleEdit">
              ✏️
            </button>
          </span>

          <span class="user-email">
            {{ user?.username }}
          </span>
        </div>
      </div>

      <!-- FORMULAIRE EDIT -->
      <div v-if="editMode" class="edit-form">
        <input v-model="editPrenom" placeholder="Prénom" />
        <input v-model="editName" placeholder="Nom" />

        <div class="edit-actions">
          <button @click="saveProfile">💾 Sauvegarder</button>
          <button @click="cancelEdit">❌ Annuler</button>
        </div>
      </div>

      <div class="dropdown-divider"></div>

      <a href="#" class="dropdown-item logout" @click.prevent="logout">
        Déconnexion
      </a>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import axios from 'axios';
import router from '../router';

interface User {
  id: number;
  username: string;
  name: string;
  prenom: string;
}

export default defineComponent({
  name: 'UserDropdown',
  setup() {
    const showProfileMenu = ref(false);
    const user = ref<User | null>(null);

    const editMode = ref(false);
    const editName = ref('');
    const editPrenom = ref('');

    const toggleProfileMenu = () => {
      showProfileMenu.value = !showProfileMenu.value;
    };

    // 🔹 FETCH USER
    const fetchUserInfo = async () => {
      const userId = localStorage.getItem('userId');
      if (!userId) return;

      try {
        const response = await axios.get<User>(
          `http://localhost:8080/users/${userId}`
        );
        user.value = response.data;

      } catch (err) {
        console.error('Erreur récupération utilisateur :', err);
      }
    };

    // 🔹 EDIT MODE
    const toggleEdit = () => {
      if (!user.value) return;

      editMode.value = true;
      editName.value = user.value.name;
      editPrenom.value = user.value.prenom;
    };

    const cancelEdit = () => {
      editMode.value = false;
    };

    // 🔹 SAVE PROFILE
    const saveProfile = async () => {
      if (!user.value) return;

      try {
        await axios.put(
          `http://localhost:8080/users/${user.value.id}`,
          {
            name: editName.value,
            prenom: editPrenom.value
          }
        );

        // update local
        user.value.name = editName.value;
        user.value.prenom = editPrenom.value;

        editMode.value = false;

        alert("Profil mis à jour !");
      } catch (err) {
        console.error("Erreur update :", err);
      }
    };

    const logout = () => {
      localStorage.removeItem('userId'); // Supprime l'ID utilisateur
      router.push('/');             // Redirige vers la page de login
    };

    onMounted(fetchUserInfo);

    return {
      showProfileMenu,
      toggleProfileMenu,
      user,
      logout,
      editMode,
      editName,
      editPrenom,
      toggleEdit,
      saveProfile,
      cancelEdit
    };
  }
});
</script>

<style scoped>
.profile-dropdown {
  position: relative;
}

.profile-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  background: #333;
  border-radius: 20px;
  color: white;
  cursor: pointer;
}

.avatar {
  width: 30px;
  height: 30px;
}

.profile-name {
  font-weight: bold;
}

.chevron {
  width: 15px;
}

.rotate {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  right: 0;
  background: white;
  width: 260px;
  border-radius: 10px;
  margin-top: 10px;
  padding: 10px;
}

.dropdown-header {
  display: flex;
  gap: 10px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: bold;
}

.user-email {
  font-size: 12px;
  color: gray;
}

.edit-btn {
  margin-left: 10px;
  cursor: pointer;
  border: none;
  background: none;
}

.edit-form {
  margin-top: 10px;
}

.edit-form input {
  width: 100%;
  margin-bottom: 5px;
}

.edit-actions {
  display: flex;
  gap: 5px;
}

.dropdown-item.logout {
  color: red;
  cursor: pointer;
}
</style>