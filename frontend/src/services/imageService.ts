import axios from 'axios';
import type { ImageItem } from '../Types/Image.ts';

const API_BASE = 'http://localhost:8080/images';

export const getImages = async (): Promise<ImageItem[]> => {
  try {
    const response = await axios.get<ImageItem[]>(API_BASE);

    // Ajouter l'URL complète pour chaque image
    return response.data.map(img => ({
      ...img,
      url: `${API_BASE}/${img.id}` // ← cette URL servira à <img :src="...">
    }));
  } catch (error) {
    console.error('Erreur récupération des images :', error);
    return [];
  }
};