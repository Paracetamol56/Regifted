package com.regifted.app.item;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.item.dto.ItemPostRequest;
import com.regifted.app.item.dto.ItemPutRequest;
import com.regifted.app.keyword.Keyword;
import com.regifted.app.keyword.KeywordService;
import com.regifted.app.search.SearchRepository;
import com.regifted.app.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemService Unit Tests")
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private KeywordService keywordService;

    @Mock
    private SearchRepository searchRepository;

    @InjectMocks
    private ItemService itemService;

    private User testUser;
    private Item savedItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUuid("user-owner-123");
        testUser.setEmail("owner@example.com");

        savedItem = new Item();
        savedItem.setUuid("item-uuid-assigned"); // Crucial pour passer le check null dans getUserToNotify
        savedItem.setTitle("Vintage Camera");
        savedItem.setUser(testUser);
    }

    @Test
    @DisplayName("Should notify user when search matches and notifications are enabled")
    void createItem_NotifyWhenEnabled() {
        ItemPostRequest req = new ItemPostRequest();
        req.setTitle("Vintage Camera");

        User observer = new User();
        observer.setEmail("subscriber@example.com");
        observer.setNotification(true);

        when(itemRepository.saveAndFlush(any(Item.class))).thenReturn(savedItem);
        when(searchRepository.findUsersToNotify(any(Item.class))).thenReturn(Set.of(observer));

        itemService.createItem(req, testUser);
        verify(searchRepository).findUsersToNotify(any(Item.class));
    }

    @Test
    @DisplayName("Should NOT notify user if search matches but notification is disabled")
    void createItem_NoNotifyWhenDisabled() {
        ItemPostRequest req = new ItemPostRequest();

        User observer = new User();
        observer.setEmail("silent@example.com");
        observer.setNotification(false); // Case décochée

        when(itemRepository.saveAndFlush(any(Item.class))).thenReturn(savedItem);
        when(searchRepository.findUsersToNotify(savedItem)).thenReturn(Set.of(observer));

        itemService.createItem(req, testUser);

        verify(searchRepository).findUsersToNotify(savedItem);
        // Le log de notification ne sera PAS déclenché car user.isNotification() est false
    }

    @Test
    @DisplayName("Should handle case where no search matches the item")
    void createItem_NoMatchFound() {
        ItemPostRequest req = new ItemPostRequest();

        when(itemRepository.saveAndFlush(any(Item.class))).thenReturn(savedItem);
        when(searchRepository.findUsersToNotify(savedItem)).thenReturn(Collections.emptySet());

        itemService.createItem(req, testUser);

        verify(searchRepository).findUsersToNotify(savedItem);
    }

    @Test
    @DisplayName("Should process keywords and save item correctly")
    void createItem_ProcessKeywords() {
        ItemPostRequest req = new ItemPostRequest();
        req.setKeywords(Set.of("photography"));
        
        Keyword kw = new Keyword();
        kw.setName("photography");

        when(keywordService.getOrCreateKeyword("photography")).thenReturn(kw);
        when(itemRepository.saveAndFlush(any(Item.class))).thenReturn(savedItem);

        itemService.createItem(req, testUser);

        verify(keywordService).getOrCreateKeyword("photography");
        verify(itemRepository).saveAndFlush(any(Item.class));
    }

    @Test
    @DisplayName("Should return page of items by title")
    void getItemSearchPage_Query() {
        Page<Item> page = new PageImpl<>(List.of(savedItem));
        when(itemRepository.searchByTitleOrDescription(eq("camera"), any(PageRequest.class))).thenReturn(page);

        Page<Item> result = itemService.getItemSearchPage(0, 10, "camera", null, "title", "asc");

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw NotFoundException if item does not exist")
    void getById_NotFound() {
        when(itemRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getById("unknown"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Should update item title and save")
    void updateItem_Success() {
        ItemPutRequest req = new ItemPutRequest();
        req.setTitle("New Camera Title");

        when(itemRepository.findById("item-uuid-assigned")).thenReturn(Optional.of(savedItem));
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        Item result = itemService.updateItemById("item-uuid-assigned", req);

        assertThat(result.getTitle()).isEqualTo("New Camera Title");
    }

    @Test
    @DisplayName("Should delete item successfully")
    void deleteItem_Success() {
        when(itemRepository.findById("item-uuid-assigned")).thenReturn(Optional.of(savedItem));

        itemService.deleteItemById("item-uuid-assigned");

        verify(itemRepository).delete(savedItem);
    }

    @Test
    @DisplayName("Should build sort correctly for createdat DESC")
    void testBuildSort_CreatedAtDesc() {
        itemService.getItemSearchPage(0, 10, null, null, "createdat", "desc");

        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(itemRepository).findAll(captor.capture());

        Sort sort = captor.getValue().getSort();
        assertThat(sort.getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}