package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    ItemRepository itemRepository;
    User user = User.builder()
            .id(null)
            .name("Smith")
            .email("smith@mail.ru")
            .build();
    Item item1;
    Item item2;
    Item item3;

    @BeforeEach
    void beforeEach() {
        entityManager.persist(user);
        item1 = Item.builder()
                .id(null)
                .name("pen1")
                .description("pen1")
                .available(true)
                .owner(user)
                .request(null)
                .build();
        item2 = Item.builder()
                .id(null)
                .name("pen2")
                .description("oldPen")
                .available(true)
                .owner(user)
                .request(null)
                .build();
        item3 = Item.builder()
                .id(null)
                .name("pen3")
                .description("newPen")
                .available(true)
                .owner(user)
                .request(null)
                .build();
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);
    }

    @Test
    void shouldReturnAllItems() {
        var items = itemRepository.search("pen");
        assertThat(items, containsInAnyOrder(item1, item2, item3));
        assertThat(items, hasSize(3));
    }


    @Test
    void shouldReturnNoItems() {
        var items = itemRepository.search("brick");
        assertThat(items, empty());
    }
}
