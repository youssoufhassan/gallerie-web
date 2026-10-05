<template>
  <div class="journal-wrapper">
    <!-- Navbar -->
    <nav class="navbar">
      <div class="navbar-brand">
        <svg class="logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="2" y="2" width="20" height="20" rx="5" ry="5"/>
          <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"/>
          <line x1="17.5" y1="6.5" x2="17.51" y2="6.5"/>
        </svg>
        <span>Journal</span>
      </div>
<div class="navbar-menu">
        <router-link to="/gallery" class="nav-btn gallery-link">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
          </svg>
          Galerie
        </router-link>
        <button class="nav-btn" @click="loadPublicImages">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"/>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
          </svg>
          Actualiser
        </button>
        <UserDropdown />
      </div>
    </nav>

    <!-- Main Feed -->
    <div class="feed-container">
      <div class="feed">
        <!-- Post Card -->
        <article v-for="post in posts" :key="post.id" class="post-card">
          <!-- Post Header -->
          <header class="post-header">
            <div class="user-info">
              <div class="avatar">
                <span>{{ getInitials(post.userName) }}</span>
              </div>
              <div class="user-details">
                <span class="username">{{ post.userName }}</span>
                <span class="post-meta">{{ post.name }}</span>
              </div>
            </div>
            <button class="more-btn">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <circle cx="12" cy="12" r="1.5"/>
                <circle cx="6" cy="12" r="1.5"/>
                <circle cx="18" cy="12" r="1.5"/>
              </svg>
            </button>
          </header>

          <!-- Post Image -->
          <div class="post-image">
            <img
              :src="`http://localhost:8080/images/${post.id}`"
              :alt="post.name"
              @dblclick="toggleLike(post)"
            />
            <transition name="heart-pop">
              <div v-if="post.showHeart" class="heart-animation">
                <svg viewBox="0 0 24 24" fill="#FFEDAC">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
              </div>
            </transition>
          </div>

          <!-- Post Actions -->
          <div class="post-actions">
            <div class="actions-left">
              <button
                class="action-btn like-btn"
                :class="{ liked: post.hasLiked }"
                @click="toggleLike(post)"
              >
                <svg viewBox="0 0 24 24" :fill="post.hasLiked ? '#D4A574' : 'none'" stroke="currentColor" stroke-width="2">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
              </button>
              <button class="action-btn" @click="focusComment(post.id)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
                </svg>
              </button>
              <button class="action-btn">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="22" y1="2" x2="11" y2="13"/>
                  <polygon points="22 2 15 22 11 13 2 9 22 2"/>
                </svg>
              </button>
            </div>
            <button class="action-btn bookmark-btn">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
              </svg>
            </button>
          </div>

          <!-- Likes Count -->
          <div class="likes-count">
            <span>{{ post.likes }} {{ post.likes === 1 ? 'like' : 'likes' }}</span>
          </div>

          <!-- Keywords -->
          <div v-if="post.keywords && post.keywords.length > 0" class="keywords">
            <span v-for="kw in post.keywords" :key="kw" class="keyword-tag">#{{ kw }}</span>
          </div>

          <!-- Comments Section -->
          <div class="comments-section">
            <button
              v-if="post.comments && post.comments.length > 2 && !post.showAllComments"
              class="view-all-comments"
              @click="post.showAllComments = true"
            >
              Voir les {{ post.comments.length }} commentaires
            </button>

            <div class="comments-list">
              <div
                v-for="(comment, idx) in getDisplayedComments(post)"
                :key="idx"
                class="comment"
              >
                <span class="comment-user">{{ comment.userName }}</span>
                <span class="comment-text">{{ comment.text }}</span>
              </div>
            </div>
          </div>

          <!-- Add Comment -->
          <div class="add-comment">
            <div class="emoji-btn">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <path d="M8 14s1.5 2 4 2 4-2 4-2"/>
                <line x1="9" y1="9" x2="9.01" y2="9"/>
                <line x1="15" y1="9" x2="15.01" y2="9"/>
              </svg>
            </div>
            <input
              :ref="el => { if (el) commentInputRefs[post.id] = el as HTMLInputElement }"
              type="text"
              v-model="post.newComment"
              placeholder="Ajouter un commentaire..."
              @keyup.enter="addComment(post)"
            />
            <button
              class="post-comment-btn"
              :disabled="!post.newComment?.trim()"
              @click="addComment(post)"
            >
              Publier
            </button>
          </div>
        </article>

        <!-- Empty State -->
        <div v-if="posts.length === 0 && !loading" class="empty-state">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
          </svg>
          <h3>Aucune publication</h3>
          <p>Il n&apos;y a pas encore de photos partagées dans le journal</p>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <p>Chargement du journal...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, reactive } from 'vue';
import axios from 'axios';
import UserDropdown from './UserDropdown.vue';

interface Comment {
  userName: string;
  text: string;
}

interface Post {
  id: number;
  name: string;
  userName: string;
  likes: number;
  hasLiked: boolean;
  comments: Comment[];
  keywords: string[];
  showAllComments: boolean;
  showHeart: boolean;
  newComment: string;
}

export default defineComponent({
  name: 'Journal',
  components: { UserDropdown },
  setup() {
    const API_BASE = 'http://localhost:8080/images';
    const posts = ref<Post[]>([]);
    const loading = ref(true);
    const userId = ref<number | null>(null);
    const commentInputRefs = reactive<Record<number, HTMLInputElement>>({});

    // ✅ Lit 'userId' comme sauvegardé par le login
    const getCurrentUserId = (): number | null => {
      const storedId = localStorage.getItem('userId');
      return storedId ? parseInt(storedId) : null;
    };

    const loadPublicImages = async () => {
      loading.value = true;
      try {
        const response = await axios.get(`${API_BASE}/public`);
        const data = response.data;

        const loadedPosts: Post[] = data.map((item: any) => ({
          id: item.id,
          name: item.name || 'Sans nom',
          userName: item.userName || 'Anonyme',
          likes: item.likes || 0,
          hasLiked: false,
          comments: [],
          keywords: item.keywords || [],
          showAllComments: false,
          showHeart: false,
          newComment: ''
        }));

        // ✅ GET /{id}/like?userId=X — vérifie si l'user a liké chaque post
        if (userId.value) {
          for (const post of loadedPosts) {
            try {
              const res = await axios.get(`${API_BASE}/${post.id}/like`, {
                params: { userId: userId.value }
              });
              post.hasLiked = res.data;
            } catch (err) {
              console.error(`Erreur like status post ${post.id}:`, err);
            }
          }
        }

        // ✅ GET /{id}/comments — charge les commentaires pour chaque post
        for (const post of loadedPosts) {
          try {
            const res = await axios.get(`${API_BASE}/${post.id}/comments`);
            post.comments = (res.data || []).map((c: any) => ({
            userName: c.userName || 'Anonyme',
            text: c.comment
          }));
          } catch (err) {
            console.error(`Erreur chargement commentaires post ${post.id}:`, err);
          }
        }

        // ✅ GET /{id}/likes/count — charge le nombre de likes pour chaque post
        for (const post of loadedPosts) {
          try {
            const res = await axios.get(`${API_BASE}/${post.id}/likes/count`);
            post.likes = res.data || 0;
          } catch (err) {
            console.error(`Erreur chargement likes post ${post.id}:`, err);
          }
        }

        posts.value = loadedPosts;
      } catch (err) {
        console.error('Erreur chargement images publiques:', err);
      } finally {
        loading.value = false;
      }
    };

    // ✅ POST /{id}/like?userId=X — toggle like/unlike
    const toggleLike = async (post: Post) => {
      if (!userId.value) {
        alert('Vous devez être connecté pour liker');
        return;
      }
      try {
        await axios.post(`${API_BASE}/${post.id}/like`, null, {
          params: { userId: userId.value }
        });
        if (post.hasLiked) {
          post.likes--;
          post.hasLiked = false;
        } else {
          post.likes++;
          post.hasLiked = true;
          post.showHeart = true;
          setTimeout(() => { post.showHeart = false; }, 1000);
        }
      } catch (err) {
        console.error('Erreur toggle like:', err);
      }
    };

    // ✅ POST /{id}/comment?userId=X&comment=texte
    const addComment = async (post: Post) => {
      if (!userId.value) {
        alert('Vous devez être connecté pour commenter');
        return;
      }
      const commentText = post.newComment?.trim();
      if (!commentText) return;

      try {
        await axios.post(`${API_BASE}/${post.id}/comment`, null, {
          params: {
            userId: userId.value,
            comment: commentText
          }
        });

        // ✅ Lit le username sauvegardé par le login dans localStorage
        const username = localStorage.getItem('username') || 'Moi';

        post.comments.push({
          userName: username,
          text: commentText
        });

        post.newComment = '';
      } catch (err) {
        console.error('Erreur ajout commentaire:', err);
      }
    };

    const getDisplayedComments = (post: Post): Comment[] => {
      if (!post.comments) return [];
      if (post.showAllComments) return post.comments;
      return post.comments.slice(-2);
    };

    const focusComment = (postId: number) => {
      const input = commentInputRefs[postId];
      if (input) input.focus();
    };

    const getInitials = (name: string): string => {
      if (!name) return '?';
      const parts = name.split(' ');
      if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
      return name.substring(0, 2).toUpperCase();
    };

    onMounted(() => {
      userId.value = getCurrentUserId();
      loadPublicImages();
    });

    return {
      posts,
      loading,
      commentInputRefs,
      loadPublicImages,
      toggleLike,
      addComment,
      getDisplayedComments,
      focusComment,
      getInitials
    };
  }
});
</script>

<style scoped>
.journal-wrapper {
  min-height: 100vh;
  background: linear-gradient(135deg, #FFEDAC 0%, #FFF8E7 50%, #F5E6A3 100%);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

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
  font-size: 1.4rem;
  font-weight: 700;
}

.logo-icon { width: 32px; height: 32px; stroke: #FFEDAC; }

.navbar-menu { display: flex; align-items: center; gap: 12px; }

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

.nav-btn:hover { background: rgba(212, 165, 116, 0.4); transform: translateY(-2px); }
.nav-btn svg { width: 18px; height: 18px; }

.nav-btn.gallery-link {
  text-decoration: none;
  background: linear-gradient(135deg, #5D4037, #3E2723);
  border: 1px solid rgba(255, 237, 172, 0.3);
  position: relative;
  overflow: hidden;
}

.nav-btn.gallery-link::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 237, 172, 0.2), transparent);
  transition: left 0.5s ease;
}

.nav-btn.gallery-link:hover::before {
  left: 100%;
}

.nav-btn.gallery-link:hover {
  background: linear-gradient(135deg, #FFEDAC, #D4A574);
  color: #3E2723;
  box-shadow: 0 4px 15px rgba(212, 165, 116, 0.4);
}

.feed-container { max-width: 520px; margin: 0 auto; padding: 24px 16px; }

.post-card {
  background: #FFF8E7;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(62, 39, 35, 0.1);
  overflow: hidden;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
}

.user-info { display: flex; align-items: center; gap: 12px; }

.avatar {
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, #D4A574, #3E2723);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #FFEDAC;
  font-size: 0.9rem;
}

.user-details { display: flex; flex-direction: column; }
.username { font-weight: 600; color: #3E2723; font-size: 0.95rem; }
.post-meta { font-size: 0.8rem; color: #8D6E63; }

.more-btn {
  width: 32px;
  height: 32px;
  background: transparent;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5D4037;
  transition: all 0.2s ease;
}

.more-btn:hover { background: rgba(62, 39, 35, 0.1); }
.more-btn svg { width: 20px; height: 20px; }

.post-image {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  background: #F5E6A3;
  overflow: hidden;
  cursor: pointer;
}

.post-image img { width: 100%; height: 100%; object-fit: cover; }

.heart-animation {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.heart-animation svg {
  width: 100px;
  height: 100px;
  filter: drop-shadow(0 4px 20px rgba(62, 39, 35, 0.3));
}

.heart-pop-enter-active { animation: heartPop 1s ease; }
.heart-pop-leave-active { animation: heartPop 0.3s ease reverse; }

@keyframes heartPop {
  0%   { transform: translate(-50%, -50%) scale(0); opacity: 0; }
  15%  { transform: translate(-50%, -50%) scale(1.2); opacity: 1; }
  30%  { transform: translate(-50%, -50%) scale(0.95); }
  45%  { transform: translate(-50%, -50%) scale(1); }
  80%  { transform: translate(-50%, -50%) scale(1); opacity: 1; }
  100% { transform: translate(-50%, -50%) scale(1); opacity: 0; }
}

.post-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
}

.actions-left { display: flex; gap: 16px; }

.action-btn {
  width: 28px;
  height: 28px;
  background: transparent;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3E2723;
  transition: all 0.2s ease;
}

.action-btn svg { width: 26px; height: 26px; }
.action-btn:hover { transform: scale(1.1); }
.action-btn.like-btn:hover svg,
.action-btn.like-btn.liked svg { stroke: #D4A574; }
.action-btn.like-btn.liked { animation: likeScale 0.3s ease; }

@keyframes likeScale { 50% { transform: scale(1.3); } }

.likes-count { padding: 0 16px; margin-bottom: 8px; }
.likes-count span { font-weight: 600; color: #3E2723; font-size: 0.95rem; }

.keywords {
  padding: 0 16px;
  margin-bottom: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag { color: #D4A574; font-size: 0.9rem; font-weight: 500; cursor: pointer; }
.keyword-tag:hover { text-decoration: underline; }

.comments-section { padding: 0 16px 8px; }

.view-all-comments {
  background: none;
  border: none;
  color: #8D6E63;
  font-size: 0.9rem;
  cursor: pointer;
  padding: 0;
  margin-bottom: 8px;
}

.view-all-comments:hover { color: #5D4037; }

.comments-list { display: flex; flex-direction: column; gap: 6px; }
.comment { font-size: 0.9rem; line-height: 1.4; }
.comment-user { font-weight: 600; color: #3E2723; margin-right: 6px; }
.comment-text { color: #5D4037; }

.add-comment {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid rgba(212, 165, 116, 0.3);
}

.emoji-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #5D4037;
}

.emoji-btn svg { width: 24px; height: 24px; }

.add-comment input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  font-size: 0.9rem;
  color: #3E2723;
}

.add-comment input::placeholder { color: #8D6E63; }

.post-comment-btn {
  background: none;
  border: none;
  color: #D4A574;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.post-comment-btn:hover:not(:disabled) { color: #3E2723; }
.post-comment-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: #FFF8E7;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(62, 39, 35, 0.1);
}

.empty-state svg { width: 80px; height: 80px; stroke: #D4A574; margin-bottom: 20px; }
.empty-state h3 { color: #3E2723; font-size: 1.4rem; margin: 0 0 8px 0; }
.empty-state p { color: #5D4037; margin: 0; }

.loading-state { text-align: center; padding: 60px 20px; }

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(212, 165, 116, 0.3);
  border-top-color: #D4A574;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin { to { transform: rotate(360deg); } }

.loading-state p { color: #5D4037; font-size: 1rem; }

@media (max-width: 600px) {
  .feed-container { padding: 16px 8px; }
  .navbar { padding: 12px 16px; }
  .navbar-brand span { font-size: 1.2rem; }
  .nav-btn span { display: none; }
  .nav-btn { padding: 10px; }
}
</style>
