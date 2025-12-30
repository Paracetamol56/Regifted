package com.regifted.app.search;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.user.User;
import com.regifted.app.user.UserService;

@Service
public class SearchService {

  private final SearchRepository searchRepository;
  private final UserService userService;

  public SearchService(SearchRepository searchRepository, UserService userService) {
    this.searchRepository = searchRepository;
    this.userService = userService;
  }

  public Search createSearch(String query, User user) {

    // On recupere l'utilisateur au complet à partir du mail
    User userComplet = userService.getByEmail(user.getEmail());

    Search search = new Search();
    search.setUuid(UUID.randomUUID().toString());
    search.setUser(userComplet);
    search.setQuery(query);

    return searchRepository.save(search);
  }

  public Page<Search> getAllSearchesForUser(User user, Pageable pageable) {
    return searchRepository.findByUser(user, pageable);
  }

  public Search getSearchByUuid(String uuid, User user) {
    Search search = searchRepository.findById(uuid)
        .orElseThrow(() -> new NotFoundException(uuid));

    return search;
  }

  public void deleteSearch(String uuid, User user) {
    Search search = getSearchByUuid(uuid, user);
    searchRepository.delete(search);
  }

}
