
<template>
  <div class="app-wrapper">
    <!-- Navbar -->
    <nav class="navbar">
      <div class="navbar-brand">
        <svg class="logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
          <circle cx="8.5" cy="8.5" r="1.5"/>
          <polyline points="21 15 16 10 5 21"/>
        </svg>
        <span>Ma Galerie</span>
      </div>
<div class="navbar-menu">
        <router-link to="/journal" class="nav-btn journal-link">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="2" y="2" width="20" height="20" rx="5" ry="5"/>
            <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"/>
            <line x1="17.5" y1="6.5" x2="17.51" y2="6.5"/>
          </svg>
          Journal
        </router-link>
        <button class="nav-btn upload-trigger" @click="showUploadModal = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="17 8 12 3 7 8"/>
            <line x1="12" y1="3" x2="12" y2="15"/>
          </svg>
          Ajouter
        </button>
        <button class="nav-btn" @click="loadImages">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"/>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
          </svg>
          Actualiser
        </button>
        <UserDropdown />
      </div>
    </nav>

    <!-- Main Content -->
    <div class="main-content" :class="{ 'with-panel': selectedImage }">
      <!-- Gallery Section -->
      <div class="gallery-section">
        <!-- Search Bar -->
        <div class="search-bar">
          <div class="search-input-wrapper">
            <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="M21 21l-4.35-4.35"/>
            </svg>
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="Rechercher par mots-cles..." 
              @keyup.enter="searchImages"
            />
            <button v-if="searchQuery" class="clear-btn" @click="searchQuery = ''; loadImages()">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <button class="search-btn" @click="searchImages">Rechercher</button>
        </div>

        <!-- Images Grid -->
        <div class="gallery-grid">
          <div 
            v-for="img in images" 
            :key="img.id" 
            class="image-card"
            :class="{ 'selected': selectedImage?.id === img.id }"
            @click="selectImage(img)"
          >
            <div class="image-wrapper">
              <img 
                :src="img.url" 
                :alt="img.name || 'Image'" 
                @error="onImageError($event, img.name || '')"
              />
              <div class="image-overlay">
                <span class="image-name">{{ img.name || 'Sans nom' }}</span>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-if="images.length === 0" class="empty-state">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
            <h3>Aucune image</h3>
            <p>Commencez par ajouter des images a votre galerie</p>
            <button class="add-first-btn" @click="showUploadModal = true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"/>
                <line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              Ajouter une image
            </button>
          </div>
        </div>
      </div>

      <!-- Detail Panel -->
      <transition name="slide">
        <div v-if="selectedImage" class="detail-panel">
          <div class="panel-header">
            <h2>Details de l&apos;image</h2>
            <button class="close-panel" @click="selectedImage = null">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>

          <div class="panel-content">
            <!-- Image Preview -->
            <div class="preview-section">
              <img :src="selectedImage.url" :alt="selectedImage.name || 'Image'" />
            </div>

            <!-- Metadata -->
            <div class="metadata-section">
              <h3>Informations</h3>
              <div class="meta-grid">
                <div class="meta-item">
                  <span class="meta-label">Nom</span>
                  <span class="meta-value">{{ selectedMetadata?.name || 'N/A' }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">Type</span>
                  <span class="meta-value">{{ selectedMetadata?.type || 'N/A' }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">Taille</span>
                  <span class="meta-value">{{ selectedMetadata?.size || 'N/A' }}</span>
                </div>
              </div>
            </div>

            <!-- Keywords -->
            <div class="keywords-section">
              <h3>Mots-cles</h3>
              <div class="keywords-list">
                <span v-for="kw in selectedMetadata?.keywords" :key="kw" class="keyword-tag">
                  {{ kw }}
                  <button class="remove-keyword" @click="removeKeyword(kw)">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="18" y1="6" x2="6" y2="18"/>
                      <line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                  </button>
                </span>
                <span v-if="!selectedMetadata?.keywords?.length" class="no-keywords">Aucun mot-cle</span>
              </div>
              <div class="add-keyword-form">
                <input 
                  type="text" 
                  v-model="newKeyword" 
                  placeholder="Nouveau mot-cle..." 
                  @keyup.enter="addKeyword"
                />
                <button @click="addKeyword" :disabled="!newKeyword.trim()">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="12" y1="5" x2="12" y2="19"/>
                    <line x1="5" y1="12" x2="19" y2="12"/>
                  </svg>
                </button>
              </div>
            </div>

<!-- Actions -->
            <div class="actions-section">
              <button class="action-btn visibility" @click="toggleVisibility(selectedImage)">
                <svg v-if="selectedImage?.isPublic" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </svg>
                 Modifier la visibilité 
              </button>
              <button class="action-btn download" @click="downloadImage(selectedImage)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                  <polyline points="7 10 12 15 17 10"/>
                  <line x1="12" y1="15" x2="12" y2="3"/>
                </svg>
                Telecharger
              </button>
              <button class="action-btn delete" @click="deleteImage(selectedImage)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"/>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                  <line x1="10" y1="11" x2="10" y2="17"/>
                  <line x1="14" y1="11" x2="14" y2="17"/>
                </svg>
                Supprimer
              </button>
            </div>

            <!-- Similar Images -->
            <div class="similar-section">
              <h3>Images similaires</h3>
              <div class="similar-options">
                <label>
                  <span>Nombre:</span>
                  <input type="number" v-model.number="similarNumber" min="1" max="20" />
                </label>
                <label>
                  <span>Descripteur:</span>
                  <select v-model="similarDescriptor">
                    <option value="GRADIENT1D">Gradient1D</option>
                    <option value="GRAYSCALE">HS2D</option>
                    <option value="RGB">RGB3D</option>
                  </select>
                </label>
                <button class="find-similar-btn" @click="fetchSimilar(selectedImage)">Rechercher</button>
              </div>
              <div v-if="similarImages.length > 0" class="similar-grid">
                <div v-for="sim in similarImages" :key="sim.id" class="similar-thumb">
                  <img :src="`http://localhost:8080/images/${sim.id}`" :alt="String(sim.id)" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </div>

    <!-- Upload Modal -->
    <transition name="fade">
      <div v-if="showUploadModal" class="modal-overlay" @click.self="showUploadModal = false">
        <div class="upload-modal">
          <div class="modal-header">
            <h2>Ajouter une image</h2>
            <button class="close-modal" @click="showUploadModal = false">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div 
              class="drop-zone" 
              :class="{ 'drag-over': isDragging }"
              @dragover.prevent="isDragging = true"
              @dragleave="isDragging = false"
              @drop.prevent="handleDrop"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/>
                <line x1="12" y1="3" x2="12" y2="15"/>
              </svg>
              <p>Glissez une image ici ou</p>
              <label class="file-label">
                <input type="file" accept="image/*" @change="handleFileUpload" />
                <span>Parcourir</span>
              </label>
              <span v-if="file" class="file-name">{{ file.name }}</span>
            </div>
          </div>
          <div class="modal-footer">
            <button class="cancel-btn" @click="showUploadModal = false; file = null">Annuler</button>
            <button class="submit-btn" @click="submitFile" :disabled="!file">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              Envoyer
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import axios from 'axios';
import type { ImageItem } from '../Types/Image.ts';
import type { ImageMetadata } from '../Types/ImageMetadata.ts';
import UserDropdown from './UserDropdown.vue';

export default defineComponent({
  name: 'ImageGallery',
  components: { UserDropdown },
  setup() {
    const API_BASE = 'http://localhost:8080/images';
    const images = ref<ImageItem[]>([]);
    const selectedImage = ref<ImageItem | null>(null);
    const selectedMetadata = ref<ImageMetadata | null>(null);
    const similarImages = ref<{id:number, score:number}[]>([]);
    const similarNumber = ref(5);
    const similarDescriptor = ref('Gradient1D');
    const file = ref<File | null>(null);
    const newKeyword = ref('');
    const searchQuery = ref('');
    const userId = ref<number | null>(null);
    const showUploadModal = ref(false);
    const isDragging = ref(false);

    onMounted(() => {
      const id = localStorage.getItem('userId');
      if (id) userId.value = Number(id);
      loadImages();
    });

    const loadImages = async () => {
      if (!userId.value) return;
      try {
        const response = await axios.get<ImageItem[]>(API_BASE, {
          params: { userId: userId.value }
        });
        images.value = response.data.map(img => ({
          ...img,
          url: `${API_BASE}/${img.id}`
        }));
      } catch (err) {
        console.error('Erreur récupération images :', err);
      }
    };

    const selectImage = async (img: ImageItem) => {
      selectedImage.value = img;
      similarImages.value = [];
      try {
        const response = await axios.get<ImageMetadata>(`${API_BASE}/${img.id}/metadata`);
        selectedMetadata.value = response.data;
      } catch (err) {
        console.error('Erreur métadonnées :', err);
        selectedMetadata.value = null;
      }
    };

    const fetchSimilar = async (img: ImageItem | null) => {
      if (!img) return;
      try {
        const response = await axios.get(`${API_BASE}/${img.id}/similar`, {
          params: { number: similarNumber.value, descriptor: similarDescriptor.value }
        });
        similarImages.value = response.data;
      } catch (err) {
        console.error(err);
        similarImages.value = [];
      }
    };

    const onImageError = (event: Event, imageName: string) => {
      (event.target as HTMLImageElement).src = 'https://via.placeholder.com/200x200?text=No+Image';
    };

    const downloadImage = async (img: ImageItem | null) => {
      if (!img) return;
      try {
        const response = await axios.get(`${API_BASE}/${img.id}`, { responseType: 'blob' });
        const blob = response.data;
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = img.name || `image_${img.id}.jpg`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
      } catch (err) {
        console.error("Erreur téléchargement :", err);
      }
    };

const handleFileUpload = (event: Event) => {
      const target = event.target as HTMLInputElement;
      if (target.files && target.files.length > 0) file.value = target.files[0] as File;
    };

    const handleDrop = (event: DragEvent) => {
      isDragging.value = false;
      const files = event.dataTransfer?.files;
      if (files && files.length > 0) {
        file.value = files[0] as File;
      }
    };

const submitFile = async () => {
      if (!file.value || !userId.value) return;
      const formData = new FormData();
      formData.append('file', file.value);
      formData.append('userId', userId.value.toString());
      try {
        await axios.post(API_BASE, formData, { headers: { 'Content-Type': 'multipart/form-data' } });
        file.value = null;
        showUploadModal.value = false;
        await loadImages();
      } catch (err) {
        console.error('Erreur ajout image :', err);
      }
    };

    const deleteImage = async (img: ImageItem) => {
      if (!confirm(`Supprimer l'image ${img.name}?`)) return;
      try {
        await axios.delete(`${API_BASE}/${img.id}`);
        if (selectedImage.value?.id === img.id) selectedImage.value = null;
        await loadImages();
      } catch (err) {
        console.error('Erreur suppression image :', err);
      }
    };

    // ======================
    // Ajouter un keyword
    // ======================
    const addKeyword = async () => {
      if (!selectedImage.value || !newKeyword.value.trim()) return;
      try {
        await axios.put(
          `${API_BASE}/${selectedImage.value.id}/keywords`,
          {}, // body vide
          { params: { tag: newKeyword.value.trim() } }
        );
        newKeyword.value = '';
        const response = await axios.get<ImageMetadata>(`${API_BASE}/${selectedImage.value.id}/metadata`);
        selectedMetadata.value = response.data;
      } catch (err) {
        console.error('Erreur ajout mot-clé :', err);
      }
    };

    // ======================
    // Supprimer un keyword
    // ======================
    const removeKeyword = async (kw: string) => {
      if (!selectedImage.value) return;
      try {
        await axios.delete(
          `${API_BASE}/${selectedImage.value.id}/keywords`,
          { params: { tag: kw } }
        );
        const response = await axios.get<ImageMetadata>(`${API_BASE}/${selectedImage.value.id}/metadata`);
        selectedMetadata.value = response.data;
      } catch (err) {
        console.error('Erreur suppression mot-clé :', err);
      }
    };

// ======================
    // Recherche par mot-clé (une seule keyword)
    // ======================
   const searchImages = async () => {
  if (!searchQuery.value.trim()) return;

  try {
    const response = await axios.get<ImageItem[]>(`${API_BASE}/search`, {
      params: { keyword: searchQuery.value.trim() }
    });

    images.value = response.data.map(img => ({
      ...img,
      url: `${API_BASE}/${img.id}`
    }));

    selectedImage.value = null;

  } catch (err) {
    console.error('Erreur recherche images :', err);
    images.value = [];
  }
};

    // ======================
    // Toggle Visibility (public/privé)
    // ======================
    const toggleVisibility = async (img: ImageItem | null) => {
      if (!img || !userId.value) return;
      try {
        const response = await axios.put(`${API_BASE}/${img.id}/visibility`, null, {
          params: { userId: userId.value }
        });
        // Mettre à jour l'état local
        const index = images.value.findIndex(i => i.id === img.id);
        if (index !== -1) {
          images.value[index].isPublic = !images.value[index].isPublic;
        }
        if (selectedImage.value?.id === img.id) {
          selectedImage.value.isPublic = !selectedImage.value.isPublic;
        }
        // Rafraîchir les métadonnées
        const metaResponse = await axios.get<ImageMetadata>(`${API_BASE}/${img.id}/metadata`);
        selectedMetadata.value = metaResponse.data;
        alert(response.data);
      } catch (err) {
        console.error('Erreur modification visibilité :', err);
        alert('Erreur lors de la modification de la visibilité');
      }
    };

return {
      images, selectedImage, selectedMetadata, similarImages,
      similarNumber, similarDescriptor, newKeyword, searchQuery,
      showUploadModal, isDragging, file,
      onImageError, selectImage, fetchSimilar, downloadImage,
      handleFileUpload, handleDrop, submitFile, deleteImage, addKeyword, removeKeyword,
      searchImages, loadImages, toggleVisibility
    };
  }
});
</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  background: linear-gradient(135deg, #FFEDAC 0%, #FFF8E7 50%, #F5E6A3 100%);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Navbar */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 32px;
  background: linear-gradient(135deg, #3E2723 0%, #5D4037 100%);
  box-shadow: 0 4px 20px rgba(62, 39, 35, 0.3);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #FFEDAC;
  font-size: 1.3rem;
  font-weight: 700;
}

.logo-icon {
  width: 28px;
  height: 28px;
  stroke: #FFEDAC;
}

.navbar-menu {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  background: rgba(212, 165, 116, 0.2);
  color: #FFEDAC;
  border: 1px solid rgba(212, 165, 116, 0.4);
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-btn:hover {
  background: rgba(212, 165, 116, 0.4);
  transform: translateY(-2px);
}

.nav-btn svg {
  width: 18px;
  height: 18px;
}

.nav-btn.journal-link {
  text-decoration: none;
  background: linear-gradient(135deg, #5D4037, #3E2723);
  border: 1px solid rgba(255, 237, 172, 0.3);
  position: relative;
  overflow: hidden;
}

.nav-btn.journal-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 237, 172, 0.2), transparent);
  transition: left 0.5s ease;
}

.nav-btn.journal-link:hover::before {
  left: 100%;
}

.nav-btn.journal-link:hover {
  background: linear-gradient(135deg, #FFEDAC, #D4A574);
  color: #3E2723;
  box-shadow: 0 4px 15px rgba(212, 165, 116, 0.4);
}

.nav-btn.upload-trigger {
  background: linear-gradient(135deg, #D4A574, #8D6E63);
  border: none;
}

.nav-btn.upload-trigger:hover {
  background: linear-gradient(135deg, #FFEDAC, #D4A574);
  color: #3E2723;
}

/* Main Content */
.main-content {
  display: flex;
  height: calc(100vh - 60px);
  transition: all 0.3s ease;
}

.main-content.with-panel .gallery-section {
  width: 55%;
}

/* Gallery Section */
.gallery-section {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  transition: all 0.3s ease;
}

/* Search Bar */
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.search-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 16px;
  width: 20px;
  height: 20px;
  stroke: #8D6E63;
}

.search-input-wrapper input {
  width: 100%;
  padding: 14px 44px;
  background: #FFF8E7;
  border: 2px solid #D4A574;
  border-radius: 12px;
  font-size: 0.95rem;
  color: #3E2723;
  outline: none;
  transition: all 0.3s ease;
}

.search-input-wrapper input:focus {
  border-color: #3E2723;
  box-shadow: 0 0 0 4px rgba(212, 165, 116, 0.2);
}

.search-input-wrapper input::placeholder {
  color: #8D6E63;
}

.clear-btn {
  position: absolute;
  right: 12px;
  width: 24px;
  height: 24px;
  background: #D4A574;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.clear-btn svg {
  width: 14px;
  height: 14px;
  stroke: #3E2723;
}

.clear-btn:hover {
  background: #3E2723;
}

.clear-btn:hover svg {
  stroke: #FFEDAC;
}

.search-btn {
  padding: 14px 28px;
  background: linear-gradient(135deg, #3E2723, #5D4037);
  color: #FFEDAC;
  border: none;
  border-radius: 12px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(62, 39, 35, 0.3);
}

/* Gallery Grid */
.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

.image-card {
  position: relative;
  aspect-ratio: 1;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #FFF8E7;
  box-shadow: 0 4px 15px rgba(62, 39, 35, 0.1);
}

.image-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 30px rgba(62, 39, 35, 0.2);
}

.image-card.selected {
  outline: 3px solid #3E2723;
  outline-offset: 2px;
}

.image-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

.image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px;
  background: linear-gradient(transparent, rgba(62, 39, 35, 0.8));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-card:hover .image-overlay {
  opacity: 1;
}

.image-name {
  color: #FFEDAC;
  font-size: 0.85rem;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Empty State */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-state svg {
  width: 80px;
  height: 80px;
  stroke: #D4A574;
  margin-bottom: 20px;
}

.empty-state h3 {
  color: #3E2723;
  font-size: 1.4rem;
  margin: 0 0 8px 0;
}

.empty-state p {
  color: #5D4037;
  margin: 0 0 24px 0;
}

.add-first-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  background: linear-gradient(135deg, #D4A574, #3E2723);
  color: #FFEDAC;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-first-btn svg {
  width: 20px;
  height: 20px;
  stroke: #FFEDAC;
  margin: 0;
}

.add-first-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(62, 39, 35, 0.3);
}

/* Detail Panel */
.detail-panel {
  width: 45%;
  background: #FFF8E7;
  border-left: 1px solid #D4A574;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #D4A574;
  background: linear-gradient(135deg, #FFEDAC, #FFF8E7);
}

.panel-header h2 {
  margin: 0;
  color: #3E2723;
  font-size: 1.2rem;
  font-weight: 600;
}

.close-panel {
  width: 36px;
  height: 36px;
  background: rgba(62, 39, 35, 0.1);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-panel svg {
  width: 20px;
  height: 20px;
  stroke: #3E2723;
}

.close-panel:hover {
  background: #3E2723;
}

.close-panel:hover svg {
  stroke: #FFEDAC;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* Preview Section */
.preview-section {
  background: linear-gradient(145deg, #FFEDAC, #F5E6A3);
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 24px;
}

.preview-section img {
  width: 100%;
  max-height: 280px;
  object-fit: contain;
  border-radius: 12px;
}

/* Metadata Section */
.metadata-section {
  margin-bottom: 24px;
}

.metadata-section h3,
.keywords-section h3,
.similar-section h3 {
  color: #3E2723;
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #D4A574;
}

.meta-grid {
  display: grid;
  gap: 12px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(212, 165, 116, 0.15);
  border-radius: 10px;
}

.meta-label {
  color: #5D4037;
  font-size: 0.9rem;
}

.meta-value {
  color: #3E2723;
  font-weight: 600;
  font-size: 0.9rem;
}

/* Keywords Section */
.keywords-section {
  margin-bottom: 24px;
}

.keywords-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.keyword-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: linear-gradient(135deg, #D4A574, #FFEDAC);
  color: #3E2723;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.remove-keyword {
  width: 18px;
  height: 18px;
  background: #3E2723;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.remove-keyword svg {
  width: 10px;
  height: 10px;
  stroke: #FFEDAC;
}

.remove-keyword:hover {
  background: #5D4037;
  transform: scale(1.1);
}

.no-keywords {
  color: #8D6E63;
  font-style: italic;
  font-size: 0.9rem;
}

.add-keyword-form {
  display: flex;
  gap: 10px;
}

.add-keyword-form input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #D4A574;
  border-radius: 10px;
  font-size: 0.9rem;
  outline: none;
  background: #FFFDF7;
  transition: all 0.3s ease;
}

.add-keyword-form input:focus {
  border-color: #3E2723;
}

.add-keyword-form button {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #D4A574, #3E2723);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.add-keyword-form button svg {
  width: 20px;
  height: 20px;
  stroke: #FFEDAC;
}

.add-keyword-form button:hover:not(:disabled) {
  transform: translateY(-2px);
}

.add-keyword-form button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Actions Section */
.actions-section {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 20px;
  border: none;
  border-radius: 12px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn svg {
  width: 20px;
  height: 20px;
}

.action-btn.download {
  background: linear-gradient(135deg, #3E2723, #5D4037);
  color: #FFEDAC;
}

.action-btn.download:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(62, 39, 35, 0.3);
}

.action-btn.visibility {
  background: linear-gradient(135deg, #8B5CF6, #7C3AED);
  color: #fff;
}

.action-btn.visibility:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(139, 92, 246, 0.4);
}

.action-btn.delete {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: #fff;
}

.action-btn.delete:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.3);
}

/* Similar Section */
.similar-section {
  margin-bottom: 24px;
}

.similar-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.similar-options label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #5D4037;
  font-size: 0.9rem;
}

.similar-options input[type="number"] {
  width: 60px;
  padding: 8px 10px;
  border: 2px solid #D4A574;
  border-radius: 8px;
  font-size: 0.9rem;
  outline: none;
  background: #FFFDF7;
}

.similar-options select {
  padding: 8px 12px;
  border: 2px solid #D4A574;
  border-radius: 8px;
  font-size: 0.9rem;
  outline: none;
  background: #FFFDF7;
  color: #3E2723;
}

.find-similar-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #5D4037, #3E2723);
  color: #FFEDAC;
  border: none;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.find-similar-btn:hover {
  transform: translateY(-2px);
}

.similar-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
}

.similar-thumb {
  position: relative;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  background: #FFEDAC;
}

.similar-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.similar-thumb .score {
  position: absolute;
  bottom: 6px;
  left: 50%;
  transform: translateX(-50%);
  padding: 4px 10px;
  background: rgba(62, 39, 35, 0.9);
  color: #FFEDAC;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
}

/* Upload Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(62, 39, 35, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.upload-modal {
  width: 90%;
  max-width: 480px;
  background: #FFF8E7;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(62, 39, 35, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #FFEDAC, #F5E6A3);
  border-bottom: 1px solid #D4A574;
}

.modal-header h2 {
  margin: 0;
  color: #3E2723;
  font-size: 1.2rem;
}

.close-modal {
  width: 36px;
  height: 36px;
  background: rgba(62, 39, 35, 0.1);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-modal svg {
  width: 20px;
  height: 20px;
  stroke: #3E2723;
}

.close-modal:hover {
  background: #3E2723;
}

.close-modal:hover svg {
  stroke: #FFEDAC;
}

.modal-body {
  padding: 24px;
}

.drop-zone {
  border: 3px dashed #D4A574;
  border-radius: 16px;
  padding: 40px 20px;
  text-align: center;
  transition: all 0.3s ease;
}

.drop-zone.drag-over {
  border-color: #3E2723;
  background: rgba(212, 165, 116, 0.2);
}

.drop-zone svg {
  width: 60px;
  height: 60px;
  stroke: #D4A574;
  margin-bottom: 16px;
}

.drop-zone p {
  color: #5D4037;
  margin: 0 0 16px 0;
}

.file-label {
  display: inline-block;
}

.file-label input {
  display: none;
}

.file-label span {
  display: inline-block;
  padding: 12px 28px;
  background: linear-gradient(135deg, #D4A574, #8D6E63);
  color: #FFF8E7;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.file-label span:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(212, 165, 116, 0.4);
}

.file-name {
  display: block;
  margin-top: 16px;
  color: #3E2723;
  font-weight: 500;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #D4A574;
  background: rgba(212, 165, 116, 0.1);
}

.cancel-btn {
  flex: 1;
  padding: 14px 24px;
  background: transparent;
  color: #5D4037;
  border: 2px solid #D4A574;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: #D4A574;
  color: #3E2723;
}

.submit-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 24px;
  background: linear-gradient(135deg, #3E2723, #5D4037);
  color: #FFEDAC;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-btn svg {
  width: 20px;
  height: 20px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(62, 39, 35, 0.3);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Transitions */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-enter-from .upload-modal,
.fade-leave-to .upload-modal {
  transform: scale(0.9);
}
</style>
