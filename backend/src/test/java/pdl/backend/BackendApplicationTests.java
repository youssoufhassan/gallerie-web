package pdl.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import pdl.backend.controller.ImageController;
import pdl.backend.controller.ImageInteractionController;
import pdl.backend.controller.UserController;
import pdl.backend.dao.ImageDao;
import pdl.backend.dao.ImageInteractionDao;
import pdl.backend.dao.UserDao;
import pdl.backend.model.Image;
import pdl.backend.model.User;
import pdl.backend.utils.ImageSimilarityService;
import pdl.backend.utils.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BackendApplicationTests {

    // ========================================================
    //  ImageController test suite
    // ========================================================
    @Nested
    @DisplayName("ImageController")
    @WebMvcTest(ImageController.class)
    @AutoConfigureMockMvc(addFilters = false)
    class ImageControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean private ImageDao imageDao;
        @MockBean private UserDao userDao;
        @MockBean private ImageSimilarityService imageSimilarityService;
        @MockBean private ImageInteractionDao imageInteractionDao;

        private Image fakeImage;

        @BeforeEach
        void setUp() {
            fakeImage = new Image("test.jpg", MediaType.IMAGE_JPEG, "fake-bytes".getBytes(), 1L);

            try {
                var idField = Image.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(fakeImage, 1L);
            } catch (Exception ignored) {}

            fakeImage.addKeyword("nature");
        }

        @Test
        @DisplayName("GET /images/{id} → 200 if image exists")
        void getImage_found() throws Exception {
            when(imageDao.retrieve(185L)).thenReturn(Optional.of(fakeImage));

            mockMvc.perform(get("/images/185"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.IMAGE_JPEG));
        }

        @Test
        @DisplayName("GET /images/{id} → 404 if not found")
        void getImage_notFound() throws Exception {
            when(imageDao.retrieve(1L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/images/1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /images/{id} → 204 if exists")
        void deleteImage_found() throws Exception {
            when(imageDao.retrieve(185L)).thenReturn(Optional.of(fakeImage));
            doNothing().when(imageDao).delete(any(Image.class));

            mockMvc.perform(delete("/images/185"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /images/{id} → 404 if not found")
        void deleteImage_notFound() throws Exception {
            when(imageDao.retrieve(1L)).thenReturn(Optional.empty());

            mockMvc.perform(delete("/images/1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /images → 201 valid file")
        void addImage_valid() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", "fake-image-content".getBytes()
            );

            doNothing().when(imageDao).create(any(Image.class));

            mockMvc.perform(multipart("/images")
                            .file(file)
                            .param("userId", "1"))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("POST /images → 415 unsupported type")
        void addImage_unsupportedType() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.gif", "image/gif", "fake-gif".getBytes()
            );

            mockMvc.perform(multipart("/images")
                            .file(file)
                            .param("userId", "1"))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("POST /images → 400 empty file")
        void addImage_emptyFile() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "empty.jpg", "image/jpeg", new byte[0]
            );

            mockMvc.perform(multipart("/images")
                            .file(file)
                            .param("userId", "1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /images?userId=1 → list")
        void getImageList() throws Exception {
            when(imageDao.retrieveAllForUser(1L)).thenReturn(List.of(fakeImage));

            mockMvc.perform(get("/images").param("userId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("test.jpg"));
        }

        @Test
        @DisplayName("PUT /images/{id}/keywords → add")
        void addKeyword_found() throws Exception {
            when(imageDao.retrieve(1L)).thenReturn(Optional.of(fakeImage));
            doNothing().when(imageDao).addKeyword(anyLong(), anyString());

            mockMvc.perform(put("/images/1/keywords").param("tag", "sunset"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("PUT /images/{id}/keywords → 404")
        void addKeyword_notFound() throws Exception {
            when(imageDao.retrieve(99L)).thenReturn(Optional.empty());

            mockMvc.perform(put("/images/99/keywords").param("tag", "sunset"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /images/{id}/keywords → remove")
        void removeKeyword_found() throws Exception {
            when(imageDao.retrieve(1L)).thenReturn(Optional.of(fakeImage));
            doNothing().when(imageDao).removeKeyword(anyLong(), anyString());

            mockMvc.perform(delete("/images/1/keywords").param("tag", "nature"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /images/{id}/keywords → bad request")
        void removeKeyword_notPresent() throws Exception {
            when(imageDao.retrieve(1L)).thenReturn(Optional.of(fakeImage));

            mockMvc.perform(delete("/images/1/keywords").param("tag", "inexistant"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Search → found")
        void searchByKeyword_found() throws Exception {
            when(imageDao.retrieveAll()).thenReturn(List.of(fakeImage));

            mockMvc.perform(get("/images/search").param("keyword", "nature"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("test.jpg"));
        }

        @Test
        @DisplayName("Search → not found")
        void searchByKeyword_notFound() throws Exception {
            when(imageDao.retrieveAll()).thenReturn(List.of(fakeImage));

            mockMvc.perform(get("/images/search").param("keyword", "xyz"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Visibility toggle → authorized")
        void toggleVisibility_authorized() throws Exception {
            when(imageDao.retrieve(1L)).thenReturn(Optional.of(fakeImage));
            doNothing().when(imageDao).updateVisibility(anyLong(), anyBoolean());

            mockMvc.perform(put("/images/1/visibility").param("userId", "1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Visibility toggle → forbidden")
        void toggleVisibility_forbidden() throws Exception {
            when(imageDao.retrieve(1L)).thenReturn(Optional.of(fakeImage));

            mockMvc.perform(put("/images/1/visibility").param("userId", "99"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET public images")
        void getPublicImages() throws Exception {
            when(imageDao.retrieveAllPublic()).thenReturn(List.of(fakeImage));
            when(userDao.findById(1L)).thenReturn(Optional.of(new User()));
            when(imageInteractionDao.countLikes(anyLong())).thenReturn(3);
            when(imageInteractionDao.getComments(anyLong())).thenReturn(List.of());

            mockMvc.perform(get("/images/public"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].likes").value(3));
        }
    }

    // ========================================================
    //  ImageInteractionController test suite
    // ========================================================
    @Nested
    @DisplayName("ImageInteractionController")
    @WebMvcTest(ImageInteractionController.class)
    @AutoConfigureMockMvc(addFilters = false)
    class ImageInteractionControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ImageInteractionDao interactionDao;

        @Test
        @DisplayName("GET like → true")
        void hasUserLiked_true() throws Exception {
            when(interactionDao.hasUserLiked(1L, 1L)).thenReturn(true);

            mockMvc.perform(get("/images/1/like").param("userId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));
        }

        @Test
        @DisplayName("GET like → false")
        void hasUserLiked_false() throws Exception {
            when(interactionDao.hasUserLiked(1L, 1L)).thenReturn(false);

            mockMvc.perform(get("/images/1/like").param("userId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }

        @Test
        @DisplayName("POST like add/remove")
        void toggleLike_add() throws Exception {
            when(interactionDao.hasUserLiked(1L, 1L)).thenReturn(false);
            doNothing().when(interactionDao).addLike(1L, 1L);

            mockMvc.perform(post("/images/1/like").param("userId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Like ajouté"));
        }

        @Test
        @DisplayName("POST like remove")
        void toggleLike_remove() throws Exception {
            when(interactionDao.hasUserLiked(1L, 1L)).thenReturn(true);
            doNothing().when(interactionDao).removeLike(1L, 1L);

            mockMvc.perform(post("/images/1/like").param("userId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Like retiré"));
        }

        @Test
        @DisplayName("Count likes")
        void countLikes() throws Exception {
            when(interactionDao.countLikes(1L)).thenReturn(42);

            mockMvc.perform(get("/images/1/likes/count"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("42"));
        }

        @Test
        @DisplayName("Add comment")
        void addComment_success() throws Exception {
            doNothing().when(interactionDao).addComment(1L, 1L, "Super photo !");

            mockMvc.perform(post("/images/1/comment")
                            .param("userId", "1")
                            .param("comment", "Super photo !"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Commentaire ajouté"));
        }

        @Test
        @DisplayName("Get comments")
        void getComments_success() throws Exception {
            ImageInteractionDao.Comment c = Mockito.mock(ImageInteractionDao.Comment.class);
            when(c.getUserId()).thenReturn(1L);
            when(c.getComment()).thenReturn("Bonne photo");
            when(interactionDao.getComments(1L)).thenReturn(List.of(c));

            mockMvc.perform(get("/images/1/comments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ========================================================
    //  UserController test suite
    // ========================================================
    @Nested
    @DisplayName("UserController")
    @WebMvcTest(UserController.class)
    @AutoConfigureMockMvc(addFilters = false)
    class UserControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private UserService userService;

        private User fakeUser;

        @BeforeEach
        void setUp() {
            fakeUser = new User();
            fakeUser.setUsername("alice");
            fakeUser.setPassword("password123");

            try {
                var idField = User.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(fakeUser, 1L);
            } catch (Exception ignored) {}
        }

        @Test
        @DisplayName("POST register success")
        void register_success() throws Exception {
            when(userService.register(any(User.class))).thenReturn(fakeUser);

            mockMvc.perform(post("/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(fakeUser)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST register duplicate")
        void register_duplicate() throws Exception {
            when(userService.register(any(User.class)))
                    .thenThrow(new RuntimeException("Username déjà utilisé"));

            mockMvc.perform(post("/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(fakeUser)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST login success")
        void login_success() throws Exception {
            when(userService.login("alice", "password123")).thenReturn(fakeUser);

            mockMvc.perform(post("/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(fakeUser)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST login unauthorized")
        void login_unauthorized() throws Exception {
            when(userService.login(anyString(), anyString()))
                    .thenThrow(new RuntimeException("Identifiants invalides"));

            mockMvc.perform(post("/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(fakeUser)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET user by id")
        void getUserById_found() throws Exception {
            when(userService.findById(1L)).thenReturn(Optional.of(fakeUser));

            mockMvc.perform(get("/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("alice"));
        }
    }
}