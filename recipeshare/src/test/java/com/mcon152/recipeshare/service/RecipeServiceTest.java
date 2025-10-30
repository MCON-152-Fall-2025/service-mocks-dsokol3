package com.mcon152.recipeshare.service;

import com.mcon152.recipeshare.Recipe;
import com.mcon152.recipeshare.repository.RecipeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Assignment: Implement all TODOs using Mockito features covered in class:
 *  - @Mock, @InjectMocks, @Captor, @ExtendWith(MockitoExtension.class)
 *  - Stubbing: thenReturn / thenAnswer / thenThrow
 *  - Verifications: verify(...), times/never/atLeast..., verifyNoMoreInteractions
 *  - InOrder (where meaningful)
 *  - Void stubbing: doNothing / doThrow (use deleteById for this)
 *  - Matchers: any(), eq(), argThat()
 *  - ArgumentCaptor
 *  - (Optional) Spy demo if you introduce a small helper in tests
 *
 * NOTE: This is a pure unit test. Do NOT start a Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeService (Mockito) — Assignment Skeleton")
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeServiceImpl recipeService; // CUT implements RecipeService

    @Captor
    private ArgumentCaptor<Recipe> recipeCaptor;

    // --- Helpers for sample data ---

    private Recipe newRecipeNoId() {
        return new Recipe(
                null,
                "Chocolate Cake",
                "Moist chocolate cake",
                "flour, eggs, cocoa",
                "mix, bake",
                8
        );
    }

    private Recipe savedRecipe(long id) {
        return new Recipe(
                id,
                "Chocolate Cake",
                "Moist chocolate cake",
                "flour, eggs, cocoa",
                "mix, bake",
                8
        );
    }

    // ------------------ addRecipe ------------------

    @Nested
    @DisplayName("addRecipe(Recipe)")
    class AddRecipe {

        @Test
        @DisplayName("returns saved entity (thenReturn) and calls repository.save once")
        void returnsSaved_andSavesOnce() {
            // TODO:
            // 1) when(recipeRepository.save(...)).thenReturn(savedRecipe(1L))
            // 2) call recipeService.addRecipe(newRecipeNoId())
            // 3) assert non-null id and fields
            // 4) verify(recipeRepository).save(any(Recipe.class)); verifyNoMoreInteractions(recipeRepository)

            //See code below as an example answer

            Recipe input = newRecipeNoId();
            Recipe saved = savedRecipe(1L);

            when(recipeRepository.save(any(Recipe.class))).thenReturn(saved);

            Recipe out = recipeService.addRecipe(input);
            assertEquals(1L, out.getId());
            assertEquals(saved, out);

            verify(recipeRepository).save(any(Recipe.class));
            verifyNoMoreInteractions(recipeRepository);
        }

        @Test
        @DisplayName("assigns ID dynamically (thenAnswer) and captures argument")
        void assignsId_thenAnswer_andCaptures() {
            // TODO:
            // 1) Use thenAnswer to return a new Recipe with id=1L, copying fields from arg
            // 2) capture the arg with ArgumentCaptor and assert title, id==null pre-save

            //See code below as an example answer

            when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> {
                Recipe r = inv.getArgument(0);
                return new Recipe(1L, r.getTitle(), r.getDescription(),
                        r.getIngredients(), r.getInstructions(), r.getServings());
            });

            Recipe out = recipeService.addRecipe(newRecipeNoId());
            assertEquals(1L, out.getId());

            verify(recipeRepository).save(recipeCaptor.capture());
            Recipe sent = recipeCaptor.getValue();
            assertNull(sent.getId()); // before persistence
            assertEquals("Chocolate Cake", sent.getTitle());
        }

        @Test
        @DisplayName("propagates repository failure (thenThrow)")
        void propagatesRepositoryFailure() {
            // TODO:
            when(recipeRepository.save(any())).thenThrow(new IllegalStateException("DB down"));
            assertThrows(IllegalStateException.class,
                    () -> recipeService.addRecipe(newRecipeNoId()));

            verify(recipeRepository).save(any());
        }
    }

    // ------------------ getAllRecipes ------------------

    @Nested
    @DisplayName("getAllRecipes()")
    class GetAllRecipes {

        @Test
        @DisplayName("returns list from repository")
        void returnsList() {
            // TODO:

            List<Recipe> fake = List.of(savedRecipe(1L), savedRecipe(2L));

            when(recipeRepository.findAll()).thenReturn(fake);

            List<Recipe> out = recipeService.getAllRecipes();

            assertEquals(fake.size(), out.size());
            assertTrue(out.containsAll(fake));

            verify(recipeRepository).findAll();
            verifyNoMoreInteractions(recipeRepository);
        }
    }

    // ------------------ getRecipeById ------------------

    @Nested
    @DisplayName("getRecipeById(long)")
    class GetById {

        @Test
        @DisplayName("returns Optional.present when found")
        void present() {
            when(recipeRepository.findById(1L)).thenReturn(Optional.of(savedRecipe(1L)));

            Optional<Recipe> result = recipeService.getRecipeById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
            verify(recipeRepository).findById(1L);
        }

        @Test
        @DisplayName("returns Optional.empty when missing")
        void empty() {
            // TODO: stub Optional.empty, assert empty
            when(recipeRepository.findById(1L)).thenReturn(Optional.empty());

            Optional<Recipe> result = recipeService.getRecipeById(1L);

            assertTrue(result.isEmpty());
            verify(recipeRepository).findById(1L);

        }
    }

    // ------------------ deleteRecipe ------------------

    @Nested
    @DisplayName("deleteRecipe(long)")
    class DeleteRecipe {

        @Test
        @DisplayName("returns true when entity existed")
        void returnsTrue_whenExists() {
            // TODO:
            // when(recipeRepository.existsById(id)).thenReturn(true)
            // doNothing().when(recipeRepository).deleteById(id)
            // assert true; verify order: existsById -> deleteById
            long id = 1L;
            when(recipeRepository.existsById(id)).thenReturn(true);
            doNothing().when(recipeRepository).deleteById(id);

            boolean result = recipeService.deleteRecipe(id);

            assertTrue(result);

            InOrder order = inOrder(recipeRepository);
            order.verify(recipeRepository).existsById(id);
            order.verify(recipeRepository).deleteById(id);
        }

        @Test
        @DisplayName("returns false when missing (never deletes)")
        void returnsFalse_whenMissing() {
            // TODO: existsById -> false; assert false; verify deleteById never called
            long id = 1L;
            when(recipeRepository.existsById(id)).thenReturn(false);

            // Act
            boolean result = recipeService.deleteRecipe(id);

            // Assert
            assertFalse(result); // the service should return false
            verify(recipeRepository).existsById(id); // it did check existence
            verify(recipeRepository, never()).deleteById(anyLong()); // it never tried to delete
        }

        @Test
        @DisplayName("propagates delete error (doThrow)")
        void propagatesDeleteError() {
            // TODO: existsById -> true; doThrow(...) on deleteById; assertThrows
            long id = 1L;
            when(recipeRepository.existsById(id)).thenReturn(true);
            doThrow(new IllegalStateException("Delete failed")).when(recipeRepository).deleteById(id);

            assertThrows(IllegalStateException.class, () -> recipeService.deleteRecipe(id));

            verify(recipeRepository).existsById(id);
            verify(recipeRepository).deleteById(id);
        }
    }

    // ------------------ updateRecipe ------------------

    @Nested
    @DisplayName("updateRecipe(long, Recipe)")
    class UpdateRecipe {

        @Test
        @DisplayName("returns updated entity when exists")
        void returnsUpdated_whenExists() {
            // TODO:
            // findById -> present(existing)
            // save(...) -> updatedSaved
            // assert Optional.present & fields updated
            // capture arg and assert values
            long id = 1L;
            Recipe existing = savedRecipe(id);
            Recipe updated = new Recipe(id, "Updated Title", existing.getDescription(), existing.getIngredients(), existing.getInstructions(), existing.getServings());

            when(recipeRepository.findById(id)).thenReturn(Optional.of(existing));
            when(recipeRepository.save(any(Recipe.class))).thenReturn(updated);

            Optional<Recipe> result = recipeService.updateRecipe(id, updated);

            assertTrue(result.isPresent());
            assertEquals("Updated Title", result.get().getTitle());

            verify(recipeRepository).findById(id);
            verify(recipeRepository).save(recipeCaptor.capture());
            Recipe captured = recipeCaptor.getValue();
            assertEquals("Updated Title", captured.getTitle());
        }

        @Test
        @DisplayName("returns empty when entity missing")
        void returnsEmpty_whenMissing() {
            // TODO: findById -> empty; assert Optional.empty; verify save never called
            long id = 1L;
            when(recipeRepository.findById(id)).thenReturn(Optional.empty());

            Optional<Recipe> result = recipeService.updateRecipe(id, newRecipeNoId());

            assertTrue(result.isEmpty());
            verify(recipeRepository).findById(id);
            verify(recipeRepository, never()).save(any());
        }

        @Test
        @DisplayName("applies only non-null fields (argThat)")
        void appliesNonNullFields_only() {
            // TODO:
            // findById -> present(existing)
            // provide partial with only title set
            // repository.save returns the modified entity (use thenAnswer echo)
            // verify save(argThat(...)) to ensure unchanged fields remain as-is
            long id = 1L;
            Recipe existing = savedRecipe(id);
            Recipe partialUpdate = new Recipe(null, "New Title", null, null, null, null);

            when(recipeRepository.findById(id)).thenReturn(Optional.of(existing));
            when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

            Optional<Recipe> result = recipeService.patchRecipe(id, partialUpdate);

            assertTrue(result.isPresent());
            assertEquals("New Title", result.get().getTitle());
            assertEquals(existing.getDescription(), result.get().getDescription());

            verify(recipeRepository).findById(id);
            verify(recipeRepository).save(argThat(recipe ->
                    "New Title".equals(recipe.getTitle()) &&
                    existing.getDescription().equals(recipe.getDescription())
            ));
        }

        @Test
        @DisplayName("returns empty when entity missing")
        void returnsEmpty_whenMissing_patch() {
            // TODO: findById -> empty; assert Optional.empty; verify save never called
            long id = 1L;
            when(recipeRepository.findById(id)).thenReturn(Optional.empty());

            Optional<Recipe> result = recipeService.patchRecipe(id, newRecipeNoId());

            assertTrue(result.isEmpty());
            verify(recipeRepository).findById(id);
            verify(recipeRepository, never()).save(any());
        }

        @Test
        @DisplayName("consecutive stubs on existsById (true, false)")
        void consecutiveStubs_existsById() {
            // TODO: when(existsById(1L)).thenReturn(true, false); verify two calls and no more
            long id = 1L;
            when(recipeRepository.existsById(id)).thenReturn(true, false);

            assertTrue(recipeRepository.existsById(id));
            assertFalse(recipeRepository.existsById(id));

            verify(recipeRepository, times(2)).existsById(id);
            verifyNoMoreInteractions(recipeRepository);
        }
    }
}
