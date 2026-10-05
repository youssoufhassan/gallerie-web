<template>
  <div class="post">
    <!-- Header profil -->
    <div class="post-header">
      <div class="avatar">{{ userInitials }}</div>
      <div class="username">{{ post.userName }}</div>
    </div>

    <!-- Image -->
    <div class="post-image">
      <img :src="post.imageUrl" :alt="'Image de ' + post.userName" />
    </div>

    <!-- Actions (Like, toggle comments) -->
    <div class="post-actions">
      <button @click="toggleLike">
        <span v-if="liked">❤️</span>
        <span v-else>🤍</span>
      </button>
      <span>{{ post.likes }} like{{ post.likes > 1 ? 's' : '' }}</span>
      <button @click="toggleComments">
        💬 {{ post.comments.length }}
      </button>
    </div>

    <!-- Commentaires -->
    <div v-if="showComments" class="post-comments">
      <div v-for="(c, i) in post.comments" :key="i" class="comment">
        <strong>{{ c.userName }}:</strong> {{ c.text }}
      </div>

      <!-- Formulaire ajouter commentaire -->
      <div class="add-comment">
        <input v-model="newComment" type="text" placeholder="Ajouter un commentaire..." />
        <button @click="addComment">Envoyer</button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted, type PropType } from 'vue';
import axios from 'axios';

interface Comment {
  userId: number;
  userName: string;
  text: string;
  createdAt?: string;
}

interface PostType {
  id: number;
  userId: number;
  userName: string;
  imageUrl: string;
  likes: number;
  comments: Comment[];
}

export default defineComponent({
  name: 'Post',
  props: {
    post: {
      type: Object as PropType<PostType>,
      required: true
    },
    currentUserId: {
      type: Number,
      required: true
    }
  },
  setup(props) {
    const liked = ref(false);
    const showComments = ref(false);
    const newComment = ref('');

    // ===========================
    // Initialiser l'état liked depuis le backend
    // ===========================
    onMounted(async () => {
      try {
        const res = await axios.get(`http://localhost:8080/images/${props.post.id}/like`, {
          params: { userId: props.currentUserId }
        });
        liked.value = res.data; // true ou false
      } catch (err) {
        console.error('Erreur récupération état like:', err);
      }
    });

    // ===========================
    // Toggle like
    // ===========================
    const toggleLike = async () => {
      try {
        await axios.post(`http://localhost:8080/images/${props.post.id}/like`, null, {
          params: { userId: props.currentUserId }
        });

        // Mise à jour locale
        liked.value = !liked.value;
        liked.value ? props.post.likes++ : props.post.likes--;
      } catch (err) {
        console.error('Erreur like/unlike:', err);
      }
    };

    // ===========================
    // Toggle affichage commentaires
    // ===========================
    const toggleComments = () => { showComments.value = !showComments.value; }

    // ===========================
    // Ajouter un commentaire
    // ===========================
    const addComment = async () => {
      const text = newComment.value.trim();
      if (!text) return;

      try {
        await axios.post(`http://localhost:8080/images/${props.post.id}/comment`, null, {
          params: { userId: props.currentUserId, comment: text }
        });

        // Ajout local
        props.post.comments.push({
          userId: props.currentUserId,
          userName: 'Moi',
          text,
          createdAt: new Date().toISOString()
        });
        newComment.value = '';
      } catch (err) {
        console.error('Erreur ajout commentaire:', err);
      }
    };

    // ===========================
    // Calculer les initiales
    // ===========================
    const userInitials = computed(() => {
      const name = props.post.userName?.trim() || 'U';
      return name
        .split(' ')
        .map(n => n[0]?.toUpperCase() || '')
        .join('')
        .slice(0, 2);
    });

    return {
      liked,
      showComments,
      newComment,
      toggleLike,
      toggleComments,
      addComment,
      userInitials
    };
  }
});
</script>

<style scoped>
.post {
  border: 1px solid #ccc;
  border-radius: 12px;
  margin-bottom: 20px;
  background: #fff;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.post-header {
  display: flex;
  align-items: center;
  padding: 10px;
  gap: 10px;
  background: #f5f5f5;
}

.avatar {
  width: 40px;
  height: 40px;
  background: #6c63ff;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.username {
  font-weight: 600;
}

.post-image img {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
}

.post-actions {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
}

.post-actions button {
  cursor: pointer;
  background: none;
  border: none;
  font-size: 1.2rem;
}

.post-comments {
  padding: 10px;
}

.comment {
  margin-bottom: 5px;
}

.add-comment {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.add-comment input {
  flex: 1;
  padding: 5px 10px;
}

.add-comment button {
  padding: 5px 10px;
  cursor: pointer;
}
</style>