package com.regifted.app.search;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.regifted.app.exception.NotFoundException;
import com.regifted.app.user.User;

@Service
public class SearchService {

  private final SearchRepository searchRepository;

  public SearchService(SearchRepository searchRepository) {
    this.searchRepository = searchRepository;
  }

  public Search createSearch(String query, User user) {
    Search search = new Search();
    search.setUuid(UUID.randomUUID().toString());
    search.setUser(user);
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
