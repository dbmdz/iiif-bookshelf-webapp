package de.digitalcollections.iiif.bookshelf.frontend.model;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * "Spring Data Page interface has many nice functions to get current page
 * number, get total pages, etc. But it’s still lack of ways to let me only
 * display partial page range of total pagination. So I created an adapter class
 * to wrap Sprng Data Page interface with additional features."
 *
 * @author ralf
 * @see
 * https://www.javacodegeeks.com/2013/03/implement-bootstrap-pagination-with-spring-data-and-thymeleaf.html
 */
public class PageWrapper<T> {

  public static final int MAX_PAGE_ITEM_DISPLAY = 5;
  private final Page<T> page;
  private final List<PageItem> items;
  private final int currentNumber;
  private final long totalItems;

  public long getTotalItems() {
    return totalItems;
  }
  private String url;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public PageWrapper(Page<T> page, String url) {
    this.page = page;
    this.totalItems = page.getTotalElements();
    this.url = url;
    items = new ArrayList<>();

    currentNumber = page.getNumber() + 1; //start from 1 to match page.page

    int start, size;
    if (page.getTotalPages() <= MAX_PAGE_ITEM_DISPLAY) {
      start = 1;
      size = page.getTotalPages();
    } else if (currentNumber <= MAX_PAGE_ITEM_DISPLAY - MAX_PAGE_ITEM_DISPLAY / 2) {
      start = 1;
      size = MAX_PAGE_ITEM_DISPLAY;
    } else if (currentNumber >= page.getTotalPages() - MAX_PAGE_ITEM_DISPLAY / 2) {
      start = page.getTotalPages() - MAX_PAGE_ITEM_DISPLAY + 1;
      size = MAX_PAGE_ITEM_DISPLAY;
    } else {
      start = currentNumber - MAX_PAGE_ITEM_DISPLAY / 2;
      size = MAX_PAGE_ITEM_DISPLAY;
    }

    for (int i = 0; i < size; i++) {
      items.add(new PageItem(start + i, (start + i) == currentNumber));
    }
  }

  public List<PageItem> getItems() {
    return items;
  }

  public int getNumber() {
    return currentNumber;
  }

  public List<T> getContent() {
    return page.getContent();
  }

  public int getSize() {
    return page.getSize();
  }

  public int getTotalPages() {
    return page.getTotalPages();
  }

  public boolean isFirstPage() {
    return page.isFirst();
  }

  public boolean isLastPage() {
    return page.isLast();
  }

  public boolean isHasPreviousPage() {
    return page.hasPrevious();
  }

  public boolean isHasNextPage() {
    return page.hasNext();

  }

  public class PageItem {

    private final int number;
    private final boolean current;

    public PageItem(int number, boolean current) {
      this.number = number;
      this.current = current;
    }

    public int getNumber() {
      return this.number;
    }

    public boolean isCurrent() {
      return this.current;
    }
  }
}
