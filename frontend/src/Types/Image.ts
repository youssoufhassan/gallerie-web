export interface ImageItem {
  id: number;           // l'ID renvoyé par backend
  name: string;         // nom de l'image
  keywords?: string[];  // mots-clés associés
  url?: string;         // URL construite côté frontend pour <img>
}